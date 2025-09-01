package xyz.kayaaa.xenon.shared.redis.packets.misc;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import xyz.kayaaa.xenon.shared.redis.RedisPacket;

@AllArgsConstructor @NoArgsConstructor @Getter
public class MessagePacket implements RedisPacket {

    private String server;
    private String message;

    @Override
    public String getID() {
        return "TEXT_MESSAGE";
    }

}
