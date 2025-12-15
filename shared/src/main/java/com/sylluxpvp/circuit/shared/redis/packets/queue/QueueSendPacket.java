package com.sylluxpvp.circuit.shared.redis.packets.queue;

import com.sylluxpvp.circuit.shared.redis.RedisPacket;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class QueueSendPacket implements RedisPacket {

    private UUID playerUUID;
    private String targetServer;

    @Override
    public String getID() {
        return "QUEUE_SEND";
    }
}
