package xyz.kayaaa.xenon.bukkit.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;
import xyz.kayaaa.xenon.bukkit.XenonPlugin;
import xyz.kayaaa.xenon.bukkit.profile.BukkitProfile;
import xyz.kayaaa.xenon.bukkit.service.BukkitChatService;
import xyz.kayaaa.xenon.bukkit.service.BukkitProfileService;
import xyz.kayaaa.xenon.shared.chat.ChatChannel;
import xyz.kayaaa.xenon.shared.grant.Grant;
import xyz.kayaaa.xenon.shared.profile.Profile;
import xyz.kayaaa.xenon.shared.punishment.Punishment;
import xyz.kayaaa.xenon.shared.punishment.PunishmentType;
import xyz.kayaaa.xenon.shared.redis.packets.staff.StaffChatPacket;
import xyz.kayaaa.xenon.shared.service.ServiceContainer;
import xyz.kayaaa.xenon.shared.service.impl.ProfileService;
import xyz.kayaaa.xenon.shared.tools.java.TimeUtils;
import xyz.kayaaa.xenon.shared.tools.string.CC;
import xyz.kayaaa.xenon.shared.tools.string.StringHelper;

import java.util.Arrays;

public class PlayerListener implements Listener {

    @EventHandler
    public void onAsyncPlayerLogin(AsyncPlayerPreLoginEvent event) {
        if (!XenonPlugin.getInstance().isJoinable()) {
            event.setLoginResult(AsyncPlayerPreLoginEvent.Result.KICK_OTHER);
            event.setKickMessage(CC.translate("&cServer is still loading up."));
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        event.setJoinMessage(null);
        Player player = event.getPlayer();
        Profile profile = ServiceContainer.getService(ProfileService.class).load(player.getUniqueId());
        String ip = player.getAddress().getAddress().getHostAddress();
        profile.setAddress(ip);
        BukkitProfile bukkitProfile = ServiceContainer.getService(BukkitProfileService.class).create(profile);
        bukkitProfile.setupPlayer();
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) {
        event.setQuitMessage(null);
        Player player = event.getPlayer();
        BukkitProfile bukkitProfile = ServiceContainer.getService(BukkitProfileService.class).find(player.getUniqueId());
        bukkitProfile.leave();
    }

    @EventHandler
    public void onPlayerKick(PlayerKickEvent event) {
        Player player = event.getPlayer();
        BukkitProfile bukkitProfile = ServiceContainer.getService(BukkitProfileService.class).find(player.getUniqueId());
        bukkitProfile.leave();
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
            String message = PunishmentType.MUTE.format(punishmentGrant.getReason(), expire);
            Arrays.stream(StringHelper.splitByNewline(message)).forEach(player::sendMessage);
        }

        if (profile.getChannel() == ChatChannel.STAFF) {
            event.setCancelled(true);
            XenonPlugin.getInstance().getShared().getRedis().sendPacket(new StaffChatPacket(player.getUniqueId(), XenonPlugin.getInstance().getShared().getServer().getName(), event.getMessage()));
            return;
        }

        if (!ServiceContainer.getService(BukkitChatService.class).canChat(player) && (!profile.getCurrentGrant().getData().isStaff() && !player.isOp())) {
            event.setCancelled(true);
            long seconds = ServiceContainer.getService(BukkitChatService.class).getSlowdown() - (System.currentTimeMillis() - ServiceContainer.getService(BukkitChatService.class).getChatCooldown().getOrDefault(player, 0L));
            player.sendMessage(CC.RED + "You're in chat cooldown. " + TimeUtils.formatTime(seconds) + " remaining.");
            return;
        }

        event.setFormat(CC.translate(profile.getCurrentGrant().getData().getPrefix() + (profile.getColor() != null && !profile.getColor().isEmpty() ? profile.getColor() : "") + player.getName() + profile.getCurrentGrant().getData().getSuffix() + "&7: &f" + event.getMessage()));
        ServiceContainer.getService(BukkitChatService.class).getChatCooldown().put(player, System.currentTimeMillis());
    }
}
