package xyz.kayaaa.xenon.bukkit.tools;

import lombok.experimental.UtilityClass;
import org.bukkit.ChatColor;

import java.util.HashMap;
import java.util.Map;

@UtilityClass
public class InkMapping {

    public static final Map<ChatColor, Integer> dyeMap = new HashMap<ChatColor, Integer>() {{
        put(ChatColor.WHITE, 15);
        put(ChatColor.GOLD, 14);
        put(ChatColor.AQUA, 12);
        put(ChatColor.YELLOW, 11);
        put(ChatColor.GREEN, 10);
        put(ChatColor.LIGHT_PURPLE, 9);
        put(ChatColor.GRAY, 8);
        put(ChatColor.DARK_GRAY, 7);
        put(ChatColor.DARK_AQUA, 6);
        put(ChatColor.DARK_PURPLE, 5);
        put(ChatColor.BLUE, 4);
        put(ChatColor.DARK_GREEN, 2);
        put(ChatColor.RED, 1);
        put(ChatColor.DARK_RED, 1);
        put(ChatColor.BLACK, 0);
    }};

    public short getItemDurability(String rankColor) {
        if (rankColor == null || rankColor.isEmpty()) return 0;
        if (rankColor.contains("§") || rankColor.contains("&")) {
            char codeChar = rankColor.charAt(1);
            ChatColor color = ChatColor.getByChar(codeChar);

            if (color == null || !color.isColor()) return 0;

            rankColor = color.name();
        }

        ChatColor color = ChatColor.valueOf(rankColor.toUpperCase());
        Integer durability = dyeMap.get(color);
        return durability != null ? durability.shortValue() : 0;
    }

}
