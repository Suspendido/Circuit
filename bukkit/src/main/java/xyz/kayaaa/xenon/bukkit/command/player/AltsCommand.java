package xyz.kayaaa.xenon.bukkit.command.player;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import xyz.kayaaa.xenon.shared.XenonConstants;
import xyz.kayaaa.xenon.shared.profile.Profile;
import xyz.kayaaa.xenon.shared.service.ServiceContainer;
import xyz.kayaaa.xenon.shared.service.impl.ProfileService;
import xyz.kayaaa.xenon.shared.tools.string.CC;
import java.util.Set;
import java.util.stream.Collectors;

@CommandAlias("alts|dupeip")
@CommandPermission("xenon.cmd.alts")
public class AltsCommand extends BaseCommand {

    @Default
    public void alts(CommandSender sender, @Name("target") OfflinePlayer target) {
        Profile profile = ServiceContainer.getService(ProfileService.class).find(target.getUniqueId());
        if (profile == null) {
            sender.sendMessage(XenonConstants.getPlayerNotFound());
            return;
        }

        Set<Profile> profiles = ServiceContainer.getService(ProfileService.class).findFromAddress(profile).stream().filter(profile1 -> !profile1.equals(profile)).collect(Collectors.toSet());
        if (profiles.isEmpty()) {
            sender.sendMessage(CC.RED + "No alts found!");
            return;
        }

        sender.sendMessage("");
        sender.sendMessage(CC.translate("&9Alts for &7" + profile.getCurrentGrant().getData().getColor() + profile.getName() + " &7(&9" + profiles.size() + "&7)"));
        for (Profile alt : profiles) {
            String status = (Bukkit.getPlayer(alt.getUUID()) != null && Bukkit.getPlayer(alt.getUUID()).isOnline() ? "&aOnline" : "&cOffline") + " &7- " + (alt.findActivePunishment() != null ? "&c" + alt.findActivePunishment().getData().getPunishmentType().getAction() : "&cnot punished");
            sender.sendMessage(CC.translate("&7- " + alt.getCurrentGrant().getData().getColor() + alt.getName() + " &7(" + status + "&7)"));
        }
        sender.sendMessage("");
    }

}
