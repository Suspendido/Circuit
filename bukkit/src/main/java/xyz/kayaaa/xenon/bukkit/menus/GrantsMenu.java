package xyz.kayaaa.xenon.bukkit.menus;

import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import xyz.kayaaa.xenon.bukkit.service.BukkitGrantService;
import xyz.kayaaa.xenon.bukkit.tools.menu.Button;
import xyz.kayaaa.xenon.bukkit.tools.menu.Menu;
import xyz.kayaaa.xenon.bukkit.tools.spigot.ColorMapping;
import xyz.kayaaa.xenon.bukkit.tools.spigot.ItemBuilder;
import xyz.kayaaa.xenon.bukkit.tools.xenon.GrantUtils;
import xyz.kayaaa.xenon.shared.grant.Grant;
import xyz.kayaaa.xenon.shared.profile.Profile;
import xyz.kayaaa.xenon.shared.rank.Rank;
import xyz.kayaaa.xenon.shared.service.ServiceContainer;
import xyz.kayaaa.xenon.shared.service.impl.GrantService;
import xyz.kayaaa.xenon.shared.service.impl.ProfileService;
import xyz.kayaaa.xenon.shared.tools.java.TimeUtils;
import xyz.kayaaa.xenon.shared.tools.string.CC;

import java.util.*;
import java.util.stream.Collectors;

public class GrantsMenu extends Menu {

    private final UUID target;

    public GrantsMenu(UUID target) {
        this.target = target;
    }

    @Override
    public String getTitle(Player player) {
        return "&9Grants for " + Bukkit.getOfflinePlayer(target).getName();
    }

    @Override
    public int getSize() {
        return 45;
    }

    @Override
    public Map<Integer, Button> getButtons(Player player) {
        Map<Integer, Button> buttons = new HashMap<>();
        this.fillBorder(buttons);
        Profile profile = ServiceContainer.getService(ProfileService.class).find(target);
        if (profile == null) return buttons;
        int i = 0;
        Comparator<Grant<Rank>> comparator = Comparator.<Grant<Rank>>comparingInt(grant -> grant.getData().getWeight()).reversed();
        for (Grant<Rank> grant : profile.getRankGrants().stream().sorted(comparator).collect(Collectors.toList())) {
            while (isBorderSlot(i)) i++;
            buttons.put(i++, new GrantButton(profile, grant));
        }
        return buttons;
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
                lore.add("&eClick to remove!");
            } else {
                lore.add("");
                lore.add("&fRemoved by: &9" + GrantUtils.getRemover(grant));
                lore.add("&fRemoved at: &9" + TimeUtils.formatDate(grant.getRemovedAt()));
                lore.add("&fRemoval reason: &9" + (grant.getRemovalReason() == null ? "&cNone" : grant.getRemovalReason()));
                lore.add("");
                lore.add("&eClick to destroy!");
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
