package com.sylluxpvp.circuit.bukkit.command.queue;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Default;
import com.sylluxpvp.circuit.bukkit.CircuitPlugin;
import com.sylluxpvp.circuit.shared.redis.packets.queue.QueueLeavePacket;
import com.sylluxpvp.circuit.shared.service.ServiceContainer;
import com.sylluxpvp.circuit.shared.service.impl.QueueService;
import com.sylluxpvp.circuit.shared.tools.string.CC;
import org.bukkit.entity.Player;

@CommandAlias("leavequeue|lq")
public class LeaveQueueCommand extends BaseCommand {

    @Default
    public void onLeaveQueue(Player player) {
        QueueService queueService = ServiceContainer.getService(QueueService.class);
        
        // Check cooldown
        if (queueService.isOnCooldown(player.getUniqueId())) {
            long remaining = queueService.getRemainingCooldown(player.getUniqueId()) / 1000;
            player.sendMessage(CC.translate("&cPlease wait " + (remaining + 1) + " seconds."));
            return;
        }
        
        String queueName = queueService.getPlayerQueueName(player.getUniqueId());
        if (queueName == null) {
            player.sendMessage(CC.translate("&cYou are not in any queue!"));
            return;
        }
        
        // Send leave packet
        CircuitPlugin.getInstance().getShared().getRedis().sendPacket(
                new QueueLeavePacket(player.getUniqueId())
        );
        
        // Set cooldown
        queueService.setCooldown(player.getUniqueId());
        
        player.sendMessage(CC.translate("&aYou have left the queue for &f" + queueName + "&a."));
    }
}
