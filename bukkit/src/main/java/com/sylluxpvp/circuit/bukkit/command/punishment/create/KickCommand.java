package com.sylluxpvp.circuit.bukkit.command.punishment.create;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import co.aikar.commands.bukkit.contexts.OnlinePlayer;
import com.sylluxpvp.circuit.bukkit.module.impl.PunishmentModule;
import com.sylluxpvp.circuit.shared.tools.string.CC;
import org.bukkit.Bukkit;
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

@CommandAlias("kick")
@CommandPermission("circuit.punish.kick")
public class KickCommand extends BaseCommand {

    @Default
    @CommandCompletion("@players *")
    public void kick(CommandSender sender, @Name("target") OnlinePlayer target, @Optional @Name("reason") @Flags("remaining") String reason) {
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
        Profile profile = profileService.find(target.getPlayer().getUniqueId());
        if (profile == null) {
            sender.sendMessage(CircuitConstants.getPlayerNotFound());
            return;
        }
        UUID senderUUID = sender instanceof Player ? ((Player)sender).getUniqueId() : CircuitConstants.getConsoleUUID();
        UUID targetUUID = target.getPlayer().getUniqueId();
        Grant<Punishment> grant = ServiceContainer.getService(PunishmentService.class).create(senderUUID, PunishmentType.KICK, reason, -1L);
        (profileService.saveWithPendingGrant(profile, grant).thenAccept(success -> Bukkit.getScheduler().runTask(CircuitPlugin.getInstance(), () -> {
            if (success) {
                profile.addGrant(grant);
                CircuitPlugin.getInstance().getShared().getRedis().sendPacket(new PunishmentUpdatePacket(senderUUID, targetUUID, PunishmentType.KICK.name(), System.currentTimeMillis(), -1L, reason, false, true));
            } else {
                sender.sendMessage(CC.translate("&c&lError: &cFailed to save kick to database. Try again later."));
                if (punishmentModule != null) {
                    punishmentModule.reportFailure("kick command - save failed");
                }
            }
        }))).exceptionally(ex -> {
            Bukkit.getScheduler().runTask(CircuitPlugin.getInstance(), () -> {
                sender.sendMessage(CC.translate("&c&lError: &cPunishment system unavailable. Try again later."));
                if (punishmentModule != null) {
                    punishmentModule.reportFailure("kick command - exception: " + ex.getMessage());
                }
            });
            return null;
        });
    }
}
