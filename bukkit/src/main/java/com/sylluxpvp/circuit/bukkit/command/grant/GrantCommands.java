package com.sylluxpvp.circuit.bukkit.command.grant;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import com.sylluxpvp.circuit.bukkit.menus.grant.GrantMenu;
import com.sylluxpvp.circuit.bukkit.service.BukkitGrantService;
import com.sylluxpvp.circuit.shared.CircuitConstants;
import com.sylluxpvp.circuit.shared.grant.GrantProcedure;
import com.sylluxpvp.circuit.shared.profile.Profile;
import com.sylluxpvp.circuit.shared.rank.Rank;
import com.sylluxpvp.circuit.shared.service.ServiceContainer;
import com.sylluxpvp.circuit.shared.service.impl.ProfileService;
import com.sylluxpvp.circuit.shared.tools.java.TimeUtils;
import com.sylluxpvp.circuit.shared.tools.string.CC;

import java.util.UUID;

@CommandAlias("grant")
@CommandPermission("circuit.grant.create")
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
            sender.sendMessage(CircuitConstants.getPlayerNotFound());
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

        UUID author = sender instanceof Player ? ((Player) sender).getUniqueId() : CircuitConstants.getConsoleUUID();
        GrantProcedure<Rank> procedure = new GrantProcedure<>(author, target.getUniqueId(), rank, duration, reason);
        ServiceContainer.getService(BukkitGrantService.class).applyGrant(author, target.getUniqueId(), procedure);
    }
}
