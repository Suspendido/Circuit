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
import xyz.kayaaa.xenon.shared.tools.java.TimeUtils;
import xyz.kayaaa.xenon.shared.tools.string.CC;

public class MuteCommand extends CommandBase {

    public MuteCommand() {
        super("mute", false);
    }

    @Command(name = "", desc = "Mutes a player", usage = "<player> <time> <reason>")
    @Require("xenon.punish.mute")
    public void mute(@Sender CommandSender sender, Player player, String time, @OptArg @Text String reason) {
        if (player == null) {
            sender.sendMessage(CC.translate("&cPlayer not found. Please recheck their username!"));
            return;
        }

        Profile profile = ServiceContainer.getService(ProfileService.class).find(player.getUniqueId());
        if (profile == null) {
            sender.sendMessage(CC.translate("&cPlayer not found. Please recheck their username!"));
            return;
        }

        if (profile.findActivePunishment(PunishmentType.MUTE) != null) {
            sender.sendMessage(CC.translate("&cPlayer is already muted!"));
            return;
        }

        boolean isPermanent = time.equalsIgnoreCase("perm") || time.equalsIgnoreCase("permanent");
        long duration = isPermanent ? -1 : TimeUtils.parseTime(time);
        if (!isPermanent && !TimeUtils.isTime(time)) {
            player.sendMessage(CC.translate("&cDuration needs to be a valid time unit."));
            return;
        }

        if (!isPermanent && duration < 0 || duration == Long.MAX_VALUE) {
            player.sendMessage(CC.translate("&cDuration needs to be a valid time."));
            return;
        }
        Grant<Punishment> grant = ServiceContainer.getService(PunishmentService.class).create(sender instanceof Player ? ((Player) sender).getUniqueId() : XenonConstants.getConsoleUUID(), PunishmentType.MUTE, reason, duration);
        profile.addGrant(grant);
        ServiceContainer.getService(ProfileService.class).save(profile);
        XenonPlugin.getInstance().getShared().getRedis().sendPacket(new PunishmentUpdatePacket(sender instanceof Player ? ((Player) sender).getUniqueId() : XenonConstants.getConsoleUUID(), player.getUniqueId(), PunishmentType.MUTE.name(), grant.getTimeCreated(), duration, reason, false, false));
    }
}
