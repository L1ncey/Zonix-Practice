package us.zonix.practice.pvpclasses.pvpclasses;

import java.util.concurrent.ConcurrentHashMap;
import com.google.common.collect.ImmutableSet;
import us.zonix.practice.match.Match;
import us.zonix.practice.party.Party;
import us.zonix.practice.match.MatchTeam;
import org.bukkit.entity.Entity;
import java.util.ArrayList;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.ItemStack;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.Plugin;
import java.util.Iterator;
import us.zonix.practice.pvpclasses.PvPClassHandler;
import org.bukkit.entity.Player;
import us.zonix.practice.Practice;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.potion.PotionEffect;
import java.util.HashMap;
import java.util.List;
import us.zonix.practice.pvpclasses.pvpclasses.bard.BardEffect;
import org.bukkit.Material;
import java.util.Map;
import org.bukkit.potion.PotionEffectType;
import java.util.Set;
import org.bukkit.event.Listener;
import us.zonix.practice.pvpclasses.PvPClass;

public class BardClass extends PvPClass implements Listener
{
    public static final Set<PotionEffectType> DEBUFFS;
    public final Map<Material, BardEffect> BARD_CLICK_EFFECTS;
    public final Map<Material, BardEffect> BARD_PASSIVE_EFFECTS;
    private static Map<String, Long> lastEffectUsage;
    private static Map<String, Float> energy;
    public static final int BARD_RANGE = 20;
    public static final int EFFECT_COOLDOWN = 10000;
    public static final float MAX_ENERGY = 100.0f;
    public static final float ENERGY_REGEN_PER_SECOND = 1.0f;
    
    public static Map<String, Long> getLastEffectUsage() {
        return BardClass.lastEffectUsage;
    }
    
    public static Map<String, Float> getEnergy() {
        return BardClass.energy;
    }
    
