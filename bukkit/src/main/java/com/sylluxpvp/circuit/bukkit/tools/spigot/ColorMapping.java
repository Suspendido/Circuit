package com.sylluxpvp.circuit.bukkit.tools.spigot;

import lombok.experimental.UtilityClass;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import com.sylluxpvp.circuit.shared.punishment.PunishmentType;

import java.util.HashMap;
import java.util.Map;

@UtilityClass
public class ColorMapping {

    // Material mapping for dyes (modernized - no durability needed)
    public static final Map<ChatColor, Material> dyeMaterialMap = new HashMap<>() {{
        put(ChatColor.BLACK, Material.BLACK_DYE);
        put(ChatColor.DARK_RED, Material.RED_DYE);
        put(ChatColor.RED, Material.RED_DYE);
        put(ChatColor.DARK_GREEN, Material.GREEN_DYE);
        put(ChatColor.GOLD, Material.ORANGE_DYE);
        put(ChatColor.DARK_BLUE, Material.BLUE_DYE);
        put(ChatColor.BLUE, Material.LIGHT_BLUE_DYE);
        put(ChatColor.DARK_PURPLE, Material.PURPLE_DYE);
        put(ChatColor.DARK_AQUA, Material.CYAN_DYE);
        put(ChatColor.AQUA, Material.LIGHT_BLUE_DYE);
        put(ChatColor.GRAY, Material.LIGHT_GRAY_DYE);
        put(ChatColor.DARK_GRAY, Material.GRAY_DYE);
        put(ChatColor.LIGHT_PURPLE, Material.PINK_DYE);
        put(ChatColor.GREEN, Material.LIME_DYE);
        put(ChatColor.YELLOW, Material.YELLOW_DYE);
        put(ChatColor.WHITE, Material.WHITE_DYE);
    }};

    // Material mapping for wool (modernized - no durability needed)
    public static final Map<ChatColor, Material> woolMaterialMap = new HashMap<>() {{
        put(ChatColor.WHITE, Material.WHITE_WOOL);
        put(ChatColor.GOLD, Material.ORANGE_WOOL);
        put(ChatColor.LIGHT_PURPLE, Material.PINK_WOOL);
        put(ChatColor.AQUA, Material.LIGHT_BLUE_WOOL);
        put(ChatColor.YELLOW, Material.YELLOW_WOOL);
        put(ChatColor.GREEN, Material.LIME_WOOL);
        put(ChatColor.DARK_GRAY, Material.GRAY_WOOL);
        put(ChatColor.GRAY, Material.LIGHT_GRAY_WOOL);
        put(ChatColor.DARK_AQUA, Material.CYAN_WOOL);
        put(ChatColor.DARK_PURPLE, Material.PURPLE_WOOL);
        put(ChatColor.DARK_BLUE, Material.BLUE_WOOL);
        put(ChatColor.BLUE, Material.LIGHT_BLUE_WOOL);
        put(ChatColor.DARK_GREEN, Material.GREEN_WOOL);
        put(ChatColor.RED, Material.RED_WOOL);
        put(ChatColor.DARK_RED, Material.RED_WOOL);
        put(ChatColor.BLACK, Material.BLACK_WOOL);
    }};

    /**
     * Gets the dye Material for a given rank color string
     */
    public Material getDyeMaterial(String rankColor) {
        if (rankColor == null || rankColor.isEmpty()) return Material.WHITE_DYE;

        ChatColor color = parseChatColor(rankColor);
        if (color == null || !color.isColor()) return Material.WHITE_DYE;

        Material material = dyeMaterialMap.get(color);
        return material != null ? material : Material.WHITE_DYE;
    }

    /**
     * Gets the wool Material for a given ChatColor
     */
    public Material getWoolMaterial(ChatColor chatColor) {
        if (chatColor == null || !chatColor.isColor()) return Material.WHITE_WOOL;

        Material material = woolMaterialMap.get(chatColor);
        return material != null ? material : Material.WHITE_WOOL;
    }

