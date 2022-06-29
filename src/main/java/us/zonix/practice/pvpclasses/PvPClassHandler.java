package us.zonix.practice.pvpclasses;

import java.util.HashMap;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.EventPriority;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.event.Event;
import us.zonix.practice.pvpclasses.event.BardRestoreEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import java.util.Iterator;
import org.bukkit.plugin.Plugin;
import us.zonix.practice.Practice;
import us.zonix.practice.pvpclasses.pvpclasses.ArcherClass;
import us.zonix.practice.pvpclasses.pvpclasses.BardClass;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.Map;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

public class PvPClassHandler extends BukkitRunnable implements Listener
{
    private static Map<String, PvPClass> equippedKits;
    private static Map<UUID, PvPClass.SavedPotion> savedPotions;
    List<PvPClass> pvpClasses;
    
    public static Map<String, PvPClass> getEquippedKits() {
        return PvPClassHandler.equippedKits;
    }
    
    public static Map<UUID, PvPClass.SavedPotion> getSavedPotions() {
        return PvPClassHandler.savedPotions;
    }
    
    public List<PvPClass> getPvpClasses() {
        return this.pvpClasses;
    }
    
    public PvPClassHandler() {
        (this.pvpClasses = new ArrayList<PvPClass>()).add(new BardClass());
        this.pvpClasses.add(new ArcherClass());
        for (final PvPClass pvpClass : this.pvpClasses) {
            Practice.getInstance().getServer().getPluginManager().registerEvents((Listener)pvpClass, (Plugin)Practice.getInstance());
        }
        Practice.getInstance().getServer().getScheduler().runTaskTimer((Plugin)Practice.getInstance(), (BukkitRunnable)this, 2L, 2L);
        Practice.getInstance().getServer().getPluginManager().registerEvents((Listener)this, (Plugin)Practice.getInstance());
    }
    
    public void run() {
        for (final Player player : Practice.getInstance().getServer().getOnlinePlayers()) {
            if (PvPClassHandler.equippedKits.containsKey(player.getName())) {
                final PvPClass equippedPvPClass = PvPClassHandler.equippedKits.get(player.getName());
                if (!equippedPvPClass.qualifies(player.getInventory())) {
                    PvPClassHandler.equippedKits.remove(player.getName());
                    player.sendMessage(ChatColor.RED + equippedPvPClass.getName() + " class has been disabled.");
                    equippedPvPClass.remove(player);
                    PvPClass.removeInfiniteEffects(player);
                }
                else {
                    if (player.hasMetadata("frozen")) {
                        continue;
                    }
                    equippedPvPClass.tick(player);
                }
            }
            else {
                for (final PvPClass pvpClass : this.pvpClasses) {
                    if (pvpClass.qualifies(player.getInventory()) && pvpClass.canApply(player) && !player.hasMetadata("frozen")) {
                        pvpClass.apply(player);
                        getEquippedKits().put(player.getName(), pvpClass);
                        player.sendMessage(ChatColor.GREEN + pvpClass.getName() + " class has been enabled.");
                    }
                }
            }
        }
        this.checkSavedPotions();
    }
    
    public void checkSavedPotions() {
        final Iterator<Map.Entry<UUID, PvPClass.SavedPotion>> idIterator = PvPClassHandler.savedPotions.entrySet().iterator();
        while (idIterator.hasNext()) {
            final Map.Entry<UUID, PvPClass.SavedPotion> id = idIterator.next();
            final Player player = Bukkit.getPlayer((UUID)id.getKey());
            if (player != null && player.isOnline()) {
                Bukkit.getPluginManager().callEvent((Event)new BardRestoreEvent(player, id.getValue()));
                if (id.getValue().getTime() >= System.currentTimeMillis() || id.getValue().isPerm()) {
                    continue;
                }
                if (player.hasPotionEffect(id.getValue().getPotionEffect().getType())) {
                    final Map.Entry<K, PvPClass.SavedPotion> entry;
                    final PotionEffect restore;
                    final Player player2;
                    player.getActivePotionEffects().forEach(potion -> {
                        restore = entry.getValue().getPotionEffect();
                        if (potion.getType() == restore.getType() && potion.getDuration() < restore.getDuration() && potion.getAmplifier() <= restore.getAmplifier()) {
                            player2.removePotionEffect(restore.getType());
                        }
                        return;
                    });
                }
                if (!player.addPotionEffect(id.getValue().getPotionEffect(), true)) {
                    continue;
                }
                idIterator.remove();
            }
            else {
                idIterator.remove();
            }
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerInteract(final PlayerInteractEvent event) {
        if (event.getPlayer().getItemInHand() == null || (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK)) {
            return;
        }
        for (final PvPClass pvPClass : this.pvpClasses) {
            if (hasKitOn(event.getPlayer(), pvPClass) && pvPClass.getConsumables() != null && pvPClass.getConsumables().contains(event.getPlayer().getItemInHand().getType()) && pvPClass.itemConsumed(event.getPlayer(), event.getItem().getType())) {
                if (event.getPlayer().getItemInHand().getAmount() > 1) {
                    event.getPlayer().getItemInHand().setAmount(event.getPlayer().getItemInHand().getAmount() - 1);
                }
                else {
                    event.getPlayer().getInventory().remove(event.getPlayer().getItemInHand());
                }
            }
        }
    }
    
    public static PvPClass getPvPClass(final Player player) {
        return PvPClassHandler.equippedKits.getOrDefault(player.getName(), null);
    }
    
    public static boolean hasKitOn(final Player player, final PvPClass pvpClass) {
        return PvPClassHandler.equippedKits.containsKey(player.getName()) && PvPClassHandler.equippedKits.get(player.getName()) == pvpClass;
    }
    
    @EventHandler
    public void onPlayerQuit(final PlayerQuitEvent event) {
        if (PvPClassHandler.equippedKits.containsKey(event.getPlayer().getName())) {
            PvPClassHandler.equippedKits.get(event.getPlayer().getName()).remove(event.getPlayer());
            PvPClassHandler.equippedKits.remove(event.getPlayer().getName());
        }
    }
    
    @EventHandler
    public void onPlayerKick(final PlayerKickEvent event) {
        if (PvPClassHandler.equippedKits.containsKey(event.getPlayer().getName())) {
            PvPClassHandler.equippedKits.get(event.getPlayer().getName()).remove(event.getPlayer());
            PvPClassHandler.equippedKits.remove(event.getPlayer().getName());
        }
    }
    
    @EventHandler
    public void onPlayerJoin(final PlayerJoinEvent event) {
        if (PvPClassHandler.equippedKits.containsKey(event.getPlayer().getName())) {
            PvPClassHandler.equippedKits.get(event.getPlayer().getName()).remove(event.getPlayer());
            PvPClassHandler.equippedKits.remove(event.getPlayer().getName());
        }
        for (final PotionEffect potionEffect : event.getPlayer().getActivePotionEffects()) {
            if (potionEffect.getDuration() > 1000000) {
                event.getPlayer().removePotionEffect(potionEffect.getType());
            }
        }
    }
    
    static {
        PvPClassHandler.equippedKits = new HashMap<String, PvPClass>();
        PvPClassHandler.savedPotions = new HashMap<UUID, PvPClass.SavedPotion>();
    }
}
