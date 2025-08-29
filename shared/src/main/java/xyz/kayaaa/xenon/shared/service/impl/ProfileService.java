package xyz.kayaaa.xenon.shared.service.impl;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import lombok.Getter;
import lombok.NonNull;
import org.apache.commons.lang3.Validate;
import org.bson.Document;
import xyz.kayaaa.xenon.shared.XenonShared;
import xyz.kayaaa.xenon.shared.grant.Grant;
import xyz.kayaaa.xenon.shared.profile.Profile;
import xyz.kayaaa.xenon.shared.punishment.Punishment;
import xyz.kayaaa.xenon.shared.rank.Rank;
import xyz.kayaaa.xenon.shared.service.Service;
import xyz.kayaaa.xenon.shared.service.ServiceContainer;
import xyz.kayaaa.xenon.shared.tools.java.CryptographyUtils;
import xyz.kayaaa.xenon.shared.tools.xenon.Serializable;

import java.util.*;

@Getter
public class ProfileService extends Service {

    private List<Profile> profiles;
    private MongoCollection<Document> profilesCollection;

    @Override @NonNull
    public String getIdentifier() {
        return "profile";
    }

    @Override
    public void enable() {
        this.profiles = new ArrayList<>();
        this.profilesCollection = XenonShared.getInstance().getMongo().getDatabase().getCollection("profiles");
    }

    @Override
    public void disable() {
        this.saveAll();
        this.profiles.clear();
        this.profiles = null;
        this.profilesCollection = null;
    }

    public Profile find(UUID uuid) {
        Validate.notNull(uuid, "UUID cannot be null");
        return this.profiles.stream().filter(profile -> profile.getUUID().equals(uuid)).findFirst().orElse(null);
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
            XenonShared.getInstance().getLogger().warn("Couldn't locate profile with uuid: " + uuid);
            profile = create(uuid);
        } else {
            profile = fromDocument(doc);
        }

        if (!this.profiles.contains(profile)) {
            this.profiles.add(profile);
        }
        return profile;
    }

    public Profile load(String token) {
        Validate.notNull(token, "Profile token cannot be null");
        Profile memoryProfile = this.profiles.stream()
                .filter(profile -> profile.getToken().equalsIgnoreCase(token))
                .findFirst()
                .orElse(null);

        if (memoryProfile != null) {
            return memoryProfile;
        }

        Document doc = profilesCollection.find(Filters.eq("token", token)).first();

        if (doc != null) {
            Profile profile = fromDocument(doc);
            if (!this.profiles.contains(profile)) {
                this.profiles.add(profile);
            }
            return profile;
        }

        return null;
    }

    public void save(Profile profile) {
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
        profilesCollection.deleteOne(Filters.eq("uuid", uuid.toString()));
        profiles.removeIf(profile -> profile.getUUID().equals(uuid));
    }

    public void loadAll() {
        this.profiles.clear();
        List<Document> allProfiles = profilesCollection.find().into(new ArrayList<>());

        for (Document doc : allProfiles) {
            Profile profile = fromDocument(doc);
            if (profile == null) continue;

            this.profiles.add(profile);
        }

        this.print("&aLoaded " + profiles.size() + " profiles from MongoDB");
    }

    public void saveAll() {
        profiles.forEach(this::save);
        this.print("&aSaved " + profiles.size() + " profiles to MongoDB");
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

        return new Profile(address, uuid, name, color, token, permissions, rankGrants, punishments);
    }

    public List<Profile> findFromAddress(Profile base) {
        Validate.notNull(base, "Profile cannot be null");
        Validate.notNull(base.getAddress(), "Address cannot be null");
        List<Profile> candidates = new ArrayList<>();
        List<Document> allProfiles = profilesCollection.find().into(new ArrayList<>());

        for (Document doc : allProfiles) {
            Profile profile = fromDocument(doc);
            if (profile != null && profile != base && profile.getAddress().equalsIgnoreCase(base.getAddress())) {
                candidates.add(profile);
            }
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
}