    public BardClass() {
        super("Bard", 15, "GOLD_", null);
        this.BARD_CLICK_EFFECTS = new HashMap<Material, BardEffect>();
        this.BARD_PASSIVE_EFFECTS = new HashMap<Material, BardEffect>();
        this.BARD_CLICK_EFFECTS.put(Material.BLAZE_POWDER, BardEffect.fromPotionAndEnergy(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 100, 1), 45));
        this.BARD_CLICK_EFFECTS.put(Material.SUGAR, BardEffect.fromPotionAndEnergy(new PotionEffect(PotionEffectType.SPEED, 120, 2), 20));
        this.BARD_CLICK_EFFECTS.put(Material.FEATHER, BardEffect.fromPotionAndEnergy(new PotionEffect(PotionEffectType.JUMP, 100, 6), 25));
        this.BARD_CLICK_EFFECTS.put(Material.IRON_INGOT, BardEffect.fromPotionAndEnergy(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 100, 2), 40));
        this.BARD_CLICK_EFFECTS.put(Material.GHAST_TEAR, BardEffect.fromPotionAndEnergy(new PotionEffect(PotionEffectType.REGENERATION, 100, 2), 40));
        this.BARD_CLICK_EFFECTS.put(Material.MAGMA_CREAM, BardEffect.fromPotionAndEnergy(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 900, 0), 40));
        this.BARD_CLICK_EFFECTS.put(Material.WHEAT, BardEffect.fromEnergy(25));
        this.BARD_CLICK_EFFECTS.put(Material.SPIDER_EYE, BardEffect.fromPotionAndEnergy(new PotionEffect(PotionEffectType.WITHER, 100, 1), 35));
        this.BARD_PASSIVE_EFFECTS.put(Material.BLAZE_POWDER, BardEffect.fromPotion(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 120, 0)));
        this.BARD_PASSIVE_EFFECTS.put(Material.SUGAR, BardEffect.fromPotion(new PotionEffect(PotionEffectType.SPEED, 120, 1)));
        this.BARD_PASSIVE_EFFECTS.put(Material.FEATHER, BardEffect.fromPotion(new PotionEffect(PotionEffectType.JUMP, 120, 1)));
        this.BARD_PASSIVE_EFFECTS.put(Material.IRON_INGOT, BardEffect.fromPotion(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 120, 0)));
        this.BARD_PASSIVE_EFFECTS.put(Material.GHAST_TEAR, BardEffect.fromPotion(new PotionEffect(PotionEffectType.REGENERATION, 120, 0)));
        this.BARD_PASSIVE_EFFECTS.put(Material.MAGMA_CREAM, BardEffect.fromPotion(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 120, 0)));
        new BukkitRunnable() {
            public void run() {
                for (final Player player : Practice.getInstance().getServer().getOnlinePlayers()) {
                    if (!PvPClassHandler.hasKitOn(player, BardClass.this)) {
                        continue;
                    }
                    if (BardClass.energy.containsKey(player.getName())) {
                        if (BardClass.energy.get(player.getName()) == 100.0f) {
                            continue;
                        }
                        BardClass.energy.put(player.getName(), Math.min(100.0f, BardClass.energy.get(player.getName()) + 1.0f));
                    }
                    else {
                        BardClass.energy.put(player.getName(), 0.0f);
                    }
                }
            }
        }.runTaskTimer((Plugin)Practice.getInstance(), 15L, 20L);
    }
    
    @Override
    public void apply(final Player player) {
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 1));
        player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, Integer.MAX_VALUE, 1), true);
        player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, Integer.MAX_VALUE, 0), true);
    }
    
    @Override
    public void tick(final Player player) {
        if (!player.hasPotionEffect(PotionEffectType.SPEED)) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 1));
        }
        if (!player.hasPotionEffect(PotionEffectType.DAMAGE_RESISTANCE)) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, Integer.MAX_VALUE, 1));
        }
        if (!player.hasPotionEffect(PotionEffectType.REGENERATION)) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, Integer.MAX_VALUE, 0));
        }
        if (player.getItemInHand() != null && this.BARD_PASSIVE_EFFECTS.containsKey(player.getItemInHand().getType())) {
            if (player.getItemInHand().getType() == Material.FERMENTED_SPIDER_EYE && getLastEffectUsage().containsKey(player.getName()) && getLastEffectUsage().get(player.getName()) > System.currentTimeMillis()) {
                return;
            }
            this.giveBardEffect(player, this.BARD_PASSIVE_EFFECTS.get(player.getItemInHand().getType()), true, false);
        }
        super.tick(player);
    }
    
    @Override
    public void remove(final Player player) {
        BardClass.energy.remove(player.getName());
        for (final BardEffect bardEffect : this.BARD_CLICK_EFFECTS.values()) {
            bardEffect.getLastMessageSent().remove(player.getName());
        }
        for (final BardEffect bardEffect : this.BARD_CLICK_EFFECTS.values()) {
            bardEffect.getLastMessageSent().remove(player.getName());
        }
    }
    
    @EventHandler
    public void onPlayerInteract(final PlayerInteractEvent event) {
        if (!event.getAction().name().contains("RIGHT_") || !event.hasItem() || !this.BARD_CLICK_EFFECTS.containsKey(event.getItem().getType()) || !PvPClassHandler.hasKitOn(event.getPlayer(), this) || !BardClass.energy.containsKey(event.getPlayer().getName())) {
            return;
        }
        if (getLastEffectUsage().containsKey(event.getPlayer().getName()) && getLastEffectUsage().get(event.getPlayer().getName()) > System.currentTimeMillis() && event.getPlayer().getGameMode() != GameMode.CREATIVE) {
            final long millisLeft = getLastEffectUsage().get(event.getPlayer().getName()) - System.currentTimeMillis();
            final double value = millisLeft / 1000.0;
            final double sec = Math.round(10.0 * value) / 10.0;
            event.getPlayer().sendMessage(ChatColor.RED + "You can't use this for another " + ChatColor.BOLD + sec + ChatColor.RED + " seconds!");
            return;
        }
        final BardEffect bardEffect = this.BARD_CLICK_EFFECTS.get(event.getItem().getType());
        if (bardEffect.getEnergy() > BardClass.energy.get(event.getPlayer().getName())) {
            event.getPlayer().sendMessage(ChatColor.RED + "You do not have enough energy to do this! You need " + bardEffect.getEnergy() + " energy.");
            return;
        }
        BardClass.energy.put(event.getPlayer().getName(), BardClass.energy.get(event.getPlayer().getName()) - bardEffect.getEnergy());
        final boolean negative = bardEffect.getPotionEffect() != null && BardClass.DEBUFFS.contains(bardEffect.getPotionEffect().getType());
        getLastEffectUsage().put(event.getPlayer().getName(), System.currentTimeMillis() + 10000L);
        this.giveBardEffect(event.getPlayer(), bardEffect, !negative, true);
        if (event.getPlayer().getItemInHand().getAmount() == 1) {
            event.getPlayer().setItemInHand(new ItemStack(Material.AIR));
            event.getPlayer().updateInventory();
        }
        else {
            event.getPlayer().getItemInHand().setAmount(event.getPlayer().getItemInHand().getAmount() - 1);
        }
    }
    
    public void giveBardEffect(final Player source, final BardEffect bardEffect, final boolean friendly, final boolean persistOldValues) {
        for (final Player player : this.getNearbyPlayers(source, friendly)) {
            if (PvPClassHandler.hasKitOn(player, this) && bardEffect.getPotionEffect() != null && bardEffect.getPotionEffect().getType().equals((Object)PotionEffectType.INCREASE_DAMAGE)) {
                continue;
            }
            if (bardEffect.getPotionEffect() != null) {
                PvPClass.smartAddPotion(player, bardEffect.getPotionEffect(), persistOldValues, this);
            }
            else {
                final Material material = source.getItemInHand().getType();
                this.giveCustomBardEffect(player, material);
            }
        }
    }
    
    public void giveCustomBardEffect(final Player player, final Material material) {
        switch (material) {
            case WHEAT: {
                for (final Player nearbyPlayer : this.getNearbyPlayers(player, true)) {
                    nearbyPlayer.setFoodLevel(20);
                    nearbyPlayer.setSaturation(10.0f);
                }
            }
            case FERMENTED_SPIDER_EYE: {}
            default: {}
        }
    }
    
    public List<Player> getNearbyPlayers(final Player player, final boolean friendly) {
        final List<Player> valid = new ArrayList<Player>();
        final Party sourceTeam = Practice.getInstance().getPartyManager().getParty(player.getUniqueId());
        final Match match = Practice.getInstance().getMatchManager().getMatch(player.getUniqueId());
        if (match == null) {
            return valid;
        }
        for (final Entity entity : player.getNearbyEntities(20.0, 10.0, 20.0)) {
            if (entity instanceof Player) {
                final Player nearbyPlayer = (Player)entity;
                if (sourceTeam == null) {
                    if (friendly) {
                        continue;
                    }
                    valid.add(nearbyPlayer);
                }
                else {
                    final boolean isFriendly = sourceTeam.getMembers().contains(nearbyPlayer.getUniqueId());
                    final boolean isOtherTeam = match.getOtherTeam(player).get(0).getPlayers().contains(nearbyPlayer.getUniqueId());
                    if (friendly && isFriendly) {
                        valid.add(nearbyPlayer);
                    }
                    else {
                        if (friendly || isFriendly || !isOtherTeam) {
                            continue;
                        }
                        valid.add(nearbyPlayer);
                    }
                }
            }
        }
        valid.add(player);
        return valid;
    }
    
    static {
        DEBUFFS = (Set)ImmutableSet.of((Object)PotionEffectType.POISON, (Object)PotionEffectType.SLOW, (Object)PotionEffectType.WEAKNESS, (Object)PotionEffectType.HARM, (Object)PotionEffectType.WITHER);
        BardClass.lastEffectUsage = new ConcurrentHashMap<String, Long>();
        BardClass.energy = new ConcurrentHashMap<String, Float>();
    }
}
