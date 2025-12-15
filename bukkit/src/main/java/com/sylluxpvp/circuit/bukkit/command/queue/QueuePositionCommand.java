package com.sylluxpvp.circuit.bukkit.command.queue;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Default;
import com.sylluxpvp.circuit.shared.queue.Queue;
import com.sylluxpvp.circuit.shared.service.ServiceContainer;
import com.sylluxpvp.circuit.shared.service.impl.QueueService;
import com.sylluxpvp.circuit.shared.tools.string.CC;
import org.bukkit.entity.Player;

@CommandAlias("queueposition|qpos|position")
public class QueuePositionCommand extends BaseCommand {

    @Default
    public void onQueuePosition(Player player) {
        QueueService queueService = ServiceContainer.getService(QueueService.class);
        
        String queueName = queueService.getPlayerQueueName(player.getUniqueId());
        if (queueName == null) {
            player.sendMessage(CC.translate("&cYou are not in any queue!"));
            return;
        }
        
        Queue queue = queueService.getQueue(queueName);
        if (queue == null) {
            player.sendMessage(CC.translate("&cQueue not found."));
            return;
        }
        
        int position = queue.getPosition(player.getUniqueId());
        int total = queue.size();
        
        player.sendMessage(CC.translate("&9[Queue] &7You are position &f#" + position + " &7of &f" + total + " &7in queue for &f" + queueName + "&7."));
    }
}
