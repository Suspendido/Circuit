package com.sylluxpvp.circuit.bukkit.listener;

import com.sylluxpvp.circuit.bukkit.command.staff.AdminChatCommand;
import com.sylluxpvp.circuit.bukkit.command.staff.StaffChatCommand;
import com.sylluxpvp.circuit.shared.CircuitConstants;
import com.sylluxpvp.circuit.shared.cache.ProfileCache;
import com.sylluxpvp.circuit.shared.rank.Rank;
import com.sylluxpvp.circuit.shared.server.Server;
import com.sylluxpvp.circuit.shared.service.impl.*;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;
import com.sylluxpvp.circuit.bukkit.CircuitPlugin;
import com.sylluxpvp.circuit.bukkit.profile.BukkitProfile;
import com.sylluxpvp.circuit.bukkit.service.BukkitChatService;
import com.sylluxpvp.circuit.bukkit.service.BukkitProfileService;
import com.sylluxpvp.circuit.shared.chat.ChatChannel;
import com.sylluxpvp.circuit.shared.grant.Grant;
import com.sylluxpvp.circuit.shared.profile.Profile;
import com.sylluxpvp.circuit.shared.punishment.Punishment;
import com.sylluxpvp.circuit.shared.punishment.PunishmentType;
import com.sylluxpvp.circuit.shared.redis.packets.misc.MessagePacket;
import com.sylluxpvp.circuit.shared.service.ServiceContainer;
import com.sylluxpvp.circuit.shared.tag.Tag;
import com.sylluxpvp.circuit.shared.redis.packets.queue.QueueLeavePacket;
import com.sylluxpvp.circuit.shared.tools.java.TimeUtils;
import com.sylluxpvp.circuit.shared.tools.string.CC;
import com.sylluxpvp.circuit.shared.tools.string.StringHelper;

import java.io.IOException;
import java.net.URL;
import java.util.*;

public class PlayerListener implements Listener {

