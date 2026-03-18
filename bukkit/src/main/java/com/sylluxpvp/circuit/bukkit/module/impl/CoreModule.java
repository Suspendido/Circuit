package com.sylluxpvp.circuit.bukkit.module.impl;

import com.sylluxpvp.circuit.bukkit.CircuitPlugin;
import com.sylluxpvp.circuit.bukkit.command.misc.ModulesCommand;
import com.sylluxpvp.circuit.bukkit.listener.PlayerListener;
import com.sylluxpvp.circuit.bukkit.listener.ServerListener;
import com.sylluxpvp.circuit.bukkit.module.Module;
import com.sylluxpvp.circuit.bukkit.placeholder.CircuitExpansion;
import com.sylluxpvp.circuit.bukkit.redis.*;
import com.sylluxpvp.circuit.bukkit.service.BukkitChatService;
import com.sylluxpvp.circuit.bukkit.service.BukkitGrantService;
import com.sylluxpvp.circuit.bukkit.service.BukkitProfileService;
import com.sylluxpvp.circuit.bukkit.task.DatabaseHealthTask;
import com.sylluxpvp.circuit.bukkit.task.GrantDurationTask;
import com.sylluxpvp.circuit.bukkit.tools.menu.MenuListener;
import com.sylluxpvp.circuit.bukkit.tools.spigot.BungeeUtils;
import com.sylluxpvp.circuit.shared.redis.listener.RankUpdateListener;
import com.sylluxpvp.circuit.shared.redis.listener.TagUpdateListener;
import com.sylluxpvp.circuit.shared.redis.listener.VIPUpdateListener;
import com.sylluxpvp.circuit.shared.redis.packets.broadcast.BroadcastPacket;
import com.sylluxpvp.circuit.shared.redis.packets.discord.DiscordSyncUpdatePacket;
import com.sylluxpvp.circuit.shared.redis.packets.misc.MessagePacket;
import com.sylluxpvp.circuit.shared.redis.packets.punish.PunishmentUpdatePacket;
import com.sylluxpvp.circuit.shared.redis.packets.rank.RankUpdatePacket;
import com.sylluxpvp.circuit.shared.redis.packets.server.ServerCommandPacket;
import com.sylluxpvp.circuit.shared.redis.packets.server.ServerDiscoveryPacket;
import com.sylluxpvp.circuit.shared.redis.packets.server.ServerStatusPacket;
import com.sylluxpvp.circuit.shared.redis.packets.tag.TagUpdatePacket;
import com.sylluxpvp.circuit.shared.redis.packets.vip.VIPUpdatePacket;
import com.sylluxpvp.circuit.shared.service.ServiceContainer;
import com.sylluxpvp.circuit.shared.service.impl.QueueService;
import org.bukkit.Bukkit;

public class CoreModule extends Module {
    private DatabaseHealthTask healthTask;

    public CoreModule() {
        super("Core", "Essential functionality - profiles, ranks, grants");
    }

    @Override
    protected void onEnable() {
        ServiceContainer.registerService(new BukkitChatService());
        ServiceContainer.registerService(new BukkitProfileService());
        ServiceContainer.registerService(new BukkitGrantService());
        ServiceContainer.registerService(new QueueService());
        this.registerPacketListener(new MessagePacket(), new MessageListener());
        this.registerPacketListener(new ServerStatusPacket(), new ServerStatusListener());
        this.registerPacketListener(new PunishmentUpdatePacket(), new PunishmentUpdateListener());
        this.registerPacketListener(new ServerCommandPacket(), new ServerCommandListener());
        this.registerPacketListener(new BroadcastPacket(), new BroadcastListener());
        this.registerPacketListener(new ServerDiscoveryPacket(), new ServerDiscoveryListener());
        this.registerPacketListener(new RankUpdatePacket(), new RankUpdateListener());
        this.registerPacketListener(new TagUpdatePacket(), new TagUpdateListener());
        this.registerPacketListener(new VIPUpdatePacket(), new VIPUpdateListener());
        this.registerPacketListener(new DiscordSyncUpdatePacket(), new DiscordSyncUpdateListener());
        this.registerListener(new PlayerListener());
        this.registerListener(new ServerListener());
        this.registerListener(new MenuListener());
        this.registerCommand(new ModulesCommand());
        new GrantDurationTask();
        this.healthTask = new DatabaseHealthTask();
        this.healthTask.start();
        BungeeUtils.registerChannel();
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new CircuitExpansion().register();
            CircuitPlugin.getInstance().getLogger().info("PlaceholderAPI expansion registered!");
        }
    }

    @Override
    protected void onDisable() {
        if (this.healthTask != null) {
            this.healthTask.stop();
        }
    }
}

