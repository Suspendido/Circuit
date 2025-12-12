package com.sylluxpvp.circuit.shared.credentials;

import lombok.Getter;

@Getter
public class RedisCredentials {

    private final String hostname;
    private final int port;
    private String password;

    public RedisCredentials(String hostname, int port) {
        this.hostname = hostname;
        this.port = port;
    }

    public RedisCredentials(String hostname, int port, String password) {
        this.hostname = hostname;
        this.port = port;
        this.password = password;
    }

    public static RedisCredentials getDefault() {
        return new RedisCredentials("localhost", 6379);
    }

}
