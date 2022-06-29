package us.zonix.practice.pvpclasses.pvpclasses;

import java.util.concurrent.ConcurrentHashMap;
import java.util.HashMap;
import us.zonix.practice.party.Party;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.List;
import java.util.concurrent.TimeUnit;
import us.zonix.practice.util.TimeUtils;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventPriority;
import java.util.Iterator;
import java.util.HashSet;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import us.zonix.practice.pvpclasses.PvPClassHandler;
import org.bukkit.entity.Arrow;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.metadata.FixedMetadataValue;
import us.zonix.practice.Practice;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.entity.Player;
import java.util.Arrays;
import org.bukkit.Material;
import us.zonix.practice.util.Pair;
import java.util.Set;
import java.util.Map;
import us.zonix.practice.pvpclasses.PvPClass;

public class ArcherClass extends PvPClass
{
    public static final int MARK_SECONDS = 10;
    private static Map<String, Long> lastSpeedUsage;
    private static Map<String, Long> lastJumpUsage;
    private static Map<String, Long> markedPlayers;
    private static Map<String, Set<Pair<String, Long>>> markedBy;
    
    public static Map<String, Long> getMarkedPlayers() {
        return ArcherClass.markedPlayers;
    }
    
    public static Map<String, Set<Pair<String, Long>>> getMarkedBy() {
        return ArcherClass.markedBy;
    }
    
    public ArcherClass() {
        super("Archer", 15, "LEATHER_", Arrays.asList(Material.SUGAR, Material.FEATHER));
    }
    
