package com.sylluxpvp.circuit.bukkit.hook.impl;

import com.lunarclient.apollo.Apollo;
import com.lunarclient.apollo.module.ApolloModuleManager;
import com.lunarclient.apollo.module.richpresence.RichPresenceModule;
import com.lunarclient.apollo.module.richpresence.ServerRichPresence;
import com.lunarclient.apollo.player.ApolloPlayerManager;
import com.sylluxpvp.circuit.bukkit.hook.ClientHook;
import com.sylluxpvp.circuit.shared.server.Server;
import com.sylluxpvp.circuit.shared.service.ServiceContainer;
import com.sylluxpvp.circuit.shared.service.impl.ServerService;
import org.bukkit.entity.Player;

import java.util.List;

public class LunarClient implements ClientHook {

    private final ApolloPlayerManager playerManager = Apollo.getPlayerManager();
    private final RichPresenceModule richPresenceModule;

    public LunarClient() {
        ApolloModuleManager moduleManager = Apollo.getModuleManager();
        this.richPresenceModule = moduleManager.getModule(RichPresenceModule.class);
    }

    @Override
    public void overrideServerRichPresence(Player player) {
        ServerService serverService = ServiceContainer.getService(ServerService.class);

        if (serverService == null) return;

        List<Server> servers = serverService.getServers();
        if (servers == null || servers.isEmpty()) return;

        playerManager.getPlayer(player.getUniqueId()).ifPresent((apolloPlayer) ->
                richPresenceModule.overrideServerRichPresence(apolloPlayer,
                        ServerRichPresence.builder()
                                .gameName(servers.getFirst().getName())
                                .playerState("Playing")
                                .build()
                )
        );
    }
}
