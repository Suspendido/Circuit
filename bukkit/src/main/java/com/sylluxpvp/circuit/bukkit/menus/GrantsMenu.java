package com.sylluxpvp.circuit.bukkit.menus;

import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import com.sylluxpvp.circuit.bukkit.menus.grant.GrantMenu;
import com.sylluxpvp.circuit.bukkit.service.BukkitGrantService;
import com.sylluxpvp.circuit.bukkit.tools.menu.Button;
import com.sylluxpvp.circuit.bukkit.tools.menu.Menu;
import com.sylluxpvp.circuit.bukkit.tools.spigot.ColorMapping;
import com.sylluxpvp.circuit.bukkit.tools.spigot.ItemBuilder;
import com.sylluxpvp.circuit.bukkit.tools.circuit.GrantUtils;
import com.sylluxpvp.circuit.shared.grant.Grant;
import com.sylluxpvp.circuit.shared.profile.Profile;
import com.sylluxpvp.circuit.shared.rank.Rank;
import com.sylluxpvp.circuit.shared.service.ServiceContainer;
import com.sylluxpvp.circuit.shared.service.impl.GrantService;
import com.sylluxpvp.circuit.shared.service.impl.ProfileService;
import com.sylluxpvp.circuit.shared.tools.java.TimeUtils;
import com.sylluxpvp.circuit.shared.tools.string.CC;

import java.util.*;
import java.util.stream.Collectors;

public class GrantsMenu extends Menu {

    private static final int MENU_SIZE = 45;

    private final UUID target;

    public GrantsMenu(UUID target) {
        this.target = target;
    }

    @Override
    public String getTitle(Player player) {
        Profile profile = ServiceContainer.getService(ProfileService.class).find(target);
        String name = profile != null ? profile.getName() : Bukkit.getOfflinePlayer(target).getName();
        return "&9Grants: " + name;
    }

    @Override
    public int getSize() {
        return MENU_SIZE;
    }

    @Override
    public Map<Integer, Button> getButtons(Player player) {
        Map<Integer, Button> buttons = new HashMap<>();
        fillBorder(buttons);
        Profile profile = ServiceContainer.getService(ProfileService.class).find(target);
        if (profile == null) return buttons;

        buttons.put(4, new PlayerInfoButton(profile));

        int slot = 10;
        Comparator<Grant<Rank>> comparator = Comparator.<Grant<Rank>>comparingInt(grant -> grant.getData().getWeight()).reversed();
        for (Grant<Rank> grant : profile.getRankGrants().stream().sorted(comparator).collect(Collectors.toList())) {
            while (isBorderSlot(slot) && slot < MENU_SIZE - 9) slot++;
            if (slot >= MENU_SIZE - 9) break;
            buttons.put(slot++, new GrantButton(profile, grant));
        }

        buttons.put(MENU_SIZE - 6, new AddGrantButton());
        buttons.put(MENU_SIZE - 5, new CloseButton());

        return buttons;
    }

    @RequiredArgsConstructor
    private class PlayerInfoButton extends Button {

        private final Profile profile;

        @Override
        public ItemStack getButtonItem(Player player) {
            ItemBuilder builder = new ItemBuilder(Material.SKULL_ITEM);
            builder.skull(profile.getName());
            builder.name(profile.getCurrentGrant().getData().getColor() + profile.getName());

            long activeGrants = profile.getRankGrants().stream().filter(Grant::isActive).count();
            long totalGrants = profile.getRankGrants().size();

            List<String> lore = new ArrayList<>();
            lore.add("");
            lore.add("&9&lGrant Summary");
            lore.add("&fCurrent Rank: " + profile.getCurrentGrant().getData().getColor() + profile.getCurrentGrant().getData().getName());
            lore.add("&fActive Grants: &a" + activeGrants);
            lore.add("&fTotal Grants: &9" + totalGrants);

            builder.lore(lore);
            return builder.build();
        }
    }

    private class AddGrantButton extends Button {

        @Override
        public ItemStack getButtonItem(Player player) {
            ItemBuilder builder = new ItemBuilder(Material.EMERALD);
            builder.name("&a&lAdd Grant");

            List<String> lore = new ArrayList<>();
            lore.add("&7Grant a new rank to this player.");
            lore.add("");
            lore.add("&aClick to add!");

            builder.lore(lore);
            return builder.build();
        }

        @Override
        public void clicked(Player player, ClickType clickType) {
            Button.playNeutral(player);
            new GrantMenu(target).openMenu(player);
        }
    }

    private class CloseButton extends Button {

        @Override
        public ItemStack getButtonItem(Player player) {
            ItemBuilder builder = new ItemBuilder(Material.REDSTONE);
            builder.name("&c&lClose");

            List<String> lore = new ArrayList<>();
            lore.add("&7Close this menu.");

            builder.lore(lore);
            return builder.build();
        }

        @Override
        public void clicked(Player player, ClickType clickType) {
            Button.playNeutral(player);
            player.closeInventory();
        }
    }

    @RequiredArgsConstructor
    private class GrantButton extends Button {

        private final Profile profile;
        private final Grant<Rank> grant;

        @Override
        public ItemStack getButtonItem(Player player) {
            ItemBuilder builder = new ItemBuilder(Material.INK_SACK);
            builder.amount(1);
            builder.durability(ColorMapping.getItemDurability(grant.getData().getColor()));
            builder.name(grant.getData().getColor() + grant.getData().getName() + " &7- " + TimeUtils.formatDate(grant.getTimeCreated()) + (grant.isActive() ? " &a(Active)" : " &c(Inactive)"));
            List<String> lore = new ArrayList<>();
            lore.add("");
            lore.add("&fIssued by: &9" + GrantUtils.getAuthor(grant));
            lore.add("&fReason: &9" + (grant.getReason() == null ? "&cNone" : grant.getReason()));
            lore.add("&fDuration: &9" + (grant.getDuration() == -1 ? "Permanent" : TimeUtils.formatTimeShort(grant.getDuration())));

            if (grant == ServiceContainer.getService(GrantService.class).getDefaultGrant() || grant.getData().isDefaultRank()) {
                lore.add("");
                lore.add("&cYou cannot remove this grant.");
                builder.lore(lore);
                return builder.build();
            }

            if (grant.isActive()) {
                lore.add("");
                lore.add("&cClick to remove!");
            } else {
                lore.add("");
                lore.add("&fRemoved by: &9" + GrantUtils.getRemover(grant));
                lore.add("&fRemoved at: &9" + TimeUtils.formatDate(grant.getRemovedAt()));
                lore.add("&fRemoval reason: &9" + (grant.getRemovalReason() == null ? "&cNone" : grant.getRemovalReason()));
                lore.add("");
                lore.add("&cClick to destroy!");
            }
            builder.lore(lore);
            return builder.build();
        }

        @Override
        public void clicked(Player player, ClickType clickType) {
            if (grant == ServiceContainer.getService(GrantService.class).getDefaultGrant() || grant.getData().isDefaultRank()) {
                player.sendMessage(CC.RED + "You cannot remove this grant!");
                return;
            }
            ServiceContainer.getService(BukkitGrantService.class).removeGrant(player.getUniqueId(), profile, grant);
            openMenu(player);
        }
    }
}
