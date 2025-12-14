package com.sylluxpvp.circuit.bukkit.command.player;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import org.bukkit.entity.Player;
import com.sylluxpvp.circuit.bukkit.CircuitPlugin;
import com.sylluxpvp.circuit.shared.profile.Profile;
import com.sylluxpvp.circuit.shared.redis.packets.staff.RequestPacket;
import com.sylluxpvp.circuit.shared.service.ServiceContainer;
import com.sylluxpvp.circuit.shared.service.impl.ProfileService;
import com.sylluxpvp.circuit.shared.tools.string.CC;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@CommandAlias("request|helpop")
public class RequestCommand extends BaseCommand {

    private static final Map<UUID, Long> cooldowns = new HashMap<>();
<<<<<<< HEAD
    private static final long COOLDOWN_MS = 60_000L;
=======
    private static final long COOLDOWN_MS = 30_000L;
>>>>>>> 0ad8df0acd0dd3bb1a047959dda167bd8ce3c136

    @Default
    @Syntax("<message>")
    public void onRequest(Player sender, String message) {
        if (cooldowns.containsKey(sender.getUniqueId())) {
            long remaining = (cooldowns.get(sender.getUniqueId()) + COOLDOWN_MS) - System.currentTimeMillis();
            if (remaining > 0) {
                sender.sendMessage(CC.translate("&cYou must wait " + (remaining / 1000) + " seconds before requesting again."));
                return;
            }
        }

        cooldowns.put(sender.getUniqueId(), System.currentTimeMillis());

        Profile profile = ServiceContainer.getService(ProfileService.class).find(sender.getUniqueId());
        String playerColor = "&7";
        if (profile != null && profile.getCurrentGrant() != null && profile.getCurrentGrant().getData() != null) {
            playerColor = profile.getCurrentGrant().getData().getColor();
        }

        String serverName = CircuitPlugin.getInstance().getShared().getServer().getName();

        // Send via Redis to all servers
        CircuitPlugin.getInstance().getShared().getRedis().sendPacket(
                new RequestPacket(sender.getUniqueId(), sender.getName(), playerColor, serverName, message)
        );

        sender.sendMessage(CC.translate("&aYour request has been sent to staff."));
    }
}
