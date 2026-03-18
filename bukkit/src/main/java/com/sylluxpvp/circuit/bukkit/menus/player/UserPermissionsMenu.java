package com.sylluxpvp.circuit.bukkit.menus.player;

import com.sylluxpvp.circuit.bukkit.CircuitPlugin;
import com.sylluxpvp.circuit.bukkit.module.impl.PunishmentModule;
import com.sylluxpvp.circuit.bukkit.tools.menu.Button;
import com.sylluxpvp.circuit.bukkit.tools.menu.pagination.PaginatedMenu;
import com.sylluxpvp.circuit.bukkit.tools.spigot.ItemBuilder;
import com.sylluxpvp.circuit.shared.profile.Profile;
import com.sylluxpvp.circuit.shared.service.ServiceContainer;
import com.sylluxpvp.circuit.shared.service.impl.ProfileService;
import com.sylluxpvp.circuit.shared.tools.string.CC;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import lombok.Generated;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

public class UserPermissionsMenu extends PaginatedMenu {
    private static final int MENU_SIZE = 45;
    private final Profile targetProfile;

    @Override
    public int getSize() {
        return MENU_SIZE;
    }

    @Override
    public String getPrePaginatedTitle(Player player) {
        return "&9Permissions: " + this.targetProfile.getName();
    }

    @Override
    public Map<Integer, Button> getAllPagesButtons(Player player) {
        HashMap<Integer, Button> buttons = new HashMap<>();
        for (String permission : this.targetProfile.getPermissions()) {
            buttons.put(buttons.size(), new PermissionButton(permission));
        }
        return buttons;
    }

    @Override
    public Map<Integer, Button> getGlobalButtons(Player player) {
        HashMap<Integer, Button> buttons = new HashMap<>();
        buttons.put(40, new AddPermissionButton());
        buttons.put(41, new InfoButton());
        return buttons;
    }

    @Generated
    public UserPermissionsMenu(Profile targetProfile) {
        this.targetProfile = targetProfile;
    }

    private class PermissionButton
    extends Button {
        private final String permission;

        @Override
        public ItemStack getButtonItem(Player player) {
            boolean isNegative = this.permission.startsWith("-");
            ItemBuilder builder = new ItemBuilder(isNegative ? Material.REDSTONE : Material.EMERALD);
            builder.name((isNegative ? "&c" : "&a") + this.permission);
            ArrayList<String> lore = new ArrayList<>();
            lore.add("");
            lore.add(isNegative ? "&7This permission is &cnegated&7." : "&7This permission is &aactive&7.");
            lore.add("");
            lore.add("&cClick to remove!");
            builder.lore(lore);
            return builder.build();
        }

        @Override
        public void clicked(Player player, ClickType clickType) {
            PunishmentModule pm = CircuitPlugin.getInstance().getModuleManager().getModule(PunishmentModule.class);
            if (pm != null && !pm.canWrite()) {
                player.sendMessage(CC.translate("&cSystem temporarily unavailable."));
                Button.playFail(player);
                return;
            }
            Button.playNeutral(player);
            player.sendMessage(CC.translate("&7Processing..."));
            (ServiceContainer.getService(ProfileService.class).saveWithPendingPermission(UserPermissionsMenu.this.targetProfile, this.permission, false).thenAccept(success -> Bukkit.getScheduler().runTask(CircuitPlugin.getInstance(), () -> {
                if (success) {
                    UserPermissionsMenu.this.targetProfile.removePermission(this.permission);
                    player.sendMessage(CC.RED + "Removed permission: " + this.permission);
                    CircuitPlugin.getInstance().getLogger().info(player.getName() + " removed permission " + this.permission + " from " + UserPermissionsMenu.this.targetProfile.getName());
                } else {
                    player.sendMessage(CC.translate("&cFailed to save. Database may be unavailable."));
                }
                new UserPermissionsMenu(UserPermissionsMenu.this.targetProfile).openMenu(player);
            }))).exceptionally(ex -> {
                Bukkit.getScheduler().runTask(CircuitPlugin.getInstance(), () -> {
                    player.sendMessage(CC.translate("&cSystem unavailable."));
                    new UserPermissionsMenu(UserPermissionsMenu.this.targetProfile).openMenu(player);
                });
                return null;
            });
        }

        @Generated
        public PermissionButton(String permission) {
            this.permission = permission;
        }
    }

    private class AddPermissionButton
    extends Button {
        private AddPermissionButton() {
        }

        @Override
        public ItemStack getButtonItem(Player player) {
            ItemBuilder builder = new ItemBuilder(Material.NETHER_STAR);
            builder.name("&a&lAdd Permission");
            ArrayList<String> lore = new ArrayList<>();
            lore.add("&7Click to add a new permission.");
            lore.add("");
            lore.add("&7Use &f-permission &7to negate.");
            lore.add("");
            lore.add("&eClick to add!");
            builder.lore(lore);
            return builder.build();
        }

        @Override
        public void clicked(Player player, ClickType clickType) {
            Button.playNeutral(player);
            player.closeInventory();
            new UserPermissionInputHandler(player, UserPermissionsMenu.this.targetProfile, new UserPermissionsMenu(UserPermissionsMenu.this.targetProfile)).start();
        }
    }

    private class InfoButton
    extends Button {
        private InfoButton() {
        }

        @Override
        public ItemStack getButtonItem(Player player) {
            ItemBuilder builder = new ItemBuilder(Material.BOOK);
            builder.name("&9&lPlayer Info");
            ArrayList<String> lore = new ArrayList<>();
            lore.add("");
            lore.add("&fPlayer: &9" + UserPermissionsMenu.this.targetProfile.getName());
            lore.add("&fPermissions: &9" + UserPermissionsMenu.this.targetProfile.getPermissions().size());
            lore.add("");
            lore.add("&7These are individual permissions");
            lore.add("&7assigned directly to this player.");
            builder.lore(lore);
            return builder.build();
        }
    }
}

