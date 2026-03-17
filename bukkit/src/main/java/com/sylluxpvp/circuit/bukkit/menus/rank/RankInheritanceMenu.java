package com.sylluxpvp.circuit.bukkit.menus.rank;

import lombok.RequiredArgsConstructor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import com.sylluxpvp.circuit.bukkit.CircuitPlugin;
import com.sylluxpvp.circuit.bukkit.tools.menu.Button;
import com.sylluxpvp.circuit.bukkit.tools.menu.pagination.PaginatedMenu;
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
import java.util.logging.Logger;

@RequiredArgsConstructor
public class RankInheritanceMenu extends PaginatedMenu {

    private static final int MENU_SIZE = 45;
    private static final Logger LOGGER = CircuitPlugin.getInstance().getLogger();

    private final Rank rank;

    @Override
    public int getSize() {
        return MENU_SIZE;
    }

    @Override
    public String getPrePaginatedTitle(Player player) {
        return "&9Inheritances: " + rank.getName();
    }

    @Override
    public Map<Integer, Button> getAllPagesButtons(Player player) {
        Map<Integer, Button> buttons = new HashMap<>();
        RankService service = ServiceContainer.getService(RankService.class);

        List<Rank> availableRanks = service.getRanks().stream()
                .filter(r -> !r.getUuid().equals(rank.getUuid()))
                .filter(r -> r.getWeight() < rank.getWeight())
                .sorted(Comparator.comparingInt(Rank::getWeight).reversed())
                .toList();

        for (Rank r : availableRanks) {
            buttons.put(buttons.size(), new InheritanceButton(r));
        }

        return buttons;
    }

    @Override
    public Map<Integer, Button> getGlobalButtons(Player player) {
        Map<Integer, Button> buttons = new HashMap<>();
        buttons.put(MENU_SIZE - 5, new BackToEditorButton());
        return buttons;
    }

    @RequiredArgsConstructor
    private class InheritanceButton extends Button {

        private final Rank targetRank;

        @Override
        public ItemStack getButtonItem(Player player) {
            boolean inherited = rank.getInheritances().contains(targetRank.getName());

            ItemBuilder builder = new ItemBuilder(inherited ? Material.LIME_DYE : Material.GRAY_DYE);
            builder.name(targetRank.getColor() + targetRank.getName());

            List<String> lore = new ArrayList<>();
            lore.add("");
            lore.add("&fWeight: &7" + targetRank.getWeight());
            lore.add("&7Permissions: &f" + targetRank.getPermissions().size());
            lore.add("");
            lore.add(inherited ? "&a(Inherited)" : "&7(Not inherited)");
            lore.add("");
            lore.add(inherited ? "&cClick to remove!" : "&aClick to add!");

            builder.lore(lore);
            return builder.build();
        }

        @Override
        public void clicked(Player player, ClickType clickType) {
            boolean inherited = rank.getInheritances().contains(targetRank.getName());

            if (inherited) {
                rank.removeInheritance(targetRank.getName());
                Button.playNeutral(player);
                player.sendMessage(CC.translate("&cRemoved inheritance: " + targetRank.getColor() + targetRank.getName()));
                LOGGER.info(player.getName() + " removed inheritance " + targetRank.getName() + " from rank " + rank.getName());
            } else {
                rank.addInheritance(targetRank.getName());
                Button.playSuccess(player);
                player.sendMessage(CC.translate("&aAdded inheritance: " + targetRank.getColor() + targetRank.getName()));
                LOGGER.info(player.getName() + " added inheritance " + targetRank.getName() + " to rank " + rank.getName());
            }

            ServiceContainer.getService(RankService.class).save(rank);
            new RankInheritanceMenu(rank).openMenu(player);
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
