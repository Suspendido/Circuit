/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.sylluxpvp.circuit.bukkit.module.impl;

import com.sylluxpvp.circuit.bukkit.command.staff.AdminChatCommand;
import com.sylluxpvp.circuit.bukkit.command.staff.ClearChatCommand;
import com.sylluxpvp.circuit.bukkit.command.staff.FreezeCommand;
import com.sylluxpvp.circuit.bukkit.command.staff.MuteChatCommand;
import com.sylluxpvp.circuit.bukkit.command.staff.SlowChatCommand;
import com.sylluxpvp.circuit.bukkit.command.staff.StaffChatCommand;
import com.sylluxpvp.circuit.bukkit.command.staff.UnfreezeCommand;
import com.sylluxpvp.circuit.bukkit.command.staff.UserPermissionCommand;
import com.sylluxpvp.circuit.bukkit.listener.FreezeListener;
import com.sylluxpvp.circuit.bukkit.module.Module;
import com.sylluxpvp.circuit.bukkit.redis.AdminChatListener;
import com.sylluxpvp.circuit.bukkit.redis.ManagementBroadcastListener;
import com.sylluxpvp.circuit.bukkit.redis.ReportListener;
import com.sylluxpvp.circuit.bukkit.redis.RequestListener;
import com.sylluxpvp.circuit.bukkit.redis.StaffChatListener;
import com.sylluxpvp.circuit.bukkit.redis.StaffStatusListener;
import com.sylluxpvp.circuit.shared.redis.packets.broadcast.ManagementBroadcastPacket;
import com.sylluxpvp.circuit.shared.redis.packets.staff.AdminChatPacket;
import com.sylluxpvp.circuit.shared.redis.packets.staff.ReportPacket;
import com.sylluxpvp.circuit.shared.redis.packets.staff.RequestPacket;
import com.sylluxpvp.circuit.shared.redis.packets.staff.StaffChatPacket;
import com.sylluxpvp.circuit.shared.redis.packets.staff.StaffStatusPacket;
import java.util.Collections;
import java.util.List;

public class StaffModule
extends Module {
    public StaffModule() {
        super("Staff", "Staff tools and communication");
    }

    @Override
    public List<String> getDependencies() {
        return Collections.singletonList("Core");
    }

    @Override
    protected void onEnable() {
        this.registerPacketListener(new StaffStatusPacket(), new StaffStatusListener());
        this.registerPacketListener(new StaffChatPacket(), new StaffChatListener());
        this.registerPacketListener(new AdminChatPacket(), new AdminChatListener());
        this.registerPacketListener(new ManagementBroadcastPacket(), new ManagementBroadcastListener());
        this.registerPacketListener(new RequestPacket(), new RequestListener());
        this.registerPacketListener(new ReportPacket(), new ReportListener());
        this.registerListener(new FreezeListener());
        this.registerCommand(new StaffChatCommand());
        this.registerCommand(new AdminChatCommand());
        this.registerCommand(new FreezeCommand());
        this.registerCommand(new UnfreezeCommand());
        this.registerCommand(new ClearChatCommand());
        this.registerCommand(new MuteChatCommand());
        this.registerCommand(new SlowChatCommand());
        this.registerCommand(new UserPermissionCommand());
    }

    @Override
    protected void onDisable() {
    }
}

