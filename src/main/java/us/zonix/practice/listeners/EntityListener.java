package us.zonix.practice.listeners;

import org.bukkit.entity.Projectile;
import org.bukkit.entity.EnderPearl;
import java.util.Iterator;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.ChatColor;
import org.bukkit.entity.Arrow;
import us.zonix.practice.events.redrover.RedroverPlayer;
import us.zonix.practice.events.redrover.RedroverEvent;
import us.zonix.practice.events.tnttag.TNTTagPlayer;
import us.zonix.practice.events.lights.LightsEvent;
import org.bukkit.GameMode;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import us.zonix.practice.events.PracticeEvent;
import us.zonix.practice.match.Match;
import us.zonix.practice.player.PlayerData;
import us.zonix.practice.player.PlayerState;
import us.zonix.practice.events.tnttag.TNTTagEvent;
import us.zonix.practice.events.woolmixup.WoolMixUpEvent;
import us.zonix.practice.events.waterdrop.WaterDropEvent;
import us.zonix.practice.events.parkour.ParkourEvent;
import us.zonix.practice.events.oitc.OITCPlayer;
import us.zonix.practice.events.oitc.OITCEvent;
import us.zonix.practice.events.sumo.SumoPlayer;
import us.zonix.practice.events.sumo.SumoEvent;
import us.zonix.practice.CustomLocation;
import us.zonix.practice.events.EventState;
import us.zonix.practice.match.MatchState;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.CreatureSpawnEvent;
import us.zonix.practice.Practice;
import org.bukkit.event.Listener;

public class EntityListener implements Listener
{
    private final Practice plugin;
    
    public EntityListener() {
        this.plugin = Practice.getInstance();
    }
    
    @EventHandler
    public void onCreateSpawn(final CreatureSpawnEvent e) {
        e.setCancelled(true);
    }
    
