package com.sylluxpvp.circuit.bukkit.menus;

import com.sylluxpvp.circuit.bukkit.tools.xenon.GrantUtils;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import com.sylluxpvp.circuit.bukkit.tools.menu.Button;
import com.sylluxpvp.circuit.bukkit.tools.menu.Menu;
import com.sylluxpvp.circuit.bukkit.tools.spigot.ColorMapping;
import com.sylluxpvp.circuit.bukkit.tools.spigot.ItemBuilder;
import com.sylluxpvp.circuit.shared.grant.Grant;
import com.sylluxpvp.circuit.shared.profile.Profile;
import com.sylluxpvp.circuit.shared.punishment.Punishment;
import com.sylluxpvp.circuit.shared.punishment.PunishmentType;
import com.sylluxpvp.circuit.shared.service.ServiceContainer;
import com.sylluxpvp.circuit.shared.service.impl.ProfileService;
import com.sylluxpvp.circuit.shared.tools.java.TimeUtils;
import com.sylluxpvp.circuit.shared.tools.string.CC;

import java.util.*;

public class PunishmentsMenu extends Menu {

    private static final int MENU_SIZE = 45;

    // Mapeo de DyeColor a Material de tinte
    private static final Map<DyeColor, Material> dyeColorToMaterial = new HashMap<>() {{
        put(DyeColor.RED, Material.RED_DYE);
        put(DyeColor.GREEN, Material.GREEN_DYE);
        put(DyeColor.ORANGE, Material.ORANGE_DYE);
        put(DyeColor.PURPLE, Material.PURPLE_DYE);
        put(DyeColor.WHITE, Material.WHITE_DYE);
        put(DyeColor.LIGHT_BLUE, Material.LIGHT_BLUE_DYE);
        put(DyeColor.YELLOW, Material.YELLOW_DYE);
        put(DyeColor.LIME, Material.LIME_DYE);
        put(DyeColor.PINK, Material.PINK_DYE);
        put(DyeColor.GRAY, Material.GRAY_DYE);
        put(DyeColor.LIGHT_GRAY, Material.LIGHT_GRAY_DYE);
        put(DyeColor.CYAN, Material.CYAN_DYE);
        put(DyeColor.BLUE, Material.BLUE_DYE);
        put(DyeColor.BROWN, Material.BROWN_DYE);
        put(DyeColor.BLACK, Material.BLACK_DYE);
        put(DyeColor.MAGENTA, Material.MAGENTA_DYE);
    }};

    private final UUID target;
    @Setter private PunishmentType type;

    public PunishmentsMenu(UUID target) {
        this.target = target;
    }

    @Override
    public String getTitle(Player player) {
        Profile profile = ServiceContainer.getService(ProfileService.class).find(target);
        String name = profile != null ? profile.getName() : Bukkit.getOfflinePlayer(target).getName();
        return type == null ? "&9Punishments: " + name : "&9" + type.prettyName() + "s: " + name;
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

        if (type == null) {
            buttons.put(4, new PlayerInfoButton(profile));
            buttons.put(19, new PunishmentTypeButton(profile, PunishmentType.KICK));
            buttons.put(21, new PunishmentTypeButton(profile, PunishmentType.MUTE));
            buttons.put(23, new PunishmentTypeButton(profile, PunishmentType.BAN));
            buttons.put(25, new PunishmentTypeButton(profile, PunishmentType.BLACKLIST));
            buttons.put(MENU_SIZE - 5, new CloseButton());
        } else {
            buttons.put(4, new TypeInfoButton(profile));
            int slot = 10;
            List<Grant<Punishment>> punishments = profile.getPunishments().stream()
                    .filter(grant -> grant.getData().getPunishmentType() == type)
                    .sorted(Comparator.<Grant<Punishment>>comparingLong(Grant::getTimeCreated).reversed())
                    .toList();

            for (Grant<Punishment> grant : punishments) {
                while (isBorderSlot(slot) && slot < MENU_SIZE - 9) slot++;
                if (slot >= MENU_SIZE - 9) break;
                buttons.put(slot++, new PunishmentButton(grant, profile));
            }
            buttons.put(MENU_SIZE - 5, new BackButton());
        }

        return buttons;
    }

    @RequiredArgsConstructor
    private static class PlayerInfoButton extends Button {

        private final Profile profile;

        @Override
        public ItemStack getButtonItem(Player player) {
            ItemBuilder builder = new ItemBuilder(Material.PLAYER_HEAD);
            builder.skull(profile.getName());
            builder.name(profile.getCurrentGrant().getData().getColor() + profile.getName());

            int total = profile.getPunishments().size();
            long active = profile.getPunishments().stream().filter(Grant::isActive).count();

            List<String> lore = new ArrayList<>();
            lore.add("");
            lore.add("&9&lPunishment Summary");
            lore.add("&fTotal: &9" + total);
            lore.add("&fActive: &c" + active);
            lore.add("");
            lore.add("&fKicks: &9" + profile.getAllPunishmentsByType(PunishmentType.KICK).size());
            lore.add("&fMutes: &9" + profile.getAllPunishmentsByType(PunishmentType.MUTE).size());
            lore.add("&fBans: &9" + profile.getAllPunishmentsByType(PunishmentType.BAN).size());
            lore.add("&fBlacklists: &9" + profile.getAllPunishmentsByType(PunishmentType.BLACKLIST).size());

            builder.lore(lore);
            return builder.build();
        }
    }

