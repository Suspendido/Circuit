package com.sylluxpvp.circuit.shared.service.impl;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.apache.commons.lang3.Validate;
import org.bson.Document;
import com.sylluxpvp.circuit.shared.CircuitShared;
import com.sylluxpvp.circuit.shared.cache.ProfileCache;
import com.sylluxpvp.circuit.shared.grant.Grant;
import com.sylluxpvp.circuit.shared.profile.Profile;
import com.sylluxpvp.circuit.shared.punishment.Punishment;
import com.sylluxpvp.circuit.shared.rank.Rank;
import com.sylluxpvp.circuit.shared.service.Service;
import com.sylluxpvp.circuit.shared.service.ServiceContainer;
import com.sylluxpvp.circuit.shared.tools.async.AsyncExecutor;
import com.sylluxpvp.circuit.shared.tools.java.CryptographyUtils;
import com.sylluxpvp.circuit.shared.tools.circuit.Serializable;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Getter @Setter
public class ProfileService extends Service {

    private Set<Profile> onlineProfiles;
    private MongoCollection<Document> profilesCollection;

    @Override @NonNull
    public String getIdentifier() {
        return "profile";
    }

    @Override
    public void enable() {
        this.onlineProfiles = ConcurrentHashMap.newKeySet();
        this.profilesCollection = CircuitShared.getInstance().getMongo().getDatabase().getCollection("profiles");
        AsyncExecutor.init();
    }

    @Override
    public void disable() {
        this.saveAllSync();
        ProfileCache.clear();
        this.onlineProfiles.clear();
        this.onlineProfiles = null;
        this.profilesCollection = null;
        AsyncExecutor.shutdown();
    }

    @Override
    public List<Class<? extends Service>> getDependencies() {
        return Arrays.asList(RankService.class, GrantService.class, PunishmentService.class);
    }

    public Profile find(UUID uuid) {
        Validate.notNull(uuid, "UUID cannot be null");
        Profile cached = ProfileCache.get(uuid);
        if (cached != null) return cached;
        return this.onlineProfiles.stream()
                .filter(profile -> profile.getUUID().equals(uuid))
                .findFirst()
                .orElse(null);
    }

    public Profile load(UUID uuid) {
        Validate.notNull(uuid, "UUID cannot be null");
        Profile memoryProfile = find(uuid);
        if (memoryProfile != null) {
            return memoryProfile;
        }

        Document doc = profilesCollection.find(Filters.eq("uuid", uuid.toString())).first();

        Profile profile;
        if (doc == null) {
            profile = create(uuid);
        } else {
            profile = fromDocument(doc);
        }

        this.onlineProfiles.add(profile);
        ProfileCache.putOnline(profile);

        return profile;
    }

    public CompletableFuture<Profile> loadAsync(UUID uuid) {
        Validate.notNull(uuid, "UUID cannot be null");
        Profile memoryProfile = find(uuid);
        if (memoryProfile != null) {
            return CompletableFuture.completedFuture(memoryProfile);
        }

        return AsyncExecutor.supplyAsync(() -> {
            Document doc = profilesCollection.find(Filters.eq("uuid", uuid.toString())).first();
            Profile profile;
            if (doc == null) {
                profile = create(uuid);
            } else {
                profile = fromDocument(doc);
            }
            this.onlineProfiles.add(profile);
            ProfileCache.putOnline(profile);
            return profile;
        });
    }

    public void save(Profile profile) {
        Validate.notNull(profile, "Profile cannot be null");
        AsyncExecutor.runAsync(() -> saveSync(profile));
    }

    public void saveSync(Profile profile) {
        Validate.notNull(profile, "Profile cannot be null");
        Document doc = profile.toDocument();
        profilesCollection.replaceOne(
                Filters.eq("uuid", profile.getUUID().toString()),
                doc,
                new ReplaceOptions().upsert(true)
        );
    }

    public Profile create(UUID uuid) {
        Validate.notNull(uuid, "UUID cannot be null");
        Profile profile = new Profile(uuid);
        profile.addGrant(ServiceContainer.getService(GrantService.class).getDefaultGrant());
        return profile;
    }

    public void delete(UUID uuid) {
        AsyncExecutor.runAsync(() -> {
            profilesCollection.deleteOne(Filters.eq("uuid", uuid.toString()));
        });
        onlineProfiles.removeIf(profile -> profile.getUUID().equals(uuid));
        ProfileCache.remove(uuid);
    }

    public void unload(UUID uuid) {
        Profile profile = find(uuid);
        if (profile != null) {
            save(profile);
            onlineProfiles.remove(profile);
        }
    }

    public void saveAllSync() {
        onlineProfiles.forEach(this::saveSync);
    }

