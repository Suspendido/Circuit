package com.sylluxpvp.circuit.bukkit.menus.player;

import com.sylluxpvp.circuit.bukkit.CircuitPlugin;
import com.sylluxpvp.circuit.bukkit.module.impl.PunishmentModule;
import com.sylluxpvp.circuit.bukkit.tools.menu.Menu;
import com.sylluxpvp.circuit.shared.profile.Profile;
import com.sylluxpvp.circuit.shared.service.ServiceContainer;
import com.sylluxpvp.circuit.shared.service.impl.ProfileService;
import com.sylluxpvp.circuit.shared.tools.string.CC;

import java.util.logging.Logger;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class UserPermissionInputHandler implements Listener {
    private static final Logger LOGGER = CircuitPlugin.getInstance().getLogger();
    private static final String CANCEL_KEYWORD = "cancel";
    private final Player player;
    private final Profile targetProfile;
    private final Menu returnMenu;

    public UserPermissionInputHandler(Player player, Profile targetProfile, Menu returnMenu) {
        this.player = player;
        this.targetProfile = targetProfile;
        this.returnMenu = returnMenu;
    }

    public void start() {
        CircuitPlugin.getInstance().getServer().getPluginManager().registerEvents(this, CircuitPlugin.getInstance());
        this.player.sendMessage(CC.translate("&aType the permission to add (use &f-permission &ato negate)"));
        this.player.sendMessage(CC.translate("&7Type &ccancel &7to cancel."));
    }

    @EventHandler(priority=EventPriority.LOWEST)
    public void onChat(AsyncPlayerChatEvent event) {
        if (!event.getPlayer().getUniqueId().equals(this.player.getUniqueId())) {
            return;
        }
        event.setCancelled(true);
        String message = event.getMessage().trim();
        if (message.equalsIgnoreCase(CANCEL_KEYWORD)) {
            this.cleanup();
            this.player.sendMessage(CC.translate("&cCancelled."));
            CircuitPlugin.getInstance().getServer().getScheduler().runTask(CircuitPlugin.getInstance(), () -> this.returnMenu.openMenu(this.player));
            return;
        }
        CircuitPlugin.getInstance().getServer().getScheduler().runTask(CircuitPlugin.getInstance(), () -> {
            PunishmentModule pm = CircuitPlugin.getInstance().getModuleManager().getModule(PunishmentModule.class);
            if (pm != null && !pm.canWrite()) {
                this.player.sendMessage(CC.translate("&cSystem temporarily unavailable. Please try again later."));
                this.cleanup();
                this.returnMenu.openMenu(this.player);
                return;
            }
            boolean hadPermission = this.targetProfile.hasPermission(message);
            boolean add = !hadPermission;
            this.player.sendMessage(CC.translate("&7Processing..."));
            (ServiceContainer.getService(ProfileService.class).saveWithPendingPermission(this.targetProfile, message, add).thenAccept(success -> CircuitPlugin.getInstance().getServer().getScheduler().runTask(CircuitPlugin.getInstance(), () -> {
                if (success) {
                    if (add) {
                        this.targetProfile.addPermission(message);
                        this.player.sendMessage(CC.translate("&aAdded permission: " + message));
                        LOGGER.info(this.player.getName() + " added permission " + message + " to " + this.targetProfile.getName());
                    } else {
                        this.targetProfile.removePermission(message);
                        this.player.sendMessage(CC.translate("&cRemoved permission: " + message));
                        LOGGER.info(this.player.getName() + " removed permission " + message + " from " + this.targetProfile.getName());
                    }
                } else {
                    this.player.sendMessage(CC.translate("&cFailed to save. Database may be unavailable."));
                }
                this.cleanup();
                new UserPermissionsMenu(this.targetProfile).openMenu(this.player);
            }))).exceptionally(ex -> {
                CircuitPlugin.getInstance().getServer().getScheduler().runTask(CircuitPlugin.getInstance(), () -> {
                    this.player.sendMessage(CC.translate("&cSystem unavailable. Please try again later."));
                    this.cleanup();
                    new UserPermissionsMenu(this.targetProfile).openMenu(this.player);
                });
                return null;
            });
        });
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        if (event.getPlayer().getUniqueId().equals(this.player.getUniqueId())) {
            this.cleanup();
        }
    }

    private void cleanup() {
        HandlerList.unregisterAll(this);
    }
}