    @EventHandler
    public void onEntityDamage(final EntityDamageEvent e) {
        if (e.getEntity() instanceof Player) {
            final Player player = (Player)e.getEntity();
            final PlayerData playerData = this.plugin.getPlayerManager().getPlayerData(player.getUniqueId());
            switch (playerData.getPlayerState()) {
                case FIGHTING: {
                    final Match match = this.plugin.getMatchManager().getMatch(playerData);
                    if (match.getMatchState() != MatchState.FIGHTING) {
                        e.setCancelled(true);
                    }
                    if (e.getCause() == EntityDamageEvent.DamageCause.VOID) {
                        this.plugin.getMatchManager().removeFighter(player, playerData, true);
                    }
                    if (match.getKit().isParkour()) {
                        e.setCancelled(true);
                        break;
                    }
                    break;
                }
                case EVENT: {
                    final PracticeEvent event = this.plugin.getEventManager().getEventPlaying(player);
                    if (event == null) {
                        break;
                    }
                    if (event.getState() == EventState.WAITING) {
                        e.setCancelled(true);
                        if (e.getCause() == EntityDamageEvent.DamageCause.VOID) {
                            final CustomLocation location = (CustomLocation)event.getSpawnLocations().get(0);
                            if (location != null) {
                                player.teleport(location.toBukkitLocation());
                            }
                            break;
                        }
                        break;
                    }
                    else {
                        if (event instanceof SumoEvent) {
                            final SumoEvent sumoEvent = (SumoEvent)event;
                            final SumoPlayer sumoPlayer = sumoEvent.getPlayer(player);
                            if (e.getCause() == EntityDamageEvent.DamageCause.FALL) {
                                e.setCancelled(true);
                            }
                            else if (sumoPlayer != null && sumoPlayer.getState() == SumoPlayer.SumoState.FIGHTING) {
                                e.setCancelled(false);
                            }
                            break;
                        }
                        if (event instanceof OITCEvent) {
                            final OITCEvent oitcEvent = (OITCEvent)event;
                            final OITCPlayer oitcPlayer = oitcEvent.getPlayer(player);
                            if (oitcPlayer != null && oitcPlayer.getState() == OITCPlayer.OITCState.FIGHTING && e.getCause() != EntityDamageEvent.DamageCause.FALL) {
                                e.setCancelled(false);
                            }
                            else {
                                e.setCancelled(true);
                            }
                            break;
                        }
                        if (event instanceof ParkourEvent) {
                            e.setCancelled(true);
                            break;
                        }
                        if (event instanceof WaterDropEvent) {
                            if (e.getCause() == EntityDamageEvent.DamageCause.FALL) {
                                final WaterDropEvent waterDropEvent = (WaterDropEvent)event;
                                waterDropEvent.onDeath().accept(player);
                            }
                            else if (e.getCause() == EntityDamageEvent.DamageCause.VOID) {
                                final WaterDropEvent waterDropEvent = (WaterDropEvent)event;
                                waterDropEvent.teleportToSpawn(player);
                            }
                            e.setCancelled(true);
                            break;
                        }
                        if (event instanceof WoolMixUpEvent) {
                            if (e.getCause() == EntityDamageEvent.DamageCause.VOID) {
                                final WoolMixUpEvent woolMixUpEvent = (WoolMixUpEvent)event;
                                woolMixUpEvent.onDeath().accept(player);
                            }
                            if (e.getCause() == EntityDamageEvent.DamageCause.FALL) {
                                final WoolMixUpEvent woolMixUpEvent = (WoolMixUpEvent)event;
                                woolMixUpEvent.onDeath().accept(player);
                            }
                            e.setCancelled(true);
                            break;
                        }
                        if (event instanceof TNTTagEvent && e.getCause() == EntityDamageEvent.DamageCause.FALL) {
                            e.setCancelled(true);
                            break;
                        }
                        break;
                    }
                    break;
                }
                case FFA: {
                    e.setCancelled(false);
                    break;
                }
                default: {
                    if (e.getCause() == EntityDamageEvent.DamageCause.VOID && (playerData.getPlayerState() != PlayerState.FFA || playerData.getPlayerState() != PlayerState.EVENT)) {
                        e.getEntity().teleport(this.plugin.getSpawnManager().getSpawnLocation().toBukkitLocation());
                    }
                    e.setCancelled(true);
                    break;
                }
            }
        }
    }
    
