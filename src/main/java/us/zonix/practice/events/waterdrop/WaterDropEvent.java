package us.zonix.practice.events.waterdrop;

import us.zonix.practice.util.BlockUtil;
import java.util.Arrays;
import org.bukkit.scheduler.BukkitRunnable;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.function.Function;
import us.zonix.practice.events.EventPlayer;
import java.util.concurrent.ThreadLocalRandom;
import java.util.Collection;
import com.sk89q.worldedit.EditSession;
import java.util.Objects;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.Vector;
import com.boydti.fawe.util.EditSessionBuilder;
import com.boydti.fawe.util.TaskManager;
import org.bukkit.block.Block;
import com.google.common.collect.Lists;
import us.zonix.practice.player.PlayerData;
import us.zonix.practice.Practice;
import us.zonix.practice.util.PlayerUtil;
import org.bukkit.Bukkit;
import java.util.Iterator;
import org.bukkit.entity.Player;
import java.util.function.Consumer;
import org.bukkit.plugin.Plugin;
import java.util.Collections;
import us.zonix.practice.CustomLocation;
import java.util.ArrayList;
import java.util.HashMap;
import us.zonix.practice.util.ItemUtil;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import us.zonix.practice.util.cuboid.Cuboid;
import us.zonix.practice.events.EventCountdownTask;
import java.util.List;
import java.util.UUID;
import java.util.Map;
import us.zonix.practice.events.PracticeEvent;

public class WaterDropEvent extends PracticeEvent<WaterDropPlayer>
{
    private final Map<UUID, WaterDropPlayer> players;
    private List<UUID> possiblePlayers;
    private WaterDropGameTask gameTask;
    private final EventCountdownTask countdownTask;
    private WaterDropCheckTask waterCheckTask;
    private List<UUID> visibility;
    private int round;
    private Cuboid cuboid;
    
    public WaterDropEvent() {
        super("WaterDrop", ItemUtil.createItem(Material.BUCKET, ChatColor.RED + "WaterDrop Event"), false);
        this.players = new HashMap<UUID, WaterDropPlayer>();
        this.possiblePlayers = new ArrayList<UUID>();
        this.gameTask = null;
        this.countdownTask = new EventCountdownTask(this);
    }
    
    @Override
    public Map<UUID, WaterDropPlayer> getPlayers() {
        return this.players;
    }
    
    @Override
    public EventCountdownTask getCountdownTask() {
        return this.countdownTask;
    }
    
    @Override
    public List<CustomLocation> getSpawnLocations() {
        return Collections.singletonList(this.getPlugin().getSpawnManager().getWaterDropLocation());
    }
    
    @Override
    public void onStart() {
        (this.gameTask = new WaterDropGameTask()).runTaskTimerAsynchronously((Plugin)this.getPlugin(), 0L, 20L);
        (this.waterCheckTask = new WaterDropCheckTask()).runTaskTimer((Plugin)this.getPlugin(), 0L, 10L);
        this.visibility = new ArrayList<UUID>();
        this.round = 0;
        this.possiblePlayers.clear();
        this.cuboid = new Cuboid(this.getPlugin().getSpawnManager().getWaterDropFirst().toBukkitLocation(), this.getPlugin().getSpawnManager().getWaterDropSecond().toBukkitLocation());
    }
    
    @Override
    public Consumer<Player> onJoin() {
        return player -> {
            this.players.put(player.getUniqueId(), new WaterDropPlayer(player.getUniqueId(), this));
            this.getPlayer(player).setState(WaterDropPlayer.WaterDropState.LOBBY);
        };
    }
    
    @Override
    public Consumer<Player> onDeath() {
        final WaterDropPlayer data;
        return player -> {
            data = this.getPlayer(player);
            if (data.getState() != WaterDropPlayer.WaterDropState.LOBBY) {
                this.possiblePlayers.add(player.getUniqueId());
                this.teleportToSpawn(player);
                this.giveItems(player);
                this.sendMessage(ChatColor.GRAY + "[" + ChatColor.YELLOW + "Round " + this.round + ChatColor.GRAY + "] " + ChatColor.RED + player.getName() + " didn't make it to the next round.");
            }
        };
    }
    
    public void toggleVisibility(final Player player) {
        if (this.visibility.contains(player.getUniqueId())) {
            for (final Player playing : this.getBukkitPlayers()) {
                player.showPlayer(playing);
            }
            this.visibility.remove(player.getUniqueId());
            player.sendMessage(ChatColor.GREEN + "You are now showing players.");
            return;
        }
        for (final Player playing : this.getBukkitPlayers()) {
            player.hidePlayer(playing);
        }
        this.visibility.add(player.getUniqueId());
        player.sendMessage(ChatColor.GREEN + "You are now hiding players.");
    }
    
