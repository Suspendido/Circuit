package com.sylluxpvp.circuit.shared.redis.packets.tag;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import com.sylluxpvp.circuit.shared.redis.RedisPacket;

import java.util.UUID;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class TagUpdatePacket implements RedisPacket {

    private UUID tagUUID;
    private boolean deleted;

    @Override
    public String getID() {
        return "TAG_UPDATE";
    }
}
