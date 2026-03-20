package com.sylluxpvp.circuit.shared.service.impl;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.bson.Document;
import com.sylluxpvp.circuit.shared.CircuitShared;
import com.sylluxpvp.circuit.shared.rank.Rank;
import com.sylluxpvp.circuit.shared.service.Service;
import com.sylluxpvp.circuit.shared.tools.async.AsyncExecutor;
import com.sylluxpvp.circuit.shared.redis.packets.rank.RankUpdatePacket;

import com.sylluxpvp.circuit.shared.grant.Grant;
import com.sylluxpvp.circuit.shared.profile.Profile;
import com.sylluxpvp.circuit.shared.service.ServiceContainer;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

@Getter @Setter
public class RankService extends Service {

    private List<Rank> ranks;
    private Rank defaultRank;
    private MongoCollection<Document> ranksCollection;
    private Consumer<Rank> onRankUpdate;

    @Override @NonNull
    public String getIdentifier() {
        return "rank";
    }

    @Override
    public void enable() {
        this.ranks = new CopyOnWriteArrayList<>();
        this.ranksCollection = CircuitShared.getInstance().getMongo().getDatabase().getCollection("ranks");
        this.loadAll();
    }

    @Override
    public void disable() {
        try {
            this.saveAll();
        } catch (Exception e) {
            CircuitShared.getInstance().getLogger().warn("Could not save ranks during shutdown (MongoDB may be down): " + e.getMessage());
        }
        if (this.ranks != null) {
            this.ranks.clear();
            this.ranks = null;
        }
        this.defaultRank = null;
    }

    public Rank getDefaultRank() {
        return this.ranks.stream().filter(Rank::isDefaultRank).findFirst().orElse(defaultRank);
    }

    public void loadAll() {
        List<Document> allProfiles = ranksCollection.find().into(new ArrayList<>());

        for (Document doc : allProfiles) {
            Rank rank = fromDocument(doc);
            if (rank == null) continue;
            this.ranks.add(rank);
            if (rank.isDefaultRank() && defaultRank == null) {
                this.defaultRank = rank;
            }
        }
        if (defaultRank == null) {
            this.defaultRank = new Rank(UUID.randomUUID(), "Default");
            this.defaultRank.setDefaultRank(true);
            this.defaultRank.setPrefix("&7");
            this.defaultRank.setSuffix("");
            this.defaultRank.setColor("&7");
            this.ranks.add(this.defaultRank);
        }
    }

    public void saveAll() {
        this.ranks.forEach(this::saveSync);
    }

    public Rank getRank(UUID uuid) {
        if (uuid == null) return null;
        return this.ranks.stream().filter(rank -> rank.getUuid().equals(uuid)).findFirst().orElse(null);
    }

    public Rank getRank(String name) {
        if (name == null || name.isEmpty()) return null;
        return this.ranks.stream().filter(rank -> rank.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    public void notifyRankUpdate(Rank rank) {
        if (onRankUpdate != null) onRankUpdate.accept(rank);
    }

    public Rank create(String name) {
        Rank rank = new Rank(java.util.UUID.randomUUID(), name);
        rank.setPrefix("&7");
        rank.setSuffix("");
        rank.setColor("&7");
        rank.setDefaultRank(false);
        rank.setStaff(false);
        rank.setPurchasable(false);
        rank.setHidden(false);
        rank.setWeight(0);
        rank.setPermissions(new ArrayList<>());
        this.ranks.add(rank);
        return rank;
    }

    public void save(Rank rank) {
        AsyncExecutor.runAsync(() -> {
            saveSync(rank);
            CircuitShared.getInstance().getRedis().sendPacket(new RankUpdatePacket(rank.getUuid(), false));
        });
    }

    public List<String> getAllPermissions(Rank rank) {
        List<String> allPerms = new ArrayList<>(rank.getPermissions());
        for (String inheritanceName : rank.getInheritances()) {
            Rank inherited = getRank(inheritanceName);
            if (inherited == null || inherited.equals(rank)) continue;
            allPerms.addAll(getAllPermissions(inherited));
        }
        return allPerms;
    }

    public void saveSync(Rank rank) {
        Document doc = rank.toDocument();
        ranksCollection.replaceOne(
                Filters.eq("uuid", rank.getUuid().toString()),
                doc,
                new ReplaceOptions().upsert(true)
        );
    }

    public void delete(Rank rank) {
        if (rank == null || !this.ranks.contains(rank)) return;
        this.ranks.remove(rank);
        
        ProfileService profileService = ServiceContainer.getService(ProfileService.class);
        if (profileService != null && profileService.getOnlineProfiles() != null) {
            for (Profile profile : profileService.getOnlineProfiles()) {
                if (profile.hasRank(rank)) {
                    Grant<Rank> grant = profile.findByRank(rank);
                    if (grant != null) {
                        profile.removeGrant(grant);
                        profileService.save(profile);
                    }
                }
            }
        }

        AsyncExecutor.runAsync(() -> {
            ranksCollection.deleteOne(Filters.eq("uuid", rank.getUuid().toString()));
            CircuitShared.getInstance().getRedis().sendPacket(new RankUpdatePacket(rank.getUuid(), true));
        });
    }

    public Rank fromDocument(Document doc) {
        Rank rank = new Rank(java.util.UUID.fromString(doc.getString("uuid")), doc.getString("name"));
        rank.setPrefix(doc.getString("prefix"));
        rank.setSuffix(doc.getString("suffix"));
        rank.setColor(doc.getString("color"));
        rank.setDefaultRank(doc.getBoolean("default"));
        rank.setStaff(doc.getBoolean("staff"));
        rank.setPurchasable(doc.getBoolean("purchasable"));
        rank.setHidden(doc.getBoolean("hidden"));
        rank.setWeight(doc.getInteger("weight"));
        try {
            rank.setPermissions(doc.getList("permissions", String.class));
        } catch (Exception e) {
            rank.setPermissions(new ArrayList<>());
        }
        try {
            rank.setInheritances(doc.getList("inheritances", String.class));
        } catch (Exception e) {
            rank.setInheritances(new ArrayList<>());
        }
        return rank;
    }
}