    /**
     * Parses a color string to ChatColor
     */
    private ChatColor parseChatColor(String color) {
        if (color.contains("§") || color.contains("&")) {
            char codeChar = color.charAt(1);
            return ChatColor.getByChar(codeChar);
        }
        try {
            return ChatColor.valueOf(color.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
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
        return switch (type) {
            case BAN -> DyeColor.RED;
            case MUTE -> DyeColor.GREEN;
            case KICK -> DyeColor.ORANGE;
            case BLACKLIST -> DyeColor.PURPLE;
            default -> DyeColor.WHITE;
        };
    }

    public Color chatColorToLeatherColor(ChatColor chatColor) {
        if (chatColor == null) return Color.WHITE;

        return switch (chatColor) {
            case BLACK -> Color.fromRGB(0, 0, 0);
            case DARK_BLUE -> Color.fromRGB(0, 0, 170);
            case DARK_GREEN -> Color.fromRGB(0, 170, 0);
            case DARK_AQUA -> Color.fromRGB(0, 170, 170);
            case DARK_RED -> Color.fromRGB(170, 0, 0);
            case DARK_PURPLE -> Color.fromRGB(170, 0, 170);
            case GOLD -> Color.fromRGB(255, 170, 0);
            case GRAY -> Color.fromRGB(170, 170, 170);
            case DARK_GRAY -> Color.fromRGB(85, 85, 85);
            case BLUE -> Color.fromRGB(85, 85, 255);
            case GREEN -> Color.fromRGB(85, 255, 85);
            case AQUA -> Color.fromRGB(85, 255, 255);
            case RED -> Color.fromRGB(255, 85, 85);
            case LIGHT_PURPLE -> Color.fromRGB(255, 85, 255);
            case YELLOW -> Color.fromRGB(255, 255, 85);
            case WHITE -> Color.fromRGB(255, 255, 255);
            default -> Color.WHITE;
        };
    }

    public ChatColor dyeColorToChatColor(DyeColor dyeColor) {
        return switch (dyeColor) {
            case ORANGE, BROWN -> ChatColor.GOLD;
            case MAGENTA, PINK -> ChatColor.LIGHT_PURPLE;
            case LIGHT_BLUE -> ChatColor.BLUE;
            case YELLOW -> ChatColor.YELLOW;
            case LIME -> ChatColor.GREEN;
            case GRAY -> ChatColor.DARK_GRAY;
            case LIGHT_GRAY -> ChatColor.GRAY;
            case CYAN -> ChatColor.DARK_AQUA;
            case PURPLE -> ChatColor.DARK_PURPLE;
            case BLUE -> ChatColor.DARK_BLUE;
            case GREEN -> ChatColor.DARK_GREEN;
            case RED -> ChatColor.RED;
            case BLACK -> ChatColor.BLACK;
            default -> ChatColor.WHITE;
        };
    }

    public ChatColor fromString(String color) {
        if (color.contains("§") || color.contains("&")) {
            char codeChar = color.charAt(1);
            return ChatColor.getByChar(codeChar);
        }
        return null;
    }

    public DyeColor chatColorToDyeColor(ChatColor color) {
        return switch (color) {
            case GOLD -> DyeColor.ORANGE;
            case LIGHT_PURPLE -> DyeColor.PINK;
            case BLUE -> DyeColor.LIGHT_BLUE;
            case YELLOW -> DyeColor.YELLOW;
            case GREEN -> DyeColor.LIME;
            case DARK_GRAY -> DyeColor.GRAY;
            case GRAY -> DyeColor.LIGHT_GRAY;
            case DARK_AQUA -> DyeColor.CYAN;
            case DARK_PURPLE -> DyeColor.PURPLE;
            case DARK_BLUE -> DyeColor.BLUE;
            case DARK_GREEN -> DyeColor.GREEN;
            case RED, DARK_RED -> DyeColor.RED;
            case BLACK -> DyeColor.BLACK;
            default -> DyeColor.WHITE;
        };
    }

    /**
     * @deprecated Wool no longer uses durability in modern versions
     */
    @Deprecated
    public short getWoolDurability(ChatColor color) {
        return 0;
    }
}