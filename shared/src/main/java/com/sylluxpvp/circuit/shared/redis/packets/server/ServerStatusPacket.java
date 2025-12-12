package com.sylluxpvp.circuit.shared.redis.packets.server;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import com.sylluxpvp.circuit.shared.redis.RedisPacket;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class ServerStatusPacket implements RedisPacket {
    private String serverName;
    private boolean online;
    private boolean whitelisted;

    @Override
    public String getID() {
        return "SERVER_STATUS";
    }
}
