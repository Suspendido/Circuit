package com.sylluxpvp.circuit.bukkit.module;

import com.sylluxpvp.circuit.bukkit.CircuitPlugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.configuration.file.YamlConfiguration;

@Getter @Setter
public class ModuleManager {
    private final Map<String, Module> modules = new ConcurrentHashMap<>();
    private final Map<String, Boolean> moduleStates = new ConcurrentHashMap<>();
    private final List<String> enableOrder = new ArrayList<>();

    public void registerModule(Module module) {
        if (module == null) {
            throw new IllegalArgumentException("Module cannot be null");
        }
        if (this.modules.containsKey(module.getName().toLowerCase())) {
            CircuitPlugin.getInstance().getLogger().warning("[ModuleManager] Module " + module.getName() + " is already registered!");
            return;
        }
        this.modules.put(module.getName().toLowerCase(), module);
        CircuitPlugin.getInstance().getLogger().info("[ModuleManager] Registered module: " + module.getName());
    }

    public void loadModuleStates(YamlConfiguration config) {
        for (String moduleName : this.modules.keySet()) {
            if (moduleName.equalsIgnoreCase("core")) {
                this.moduleStates.put("core", true);
                continue;
            }
            boolean enabled = config.getBoolean("modules." + moduleName + ".enabled", true);
            this.moduleStates.put(moduleName.toLowerCase(), enabled);
        }
    }

    public void enableModules() {
        Module module;
        HashSet<String> enabled = new HashSet<>();
        HashSet<String> pending = new HashSet<>(this.modules.keySet());
        int maxIterations = this.modules.size() * 2;
        int iteration = 0;
        while (!pending.isEmpty() && iteration < maxIterations) {
            ++iteration;
            Iterator iterator = pending.iterator();
            while (iterator.hasNext()) {
                String moduleName = (String)iterator.next();
                module = this.modules.get(moduleName);
                if (!this.moduleStates.getOrDefault(moduleName, true).booleanValue()) {
                    module.markDisabled();
                    iterator.remove();
                    continue;
                }
                boolean depsReady = true;
                String missingDep = null;
                for (String dep : module.getDependencies()) {
                    String depLower = dep.toLowerCase();
                    if (enabled.contains(depLower)) continue;
                    if (!this.modules.containsKey(depLower)) {
                        missingDep = dep + " (not registered)";
                        depsReady = false;
                        break;
                    }
                    if (!this.moduleStates.getOrDefault(depLower, true).booleanValue()) {
                        missingDep = dep + " (disabled in config)";
                        depsReady = false;
                        break;
                    }
                    Module depModule = this.modules.get(depLower);
                    if (depModule.getState().isFailed()) {
                        missingDep = dep + " (failed to start)";
                        depsReady = false;
                        break;
                    }
                    depsReady = false;
                }
                if (missingDep != null) {
                    module.markMissingDependency(missingDep);
                    iterator.remove();
                    continue;
                }
                if (!depsReady) continue;
                this.enableModule(module);
                enabled.add(moduleName);
                this.enableOrder.add(moduleName);
                iterator.remove();
            }
        }
        for (String moduleName : pending) {
            module = this.modules.get(moduleName);
            module.markMissingDependency("circular dependency detected");
        }
        this.logModuleSummary();
    }

    private void enableModule(Module module) {
        try {
            module.enable();
        } catch (Exception e) {
            CircuitPlugin.getInstance().getLogger().log(Level.SEVERE, "[ModuleManager] Failed to enable module " + module.getName() + ": " + e.getMessage(), e);
        }
    }

    public void disableModules() {
        ArrayList<String> reversed = new ArrayList<String>(this.enableOrder);
        Collections.reverse(reversed);
        for (String moduleName : reversed) {
            Module module = this.modules.get(moduleName);
            if (module == null || !module.isEnabled()) continue;
            try {
                module.disable();
            } catch (Exception e) {
                CircuitPlugin.getInstance().getLogger().log(Level.SEVERE, "[ModuleManager] Error disabling module " + module.getName() + ": " + e.getMessage(), e);
            }
        }
        this.enableOrder.clear();
        CircuitPlugin.getInstance().getLogger().info("[ModuleManager] All modules disabled.");
    }

    public <T extends Module> T getModule(String name) {
        return (T)this.modules.get(name.toLowerCase());
    }

    public <T extends Module> T getModule(Class<T> clazz) {
        for (Module module : this.modules.values()) {
            if (!clazz.isInstance(module)) continue;
            return (T)module;
        }
        return null;
    }

    public boolean isModuleEnabled(String name) {
        Module module = this.modules.get(name.toLowerCase());
        return module != null && module.isEnabled();
    }

    public List<Module> getEnabledModules() {
        ArrayList<Module> enabled = new ArrayList<Module>();
        for (Module module : this.modules.values()) {
            if (!module.isEnabled()) continue;
            enabled.add(module);
        }
        return enabled;
    }

    public List<Module> getModulesByState(ModuleState state) {
        ArrayList<Module> result = new ArrayList<Module>();
        for (Module module : this.modules.values()) {
            if (module.getState() != state) continue;
            result.add(module);
        }
        return result;
    }

    public void logModuleSummary() {
        int enabled = 0;
        int disabled = 0;
        int failed = 0;
        CircuitPlugin.getInstance().getLogger().info("[ModuleManager] === Module Status ===");
        for (Module module : this.modules.values()) {
            String status = module.getState().getDisplayName();
            String extra = module.getFailureReason() != null ? " - " + module.getFailureReason() : "";
            CircuitPlugin.getInstance().getLogger().info("  " + module.getName() + ": " + status + extra);
            if (module.getState() == ModuleState.ENABLED) {
                ++enabled;
                continue;
            }
            if (module.getState() == ModuleState.DISABLED) {
                ++disabled;
                continue;
            }
            if (!module.getState().isFailed()) continue;
            ++failed;
        }
        CircuitPlugin.getInstance().getLogger().info("[ModuleManager] Summary: " + enabled + " enabled, " + disabled + " disabled, " + failed + " failed");
    }

    public List<String> getFormattedStatus() {
        ArrayList<String> lines = new ArrayList<String>();
        for (Module module : this.modules.values()) {
            String status = module.getState().getColoredName();
            String extra = module.getFailureReason() != null ? " &7(" + module.getFailureReason() + ")" : "";
            lines.add("&7- &f" + module.getName() + "&7: " + status + extra);
        }
        return lines;
    }
}

