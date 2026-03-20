package com.sylluxpvp.circuit.bukkit.command.punishment.create;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import com.sylluxpvp.circuit.bukkit.module.impl.PunishmentModule;
import com.sylluxpvp.circuit.shared.tools.string.CC;
import org.bukkit.Bukkit;
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

@CommandAlias("blacklist|bl")
@CommandPermission("circuit.punish.blacklist")
public class BlacklistCommand extends BaseCommand {

    @Default
    @CommandCompletion("@players *")
    public void blacklist(CommandSender sender, @Name("target") OfflinePlayer target, @Optional @Name("reason") @Flags("remaining") String reason) {
        PunishmentModule punishmentModule = CircuitPlugin.getInstance().getModuleManager().getModule(PunishmentModule.class);
        if (punishmentModule != null && !punishmentModule.canWrite()) {
            sender.sendMessage(CC.translate(punishmentModule.getDegradedMessage()));
            return;
        }
        if (target == null) {
            sender.sendMessage(CircuitConstants.getPlayerNotFound());
            return;
        }
        ProfileService profileService = ServiceContainer.getService(ProfileService.class);
        Profile profile = profileService.find(target.getUniqueId());
        if (profile == null) {
            sender.sendMessage(CircuitConstants.getPlayerNotFound());
            return;
        }

        if (profile.findActivePunishment(PunishmentType.BLACKLIST) != null || profile.findActivePunishment(PunishmentType.BAN) != null) {
            sender.sendMessage(CircuitConstants.getPlayerAlreadyPunished().replace("<punishment_type>", PunishmentType.BAN.getAction()));
            return;
        }
        UUID senderUUID = sender instanceof Player ? ((Player)sender).getUniqueId() : CircuitConstants.getConsoleUUID();
        Grant<Punishment> grant = ServiceContainer.getService(PunishmentService.class).create(senderUUID, PunishmentType.BLACKLIST, reason, -1L);
        (profileService.saveWithPendingGrant(profile, grant).thenAccept(success -> Bukkit.getScheduler().runTask(CircuitPlugin.getInstance(), () -> {
            if (success) {
                profile.addGrant(grant);
                CircuitPlugin.getInstance().getShared().getRedis().sendPacket(new PunishmentUpdatePacket(senderUUID, target.getUniqueId(), PunishmentType.BLACKLIST.name(), grant.getTimeCreated(), -1L, reason, false, false));
            } else {
                sender.sendMessage(CC.translate("&c&lError: &cFailed to save blacklist to database. Try again later."));
                if (punishmentModule != null) {
                    punishmentModule.reportFailure("blacklist command - save failed");
                }
            }
        }))).exceptionally(ex -> {
            Bukkit.getScheduler().runTask(CircuitPlugin.getInstance(), () -> {
                sender.sendMessage(CC.translate("&c&lError: &cPunishment system unavailable. Try again later."));
                if (punishmentModule != null) {
                    punishmentModule.reportFailure("blacklist command - exception: " + ex.getMessage());
                }
            });
            return null;
        });
    }
}
