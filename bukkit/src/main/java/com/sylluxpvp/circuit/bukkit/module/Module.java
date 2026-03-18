/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  org.bukkit.event.Listener
 *  org.bukkit.plugin.Plugin
 */
package com.sylluxpvp.circuit.bukkit.module;

import co.aikar.commands.BaseCommand;
import com.sylluxpvp.circuit.bukkit.CircuitPlugin;
import com.sylluxpvp.circuit.bukkit.module.ModuleState;
import com.sylluxpvp.circuit.shared.redis.RedisPacket;
import com.sylluxpvp.circuit.shared.redis.listener.PacketListener;
import com.sylluxpvp.circuit.shared.tools.java.ClassUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import lombok.Generated;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

@Getter @Setter
public abstract class Module {
    private final String name;
    private final String description;
    private ModuleState state = ModuleState.REGISTERED;
    private String failureReason = null;
    private final List<Listener> registeredListeners = new ArrayList<>();
    private final List<BaseCommand> registeredCommands = new ArrayList<>();
    private final List<RedisPacketRegistration<?>> registeredPackets = new ArrayList();

    public Module(String name, String description) {
        this.name = name;
        this.description = description;
    }

    protected abstract void onEnable();

    protected abstract void onDisable();

    protected void onDegraded(String reason) {
    }

    protected void onRecovered() {
    }

    public List<String> getDependencies() {
        return Collections.emptyList();
    }

    public boolean isOperational() {
        return this.state.isOperational();
    }

    public boolean isEnabled() {
        return this.state == ModuleState.ENABLED;
    }

    protected void registerListener(Listener listener) {
        CircuitPlugin.getInstance().getServer().getPluginManager().registerEvents(listener, (Plugin)CircuitPlugin.getInstance());
        this.registeredListeners.add(listener);
    }

    protected void registerCommand(BaseCommand command) {
        CircuitPlugin.getInstance().getCommandManager().registerCommand(command);
        this.registeredCommands.add(command);
    }

    protected <T extends RedisPacket> void registerPacketListener(T packet, PacketListener<T> listener) {
        CircuitPlugin.getInstance().getShared().getRedis().registerListener(packet, listener);
        this.registeredPackets.add(new RedisPacketRegistration<T>(packet, listener));
    }

    protected void loadCommandsFromPackage(String packageName) {
        try {
            List<Class<?>> classes = ClassUtils.getClasses(CircuitPlugin.getInstance().getPluginFile(), packageName);
            for (Class<?> clazz : classes) {
                if (clazz.getName().contains("$") || !BaseCommand.class.isAssignableFrom(clazz)) continue;
                try {
                    BaseCommand command = (BaseCommand)clazz.getDeclaredConstructor(new Class[0]).newInstance(new Object[0]);
                    this.registerCommand(command);
                } catch (Exception e) {
                    CircuitPlugin.getInstance().getLogger().warning("[" + this.name + "] Failed to load command: " + clazz.getSimpleName());
                }
            }
        } catch (Exception e) {
            CircuitPlugin.getInstance().getLogger().warning("[" + this.name + "] Failed to load commands from: " + packageName);
        }
    }

    final void enable() {
        long startTime = System.currentTimeMillis();
        try {
            this.state = ModuleState.PENDING;
            this.onEnable();
            this.state = ModuleState.ENABLED;
            long elapsed = System.currentTimeMillis() - startTime;
            String timeInfo = elapsed > 100L ? " (" + elapsed + "ms)" : "";
            CircuitPlugin.getInstance().getLogger().info("[Module] " + this.name + " " + this.state.getDisplayName() + timeInfo);
            if (elapsed > 1000L) {
                CircuitPlugin.getInstance().getLogger().warning("[Module] " + this.name + " took " + elapsed + "ms to enable - consider optimizing!");
            }
        } catch (Exception e) {
            this.state = ModuleState.FAILED;
            this.failureReason = e.getMessage();
            long elapsed = System.currentTimeMillis() - startTime;
            CircuitPlugin.getInstance().getLogger().log(Level.SEVERE, "[Module] " + this.name + " FAILED after " + elapsed + "ms: " + e.getMessage(), e);
        }
    }

    final void markDisabled() {
        this.state = ModuleState.DISABLED;
        CircuitPlugin.getInstance().getLogger().info("[Module] " + this.name + " is DISABLED via config");
    }

    final void markMissingDependency(String missingDep) {
        this.state = ModuleState.MISSING_DEPENDENCY;
        this.failureReason = "Missing dependency: " + missingDep;
        CircuitPlugin.getInstance().getLogger().severe("[Module] " + this.name + " cannot start - " + this.failureReason);
    }

    public final void enterDegradedMode(String reason) {
        if (this.state == ModuleState.ENABLED) {
            this.state = ModuleState.DEGRADED;
            this.failureReason = reason;
            CircuitPlugin.getInstance().getLogger().warning("[Module] " + this.name + " entering DEGRADED mode: " + reason);
            this.onDegraded(reason);
        }
    }

    public final void recoverFromDegradedMode() {
        if (this.state == ModuleState.DEGRADED) {
            this.state = ModuleState.ENABLED;
            this.failureReason = null;
            CircuitPlugin.getInstance().getLogger().info("[Module] " + this.name + " RECOVERED from degraded mode");
            this.onRecovered();
        }
    }

    final void disable() {
        if (!this.state.isOperational()) {
            return;
        }
        try {
            this.onDisable();
            for (BaseCommand command : this.registeredCommands) {
                CircuitPlugin.getInstance().getCommandManager().unregisterCommand(command);
            }
            this.registeredCommands.clear();
            this.registeredListeners.clear();
            this.registeredPackets.clear();
            this.state = ModuleState.DISABLED;
            CircuitPlugin.getInstance().getLogger().info("[Module] " + this.name + " disabled.");
        } catch (Exception e) {
            CircuitPlugin.getInstance().getLogger().log(Level.SEVERE, "[Module] Error disabling " + this.name + ": " + e.getMessage(), e);
        }
    }

    protected static class RedisPacketRegistration<T extends RedisPacket> {
        private final T packet;
        private final PacketListener<T> listener;

        public RedisPacketRegistration(T packet, PacketListener<T> listener) {
            this.packet = packet;
            this.listener = listener;
        }

        @Generated
        public T getPacket() {
            return this.packet;
        }

        @Generated
        public PacketListener<T> getListener() {
            return this.listener;
        }
    }
}

