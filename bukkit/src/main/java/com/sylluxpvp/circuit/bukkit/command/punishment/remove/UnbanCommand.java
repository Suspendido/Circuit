package com.sylluxpvp.circuit.bukkit.command.punishment.remove;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import com.sylluxpvp.circuit.bukkit.CircuitPlugin;
import com.sylluxpvp.circuit.shared.CircuitConstants;
import com.sylluxpvp.circuit.shared.grant.Grant;
import com.sylluxpvp.circuit.shared.profile.Profile;
import com.sylluxpvp.circuit.shared.punishment.Punishment;
import com.sylluxpvp.circuit.shared.punishment.PunishmentType;
import com.sylluxpvp.circuit.shared.redis.packets.punish.PunishmentUpdatePacket;
import com.sylluxpvp.circuit.shared.service.ServiceContainer;
import com.sylluxpvp.circuit.shared.service.impl.ProfileService;
import com.sylluxpvp.circuit.shared.service.impl.PunishmentService;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@CommandAlias("unban")
@CommandPermission("circuit.punish.unban")
public class UnbanCommand extends BaseCommand {

    @Default
    @CommandCompletion("@players *")
    public void unban(CommandSender sender, @Name("target") OfflinePlayer target, @Optional @Name("reason") @Flags("remaining") String reason) {
        if (target == null) {
            sender.sendMessage(CircuitConstants.getPlayerNotFound());
            return;
        }

        ProfileService profileService = ServiceContainer.getService(ProfileService.class);
        Profile profile = profileService.find(target.getUniqueId());

        if (profile == null) {
            try {
                profile = profileService.loadAsync(target.getUniqueId()).get(10, TimeUnit.SECONDS);
            } catch (Exception e) {
                sender.sendMessage(CircuitConstants.getPlayerNotFound());
                return;
            }
        }

        if (profile == null) {
            sender.sendMessage(CircuitConstants.getPlayerNotFound());
            return;
        }

        if (profile.findActivePunishment(PunishmentType.BAN) == null) {
            sender.sendMessage(CircuitConstants.getPlayerNotPunished().replace("<punishment_type>", PunishmentType.BAN.getAction()));
            return;
        }

        Grant<Punishment> punishment = profile.findActivePunishment(PunishmentType.BAN);
        UUID authorUUID = sender instanceof Player ? ((Player) sender).getUniqueId() : CircuitConstants.getConsoleUUID();

        ServiceContainer.getService(PunishmentService.class).removePunishment(authorUUID, profile, punishment, reason);
        profileService.save(profile);
        CircuitPlugin.getInstance().getShared().getRedis().sendPacket(
                new PunishmentUpdatePacket(authorUUID, target.getUniqueId(), PunishmentType.BAN.name(), -1, -1, reason, true, false)
        );
    }
}
