package xyz.kayaaa.xenon.bukkit.command.impl;

import com.jonahseguin.drink.annotation.Command;
import com.jonahseguin.drink.annotation.Require;
import com.jonahseguin.drink.annotation.Sender;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import xyz.kayaaa.xenon.bukkit.command.CommandBase;
import xyz.kayaaa.xenon.shared.grant.Grant;
import xyz.kayaaa.xenon.shared.profile.Profile;
import xyz.kayaaa.xenon.shared.rank.Rank;
import xyz.kayaaa.xenon.shared.service.ServiceContainer;
import xyz.kayaaa.xenon.shared.service.impl.GrantService;
import xyz.kayaaa.xenon.shared.service.impl.ProfileService;
import xyz.kayaaa.xenon.shared.tools.CC;

public class GrantCommands extends CommandBase {

    public GrantCommands() {
        super("grant", false);
    }

    @Command(name = "", desc = "Grants a rank to a player", usage = "<player> <rank>")
    @Require("xenon.grant.create")
    public void grant(@Sender CommandSender sender, Player player, Rank rank) {
        if (sender instanceof Player) {
            sender.sendMessage(CC.translate("&cThis command needs to be executed from console!"));
            return;
        }

        if (player == null) {
            sender.sendMessage(CC.translate("&cPlayer not found. Please recheck their username!"));
            return;
        }

        Profile profile = ServiceContainer.getService(ProfileService.class).find(player.getUniqueId());
        if (profile.hasRank(rank)) {
            sender.sendMessage(CC.translate("&cThis player already has this rank!"));
            return;
        }

        GrantService service = ServiceContainer.getService(GrantService.class);
        Grant<Rank> grant = service.createGrant(rank, service.getConsole(), "Default");
        profile.addGrant(grant);
        player.sendMessage(CC.translate("&aYou have been granted " + rank.getColor() + rank.getName() + "&a!"));
    }

    @Command(name = "remove", desc = "Removes a player's grant", usage = "<player> <rank>")
    @Require("xenon.grant.remove")
    public void remove(@Sender CommandSender sender, Player player, Rank rank) {
        if (sender instanceof Player) {
            sender.sendMessage(CC.translate("&cThis command needs to be executed from console!"));
            return;
        }

        if (player == null) {
            sender.sendMessage(CC.translate("&cPlayer not found. Please recheck their username!"));
            return;
        }

        Profile profile = ServiceContainer.getService(ProfileService.class).find(player.getUniqueId());
        if (!profile.hasRank(rank) || profile.findByRank(rank) == null) {
            sender.sendMessage(CC.translate("&cThis player doesn't have this rank!"));
            return;
        }
        Grant<Rank> grant = profile.findByRank(rank);
        grant.setRemoved(true);
        grant.setRemovedAt(System.currentTimeMillis());
        grant.setRemovalReason("None");
        grant.setRemovedBy(ServiceContainer.getService(GrantService.class).getConsole());
        player.sendMessage(CC.translate("&cYour " + grant.getData().getColor() + grant.getData().getName() + " &cgrant was removed."));
    }

}
