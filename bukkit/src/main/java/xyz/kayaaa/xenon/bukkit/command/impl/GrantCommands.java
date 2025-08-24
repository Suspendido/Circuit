package xyz.kayaaa.xenon.bukkit.command.impl;

import com.jonahseguin.drink.annotation.*;
import org.bukkit.entity.Player;
import xyz.kayaaa.xenon.bukkit.command.CommandBase;
import xyz.kayaaa.xenon.shared.grant.Grant;
import xyz.kayaaa.xenon.shared.profile.Profile;
import xyz.kayaaa.xenon.shared.rank.Rank;
import xyz.kayaaa.xenon.shared.service.ServiceContainer;
import xyz.kayaaa.xenon.shared.service.impl.GrantService;
import xyz.kayaaa.xenon.shared.service.impl.ProfileService;
import xyz.kayaaa.xenon.shared.tools.java.TimeUtils;
import xyz.kayaaa.xenon.shared.tools.string.CC;

public class GrantCommands extends CommandBase {

    public GrantCommands() {
        super("grant", false);
    }

    @Command(name = "", desc = "Grants a rank to a player", usage = "<player> <rank> <time> <reason>")
    @Require("xenon.grant.create")
    public void grant(@Sender Player sender, Player player, Rank rank, String time, @OptArg @Text String reason) {
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
        long duration = TimeUtils.parseTime(time);
        Grant<Rank> grant = service.createGrant(rank, sender.getUniqueId(), duration, reason == null ? "None" : reason);
        profile.addGrant(grant);
        player.sendMessage(CC.translate("&aYou have been granted " + rank.getColor() + rank.getName() + "&a!"));
    }

}
