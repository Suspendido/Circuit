package xyz.kayaaa.xenon.bukkit.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import xyz.kayaaa.xenon.bukkit.profile.BukkitProfile;
import xyz.kayaaa.xenon.bukkit.service.BukkitProfileService;
import xyz.kayaaa.xenon.shared.profile.Profile;
import xyz.kayaaa.xenon.shared.service.ServiceContainer;
import xyz.kayaaa.xenon.shared.service.impl.ProfileService;
import xyz.kayaaa.xenon.shared.tools.CC;

public class PlayerListener implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        Profile profile = ServiceContainer.getService(ProfileService.class).load(player.getUniqueId());
        BukkitProfile bukkitProfile = ServiceContainer.getService(BukkitProfileService.class).create(profile);
        bukkitProfile.setupPlayer();
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        Profile profile = ServiceContainer.getService(ProfileService.class).find(player.getUniqueId());
        ServiceContainer.getService(ProfileService.class).save(profile);
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        Profile profile = ServiceContainer.getService(ProfileService.class).find(player.getUniqueId());
        if (profile == null) {
            event.setCancelled(true);
            return;
        }

        event.setFormat(CC.translate(profile.getCurrentGrant().getData().getPrefix() + (profile.getColor() != null ? profile.getColor() : "") + player.getName() + profile.getCurrentGrant().getData().getSuffix() + "&7: &f" + event.getMessage()));
    }
}
