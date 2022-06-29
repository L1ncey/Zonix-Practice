package us.zonix.practice.party.selection.archer;

import org.bukkit.Sound;
import org.bukkit.event.inventory.InventoryClickEvent;
import us.zonix.practice.util.ItemBuilder;
import org.bukkit.Material;
import us.zonix.practice.player.PlayerData;
import org.bukkit.inventory.ItemStack;
import java.util.concurrent.atomic.AtomicBoolean;
import org.bukkit.Bukkit;
import java.util.UUID;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;
import us.zonix.practice.Practice;
import us.zonix.practice.party.Party;
import org.bukkit.entity.Player;
import us.zonix.practice.util.inventory.InventoryUI;

public class ArcherSelectMenu
{
    private static final InventoryUI.ClickableItem GO_BACK_CLICKABLE;
    private static final InventoryUI.ClickableItem RED_GLASS_CLICKABLE;
    private final Player player;
    private final Party party;
    private final InventoryUI inventoryUI;
    
    public ArcherSelectMenu(final Player player) {
        this.player = player;
        this.party = ((Practice)JavaPlugin.getPlugin((Class)Practice.class)).getPartyManager().getParty(player.getUniqueId());
        this.inventoryUI = new InventoryUI(ChatColor.GRAY + "Archer Selection", (int)(Math.ceil(this.party.getMembers().size() / 9.0f) + 1.0));
    }
    
    public void open() {
        if (this.player == null || this.party == null) {
            return;
        }
        this.refreshItems();
        this.player.openInventory(this.inventoryUI.getCurrentPage());
    }
    
    private void refreshItems() {
        for (int i = 0; i < 9 * this.inventoryUI.getRows(); ++i) {
            this.inventoryUI.setItem(i, ArcherSelectMenu.RED_GLASS_CLICKABLE);
        }
        for (int i = 0; i < this.party.getMembers().size(); ++i) {
            this.inventoryUI.setItem(i, this.getItemStack(Bukkit.getPlayer((UUID)this.party.getMembers().get(i))));
        }
        this.inventoryUI.setItem(this.inventoryUI.getRows() * 9 - 1, ArcherSelectMenu.GO_BACK_CLICKABLE);
    }
    
    private InventoryUI.ClickableItem getItemStack(final Player player) {
        final AtomicBoolean archer = new AtomicBoolean(this.party.getArchers().contains(player.getUniqueId()));
        final PlayerData playerData = ((Practice)JavaPlugin.getPlugin((Class)Practice.class)).getPlayerManager().getPlayerData(player.getUniqueId());
        return new InventoryUI.ClickableItem() {
            private final ItemStack def = new ItemBuilder(archer.get() ? Material.LEATHER_HELMET : Material.DIAMOND_HELMET).name(ChatColor.GOLD.toString() + player.getName() + (archer.get() ? (ChatColor.RED + " (Archer)") : (ChatColor.AQUA + " (Diamond)"))).lore(ChatColor.translateAlternateColorCodes('&', "&7Click to set &7" + player.getName() + "'s PvP Class &7to " + (archer.get() ? "&bDiamond" : "&cArcher") + "&7.")).lore(ChatColor.translateAlternateColorCodes('&', "&6" + player.getName() + " &7has played &cArcher &7" + playerData.getPlayedArcher() + " time" + ((playerData.getPlayedArcher() == 1) ? "" : "s") + "&7.")).build();
            private ItemStack itemStack = this.def.clone();
            
            @Override
            public void onClick(final InventoryClickEvent event) {
                event.setCancelled(true);
                if (archer.get()) {
                    ArcherSelectMenu.this.party.getArchers().remove(player.getUniqueId());
                }
                else {
                    final Player sender = (Player)event.getWhoClicked();
                    if (ArcherSelectMenu.this.party.getArchers().size() >= ArcherSelectMenu.this.party.getMaxArchers()) {
                        sender.sendMessage(ChatColor.RED + String.format("Your party has already reached the limit of %s archer" + ((ArcherSelectMenu.this.party.getMaxArchers() == 1) ? "" : "s") + ".", ArcherSelectMenu.this.party.getMaxArchers()));
                        return;
                    }
                    if (ArcherSelectMenu.this.party.getArchers().contains(player.getUniqueId())) {
                        sender.sendMessage(ChatColor.RED + String.format("%s's role is already archer.", player.getName()));
                        return;
                    }
                    ArcherSelectMenu.this.party.addArcher(player);
                    sender.sendMessage(ChatColor.GREEN + player.getName() + "'s role is now archer.");
                }
                ArcherSelectMenu.this.refreshItems();
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
    }
    
    static {
        GO_BACK_CLICKABLE = new InventoryUI.ClickableItem() {
            private final ItemStack def = new ItemBuilder(Material.FIREBALL).name(ChatColor.translateAlternateColorCodes('&', "&cGo Back")).lore(ChatColor.translateAlternateColorCodes('&', "&7Click to go back to the Class Selection Menu.")).build();
            private ItemStack itemStack = this.def.clone();
            
            @Override
            public void onClick(final InventoryClickEvent event) {
                ((Player)event.getWhoClicked()).playSound(event.getWhoClicked().getLocation(), Sound.CLICK, 1.0f, 1.0f);
                Party.CLASS_SELECTION_MENU.open((Player)event.getWhoClicked());
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
        RED_GLASS_CLICKABLE = new InventoryUI.ClickableItem() {
            private final ItemStack def = new ItemBuilder(Material.STAINED_GLASS_PANE).name(ChatColor.translateAlternateColorCodes('&', "&c")).durability(14).build();
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
    }
}
