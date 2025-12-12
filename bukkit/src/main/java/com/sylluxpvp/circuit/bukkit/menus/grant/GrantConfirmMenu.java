package com.sylluxpvp.circuit.bukkit.menus.grant;

import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import com.sylluxpvp.circuit.bukkit.service.BukkitGrantService;
import com.sylluxpvp.circuit.bukkit.tools.menu.Button;
import com.sylluxpvp.circuit.bukkit.tools.menu.Menu;
import com.sylluxpvp.circuit.bukkit.tools.spigot.ItemBuilder;
import com.sylluxpvp.circuit.shared.grant.GrantProcedure;
import com.sylluxpvp.circuit.shared.profile.Profile;
import com.sylluxpvp.circuit.shared.rank.Rank;
import com.sylluxpvp.circuit.shared.service.ServiceContainer;
import com.sylluxpvp.circuit.shared.service.impl.ProfileService;
import com.sylluxpvp.circuit.shared.tools.java.TimeUtils;
import com.sylluxpvp.circuit.shared.tools.string.CC;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RequiredArgsConstructor
public class GrantConfirmMenu extends Menu {

    private static final int MENU_SIZE = 27;
    private static final int INFO_SLOT = 4;
    private static final int CONFIRM_SLOT = 11;
    private static final int CANCEL_SLOT = 15;

    private final UUID target;
    private final GrantProcedure<Rank> procedure;

    @Override
    public int getSize() {
        return MENU_SIZE;
    }

    @Override
    public String getTitle(Player player) {
        return "&9Confirm Grant?";
    }

    @Override
    public Map<Integer, Button> getButtons(Player player) {
        Map<Integer, Button> buttons = new HashMap<>();
        fillBorder(buttons);

        buttons.put(INFO_SLOT, new InfoButton());
        buttons.put(CONFIRM_SLOT, new ConfirmButton());
        buttons.put(CANCEL_SLOT, new CancelButton());

        return buttons;
    }

    private class InfoButton extends Button {

        @Override
        public ItemStack getButtonItem(Player player) {
            Profile targetProfile = ServiceContainer.getService(ProfileService.class).find(target);
            String targetName = targetProfile != null ? targetProfile.getName() : Bukkit.getOfflinePlayer(target).getName();
            Rank rank = procedure.getData();

            ItemBuilder builder = new ItemBuilder(Material.SKULL_ITEM);
            builder.skull(targetName);
            builder.name("&9&lGrant Summary");

            List<String> lore = new ArrayList<>();
            lore.add("");
            lore.add("&fTarget: &9" + targetName);
            lore.add("&fRank: " + rank.getColor() + rank.getName());
            lore.add("&fDuration: &9" + (procedure.getDuration() == -1 ? "&cPermanent" : TimeUtils.formatTimeShort(procedure.getDuration())));
            lore.add("&fReason: &9" + procedure.getReason());

            builder.lore(lore);
            return builder.build();
        }
    }

    @SuppressWarnings("deprecation")
    private class ConfirmButton extends Button {

        @Override
        public ItemStack getButtonItem(Player player) {
            ItemBuilder builder = new ItemBuilder(Material.WOOL);
            builder.durability(DyeColor.LIME.getWoolData());
            builder.name("&a&lConfirm Grant");

            Rank rank = procedure.getData();
            Profile targetProfile = ServiceContainer.getService(ProfileService.class).find(target);
            String targetName = targetProfile != null ? targetProfile.getName() : Bukkit.getOfflinePlayer(target).getName();

            List<String> lore = new ArrayList<>();
            lore.add("");
            lore.add("&7Grant " + rank.getColor() + rank.getName());
            lore.add("&7to &f" + targetName + "&7.");
            lore.add("");
            lore.add("&aClick to confirm!");

            builder.lore(lore);
            return builder.build();
        }

        @Override
        public void clicked(Player player, ClickType clickType) {
            Button.playSuccess(player);
            ServiceContainer.getService(BukkitGrantService.class).applyGrant(player.getUniqueId(), target, procedure);
            player.closeInventory();
        }
    }

    @SuppressWarnings("deprecation")
    private class CancelButton extends Button {

        @Override
        public ItemStack getButtonItem(Player player) {
            ItemBuilder builder = new ItemBuilder(Material.WOOL);
            builder.durability(DyeColor.RED.getWoolData());
            builder.name("&c&lCancel");

            List<String> lore = new ArrayList<>();
            lore.add("");
            lore.add("&7Cancel this grant.");
            lore.add("");
            lore.add("&cClick to cancel!");

            builder.lore(lore);
            return builder.build();
        }

        @Override
        public void clicked(Player player, ClickType clickType) {
            Button.playNeutral(player);
            player.sendMessage(CC.translate("&cGrant cancelled."));
            player.closeInventory();
        }
    }
}
