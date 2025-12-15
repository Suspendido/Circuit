package com.sylluxpvp.circuit.shared.redis.packets.queue;

import com.sylluxpvp.circuit.shared.redis.RedisPacket;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class QueuePositionPacket implements RedisPacket {

    private UUID playerUUID;
    private String queueName;
    private int position;
    private int total;
    private int estimatedSeconds;

    @Override
    public String getID() {
        return "QUEUE_POSITION";
    }
}
