package xyz.kayaaa.xenon.bukkit.command.impl.punishment;

import com.jonahseguin.drink.annotation.*;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import xyz.kayaaa.xenon.bukkit.XenonPlugin;
import xyz.kayaaa.xenon.bukkit.command.CommandBase;
import xyz.kayaaa.xenon.shared.XenonConstants;
import xyz.kayaaa.xenon.shared.grant.Grant;
import xyz.kayaaa.xenon.shared.profile.Profile;
import xyz.kayaaa.xenon.shared.punishment.Punishment;
import xyz.kayaaa.xenon.shared.punishment.PunishmentType;
import xyz.kayaaa.xenon.shared.redis.packets.punish.PunishmentUpdatePacket;
import xyz.kayaaa.xenon.shared.service.ServiceContainer;
import xyz.kayaaa.xenon.shared.service.impl.ProfileService;
import xyz.kayaaa.xenon.shared.service.impl.PunishmentService;
import xyz.kayaaa.xenon.shared.tools.string.CC;

public class BlacklistCommand extends CommandBase {

    public BlacklistCommand() {
        super("blacklist", false, "ipban", "bl");
    }

    @Command(name = "", desc = "Blacklists a player", usage = "<player> <time> <reason>")
    @Require("xenon.punish.blacklist")
    public void blacklist(@Sender CommandSender sender, Player player, @OptArg @Text String reason) {
        if (player == null) {
            sender.sendMessage(CC.translate("&cPlayer not found. Please recheck their username!"));
            return;
        }

        Profile profile = ServiceContainer.getService(ProfileService.class).find(player.getUniqueId());
        if (profile == null) {
            sender.sendMessage(CC.translate("&cPlayer not found. Please recheck their username!"));
            return;
        }

        if (profile.findActivePunishment(PunishmentType.BAN) != null || profile.findActivePunishment(PunishmentType.BLACKLIST) != null) {
            sender.sendMessage(CC.translate("&cPlayer is already banned!"));
            return;
        }

        Grant<Punishment> grant = ServiceContainer.getService(PunishmentService.class).create(sender instanceof Player ? ((Player) sender).getUniqueId() : XenonConstants.getConsoleUUID(), PunishmentType.BLACKLIST, reason, -1);
        profile.addGrant(grant);
        ServiceContainer.getService(ProfileService.class).save(profile);
        XenonPlugin.getInstance().getShared().getRedis().sendPacket(new PunishmentUpdatePacket(sender instanceof Player ? ((Player) sender).getUniqueId() : XenonConstants.getConsoleUUID(), player.getUniqueId(), PunishmentType.BLACKLIST.name(), grant.getTimeCreated(), -1, reason, false, false));
    }
}
