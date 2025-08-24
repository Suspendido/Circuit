package xyz.kayaaa.xenon.shared.mongo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor @Getter @AllArgsConstructor
public class MongoCredentials {

    private final String hostname;
    private final int port;
    @Getter private String username;
    @Getter private String password;
    @Getter private String authSource;


    public static MongoCredentials getDefault() {
        return new MongoCredentials("localhost", 27017);
    }
}
