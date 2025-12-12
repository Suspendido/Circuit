package com.sylluxpvp.circuit.shared.redis;

import lombok.experimental.UtilityClass;
import com.sylluxpvp.circuit.shared.CircuitShared;
import com.sylluxpvp.circuit.shared.tools.java.ClassUtils;

import java.util.HashMap;
import java.util.Map;

@UtilityClass
public class RedisPacketRegistry {

    Map<String, Class<? extends RedisPacket>> PACKETS = new HashMap<>();

    public void loadPackets() {
        ClassUtils.getClasses(
                        CircuitShared.getInstance().getFile(),
                        RedisPacket.class.getPackage().getName() + ".packets"
                )
                .stream()
                .filter(RedisPacket.class::isAssignableFrom)
                .forEach(clazz -> {
                    try {
                        RedisPacket packet = (RedisPacket) clazz.getDeclaredConstructor().newInstance();
                        register(packet.getID(), (Class<? extends RedisPacket>) clazz);
                    } catch (Exception e) {
                        CircuitShared.getInstance().getLogger()
                                .log(false, "Falha ao registrar packet: " + clazz.getName() + " - " + e.getMessage());
                    }
                });
    }

    public void register(String id, Class<? extends RedisPacket> clazz) {
        PACKETS.put(id, clazz);
    }

    public Class<? extends RedisPacket> get(String id) {
        return PACKETS.get(id);
    }

}