    @RequiredArgsConstructor
    private class TypeInfoButton extends Button {

        private final Profile profile;

        @Override
        public ItemStack getButtonItem(Player player) {
            List<Grant<Punishment>> punishments = profile.getAllPunishmentsByType(type);
            long active = punishments.stream().filter(Grant::isActive).count();

            DyeColor dyeColor = ColorMapping.getColor(type);
            Material dyeMaterial = dyeColorToMaterial.getOrDefault(dyeColor, Material.WHITE_DYE);

            ItemBuilder builder = new ItemBuilder(dyeMaterial);
            builder.name(ColorMapping.dyeColorToChatColor(dyeColor) + type.prettyName() + "s");

            List<String> lore = new ArrayList<>();
            lore.add("");
            lore.add("&fTotal: &9" + punishments.size());
            lore.add("&fActive: " + (active > 0 ? "&c" : "&a") + active);

            builder.lore(lore);
            return builder.build();
        }
    }

    @RequiredArgsConstructor
    private class PunishmentTypeButton extends Button {

        private final Profile profile;
        private final PunishmentType punishmentType;

        @Override
        public ItemStack getButtonItem(Player player) {
            List<Grant<Punishment>> punishments = profile.getAllPunishmentsByType(punishmentType);
            long active = punishments.stream().filter(Grant::isActive).count();

            DyeColor dyeColor = ColorMapping.getColor(punishmentType);
            Material dyeMaterial = dyeColorToMaterial.getOrDefault(dyeColor, Material.WHITE_DYE);
            ItemBuilder builder = new ItemBuilder(dyeMaterial);
            builder.name("&9&l" + punishmentType.prettyName() + "s");

            List<String> lore = new ArrayList<>();
            lore.add("");
            lore.add("&fTotal: &9" + punishments.size());
            lore.add("&fActive: " + (active > 0 ? "&c" : "&a") + active);
            lore.add("");
            lore.add("&aClick to view!");

            builder.lore(lore);
            return builder.build();
        }

        @Override
        public void clicked(Player player, ClickType clickType) {
            Button.playNeutral(player);
            PunishmentsMenu menu = new PunishmentsMenu(target);
            menu.setType(punishmentType);
            menu.openMenu(player);
        }
    }

    @RequiredArgsConstructor
    private class PunishmentButton extends Button {

        private final Grant<Punishment> grant;
        private final Profile profile;

        @Override
        public ItemStack getButtonItem(Player player) {
            boolean active = grant.isActive();

            ItemBuilder builder = new ItemBuilder(Material.PAPER);
            builder.name((active ? "&a" : "&c") + TimeUtils.formatDate(grant.getTimeCreated()));

            List<String> lore = new ArrayList<>();
            lore.add("");
            lore.add("&fStatus: " + (active ? "&aActive" : "&cInactive"));
            lore.add("&fIssued by: &9" + GrantUtils.getAuthor(grant));
            lore.add("&fReason: &7" + (grant.getReason() != null ? grant.getReason() : "None"));
            lore.add("&fDuration: &9" + (grant.getDuration() == -1 ? "Permanent" : TimeUtils.formatTimeShort(grant.getDuration())));

            if (!active && grant.getRemovedAt() > 0) {
                lore.add("");
                lore.add("&c&lRemoval Info");
                lore.add("&fRemoved by: &9" + GrantUtils.getRemover(grant));
                lore.add("&fRemoved at: &9" + TimeUtils.formatDate(grant.getRemovedAt()));
                lore.add("&fReason: &7" + (grant.getRemovalReason() != null ? grant.getRemovalReason() : "None"));
            }

            if (active && player.hasPermission("circuit.punish.remove")) {
                lore.add("");
                lore.add("&cClick to remove!");
            }

            builder.lore(lore);
            return builder.build();
        }

        @Override
        public void clicked(Player player, ClickType clickType) {
            if (!grant.isActive()) return;
            if (!player.hasPermission("circuit.punish.remove")) return;

            grant.setRemoved(true);
            grant.setRemovedBy(player.getUniqueId());
            grant.setRemovedAt(System.currentTimeMillis());
            grant.setRemovalReason("Removed via menu");

            ServiceContainer.getService(ProfileService.class).save(profile);
            Button.playSuccess(player);
            player.sendMessage(CC.translate("&aRemoved " + type.prettyName().toLowerCase() + " from " + profile.getName() + "!"));

            PunishmentsMenu menu = new PunishmentsMenu(target);
            menu.setType(type);
            menu.openMenu(player);
        }
    }

    private class BackButton extends Button {

        @Override
        public ItemStack getButtonItem(Player player) {
            ItemBuilder builder = new ItemBuilder(Material.REDSTONE);
            builder.name("&c&lBack");

            List<String> lore = new ArrayList<>();
            lore.add("&7Return to punishment types.");

            builder.lore(lore);
            return builder.build();
        }

        @Override
        public void clicked(Player player, ClickType clickType) {
            Button.playNeutral(player);
            new PunishmentsMenu(target).openMenu(player);
        }
    }

    private static class CloseButton extends Button {

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
}