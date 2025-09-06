package xyz.kayaaa.xenon.bukkit.command.grant;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import xyz.kayaaa.xenon.bukkit.menus.grant.GrantMenu;
import xyz.kayaaa.xenon.bukkit.service.BukkitGrantService;
import xyz.kayaaa.xenon.shared.XenonConstants;
import xyz.kayaaa.xenon.shared.grant.GrantProcedure;
import xyz.kayaaa.xenon.shared.profile.Profile;
import xyz.kayaaa.xenon.shared.rank.Rank;
import xyz.kayaaa.xenon.shared.service.ServiceContainer;
import xyz.kayaaa.xenon.shared.service.impl.GrantService;
import xyz.kayaaa.xenon.shared.service.impl.ProfileService;
import xyz.kayaaa.xenon.shared.tools.java.TimeUtils;
import xyz.kayaaa.xenon.shared.tools.string.CC;

import java.util.UUID;

@CommandAlias("grant")
@CommandPermission("xenon.grant.create")
public class GrantCommands extends BaseCommand {

    @Default
    @Description("Grants a rank to a player")
    @CommandCompletion("@players")
    public void grants(Player sender, @Name("target") OfflinePlayer target) {
        if (target == null) {
            sender.sendMessage(CC.RED + "Player not found. Please recheck their username!");
            return;
        }

        new GrantMenu(target.getUniqueId()).openMenu(sender);
    }

    @Subcommand("manual")
    @Description("Grants a rank to a player")
    @CommandCompletion("@players @ranks @times *")
    public void grant(CommandSender sender, @Name("target") Player target, @Name("rank") Rank rank, @Name("time") String time, @Optional @Name("reason") @Flags("remaining") String reason) {
        if (target == null) {
            sender.sendMessage(XenonConstants.getPlayerNotFound());
            return;
        }

        Profile profile = ServiceContainer.getService(ProfileService.class).find(target.getUniqueId());
        if (profile.hasRank(rank)) {
            sender.sendMessage(CC.translate("&cThis player already has this rank!"));
            return;
        }

        boolean isPermanent = time.equalsIgnoreCase("perm") || time.equalsIgnoreCase("permanent");
        long duration = isPermanent ? -1 : TimeUtils.parseTime(time);

        if (!isPermanent && !TimeUtils.isTime(time)) {
            sender.sendMessage(CC.translate("&cDuration needs to be a valid time unit."));
            return;
        }

        if (!isPermanent && duration < 0 || duration == Long.MAX_VALUE) {
            sender.sendMessage(CC.translate("&cDuration needs to be a valid time."));
            return;
        }

        UUID author = sender instanceof Player ? ((Player) sender).getUniqueId() : XenonConstants.getConsoleUUID();
        GrantProcedure<Rank> procedure = new GrantProcedure<>(author, target.getUniqueId(), rank, duration, reason);
        ServiceContainer.getService(BukkitGrantService.class).applyGrant(author, target.getUniqueId(), procedure);
    }
}
