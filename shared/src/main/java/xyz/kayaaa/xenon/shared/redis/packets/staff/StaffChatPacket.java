package xyz.kayaaa.xenon.shared.redis.packets.staff;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import xyz.kayaaa.xenon.shared.redis.RedisPacket;

import java.util.UUID;

@AllArgsConstructor @NoArgsConstructor @Getter
public class StaffChatPacket implements RedisPacket {

    private UUID staffUUID;
    private String server;
    private String message;

    @Override
    public String getID() {
        return "STAFF_CHAT";
    }
}
