package com.sylluxpvp.circuit.shared.credentials;

import lombok.Getter;

@Getter
public class MongoCredentials {

    private final String hostname;
    private final int port;
    private String username;
    private String password;
    private String authSource;

    public MongoCredentials(String hostname, int port) {
        this.hostname = hostname;
        this.port = port;
    }

    public MongoCredentials(String hostname, int port, String username, String password, String authSource) {
        this.hostname = hostname;
        this.port = port;
        this.username = username;
        this.password = password;
        this.authSource = authSource;
    }


    public static MongoCredentials getDefault() {
        return new MongoCredentials("localhost", 27017);
    }
}
