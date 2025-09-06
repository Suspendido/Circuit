package xyz.kayaaa.xenon.bukkit.command.punishment.remove;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import xyz.kayaaa.xenon.bukkit.XenonPlugin;
import xyz.kayaaa.xenon.shared.XenonConstants;
import xyz.kayaaa.xenon.shared.grant.Grant;
import xyz.kayaaa.xenon.shared.profile.Profile;
import xyz.kayaaa.xenon.shared.punishment.Punishment;
import xyz.kayaaa.xenon.shared.punishment.PunishmentType;
import xyz.kayaaa.xenon.shared.redis.packets.punish.PunishmentUpdatePacket;
import xyz.kayaaa.xenon.shared.service.ServiceContainer;
import xyz.kayaaa.xenon.shared.service.impl.ProfileService;
import xyz.kayaaa.xenon.shared.service.impl.PunishmentService;

@CommandAlias("unblacklist")
@CommandPermission("xenon.punish.blacklist")
public class UnblacklistCommand extends BaseCommand {

    @Default
    @CommandCompletion("@players *")
    public void unblacklist(CommandSender sender, @Name("target") OfflinePlayer target, @Optional @Name("reason") @Flags("remaining") String reason) {
        if (target == null) {
            sender.sendMessage(XenonConstants.getPlayerNotFound());
            return;
        }

        Profile profile = ServiceContainer.getService(ProfileService.class).find(target.getUniqueId());
        if (profile == null) {
            sender.sendMessage(XenonConstants.getPlayerNotFound());
            return;
        }

        if (profile.findActivePunishment(PunishmentType.BLACKLIST) == null) {
            sender.sendMessage(XenonConstants.getPlayerNotPunished().replace("<punishment_type>", PunishmentType.BLACKLIST.getAction()));
            return;
        }

        Grant<Punishment> punishment = profile.findActivePunishment(PunishmentType.BLACKLIST);
        ServiceContainer.getService(PunishmentService.class).removePunishment(sender instanceof Player ? ((Player) sender).getUniqueId() : XenonConstants.getConsoleUUID(), profile, punishment, reason);
        ServiceContainer.getService(ProfileService.class).save(profile);
        XenonPlugin.getInstance().getShared().getRedis().sendPacket(new PunishmentUpdatePacket(sender instanceof Player ? ((Player) sender).getUniqueId() : XenonConstants.getConsoleUUID(), target.getUniqueId(), PunishmentType.BLACKLIST.name(), -1, -1, reason, true, false));
    }
}
