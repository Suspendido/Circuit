package com.sylluxpvp.circuit.shared.redis.packets.staff;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import com.sylluxpvp.circuit.shared.redis.RedisPacket;

import java.util.UUID;

@AllArgsConstructor @NoArgsConstructor @Getter
public class StaffStatusPacket implements RedisPacket {

    private UUID staffUUID;
    private boolean joined;
    private String serverName;

    @Override
    public String getID() {
        return "STAFF_STATUS";
    }
}
