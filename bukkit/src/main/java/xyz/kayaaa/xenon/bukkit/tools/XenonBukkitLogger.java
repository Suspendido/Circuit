package xyz.kayaaa.xenon.bukkit.tools;

import org.bukkit.Bukkit;
import xyz.kayaaa.xenon.shared.tools.string.CC;
import xyz.kayaaa.xenon.shared.tools.xenon.XenonLogger;

public class XenonBukkitLogger implements XenonLogger {

    @Override
    public void log(boolean prefix, String message) {
        Bukkit.getConsoleSender().sendMessage(CC.translate((prefix ? "[Xenon - INFO] " : "") + message));
    }

    @Override
    public void log(String message) {
        log(true, CC.translate(message));
    }

    @Override
    public void warn(boolean prefix, String message) {
        Bukkit.getConsoleSender().sendMessage(CC.translate((prefix ? "&e[Xenon - WARN] " : "") + message));
    }

    @Override
    public void warn(String message) {
        warn(true, CC.translate(message));
    }

    @Override
    public void error(boolean prefix, String message) {
        Bukkit.getConsoleSender().sendMessage(CC.translate((prefix ? "&c[Xenon - ERROR] " : "") + message));
    }

    @Override
    public void error(String message) {
        error(true, CC.translate(message));
    }
}
