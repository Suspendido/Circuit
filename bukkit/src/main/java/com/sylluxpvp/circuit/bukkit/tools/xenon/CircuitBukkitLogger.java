package com.sylluxpvp.circuit.bukkit.tools.circuit;

import org.bukkit.Bukkit;
import com.sylluxpvp.circuit.shared.tools.string.CC;
import com.sylluxpvp.circuit.shared.tools.circuit.CircuitLogger;

public class CircuitBukkitLogger implements CircuitLogger {

    @Override
    public void log(boolean prefix, String message) {
        Bukkit.getConsoleSender().sendMessage(CC.translate((prefix ? "[Circuit - INFO] " : "") + message));
    }

    @Override
    public void log(String message) {
        log(true, CC.translate(message));
    }

    @Override
    public void warn(boolean prefix, String message) {
        Bukkit.getConsoleSender().sendMessage(CC.translate((prefix ? "&e[Circuit - WARN] " : "") + message));
    }

    @Override
    public void warn(String message) {
        warn(true, CC.translate(message));
    }

    @Override
    public void error(boolean prefix, String message) {
        Bukkit.getConsoleSender().sendMessage(CC.translate((prefix ? "&c[Circuit - ERROR] " : "") + message));
    }

    @Override
    public void error(String message) {
        error(true, CC.translate(message));
    }
}
