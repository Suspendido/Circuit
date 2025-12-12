package com.sylluxpvp.circuit.bukkit.listener;

import com.sylluxpvp.circuit.bukkit.command.staff.AdminChatCommand;
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
import com.sylluxpvp.circuit.shared.redis.packets.staff.StaffChatPacket;
import com.sylluxpvp.circuit.shared.service.ServiceContainer;
import com.sylluxpvp.circuit.shared.service.impl.ProfileService;
import com.sylluxpvp.circuit.shared.tools.java.TimeUtils;
import com.sylluxpvp.circuit.shared.tools.string.CC;
import com.sylluxpvp.circuit.shared.tools.string.StringHelper;

import java.util.Arrays;

public class PlayerListener implements Listener {

    @EventHandler
    public void onAsyncPlayerLogin(AsyncPlayerPreLoginEvent event) {
        if (!CircuitPlugin.getInstance().isJoinable()) {
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
            CircuitPlugin.getInstance().getShared().getRedis().sendPacket(new StaffChatPacket(player.getUniqueId(), CircuitPlugin.getInstance().getShared().getServer().getName(), event.getMessage()));
            return;
        }

        if (profile.getChannel() == ChatChannel.ADMIN) {
            event.setCancelled(true);
            AdminChatCommand.sendAdminMessage(player, event.getMessage());
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

        String chatColor = profile.getColor() != null && !profile.getColor().isEmpty() 
                ? profile.getColor() 
                : "&f";
        event.setFormat(CC.translate(profile.getCurrentGrant().getData().getPrefix() + profile.getCurrentGrant().getData().getColor() + player.getName() + profile.getCurrentGrant().getData().getSuffix() + "&7: " + chatColor + event.getMessage()));
        ServiceContainer.getService(BukkitChatService.class).getChatCooldown().put(player, System.currentTimeMillis());
    }
}
