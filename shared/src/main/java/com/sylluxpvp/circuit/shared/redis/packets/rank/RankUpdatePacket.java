package com.sylluxpvp.circuit.shared.redis.packets.rank;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import com.sylluxpvp.circuit.shared.redis.RedisPacket;

import java.util.UUID;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class RankUpdatePacket implements RedisPacket {

    private UUID rankUUID;
    private boolean deleted;

    @Override
    public String getID() {
        return "RANK_UPDATE";
    }
}