    @SuppressWarnings("unchecked")
    public Profile fromDocument(Document doc) {
        Validate.notNull(doc, "Document cannot be null");
        UUID uuid = UUID.fromString(doc.getString("uuid"));
        String name = doc.getString("name");
        String color = doc.getString("color");
        String token = doc.getString("token");
        String address = CryptographyUtils.decrypt(doc.getString("address"), token);

        List<String> permissions = (List<String>) doc.get("permissions", List.class);
        GrantService grantService = ServiceContainer.getService(GrantService.class);

        List<Grant<Rank>> rankGrants = this.extractGrants(doc, "rankGrants", "rank", grantService);
        List<Grant<Punishment>> punishments = this.extractGrants(doc, "punishments", "punishment", grantService);
        int coins = doc.getInteger("coins", 0);

        String activeTagIdStr = doc.getString("activeTagId");
        UUID activeTagId = activeTagIdStr != null ? UUID.fromString(activeTagIdStr) : null;
        boolean vipStatus = doc.getBoolean("vipStatus", false);

        return new Profile(address, uuid, name, color, token, permissions, rankGrants, punishments, coins, activeTagId, vipStatus);
    }

    public Set<Profile> findFromAddress(Profile base) {
        Validate.notNull(base, "Profile cannot be null");
        Validate.notNull(base.getAddress(), "Address cannot be null");
        Set<Profile> candidates = new HashSet<>();

        for (Document doc : profilesCollection.find(Filters.eq("address", base.getAddress()))) {
            try {
                Profile profile = fromDocument(doc);
                if (profile == null || profile.getUUID().equals(base.getUUID())) continue;
                candidates.add(profile);
                ProfileCache.put(profile);
            } catch (Exception ignored) {}
        }
        return candidates;
    }

    @SuppressWarnings("unchecked")
    private <T extends Serializable> List<Grant<T>> extractGrants(Document doc, String key, String type, GrantService grantService) {
        Validate.notNull(doc, "Document cannot be null");
        List<Document> documents = (List<Document>) doc.get(key, List.class);
        if (documents == null) {
            List<Grant<T>> grants = new ArrayList<>();
            if (type.equalsIgnoreCase("rank")) {
                grants.add((Grant<T>) grantService.getDefaultGrant());
            }
            return grants;
        }
        List<Grant<T>> grants = new ArrayList<>();

        for (Document grantDoc : documents) {
            Grant<?> grant = grantService.fromDocument(grantDoc);
            if (grant != null && grant.getData().getType().equalsIgnoreCase(type)) {
                grants.add((Grant<T>) grant);
            }
        }
        return grants;
    }

    public Profile getProfileByName(String name) {
        Validate.notNull(name, "Name cannot be null");
        Validate.isTrue(!name.isEmpty(), "Name cannot be empty");

        Profile onlineProfile = onlineProfiles.stream()
                .filter(profile -> profile.getName() != null && profile.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);

        if (onlineProfile != null) {
            return onlineProfile;
        }

        Profile cachedProfile = ProfileCache.getByName(name);
        if (cachedProfile != null) {
            return cachedProfile;
        }

        try {
            Document doc = profilesCollection.find(
                    Filters.regex("name", "^" + name + "$", "i") // Case-insensitive regex
            ).first();

            if (doc == null) {
                return null;
            }

            Profile profile = fromDocument(doc);
            ProfileCache.put(profile);

            return profile;

        } catch (Exception e) {
            CircuitShared.getInstance().getLogger().log("Error fetching profile by name: " + name);
            e.printStackTrace();
            return null;
        }
    }

    public CompletableFuture<Profile> getProfileByNameAsync(String name) {
        Validate.notNull(name, "Name cannot be null");
        Validate.isTrue(!name.isEmpty(), "Name cannot be empty");

        Profile onlineProfile = onlineProfiles.stream()
                .filter(profile -> profile.getName() != null && profile.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);

        if (onlineProfile != null) {
            return CompletableFuture.completedFuture(onlineProfile);
        }

        Profile cachedProfile = ProfileCache.getByName(name);
        if (cachedProfile != null) {
            return CompletableFuture.completedFuture(cachedProfile);
        }

        return AsyncExecutor.supplyAsync(() -> {
            try {
                Document doc = profilesCollection.find(
                        Filters.regex("name", "^" + name + "$", "i")
                ).first();

                if (doc == null) {
                    return null;
                }

                Profile profile = fromDocument(doc);
                ProfileCache.put(profile);
                return profile;

            } catch (Exception e) {
                CircuitShared.getInstance().getLogger().log("Error fetching profile by name: " + name);
                e.printStackTrace();
                return null;
            }
        });
    }

    public boolean existsByName(String name) {
        Validate.notNull(name, "Name cannot be null");

        boolean existsInMemory = onlineProfiles.stream()
                .anyMatch(profile -> profile.getName() != null && profile.getName().equalsIgnoreCase(name));

        if (existsInMemory) {
            return true;
        }

        try {
            long count = profilesCollection.countDocuments(
                    Filters.regex("name", "^" + name + "$", "i")
            );
            return count > 0;
        } catch (Exception e) {
            CircuitShared.getInstance().getLogger().log("Error checking if profile exists: " + name);
            e.printStackTrace();
            return false;
        }
    }
}