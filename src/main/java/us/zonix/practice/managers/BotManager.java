package us.zonix.practice.managers;

import java.util.Map;
import org.bukkit.plugin.Plugin;
import org.bukkit.Bukkit;
import java.util.Iterator;
import net.citizensnpcs.api.npc.NPC;
import us.zonix.practice.player.PlayerData;
import us.zonix.practice.arena.Arena;
import us.zonix.practice.player.PlayerState;
import java.util.Collections;
import org.bukkit.entity.EntityType;
import net.citizensnpcs.api.CitizensAPI;
import org.bukkit.ChatColor;
import us.zonix.practice.kit.Kit;
import org.bukkit.entity.Player;
import us.zonix.practice.bots.ZonixBot;
import java.util.UUID;
import java.util.HashMap;
import us.zonix.practice.Practice;

public class BotManager
{
    private final Practice plugin;
    private HashMap<UUID, ZonixBot> npcRegistry;
    
    public BotManager() {
        this.plugin = Practice.getInstance();
        this.npcRegistry = new HashMap<UUID, ZonixBot>();
    }
    
    public void createMatch(final Player player, final Kit kit, final ZonixBot.BotDifficulty difficulty) {
        final Arena arena = this.plugin.getArenaManager().getRandomArena(kit);
        final PlayerData playerData = this.plugin.getPlayerManager().getPlayerData(player.getUniqueId());
        player.sendMessage(ChatColor.YELLOW + "Starting training match. " + ChatColor.GREEN + "(" + player.getName() + " vs Zeus)");
        final NPC npc = CitizensAPI.getNPCRegistry().createNPC(EntityType.PLAYER, "Zeus");
        npc.data().set("player-skin-name", (Object)"Emilio");
        npc.spawn(arena.getA().toBukkitLocation());
        player.teleport(arena.getB().toBukkitLocation());
        final ZonixBot bot = new ZonixBot();
        bot.setBotDifficulty(difficulty);
        bot.setKit(kit);
        bot.setArena(arena);
        bot.setDestroyed(false);
        bot.setNpc(npc);
        bot.startMechanics(Collections.singletonList(player.getUniqueId()), difficulty);
        playerData.setPlayerState(PlayerState.TRAINING);
        kit.applyToPlayer(player);
        for (final Player online : this.plugin.getServer().getOnlinePlayers()) {
            online.hidePlayer(player);
            player.hidePlayer(online);
        }
        for (final Player online : this.plugin.getServer().getOnlinePlayers()) {
            online.hidePlayer(bot.getBukkitEntity());
            bot.getBukkitEntity().hidePlayer(online);
        }
        player.showPlayer(bot.getBukkitEntity());
        bot.getBukkitEntity().showPlayer(player);
        this.npcRegistry.put(player.getUniqueId(), bot);
    }
    
    public void removeMatch(final Player player, final boolean won) {
        if (!this.isTraining(player)) {
            return;
        }
        final ZonixBot bot = this.npcRegistry.get(player.getUniqueId());
        if (bot.getBotMechanics() != null) {
            bot.getBotMechanics().cancel();
        }
        bot.destroy();
        this.npcRegistry.remove(player.getUniqueId());
        this.plugin.getPlayerManager().sendToSpawnAndReset(player);
        player.sendMessage(ChatColor.YELLOW + "You " + (won ? (ChatColor.GREEN.toString() + ChatColor.BOLD + "WON") : (ChatColor.RED.toString() + ChatColor.BOLD + "LOST")) + ChatColor.YELLOW.toString() + " against the training bot.");
    }
    
    public void forceRemoveMatch(final Player player) {
        if (!this.isTraining(player)) {
            return;
        }
        final ZonixBot bot = this.npcRegistry.get(player.getUniqueId());
        if (bot.getBotMechanics() != null) {
            bot.getBotMechanics().cancel();
        }
        bot.destroy();
        this.npcRegistry.remove(player.getUniqueId());
        this.plugin.getPlayerManager().sendToSpawnAndReset(player);
    }
    
    public void removeMatch(final NPC npc) {
        final UUID uuid = this.getPlayerMatch(npc);
        if (uuid == null) {
            return;
        }
        if (!this.npcRegistry.containsKey(uuid)) {
            return;
        }
        final Player player = Bukkit.getPlayer(uuid);
        if (player != null) {
            final Player player2;
            Bukkit.getScheduler().runTaskLater((Plugin)this.plugin, () -> {
                this.plugin.getPlayerManager().sendToSpawnAndReset(player2);
                player2.sendMessage(ChatColor.YELLOW + "You " + ChatColor.GREEN.toString() + ChatColor.BOLD + "WON" + ChatColor.YELLOW.toString() + " against the training bot.");
                return;
            }, 10L);
        }
        final ZonixBot bot = this.npcRegistry.get(uuid);
        if (bot.getBotMechanics() != null) {
            bot.getBotMechanics().cancel();
        }
        bot.destroy();
        this.npcRegistry.remove(uuid);
    }
    
    public boolean isTraining(final Player player) {
        return this.npcRegistry.containsKey(player.getUniqueId());
    }
    
    public UUID getPlayerMatch(final NPC npcMatching) {
        for (final Map.Entry<UUID, ZonixBot> map : this.npcRegistry.entrySet()) {
            if (map.getValue().getNpc().getUniqueId() == npcMatching.getUniqueId()) {
                return map.getKey();
            }
        }
        return null;
    }
    
    public ZonixBot getBotFromNPC(final NPC npc) {
        for (final Map.Entry<UUID, ZonixBot> map : this.npcRegistry.entrySet()) {
            if (map.getValue().getNpc().getUniqueId() == npc.getUniqueId()) {
                return map.getValue();
            }
        }
        return null;
    }
    
    public ZonixBot getBotFromPlayer(final Player player) {
        for (final Map.Entry<UUID, ZonixBot> map : this.npcRegistry.entrySet()) {
            if (map.getKey() == player.getUniqueId()) {
                return map.getValue();
            }
        }
        return null;
    }
    
    public HashMap<UUID, ZonixBot> getNpcRegistry() {
        return this.npcRegistry;
    }
}
