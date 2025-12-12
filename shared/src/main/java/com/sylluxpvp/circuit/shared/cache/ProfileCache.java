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

    public void put(Profile profile) {
        put(profile, DEFAULT_TTL);
    }

    public void put(Profile profile, long ttlMillis) {
        if (profile == null) return;
        cache.put(profile.getUUID(), CacheEntry.of(profile, ttlMillis));
    }

    public void putOnline(Profile profile) {
        put(profile, ONLINE_TTL);
    }

    public Profile get(UUID uuid) {
        CacheEntry<Profile> entry = cache.get(uuid);
        if (entry == null) return null;
        if (entry.isExpired()) {
            cache.remove(uuid);
            return null;
        }
        return entry.getValue();
    }

    public void remove(UUID uuid) {
        cache.remove(uuid);
    }

    public void clear() {
        cache.clear();
    }

    public void cleanup() {
        cache.entrySet().removeIf(entry -> entry.getValue().isExpired());
    }

    public int size() {
        return cache.size();
    }

    public boolean contains(UUID uuid) {
        return get(uuid) != null;
    }
}
