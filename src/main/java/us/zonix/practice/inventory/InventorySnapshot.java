package us.zonix.practice.inventory;

import org.bukkit.enchantments.Enchantment;
import org.json.simple.JSONObject;
import java.util.Iterator;
import java.util.List;
import us.zonix.practice.player.PlayerData;
import org.bukkit.command.CommandSender;
import org.bukkit.event.inventory.InventoryClickEvent;
import us.zonix.practice.util.ItemUtil;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.Objects;
import java.util.Arrays;
import org.bukkit.Material;
import org.bukkit.ChatColor;
import us.zonix.practice.util.StringUtil;
import us.zonix.practice.util.MathUtil;
import org.bukkit.potion.PotionEffect;
import java.util.ArrayList;
import us.zonix.practice.Practice;
import us.zonix.practice.match.Match;
import org.bukkit.entity.Player;
import java.util.UUID;
import org.bukkit.inventory.ItemStack;
import us.zonix.practice.util.inventory.InventoryUI;

public class InventorySnapshot
{
    private final InventoryUI inventoryUI;
    private final ItemStack[] originalInventory;
    private final ItemStack[] originalArmor;
    private final UUID snapshotId;
    
    public InventorySnapshot(final Player player, final Match match) {
        this.snapshotId = UUID.randomUUID();
        final ItemStack[] contents = player.getInventory().getContents();
        final ItemStack[] armor = player.getInventory().getArmorContents();
        this.originalInventory = contents;
        this.originalArmor = armor;
        final PlayerData playerData = Practice.getInstance().getPlayerManager().getPlayerData(player.getUniqueId());
        final double health = player.getHealth();
        final double food = player.getFoodLevel();
        final List<String> potionEffectStrings = new ArrayList<String>();
        for (final PotionEffect potionEffect : player.getActivePotionEffects()) {
            final String romanNumeral = MathUtil.convertToRomanNumeral(potionEffect.getAmplifier() + 1);
            final String effectName = StringUtil.toNiceString(potionEffect.getType().getName().toLowerCase());
            final String duration = MathUtil.convertTicksToMinutes(potionEffect.getDuration());
            potionEffectStrings.add(ChatColor.YELLOW.toString() + ChatColor.BOLD + "* " + ChatColor.WHITE + effectName + " " + romanNumeral + ChatColor.GRAY + " (" + duration + ")");
        }
        this.inventoryUI = new InventoryUI(player.getName() + "'s Inventory", true, 6);
        for (int i = 0; i < 9; ++i) {
            this.inventoryUI.setItem(i + 27, new InventoryUI.EmptyClickableItem(contents[i]));
            this.inventoryUI.setItem(i + 18, new InventoryUI.EmptyClickableItem(contents[i + 27]));
            this.inventoryUI.setItem(i + 9, new InventoryUI.EmptyClickableItem(contents[i + 18]));
            this.inventoryUI.setItem(i, new InventoryUI.EmptyClickableItem(contents[i + 9]));
        }
        boolean potionMatch = false;
        boolean soupMatch = false;
        for (final ItemStack item : match.getKit().getContents()) {
            if (item != null) {
                if (item.getType() == Material.MUSHROOM_SOUP) {
                    soupMatch = true;
                    break;
                }
                if (item.getType() == Material.POTION && item.getDurability() == 16421) {
                    potionMatch = true;
                    break;
                }
            }
        }
        int potCount = 0;
        if (potionMatch) {
            potCount = (int)Arrays.stream(contents).filter(Objects::nonNull).map((Function<? super ItemStack, ?>)ItemStack::getDurability).filter(d -> d == 16421).count();
            this.inventoryUI.setItem(45, new InventoryUI.EmptyClickableItem(ItemUtil.reloreItem(ItemUtil.createItem(Material.POTION, ChatColor.LIGHT_PURPLE.toString() + ChatColor.BOLD + "Potions", potCount, (short)16421), ChatColor.DARK_PURPLE.toString() + ChatColor.BOLD + "* " + ChatColor.WHITE + "Health Pots: " + ChatColor.GRAY + potCount + " Potion" + ((potCount > 1) ? "s" : ""), ChatColor.DARK_PURPLE.toString() + ChatColor.BOLD + "* " + ChatColor.WHITE + "Missed Pots: " + ChatColor.GRAY + playerData.getMissedPots() + " Potion" + ((playerData.getMissedPots() > 1) ? "s" : ""))));
        }
        else if (soupMatch) {
            final int soupCount = (int)Arrays.stream(contents).filter(Objects::nonNull).map((Function<? super ItemStack, ?>)ItemStack::getType).filter(d -> d == Material.MUSHROOM_SOUP).count();
            this.inventoryUI.setItem(45, new InventoryUI.EmptyClickableItem(ItemUtil.createItem(Material.MUSHROOM_SOUP, ChatColor.GOLD.toString() + ChatColor.BOLD + "Soups Left: " + ChatColor.WHITE + soupCount, soupCount, (short)16421)));
        }
        final double roundedHealth = Math.round(health / 2.0 * 2.0) / 2.0;
        this.inventoryUI.setItem(49, new InventoryUI.EmptyClickableItem(ItemUtil.createItem(Material.SKULL_ITEM, ChatColor.RED.toString() + ChatColor.BOLD + "\u2764 " + roundedHealth + " HP", (int)Math.round(health / 2.0))));
        final double roundedFood = Math.round(food / 2.0 * 2.0) / 2.0;
        this.inventoryUI.setItem(48, new InventoryUI.EmptyClickableItem(ItemUtil.createItem(Material.COOKED_BEEF, ChatColor.RED.toString() + ChatColor.BOLD + roundedFood + " Hunger", (int)Math.round(food / 2.0))));
        this.inventoryUI.setItem(47, new InventoryUI.EmptyClickableItem(ItemUtil.reloreItem(ItemUtil.createItem(Material.BREWING_STAND_ITEM, ChatColor.GOLD.toString() + ChatColor.BOLD + "Potion Effects", potionEffectStrings.size()), (String[])potionEffectStrings.toArray(new String[0]))));
        this.inventoryUI.setItem(46, new InventoryUI.EmptyClickableItem(ItemUtil.reloreItem(ItemUtil.createItem(Material.CAKE, ChatColor.YELLOW.toString() + ChatColor.BOLD + "Stats"), ChatColor.GOLD.toString() + ChatColor.BOLD + "* " + ChatColor.WHITE + "Longest Combo: " + ChatColor.GRAY + playerData.getLongestCombo() + " Hit" + ((playerData.getLongestCombo() > 1) ? "s" : ""), ChatColor.GOLD.toString() + ChatColor.BOLD + "* " + ChatColor.WHITE + "Total Hits: " + ChatColor.GRAY + playerData.getHits() + " Hit" + ((playerData.getHits() > 1) ? "s" : ""), ChatColor.GOLD.toString() + ChatColor.BOLD + "* " + ChatColor.WHITE + "Potion Accuracy: " + ChatColor.GRAY + ((playerData.getMissedPots() > 0) ? ((int)((28.0 - playerData.getMissedPots()) / 28.0 * 100.0) + "%") : "100%"))));
        if (!match.isParty()) {
            this.inventoryUI.setItem(53, new InventoryUI.AbstractClickableItem(ItemUtil.reloreItem(ItemUtil.createItem(Material.LEVER, ChatColor.RED + "Next Inventory"), new String[0])) {
                @Override
                public void onClick(final InventoryClickEvent inventoryClickEvent) {
                    final Player clicker = (Player)inventoryClickEvent.getWhoClicked();
                    if (Practice.getInstance().getMatchManager().isRematching(player.getUniqueId())) {
                        clicker.closeInventory();
                        Practice.getInstance().getServer().dispatchCommand((CommandSender)clicker, "inventory " + Practice.getInstance().getMatchManager().getRematcherInventory(player.getUniqueId()));
                    }
                }
            });
        }
        for (int j = 36; j < 40; ++j) {
            this.inventoryUI.setItem(j, new InventoryUI.EmptyClickableItem(armor[39 - j]));
        }
    }
    
