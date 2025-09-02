package xyz.kayaaa.xenon.bukkit.redis;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import xyz.kayaaa.xenon.bukkit.tools.spigot.ServerUtils;
import xyz.kayaaa.xenon.bukkit.tools.spigot.TaskUtil;
import xyz.kayaaa.xenon.shared.XenonConstants;
import xyz.kayaaa.xenon.shared.XenonShared;
import xyz.kayaaa.xenon.shared.profile.Profile;
import xyz.kayaaa.xenon.shared.punishment.PunishmentType;
import xyz.kayaaa.xenon.shared.redis.listener.PacketListener;
import xyz.kayaaa.xenon.shared.redis.packets.punish.PunishmentUpdatePacket;
import xyz.kayaaa.xenon.shared.service.ServiceContainer;
import xyz.kayaaa.xenon.shared.service.impl.ProfileService;
import xyz.kayaaa.xenon.shared.tools.java.TimeUtils;
import xyz.kayaaa.xenon.shared.tools.string.StringHelper;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class PunishmentUpdateListener extends PacketListener<PunishmentUpdatePacket> {

    @Override
    public void listen(PunishmentUpdatePacket packet) {
        TaskUtil.runTask(() -> {
            UUID authorUUID = packet.getAuthor();
            UUID targetUUID = packet.getTarget();
            PunishmentType type = PunishmentType.from(packet.getPunishmentType());
            if (type == null) {
                XenonShared.getInstance().getLogger().warn("Tried sending a punishment update with invalid punishment type!");
                return;
            }

            String author = "&4&lConsole";
            if (!authorUUID.equals(XenonConstants.getConsoleUUID())) {
                Profile authorProfile = ServiceContainer.getService(ProfileService.class).find(authorUUID);
                String authorName = Bukkit.getOfflinePlayer(authorUUID).getName();
                author = authorProfile.getCurrentGrant().getData().getColor() + authorName;
            }

            Profile targetProfile = ServiceContainer.getService(ProfileService.class).find(targetUUID);
            String targetName = Bukkit.getOfflinePlayer(targetUUID).getName();
            String target = targetProfile.getCurrentGrant().getData().getColor() + targetName;

            if (packet.isRemoved()) {
                ServerUtils.sendMessage(author + " &ahas un" + type.getType() + " " + target + " &afor " + packet.getReason() + "!", player -> {
                    Profile profile = ServiceContainer.getService(ProfileService.class).find(player.getUniqueId());
                    if (profile == null) return false;
                    if (!packet.isSilent()) return true;

                    return profile.getCurrentGrant().getData().isStaff() || player.isOp();
                });
                return;
            } else {
                String duration = packet.getDuration() == -1
                        ? " &apermanently for "
                        : " &atemporarily &7(" + TimeUtils.formatTime(packet.getDuration()) + ") &afor ";
                if (type == PunishmentType.KICK) duration = " &afor ";
                ServerUtils.sendMessage(author + " &ahas " + type.getType() + " " + target + duration + packet.getReason() + "!", player -> {
                    Profile profile = ServiceContainer.getService(ProfileService.class).find(player.getUniqueId());
                    if (profile == null) return false;
                    if (!packet.isSilent()) return true;

                    return profile.getCurrentGrant().getData().isStaff() || player.isOp();
                });
            }

            Player player = Bukkit.getPlayer(targetUUID);
            if (player == null) return;

            if (!packet.isRemoved() && (type == PunishmentType.BAN || type == PunishmentType.BLACKLIST || type == PunishmentType.KICK)) {
                String expire = packet.getDuration() == -1 ? "Never" : TimeUtils.formatDate(packet.getTimeCreated() + packet.getDuration());
                if (type != PunishmentType.KICK) {
                    player.kickPlayer(type.format(packet.getReason(), expire));
                } else {
                    player.kickPlayer(type.format(packet.getReason()));
                }

                if (type == PunishmentType.BLACKLIST) {
                    List<Profile> alts = ServiceContainer.getService(ProfileService.class).findFromAddress(targetProfile);

                    for (Profile alt : alts) {
                        Player altPlayer = Bukkit.getPlayer(alt.getUUID());
                        if (altPlayer == null) continue;

                        altPlayer.kickPlayer(
                                type.formatRelation(player.getName(), packet.getReason(), expire)
                        );
                    }
                }
                return;
            }

            if (!packet.isRemoved() && type == PunishmentType.MUTE) {
                String expire = packet.getDuration() == -1 ? "Never" : TimeUtils.formatDate(packet.getTimeCreated() + packet.getDuration());
                String message = PunishmentType.MUTE.format(packet.getReason(), expire);
                Arrays.stream(StringHelper.splitByNewline(message)).forEach(player::sendMessage);
            }
        });
    }
}
