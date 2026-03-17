package com.sylluxpvp.circuit.bukkit.hook;

import com.sylluxpvp.circuit.bukkit.CircuitPlugin;
import com.sylluxpvp.circuit.bukkit.hook.impl.LunarClient;
import com.sylluxpvp.circuit.bukkit.hook.listener.ClientListener;
import lombok.Getter;
import org.bukkit.plugin.PluginManager;

@Getter
public class HookManager {

    private final ClientHook hook;

    public HookManager() {
        PluginManager pm = CircuitPlugin.getInstance().getServer().getPluginManager();
        this.hook = this.isPluginEnabled(pm, "Apollo-Bukkit") ? new LunarClient() : null;
        if (this.hook != null) pm.registerEvents(new ClientListener(hook), CircuitPlugin.getInstance());
    }

    private boolean isPluginEnabled(PluginManager pm, String name) {
        return pm.getPlugin(name) != null && pm.isPluginEnabled(name);
    }
}
