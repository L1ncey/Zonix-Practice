package us.zonix.practice.party.selection;

import us.zonix.practice.party.selection.archer.ArcherSelectMenu;
import org.bukkit.Sound;
import us.zonix.practice.party.selection.bard.BardSelectMenu;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import us.zonix.practice.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.ChatColor;
import us.zonix.practice.util.inventory.InventoryUI;

public class ClassSelectionMenu
{
    private final InventoryUI inventoryUI;
    private final InventoryUI.ClickableItem clickableItem;
    
    public ClassSelectionMenu() {
        this.inventoryUI = new InventoryUI(ChatColor.GRAY + "Class Selection", true, 3);
        this.clickableItem = new InventoryUI.ClickableItem() {
            private final ItemStack def = new ItemBuilder(Material.STAINED_GLASS_PANE).name(ChatColor.translateAlternateColorCodes('&', "&c")).durability(15).build();
            private ItemStack itemStack = this.def.clone();
            
            @Override
            public void onClick(final InventoryClickEvent event) {
                event.setCancelled(true);
            }
            
            @Override
            public ItemStack getItemStack() {
                return this.itemStack;
            }
            
            @Override
            public void setItemStack(final ItemStack itemStack) {
                this.itemStack = itemStack;
            }
            
            @Override
            public ItemStack getDefaultItemStack() {
                return this.def;
            }
        };
        this.initializeMenu();
    }
    
    private void initializeMenu() {
        for (int i = 0; i < this.inventoryUI.getRows() * 9; ++i) {
            this.inventoryUI.setItem(i, this.clickableItem);
        }
        this.inventoryUI.setItem(11, new InventoryUI.ClickableItem() {
            private final ItemStack def = new ItemBuilder(Material.WOOL).name(ChatColor.GOLD.toString() + ChatColor.BOLD + "Select Bards").lore(ChatColor.translateAlternateColorCodes('&', "&7Click to open the &6Bard Selection Menu&7.")).durability(4).build();
            private ItemStack itemStack = this.def.clone();
            
            @Override
            public void onClick(final InventoryClickEvent event) {
                if (event.getWhoClicked() instanceof Player) {
                    new BardSelectMenu((Player)event.getWhoClicked()).open();
                    ((Player)event.getWhoClicked()).playSound(event.getWhoClicked().getLocation(), Sound.CLICK, 1.0f, 1.0f);
                }
                event.setCancelled(true);
            }
            
            @Override
            public ItemStack getItemStack() {
                return this.itemStack;
            }
            
            @Override
            public void setItemStack(final ItemStack itemStack) {
                this.itemStack = itemStack;
            }
            
            @Override
            public ItemStack getDefaultItemStack() {
                return this.def;
            }
        });
        this.inventoryUI.setItem(13, new InventoryUI.ClickableItem() {
            private final ItemStack def = new ItemBuilder(Material.NETHER_STAR).name(ChatColor.DARK_AQUA.toString() + ChatColor.BOLD + "Class Selection").lore(ChatColor.translateAlternateColorCodes('&', "&7Select your party's &cArchers &7and &6Bards &7using this gui.")).build();
            private ItemStack itemStack = this.def.clone();
            
            @Override
            public void onClick(final InventoryClickEvent event) {
                event.setCancelled(true);
            }
            
            @Override
            public ItemStack getItemStack() {
                return this.itemStack;
            }
            
            @Override
            public void setItemStack(final ItemStack itemStack) {
                this.itemStack = itemStack;
            }
            
            @Override
            public ItemStack getDefaultItemStack() {
                return this.def;
            }
        });
        this.inventoryUI.setItem(15, new InventoryUI.ClickableItem() {
            private final ItemStack def = new ItemBuilder(Material.WOOL).name(ChatColor.RED.toString() + ChatColor.BOLD + "Select Archers").lore(ChatColor.translateAlternateColorCodes('&', "&7Click to open the &cArcher Selection Menu&7.")).durability(14).build();
            private ItemStack itemStack = this.def.clone();
            
            @Override
            public void onClick(final InventoryClickEvent event) {
                if (event.getWhoClicked() instanceof Player) {
                    new ArcherSelectMenu((Player)event.getWhoClicked()).open();
                    ((Player)event.getWhoClicked()).playSound(event.getWhoClicked().getLocation(), Sound.CLICK, 1.0f, 1.0f);
                }
                event.setCancelled(true);
            }
            
            @Override
            public ItemStack getItemStack() {
                return this.itemStack;
            }
            
            @Override
            public void setItemStack(final ItemStack itemStack) {
                this.itemStack = itemStack;
            }
            
            @Override
            public ItemStack getDefaultItemStack() {
                return this.def;
            }
        });
    }
    
    public void open(final Player player) {
        player.openInventory(this.inventoryUI.getCurrentPage());
    }
}
