package xyz.kayaaa.xenon.bukkit.menus;

import lombok.RequiredArgsConstructor;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import xyz.kayaaa.xenon.bukkit.tools.menu.Button;
import xyz.kayaaa.xenon.bukkit.tools.menu.Menu;
import xyz.kayaaa.xenon.bukkit.tools.spigot.ColorMapping;
import xyz.kayaaa.xenon.bukkit.tools.spigot.ItemBuilder;
import xyz.kayaaa.xenon.shared.profile.Profile;
import xyz.kayaaa.xenon.shared.service.ServiceContainer;
import xyz.kayaaa.xenon.shared.service.impl.ProfileService;
import xyz.kayaaa.xenon.shared.tools.string.CC;
import xyz.kayaaa.xenon.shared.tools.string.StringHelper;

import java.util.*;
import java.util.stream.Collectors;

public class ChatColorMenu extends Menu {

    @Override
    public String getTitle(Player player) {
        return "&9Select a chat color...";
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
        for (ChatColor color : Arrays.stream(ChatColor.values()).sorted(Comparator.comparing(ChatColor::name)).collect(Collectors.toList())) {
            if (isBadColor(color)) continue;
            while (isBorderSlot(i)) i++;
            buttons.put(i++, new ChatColorButton(color));
        }
        buttons.put(4, new ChatColorButton(ChatColor.RESET));
        return buttons;
    }

    private boolean isBadColor(ChatColor color) {
        return color == ChatColor.BOLD || color == ChatColor.ITALIC || color == ChatColor.UNDERLINE || color == ChatColor.STRIKETHROUGH || color == ChatColor.MAGIC || color == ChatColor.RESET;
    }

    @RequiredArgsConstructor
    private static class ChatColorButton extends Button {

        private final ChatColor color;

        @Override
        public ItemStack getButtonItem(Player player) {
            ItemBuilder builder = new ItemBuilder(Material.LEATHER_CHESTPLATE);
            builder.color(ColorMapping.chatColorToLeatherColor(color));
            builder.name(color + StringHelper.capitalizeAllWords(color.name().toLowerCase().replace("_", " ")));
            Profile profile = ServiceContainer.getService(ProfileService.class).find(player.getUniqueId());
            if (profile == null) return builder.build();
            List<String> lore = new ArrayList<>();
            if (color != ChatColor.RESET) {
                lore.add("&7This is how the color will look:");
                lore.add(profile.getCurrentGrant().getData().getPrefix() + color + player.getName() + profile.getCurrentGrant().getData().getSuffix() + "&7: &fHey!");
                lore.add("");
                lore.add(ColorMapping.isChatColor(profile.getColor(), color) ? "&aYou're using this color." : "&eClick to select!");
            } else {
                lore.add("&7This one will remove");
                lore.add("&7your current color.");
                lore.add("");
                lore.add("&eClick to reset!");
            }
            builder.lore(lore);
            return builder.build();
        }

        @Override
        public void clicked(Player player, ClickType clickType) {
            Profile profile = ServiceContainer.getService(ProfileService.class).find(player.getUniqueId());
            if (profile == null) return;

            if (color == ChatColor.RESET) {
                profile.setColor("");
                player.sendMessage(CC.GREEN + "Your color was reset!");

                return;
            }

            if (ColorMapping.isChatColor(profile.getColor(), color)) {
                player.sendMessage(CC.RED + "You're already using this color.");
                return;
            }
            profile.setColor("&" + color.getChar());
            player.sendMessage(CC.GREEN + "Your color was changed to " + color + StringHelper.capitalizeAllWords(color.name().toLowerCase().replace("_", " ")) + "!");
        }
    }
}
