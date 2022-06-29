package us.zonix.practice.events.oitc;

import java.util.Comparator;
import java.util.Iterator;
import java.util.Arrays;
import java.util.Set;
import org.bukkit.GameMode;
import java.util.Objects;
import org.bukkit.scheduler.BukkitRunnable;
import us.zonix.practice.events.EventState;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.function.Function;
import us.zonix.practice.events.EventPlayer;
import java.util.Collection;
import java.util.concurrent.ThreadLocalRandom;
import org.bukkit.scheduler.BukkitTask;
import us.zonix.practice.player.PlayerData;
import org.bukkit.Bukkit;
import us.zonix.practice.Practice;
import us.zonix.practice.util.PlayerUtil;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Sound;
import org.bukkit.inventory.ItemStack;
import org.bukkit.entity.Player;
import java.util.function.Consumer;
import org.bukkit.plugin.Plugin;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import us.zonix.practice.util.ItemUtil;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import us.zonix.practice.CustomLocation;
import java.util.List;
import us.zonix.practice.events.EventCountdownTask;
import java.util.UUID;
import java.util.Map;
import us.zonix.practice.events.PracticeEvent;

public class OITCEvent extends PracticeEvent<OITCPlayer>
{
    private final Map<UUID, OITCPlayer> players;
    private OITCGameTask gameTask;
    private final EventCountdownTask countdownTask;
    private List<CustomLocation> respawnLocations;
    
    public OITCEvent() {
        super("OITC", ItemUtil.createItem(Material.DIAMOND_SWORD, ChatColor.RED + "OITC Event"), false);
        this.players = new HashMap<UUID, OITCPlayer>();
        this.gameTask = null;
        this.countdownTask = new EventCountdownTask(this);
    }
    
    @Override
    public Map<UUID, OITCPlayer> getPlayers() {
        return this.players;
    }
    
    @Override
    public EventCountdownTask getCountdownTask() {
        return this.countdownTask;
    }
    
    @Override
    public List<CustomLocation> getSpawnLocations() {
        return Collections.singletonList(this.getPlugin().getSpawnManager().getOitcLocation());
    }
    
    @Override
    public void onStart() {
        this.respawnLocations = new ArrayList<CustomLocation>();
        (this.gameTask = new OITCGameTask()).runTaskTimerAsynchronously((Plugin)this.getPlugin(), 0L, 20L);
    }
    
    @Override
    public Consumer<Player> onJoin() {
        return player -> this.players.put(player.getUniqueId(), new OITCPlayer(player.getUniqueId(), this));
    }
    
    @Override
    public Consumer<Player> onDeath() {
        final OITCPlayer data;
        String deathMessage;
        final OITCPlayer killerData;
        final Player killer;
        final int count;
        final FireworkEffect fireworkEffect;
        String string;
        final StringBuilder sb;
        final PlayerData winnerData;
        final String announce;
        final BukkitTask respawnTask;
        return player -> {
            data = this.getPlayer(player);
            if (data != null) {
                if (data.getState() != OITCPlayer.OITCState.WAITING) {
                    if (data.getState() == OITCPlayer.OITCState.FIGHTING || data.getState() == OITCPlayer.OITCState.PREPARING || data.getState() == OITCPlayer.OITCState.RESPAWNING) {
                        deathMessage = ChatColor.RED + "[Event] " + ChatColor.RED + player.getName() + "(" + data.getScore() + ")" + ChatColor.GRAY + " has been eliminated from the game.";
                        if (data.getLastKiller() != null) {
                            killerData = data.getLastKiller();
                            killer = this.getPlugin().getServer().getPlayer(killerData.getUuid());
                            count = killerData.getScore() + 1;
                            killerData.setScore(count);
                            killer.getInventory().setItem(6, ItemUtil.createItem(Material.GLOWSTONE_DUST, ChatColor.YELLOW.toString() + ChatColor.BOLD + "Kills", (killerData.getScore() == 0) ? 1 : killerData.getScore()));
                            if (killer.getInventory().contains(Material.ARROW)) {
                                killer.getInventory().getItem(8).setAmount(killer.getInventory().getItem(8).getAmount() + 2);
                            }
                            else {
                                killer.getInventory().setItem(8, new ItemStack(Material.ARROW, 2));
                            }
                            killer.updateInventory();
                            killer.playSound(killer.getLocation(), Sound.NOTE_PLING, 1.0f, 1.0f);
                            fireworkEffect = FireworkEffect.builder().withColor(Color.fromRGB(127, 56, 56)).withFade(Color.fromRGB(127, 56, 56)).with(FireworkEffect.Type.BALL).build();
                            PlayerUtil.sendFirework(fireworkEffect, player.getLocation().add(0.0, 1.5, 0.0));
                            data.setLastKiller(null);
                            new StringBuilder().append(ChatColor.RED).append("[Event] ").append(ChatColor.RED).append(player.getName()).append("(").append(data.getScore()).append(")").append(ChatColor.GRAY).append(" has been killed");
                            if (killer == null) {
                                string = ".";
                            }
                            else {
                                string = " by " + ChatColor.GREEN + killer.getName() + "(" + count + ")";
                            }
                            deathMessage = sb.append(string).toString();
                            if (count == 25) {
                                winnerData = Practice.getInstance().getPlayerManager().getPlayerData(killer.getUniqueId());
                                winnerData.setOitcEventWins(winnerData.getOitcEventWins() + 1);
                                announce = ChatColor.DARK_RED + killer.getName() + ChatColor.WHITE + " has won our " + ChatColor.DARK_RED + "OITC" + ChatColor.WHITE + " event!";
                                Bukkit.broadcastMessage(announce);
                                this.gameTask.cancel();
                                this.end();
                            }
                        }
                        if (data.getLastKiller() == null) {
                            data.setLives(data.getLives() - 1);
                            if (data.getLives() == 0) {
                                this.getPlayers().remove(player.getUniqueId());
                                player.sendMessage(ChatColor.RED + "[Event] " + ChatColor.GRAY + "You have been eliminated from the game.");
                                this.getPlugin().getServer().getScheduler().runTask((Plugin)this.getPlugin(), () -> {
                                    this.getPlugin().getPlayerManager().sendToSpawnAndReset(player);
                                    if (this.getPlayers().size() >= 2) {
                                        this.getPlugin().getEventManager().addSpectatorOITC(player, this.getPlugin().getPlayerManager().getPlayerData(player.getUniqueId()), this);
                                    }
                                    return;
                                });
                            }
                            else {
                                respawnTask = new RespawnTask(player, data).runTaskTimerAsynchronously((Plugin)this.getPlugin(), 0L, 20L);
                                data.setRespawnTask(respawnTask);
                            }
                        }
                        this.sendMessage(deathMessage);
                    }
                }
            }
        };
    }
    
