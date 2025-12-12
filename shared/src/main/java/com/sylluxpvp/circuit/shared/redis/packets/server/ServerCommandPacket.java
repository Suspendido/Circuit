package com.sylluxpvp.circuit.shared.redis.packets.server;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import com.sylluxpvp.circuit.shared.redis.RedisPacket;

@NoArgsConstructor @AllArgsConstructor @Getter
public class ServerCommandPacket implements RedisPacket {

    private String server;
    private String command;

    @Override
    public String getID() {
        return "SERVER_COMMAND";
    }
}