    public void teleportToSpawn(final Player player) {
        Bukkit.getServer().getScheduler().runTask((Plugin)this.getPlugin(), () -> player.teleport(this.getPlugin().getSpawnManager().getWaterDropLocation().toBukkitLocation()));
    }
    
    private void giveItems(final Player player) {
        this.getPlugin().getServer().getScheduler().runTask((Plugin)this.getPlugin(), () -> {
            PlayerUtil.clearPlayer(player);
            player.getInventory().setItem(0, ItemUtil.createItem(Material.FIREBALL, ChatColor.GREEN.toString() + "Toggle Visibility"));
            player.getInventory().setItem(4, ItemUtil.createItem(Material.NETHER_STAR, ChatColor.RED.toString() + "Leave Event"));
            player.updateInventory();
        });
    }
    
    private void nextRound() {
        final List<Player> waterDropPlayers = this.prepareNextRoundPlayers();
        if (waterDropPlayers.size() == 0) {
            this.endGame();
            return;
        }
        this.sendMessage(ChatColor.GRAY + "[" + ChatColor.YELLOW + "Round " + this.round + ChatColor.GRAY + "] " + ChatColor.GOLD + "Checking players left...");
        for (final Player player : waterDropPlayers) {
            this.getPlayer(player).setState(WaterDropPlayer.WaterDropState.JUMPING);
            Bukkit.getServer().getScheduler().runTask((Plugin)this.getPlugin(), () -> player.teleport(this.getPlugin().getSpawnManager().getWaterDropJump().toBukkitLocation()));
        }
        this.sendMessage(ChatColor.GRAY + "[" + ChatColor.YELLOW + "Round " + this.round + ChatColor.GRAY + "] " + ChatColor.GOLD + this.getByState(WaterDropPlayer.WaterDropState.JUMPING).size() + " players remaining.");
    }
    
