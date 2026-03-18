package com.sylluxpvp.circuit.bukkit.task;

import com.sylluxpvp.circuit.bukkit.CircuitPlugin;
import com.sylluxpvp.circuit.bukkit.module.impl.PunishmentModule;
import com.sylluxpvp.circuit.shared.CircuitShared;
import lombok.Getter;
import org.bukkit.scheduler.BukkitRunnable;

@Getter
public class DatabaseHealthTask extends BukkitRunnable {
    private static final int CHECK_INTERVAL_TICKS = 100;
    private static final int INITIAL_DELAY_TICKS = 60;
    private int consecutiveFailures = 0;
    private boolean lastCheckFailed = false;
    private volatile boolean stopped = false;
    private volatile boolean alreadyDegraded = false;

    public void start() {
        this.runTaskTimerAsynchronously(CircuitPlugin.getInstance(), INITIAL_DELAY_TICKS, CHECK_INTERVAL_TICKS);
    }

    public void stop() {
        this.stopped = true;
        try {
            this.cancel();
        } catch (Exception exception) {
            // empty catch block
        }
    }

    public void run() {
        block8: {
            if (this.stopped || !CircuitPlugin.getInstance().isEnabled()) {
                return;
            }
            try {
                CircuitShared shared = CircuitPlugin.getInstance().getShared();
                if (shared == null || shared.getMongo() == null) {
                    this.logWarning("MongoDB instance is null");
                    return;
                }
                long start = System.currentTimeMillis();
                shared.getMongo().getDatabase().listCollectionNames().first();
                long elapsed = System.currentTimeMillis() - start;
                if (this.stopped || !CircuitPlugin.getInstance().isEnabled()) {
                    return;
                }
                if (this.lastCheckFailed || this.alreadyDegraded) {
                    CircuitPlugin.getInstance().getLogger().info("[HealthCheck] MongoDB connection restored (ping: " + elapsed + "ms)");
                    this.triggerRecovery();
                }
                this.consecutiveFailures = 0;
                this.lastCheckFailed = false;
                this.alreadyDegraded = false;
                if (elapsed > CHECK_INTERVAL_TICKS * 10 && !this.stopped && CircuitPlugin.getInstance().isEnabled()) {
                    CircuitPlugin.getInstance().getLogger().warning("[HealthCheck] MongoDB responding slowly: " + elapsed + "ms");
                }
            } catch (Exception e) {
                if (this.stopped || !CircuitPlugin.getInstance().isEnabled()) {
                    return;
                }
                ++this.consecutiveFailures;
                this.lastCheckFailed = true;
                this.logWarning("MongoDB health check failed (" + this.consecutiveFailures + " consecutive): " + e.getMessage());
                if (this.consecutiveFailures < 3 || this.alreadyDegraded) break block8;
                CircuitPlugin.getInstance().getLogger().severe("[HealthCheck] MongoDB appears to be DOWN! Triggering degradation.");
                this.alreadyDegraded = true;
                this.triggerDegradation();
            }
        }
    }

    private void triggerDegradation() {
        if (this.stopped || !CircuitPlugin.getInstance().isEnabled()) {
            return;
        }
        CircuitPlugin.getInstance().getServer().getScheduler().runTask(CircuitPlugin.getInstance(), () -> {
            PunishmentModule pm = CircuitPlugin.getInstance().getModuleManager().getModule(PunishmentModule.class);
            if (pm != null && pm.canWrite()) {
                pm.forceDegradation("MongoDB unresponsive after " + this.consecutiveFailures + " health check failures");
            }
        });
    }

    private void triggerRecovery() {
        if (this.stopped || !CircuitPlugin.getInstance().isEnabled()) {
            return;
        }
        CircuitPlugin.getInstance().getServer().getScheduler().runTask(CircuitPlugin.getInstance(), () -> {
            PunishmentModule pm = CircuitPlugin.getInstance().getModuleManager().getModule(PunishmentModule.class);
            if (pm != null) {
                pm.reportSuccess();
            }
        });
    }

    private void logWarning(String message) {
        if (this.stopped || !CircuitPlugin.getInstance().isEnabled()) {
            return;
        }
        CircuitPlugin.getInstance().getLogger().warning("[HealthCheck] " + message);
    }

    public boolean isHealthy() {
        return this.consecutiveFailures < 3;
    }

}

