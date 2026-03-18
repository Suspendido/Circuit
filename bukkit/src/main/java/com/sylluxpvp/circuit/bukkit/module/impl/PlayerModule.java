package com.sylluxpvp.circuit.bukkit.module.impl;

import com.sylluxpvp.circuit.bukkit.command.misc.HubCommand;
import com.sylluxpvp.circuit.bukkit.module.Module;
import java.util.Collections;
import java.util.List;

public class PlayerModule extends Module {
    public PlayerModule() {
        super("Player", "Player utility commands");
    }

    @Override
    public List<String> getDependencies() {
        return Collections.singletonList("Core");
    }

    @Override
    protected void onEnable() {
        this.loadCommandsFromPackage("com.sylluxpvp.circuit.bukkit.command.player");
        this.registerCommand(new HubCommand());
    }

    @Override
    protected void onDisable() {
    }
}

