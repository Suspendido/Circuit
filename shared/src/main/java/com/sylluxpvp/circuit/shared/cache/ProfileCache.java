package com.sylluxpvp.circuit.shared.cache;

import lombok.experimental.UtilityClass;
import com.sylluxpvp.circuit.shared.profile.Profile;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@UtilityClass
public class ProfileCache {

    private static final long DEFAULT_TTL = 5 * 60 * 1000L; // 5 minutes
    private static final long ONLINE_TTL = 60 * 60 * 1000L; // 1 hour for online players
    private static final Map<UUID, CacheEntry<Profile>> cache = new ConcurrentHashMap<>();
    private static final Map<String, UUID> nameToUuidCache = new ConcurrentHashMap<>();

    public void put(Profile profile) {
        put(profile, DEFAULT_TTL);
    }

    public void put(Profile profile, long ttlMillis) {
        if (profile == null || profile.getUUID() == null) return;

        cache.put(profile.getUUID(), CacheEntry.of(profile, ttlMillis));
        nameToUuidCache.put(profile.getName().toLowerCase(), profile.getUUID());
    }

    public void putOnline(Profile profile) {
        put(profile, ONLINE_TTL);
    }

    public Profile get(UUID uuid) {
        if (uuid == null) return null;

        CacheEntry<Profile> entry = cache.get(uuid);
        if (entry == null) return null;

        if (entry.isExpired()) {
            remove(uuid);
            return null;
        }

        return entry.getValue();
    }

    public Profile getByName(String name) {
        if (name == null || name.isEmpty()) return null;

        UUID uuid = nameToUuidCache.get(name.toLowerCase());
        if (uuid != null) {
            return get(uuid);
        }

        for (Map.Entry<UUID, CacheEntry<Profile>> entry : cache.entrySet()) {
            CacheEntry<Profile> cacheEntry = entry.getValue();

            if (cacheEntry.isExpired()) {
                remove(entry.getKey());
                continue;
            }

            Profile profile = cacheEntry.getValue();
            if (profile.getName() != null && profile.getName().equalsIgnoreCase(name)) {
                nameToUuidCache.put(name.toLowerCase(), profile.getUUID());
                return profile;
            }
        }

        return null;
    }

    public void remove(UUID uuid) {
        CacheEntry<Profile> removed = cache.remove(uuid);
        if (removed != null && removed.getValue().getName() != null) {
            nameToUuidCache.remove(removed.getValue().getName().toLowerCase());
        }
    }

    public void clear() {
        cache.clear();
        nameToUuidCache.clear();
    }

    public void cleanup() {
        cache.entrySet().removeIf(entry -> {
            if (entry.getValue().isExpired()) {
                Profile profile = entry.getValue().getValue();
                if (profile.getName() != null) {
                    nameToUuidCache.remove(profile.getName().toLowerCase());
                }
                return true;
            }
            return false;
        });
    }

    public int size() {
        return cache.size();
    }

    public boolean contains(UUID uuid) {
        return get(uuid) != null;
    }
}
