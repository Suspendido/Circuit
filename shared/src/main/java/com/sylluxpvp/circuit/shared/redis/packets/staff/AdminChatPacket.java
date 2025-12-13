package com.sylluxpvp.circuit.shared.redis.packets.staff;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import com.sylluxpvp.circuit.shared.redis.RedisPacket;

import java.util.UUID;

@AllArgsConstructor @NoArgsConstructor @Getter
public class AdminChatPacket implements RedisPacket {

    private UUID adminUUID;
    private String server;
    private String message;

    @Override
    public String getID() {
        return "ADMIN_CHAT";
    }
}
