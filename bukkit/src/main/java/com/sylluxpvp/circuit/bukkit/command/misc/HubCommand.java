package com.sylluxpvp.circuit.bukkit.command.misc;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Default;
import com.sylluxpvp.circuit.bukkit.CircuitPlugin;
import com.sylluxpvp.circuit.bukkit.tools.spigot.BungeeUtils;
import com.sylluxpvp.circuit.shared.server.Server;
import com.sylluxpvp.circuit.shared.service.ServiceContainer;
import com.sylluxpvp.circuit.shared.service.impl.ServerService;
import com.sylluxpvp.circuit.shared.tools.string.CC;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.util.Optional;

@CommandAlias("hub|lobby")
public class HubCommand extends BaseCommand {

    @Default
    public void onHub(Player player) {
        YamlConfiguration config = CircuitPlugin.getInstance().getMainConfig();
        String hubServer = config.getString("queue-manager.hub-server", "hub");
        String currentServer = CircuitPlugin.getInstance().getShared().getServer().getName();
        
        if (currentServer.equalsIgnoreCase(hubServer)) {
            player.sendMessage(CC.translate("&cYa estas en el hub!"));
            return;
        }
        
        ServerService serverService = ServiceContainer.getService(ServerService.class);
        Optional<Server> serverOpt = serverService.find(hubServer);
        
        if (!serverOpt.isPresent() || !serverOpt.get().isOnline()) {
            player.sendMessage(CC.translate("&cEl hub se encuentra inaccesible actualmente."));
            return;
        }
        
        BungeeUtils.sendToServer(player, hubServer);
        player.sendMessage(CC.translate("&aMandandote al hub..."));
    }
}