    @Override
    public void apply(final Player player) {
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 2), true);
        player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, Integer.MAX_VALUE, 0), true);
    }
    
    @Override
    public void tick(final Player player) {
        if (!player.hasPotionEffect(PotionEffectType.SPEED)) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 2));
        }
        if (!player.hasPotionEffect(PotionEffectType.DAMAGE_RESISTANCE)) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, Integer.MAX_VALUE, 0));
        }
        super.tick(player);
    }
    
    @EventHandler
    public void onEntityShootBowEvent(final EntityShootBowEvent event) {
        if (event.getEntity() instanceof Player) {
            event.getProjectile().setMetadata("ShotFromDistance", (MetadataValue)new FixedMetadataValue((Plugin)Practice.getInstance(), (Object)event.getProjectile().getLocation()));
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityArrowHit(final EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player && event.getDamager() instanceof Arrow) {
            final Arrow arrow = (Arrow)event.getDamager();
            final Player player = (Player)event.getEntity();
            if (!(arrow.getShooter() instanceof Player)) {
                return;
            }
            final Player shooter = (Player)arrow.getShooter();
            final float pullback = arrow.getMetadata("Pullback").get(0).asFloat();
            if (!PvPClassHandler.hasKitOn(shooter, this)) {
                return;
            }
            int damage = isMarked(player) ? 4 : 3;
            if (pullback < 0.5f) {
                damage = 2;
            }
            if (player.getHealth() - damage <= 0.0) {
                event.setCancelled(true);
            }
            else {
                event.setDamage(0.0);
            }
            final Location shotFrom = (Location)arrow.getMetadata("ShotFromDistance").get(0).value();
            final double distance = shotFrom.distance(player.getLocation());
            player.setHealth(Math.max(0.0, player.getHealth() - damage));
            if (PvPClassHandler.hasKitOn(player, this)) {
                shooter.sendMessage(ChatColor.YELLOW + "(" + (int)distance + ") " + ChatColor.RED + "You can't mark other archers. " + ChatColor.RED.toString() + ChatColor.BOLD + "(" + damage / 2 + " heart" + ((damage / 2 == 1) ? "" : "s") + ")");
            }
            else if (pullback >= 0.5f) {
                shooter.sendMessage(ChatColor.YELLOW + "(" + (int)distance + ") " + ChatColor.GREEN + "You have marked a player for 10 seconds. " + ChatColor.RED.toString() + ChatColor.BOLD + "(" + damage / 2 + " heart" + ((damage / 2 == 1) ? "" : "s") + ")");
                if (!isMarked(player)) {
                    player.sendMessage(ChatColor.RED.toString() + "You have been shot by an archer. (+25% damage) for 10 seconds.");
                }
                PotionEffect invis = null;
                for (final PotionEffect potionEffect : player.getActivePotionEffects()) {
                    if (potionEffect.getType().equals((Object)PotionEffectType.INVISIBILITY)) {
                        invis = potionEffect;
                        break;
                    }
                }
                if (invis != null) {
                    final PvPClass playerClass = PvPClassHandler.getPvPClass(player);
                    player.removePotionEffect(invis.getType());
                    final PotionEffect invisFinal = invis;
                    new BukkitRunnable() {
                        public void run() {
                            if (invisFinal.getDuration() > 1000000) {
                                return;
                            }
                            player.addPotionEffect(invisFinal);
                        }
                    }.runTaskLater((Plugin)Practice.getInstance(), 205L);
                }
                getMarkedPlayers().put(player.getName(), System.currentTimeMillis() + 10000L);
                getMarkedBy().putIfAbsent(shooter.getName(), new HashSet<Pair<String, Long>>());
                getMarkedBy().get(shooter.getName()).add(new Pair<String, Long>(player.getName(), System.currentTimeMillis() + 10000L));
            }
            else {
                shooter.sendMessage(ChatColor.YELLOW + "(" + (int)distance + ") " + ChatColor.RED + "The bow was not fully charged. " + ChatColor.RED.toString() + ChatColor.BOLD + "(" + damage / 2 + " heart" + ((damage / 2 == 1) ? "" : "s") + ")");
            }
        }
    }
    
    @EventHandler
    public void onEntityDamageByEntity(final EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player) {
            final Player player = (Player)event.getEntity();
            if (isMarked(player)) {
                Player damager = null;
                if (event.getDamager() instanceof Player) {
                    damager = (Player)event.getDamager();
                }
                else if (event.getDamager() instanceof Projectile && ((Projectile)event.getDamager()).getShooter() instanceof Player) {
                    damager = (Player)((Projectile)event.getDamager()).getShooter();
                }
                if (damager != null && !this.canUseMark(damager, player)) {
                    return;
                }
                event.setDamage(event.getDamage() * 1.25);
            }
        }
    }
    
    @EventHandler
    public void onEntityShootBow(final EntityShootBowEvent event) {
        event.getProjectile().setMetadata("Pullback", (MetadataValue)new FixedMetadataValue((Plugin)Practice.getInstance(), (Object)event.getForce()));
    }
    
    @Override
    public boolean itemConsumed(final Player player, final Material material) {
        if (material == Material.SUGAR) {
            if (ArcherClass.lastSpeedUsage.containsKey(player.getName()) && ArcherClass.lastSpeedUsage.get(player.getName()) > System.currentTimeMillis()) {
                final long millisLeft = ArcherClass.lastSpeedUsage.get(player.getName()) - System.currentTimeMillis();
                final String msg = TimeUtils.formatIntoDetailedString((int)millisLeft / 1000);
                player.sendMessage(ChatColor.RED + "You can't use this for another §f" + msg + "§c.");
                return false;
            }
            ArcherClass.lastSpeedUsage.put(player.getName(), System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(30L));
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 200, 3), true);
            return true;
        }
        else {
            if (ArcherClass.lastJumpUsage.containsKey(player.getName()) && ArcherClass.lastJumpUsage.get(player.getName()) > System.currentTimeMillis()) {
                final long millisLeft = ArcherClass.lastJumpUsage.get(player.getName()) - System.currentTimeMillis();
                final String msg = TimeUtils.formatIntoDetailedString((int)millisLeft / 1000);
                player.sendMessage(ChatColor.RED + "You can't use this for another §f" + msg + "§c.");
                return false;
            }
            ArcherClass.lastJumpUsage.put(player.getName(), System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(1L));
            player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 100, 4));
            return false;
        }
    }
    
    public static boolean isMarked(final Player player) {
        return getMarkedPlayers().containsKey(player.getName()) && getMarkedPlayers().get(player.getName()) > System.currentTimeMillis();
    }
    
    private boolean canUseMark(final Player player, final Player victim) {
        if (Practice.getInstance().getPartyManager().getParty(player.getUniqueId()) != null) {
            final Party team = Practice.getInstance().getPartyManager().getParty(player.getUniqueId());
            int amount = 0;
            for (final Player member : team.members().collect((Collector<? super Player, ?, List<? super Player>>)Collectors.toList())) {
                ++amount;
                if (PvPClassHandler.hasKitOn(member, this) && amount > 3) {
                    break;
                }
            }
            if (amount > 3) {
                player.sendMessage(ChatColor.RED + "Your team has too many archers. Archer mark was not applied.");
                return false;
            }
        }
        if (ArcherClass.markedBy.containsKey(player.getName())) {
            for (final Pair<String, Long> pair : ArcherClass.markedBy.get(player.getName())) {
                if (victim.getName().equals(pair.first) && pair.second > System.currentTimeMillis()) {
                    return false;
                }
            }
            return true;
        }
        return true;
    }
    
    static {
        ArcherClass.lastSpeedUsage = new HashMap<String, Long>();
        ArcherClass.lastJumpUsage = new HashMap<String, Long>();
        ArcherClass.markedPlayers = new ConcurrentHashMap<String, Long>();
        ArcherClass.markedBy = new HashMap<String, Set<Pair<String, Long>>>();
    }
}
