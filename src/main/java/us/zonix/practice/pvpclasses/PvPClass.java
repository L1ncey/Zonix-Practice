package us.zonix.practice.pvpclasses;

import com.google.common.collect.HashBasedTable;
import org.bukkit.event.EventPriority;
import org.bukkit.event.EventHandler;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.PotionEffectExpireEvent;
import java.util.Collection;
import org.bukkit.inventory.PlayerInventory;
import java.util.Iterator;
import us.zonix.practice.match.Match;
import us.zonix.practice.party.Party;
import us.zonix.practice.pvpclasses.pvpclasses.BardClass;
import us.zonix.practice.pvpclasses.pvpclasses.ArcherClass;
import us.zonix.practice.player.PlayerState;
import us.zonix.practice.Practice;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import java.util.UUID;
import com.google.common.collect.Table;
import org.bukkit.Material;
import java.util.List;
import org.bukkit.event.Listener;

public abstract class PvPClass implements Listener
{
    String name;
    int warmup;
    String armorContains;
    List<Material> consumables;
    private static final Table<UUID, PotionEffectType, PotionEffect> restores;
    
    public String getName() {
        return this.name;
    }
    
    public int getWarmup() {
        return this.warmup;
    }
    
    public String getArmorContains() {
        return this.armorContains;
    }
    
    public List<Material> getConsumables() {
        return this.consumables;
    }
    
    public PvPClass(final String name, final int warmup, final String armorContains, final List<Material> consumables) {
        this.name = name;
        this.warmup = warmup;
        this.armorContains = armorContains;
        this.consumables = consumables;
    }
    
    public void apply(final Player player) {
    }
    
    public void tick(final Player player) {
    }
    
    public void remove(final Player player) {
    }
    
    public boolean canApply(final Player player) {
        if (Practice.getInstance().getPlayerManager().getPlayerData(player.getUniqueId()).getPlayerState() != PlayerState.FIGHTING) {
            return false;
        }
        final Party party = Practice.getInstance().getPartyManager().getParty(player.getUniqueId());
        final Match match = Practice.getInstance().getMatchManager().getMatch(player.getUniqueId());
        if (match == null) {
            return false;
        }
        if (!match.getKit().isHcteams()) {
            return false;
        }
        if (party != null) {
            if (this instanceof ArcherClass && party.getArchers().contains(player.getUniqueId())) {
                return true;
            }
            if (this instanceof BardClass && party.getBards().contains(player.getUniqueId())) {
                return true;
            }
        }
        return false;
    }
    
    public static void removeInfiniteEffects(final Player player) {
        for (final PotionEffect potionEffect : player.getActivePotionEffects()) {
            if (potionEffect.getDuration() > 1000000) {
                player.removePotionEffect(potionEffect.getType());
            }
        }
    }
    
    public boolean itemConsumed(final Player player, final Material type) {
        return true;
    }
    
    public boolean qualifies(final PlayerInventory armor) {
        return armor.getHelmet() != null && armor.getChestplate() != null && armor.getLeggings() != null && armor.getBoots() != null && armor.getHelmet().getType().name().startsWith(this.armorContains) && armor.getChestplate().getType().name().startsWith(this.armorContains) && armor.getLeggings().getType().name().startsWith(this.armorContains) && armor.getBoots().getType().name().startsWith(this.armorContains);
    }
    
    public static void smartAddPotion(final Player player, final PotionEffect potionEffect, final boolean persistOldValues, final PvPClass pvpClass) {
        setRestoreEffect(player, potionEffect);
    }
    
    public static void setRestoreEffect(final Player player, final PotionEffect effect) {
        boolean shouldCancel = true;
        final Collection<PotionEffect> activeList = (Collection<PotionEffect>)player.getActivePotionEffects();
        for (final PotionEffect active : activeList) {
            if (!active.getType().equals((Object)effect.getType())) {
                continue;
            }
            if (effect.getAmplifier() < active.getAmplifier()) {
                return;
            }
            if (effect.getAmplifier() == active.getAmplifier() && 0 < active.getDuration() && (effect.getDuration() <= active.getDuration() || effect.getDuration() - active.getDuration() < 10)) {
                return;
            }
            PvPClass.restores.put((Object)player.getUniqueId(), (Object)active.getType(), (Object)active);
            shouldCancel = false;
        }
        player.addPotionEffect(effect, true);
        if (shouldCancel && effect.getDuration() > 120 && effect.getDuration() < 9600) {
            PvPClass.restores.remove((Object)player.getUniqueId(), (Object)effect.getType());
        }
    }
    
    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPotionEffectExpire(final PotionEffectExpireEvent event) {
        final LivingEntity livingEntity = event.getEntity();
        if (livingEntity instanceof Player) {
            final Player player = (Player)livingEntity;
            final PotionEffect previous = (PotionEffect)PvPClass.restores.remove((Object)player.getUniqueId(), (Object)event.getEffect().getType());
            if (previous != null && previous.getDuration() < 1000000) {
                event.setCancelled(true);
                player.addPotionEffect(previous, true);
            }
        }
    }
    
    static {
        restores = (Table)HashBasedTable.create();
    }
    
    public static class SavedPotion
    {
        PotionEffect potionEffect;
        long time;
        private boolean perm;
        
        public SavedPotion(final PotionEffect potionEffect, final long time, final boolean perm) {
            this.potionEffect = potionEffect;
            this.time = time;
            this.perm = perm;
        }
        
        public PotionEffect getPotionEffect() {
            return this.potionEffect;
        }
        
        public long getTime() {
            return this.time;
        }
        
        public boolean isPerm() {
            return this.perm;
        }
    }
}
