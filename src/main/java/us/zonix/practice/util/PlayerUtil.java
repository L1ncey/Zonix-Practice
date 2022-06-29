package us.zonix.practice.util;

import org.bukkit.Bukkit;
import org.bukkit.event.entity.PlayerDeathEvent;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.entity.Firework;
import org.bukkit.Location;
import org.bukkit.FireworkEffect;
import java.util.Iterator;
import java.util.Set;
import java.util.stream.Stream;
import java.util.function.Consumer;
import java.util.Objects;
import java.util.function.Function;
import org.bukkit.potion.PotionEffect;
import org.bukkit.GameMode;
import org.bukkit.plugin.Plugin;
import us.zonix.practice.Practice;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public final class PlayerUtil
{
    public static void setFirstSlotOfType(final Player player, final Material type, final ItemStack itemStack) {
        for (int i = 0; i < player.getInventory().getContents().length; ++i) {
            final ItemStack itemStack2 = player.getInventory().getContents()[i];
            if (itemStack2 == null || itemStack2.getType() == type || itemStack2.getType() == Material.AIR) {
                player.getInventory().setItem(i, itemStack);
                break;
            }
        }
    }
    
    public static int getPing(final Player player) {
        final int ping = ((CraftPlayer)player).getHandle().ping;
        return ping;
    }
    
    public static void clearPlayer(final Player player) {
        player.setHealth(20.0);
        player.setFoodLevel(20);
        player.setSaturation(12.8f);
        player.setMaximumNoDamageTicks(20);
        new BukkitRunnable() {
            public void run() {
                player.setFireTicks(0);
            }
        }.runTaskLater((Plugin)Practice.getInstance(), 1L);
        player.setFallDistance(0.0f);
        player.setLevel(0);
        player.setExp(0.0f);
        player.setWalkSpeed(0.2f);
        player.getInventory().setHeldItemSlot(0);
        player.setAllowFlight(false);
        player.getInventory().clear();
        player.getInventory().setArmorContents((ItemStack[])null);
        player.closeInventory();
        player.setGameMode(GameMode.SURVIVAL);
        ((CraftPlayer)player).getHandle().getDataWatcher().watch(9, (Object)(byte)0);
        ((CraftPlayer)player).getHandle().setFakingDeath(false);
        final Stream map = player.getActivePotionEffects().stream().map(PotionEffect::getType);
        Objects.requireNonNull(player);
        map.forEach(player::removePotionEffect);
        player.updateInventory();
    }
    
    public static void sendMessage(final String message, final Player... players) {
        for (final Player player : players) {
            player.sendMessage(message);
        }
    }
    
    public static void sendMessage(final String message, final Set<Player> players) {
        for (final Player player : players) {
            player.sendMessage(message);
        }
    }
    
    public static void sendFirework(final FireworkEffect effect, final Location location) {
        final Firework f = (Firework)location.getWorld().spawn(location, (Class)Firework.class);
        final FireworkMeta fm = f.getFireworkMeta();
        fm.addEffect(effect);
        f.setFireworkMeta(fm);
        try {
            final Class<?> entityFireworkClass = getClass("net.minecraft.server.", "EntityFireworks");
            final Class<?> craftFireworkClass = getClass("org.bukkit.craftbukkit.", "entity.CraftFirework");
            final Object firework = craftFireworkClass.cast(f);
            final Method handle = firework.getClass().getMethod("getHandle", (Class<?>[])new Class[0]);
            final Object entityFirework = handle.invoke(firework, new Object[0]);
            final Field expectedLifespan = entityFireworkClass.getDeclaredField("expectedLifespan");
            final Field ticksFlown = entityFireworkClass.getDeclaredField("ticksFlown");
            ticksFlown.setAccessible(true);
            ticksFlown.setInt(entityFirework, expectedLifespan.getInt(entityFirework) - 1);
            ticksFlown.setAccessible(false);
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    public static void respawnPlayer(final PlayerDeathEvent event) {
        new BukkitRunnable() {
            public void run() {
                try {
                    final Object nmsPlayer = event.getEntity().getClass().getMethod("getHandle", (Class<?>[])new Class[0]).invoke(event.getEntity(), new Object[0]);
                    final Object con = nmsPlayer.getClass().getDeclaredField("playerConnection").get(nmsPlayer);
                    final Class<?> EntityPlayer = Class.forName(nmsPlayer.getClass().getPackage().getName() + ".EntityPlayer");
                    final Field minecraftServer = con.getClass().getDeclaredField("minecraftServer");
                    minecraftServer.setAccessible(true);
                    final Object mcserver = minecraftServer.get(con);
                    final Object playerlist = mcserver.getClass().getDeclaredMethod("getPlayerList", (Class<?>[])new Class[0]).invoke(mcserver, new Object[0]);
                    final Method moveToWorld = playerlist.getClass().getMethod("moveToWorld", EntityPlayer, Integer.TYPE, Boolean.TYPE);
                    moveToWorld.invoke(playerlist, nmsPlayer, 0, false);
                }
                catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }.runTaskLater((Plugin)Practice.getInstance(), 2L);
    }
    
    private static Class<?> getClass(final String prefix, final String nmsClassString) throws ClassNotFoundException {
        final String version = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3] + ".";
        final String name = prefix + version + nmsClassString;
        final Class<?> nmsClass = Class.forName(name);
        return nmsClass;
    }
}