    public void teleportNextLocation(final Player player) {
        player.teleport(this.getGameLocations().remove(ThreadLocalRandom.current().nextInt(this.getGameLocations().size())).toBukkitLocation());
    }
    
    private List<CustomLocation> getGameLocations() {
        if (this.respawnLocations != null && this.respawnLocations.size() == 0) {
            this.respawnLocations.addAll(this.getPlugin().getSpawnManager().getOitcSpawnpoints());
        }
        return this.respawnLocations;
    }
    
    private void giveRespawnItems(final Player player, final OITCPlayer oitcPlayer) {
        this.getPlugin().getServer().getScheduler().runTask((Plugin)this.getPlugin(), () -> {
            PlayerUtil.clearPlayer(player);
            player.getInventory().setItem(0, ItemUtil.createItem(Material.WOOD_SWORD, ChatColor.GREEN + "Wood Sword"));
            player.getInventory().setItem(1, ItemUtil.createItem(Material.BOW, ChatColor.GREEN + "Bow"));
            player.getInventory().setItem(6, ItemUtil.createItem(Material.GLOWSTONE_DUST, ChatColor.YELLOW.toString() + ChatColor.BOLD + "Kills", (oitcPlayer.getScore() == 0) ? 1 : oitcPlayer.getScore()));
            player.getInventory().setItem(7, ItemUtil.createItem(Material.REDSTONE, ChatColor.RED.toString() + ChatColor.BOLD + "Lives", oitcPlayer.getLives()));
            player.getInventory().setItem(8, new ItemStack(Material.ARROW));
            player.updateInventory();
        });
    }
    
    private Player getWinnerPlayer() {
        if (this.getByState(OITCPlayer.OITCState.FIGHTING).size() == 0) {
            return null;
        }
        final List<OITCPlayer> fighting = this.sortedScores();
        return this.getPlugin().getServer().getPlayer(fighting.get(0).getUuid());
    }
    
    private List<UUID> getByState(final OITCPlayer.OITCState state) {
        return this.players.values().stream().filter(player -> player.getState() == state).map((Function<? super OITCPlayer, ?>)EventPlayer::getUuid).collect((Collector<? super Object, ?, List<UUID>>)Collectors.toList());
    }
    
