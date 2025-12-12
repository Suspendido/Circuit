package com.sylluxpvp.circuit.bukkit.tools.spigot;

import lombok.experimental.UtilityClass;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.DyeColor;
import com.sylluxpvp.circuit.shared.punishment.PunishmentType;

import java.util.HashMap;
import java.util.Map;

@UtilityClass
public class ColorMapping {

    // INK_SACK (dye) data values - for dye items
    public static final Map<ChatColor, Integer> dyeMap = new HashMap<ChatColor, Integer>() {{
        put(ChatColor.BLACK, 0);        // Ink Sac
        put(ChatColor.DARK_RED, 1);     // Red Dye
        put(ChatColor.RED, 1);          // Red Dye
        put(ChatColor.DARK_GREEN, 2);   // Cactus Green
        put(ChatColor.GOLD, 14);        // Orange Dye
        put(ChatColor.DARK_BLUE, 4);    // Lapis Lazuli
        put(ChatColor.BLUE, 12);        // Light Blue Dye
        put(ChatColor.DARK_PURPLE, 5);  // Purple Dye
        put(ChatColor.DARK_AQUA, 6);    // Cyan Dye
        put(ChatColor.AQUA, 12);        // Light Blue Dye
        put(ChatColor.GRAY, 7);         // Light Gray Dye
        put(ChatColor.DARK_GRAY, 8);    // Gray Dye
        put(ChatColor.LIGHT_PURPLE, 9); // Pink Dye
        put(ChatColor.GREEN, 10);       // Lime Dye
        put(ChatColor.YELLOW, 11);      // Yellow Dye
        put(ChatColor.WHITE, 15);       // Bone Meal
    }};

    // WOOL data values - for wool blocks
    public static final Map<ChatColor, Integer> woolMap = new HashMap<ChatColor, Integer>() {{
        put(ChatColor.WHITE, 0);        // White Wool
        put(ChatColor.GOLD, 1);         // Orange Wool
        put(ChatColor.LIGHT_PURPLE, 6); // Pink Wool
        put(ChatColor.AQUA, 3);         // Light Blue Wool
        put(ChatColor.YELLOW, 4);       // Yellow Wool
        put(ChatColor.GREEN, 5);        // Lime Wool
        put(ChatColor.DARK_GRAY, 7);    // Gray Wool
        put(ChatColor.GRAY, 8);         // Light Gray Wool
        put(ChatColor.DARK_AQUA, 9);    // Cyan Wool
        put(ChatColor.DARK_PURPLE, 10); // Purple Wool
        put(ChatColor.DARK_BLUE, 11);   // Blue Wool
        put(ChatColor.BLUE, 3);         // Light Blue Wool
        put(ChatColor.DARK_GREEN, 13);  // Green Wool
        put(ChatColor.RED, 14);         // Red Wool
        put(ChatColor.DARK_RED, 14);    // Red Wool
        put(ChatColor.BLACK, 15);       // Black Wool
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

    public boolean isChatColor(String color, ChatColor chatColor) {
        if (color.contains("§") || color.contains("&")) {
            char codeChar = color.charAt(1);
            ChatColor byChar = ChatColor.getByChar(codeChar);

            if (byChar == null || !byChar.isColor()) return false;

            return byChar == chatColor;
        }
        return false;
    }

    public DyeColor getColor(PunishmentType type) {
        switch (type) {
            case BAN:
                return DyeColor.RED;
            case MUTE:
                return DyeColor.GREEN;
            case KICK:
                return DyeColor.ORANGE;
            case BLACKLIST:
                return DyeColor.PURPLE;
            default:
                return DyeColor.WHITE;
        }
    }

    public Color chatColorToLeatherColor(ChatColor chatColor) {
        if (chatColor == null) return Color.WHITE;

        switch (chatColor) {
            case BLACK: return Color.fromRGB(0, 0, 0);
            case DARK_BLUE: return Color.fromRGB(0, 0, 170);
            case DARK_GREEN: return Color.fromRGB(0, 170, 0);
            case DARK_AQUA: return Color.fromRGB(0, 170, 170);
            case DARK_RED: return Color.fromRGB(170, 0, 0);
            case DARK_PURPLE: return Color.fromRGB(170, 0, 170);
            case GOLD: return Color.fromRGB(255, 170, 0);
            case GRAY: return Color.fromRGB(170, 170, 170);
            case DARK_GRAY: return Color.fromRGB(85, 85, 85);
            case BLUE: return Color.fromRGB(85, 85, 255);
            case GREEN: return Color.fromRGB(85, 255, 85);
            case AQUA: return Color.fromRGB(85, 255, 255);
            case RED: return Color.fromRGB(255, 85, 85);
            case LIGHT_PURPLE: return Color.fromRGB(255, 85, 255);
            case YELLOW: return Color.fromRGB(255, 255, 85);
            case WHITE: return Color.fromRGB(255, 255, 255);
            default: return Color.WHITE;
        }
    }

    public ChatColor dyeColorToChatColor(DyeColor dyeColor) {
        switch (dyeColor) {
            case ORANGE:
            case BROWN:
                return ChatColor.GOLD;
            case MAGENTA:
            case PINK:
                return ChatColor.LIGHT_PURPLE;
            case LIGHT_BLUE:
                return ChatColor.BLUE;
            case YELLOW:
                return ChatColor.YELLOW;
            case LIME:
                return ChatColor.GREEN;
            case GRAY:
                return ChatColor.DARK_GRAY;
            case SILVER:
                return ChatColor.GRAY;
            case CYAN:
                return ChatColor.DARK_AQUA;
            case PURPLE:
                return ChatColor.DARK_PURPLE;
            case BLUE:
                return ChatColor.DARK_BLUE;
            case GREEN:
                return ChatColor.DARK_GREEN;
            case RED:
                return ChatColor.RED;
            case BLACK:
                return ChatColor.BLACK;
            default:
                return ChatColor.WHITE;
        }
    }

    public ChatColor fromString(String color) {
        if (color.contains("§") || color.contains("&")) {
            char codeChar = color.charAt(1);
            return ChatColor.getByChar(codeChar);
        }
        return null;
    }

    @SuppressWarnings("deprecation")
    public DyeColor chatColorToDyeColor(ChatColor color) {
        Integer data = woolMap.get(color);
        if (data == null) return DyeColor.WHITE;
        return DyeColor.getByWoolData(data.byteValue());
    }

    public short getWoolDurability(ChatColor color) {
        Integer data = woolMap.get(color);
        return data != null ? data.shortValue() : 0;
    }

}