    @EventHandler
    public void onAsyncPlayerLogin(AsyncPlayerPreLoginEvent event) {
        if (!CircuitPlugin.getInstance().isJoinable()) {
            event.setLoginResult(AsyncPlayerPreLoginEvent.Result.KICK_OTHER);
            event.setKickMessage(CC.translate("&cServer is still loading up."));
            return;
        }
        Server server = CircuitPlugin.getInstance().getShared().getServer();
        if (server.isWhitelisted()) {
            UUID uuid = event.getUniqueId();
            if (server.isPlayerWhitelisted(uuid)) {
                return;
            }
            Profile profile = ServiceContainer.getService(ProfileService.class).load(uuid);
            if (profile != null) {
                RankService rankService = ServiceContainer.getService(RankService.class);
                Rank playerRank = profile.getCurrentGrant() != null ? profile.getCurrentGrant().getData() : null;
                Rank whitelistRank = rankService.getRank(server.getWhitelistRank());
                if (playerRank != null && whitelistRank != null && playerRank.getWeight() >= whitelistRank.getWeight()) {
                    return;
                }
            }
            event.setLoginResult(AsyncPlayerPreLoginEvent.Result.KICK_WHITELIST);
            event.setKickMessage(CC.translate("&cYou are not whitelisted on this server!"));
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        event.setJoinMessage(null);
        Player player = event.getPlayer();

        ProfileService profileService = ServiceContainer.getService(ProfileService.class);
        BukkitProfileService bukkitProfileService = ServiceContainer.getService(BukkitProfileService.class);
        ProfileCache.remove(player.getUniqueId());
        profileService.getOnlineProfiles().removeIf(p -> p.getUUID().equals(player.getUniqueId()));
        bukkitProfileService.getProfiles().removeIf(p -> p.getProfile().getUUID().equals(player.getUniqueId()));

        Profile profile = profileService.load(player.getUniqueId());

        if (profile.getName() == null) {
            profile.setName(player.getName());
        }

        String ip = player.getAddress().getAddress().getHostAddress();
        profile.setAddress(ip);
        BukkitProfile bukkitProfile = bukkitProfileService.create(profile);
        bukkitProfile.setupPlayer();

        Bukkit.getScheduler().runTaskAsynchronously(CircuitPlugin.getInstance(), () -> {
            boolean voted = hasVotedOnNameMC(player.getUniqueId());

            Bukkit.getScheduler().runTask(CircuitPlugin.getInstance(), () -> {
                if (!player.isOnline()) return;

                if (voted) {
                    rewardNameMCVote(player, profile, profileService);
                } else {
                    player.sendMessage(CC.translate("&eParece que todavia no nos das like en NameMC todavía, ve a"));
                    player.sendMessage(CC.translate("&ahttps://namemc.com/server/mine.lc &epara recibir el ✔ Verified Tag."));
                }
            });
        });
    }

    private void rewardNameMCVote(Player player, Profile profile, ProfileService profileService) {
        RankService rankService = ServiceContainer.getService(RankService.class);
        TagService tagService = ServiceContainer.getService(TagService.class);
        GrantService grantService = ServiceContainer.getService(GrantService.class);

        if (profile.isNameMcVoted()) return;

        Rank vipRank = rankService.getRank("VIP");
        if (vipRank == null) {
            player.sendMessage(CC.translate("&aGracias por darnos like en NameMC!"));
            return;
        }

        boolean hasVip = profile.getRankGrants().stream().anyMatch(g -> g.isActive() && g.getData() != null && g.getData().getName().equalsIgnoreCase("VIP"));

        if (!hasVip) {
            long minDuration = 7L * 24 * 60 * 60 * 1000;
            long maxDuration = 14L * 24 * 60 * 60 * 1000;
            long duration = minDuration + (long) (Math.random() * (maxDuration - minDuration));

            Grant<Rank> vipGrant = grantService.createGrant(vipRank, CircuitConstants.getConsoleUUID(), duration, "NameMC Vote Reward");
            profile.addGrant(vipGrant);
            profileService.save(profile);

            player.sendMessage(CC.translate("&aGracias por darnos like en NameMC! Has recibido el rango &6VIP &apor " + TimeUtils.formatTimeShort(duration) + "!"));
        }

        if (profile.getActiveTagId() == null) {
            List<Tag> availableTags = tagService.getTags().stream().filter(Objects::nonNull).toList();
            if (!availableTags.isEmpty()) {
                Tag randomTag = availableTags.get((int) (Math.random() * availableTags.size()));
                profile.setActiveTagId(randomTag.getUuid());
                profileService.save(profile);
                player.sendMessage(CC.translate("&aTambien recibiste el tag: " + randomTag.getDisplay() + "&a!"));
            }
        }
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) {
        event.setQuitMessage(null);
        Player player = event.getPlayer();
        BukkitProfile bukkitProfile = ServiceContainer.getService(BukkitProfileService.class).find(player.getUniqueId());
        if (bukkitProfile != null) bukkitProfile.leave();

        if (ServiceContainer.getService(QueueService.class).getPlayerQueueName(player.getUniqueId()) != null) {
            CircuitPlugin.getInstance().getShared().getRedis().sendPacket(new QueueLeavePacket(player.getUniqueId()));
        }
    }

    @EventHandler
    public void onPlayerKick(PlayerKickEvent event) {
        Player player = event.getPlayer();
        BukkitProfile bukkitProfile = ServiceContainer.getService(BukkitProfileService.class).find(player.getUniqueId());
        if (bukkitProfile != null) bukkitProfile.leave();

        if (ServiceContainer.getService(QueueService.class).getPlayerQueueName(player.getUniqueId()) != null) {
            CircuitPlugin.getInstance().getShared().getRedis().sendPacket(new QueueLeavePacket(player.getUniqueId()));
        }
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        Profile profile = ServiceContainer.getService(ProfileService.class).find(player.getUniqueId());
        if (profile == null) {
            event.setCancelled(true);
            return;
        }

        if (!ServiceContainer.getService(BukkitChatService.class).isChatEnabled() && (!profile.getCurrentGrant().getData().isStaff() && !player.isOp())) {
            event.setCancelled(true);
            player.sendMessage(CC.RED + "Chat is disabled.");
            return;
        }

        if (profile.findActivePunishment(PunishmentType.MUTE) != null) {
            Grant<Punishment> punishmentGrant = profile.findActivePunishment(PunishmentType.MUTE);
            event.setCancelled(true);
            String expire = punishmentGrant.getDuration() == -1 ? "Never" : TimeUtils.formatDate(punishmentGrant.getTimeCreated() + punishmentGrant.getDuration());
            String message = PunishmentType.MUTE.format(expire, punishmentGrant.getReason());
            Arrays.stream(StringHelper.splitByNewline(message)).forEach(player::sendMessage);
        }

        if (profile.getChannel() == ChatChannel.ADMIN) {
            event.setCancelled(true);
            AdminChatCommand.sendAdminMessage(player, event.getMessage());
            return;
        }

        if (profile.getChannel() == ChatChannel.STAFF) {
            event.setCancelled(true);
            StaffChatCommand.sendStaffMessage(player, event.getMessage());
            return;
        }

        if (!ServiceContainer.getService(BukkitChatService.class).canChat(player) && (!profile.getCurrentGrant().getData().isStaff() && !player.isOp())) {
            event.setCancelled(true);
            long seconds = ServiceContainer.getService(BukkitChatService.class).getSlowdown() - (System.currentTimeMillis() - ServiceContainer.getService(BukkitChatService.class).getChatCooldown().getOrDefault(player, 0L));
            player.sendMessage(CC.RED + "You're in chat cooldown. " + TimeUtils.formatTime(seconds) + " remaining.");
            return;
        }

        if (ServiceContainer.getService(BukkitChatService.class).shouldFilter(player, event.getMessage())) {
            event.setCancelled(true);
            player.sendMessage(CC.RED + "Your message was filtered.");
            CircuitPlugin.getInstance().getShared().getRedis().sendPacket(new MessagePacket(CircuitPlugin.getInstance().getShared().getServer().getName(), "&c[Filtered] " + profile.getCurrentGrant().getData().getColor() + player.getName() + "&7: &c" + event.getMessage(), true));
            return;
        }

        String chatColor = profile.getColor() != null && !profile.getColor().isEmpty() ? profile.getColor() : "&f";
        Tag activeTag = profile.getActiveTag();
        String tagDisplay = activeTag != null ? activeTag.getDisplay() + " " : "";
        String vipIcon = profile.getVipIcon();
        String vipDisplay = !vipIcon.isEmpty() ? vipIcon + " " : "";
        event.setFormat(CC.translate(vipDisplay + tagDisplay + profile.getCurrentGrant().getData().getPrefix() + profile.getCurrentGrant().getData().getColor() + player.getName() + profile.getCurrentGrant().getData().getSuffix() + "&7: " + chatColor + event.getMessage()));
        ServiceContainer.getService(BukkitChatService.class).getChatCooldown().put(player, System.currentTimeMillis());
    }

    public static boolean hasVotedOnNameMC(UUID uuid) {
        return hasVotedOnServer(uuid, "mine.lc") || hasVotedOnServer(uuid, "xminelc.com");
    }

    private static boolean hasVotedOnServer(UUID uuid, String server) {
        try (Scanner scanner = new Scanner(new URL("https://api.namemc.com/server/" + server + "/likes?profile=" + uuid.toString()).openStream()).useDelimiter("\\A")) {
            return Boolean.parseBoolean(scanner.next());
        } catch (IOException e) {
            return false;
        }
    }
}
