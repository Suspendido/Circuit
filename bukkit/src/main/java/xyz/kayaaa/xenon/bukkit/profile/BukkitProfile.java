package xyz.kayaaa.xenon.bukkit.profile;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachment;
import xyz.kayaaa.xenon.bukkit.XenonPlugin;
import xyz.kayaaa.xenon.shared.grant.Grant;
import xyz.kayaaa.xenon.shared.profile.Profile;
import xyz.kayaaa.xenon.shared.punishment.Punishment;
import xyz.kayaaa.xenon.shared.punishment.PunishmentType;
import xyz.kayaaa.xenon.shared.redis.packets.staff.StaffStatusPacket;
import xyz.kayaaa.xenon.shared.service.ServiceContainer;
import xyz.kayaaa.xenon.shared.service.impl.ProfileService;
import xyz.kayaaa.xenon.shared.tools.java.TimeUtils;

import java.util.List;
import java.util.Set;

@RequiredArgsConstructor @Getter
public class BukkitProfile {

    private final Profile profile;
    private final Player player;

    public void setupPlayer() {
        profile.setName(player.getName());
        if (profile.findActivePunishment(PunishmentType.BAN) != null) {
            Grant<Punishment> punishmentGrant = profile.findActivePunishment(PunishmentType.BAN);
            player.kickPlayer(PunishmentType.BAN.format(punishmentGrant.getReason(), punishmentGrant.getDuration() == -1 ? "Never" : TimeUtils.formatDate(punishmentGrant.getTimeCreated() + punishmentGrant.getDuration())));
            return;
        }

        if (profile.findActivePunishment(PunishmentType.BLACKLIST) != null) {
            Grant<Punishment> punishmentGrant = profile.findActivePunishment(PunishmentType.BLACKLIST);
            player.kickPlayer(PunishmentType.BLACKLIST.format(punishmentGrant.getReason(), punishmentGrant.getDuration() == -1 ? "Never" : TimeUtils.formatDate(punishmentGrant.getTimeCreated() + punishmentGrant.getDuration())));
            return;
        }

        Set<Profile> alts = ServiceContainer.getService(ProfileService.class).findFromAddress(profile);
        if (!alts.isEmpty()) {
            for (Profile profile1 : alts) {
                if (profile1.findActivePunishment(PunishmentType.BLACKLIST) == null) continue;
                Grant<Punishment> punishmentGrant = profile1.findActivePunishment(PunishmentType.BLACKLIST);
                player.kickPlayer(PunishmentType.BLACKLIST.formatRelation(profile1.getName(), punishmentGrant.getReason(), punishmentGrant.getDuration() == -1 ? "Never" : TimeUtils.formatDate(punishmentGrant.getTimeCreated() + punishmentGrant.getDuration())));
                return;
            }
        }

        if (profile.getCurrentGrant() == null) {
            XenonPlugin.getInstance().getLogger().warning("Player " + player.getName() + " has no grant!");
            return;
        }

        if (profile.getCurrentGrant().getData() == null) {
            XenonPlugin.getInstance().getLogger().warning("Player " + player.getName() + " has no grant data!");
            return;
        }

        PermissionAttachment attachment = player.addAttachment(XenonPlugin.getInstance());
        for (String permission : profile.getCurrentGrant().getData().getPermissions()) {
            attachment.setPermission(permission, true);
        }
        player.recalculatePermissions();

        if (profile.getCurrentGrant().getData().isStaff()) {
            XenonPlugin.getInstance().getShared().getRedis().sendPacket(new StaffStatusPacket(player.getUniqueId(), true, XenonPlugin.getInstance().getShared().getServer().getName()));
        }
    }

    public void leave() {
        ServiceContainer.getService(ProfileService.class).save(profile);
        if (profile.getCurrentGrant().getData().isStaff()) {
            XenonPlugin.getInstance().getShared().getRedis().sendPacket(new StaffStatusPacket(player.getUniqueId(), false, XenonPlugin.getInstance().getShared().getServer().getName()));
        }
    }
}
