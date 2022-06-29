package us.zonix.practice.ffa;

import java.util.HashSet;
import java.util.HashMap;
import java.util.Iterator;
import us.zonix.practice.player.PlayerData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.ChatColor;
import us.zonix.practice.util.PlayerUtil;
import us.zonix.practice.player.PlayerState;
import us.zonix.practice.ffa.killstreak.impl.GodAppleKillStreak;
import us.zonix.practice.ffa.killstreak.impl.DebuffKillStreak;
import us.zonix.practice.ffa.killstreak.impl.GappleKillStreak;
import org.bukkit.entity.Player;
import us.zonix.practice.kit.Kit;
import us.zonix.practice.CustomLocation;
import us.zonix.practice.Practice;
import us.zonix.practice.ffa.killstreak.KillStreak;
import java.util.Set;
import java.util.UUID;
import org.bukkit.entity.Item;
import java.util.Map;

public class FFAManager
{
    private final Map<Item, Long> itemTracker;
    private final Map<UUID, Integer> killStreakTracker;
    private final Set<KillStreak> killStreaks;
    private final Practice plugin;
    private final CustomLocation spawnPoint;
    private final Kit kit;
    
    public void addPlayer(final Player player) {
        if (this.killStreaks.isEmpty()) {
            this.killStreaks.add(new GappleKillStreak());
            this.killStreaks.add(new DebuffKillStreak());
            this.killStreaks.add(new GodAppleKillStreak());
        }
        final PlayerData playerData = this.plugin.getPlayerManager().getPlayerData(player.getUniqueId());
        playerData.setPlayerState(PlayerState.FFA);
        player.getInventory().setHeldItemSlot(0);
        player.teleport(this.spawnPoint.toBukkitLocation());
        player.setFlying(false);
        PlayerUtil.clearPlayer(player);
        player.sendMessage(ChatColor.GREEN + "You have been sent to the FFA arena.");
        this.kit.applyToPlayer(player);
        player.getActivePotionEffects().forEach(potionEffect -> player.removePotionEffect(potionEffect.getType()));
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 1));
        for (final PlayerData data : this.plugin.getPlayerManager().getAllData()) {
            final Player player2 = this.plugin.getServer().getPlayer(data.getUniqueId());
            if (data.getPlayerState() == PlayerState.FFA) {
                player.showPlayer(player2);
                player2.showPlayer(player);
            }
            else {
                player.hidePlayer(player2);
                player2.hidePlayer(player);
            }
        }
    }
    
    public void removePlayer(final Player player) {
        for (final PlayerData data : this.plugin.getPlayerManager().getAllData()) {
            final Player player2 = this.plugin.getServer().getPlayer(data.getUniqueId());
            if (data.getPlayerState() == PlayerState.FFA) {
                player.hidePlayer(player2);
                player2.hidePlayer(player);
            }
        }
        this.killStreakTracker.remove(player.getUniqueId());
        this.plugin.getPlayerManager().sendToSpawnAndReset(player);
    }
    
    public int getTotalPlaying() {
        int count = 0;
        for (final Player online : this.plugin.getServer().getOnlinePlayers()) {
            if (this.plugin.getPlayerManager().getPlayerData(online.getUniqueId()).getPlayerState() == PlayerState.FFA) {
                ++count;
            }
        }
        return count;
    }
    
    public FFAManager(final CustomLocation spawnPoint, final Kit kit) {
        this.itemTracker = new HashMap<Item, Long>();
        this.killStreakTracker = new HashMap<UUID, Integer>();
        this.killStreaks = new HashSet<KillStreak>();
        this.plugin = Practice.getInstance();
        this.spawnPoint = spawnPoint;
        this.kit = kit;
    }
    
    public Map<Item, Long> getItemTracker() {
        return this.itemTracker;
    }
    
    public Map<UUID, Integer> getKillStreakTracker() {
        return this.killStreakTracker;
    }
    
    public Set<KillStreak> getKillStreaks() {
        return this.killStreaks;
    }
}
