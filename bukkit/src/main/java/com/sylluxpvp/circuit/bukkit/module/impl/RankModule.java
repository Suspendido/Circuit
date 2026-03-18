/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.sylluxpvp.circuit.bukkit.module.impl;

import com.sylluxpvp.circuit.bukkit.command.RankCommands;
import com.sylluxpvp.circuit.bukkit.command.TagCommands;
import com.sylluxpvp.circuit.bukkit.command.VIPCommands;
import com.sylluxpvp.circuit.bukkit.command.grant.GrantCommands;
import com.sylluxpvp.circuit.bukkit.command.grant.GrantsCommand;
import com.sylluxpvp.circuit.bukkit.module.Module;
import java.util.Collections;
import java.util.List;

public class RankModule
extends Module {
    public RankModule() {
        super("Rank", "Rank and grant management commands");
    }

    @Override
    public List<String> getDependencies() {
        return Collections.singletonList("Core");
    }

    @Override
    protected void onEnable() {
        this.registerCommand(new RankCommands());
        this.registerCommand(new TagCommands());
        this.registerCommand(new VIPCommands());
        this.registerCommand(new GrantCommands());
        this.registerCommand(new GrantsCommand());
    }

    @Override
    protected void onDisable() {
    }
}