    @EventHandler
    public void onEntityDamageByEntity(final EntityDamageByEntityEvent e) {
        if (!(e.getEntity() instanceof Player)) {
            return;
        }
        final Player entity = (Player)e.getEntity();
        final Player damager = this.isPlayerDamager(e);
        if (damager == null) {
            return;
        }
        final PlayerData entityData = this.plugin.getPlayerManager().getPlayerData(entity.getUniqueId());
        final PlayerData damagerData = this.plugin.getPlayerManager().getPlayerData(damager.getUniqueId());
        if (entityData == null || damagerData == null) {
            return;
        }
        final boolean isEventEntity = this.plugin.getEventManager().getEventPlaying(entity) != null;
        final boolean isEventDamager = this.plugin.getEventManager().getEventPlaying(damager) != null;
        final PracticeEvent eventDamager = this.plugin.getEventManager().getEventPlaying(damager);
        final PracticeEvent eventEntity = this.plugin.getEventManager().getEventPlaying(entity);
        if (damagerData.getPlayerState() == PlayerState.SPECTATING || this.plugin.getEventManager().getSpectators().containsKey(damager.getUniqueId())) {
            e.setCancelled(true);
            return;
        }
        if (damagerData.getPlayerState() == PlayerState.FFA && entityData.getPlayerState() == PlayerState.FFA) {
            e.setCancelled(false);
            return;
        }
        if ((!entity.canSee(damager) && damager.canSee(entity)) || damager.getGameMode() == GameMode.SPECTATOR) {
            e.setCancelled(true);
            return;
        }
        if ((isEventDamager && eventDamager instanceof ParkourEvent) || (isEventEntity && eventEntity instanceof ParkourEvent)) {
            e.setCancelled(true);
            return;
        }
        if ((isEventDamager && eventDamager instanceof WaterDropEvent) || (isEventEntity && eventEntity instanceof WaterDropEvent)) {
            e.setCancelled(true);
            return;
        }
        if ((isEventDamager && eventDamager instanceof WoolMixUpEvent) || (isEventEntity && eventEntity instanceof WoolMixUpEvent)) {
            e.setCancelled(true);
            return;
        }
        if ((isEventDamager && eventDamager instanceof LightsEvent) || (isEventEntity && eventEntity instanceof LightsEvent)) {
            e.setCancelled(true);
            return;
        }
        if ((isEventDamager && eventDamager instanceof TNTTagEvent) || (isEventEntity && eventEntity instanceof TNTTagEvent)) {
            if (isEventEntity && isEventDamager && eventEntity instanceof TNTTagEvent && eventDamager instanceof TNTTagEvent) {
                final TNTTagEvent tntTagEvent = (TNTTagEvent)eventDamager;
                final TNTTagPlayer tnt = tntTagEvent.getPlayer(damager);
                final TNTTagPlayer other = tntTagEvent.getPlayer(entity);
                if (tntTagEvent.getTntTagState() != TNTTagEvent.TNTTagState.RUNNING) {
                    e.setCancelled(true);
                }
                if (tntTagEvent.getTntTagState() == TNTTagEvent.TNTTagState.RUNNING && tnt.isTagged() && !other.isTagged()) {
                    tnt.setTagged(false);
                    tnt.update();
                    other.setTagged(true);
                    other.update();
                }
                e.setDamage(0.0);
            }
            return;
        }
        if ((isEventDamager && eventDamager instanceof RedroverEvent && ((RedroverEvent)eventDamager).getPlayer(damager).getState() != RedroverPlayer.RedroverState.FIGHTING) || (isEventEntity && eventDamager instanceof RedroverEvent && ((RedroverEvent)eventEntity).getPlayer(entity).getState() != RedroverPlayer.RedroverState.FIGHTING) || (!isEventDamager && damagerData.getPlayerState() != PlayerState.FIGHTING) || (!isEventEntity && entityData.getPlayerState() != PlayerState.FIGHTING)) {
            e.setCancelled(true);
            return;
        }
        if ((isEventDamager && eventDamager instanceof SumoEvent && ((SumoEvent)eventDamager).getPlayer(damager).getState() != SumoPlayer.SumoState.FIGHTING) || (isEventEntity && eventDamager instanceof SumoEvent && ((SumoEvent)eventEntity).getPlayer(entity).getState() != SumoPlayer.SumoState.FIGHTING) || (!isEventDamager && damagerData.getPlayerState() != PlayerState.FIGHTING) || (!isEventEntity && entityData.getPlayerState() != PlayerState.FIGHTING)) {
            e.setCancelled(true);
            return;
        }
        if ((isEventDamager && eventDamager instanceof OITCEvent) || (isEventEntity && eventEntity instanceof OITCEvent) || (!isEventDamager && damagerData.getPlayerState() != PlayerState.FIGHTING) || (!isEventEntity && entityData.getPlayerState() != PlayerState.FIGHTING)) {
            if (isEventEntity && isEventDamager && eventEntity instanceof OITCEvent && eventDamager instanceof OITCEvent) {
                final OITCEvent oitcEvent = (OITCEvent)eventDamager;
                final OITCPlayer oitcKiller = oitcEvent.getPlayer(damager);
                final OITCPlayer oitcPlayer = oitcEvent.getPlayer(entity);
                if (oitcKiller.getState() != OITCPlayer.OITCState.FIGHTING || oitcPlayer.getState() != OITCPlayer.OITCState.FIGHTING) {
                    e.setCancelled(true);
                    return;
                }
                if (e.getDamager() instanceof Arrow) {
                    final Arrow arrow = (Arrow)e.getDamager();
                    if (arrow.getShooter() instanceof Player && damager != entity) {
                        oitcPlayer.setLastKiller(oitcKiller);
                        e.setDamage(0.0);
                        eventEntity.onDeath().accept(entity);
                    }
                }
            }
            return;
        }
        if ((entityData.getPlayerState() == PlayerState.EVENT && eventEntity instanceof SumoEvent) || (damagerData.getPlayerState() == PlayerState.EVENT && eventDamager instanceof SumoEvent)) {
            e.setDamage(0.0);
            return;
        }
        if ((entityData.getPlayerState() == PlayerState.EVENT && eventEntity instanceof RedroverEvent) || (damagerData.getPlayerState() == PlayerState.EVENT && eventDamager instanceof RedroverEvent)) {
            return;
        }
        final Match match = this.plugin.getMatchManager().getMatch(entityData);
        if (match == null) {
            e.setDamage(0.0);
            return;
        }
        if (damagerData.getTeamID() == entityData.getTeamID() && !match.isFFA()) {
            e.setCancelled(true);
            return;
        }
        if (match.getKit().isParkour()) {
            e.setCancelled(true);
            return;
        }
        if (match.getKit().isSpleef() || match.getKit().isSumo()) {
            e.setDamage(0.0);
        }
        if (e.getDamager() instanceof Player) {
            damagerData.setCombo(damagerData.getCombo() + 1);
            damagerData.setHits(damagerData.getHits() + 1);
            if (damagerData.getCombo() > damagerData.getLongestCombo()) {
                damagerData.setLongestCombo(damagerData.getCombo());
            }
            entityData.setCombo(0);
            if (match.getKit().isSpleef()) {
                e.setCancelled(true);
            }
        }
        else if (e.getDamager() instanceof Arrow) {
            final Arrow arrow2 = (Arrow)e.getDamager();
            if (arrow2.getShooter() instanceof Player) {
                final Player shooter = (Player)arrow2.getShooter();
                if (!entity.getName().equals(shooter.getName())) {
                    final double health = Math.ceil(entity.getHealth() - e.getFinalDamage()) / 2.0;
                    if (health > 0.0) {
                        shooter.sendMessage(ChatColor.YELLOW + "[*] " + ChatColor.GREEN + entity.getName() + " has been shot." + ChatColor.DARK_GRAY + " (" + ChatColor.RED + health + "\u2764" + ChatColor.DARK_GRAY + ")");
                    }
                }
            }
        }
    }
    
