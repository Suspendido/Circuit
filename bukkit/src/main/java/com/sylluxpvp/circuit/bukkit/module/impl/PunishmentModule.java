package com.sylluxpvp.circuit.bukkit.module.impl;

import com.sylluxpvp.circuit.bukkit.CircuitPlugin;
import com.sylluxpvp.circuit.bukkit.command.punishment.PunishmentHistoryCommand;
import com.sylluxpvp.circuit.bukkit.module.Module;
import com.sylluxpvp.circuit.bukkit.module.ModuleState;
import com.sylluxpvp.circuit.shared.service.DatabaseOperationListener;
import com.sylluxpvp.circuit.shared.service.ServiceContainer;
import com.sylluxpvp.circuit.shared.service.impl.ProfileService;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.Getter;

@Getter
public class PunishmentModule extends Module {
    private boolean readonlyMode = false;
    private final AtomicInteger consecutiveFailures = new AtomicInteger(0);
    private static final int FAILURE_THRESHOLD = 3;
    private DatabaseOperationListener dbListener;

    public PunishmentModule() {
        super("Punishment", "Player punishment system");
    }

    @Override
    public List<String> getDependencies() {
        return Collections.singletonList("Core");
    }

    @Override
    protected void onEnable() {
        this.dbListener = new DatabaseOperationListener(){

            @Override
            public void onDatabaseFailure(String operation, Exception error) {
                PunishmentModule.this.reportFailure(operation);
            }

            @Override
            public void onDatabaseSuccess() {
                PunishmentModule.this.reportSuccess();
            }
        };
        ServiceContainer.getService(ProfileService.class).addDatabaseListener(this.dbListener);
        this.registerCommand(new PunishmentHistoryCommand());
        this.loadCommandsFromPackage("com.sylluxpvp.circuit.bukkit.command.punishment.create");
        this.loadCommandsFromPackage("com.sylluxpvp.circuit.bukkit.command.punishment.remove");
    }

    @Override
    protected void onDisable() {
        ProfileService ps;
        if (this.dbListener != null && (ps = ServiceContainer.getServiceSafe(ProfileService.class)) != null) {
            ps.removeDatabaseListener(this.dbListener);
        }
    }

    @Override
    protected void onDegraded(String reason) {
        this.readonlyMode = true;
        CircuitPlugin.getInstance().getShared().getLogger().log("&c&lPunishment Module &7- Entering readonly mode: " + reason);
        CircuitPlugin.getInstance().getShared().getLogger().log("&c&lPunishment Module &7- Write operations (ban/mute/kick) are BLOCKED");
    }

    @Override
    protected void onRecovered() {
        this.readonlyMode = false;
        this.consecutiveFailures.set(0);
        CircuitPlugin.getInstance().getShared().getLogger().log("&a&lPunishment Module &7- Recovered! Write operations restored.");
    }

    public void reportFailure(String operation) {
        if (this.getState() == ModuleState.DEGRADED) {
            return;
        }
        int failures = this.consecutiveFailures.incrementAndGet();
        CircuitPlugin.getInstance().getLogger().warning("[PunishmentModule] Operation failed: " + operation + " (failures: " + failures + "/" + FAILURE_THRESHOLD + ")");
        if (failures >= FAILURE_THRESHOLD && this.getState() == ModuleState.ENABLED) {
            this.enterDegradedMode("Database unresponsive after " + failures + " failures");
        }
    }

    public void reportSuccess() {
        this.consecutiveFailures.set(0);
        if (this.getState() == ModuleState.DEGRADED) {
            this.recoverFromDegradedMode();
        }
    }

    public boolean canWrite() {
        return !this.readonlyMode && this.getState() == ModuleState.ENABLED;
    }

    public void forceDegradation(String reason) {
        if (this.getState() == ModuleState.DEGRADED) {
            return;
        }
        this.enterDegradedMode(reason);
    }

    public String getDegradedMessage() {
        return "&cPunishment system is temporarily in readonly mode. Please try again later.";
    }
}

