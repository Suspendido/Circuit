package xyz.kayaaa.xenon.shared.redis;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter @RequiredArgsConstructor @AllArgsConstructor
public class RedisCredentials {

    private final String hostname;
    private final int port;
    @Getter private String password;

    public static RedisCredentials getDefault() {
        return new RedisCredentials("localhost", 6379);
    }

}