    @Override
    public List<String> getScoreboardLines(final Player player) {
        final List<String> strings = new ArrayList<String>();
        final int playingOITC = this.getPlayers().size();
        strings.add(ChatColor.RED.toString() + ChatColor.BOLD + "* " + ChatColor.WHITE + "Players§7: " + playingOITC + "/" + this.getLimit());
        final int countdown = this.getCountdownTask().getTimeUntilStart();
        if (countdown > 0 && countdown <= 60) {
            strings.add(ChatColor.RED.toString() + ChatColor.BOLD + "* " + ChatColor.WHITE + "Starting§7: " + countdown + "s");
        }
        if (this.getPlayer(player) != null) {
            final OITCPlayer oitcPlayer = this.getPlayer(player);
            if (oitcPlayer.getState() == OITCPlayer.OITCState.FIGHTING || oitcPlayer.getState() == OITCPlayer.OITCState.RESPAWNING) {
                strings.add(ChatColor.RED.toString() + ChatColor.BOLD + "* " + ChatColor.WHITE + "Kills§7: " + oitcPlayer.getScore());
                strings.add(ChatColor.RED.toString() + ChatColor.BOLD + "* " + ChatColor.WHITE + "Lives§7: " + oitcPlayer.getLives());
            }
        }
        final List<OITCPlayer> sortedList = this.sortedScores();
        if (sortedList.size() >= 2 && this.getState() == EventState.STARTED) {
            strings.add(ChatColor.GRAY.toString() + ChatColor.STRIKETHROUGH + "-------------------");
            strings.add(ChatColor.RED.toString() + "TOP KILLS");
            final Player first = Bukkit.getPlayer(sortedList.get(0).getUuid());
            final Player second = Bukkit.getPlayer(sortedList.get(1).getUuid());
            if (first != null) {
                strings.add(ChatColor.WHITE + "[1] " + first.getName() + "§7: §a" + sortedList.get(0).getScore());
            }
            if (second != null) {
                strings.add(ChatColor.WHITE + "[2] " + second.getName() + "§7: §a" + sortedList.get(1).getScore());
            }
            if (sortedList.size() >= 3) {
                final Player third = Bukkit.getPlayer(sortedList.get(2).getUuid());
                if (third != null) {
                    strings.add(ChatColor.WHITE + "[3] " + ChatColor.WHITE + third.getName() + "§7: §a" + sortedList.get(2).getScore());
                }
            }
        }
        return strings;
    }
    
    public List<OITCPlayer> sortedScores() {
        final List<OITCPlayer> list = new ArrayList<OITCPlayer>(this.players.values());
        list.sort(new SortComparator().reversed());
        return list;
    }
    
    public OITCGameTask getGameTask() {
        return this.gameTask;
    }
    
    public class RespawnTask extends BukkitRunnable
    {
        private final Player player;
        private final OITCPlayer oitcPlayer;
        private int time;
        
        public void run() {
            if (this.oitcPlayer.getLives() == 0) {
                this.cancel();
                return;
            }
            if (this.time > 0) {
                this.player.sendMessage(ChatColor.RED + "[Event] " + ChatColor.GRAY + "Respawning in " + this.time + "...");
            }
            if (this.time == 5) {
                final Player player;
                final Object o;
                OITCEvent.this.getPlugin().getServer().getScheduler().runTask((Plugin)OITCEvent.this.getPlugin(), () -> {
                    PlayerUtil.clearPlayer(this.player);
                    OITCEvent.this.getBukkitPlayers().forEach(member -> member.hidePlayer(this.player));
                    OITCEvent.this.getBukkitPlayers();
                    player = this.player;
                    Objects.requireNonNull(player);
                    ((Iterable<Object>)o).forEach((Consumer<? super Object>)player::hidePlayer);
                    this.player.setGameMode(GameMode.SPECTATOR);
                    return;
                });
                this.oitcPlayer.setState(OITCPlayer.OITCState.RESPAWNING);
            }
            else if (this.time <= 0) {
                this.player.sendMessage(ChatColor.RED + "[Event] " + ChatColor.GRAY + "Respawning...");
                this.player.sendMessage(ChatColor.RED + "[Event] " + ChatColor.RED.toString() + ChatColor.BOLD + this.oitcPlayer.getLives() + " " + ((this.oitcPlayer.getLives() == 1) ? "LIFE" : "LIVES") + " REMAINING");
                final Player player2;
                final Object o2;
                OITCEvent.this.getPlugin().getServer().getScheduler().runTaskLater((Plugin)OITCEvent.this.getPlugin(), () -> {
                    OITCEvent.this.giveRespawnItems(this.player, this.oitcPlayer);
                    this.player.teleport(OITCEvent.this.getGameLocations().remove(ThreadLocalRandom.current().nextInt(OITCEvent.this.getGameLocations().size())).toBukkitLocation());
                    OITCEvent.this.getBukkitPlayers().forEach(member -> member.showPlayer(this.player));
                    OITCEvent.this.getBukkitPlayers();
                    player2 = this.player;
                    Objects.requireNonNull(player2);
                    ((Iterable<Object>)o2).forEach((Consumer<? super Object>)player2::showPlayer);
                    return;
                }, 2L);
                this.oitcPlayer.setState(OITCPlayer.OITCState.FIGHTING);
                this.cancel();
            }
            --this.time;
        }
        
