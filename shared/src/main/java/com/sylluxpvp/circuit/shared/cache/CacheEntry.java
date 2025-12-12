package com.sylluxpvp.circuit.shared.cache;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class CacheEntry<T> {

    private final T value;
    private final long expiresAt;

    public boolean isExpired() {
        return System.currentTimeMillis() > expiresAt;
    }

    public static <T> CacheEntry<T> of(T value, long ttlMillis) {
        return new CacheEntry<>(value, System.currentTimeMillis() + ttlMillis);
    }
}
