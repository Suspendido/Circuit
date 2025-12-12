package com.sylluxpvp.circuit.shared.redis.listener;

import com.sylluxpvp.circuit.shared.CircuitShared;
import com.sylluxpvp.circuit.shared.redis.packets.server.ServerUpdatePacket;
import com.sylluxpvp.circuit.shared.service.ServiceContainer;
import com.sylluxpvp.circuit.shared.service.impl.ServerService;

public class ServerUpdateListener extends PacketListener<ServerUpdatePacket> {

    @Override
    public void listen(ServerUpdatePacket packet) {
        String serverName = packet.getServerName();
        String serverType = packet.getServerType();
        if (serverName == null || serverType == null) {
            CircuitShared.getInstance().getLogger().warn("Received invalid server update packet!");
            return;
        }

        ServiceContainer.getService(ServerService.class).updateServer(packet);
    }

}
