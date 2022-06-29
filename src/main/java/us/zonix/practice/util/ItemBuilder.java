package us.zonix.practice.util;

import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.Color;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.material.MaterialData;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.event.Listener;

public class ItemBuilder implements Listener
{
    private final ItemStack is;
    
    public ItemBuilder(final Material mat) {
        this.is = new ItemStack(mat);
    }
    
    public ItemBuilder(final ItemStack is) {
        this.is = is;
    }
    
    public ItemBuilder amount(final int amount) {
        this.is.setAmount(amount);
        return this;
    }
    
    public ItemBuilder name(final String name) {
        final ItemMeta meta = this.is.getItemMeta();
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
        this.is.setItemMeta(meta);
        return this;
    }
    
    public ItemBuilder lore(final String name) {
        final ItemMeta meta = this.is.getItemMeta();
        List<String> lore = (List<String>)meta.getLore();
        if (lore == null) {
            lore = new ArrayList<String>();
        }
        lore.add(ChatColor.translateAlternateColorCodes('&', name));
        meta.setLore((List)lore);
        this.is.setItemMeta(meta);
        return this;
    }
    
    public ItemBuilder lore(final List<String> lore) {
        final List<String> toSet = new ArrayList<String>();
        final ItemMeta meta = this.is.getItemMeta();
        for (final String string : lore) {
            toSet.add(ChatColor.translateAlternateColorCodes('&', string));
        }
        meta.setLore((List)toSet);
        this.is.setItemMeta(meta);
        return this;
    }
    
    public ItemBuilder lore(final String... lore) {
        return this.lore(Arrays.asList(lore));
    }
    
    public ItemBuilder durability(final int durability) {
        this.is.setDurability((short)durability);
        return this;
    }
    
    public ItemBuilder data(final int data) {
        this.is.setData(new MaterialData(this.is.getType(), (byte)data));
        return this;
    }
    
    public ItemBuilder enchantment(final Enchantment enchantment, final int level) {
        this.is.addUnsafeEnchantment(enchantment, level);
        return this;
    }
    
    public ItemBuilder enchantment(final Enchantment enchantment) {
        this.is.addUnsafeEnchantment(enchantment, 1);
        return this;
    }
    
    public ItemBuilder type(final Material material) {
        this.is.setType(material);
        return this;
    }
    
    public ItemBuilder clearLore() {
        final ItemMeta meta = this.is.getItemMeta();
        meta.setLore((List)new ArrayList());
        this.is.setItemMeta(meta);
        return this;
    }
    
    public ItemBuilder clearEnchantments() {
        for (final Enchantment e : this.is.getEnchantments().keySet()) {
            this.is.removeEnchantment(e);
        }
        return this;
    }
    
    public ItemBuilder color(final Color color) {
        if (this.is.getType() == Material.LEATHER_BOOTS || this.is.getType() == Material.LEATHER_CHESTPLATE || this.is.getType() == Material.LEATHER_HELMET || this.is.getType() == Material.LEATHER_LEGGINGS) {
            final LeatherArmorMeta meta = (LeatherArmorMeta)this.is.getItemMeta();
            meta.setColor(color);
            this.is.setItemMeta((ItemMeta)meta);
            return this;
        }
        throw new IllegalArgumentException("color() only applicable for leather armor!");
    }
    
    public ItemStack build() {
        return this.is;
    }
}
