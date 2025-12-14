package com.sylluxpvp.circuit.shared.redis.packets.broadcast;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import com.sylluxpvp.circuit.shared.redis.RedisPacket;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class BroadcastPacket implements RedisPacket {

    private String serverFrom;
    private String message;

    @Override
    public String getID() {
        return "BROADCAST";
    }
}
