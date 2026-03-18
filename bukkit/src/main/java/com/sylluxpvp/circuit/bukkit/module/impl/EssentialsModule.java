package com.sylluxpvp.circuit.bukkit.module.impl;

import com.sylluxpvp.circuit.bukkit.command.essential.BackCommand;
import com.sylluxpvp.circuit.bukkit.command.essential.BroadcastCommand;
import com.sylluxpvp.circuit.bukkit.command.essential.ClearInventoryCommand;
import com.sylluxpvp.circuit.bukkit.command.essential.FeedCommand;
import com.sylluxpvp.circuit.bukkit.command.essential.FlyCommand;
import com.sylluxpvp.circuit.bukkit.command.essential.GamemodeCommand;
import com.sylluxpvp.circuit.bukkit.command.essential.HealCommand;
import com.sylluxpvp.circuit.bukkit.command.essential.InvseeCommand;
import com.sylluxpvp.circuit.bukkit.command.essential.MoreCommand;
import com.sylluxpvp.circuit.bukkit.command.essential.RenameCommand;
import com.sylluxpvp.circuit.bukkit.command.essential.SpawnCommand;
import com.sylluxpvp.circuit.bukkit.command.essential.SpeedCommand;
import com.sylluxpvp.circuit.bukkit.command.essential.TeleportCommand;
import com.sylluxpvp.circuit.bukkit.command.essential.TeleportHereCommand;
import com.sylluxpvp.circuit.bukkit.command.essential.TeleportPosCommand;
import com.sylluxpvp.circuit.bukkit.module.Module;
import java.util.Collections;
import java.util.List;

public class EssentialsModule extends Module {
    public EssentialsModule() {
        super("Essentials", "Basic utility commands");
    }

    @Override
    public List<String> getDependencies() {
        return Collections.singletonList("Core");
    }

    @Override
    protected void onEnable() {
        this.registerCommand(new BackCommand());
        this.registerCommand(new BroadcastCommand());
        this.registerCommand(new ClearInventoryCommand());
        this.registerCommand(new FeedCommand());
        this.registerCommand(new FlyCommand());
        this.registerCommand(new GamemodeCommand());
        this.registerCommand(new HealCommand());
        this.registerCommand(new InvseeCommand());
        this.registerCommand(new MoreCommand());
        this.registerCommand(new RenameCommand());
        this.registerCommand(new SpawnCommand());
        this.registerCommand(new SpeedCommand());
        this.registerCommand(new TeleportCommand());
        this.registerCommand(new TeleportHereCommand());
        this.registerCommand(new TeleportPosCommand());
    }

    @Override
    protected void onDisable() {
    }
}

