package com.sylluxpvp.circuit.bukkit.command.queue;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import com.sylluxpvp.circuit.bukkit.CircuitPlugin;
import com.sylluxpvp.circuit.shared.profile.Profile;
import com.sylluxpvp.circuit.shared.queue.Queue;
import com.sylluxpvp.circuit.shared.redis.packets.queue.QueueJoinPacket;
import com.sylluxpvp.circuit.shared.server.Server;
import com.sylluxpvp.circuit.shared.service.ServiceContainer;
import com.sylluxpvp.circuit.shared.service.impl.ProfileService;
import com.sylluxpvp.circuit.shared.service.impl.QueueService;
import com.sylluxpvp.circuit.shared.service.impl.ServerService;
import com.sylluxpvp.circuit.shared.tools.string.CC;
import org.bukkit.entity.Player;

import java.util.Optional;

@CommandAlias("joinqueue|jq")
public class QueueCommand extends BaseCommand {

    @Default
    @Syntax("<queue>")
    @CommandCompletion("@queues")
    public void onQueue(Player player, String queueName) {
        QueueService queueService = ServiceContainer.getService(QueueService.class);
        ServerService serverService = ServiceContainer.getService(ServerService.class);
        
        // Check cooldown
        if (queueService.isOnCooldown(player.getUniqueId())) {
            long remaining = queueService.getRemainingCooldown(player.getUniqueId()) / 1000;
            player.sendMessage(CC.translate("&cPlease wait " + (remaining + 1) + " seconds before joining a queue."));
            return;
        }
        
        Queue queue = queueService.getQueue(queueName);
        if (queue == null) {
            player.sendMessage(CC.translate("&cQueue not found: " + queueName));
            return;
        }
        
        if (!queue.isEnabled()) {
            player.sendMessage(CC.translate("&cThis queue is currently disabled."));
            return;
        }
        
        String currentServer = CircuitPlugin.getInstance().getShared().getServer().getName();
        if (queue.getServerName().equalsIgnoreCase(currentServer)) {
            player.sendMessage(CC.translate("&cYou are already connected to this server!"));
            return;
        }
        
        // Check if already in a queue
        String existingQueue = queueService.getPlayerQueueName(player.getUniqueId());
        if (existingQueue != null) {
            player.sendMessage(CC.translate("&cYou are already in the queue for &f" + existingQueue + "&c!"));
            player.sendMessage(CC.translate("&cUse &f/leavequeue &cto leave your current queue."));
            return;
        }
        
        // Check if target server is online (optional - queue can still accept players)
        Optional<Server> serverOpt = serverService.find(queue.getServerName());
        if (serverOpt.isPresent() && !serverOpt.get().isOnline()) {
            player.sendMessage(CC.translate("&eWarning: The target server appears to be offline."));
            player.sendMessage(CC.translate("&eYou will be sent when it comes online."));
        }
        
        // Get player priority based on rank weight (higher weight = higher priority = lower number)
        Profile profile = ServiceContainer.getService(ProfileService.class).find(player.getUniqueId());
        int priority = 100;
        if (profile != null && profile.getCurrentGrant() != null && profile.getCurrentGrant().getData() != null) {
            priority = 100 - profile.getCurrentGrant().getData().getWeight();
            if (priority < 0) priority = 0;
        }
        
        // Bypass queue if player has permission and server has room
        if (player.hasPermission("circuit.queue.bypass") && serverOpt.isPresent()) {
            Server server = serverOpt.get();
            if (server.isOnline() && server.getPlayers() < server.getMax()) {
                com.sylluxpvp.circuit.bukkit.tools.spigot.BungeeUtils.sendToServer(player, server.getName());
                player.sendMessage(CC.translate("&aSending you to &f" + server.getName() + "&a..."));
                return;
            }
        }
        
        // Send queue join packet
        CircuitPlugin.getInstance().getShared().getRedis().sendPacket(
                new QueueJoinPacket(player.getUniqueId(), player.getName(), queue.getServerName(), priority)
        );
        
        // Set cooldown
        queueService.setCooldown(player.getUniqueId());
        
        player.sendMessage(CC.translate("&aYou have joined the queue for &f" + queue.getServerName() + "&a."));
        
        if (queue.isPaused()) {
            player.sendMessage(CC.translate("&eThe queue is currently paused. You will be sent when it resumes."));
        }
        
        int position = queue.getPosition(player.getUniqueId());
        if (position > 0) {
            player.sendMessage(CC.translate("&7You are position &f#" + position + " &7in the queue."));
        }
    }
}
