package com.sylluxpvp.circuit.bukkit.menus;

import lombok.RequiredArgsConstructor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import com.sylluxpvp.circuit.bukkit.tools.menu.Button;
import com.sylluxpvp.circuit.bukkit.tools.menu.pagination.PaginatedMenu;
import com.sylluxpvp.circuit.bukkit.tools.spigot.ItemBuilder;
import com.sylluxpvp.circuit.shared.profile.Profile;
import com.sylluxpvp.circuit.shared.tag.Tag;
import com.sylluxpvp.circuit.shared.service.ServiceContainer;
import com.sylluxpvp.circuit.shared.service.impl.ProfileService;
import com.sylluxpvp.circuit.shared.service.impl.TagService;
import com.sylluxpvp.circuit.shared.tools.string.CC;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PrefixMenu extends PaginatedMenu {

    @Override
    public int getSize() {
        return 45;
    }

    @Override
    public String getPrePaginatedTitle(Player player) {
        return "&9Select a Prefix";
    }

    @Override
    public Map<Integer, Button> getAllPagesButtons(Player player) {
        Map<Integer, Button> buttons = new HashMap<>();
        TagService tagService = ServiceContainer.getService(TagService.class);
        Profile profile = ServiceContainer.getService(ProfileService.class).find(player.getUniqueId());

        for (Tag tag : tagService.getSortedTags()) {
            if (!player.hasPermission(tag.getPermission()) && !player.hasPermission("circuit.tag.*")) {
                continue;
            }
            buttons.put(buttons.size(), new TagButton(tag, profile));
        }

        return buttons;
    }

    @Override
    public Map<Integer, Button> getGlobalButtons(Player player) {
        Map<Integer, Button> buttons = new HashMap<>();
        Profile profile = ServiceContainer.getService(ProfileService.class).find(player.getUniqueId());
        buttons.put(getSize() - 5, new RemoveTagButton(profile));
        return buttons;
    }

    @RequiredArgsConstructor
    private static class TagButton extends Button {

        private final Tag tag;
        private final Profile profile;

        @Override
        public ItemStack getButtonItem(Player player) {
            boolean isSelected = profile.getActiveTagId() != null && profile.getActiveTagId().equals(tag.getUuid());
            
            ItemBuilder builder = new ItemBuilder(Material.NAME_TAG);
            if (isSelected) {
                builder.enchant(Enchantment.UNBREAKING);
            }
            builder.name((isSelected ? "&a" : "&9") + tag.getName());

            List<String> lore = new ArrayList<>();
            lore.add("");
            lore.add("&fDisplay: " + tag.getDisplay());
            lore.add("");
            if (isSelected) {
                lore.add("&a&lCurrently Selected!");
            } else {
                lore.add("&aClick to select!");
            }

            builder.lore(lore);
            return builder.build();
        }

        @Override
        public void clicked(Player player, ClickType clickType) {
            profile.setActiveTagId(tag.getUuid());
            ServiceContainer.getService(ProfileService.class).save(profile);
            player.sendMessage(CC.translate("&aYou have selected the " + tag.getDisplay() + " &aprefix!"));
            Button.playNeutral(player);
            new PrefixMenu().openMenu(player);
        }
    }

    @RequiredArgsConstructor
    private static class RemoveTagButton extends Button {

        private final Profile profile;

        @Override
        public ItemStack getButtonItem(Player player) {
            ItemBuilder builder = new ItemBuilder(Material.INK_SAC).dyeColor(org.bukkit.DyeColor.WHITE);
            builder.name("&c&lRemove Prefix");

            List<String> lore = new ArrayList<>();
            lore.add("");
            if (profile.getActiveTagId() == null) {
                lore.add("&7You don't have a prefix selected.");
            } else {
                Tag currentTag = ServiceContainer.getService(TagService.class).getTag(profile.getActiveTagId());
                lore.add("&7Current: " + (currentTag != null ? currentTag.getDisplay() : "&cNone"));
                lore.add("");
                lore.add("&cClick to remove!");
            }

            builder.lore(lore);
            return builder.build();
        }

        @Override
        public void clicked(Player player, ClickType clickType) {
            if (profile.getActiveTagId() == null) {
                Button.playFail(player);
                player.sendMessage(CC.translate("&cYou don't have a prefix selected."));
                return;
            }
            
            profile.setActiveTagId(null);
            ServiceContainer.getService(ProfileService.class).save(profile);
            player.sendMessage(CC.translate("&aYou have removed your prefix!"));
            Button.playNeutral(player);
            new PrefixMenu().openMenu(player);
        }
    }
}
