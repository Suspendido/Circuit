package com.sylluxpvp.circuit.bukkit.menus.player;

import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import com.sylluxpvp.circuit.bukkit.menus.PunishmentsMenu;
import com.sylluxpvp.circuit.bukkit.tools.menu.Button;
import com.sylluxpvp.circuit.bukkit.tools.menu.pagination.PaginatedMenu;
import com.sylluxpvp.circuit.bukkit.tools.spigot.ItemBuilder;
import com.sylluxpvp.circuit.shared.profile.Profile;
import com.sylluxpvp.circuit.shared.punishment.PunishmentType;
import com.sylluxpvp.circuit.shared.service.ServiceContainer;
import com.sylluxpvp.circuit.shared.service.impl.ProfileService;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class AltsMenu extends PaginatedMenu {

    private static final int MENU_SIZE = 45;

    private final Profile targetProfile;

    @Override
    public int getSize() {
        return MENU_SIZE;
    }

    @Override
    public String getPrePaginatedTitle(Player player) {
        return "&9Alts: " + targetProfile.getName();
    }

    @Override
    public Map<Integer, Button> getAllPagesButtons(Player player) {
        Map<Integer, Button> buttons = new HashMap<>();
        ProfileService service = ServiceContainer.getService(ProfileService.class);

        Set<Profile> alts = service.findFromAddress(targetProfile).stream()
                .filter(p -> !p.getUUID().equals(targetProfile.getUUID()))
                .collect(Collectors.toSet());

        List<Profile> sortedAlts = alts.stream()
                .sorted(Comparator.comparing(Profile::getName))
                .collect(Collectors.toList());

        for (Profile alt : sortedAlts) {
            buttons.put(buttons.size(), new AltButton(alt));
        }

        return buttons;
    }

    @Override
    public Map<Integer, Button> getGlobalButtons(Player player) {
        Map<Integer, Button> buttons = new HashMap<>();
        buttons.put(4, new TargetInfoButton());
        buttons.put(MENU_SIZE - 5, new CloseButton());
        return buttons;
    }

    private class TargetInfoButton extends Button {

        @Override
        public ItemStack getButtonItem(Player player) {
            ProfileService service = ServiceContainer.getService(ProfileService.class);
            Set<Profile> alts = service.findFromAddress(targetProfile).stream()
                    .filter(p -> !p.getUUID().equals(targetProfile.getUUID()))
                    .collect(Collectors.toSet());

            ItemBuilder builder = new ItemBuilder(Material.SKULL_ITEM);
            builder.skull(targetProfile.getName());
            builder.name(targetProfile.getCurrentGrant().getData().getColor() + targetProfile.getName());

            List<String> lore = new ArrayList<>();
            lore.add("");
            lore.add("&9&lPlayer Information");
            lore.add("&fRank: " + targetProfile.getCurrentGrant().getData().getColor() + targetProfile.getCurrentGrant().getData().getName());
            lore.add("");
            lore.add("&9&lAlt Accounts");
            lore.add("&fTotal Alts: &9" + alts.size());

            long onlineAlts = alts.stream()
                    .filter(p -> Bukkit.getPlayer(p.getUUID()) != null)
                    .count();
            lore.add("&fOnline: &a" + onlineAlts + " &7/ &c" + (alts.size() - onlineAlts));

            long punishedAlts = alts.stream()
                    .filter(p -> p.findActivePunishment() != null)
                    .count();
            if (punishedAlts > 0) {
                lore.add("");
                lore.add("&c⚠ " + punishedAlts + " alt(s) punished");
            }

            builder.lore(lore);
            return builder.build();
        }
    }

    @RequiredArgsConstructor
    private class AltButton extends Button {

        private final Profile alt;

        @Override
        public ItemStack getButtonItem(Player player) {
            boolean online = Bukkit.getPlayer(alt.getUUID()) != null;
            boolean punished = alt.findActivePunishment() != null;

            ItemBuilder builder = new ItemBuilder(Material.SKULL_ITEM);
            builder.skull(alt.getName());

            String statusColor = online ? "&a" : "&c";
            builder.name(alt.getCurrentGrant().getData().getColor() + alt.getName() + " " + statusColor + (online ? "●" : "○"));

            List<String> lore = new ArrayList<>();
            lore.add("");
            lore.add("&fRank: " + alt.getCurrentGrant().getData().getColor() + alt.getCurrentGrant().getData().getName());
            lore.add("&fStatus: " + (online ? "&aOnline" : "&cOffline"));

            if (punished) {
                lore.add("");
                lore.add("&c&lActive Punishment");
                PunishmentType type = alt.findActivePunishment().getData().getPunishmentType();
                lore.add("&fType: &c" + type.prettyName());
                lore.add("&fReason: &7" + (alt.findActivePunishment().getReason() != null ? alt.findActivePunishment().getReason() : "None"));
            }

            lore.add("");
            lore.add("&aClick to view punishments!");

            builder.lore(lore);
            return builder.build();
        }

        @Override
        public void clicked(Player player, ClickType clickType) {
            Button.playNeutral(player);
            new PunishmentsMenu(alt.getUUID()).openMenu(player);
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
}
