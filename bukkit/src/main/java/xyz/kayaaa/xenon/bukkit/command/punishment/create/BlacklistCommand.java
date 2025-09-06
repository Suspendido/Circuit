package xyz.kayaaa.xenon.bukkit.command.punishment.create;

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

@CommandAlias("blacklist|banip|ipban|bl")
@CommandPermission("xenon.punish.blacklist")
public class BlacklistCommand extends BaseCommand {

    @Default
    @CommandCompletion("@players *")
    public void blacklist(CommandSender sender, @Name("target") OfflinePlayer target, @Optional @Name("reason") @Flags("remaining") String reason) {
        if (target == null) {
            sender.sendMessage(XenonConstants.getPlayerNotFound());
            return;
        }

        Profile profile = ServiceContainer.getService(ProfileService.class).find(target.getUniqueId());
        if (profile == null) {
            sender.sendMessage(XenonConstants.getPlayerNotFound());
            return;
        }

        if (profile.findActivePunishment(PunishmentType.BLACKLIST) != null || profile.findActivePunishment(PunishmentType.BAN) != null) {
            sender.sendMessage(XenonConstants.getPlayerAlreadyPunished().replace("<punishment_type>", PunishmentType.BAN.getAction()));
            return;
        }

        Grant<Punishment> grant = ServiceContainer.getService(PunishmentService.class).create(sender instanceof Player ? ((Player) sender).getUniqueId() : XenonConstants.getConsoleUUID(), PunishmentType.BLACKLIST, reason, -1);
        profile.addGrant(grant);
        ServiceContainer.getService(ProfileService.class).save(profile);
        XenonPlugin.getInstance().getShared().getRedis().sendPacket(new PunishmentUpdatePacket(sender instanceof Player ? ((Player) sender).getUniqueId() : XenonConstants.getConsoleUUID(), target.getUniqueId(), PunishmentType.BLACKLIST.name(), grant.getTimeCreated(), -1, reason, false, false));
    }
}
