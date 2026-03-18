/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.sylluxpvp.circuit.bukkit.module.impl;

import com.sylluxpvp.circuit.bukkit.module.Module;
import java.util.Collections;
import java.util.List;

public class ServerModule
extends Module {
    public ServerModule() {
        super("Server", "Server management commands");
    }

    @Override
    public List<String> getDependencies() {
        return Collections.singletonList("Core");
    }

    @Override
    protected void onEnable() {
        this.loadCommandsFromPackage("com.sylluxpvp.circuit.bukkit.command.server");
    }

    @Override
    protected void onDisable() {
    }
}

