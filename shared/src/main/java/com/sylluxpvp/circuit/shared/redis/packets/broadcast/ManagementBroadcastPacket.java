package com.sylluxpvp.circuit.shared.redis.packets.broadcast;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import com.sylluxpvp.circuit.shared.redis.RedisPacket;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class ManagementBroadcastPacket implements RedisPacket {

    private String serverFrom;
    private String message;
    private String permission;

    public ManagementBroadcastPacket(String serverFrom, String message) {
        this.serverFrom = serverFrom;
        this.message = message;
        this.permission = "circuit.admin";
    }

    @Override
    public String getID() {
        return "MANAGEMENT_BROADCAST";
    }
}
