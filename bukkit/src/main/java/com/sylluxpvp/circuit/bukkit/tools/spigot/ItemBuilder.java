package com.sylluxpvp.circuit.bukkit.tools.spigot;

import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.SkullMeta;
import com.sylluxpvp.circuit.shared.tools.string.CC;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A fluent builder for creating and customizing ItemStacks in Bukkit/Spigot.
 * Updated for modern Minecraft versions (1.13+)
 */
public class ItemBuilder {

    private ItemStack itemStack;

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
     */
    public void amount(int amount) {
        itemStack.setAmount(amount);
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
        if (meta instanceof SkullMeta im) {
            im.setOwner(owner);
            this.itemStack.setItemMeta(im);
        }
        return this;
    }

    /**
     * @deprecated DyeColor with durability is no longer used in modern versions.
     * Use specific dye materials instead.
     */
    @Deprecated
    public ItemBuilder dyeColor(DyeColor dyeColor) {
        // This method is deprecated as dyes now use specific materials
        // Kept for backwards compatibility but does nothing
        return this;
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
     * Sets the color of the ItemStack.
     * Works with leather armor.
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

        return this;
    }

    /**
     * Changes the material of this ItemStack while preserving metadata.
     * Useful for changing wool/dye colors in modern versions.
     *
     * @param material The new material to use.
     * @return The ItemBuilder instance for method chaining.
     */
    public ItemBuilder material(Material material) {
        ItemMeta meta = itemStack.getItemMeta();
        this.itemStack = new ItemStack(material, itemStack.getAmount());
        if (meta != null) {
            itemStack.setItemMeta(meta);
        }
        return this;
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