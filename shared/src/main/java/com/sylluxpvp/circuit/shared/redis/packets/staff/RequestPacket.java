package com.sylluxpvp.circuit.shared.redis.packets.staff;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import com.sylluxpvp.circuit.shared.redis.RedisPacket;

import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class RequestPacket implements RedisPacket {

    private UUID playerUUID;
    private String playerName;
    private String playerColor;
    private String serverName;
    private String message;

    @Override
    public String getID() {
        return "REQUEST";
    }
}
