package com.sylluxpvp.circuit.bukkit.command.punishment.create;

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

@CommandAlias("blacklist|banip|ipban|bl")
@CommandPermission("circuit.punish.blacklist")
public class BlacklistCommand extends BaseCommand {

    @Default
    @CommandCompletion("@players *")
    public void blacklist(CommandSender sender, @Name("target") OfflinePlayer target, @Optional @Name("reason") @Flags("remaining") String reason) {
        if (target == null) {
            sender.sendMessage(CircuitConstants.getPlayerNotFound());
            return;
        }

        Profile profile = ServiceContainer.getService(ProfileService.class).find(target.getUniqueId());
        if (profile == null) {
            sender.sendMessage(CircuitConstants.getPlayerNotFound());
            return;
        }

        if (profile.findActivePunishment(PunishmentType.BLACKLIST) != null || profile.findActivePunishment(PunishmentType.BAN) != null) {
            sender.sendMessage(CircuitConstants.getPlayerAlreadyPunished().replace("<punishment_type>", PunishmentType.BAN.getAction()));
            return;
        }

        Grant<Punishment> grant = ServiceContainer.getService(PunishmentService.class).create(sender instanceof Player ? ((Player) sender).getUniqueId() : CircuitConstants.getConsoleUUID(), PunishmentType.BLACKLIST, reason, -1);
        profile.addGrant(grant);
        ServiceContainer.getService(ProfileService.class).save(profile);
        CircuitPlugin.getInstance().getShared().getRedis().sendPacket(new PunishmentUpdatePacket(sender instanceof Player ? ((Player) sender).getUniqueId() : CircuitConstants.getConsoleUUID(), target.getUniqueId(), PunishmentType.BLACKLIST.name(), grant.getTimeCreated(), -1, reason, false, false));
    }
}
