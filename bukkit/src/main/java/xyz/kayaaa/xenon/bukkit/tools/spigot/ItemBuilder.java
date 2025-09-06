package xyz.kayaaa.xenon.bukkit.tools.spigot;

import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.SkullMeta;
import xyz.kayaaa.xenon.shared.tools.java.Reflectionism;
import xyz.kayaaa.xenon.shared.tools.string.CC;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A fluent builder for creating and customizing ItemStacks in Bukkit/Spigot.
 */
public class ItemBuilder {

    private final ItemStack itemStack;

    /**
     * Constructs an ItemBuilder with the given Material.
     *
     * @param material The material type of the ItemStack to create.
     */
    public ItemBuilder(Material material) {
        this.itemStack = new ItemStack(material);
    }

    /**
     * Constructs an ItemBuilder with an existing ItemStack.
     *
     * @param itemStack The ItemStack to modify.
     */
    public ItemBuilder(ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    /**
     * Sets the amount of items in the ItemStack.
     *
     * @param amount The amount of items.
     * @return The ItemBuilder instance for method chaining.
     */
    public ItemBuilder amount(int amount) {
        itemStack.setAmount(amount);
        return this;
    }

    /**
     * Sets the display name of the ItemStack.
     *
     * @param name The display name to set.
     * @return The ItemBuilder instance for method chaining.
     */
    public ItemBuilder name(String name) {
        ItemMeta meta = getItemMeta();
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
        itemStack.setItemMeta(meta);
        return this;
    }

    /**
     * Adds lore lines to the ItemStack.
     *
     * @param lore The lore lines to add.
     * @return The ItemBuilder instance for method chaining.
     */
    public ItemBuilder lore(String... lore) {
        ItemMeta meta = getItemMeta();
        List<String> coloredLore = meta.getLore();

        if (coloredLore == null) {
            coloredLore = new ArrayList<>();
        }

        coloredLore.addAll(CC.translate(Arrays.asList(lore)));
        meta.setLore(coloredLore);
        itemStack.setItemMeta(meta);
        return this;
    }

    /**
     * Adds lore lines to the ItemStack.
     *
     * @param lore The list of lore lines to add.
     * @return The ItemBuilder instance for method chaining.
     */
    public ItemBuilder lore(List<String> lore) {
        if (lore == null) {
            return this;
        }
        ItemMeta meta = getItemMeta();
        List<String> coloredLore = meta.getLore();

        if (coloredLore == null) {
            coloredLore = new ArrayList<>();
        }

        coloredLore.addAll(CC.translate(lore));
        meta.setLore(coloredLore);
        itemStack.setItemMeta(meta);
        return this;
    }

    public ItemBuilder lore(String color, List<String> lore) {
        if (lore == null) {
            return this;
        }
        ItemMeta meta = getItemMeta();
        List<String> coloredLore = meta.getLore();

        if (coloredLore == null) {
            coloredLore = new ArrayList<>();
        }

        for (String line : lore) {
            coloredLore.add(CC.translate(color + line));
        }

        meta.setLore(coloredLore);
        itemStack.setItemMeta(meta);
        return this;
    }

    public ItemBuilder skull(String owner) {
        ItemMeta meta = this.itemStack.getItemMeta();
        if (meta instanceof SkullMeta) {
            SkullMeta im = (SkullMeta) meta;
            this.durability((short) 3);
            im.setOwner(owner);
            this.itemStack.setItemMeta(im);
        }
        return this;
    }

    public ItemBuilder dyeColor(DyeColor dyeColor) {
        if (this.itemStack.getType() != Material.INK_SACK) {
            return this;
        }

        return this.durability(dyeColor.getDyeData());
    }

    /**
     * Clears all lore lines from the ItemStack.
     *
     * @return The ItemBuilder instance for method chaining.
     */
    public ItemBuilder clearLore() {
        ItemMeta meta = getItemMeta();
        meta.setLore(null);
        itemStack.setItemMeta(meta);
        return this;
    }

    /**
     * Enchants the ItemStack with the specified Enchantment at the given level.
     *
     * @param enchantment The enchantment to apply.
     * @param level       The level of the enchantment.
     * @return The ItemBuilder instance for method chaining.
     */
    public ItemBuilder enchant(Enchantment enchantment, int level) {
        if (enchantment == null || level <= 0) return this;

        ItemMeta meta = getItemMeta();
        meta.addEnchant(enchantment, level, true);
        itemStack.setItemMeta(meta);
        return this;
    }

    /**
     * Enchants the ItemStack with the specified Enchantment at level 1.
     *
     * @param enchantment The enchantment to apply.
     * @return The ItemBuilder instance for method chaining.
     */
    public ItemBuilder enchant(Enchantment enchantment) {
        if (enchantment == null) return this;
        return enchant(enchantment, 1);
    }

    /**
     * Clears all enchantments from the ItemStack.
     *
     * @return The ItemBuilder instance for method chaining.
     */
    public ItemBuilder clearEnchantments() {
        itemStack.getEnchantments().keySet().forEach(itemStack::removeEnchantment);
        return this;
    }

    /**
     * Sets the durability of the ItemStack.
     *
     * @param durability The durability value to set.
     * @return The ItemBuilder instance for method chaining.
     */
    public ItemBuilder durability(int durability) {
        itemStack.setDurability((short) durability);
        return this;
    }

    /**
     * Sets the color of the ItemStack.
     * Works with leather armor, wool, and carpet.
     *
     * @param color The color to set.
     * @return The ItemBuilder instance for method chaining.
     */
    public ItemBuilder color(Color color) {
        String typeName = itemStack.getType().name();

        // Para armaduras de couro
        if (typeName.contains("LEATHER_")) {
            LeatherArmorMeta meta = (LeatherArmorMeta) getItemMeta();
            meta.setColor(color);
            itemStack.setItemMeta(meta);
        }
        // Para lã e carpete
        else if (typeName.equals("WOOL") || typeName.equals("CARPET")) {
            short woolData = getWoolDataFromColor(color);
            itemStack.setDurability(woolData);
        }

        return this;
    }

    /**
     * Converts a Color to wool data value (best match approximation).
     *
     * @param color The Color to convert.
     * @return The wool data value.
     */
    private short getWoolDataFromColor(Color color) {
        int r = color.getRed();
        int g = color.getGreen();
        int b = color.getBlue();

        // Aproximações baseadas nas cores RGB das lãs do Minecraft
        if (r >= 240 && g >= 240 && b >= 240) return 0; // WHITE
        if (r >= 200 && g >= 100 && b <= 50) return 1;  // ORANGE
        if (r >= 150 && g <= 100 && b >= 150) return 2; // MAGENTA
        if (r <= 100 && g >= 150 && b >= 200) return 3; // LIGHT_BLUE
        if (r >= 200 && g >= 200 && b <= 50) return 4;  // YELLOW
        if (r <= 100 && g >= 150 && b <= 100) return 5; // LIME
        if (r >= 200 && g >= 100 && b >= 100) return 6; // PINK
        if (r <= 100 && g <= 100 && b <= 100) return 7; // GRAY
        if (r >= 150 && g >= 150 && b >= 150) return 8; // LIGHT_GRAY
        if (r <= 100 && g >= 100 && b >= 150) return 9; // CYAN
        if (r >= 100 && g <= 100 && b >= 150) return 10; // PURPLE
        if (r <= 100 && g <= 100 && b >= 150) return 11; // BLUE
        if (r >= 100 && g >= 50 && b <= 50) return 12;   // BROWN
        if (r <= 100 && g >= 100 && b <= 100) return 13; // GREEN
        if (r >= 150 && g <= 100 && b <= 100) return 14; // RED

        return 15; // BLACK (padrão)
    }

    /**
     * Builds the final ItemStack configured by this ItemBuilder.
     *
     * @return The constructed ItemStack.
     */
    public ItemStack build() {
        if (itemStack.getType() != Material.AIR) {
            ItemMeta meta = getItemMeta();
            itemStack.setItemMeta(meta);
        }
        if (itemStack.getAmount() < 1) {
            itemStack.setAmount(1);
        }
        return itemStack;
    }

    private ItemMeta getItemMeta() {
        return itemStack.hasItemMeta() ? itemStack.getItemMeta() : Bukkit.getItemFactory().getItemMeta(itemStack.getType());
    }
}