    public JSONObject toJson() {
        final JSONObject object = new JSONObject();
        final JSONObject inventoryObject = new JSONObject();
        for (int i = 0; i < this.originalInventory.length; ++i) {
            inventoryObject.put((Object)i, (Object)this.encodeItem(this.originalInventory[i]));
        }
        object.put((Object)"inventory", (Object)inventoryObject);
        final JSONObject armourObject = new JSONObject();
        for (int j = 0; j < this.originalArmor.length; ++j) {
            armourObject.put((Object)j, (Object)this.encodeItem(this.originalArmor[j]));
        }
        object.put((Object)"armour", (Object)armourObject);
        return object;
    }
    
    private JSONObject encodeItem(final ItemStack itemStack) {
        if (itemStack == null || itemStack.getType() == Material.AIR) {
            return null;
        }
        final JSONObject object = new JSONObject();
        object.put((Object)"material", (Object)itemStack.getType().name());
        object.put((Object)"durability", (Object)itemStack.getDurability());
        object.put((Object)"amount", (Object)itemStack.getAmount());
        final JSONObject enchants = new JSONObject();
        for (final Enchantment enchantment : itemStack.getEnchantments().keySet()) {
            enchants.put((Object)enchantment.getName(), itemStack.getEnchantments().get(enchantment));
        }
        object.put((Object)"enchants", (Object)enchants);
        return object;
    }
    
    public InventoryUI getInventoryUI() {
        return this.inventoryUI;
    }
    
    public ItemStack[] getOriginalInventory() {
        return this.originalInventory;
    }
    
    public ItemStack[] getOriginalArmor() {
        return this.originalArmor;
    }
    
    public UUID getSnapshotId() {
        return this.snapshotId;
    }
}
