package xyz.kayaaa.xenon.bukkit.menus;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import xyz.kayaaa.xenon.bukkit.tools.menu.Button;
import xyz.kayaaa.xenon.bukkit.tools.menu.Menu;
import xyz.kayaaa.xenon.bukkit.tools.spigot.ColorMapping;
import xyz.kayaaa.xenon.bukkit.tools.spigot.ItemBuilder;
import xyz.kayaaa.xenon.bukkit.tools.xenon.GrantUtils;
import xyz.kayaaa.xenon.shared.grant.Grant;
import xyz.kayaaa.xenon.shared.profile.Profile;
import xyz.kayaaa.xenon.shared.punishment.Punishment;
import xyz.kayaaa.xenon.shared.punishment.PunishmentType;
import xyz.kayaaa.xenon.shared.service.ServiceContainer;
import xyz.kayaaa.xenon.shared.service.impl.ProfileService;
import xyz.kayaaa.xenon.shared.tools.java.TimeUtils;

import java.util.*;
import java.util.stream.Collectors;

public class PunishmentsMenu extends Menu {

    private final UUID target;
    @Setter private PunishmentType type;

    public PunishmentsMenu(UUID target) {
        this.target = target;
    }

    @Override
    public String getTitle(Player player) {
        return "&9Punishments for " + Bukkit.getOfflinePlayer(target).getName();
    }

    @Override
    public int getSize() {
        return type == null ? 27 : 45;
    }

    @Override
    public Map<Integer, Button> getButtons(Player player) {
        Map<Integer, Button> buttons = new HashMap<>();
        this.fillBorder(buttons);
        Profile profile = ServiceContainer.getService(ProfileService.class).find(target);
        if (profile == null) return buttons;
        if (type == null) {
            buttons.put(10, new PunishmentTypeButton(profile, PunishmentType.KICK));
            buttons.put(12, new PunishmentTypeButton(profile, PunishmentType.MUTE));
            buttons.put(14, new PunishmentTypeButton(profile, PunishmentType.BAN));
            buttons.put(16, new PunishmentTypeButton(profile, PunishmentType.BLACKLIST));
            return buttons;
        } else {
            this.addBackButton(buttons, new PunishmentsMenu(target));
        }
        int i = 0;
        for (Grant<Punishment> grant : profile.getPunishments().stream().filter(grant -> grant.getData().getPunishmentType() == type).sorted(Comparator.comparing(Grant::getTimeCreated)).collect(Collectors.toList())) {
            while (isBorderSlot(i)) i++;
            buttons.put(i++, new PunishmentButton(type, grant));
        }
        return buttons;
    }

    @RequiredArgsConstructor
    private class PunishmentTypeButton extends Button {

        private final Profile profile;
        private final PunishmentType type;

        @Override
        public ItemStack getButtonItem(Player player) {
            ItemBuilder builder = new ItemBuilder(new ItemStack(Material.INK_SACK, 1, ColorMapping.getColor(type).getDyeData()));
            builder.name(ColorMapping.dyeColorToChatColor(ColorMapping.getColor(type)) + type.prettyName() + (profile.getAllPunishmentsByType(type).isEmpty() ? " &7(Empty)" : " &e(" + profile.getAllPunishmentsByType(type).size() + " total)"));
            List<String> lore = new ArrayList<>();
            lore.add("&7View all " + type.name().toLowerCase() + "s issued for " + profile.getName() + ".");
            lore.add("");
            lore.add("&eClick to view!");
            builder.lore(lore);
            return builder.build();
        }

        @Override
        public void clicked(Player player, ClickType clickType) {
            PunishmentsMenu menu = new PunishmentsMenu(target);
            menu.setType(type);
            menu.openMenu(player);
        }
    }

    @RequiredArgsConstructor
    private class PunishmentButton extends Button {

        private final PunishmentType type;
        private final Grant<Punishment> grant;

        @Override
        public ItemStack getButtonItem(Player player) {
            ItemBuilder builder = new ItemBuilder(Material.BOOK);
            builder.amount(1);
            builder.name("&9" + grant.getData().getPunishmentType().prettyName() + " &7- " + TimeUtils.formatDate(grant.getTimeCreated()) + (grant.isActive() ? " &a(Active)" : " &c(Inactive)"));
            List<String> lore = new ArrayList<>();
            lore.add("");
            lore.add("&fIssued by: &9" + GrantUtils.getAuthor(grant));
            lore.add("&fReason: &9" + (grant.getReason() == null ? "&cNone" : grant.getReason()));
            lore.add("&fDuration: &9" + (grant.getDuration() == -1 ? "Permanent" : TimeUtils.formatTimeShort(grant.getDuration())));

            if (grant.isActive()) {
                lore.add("");
                lore.add("&eClick to remove!");
            } else {
                lore.add("");
                lore.add("&fRemoved by: &9" + GrantUtils.getRemover(grant));
                lore.add("&fRemoved at: &9" + TimeUtils.formatDate(grant.getRemovedAt()));
                lore.add("&fRemoval reason: &9" + (grant.getRemovalReason() == null ? "&cNone" : grant.getRemovalReason()));
                lore.add("");
            }
            builder.lore(lore);
            return builder.build();
        }

        @Override
        public void clicked(Player player, ClickType clickType) {
            PunishmentsMenu menu = new PunishmentsMenu(target);
            menu.setType(type);
            menu.openMenu(player);
        }
    }
}