    private List<Player> prepareNextRoundPlayers() {
        final List<Player> waterDropPlayers = new ArrayList<Player>();
        if (this.getByState(WaterDropPlayer.WaterDropState.NEXT_ROUND).size() == 1 && this.getByState(WaterDropPlayer.WaterDropState.JUMPING).size() == 0) {
            this.endGame();
            return waterDropPlayers;
        }
        if (this.getByState(WaterDropPlayer.WaterDropState.JUMPING).size() != 0 || this.getByState(WaterDropPlayer.WaterDropState.NEXT_ROUND).size() != 0) {
            if (this.getByState(WaterDropPlayer.WaterDropState.NEXT_ROUND).size() > 0) {
                for (final UUID uuid : this.getByState(WaterDropPlayer.WaterDropState.NEXT_ROUND)) {
                    final Player player = Bukkit.getPlayer(uuid);
                    if (player != null) {
                        waterDropPlayers.add(player);
                    }
                }
            }
            else if (this.getByState(WaterDropPlayer.WaterDropState.JUMPING).size() > 0) {
                for (final UUID uuid : this.getByState(WaterDropPlayer.WaterDropState.JUMPING)) {
                    final Player player = Bukkit.getPlayer(uuid);
                    if (player != null) {
                        waterDropPlayers.add(player);
                    }
                }
            }
            ++this.round;
            this.generateCuboid(this.cuboid);
            this.possiblePlayers.clear();
            return waterDropPlayers;
        }
        if (this.possiblePlayers.size() <= 1) {
            this.endGame();
            return waterDropPlayers;
        }
        for (final UUID uuid : this.possiblePlayers) {
            final Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                waterDropPlayers.add(player);
            }
        }
        ++this.round;
        this.generateCuboid(this.cuboid);
        this.possiblePlayers.clear();
        return waterDropPlayers;
    }
    
    private void endGame() {
        final Player winner = Bukkit.getPlayer((UUID)this.getByState(WaterDropPlayer.WaterDropState.JUMPING).get(0));
        final PlayerData winnerData = Practice.getInstance().getPlayerManager().getPlayerData(winner.getUniqueId());
        winnerData.setSumoEventWins(winnerData.getSumoEventWins() + 1);
        for (int i = 0; i <= 2; ++i) {
            final String announce = ChatColor.RED + winner.getName() + ChatColor.WHITE + " has won the event!";
            Bukkit.broadcastMessage(announce);
        }
        final String announce2 = ChatColor.DARK_RED + winner.getName() + ChatColor.WHITE + " has won our " + ChatColor.DARK_RED + "Water Drop" + ChatColor.WHITE + " event!";
        Bukkit.broadcastMessage(announce2);
        this.end();
    }
    
    @Override
    public List<String> getScoreboardLines(final Player player) {
        final List<String> strings = (List<String>)Lists.newArrayList();
        final int playing = this.getPlayers().size();
        strings.add(ChatColor.RED.toString() + ChatColor.BOLD + " * " + ChatColor.WHITE + "Players§7: " + playing + "/" + this.getLimit());
        final int countdown = this.getCountdownTask().getTimeUntilStart();
        if (countdown > 0 && countdown <= 60) {
            strings.add(ChatColor.RED.toString() + ChatColor.BOLD + " * " + ChatColor.WHITE + "Starting§7: " + countdown + "s");
        }
        if (this.getPlayer(player) != null && this.getPlayer(player).getState() != WaterDropPlayer.WaterDropState.LOBBY) {
            strings.add(ChatColor.RED.toString() + ChatColor.BOLD + " * " + ChatColor.WHITE + "Round§7: " + this.getRound());
        }
        return strings;
    }
    
    private void generateCuboid(final Cuboid cuboid) {
        if (cuboid == null) {
            return;
        }
        final int blocksAmount = this.getBlocksAmount();
        final List<Block> blocks = new ArrayList<Block>();
        for (final Block block : cuboid) {
            blocks.add(block);
        }
        final EditSession editSession;
        final List<Block> list;
        final Iterator<Block> iterator2;
        Block entry;
        int i;
        final int n;
        final TaskManager imp;
        TaskManager.IMP.async(() -> {
            editSession = new EditSessionBuilder(this.cuboid.getWorld().getName()).fastmode(true).allowedRegionsEverywhere().autoQueue(false).limitUnlimited().build();
            list.iterator();
            while (iterator2.hasNext()) {
                entry = iterator2.next();
                try {
                    editSession.setBlock(new Vector((double)entry.getLocation().getBlockX(), (double)entry.getLocation().getBlockY(), entry.getLocation().getZ()), new BaseBlock(9, 0));
                }
                catch (Exception ex) {}
            }
            Collections.shuffle(list);
            for (i = 0; i < n; ++i) {
                try {
                    editSession.setBlock(new Vector((double)list.get(i).getLocation().getBlockX(), (double)list.get(i).getLocation().getBlockY(), list.get(i).getLocation().getZ()), new BaseBlock(35, 0));
                }
                catch (Exception ex2) {}
            }
            editSession.flushQueue();
            imp = TaskManager.IMP;
            Objects.requireNonNull(list);
            imp.task(list::clear);
        });
    }
    
    private Player getRandomPlayer() {
        final List<Player> playersRandom = new ArrayList<Player>();
        playersRandom.addAll(this.getBukkitPlayers());
        Collections.shuffle(playersRandom);
        return playersRandom.get(ThreadLocalRandom.current().nextInt(playersRandom.size()));
    }
    
    public Cuboid getCuboid() {
        return this.cuboid;
    }
    
    private int getBlocksAmount() {
        if (this.getByState(WaterDropPlayer.WaterDropState.JUMPING).size() <= 5) {
            return 8;
        }
        if (this.getByState(WaterDropPlayer.WaterDropState.JUMPING).size() <= 10) {
            return 6;
        }
        if (this.getByState(WaterDropPlayer.WaterDropState.JUMPING).size() <= 15) {
            return 4;
        }
        if (this.getByState(WaterDropPlayer.WaterDropState.JUMPING).size() <= 20) {
            return 3;
        }
        return 1;
    }
    
    public List<UUID> getByState(final WaterDropPlayer.WaterDropState state) {
        return this.players.values().stream().filter(player -> player.getState() == state).map((Function<? super WaterDropPlayer, ?>)EventPlayer::getUuid).collect((Collector<? super Object, ?, List<UUID>>)Collectors.toList());
    }
    
    public int getRound() {
        return this.round;
    }
    
    public WaterDropGameTask getGameTask() {
        return this.gameTask;
    }
    
    public WaterDropCheckTask getWaterCheckTask() {
        return this.waterCheckTask;
    }
    
    public class WaterDropGameTask extends BukkitRunnable
    {
        private int time;
        
        public void run() {
            if (this.time == 303) {
                PlayerUtil.sendMessage(ChatColor.YELLOW + "The game starts in " + ChatColor.GREEN + 3 + ChatColor.YELLOW + "...", WaterDropEvent.this.getBukkitPlayers());
            }
            else if (this.time == 302) {
                PlayerUtil.sendMessage(ChatColor.YELLOW + "The game starts in " + ChatColor.GREEN + 2 + ChatColor.YELLOW + "...", WaterDropEvent.this.getBukkitPlayers());
            }
            else if (this.time == 301) {
                PlayerUtil.sendMessage(ChatColor.YELLOW + "The game starts in " + ChatColor.GREEN + 1 + ChatColor.YELLOW + "...", WaterDropEvent.this.getBukkitPlayers());
            }
            else if (this.time == 300) {
                PlayerUtil.sendMessage(ChatColor.GREEN + "The game has started, good luck!", WaterDropEvent.this.getBukkitPlayers());
                for (final WaterDropPlayer player : WaterDropEvent.this.getPlayers().values()) {
                    player.setState(WaterDropPlayer.WaterDropState.JUMPING);
                }
                for (final Player player2 : WaterDropEvent.this.getBukkitPlayers()) {
                    WaterDropEvent.this.giveItems(player2);
                }
                WaterDropEvent.this.nextRound();
            }
            else if (this.time <= 0) {
                final Player winner = WaterDropEvent.this.getRandomPlayer();
                if (winner != null) {
                    final PlayerData winnerData = Practice.getInstance().getPlayerManager().getPlayerData(winner.getUniqueId());
                    winnerData.setWaterDropEventWins(winnerData.getWaterDropEventWins() + 1);
                    final String announce = ChatColor.DARK_RED + winner.getName() + ChatColor.WHITE + " has won our " + ChatColor.DARK_RED + "Water Drop" + ChatColor.WHITE + " event!";
                    Bukkit.broadcastMessage(announce);
                }
                WaterDropEvent.this.end();
                this.cancel();
                return;
            }
            if (WaterDropEvent.this.getPlayers().size() == 1) {
                final Player winner = WaterDropEvent.this.getRandomPlayer();
                if (winner != null) {
                    final PlayerData winnerData = Practice.getInstance().getPlayerManager().getPlayerData(winner.getUniqueId());
                    winnerData.setWaterDropEventWins(winnerData.getWaterDropEventWins() + 1);
                    final String announce = ChatColor.DARK_RED + winner.getName() + ChatColor.WHITE + " has won our " + ChatColor.DARK_RED + "Water Drop" + ChatColor.WHITE + " event!";
                    Bukkit.broadcastMessage(announce);
                }
                WaterDropEvent.this.end();
                this.cancel();
                return;
            }
            if (Arrays.asList(60, 50, 40, 30, 25, 20, 15, 10).contains(this.time)) {
                PlayerUtil.sendMessage(ChatColor.YELLOW + "The game ends in " + ChatColor.GREEN + this.time + ChatColor.YELLOW + "...", WaterDropEvent.this.getBukkitPlayers());
            }
            else if (Arrays.asList(5, 4, 3, 2, 1).contains(this.time)) {
                PlayerUtil.sendMessage(ChatColor.YELLOW + "The game is ending in " + ChatColor.GREEN + this.time + ChatColor.YELLOW + "...", WaterDropEvent.this.getBukkitPlayers());
            }
            --this.time;
        }
        
        public int getTime() {
            return this.time;
        }
        
        public WaterDropGameTask() {
            this.time = 303;
        }
    }
    
    public class WaterDropCheckTask extends BukkitRunnable
    {
        public void run() {
            if (WaterDropEvent.this.getPlayers().size() <= 1) {
                return;
            }
            WaterDropEvent.this.getBukkitPlayers().forEach(player -> {
                if (WaterDropEvent.this.getPlayer(player) != null && WaterDropEvent.this.getPlayer(player).getState() != WaterDropPlayer.WaterDropState.JUMPING) {
                    return;
                }
                else {
                    if (BlockUtil.isStandingOn(player, Material.WATER) || BlockUtil.isStandingOn(player, Material.STATIONARY_WATER)) {
                        WaterDropEvent.this.getPlayer(player.getUniqueId()).setState(WaterDropPlayer.WaterDropState.NEXT_ROUND);
                        WaterDropEvent.this.sendMessage(ChatColor.GRAY + "[" + ChatColor.YELLOW + "Round " + WaterDropEvent.this.round + ChatColor.GRAY + "] " + ChatColor.GREEN + player.getName() + " made it to the next round.");
                        WaterDropEvent.this.teleportToSpawn(player);
                    }
                    return;
                }
            });
            if ((WaterDropEvent.this.getByState(WaterDropPlayer.WaterDropState.NEXT_ROUND).size() >= 1 && WaterDropEvent.this.getByState(WaterDropPlayer.WaterDropState.JUMPING).size() == 0) || (WaterDropEvent.this.getByState(WaterDropPlayer.WaterDropState.NEXT_ROUND).size() == 0 && WaterDropEvent.this.getByState(WaterDropPlayer.WaterDropState.JUMPING).size() == 0 && WaterDropEvent.this.possiblePlayers.size() >= 1)) {
                Bukkit.getScheduler().runTaskLaterAsynchronously((Plugin)WaterDropEvent.this.getPlugin(), () -> WaterDropEvent.this.nextRound(), 20L);
            }
        }
    }
}
