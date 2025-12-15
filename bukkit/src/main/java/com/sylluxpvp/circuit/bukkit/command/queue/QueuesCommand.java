package com.sylluxpvp.circuit.bukkit.command.queue;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Subcommand;
import com.sylluxpvp.circuit.shared.queue.Queue;
import com.sylluxpvp.circuit.shared.server.Server;
import com.sylluxpvp.circuit.shared.service.ServiceContainer;
import com.sylluxpvp.circuit.shared.service.impl.QueueService;
import com.sylluxpvp.circuit.shared.service.impl.ServerService;
import com.sylluxpvp.circuit.shared.tools.string.CC;
import org.bukkit.command.CommandSender;

import java.util.Optional;

@CommandAlias("queues")
@CommandPermission("circuit.command.queues")
public class QueuesCommand extends BaseCommand {

    @Default
    public void onQueues(CommandSender sender) {
        QueueService queueService = ServiceContainer.getService(QueueService.class);
        ServerService serverService = ServiceContainer.getService(ServerService.class);
        
        sender.sendMessage(CC.translate("&9&lQueue Information"));
        sender.sendMessage("");
        
        if (queueService.getQueues().isEmpty()) {
            sender.sendMessage(CC.translate("&7No active queues."));
            return;
        }
        
        for (Queue queue : queueService.getQueues().values()) {
            Optional<Server> serverOpt = serverService.find(queue.getServerName());
            String status = serverOpt.map(Server::getStatus).orElse("&cUnknown");
            String enabled = queue.isEnabled() ? "&aEnabled" : "&cDisabled";
            String paused = queue.isPaused() ? " &7(Paused)" : "";
            
            sender.sendMessage(CC.translate("&f" + queue.getServerName() + " &7- " + status + " &7- " + enabled + paused));
            sender.sendMessage(CC.translate("  &7Players in queue: &f" + queue.size()));
        }
    }

    @Subcommand("pause")
    @CommandPermission("circuit.command.queues.pause")
    public void onPause(CommandSender sender, String serverName) {
        QueueService queueService = ServiceContainer.getService(QueueService.class);
        Queue queue = queueService.getQueue(serverName);
        
        if (queue == null) {
            sender.sendMessage(CC.translate("&cNo queue exists for that server."));
            return;
        }
        
        queue.setPaused(!queue.isPaused());
        sender.sendMessage(CC.translate("&aQueue for &f" + serverName + " &ahas been " + (queue.isPaused() ? "&cpaused" : "&aresumed") + "&a."));
    }

    @Subcommand("clear")
    @CommandPermission("circuit.command.queues.clear")
    public void onClear(CommandSender sender, String serverName) {
        QueueService queueService = ServiceContainer.getService(QueueService.class);
        Queue queue = queueService.getQueue(serverName);
        
        if (queue == null) {
            sender.sendMessage(CC.translate("&cNo queue exists for that server."));
            return;
        }
        
        int cleared = queue.size();
        queue.getPlayers().clear();
        sender.sendMessage(CC.translate("&aCleared &f" + cleared + " &aplayers from the queue for &f" + serverName + "&a."));
    }
}
