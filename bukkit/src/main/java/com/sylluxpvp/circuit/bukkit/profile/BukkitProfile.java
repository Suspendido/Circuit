package com.sylluxpvp.circuit.bukkit.profile;

import com.sylluxpvp.circuit.shared.redis.Redis;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachment;
import com.sylluxpvp.circuit.bukkit.CircuitPlugin;
import com.sylluxpvp.circuit.shared.grant.Grant;
import com.sylluxpvp.circuit.shared.profile.Profile;
import com.sylluxpvp.circuit.shared.punishment.Punishment;
import com.sylluxpvp.circuit.shared.punishment.PunishmentType;
import com.sylluxpvp.circuit.shared.redis.packets.staff.StaffStatusPacket;
import com.sylluxpvp.circuit.shared.service.ServiceContainer;
import com.sylluxpvp.circuit.shared.service.impl.ProfileService;
import com.sylluxpvp.circuit.shared.tools.java.TimeUtils;
import redis.clients.jedis.Jedis;

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
            CircuitPlugin.getInstance().getLogger().warning("Player " + player.getName() + " has no grant!");
            return;
        }

        if (profile.getCurrentGrant().getData() == null) {
            CircuitPlugin.getInstance().getLogger().warning("Player " + player.getName() + " has no grant data!");
            return;
        }

        PermissionAttachment attachment = player.addAttachment(CircuitPlugin.getInstance());
        for (String permission : profile.getCurrentGrant().getData().getPermissions()) {
            attachment.setPermission(permission, true);
        }
        player.recalculatePermissions();

        if (profile.getCurrentGrant().getData().isStaff()) {
            String currentServer = CircuitPlugin.getInstance().getShared().getServer().getName();
            String previousServer = getLastServer();

            String action = previousServer != null ? "serverSwitch" : "networkConnect";

            CircuitPlugin.getInstance().getShared().getRedis().sendPacket(
                    new StaffStatusPacket(player.getUniqueId(), true, currentServer, previousServer, action)
            );

            setLastServer(currentServer);
        }
    }

    public void leave() {
        ServiceContainer.getService(ProfileService.class).save(profile);

        if (profile.getCurrentGrant() == null || profile.getCurrentGrant().getData() == null) return;
        if (!profile.getCurrentGrant().getData().isStaff()) return;

        markLeaving();

        Bukkit.getScheduler().runTaskLaterAsynchronously(CircuitPlugin.getInstance(), () -> {
            try (Jedis jedis = createJedis()) {
                if (jedis == null) return;
                String key = "lastServer:" + player.getUniqueId();
                String lastServer = jedis.get(key);
                String currentServer = CircuitPlugin.getInstance().getShared().getServer().getName();

                if (currentServer.equals(lastServer)) {
                    CircuitPlugin.getInstance().getShared().getRedis().sendPacket(
                            new StaffStatusPacket(player.getUniqueId(), false, lastServer, lastServer, "networkDisconnect")
                    );
                    jedis.del(key);
                }
            } catch (Exception ignored) {}
        }, 10 * 20L);
    }

    private String getLastServer() {
        try (Jedis jedis = CircuitPlugin.getInstance().getShared().getRedis().getJedisPublisher()) {
            return jedis.get("lastServer:" + player.getUniqueId());
        } catch (Exception e) {
            return null;
        }
    }

    private void setLastServer(String server) {
        try (Jedis jedis = createJedis()) {
            if (jedis == null) return;
            String key = "lastServer:" + player.getUniqueId();
            if (server == null) {
                jedis.del(key);
            } else {
                jedis.setex(key, 60, server);
            }
        } catch (Exception ignored) {}
    }

    private void markLeaving() {
        try (Jedis jedis = createJedis()) {
            if (jedis == null) return;
            String key = "lastServer:" + player.getUniqueId();
            String currentServer = CircuitPlugin.getInstance().getShared().getServer().getName();
            jedis.setex(key, 10, currentServer);
        } catch (Exception ignored) {}
    }

    private Jedis createJedis() {
        try {
            Redis redis = CircuitPlugin.getInstance().getShared().getRedis();
            Jedis jedis = new Jedis(redis.getHost(), redis.getPort());
            if (redis.getPassword() != null && !redis.getPassword().isEmpty()) {
                jedis.auth(redis.getPassword());
            }
            return jedis;
        } catch (Exception e) {
            return null;
        }
    }
}
