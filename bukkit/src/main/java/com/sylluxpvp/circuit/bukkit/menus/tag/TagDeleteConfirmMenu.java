package com.sylluxpvp.circuit.bukkit.menus.tag;

import lombok.RequiredArgsConstructor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import com.sylluxpvp.circuit.bukkit.tools.menu.Button;
import com.sylluxpvp.circuit.bukkit.tools.menu.Menu;
import com.sylluxpvp.circuit.bukkit.tools.spigot.ItemBuilder;
import com.sylluxpvp.circuit.shared.tag.Tag;
import com.sylluxpvp.circuit.shared.service.ServiceContainer;
import com.sylluxpvp.circuit.shared.service.impl.TagService;
import com.sylluxpvp.circuit.shared.tools.string.CC;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public class TagDeleteConfirmMenu extends Menu {

    private final Tag tag;

    @Override
    public int getSize() {
        return 27;
    }

    @Override
    public String getTitle(Player player) {
        return "&cDelete Tag: " + tag.getName() + "?";
    }

    @Override
    public Map<Integer, Button> getButtons(Player player) {
        Map<Integer, Button> buttons = new HashMap<>();
        fillBorder(buttons);

        buttons.put(11, new ConfirmButton());
        buttons.put(15, new CancelButton());

        return buttons;
    }

    private class ConfirmButton extends Button {

        @Override
        public ItemStack getButtonItem(Player player) {
            ItemBuilder builder = new ItemBuilder(Material.LIME_WOOL);
            builder.name("&a&lConfirm Delete");

            List<String> lore = new ArrayList<>();
            lore.add("&7Click to delete the tag");
            lore.add("&7" + tag.getName() + " &7permanently.");
            lore.add("");
            lore.add("&c&lThis cannot be undone!");

            builder.lore(lore);
            return builder.build();
        }

        @Override
        public void clicked(Player player, ClickType clickType) {
            TagService service = ServiceContainer.getService(TagService.class);
            service.delete(tag);
            player.sendMessage(CC.translate("&aTag " + tag.getName() + " has been deleted!"));
            Button.playNeutral(player);
            new TagListMenu().openMenu(player);
        }
    }

    private static class CancelButton extends Button {

        @Override
        public ItemStack getButtonItem(Player player) {
            ItemBuilder builder = new ItemBuilder(Material.RED_WOOL);
            builder.name("&c&lCancel");

            List<String> lore = new ArrayList<>();
            lore.add("&7Click to go back.");

            builder.lore(lore);
            return builder.build();
        }

        @Override
        public void clicked(Player player, ClickType clickType) {
            Button.playNeutral(player);
            new TagListMenu().openMenu(player);
        }
    }
}
