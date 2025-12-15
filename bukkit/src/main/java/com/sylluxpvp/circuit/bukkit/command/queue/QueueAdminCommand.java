package com.sylluxpvp.circuit.bukkit.command.queue;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import com.sylluxpvp.circuit.shared.queue.Queue;
import com.sylluxpvp.circuit.shared.service.ServiceContainer;
import com.sylluxpvp.circuit.shared.service.impl.QueueService;
import com.sylluxpvp.circuit.shared.tools.string.CC;
import org.bukkit.command.CommandSender;

@CommandAlias("queueadmin|qa")
@CommandPermission("circuit.queue.admin")
public class QueueAdminCommand extends BaseCommand {

    @Default
    @HelpCommand
    public void onHelp(CommandSender sender) {
        sender.sendMessage(CC.translate("&8&m-----------------------------"));
        sender.sendMessage(CC.translate("&9/queueadmin list"));
        sender.sendMessage(CC.translate("&9/queueadmin info <queue>"));
        sender.sendMessage(CC.translate("&9/queueadmin pause <queue>"));
        sender.sendMessage(CC.translate("&9/queueadmin unpause <queue>"));
        sender.sendMessage(CC.translate("&9/queueadmin enable <queue>"));
        sender.sendMessage(CC.translate("&9/queueadmin disable <queue>"));
        sender.sendMessage(CC.translate("&9/queueadmin clear <queue>"));
        sender.sendMessage(CC.translate("&9/queueadmin create <queue>"));
        sender.sendMessage(CC.translate("&8&m-----------------------------"));
    }

    @Subcommand("list")
    public void onList(CommandSender sender) {
        QueueService queueService = ServiceContainer.getService(QueueService.class);
        
        if (queueService.getQueues().isEmpty()) {
            sender.sendMessage(CC.translate("&cNo queues are currently loaded."));
            return;
        }
        
        sender.sendMessage(CC.translate("&9&lActive Queues:"));
        for (Queue queue : queueService.getQueues().values()) {
            String status = getQueueStatus(queue);
            sender.sendMessage(CC.translate("&7- &f" + queue.getServerName() + " " + status + " &8(&7" + queue.size() + " players&8)"));
        }
    }

    @Subcommand("pause")
    @Syntax("<queue>")
    @CommandCompletion("@servers")
    public void onPause(CommandSender sender, String queueName) {
        QueueService queueService = ServiceContainer.getService(QueueService.class);
        Queue queue = queueService.getQueue(queueName);
        
        if (queue == null) {
            sender.sendMessage(CC.translate("&cQueue not found: " + queueName));
            return;
        }
        
        if (queue.isPaused()) {
            sender.sendMessage(CC.translate("&cQueue is already paused."));
            return;
        }
        
        queue.setPaused(true);
        sender.sendMessage(CC.translate("&aQueue &f" + queueName + " &ahas been paused."));
    }

    @Subcommand("unpause|resume")
    @Syntax("<queue>")
    @CommandCompletion("@servers")
    public void onUnpause(CommandSender sender, String queueName) {
        QueueService queueService = ServiceContainer.getService(QueueService.class);
        Queue queue = queueService.getQueue(queueName);
        
        if (queue == null) {
            sender.sendMessage(CC.translate("&cQueue not found: " + queueName));
            return;
        }
        
        if (!queue.isPaused()) {
            sender.sendMessage(CC.translate("&cQueue is not paused."));
            return;
        }
        
        queue.setPaused(false);
        sender.sendMessage(CC.translate("&aQueue &f" + queueName + " &ahas been resumed."));
    }

    @Subcommand("enable")
    @Syntax("<queue>")
    @CommandCompletion("@servers")
    public void onEnable(CommandSender sender, String queueName) {
        QueueService queueService = ServiceContainer.getService(QueueService.class);
        Queue queue = queueService.getQueue(queueName);
        
        if (queue == null) {
            sender.sendMessage(CC.translate("&cQueue not found: " + queueName));
            return;
        }
        
        if (queue.isEnabled()) {
            sender.sendMessage(CC.translate("&cQueue is already enabled."));
            return;
        }
        
        queue.setEnabled(true);
        sender.sendMessage(CC.translate("&aQueue &f" + queueName + " &ahas been enabled."));
    }

    @Subcommand("disable")
    @Syntax("<queue>")
    @CommandCompletion("@servers")
    public void onDisable(CommandSender sender, String queueName) {
        QueueService queueService = ServiceContainer.getService(QueueService.class);
        Queue queue = queueService.getQueue(queueName);
        
        if (queue == null) {
            sender.sendMessage(CC.translate("&cQueue not found: " + queueName));
            return;
        }
        
        if (!queue.isEnabled()) {
            sender.sendMessage(CC.translate("&cQueue is already disabled."));
            return;
        }
        
        queue.setEnabled(false);
        sender.sendMessage(CC.translate("&aQueue &f" + queueName + " &ahas been disabled."));
    }

    @Subcommand("clear")
    @Syntax("<queue>")
    @CommandCompletion("@servers")
    public void onClear(CommandSender sender, String queueName) {
        QueueService queueService = ServiceContainer.getService(QueueService.class);
        Queue queue = queueService.getQueue(queueName);
        
        if (queue == null) {
            sender.sendMessage(CC.translate("&cQueue not found: " + queueName));
            return;
        }
        
        int count = queue.size();
        queue.getPlayers().clear();
        sender.sendMessage(CC.translate("&aCleared &f" + count + " &aplayers from queue &f" + queueName + "&a."));
    }

    @Subcommand("info")
    @Syntax("<queue>")
    @CommandCompletion("@servers")
    public void onInfo(CommandSender sender, String queueName) {
        QueueService queueService = ServiceContainer.getService(QueueService.class);
        Queue queue = queueService.getQueue(queueName);
        
        if (queue == null) {
            sender.sendMessage(CC.translate("&cQueue not found: " + queueName));
            return;
        }
        
        sender.sendMessage(CC.translate("&b&lQueue Info: &f" + queue.getServerName()));
        sender.sendMessage(CC.translate("&7Status: " + getQueueStatus(queue)));
        sender.sendMessage(CC.translate("&7Players: &f" + queue.size()));
        sender.sendMessage(CC.translate("&7Enabled: " + (queue.isEnabled() ? "&aYes" : "&cNo")));
        sender.sendMessage(CC.translate("&7Paused: " + (queue.isPaused() ? "&eYes" : "&aNo")));
    }

    @Subcommand("create")
    @Syntax("<queue>")
    @CommandCompletion("@servers")
    public void onCreate(CommandSender sender, String queueName) {
        QueueService queueService = ServiceContainer.getService(QueueService.class);
        
        if (queueService.getQueue(queueName) != null) {
            sender.sendMessage(CC.translate("&cQueue already exists: " + queueName));
            return;
        }
        
        queueService.getOrCreateQueue(queueName);
        sender.sendMessage(CC.translate("&aCreated queue: &f" + queueName));
    }

    private String getQueueStatus(Queue queue) {
        if (!queue.isEnabled()) {
            return "&cDisabled";
        }
        if (queue.isPaused()) {
            return "&7Paused";
        }
        return "&aActive";
    }
}
