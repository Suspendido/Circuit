package com.sylluxpvp.circuit.shared.redis.packets.misc;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import com.sylluxpvp.circuit.shared.redis.RedisPacket;

@AllArgsConstructor @NoArgsConstructor @Getter
public class MessagePacket implements RedisPacket {

    private String server;
    private String message;
    private boolean staffOnly;

    @Override
    public String getID() {
        return "TEXT_MESSAGE";
    }

}
