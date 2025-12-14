package com.sylluxpvp.circuit.shared.redis.packets.staff;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import com.sylluxpvp.circuit.shared.redis.RedisPacket;

import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class ReportPacket implements RedisPacket {

    private UUID reporterUUID;
    private String reporterName;
    private String reporterColor;
    private String targetName;
    private String targetColor;
    private String serverName;
    private String reason;

    @Override
    public String getID() {
        return "REPORT";
    }
}
