package com.sylluxpvp.circuit.bukkit.menus.rank;

import lombok.RequiredArgsConstructor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import com.sylluxpvp.circuit.bukkit.tools.menu.Button;
import com.sylluxpvp.circuit.bukkit.tools.menu.pagination.PaginatedMenu;
import com.sylluxpvp.circuit.bukkit.tools.spigot.ColorMapping;
import com.sylluxpvp.circuit.bukkit.tools.spigot.ItemBuilder;
import com.sylluxpvp.circuit.shared.rank.Rank;
import com.sylluxpvp.circuit.shared.service.ServiceContainer;
import com.sylluxpvp.circuit.shared.service.impl.RankService;
import com.sylluxpvp.circuit.shared.tools.string.CC;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class RankListMenu extends PaginatedMenu {

    private static final int MENU_SIZE = 45;

    @Override
    public int getSize() {
        return MENU_SIZE;
    }

    @Override
    public String getPrePaginatedTitle(Player player) {
        return "&9Rank Editor";
    }

    @Override
    public Map<Integer, Button> getAllPagesButtons(Player player) {
        Map<Integer, Button> buttons = new HashMap<>();
        RankService service = ServiceContainer.getService(RankService.class);

        Comparator<Rank> comparator = Comparator.comparingInt(Rank::getWeight).reversed();
        List<Rank> sortedRanks = service.getRanks().stream()
                .sorted(comparator)
                .collect(Collectors.toList());

        for (Rank rank : sortedRanks) {
            buttons.put(buttons.size(), new RankListButton(rank));
        }

        return buttons;
    }

    @Override
    public Map<Integer, Button> getGlobalButtons(Player player) {
        Map<Integer, Button> buttons = new HashMap<>();
        buttons.put(MENU_SIZE - 5, new CreateRankButton());
        return buttons;
    }

    @RequiredArgsConstructor
    private class RankListButton extends Button {

        private final Rank rank;

        @Override
        public ItemStack getButtonItem(Player player) {
            ItemBuilder builder = new ItemBuilder(Material.INK_SACK);
            builder.durability(ColorMapping.getItemDurability(rank.getColor()));
            builder.name(rank.getColor() + rank.getName());

            List<String> lore = new ArrayList<>();
            lore.add("");
            lore.add("&9&lRank Information");
            lore.add("&fPrefix: " + (rank.getPrefix() == null || rank.getPrefix().isEmpty() ? "&cEmpty" : rank.getPrefix() + player.getName()));
            lore.add("&fSuffix: " + (rank.getSuffix() == null || rank.getSuffix().isEmpty() ? "&cEmpty" : player.getName() + rank.getSuffix()));
            lore.add("&fWeight: &9" + rank.getWeight());
            lore.add("");
            lore.add("&9&lRank Properties");
            lore.add("&fStaff: " + (rank.isStaff() ? "&aYes" : "&cNo"));
            lore.add("&fDefault: " + (rank.isDefaultRank() ? "&aYes" : "&cNo"));
            lore.add("&fHidden: " + (rank.isHidden() ? "&aYes" : "&cNo"));
            lore.add("&fPurchasable: " + (rank.isPurchasable() ? "&aYes" : "&cNo"));
            lore.add("");
            lore.add("&aLeft Click to edit!");
            if (!rank.isDefaultRank()) {
                lore.add("&cRight Click to delete!");
            }

            builder.lore(lore);
            return builder.build();
        }

        @Override
        public void clicked(Player player, ClickType clickType) {
            if (clickType == ClickType.RIGHT || clickType == ClickType.SHIFT_RIGHT) {
                if (rank.isDefaultRank()) {
                    Button.playFail(player);
                    player.sendMessage(CC.translate("&cYou cannot delete the default rank!"));
                    return;
                }
                Button.playNeutral(player);
                new RankDeleteConfirmMenu(rank).openMenu(player);
                return;
            }
            Button.playNeutral(player);
            new RankEditorMenu(rank).openMenu(player);
        }
    }

    private class CreateRankButton extends Button {

        @Override
        public ItemStack getButtonItem(Player player) {
            ItemBuilder builder = new ItemBuilder(Material.EMERALD);
            builder.name("&a&lCreate Rank");

            List<String> lore = new ArrayList<>();
            lore.add("&7Click to create a new rank.");
            lore.add("");
            lore.add("&aClick to create!");

            builder.lore(lore);
            return builder.build();
        }

        @Override
        public void clicked(Player player, ClickType clickType) {
            Button.playNeutral(player);
            player.closeInventory();
            new RankInputHandler(player, null, RankInputHandler.InputType.CREATE, new RankListMenu()).start();
        }
    }
}