        public Player getPlayer() {
            return this.player;
        }
        
        public OITCPlayer getOitcPlayer() {
            return this.oitcPlayer;
        }
        
        public int getTime() {
            return this.time;
        }
        
        public RespawnTask(final Player player, final OITCPlayer oitcPlayer) {
            this.time = 5;
            this.player = player;
            this.oitcPlayer = oitcPlayer;
        }
    }
    
    public class OITCGameTask extends BukkitRunnable
    {
        private int time;
        
        public void run() {
            if (this.time == 303) {
                PlayerUtil.sendMessage(ChatColor.YELLOW + "The game starts in " + ChatColor.GREEN + 3 + ChatColor.YELLOW + "...", OITCEvent.this.getBukkitPlayers());
            }
            else if (this.time == 302) {
                PlayerUtil.sendMessage(ChatColor.YELLOW + "The game starts in " + ChatColor.GREEN + 2 + ChatColor.YELLOW + "...", OITCEvent.this.getBukkitPlayers());
            }
            else if (this.time == 301) {
                PlayerUtil.sendMessage(ChatColor.YELLOW + "The game starts in " + ChatColor.GREEN + 1 + ChatColor.YELLOW + "...", OITCEvent.this.getBukkitPlayers());
            }
            else if (this.time == 300) {
                PlayerUtil.sendMessage(ChatColor.GREEN + "The game has started, good luck!", OITCEvent.this.getBukkitPlayers());
                for (final OITCPlayer player : OITCEvent.this.getPlayers().values()) {
                    player.setScore(0);
                    player.setLives(5);
                    player.setState(OITCPlayer.OITCState.FIGHTING);
                }
                for (final Player player2 : OITCEvent.this.getBukkitPlayers()) {
                    final OITCPlayer oitcPlayer = OITCEvent.this.getPlayer(player2.getUniqueId());
                    if (oitcPlayer != null) {
                        OITCEvent.this.teleportNextLocation(player2);
                        OITCEvent.this.giveRespawnItems(player2, oitcPlayer);
                    }
                }
            }
            else if (this.time <= 0) {
                final Player winner = OITCEvent.this.getWinnerPlayer();
                if (winner != null) {
                    final PlayerData winnerData = Practice.getInstance().getPlayerManager().getPlayerData(winner.getUniqueId());
                    winnerData.setOitcEventWins(winnerData.getOitcEventWins() + 1);
                    final String announce = ChatColor.DARK_RED + winner.getName() + ChatColor.WHITE + " has won our " + ChatColor.DARK_RED + "OITC" + ChatColor.WHITE + " event!";
                    Bukkit.broadcastMessage(announce);
                }
                OITCEvent.this.gameTask.cancel();
                OITCEvent.this.end();
                this.cancel();
                return;
            }
            if (OITCEvent.this.getByState(OITCPlayer.OITCState.FIGHTING).size() == 1 || OITCEvent.this.getPlayers().size() == 1) {
                final Player winner = new ArrayList<OITCPlayer>(OITCEvent.this.players.values()).get(0).getPlayer();
                final PlayerData winnerData = Practice.getInstance().getPlayerManager().getPlayerData(winner.getUniqueId());
                winnerData.setOitcEventWins(winnerData.getOitcEventWins() + 1);
                final String announce = ChatColor.DARK_RED + winner.getName() + ChatColor.WHITE + " has won our " + ChatColor.DARK_RED + "OITC" + ChatColor.WHITE + " event!";
                Bukkit.broadcastMessage(announce);
                this.cancel();
                OITCEvent.this.end();
            }
            if (Arrays.asList(60, 50, 40, 30, 25, 20, 15, 10).contains(this.time)) {
                PlayerUtil.sendMessage(ChatColor.YELLOW + "The game ends in " + ChatColor.GREEN + this.time + ChatColor.YELLOW + "...", OITCEvent.this.getBukkitPlayers());
            }
            else if (Arrays.asList(5, 4, 3, 2, 1).contains(this.time)) {
                PlayerUtil.sendMessage(ChatColor.YELLOW + "The game is ending in " + ChatColor.GREEN + this.time + ChatColor.YELLOW + "...", OITCEvent.this.getBukkitPlayers());
            }
            --this.time;
        }
        
        public int getTime() {
            return this.time;
        }
        
        public OITCGameTask() {
            this.time = 303;
        }
    }
    
    private class SortComparator implements Comparator<OITCPlayer>
    {
        @Override
        public int compare(final OITCPlayer p1, final OITCPlayer p2) {
            return Integer.compare(p1.getScore(), p2.getScore());
        }
    }
}
