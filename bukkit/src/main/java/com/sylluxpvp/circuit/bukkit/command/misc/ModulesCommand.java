package com.sylluxpvp.circuit.bukkit.command.misc;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import com.sylluxpvp.circuit.bukkit.CircuitPlugin;
import com.sylluxpvp.circuit.bukkit.module.Module;
import com.sylluxpvp.circuit.bukkit.module.ModuleManager;
import com.sylluxpvp.circuit.bukkit.module.ModuleState;
import com.sylluxpvp.circuit.shared.tools.string.CC;
import org.bukkit.command.CommandSender;

@CommandAlias(value="modules|circuitmodules")
@CommandPermission(value="circuit.admin")
public class ModulesCommand extends BaseCommand {

    @Default
    @Description(value="View status of all Circuit modules")
    public void onModules(CommandSender sender) {
        ModuleManager manager = CircuitPlugin.getInstance().getModuleManager();
        sender.sendMessage(CC.translate("&7"));
        sender.sendMessage(CC.translate("&9&lCircuit Modules"));
        sender.sendMessage(CC.translate("&7"));
        int enabled = 0;
        int disabled = 0;
        int failed = 0;
        int degraded = 0;
        block5: for (Module module : manager.getModules().values()) {
            ModuleState state = module.getState();
            String stateDisplay = state.getColoredName();
            String deps = module.getDependencies().isEmpty() ? "" : " &8[deps: " + String.join(", ", module.getDependencies()) + "]";
            String reason = module.getFailureReason() != null ? " &7(" + module.getFailureReason() + ")" : "";
            sender.sendMessage(CC.translate("&7- &f" + module.getName() + "&7: " + stateDisplay + reason + deps));
            switch (state) {
                case ENABLED: {
                    ++enabled;
                    continue block5;
                }
                case DISABLED: {
                    ++disabled;
                    continue block5;
                }
                case DEGRADED: {
                    ++degraded;
                    continue block5;
                }
            }
            if (!state.isFailed()) continue;
            ++failed;
        }
        sender.sendMessage(CC.translate("&7"));
        sender.sendMessage(CC.translate("&7Summary: &a" + enabled + " enabled&7, &8" + disabled + " disabled&7, &c" + failed + " failed" + (degraded > 0 ? "&7, &6" + degraded + " degraded" : "")));
        boolean allCriticalOk = manager.isModuleEnabled("core");
        sender.sendMessage(CC.translate(allCriticalOk ? "&aServer is operational" : "&cCritical modules offline!"));
    }
}
