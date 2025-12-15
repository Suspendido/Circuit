package com.sylluxpvp.circuit.shared.redis.packets.vip;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import com.sylluxpvp.circuit.shared.redis.RedisPacket;

import java.util.UUID;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class VIPUpdatePacket implements RedisPacket {

    private UUID playerUUID;
    private boolean vipStatus;

    @Override
    public String getID() {
        return "VIP_UPDATE";
    }
}
