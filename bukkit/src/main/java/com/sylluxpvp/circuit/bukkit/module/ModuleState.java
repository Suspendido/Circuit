/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.sylluxpvp.circuit.bukkit.module;

import lombok.Getter;

@Getter
public enum ModuleState {
    REGISTERED("Registered", "&7"),
    PENDING("Pending", "&e"),
    ENABLED("Enabled", "&a"),
    DISABLED("Disabled", "&8"),
    FAILED("Failed", "&c"),
    MISSING_DEPENDENCY("Missing Dependency", "&c"),
    DEGRADED("Degraded", "&6");

    private final String displayName;
    private final String color;

    ModuleState(String displayName, String color) {
        this.displayName = displayName;
        this.color = color;
    }

    public String getColoredName() {
        return this.color + this.displayName;
    }

    public boolean isOperational() {
        return this == ENABLED || this == DEGRADED;
    }

    public boolean isFailed() {
        return this == FAILED || this == MISSING_DEPENDENCY;
    }

    public boolean isTerminal() {
        return this == DISABLED || this == FAILED || this == MISSING_DEPENDENCY;
    }

    public boolean canTransitionTo(ModuleState target) {
        return switch (this) {
            case REGISTERED -> target == PENDING || target == DISABLED;
            case PENDING -> target == ENABLED || target == FAILED || target == MISSING_DEPENDENCY;
            case ENABLED -> target == DEGRADED || target == DISABLED;
            case DEGRADED -> target == ENABLED || target == DISABLED;
            default -> false;
        };
    }
}

