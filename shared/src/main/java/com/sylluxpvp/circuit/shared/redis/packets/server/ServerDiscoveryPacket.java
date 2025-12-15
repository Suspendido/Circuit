package com.sylluxpvp.circuit.shared.redis.packets.server;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import com.sylluxpvp.circuit.shared.redis.RedisPacket;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class ServerDiscoveryPacket implements RedisPacket {

    private String requestingServer;

    @Override
    public String getID() {
        return "SERVER_DISCOVERY";
    }
}
