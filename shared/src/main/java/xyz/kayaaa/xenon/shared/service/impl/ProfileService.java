package xyz.kayaaa.xenon.shared.service.impl;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import lombok.Getter;
import lombok.NonNull;
import org.bson.Document;
import xyz.kayaaa.xenon.shared.XenonShared;
import xyz.kayaaa.xenon.shared.grant.Grant;
import xyz.kayaaa.xenon.shared.profile.Profile;
import xyz.kayaaa.xenon.shared.service.Service;
import xyz.kayaaa.xenon.shared.service.ServiceContainer;

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
        return this.profiles.stream().filter(profile -> profile.getUUID().equals(uuid)).findFirst().orElse(null);
    }

    public Profile load(UUID uuid) {
        Profile memoryProfile = find(uuid);
        if (memoryProfile != null) {
            return memoryProfile;
        }

        Document doc = profilesCollection.find(Filters.eq("uuid", uuid.toString())).first();

        Profile profile;
        if (doc == null) {
            XenonShared.getInstance().getLogger().warn(true, "Couldn't locate profile with uuid: " + uuid.toString());
            profile = create(uuid);
            save(profile);
        } else {
            profile = fromDocument(doc);
        }

        if (!this.profiles.contains(profile)) {
            this.profiles.add(profile);
        }
        return profile;
    }

    public Profile load(String token) {
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
        Document doc = profile.toDocument();

        profilesCollection.replaceOne(
                Filters.eq("uuid", profile.getUUID().toString()),
                doc,
                new ReplaceOptions().upsert(true)
        );
    }

    public Profile create(UUID uuid) {
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
            if (profile != null) {
                this.profiles.add(profile);
            }
        }

        this.print("&aLoaded " + profiles.size() + " profiles from MongoDB");
    }

    public void saveAll() {
        profiles.forEach(this::save);
        this.print("&aSaved " + profiles.size() + " profiles to MongoDB");
    }

    public Profile fromDocument(Document doc) {
        UUID uuid = UUID.fromString(doc.getString("uuid"));
        String name = doc.getString("name");
        String color = doc.getString("color");
        String token = doc.getString("token");

        List<Grant<?>> grants = new ArrayList<>();
        @SuppressWarnings("unchecked") List<String> permissions = (List<String>) doc.get("permissions", List.class);
        @SuppressWarnings("unchecked") List<Document> grantsDocuments = (List<Document>) doc.get("grants", List.class);

        if (grantsDocuments != null) {
            GrantService grantService = ServiceContainer.getService(GrantService.class);
            for (Document grantDoc : grantsDocuments) {
                Grant<?> grant = grantService.fromDocument(grantDoc);
                if (grant != null) grants.add(grant);
            }
        }

        return new Profile(uuid, name, color, token, permissions, grants);
    }
}