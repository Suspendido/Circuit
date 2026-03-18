package com.sylluxpvp.circuit.bukkit.command.player;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import com.sylluxpvp.circuit.bukkit.menus.player.AltsMenu;
import com.sylluxpvp.circuit.shared.CircuitConstants;
import com.sylluxpvp.circuit.shared.profile.Profile;
import com.sylluxpvp.circuit.shared.service.ServiceContainer;
import com.sylluxpvp.circuit.shared.service.impl.ProfileService;
import com.sylluxpvp.circuit.shared.tools.string.CC;

import java.util.Set;
import java.util.stream.Collectors;

@CommandAlias("alts|dupeip")
@CommandPermission("circuit.cmd.alts")
public class AltsCommand extends BaseCommand {

    @Default
    @CommandCompletion("@players")
    public void alts(Player sender, @Name("target") OfflinePlayer target) {
        Profile profile = ServiceContainer.getService(ProfileService.class).find(target.getUniqueId());
        if (profile == null) {
            sender.sendMessage(CircuitConstants.getPlayerNotFound());
            return;
        }

        Set<Profile> alts = ServiceContainer.getService(ProfileService.class).findFromAddress(profile).stream()
                .filter(p -> !p.getUUID().equals(profile.getUUID()))
                .collect(Collectors.toSet());

        if (alts.isEmpty()) {
            sender.sendMessage(CC.translate("&cNo se encontraron multicuentas al " + profile.getCurrentGrant().getData().getColor() + profile.getName() + "&c!"));
            return;
        }

        new AltsMenu(profile).openMenu(sender);
    }
}
