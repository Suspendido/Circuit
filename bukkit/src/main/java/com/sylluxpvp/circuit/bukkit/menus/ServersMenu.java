package com.sylluxpvp.circuit.bukkit.menus;

import lombok.RequiredArgsConstructor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import com.sylluxpvp.circuit.bukkit.tools.menu.Button;
import com.sylluxpvp.circuit.bukkit.tools.menu.Menu;
import com.sylluxpvp.circuit.bukkit.tools.spigot.ItemBuilder;
import com.sylluxpvp.circuit.shared.server.Server;
import com.sylluxpvp.circuit.shared.service.ServiceContainer;
import com.sylluxpvp.circuit.shared.service.impl.ServerService;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ServersMenu extends Menu {

    private static final int MENU_SIZE = 45;

    @Override
    public String getTitle(Player player) {
        return "&9Network Servers";
    }

    @Override
    public int getSize() {
        return MENU_SIZE;
    }

    @Override
    public Map<Integer, Button> getButtons(Player player) {
        Map<Integer, Button> buttons = new HashMap<>();
        fillBorder(buttons);

        buttons.put(4, new NetworkInfoButton());

        int slot = 10;
        List<Server> servers = ServiceContainer.getService(ServerService.class).getServers().stream()
                .sorted(Comparator.comparing(Server::isOnline).reversed()
                        .thenComparing(s -> s.getType().name())
                        .thenComparing(Server::getName))
                .toList();

        for (Server server : servers) {
            while (isBorderSlot(slot) && slot < MENU_SIZE - 9) slot++;
            if (slot >= MENU_SIZE - 9) break;
            buttons.put(slot++, new ServerButton(server));
        }

        buttons.put(MENU_SIZE - 5, new CloseButton());

        return buttons;
    }

    private static class NetworkInfoButton extends Button {

        @Override
        public ItemStack getButtonItem(Player player) {
            ServerService service = ServiceContainer.getService(ServerService.class);
            List<Server> servers = service.getServers();

            long online = servers.stream().filter(Server::isOnline).count();
            int totalPlayers = servers.stream().mapToInt(Server::getPlayers).sum();
            int maxPlayers = servers.stream().mapToInt(Server::getMax).sum();

            ItemBuilder builder = new ItemBuilder(Material.NETHER_STAR);
            builder.name("&9&lNetwork Status");

            List<String> lore = new ArrayList<>();
            lore.add("");
            lore.add("&fServers Online: &a" + online + " &7/ &c" + (servers.size() - online));
            lore.add("&fTotal Players: &9" + totalPlayers + "&7/&9" + maxPlayers);
            lore.add("");
            lore.add("&7Click to refresh.");

            builder.lore(lore);
            return builder.build();
        }

        @Override
        public void clicked(Player player, ClickType clickType) {
            Button.playNeutral(player);
            new ServersMenu().openMenu(player);
        }
    }

    @RequiredArgsConstructor
    private static class ServerButton extends Button {

        private final Server server;

        @Override
        public ItemStack getButtonItem(Player player) {
            ItemBuilder builder = new ItemBuilder(getByServer());

            String statusIcon = server.isOnline() ? "&a●" : "&c●";
            builder.name(statusIcon + " &9" + server.getName());

            List<String> lore = new ArrayList<>();
            lore.add("");
            lore.add("&fStatus: " + (server.isOnline() ? "&aOnline" : "&cOffline"));
            lore.add("&fPlayers: &9" + server.getPlayers() + "&7/&9" + server.getMax());
            lore.add("&fType: &9" + capitalize(server.getType().name()));

            if (server.isOnline()) {
                int percentage = server.getMax() > 0 ? (server.getPlayers() * 100 / server.getMax()) : 0;
                String bar = getProgressBar(percentage);
                lore.add("");
                lore.add("&7Capacity: " + bar + " &7" + percentage + "%");
            }

            builder.lore(lore);
            return builder.build();
        }

        private String getProgressBar(int percentage) {
            int filled = percentage / 10;
            StringBuilder bar = new StringBuilder();
            for (int i = 0; i < 10; i++) {
                bar.append(i < filled ? "&a|" : "&7|");
            }
            return bar.toString();
        }

        private String capitalize(String s) {
            return s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase();
        }

        private Material getByServer() {
            if (!server.isOnline()) return Material.REDSTONE_BLOCK;
            return switch (server.getType()) {
                case HUB -> Material.BEACON;
                case SOUP -> Material.MUSHROOM_STEW;
                case PRACTICE -> Material.POTION;
                default -> Material.EMERALD_BLOCK;
            };
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
