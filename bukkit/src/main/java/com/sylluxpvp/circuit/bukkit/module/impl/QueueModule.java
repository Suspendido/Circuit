package com.sylluxpvp.circuit.bukkit.module.impl;

import com.sylluxpvp.circuit.bukkit.CircuitPlugin;
import com.sylluxpvp.circuit.bukkit.command.queue.LeaveQueueCommand;
import com.sylluxpvp.circuit.bukkit.command.queue.QueueAdminCommand;
import com.sylluxpvp.circuit.bukkit.command.queue.QueueCommand;
import com.sylluxpvp.circuit.bukkit.command.queue.QueuePositionCommand;
import com.sylluxpvp.circuit.bukkit.command.queue.QueuesCommand;
import com.sylluxpvp.circuit.bukkit.module.Module;
import com.sylluxpvp.circuit.bukkit.redis.QueueJoinListener;
import com.sylluxpvp.circuit.bukkit.redis.QueueLeaveListener;
import com.sylluxpvp.circuit.bukkit.redis.QueuePositionListener;
import com.sylluxpvp.circuit.bukkit.redis.QueueSendListener;
import com.sylluxpvp.circuit.bukkit.task.QueueTask;
import com.sylluxpvp.circuit.shared.redis.packets.queue.QueueJoinPacket;
import com.sylluxpvp.circuit.shared.redis.packets.queue.QueueLeavePacket;
import com.sylluxpvp.circuit.shared.redis.packets.queue.QueuePositionPacket;
import com.sylluxpvp.circuit.shared.redis.packets.queue.QueueSendPacket;
import com.sylluxpvp.circuit.shared.service.ServiceContainer;
import com.sylluxpvp.circuit.shared.service.impl.QueueService;
import java.util.Collections;
import java.util.List;

import lombok.Getter;
import org.bukkit.configuration.file.YamlConfiguration;

@Getter
public class QueueModule extends Module {
    private QueueTask queueTask;

    public QueueModule() {
        super("Queue", "Player queue system for server transfers");
    }

    @Override
    public List<String> getDependencies() {
        return Collections.singletonList("Core");
    }

    @Override
    protected void onEnable() {
        if (!this.validateConfig()) {
            throw new IllegalStateException("Queue module configuration is invalid - check logs for details");
        }
        this.registerPacketListener(new QueueJoinPacket(), new QueueJoinListener());
        this.registerPacketListener(new QueueLeavePacket(), new QueueLeaveListener());
        this.registerPacketListener(new QueueSendPacket(), new QueueSendListener());
        this.registerPacketListener(new QueuePositionPacket(), new QueuePositionListener());
        this.registerCommand(new QueueCommand());
        this.registerCommand(new QueueAdminCommand());
        this.registerCommand(new LeaveQueueCommand());
        this.registerCommand(new QueuePositionCommand());
        this.registerCommand(new QueuesCommand());
        this.loadQueues();
    }

    private boolean validateConfig() {
        String currentServer;
        String hubServer;
        YamlConfiguration config = CircuitPlugin.getInstance().getMainConfig();
        boolean valid = true;
        List<String> queueNames = config.getStringList("modules.queue.queues");
        if (queueNames.isEmpty()) {
            queueNames = config.getStringList("queue-manager.queues");
        }
        if (queueNames.isEmpty()) {
            CircuitPlugin.getInstance().getShared().getLogger().log("&c&lQueue Module &7- WARNING: No queues configured in modules.queue.queues");
            valid = false;
        }
        if ((hubServer = config.getString("modules.queue.hub-server")) == null || hubServer.isEmpty()) {
            hubServer = config.getString("queue-manager.hub-server");
        }
        if (hubServer == null || hubServer.isEmpty()) {
            CircuitPlugin.getInstance().getShared().getLogger().log("&c&lQueue Module &7- WARNING: hub-server not configured");
            valid = false;
        }
        if ((currentServer = config.getString("server.name")) != null) {
            for (String queueName : queueNames) {
                if (!queueName.equalsIgnoreCase(currentServer)) continue;
                CircuitPlugin.getInstance().getShared().getLogger().log("&c&lQueue Module &7- WARNING: Queue '" + queueName + "' cannot be the same as current server!");
                valid = false;
            }
        }
        return valid;
    }

    private void loadQueues() {
        YamlConfiguration config = CircuitPlugin.getInstance().getMainConfig();
        QueueService queueService = ServiceContainer.getService(QueueService.class);
        List<String> queueNames = config.getStringList("modules.queue.queues");
        if (queueNames.isEmpty()) {
            queueNames = config.getStringList("queue-manager.queues");
        }
        CircuitPlugin.getInstance().getShared().getLogger().log("&b&lQueue Module &7- Initializing...");
        for (String queueName : queueNames) {
            queueService.getOrCreateQueue(queueName);
            CircuitPlugin.getInstance().getShared().getLogger().log("&b&lQueue Module &7- Loaded queue: &f" + queueName);
        }
        this.queueTask = new QueueTask();
        CircuitPlugin.getInstance().getShared().getLogger().log("&b&lQueue Module &7- Started with &f" + queueNames.size() + " &7queues");
    }

    @Override
    protected void onDisable() {
        QueueService queueService = ServiceContainer.getService(QueueService.class);
        if (queueService != null) {
            queueService.getQueues().clear();
            queueService.getCooldowns().clear();
        }
    }
}

