package com.sylluxpvp.circuit.bukkit.tools.spigot;

import lombok.experimental.UtilityClass;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import com.sylluxpvp.circuit.shared.tools.string.CC;

import java.util.function.Function;

@UtilityClass
public class ServerUtils {

    public void sendMessage(String message) {
        sendMessage(message, "");
    }

    public void sendMessage(String message, String permission) {
        sendMessage(message, (player -> (permission == null || permission.isEmpty()) || player.hasPermission(permission)));
    }

    public void sendMessage(String message, Function<Player, Boolean> function) {
        Bukkit.getConsoleSender().sendMessage(CC.translate(message));
        sendMessage(player -> message, function);
    }

    public void sendMessageNoConsole(String message, Function<Player, Boolean> function) {
        sendMessage(player -> message, function);
    }

    public void sendMessage(Function<Player, String> message, Function<Player, Boolean> booleanFunc) {
        Bukkit.getOnlinePlayers().forEach(player -> {
            if (booleanFunc.apply(player)) player.sendMessage(CC.translate(message.apply(player)));
        });
    }

}
