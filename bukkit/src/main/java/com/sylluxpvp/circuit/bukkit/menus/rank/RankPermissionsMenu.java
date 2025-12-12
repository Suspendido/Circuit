package com.sylluxpvp.circuit.bukkit.menus.rank;

import lombok.RequiredArgsConstructor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import com.sylluxpvp.circuit.bukkit.tools.menu.Button;
import com.sylluxpvp.circuit.bukkit.tools.menu.pagination.PaginatedMenu;
import com.sylluxpvp.circuit.bukkit.tools.spigot.ItemBuilder;
import com.sylluxpvp.circuit.shared.rank.Rank;
import com.sylluxpvp.circuit.shared.service.ServiceContainer;
import com.sylluxpvp.circuit.shared.service.impl.RankService;
import com.sylluxpvp.circuit.shared.tools.string.CC;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public class RankPermissionsMenu extends PaginatedMenu {

    private static final int MENU_SIZE = 45;

    private final Rank rank;

    @Override
    public int getSize() {
        return MENU_SIZE;
    }

    @Override
    public String getPrePaginatedTitle(Player player) {
        return "&9Permissions: " + rank.getName();
    }

    @Override
    public Map<Integer, Button> getAllPagesButtons(Player player) {
        Map<Integer, Button> buttons = new HashMap<>();

        for (String permission : rank.getPermissions()) {
            buttons.put(buttons.size(), new PermissionButton(permission));
        }

        return buttons;
    }

    @Override
    public Map<Integer, Button> getGlobalButtons(Player player) {
        Map<Integer, Button> buttons = new HashMap<>();
        buttons.put(MENU_SIZE - 5, new AddPermissionButton());
        buttons.put(MENU_SIZE - 6, new BackToEditorButton());
        return buttons;
    }

    @RequiredArgsConstructor
    private class PermissionButton extends Button {

        private final String permission;

        @Override
        public ItemStack getButtonItem(Player player) {
            ItemBuilder builder = new ItemBuilder(Material.PAPER);
            builder.name("&e" + permission);

            List<String> lore = new ArrayList<>();
            lore.add("");
            lore.add("&cClick to remove!");

            builder.lore(lore);
            return builder.build();
        }

        @Override
        public void clicked(Player player, ClickType clickType) {
            Button.playNeutral(player);
            rank.setPermission(permission);
            ServiceContainer.getService(RankService.class).save(rank);
            player.sendMessage(CC.RED + "Removed permission: " + permission);
            new RankPermissionsMenu(rank).openMenu(player);
        }
    }

    private class AddPermissionButton extends Button {

        @Override
        public ItemStack getButtonItem(Player player) {
            ItemBuilder builder = new ItemBuilder(Material.EMERALD);
            builder.name("&a&lAdd Permission");

            List<String> lore = new ArrayList<>();
            lore.add("&7Click to add a new permission.");
            lore.add("");
            lore.add("&eClick to add!");

            builder.lore(lore);
            return builder.build();
        }

        @Override
        public void clicked(Player player, ClickType clickType) {
            Button.playNeutral(player);
            player.closeInventory();
            new RankInputHandler(player, rank, RankInputHandler.InputType.PERMISSION, new RankPermissionsMenu(rank)).start();
        }
    }

    private class BackToEditorButton extends Button {

        @Override
        public ItemStack getButtonItem(Player player) {
            ItemBuilder builder = new ItemBuilder(Material.REDSTONE);
            builder.name("&c&lBack to Editor");

            List<String> lore = new ArrayList<>();
            lore.add("&7Return to the rank editor.");

            builder.lore(lore);
            return builder.build();
        }

        @Override
        public void clicked(Player player, ClickType clickType) {
            Button.playNeutral(player);
            new RankEditorMenu(rank).openMenu(player);
        }
    }
}