    @EventHandler
    public void onClick(final InventoryClickEvent event) {
        final HumanEntity player = event.getWhoClicked();
        if (player instanceof Player) {
            final PracticeEvent tnt = Practice.getInstance().getEventManager().getEventPlaying((Player)player);
            if (tnt instanceof TNTTagEvent) {
                event.setCancelled(true);
            }
        }
    }
    
    @EventHandler
    public void onPotionSplash(final PotionSplashEvent e) {
        if (!(e.getEntity().getShooter() instanceof Player)) {
            return;
        }
        for (final PotionEffect effect : e.getEntity().getEffects()) {
            if (effect.getType().equals((Object)PotionEffectType.HEAL)) {
                final Player shooter = (Player)e.getEntity().getShooter();
                if (e.getIntensity((LivingEntity)shooter) <= 0.5) {
                    final PlayerData shooterData = this.plugin.getPlayerManager().getPlayerData(shooter.getUniqueId());
                    if (shooterData != null) {
                        shooterData.setMissedPots(shooterData.getMissedPots() + 1);
                    }
                    break;
                }
                break;
            }
        }
    }
    
    private Player isPlayerDamager(final EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof EnderPearl) {
            return null;
        }
        if (event.getDamager() instanceof Player) {
            return (Player)event.getDamager();
        }
        if (event.getDamager() instanceof Projectile && ((Projectile)event.getDamager()).getShooter() instanceof Player) {
            return (Player)((Projectile)event.getDamager()).getShooter();
        }
        return null;
    }
}
