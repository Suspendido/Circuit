package xyz.kayaaa.xenon.bukkit.menus;

import lombok.RequiredArgsConstructor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import xyz.kayaaa.xenon.bukkit.tools.menu.Button;
import xyz.kayaaa.xenon.bukkit.tools.menu.Menu;
import xyz.kayaaa.xenon.bukkit.tools.spigot.ItemBuilder;
import xyz.kayaaa.xenon.shared.server.Server;
import xyz.kayaaa.xenon.shared.service.ServiceContainer;
import xyz.kayaaa.xenon.shared.service.impl.ServerService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ServersMenu extends Menu {

    @Override
    public String getTitle(Player player) {
        return "&9All Servers";
    }

    @Override
    public int getSize() {
        return 45;
    }

    @Override
    public Map<Integer, Button> getButtons(Player player) {
        Map<Integer, Button> buttons = new HashMap<>();
        this.fillBorder(buttons);
        int i = 0;
        for (Server server : ServiceContainer.getService(ServerService.class).getServers()) {
            while (isBorderSlot(i)) i++;
            buttons.put(i++, new ServerButton(server));

        }
        return buttons;
    }

    @RequiredArgsConstructor
    private static class ServerButton extends Button {

        private final Server server;

        @Override
        public ItemStack getButtonItem(Player player) {
            ItemBuilder builder = new ItemBuilder(getByServer());
            builder.name("&9" + server.getName());
            List<String> lore = new ArrayList<>();
            lore.add("");
            lore.add("&fStatus: " + server.getStatus());
            lore.add("&fPlayers: &9" + server.getPlayers() + "/" + server.getMax());
            lore.add("&fType: &9" + server.getType().name());
            builder.lore(lore);
            return builder.build();
        }

        private Material getByServer() {
            if (!server.isOnline()) return Material.REDSTONE_BLOCK;
            switch (server.getType()) {
                case HUB:
                    return Material.JUKEBOX;
                case SOUP:
                    return Material.MUSHROOM_SOUP;
                case PRACTICE:
                    return Material.POTION;
                case DEFAULT:
                default:
                    return Material.DIAMOND_SWORD;
            }
        }
    }

}
