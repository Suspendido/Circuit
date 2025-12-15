package com.sylluxpvp.circuit.bukkit.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import com.sylluxpvp.circuit.bukkit.CircuitPlugin;
import com.sylluxpvp.circuit.shared.profile.Profile;
import com.sylluxpvp.circuit.shared.redis.packets.vip.VIPUpdatePacket;
import com.sylluxpvp.circuit.shared.service.ServiceContainer;
import com.sylluxpvp.circuit.shared.service.impl.ProfileService;
import com.sylluxpvp.circuit.shared.tools.string.CC;

@CommandAlias("vip")
@CommandPermission("circuit.vip.manage")
public class VIPCommands extends BaseCommand {

    @Default
    @HelpCommand
    public void help(CommandSender sender) {
        sender.sendMessage(CC.translate("&8&m-----------------------------"));
        sender.sendMessage(CC.translate("&9/vip set <player>"));
        sender.sendMessage(CC.translate("&9/vip remove <player>"));
        sender.sendMessage(CC.translate("&9/vip check <player>"));
        sender.sendMessage(CC.translate("&8&m-----------------------------"));
    }

    @Subcommand("set")
    @Description("Set VIP status for a player")
    @CommandCompletion("@players")
    @Syntax("<player>")
    public void set(CommandSender sender, OfflinePlayer target) {
        ProfileService profileService = ServiceContainer.getService(ProfileService.class);
        Profile profile = profileService.load(target.getUniqueId());
        
        if (profile == null) {
            sender.sendMessage(CC.translate("&cProfile not found."));
            return;
        }

        if (profile.isVipStatus()) {
            sender.sendMessage(CC.translate("&c" + target.getName() + " already has VIP status."));
            return;
        }

        profile.setVipStatus(true);
        profileService.save(profile);
        CircuitPlugin.getInstance().getShared().getRedis().sendPacket(new VIPUpdatePacket(target.getUniqueId(), true));
        sender.sendMessage(CC.translate("&aVIP status set for &e" + target.getName() + "&a."));
    }

    @Subcommand("remove")
    @Description("Remove VIP status from a player")
    @CommandCompletion("@players")
    @Syntax("<player>")
    public void remove(CommandSender sender, OfflinePlayer target) {
        ProfileService profileService = ServiceContainer.getService(ProfileService.class);
        Profile profile = profileService.load(target.getUniqueId());
        
        if (profile == null) {
            sender.sendMessage(CC.translate("&cProfile not found."));
            return;
        }

        if (!profile.isVipStatus()) {
            sender.sendMessage(CC.translate("&c" + target.getName() + " doesn't have VIP status."));
            return;
        }

        profile.setVipStatus(false);
        profileService.save(profile);
        CircuitPlugin.getInstance().getShared().getRedis().sendPacket(new VIPUpdatePacket(target.getUniqueId(), false));
        sender.sendMessage(CC.translate("&aVIP status removed from &e" + target.getName() + "&a."));
    }

    @Subcommand("check")
    @Description("Check VIP status of a player")
    @CommandCompletion("@players")
    @Syntax("<player>")
    public void check(CommandSender sender, OfflinePlayer target) {
        ProfileService profileService = ServiceContainer.getService(ProfileService.class);
        Profile profile = profileService.load(target.getUniqueId());
        
        if (profile == null) {
            sender.sendMessage(CC.translate("&cProfile not found."));
            return;
        }

        if (profile.isVipStatus()) {
            sender.sendMessage(CC.translate("&a" + target.getName() + " has VIP status."));
        } else {
            sender.sendMessage(CC.translate("&c" + target.getName() + " doesn't have VIP status."));
        }
    }
}
