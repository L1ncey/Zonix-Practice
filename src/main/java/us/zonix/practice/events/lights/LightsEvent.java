package us.zonix.practice.events.lights;

import org.bukkit.inventory.PlayerInventory;
import us.zonix.practice.player.PlayerData;
import us.zonix.practice.util.PlayerUtil;
import us.zonix.practice.Practice;
import org.bukkit.scheduler.BukkitRunnable;
import java.text.DecimalFormat;
import us.zonix.practice.util.DateUtil;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.function.Function;
import us.zonix.practice.events.EventPlayer;
import java.util.Iterator;
import org.bukkit.Bukkit;
import me.maiko.dexter.util.CC;
import me.maiko.dexter.profile.Profile;
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
import java.util.Random;
import us.zonix.practice.events.EventCountdownTask;
import java.util.List;
import java.util.UUID;
import java.util.Map;
import us.zonix.practice.events.PracticeEvent;

public class LightsEvent extends PracticeEvent<LightsPlayer>
{
    private final Map<UUID, LightsPlayer> players;
    private final List<UUID> movingPlayers;
    private LightsGameTask gameTask;
    private final EventCountdownTask countdownTask;
    private int countdown;
    private int taskId;
    private long ticksPerRound;
    private int status;
    private LightsGameState current;
    private long timeLeft;
    private Random random;
    
    public LightsEvent() {
        super("RedLightGreenLight", ItemUtil.createItem(Material.REDSTONE_BLOCK, ChatColor.RED + "RedLightGreenLight Event"), true);
        this.players = new HashMap<UUID, LightsPlayer>();
        this.movingPlayers = new ArrayList<UUID>();
        this.gameTask = null;
        this.countdownTask = new EventCountdownTask(this);
        this.random = new Random();
    }
    
    @Override
    public Map<UUID, LightsPlayer> getPlayers() {
        return this.players;
    }
    
    @Override
    public EventCountdownTask getCountdownTask() {
        return this.countdownTask;
    }
    
    @Override
    public List<CustomLocation> getSpawnLocations() {
        return Collections.singletonList(this.getPlugin().getSpawnManager().getLightsLocation());
    }
    
    @Override
    public void onStart() {
        (this.gameTask = new LightsGameTask()).runTaskTimerAsynchronously((Plugin)this.getPlugin(), 0L, 20L);
        this.taskId = this.gameTask.getTaskId();
        this.current = LightsGameState.GREEN;
        this.countdown = 0;
        this.ticksPerRound = 200L;
        this.status = -1;
        this.timeLeft = 0L;
        this.movingPlayers.clear();
    }
    
    @Override
    public Consumer<Player> onJoin() {
        return player -> {
            this.players.put(player.getUniqueId(), new LightsPlayer(player.getUniqueId(), this));
            this.getPlayer(player).setState(LightsPlayer.LightsState.LOBBY);
        };
    }
    
    @Override
    public Consumer<Player> onDeath() {
        final LightsPlayer data;
        final Profile profile;
        final Player winner;
        final String announce;
        final Profile winnerProfile;
        return player -> {
            data = this.getPlayer(player);
            if (data.getState() != LightsPlayer.LightsState.LOBBY) {
                this.players.remove(player.getUniqueId());
                this.sendMessage("&7[&f" + this.getName() + "&7] &c" + player.getName() + ChatColor.WHITE + " has been eliminated from the game.");
                profile = Profile.getByUuid(player.getUniqueId());
                profile.awardCoins(player, 5);
                player.sendMessage(CC.GOLD + "You earn 5 coins for participating in the event.");
                this.getPlugin().getServer().getScheduler().runTask((Plugin)this.getPlugin(), () -> {
                    this.getPlugin().getPlayerManager().sendToSpawnAndReset(player);
                    if (this.getPlayers().size() >= 2) {
                        this.getPlugin().getEventManager().addSpectatorLights(player, this.getPlugin().getPlayerManager().getPlayerData(player.getUniqueId()), this);
                    }
                    return;
                });
                if (this.getByState(LightsPlayer.LightsState.INGAME).size() == 1) {
                    winner = Bukkit.getPlayer((UUID)this.getByState(LightsPlayer.LightsState.INGAME).stream().findFirst().get());
                    if (winner != null) {
                        announce = ChatColor.DARK_RED + winner.getName() + ChatColor.WHITE + " has won our " + ChatColor.DARK_RED + "RedLightGreenLight" + ChatColor.WHITE + " event!";
                        winnerProfile = Profile.getByUuid(winner.getUniqueId());
                        winnerProfile.awardCoins(winner, 15);
                        winner.sendMessage(CC.GOLD + "You earn 15 coins for winning the event!");
                        Bukkit.broadcastMessage(announce);
                    }
                    this.end();
                }
            }
        };
    }
    
    public void setCurrent(final LightsGameState state) {
        this.current = state;
        for (final Player player : this.getBukkitPlayers()) {
            this.giveItems(player);
        }
    }
    
    public void teleportToSpawn(final Player player) {
        Bukkit.getServer().getScheduler().runTask((Plugin)this.getPlugin(), () -> player.teleport(this.getPlugin().getSpawnManager().getLightsStart().toBukkitLocation().clone().add(0.0, 2.0, 0.0)));
    }
    
    private void giveItems(final Player player) {
        int i;
        Material wool;
        String name;
        final Object o;
        final int n;
        this.getPlugin().getServer().getScheduler().runTask((Plugin)this.getPlugin(), () -> {
            for (i = 0; i <= 8; ++i) {
                player.getInventory();
                wool = Material.WOOL;
                if (this.current == LightsGameState.GREEN) {
                    name = ChatColor.GREEN.toString() + ChatColor.BOLD + "GO";
                }
                else if (this.current == LightsGameState.YELLOW) {
                    name = ChatColor.YELLOW.toString() + ChatColor.BOLD + "SLOW";
                }
                else {
                    name = ChatColor.RED.toString() + ChatColor.BOLD + "STOP";
                }
                ((PlayerInventory)o).setItem(n, ItemUtil.createItem(wool, name, 1, (short)((this.current == LightsGameState.GREEN) ? 5 : ((this.current == LightsGameState.YELLOW) ? 4 : 14))));
            }
            player.updateInventory();
        });
    }
    
    public List<UUID> getByState(final LightsPlayer.LightsState state) {
        return this.players.values().stream().filter(player -> player.getState() == state).map((Function<? super LightsPlayer, ?>)EventPlayer::getUuid).collect((Collector<? super Object, ?, List<UUID>>)Collectors.toList());
    }
    
    public void setTaskId(final int taskId) {
        this.taskId = taskId;
    }
    
    public int getTaskId() {
        return this.taskId;
    }
    
    public LightsGameState getCurrent() {
        return this.current;
    }
    
    public List<UUID> getMovingPlayers() {
        return this.movingPlayers;
    }
    
    public String getTimeLeft() {
        final long time = this.timeLeft - System.currentTimeMillis();
        if (time >= 3600000L) {
            return DateUtil.formatTime(time);
        }
        if (time >= 60000L) {
            return DateUtil.formatTime(time);
        }
        final DecimalFormat SECONDS_FORMATTER = new DecimalFormat("#0.0");
        return SECONDS_FORMATTER.format(time / 1000.0f) + "s";
    }
    
    private int getRandomNumber() {
        return this.random.nextInt(4) + 1;
    }
    
    @Override
    public List<String> getScoreboardLines(final Player player) {
        final List<String> strings = new ArrayList<String>();
        final int playing = this.getPlayers().size();
        strings.add(ChatColor.RED.toString() + ChatColor.BOLD + " * " + ChatColor.WHITE + "Players§7: " + playing + "/" + this.getLimit());
        final int countdown = this.getCountdownTask().getTimeUntilStart();
        if (countdown > 0 && countdown <= 60) {
            strings.add(ChatColor.RED.toString() + ChatColor.BOLD + " * " + ChatColor.WHITE + "Starting§7: " + countdown + "s");
        }
        if (this.getPlayer(player) != null && this.getPlayer(player).getState() == LightsPlayer.LightsState.INGAME) {
            strings.add(" ");
            if (this.getCurrent() == LightsGameState.RED) {
                strings.add("      §4\u2b24  §4§lSTOP");
                strings.add("      §7\u2b24");
                strings.add("      §7\u2b24");
            }
            else if (this.getCurrent() == LightsGameState.YELLOW) {
                strings.add("      §7\u2b24");
                strings.add("      §e\u2b24  §6§lSLOW");
                strings.add("      §7\u2b24");
            }
            else if (this.getCurrent() == LightsGameState.GREEN) {
                strings.add("      §7\u2b24");
                strings.add("      §7\u2b24");
                strings.add("      §a\u2b24  §a§lGO");
            }
            strings.add(" ");
        }
        return strings;
    }
    
    public LightsGameTask getGameTask() {
        return this.gameTask;
    }
    
    public class LightsGameTask extends BukkitRunnable
    {
        public LightsGameTask() {
            LightsEvent.this.ticksPerRound = 150L;
            LightsEvent.this.status = -1;
            LightsEvent.this.countdown = 3;
        }
        
        public void run() {
            if (LightsEvent.this.getPlayers().size() == 1) {
                final Player winner = Bukkit.getPlayer((UUID)LightsEvent.this.getByState(LightsPlayer.LightsState.INGAME).get(0));
                if (winner != null) {
                    final PlayerData winnerData = Practice.getInstance().getPlayerManager().getPlayerData(winner.getUniqueId());
                    winnerData.setWaterDropEventWins(winnerData.getWaterDropEventWins() + 1);
                    Bukkit.broadcastMessage(ChatColor.DARK_RED + winner.getName() + ChatColor.WHITE + " has won a " + ChatColor.DARK_RED + LightsEvent.this.getName() + ChatColor.WHITE + " event!");
                    final Profile winnerProfile = Profile.getByUuid(winner.getUniqueId());
                    winnerProfile.awardCoins(winner, 15);
                    winner.sendMessage(CC.GOLD + "You earn 15 coins for winning the event!");
                }
                LightsEvent.this.end();
                this.cancel();
                return;
            }
            if (LightsEvent.this.status == -1) {
                if (LightsEvent.this.countdown == 3) {
                    PlayerUtil.sendMessage(ChatColor.YELLOW + "The game starts in " + ChatColor.GREEN + 3 + ChatColor.YELLOW + "...", LightsEvent.this.getBukkitPlayers());
                }
                else if (LightsEvent.this.countdown == 2) {
                    PlayerUtil.sendMessage(ChatColor.YELLOW + "The game starts in " + ChatColor.GREEN + 2 + ChatColor.YELLOW + "...", LightsEvent.this.getBukkitPlayers());
                }
                else if (LightsEvent.this.countdown == 1) {
                    PlayerUtil.sendMessage(ChatColor.YELLOW + "The game starts in " + ChatColor.GREEN + 1 + ChatColor.YELLOW + "...", LightsEvent.this.getBukkitPlayers());
                }
                else if (LightsEvent.this.countdown == 0) {
                    PlayerUtil.sendMessage(ChatColor.GREEN + "The game has started, good luck!", LightsEvent.this.getBukkitPlayers());
                    LightsEvent.this.status++;
                    for (final LightsPlayer player : LightsEvent.this.getPlayers().values()) {
                        player.setState(LightsPlayer.LightsState.INGAME);
                    }
                    for (final Player online : LightsEvent.this.getBukkitPlayers()) {
                        LightsEvent.this.teleportToSpawn(online);
                    }
                    Bukkit.getScheduler().cancelTask(LightsEvent.this.taskId);
                    LightsEvent.this.taskId = Bukkit.getScheduler().runTaskLaterAsynchronously((Plugin)LightsEvent.this.getPlugin(), (BukkitRunnable)this, 10L).getTaskId();
                }
                LightsEvent.this.countdown--;
            }
            else if (LightsEvent.this.status == 0) {
                LightsEvent.this.movingPlayers.clear();
                LightsEvent.this.status++;
                LightsEvent.this.sendMessage(ChatColor.GRAY + "[" + ChatColor.YELLOW + "RedLightGreenLight" + ChatColor.GRAY + "] " + ChatColor.GREEN.toString() + ChatColor.BOLD + "GO GO GO");
                LightsEvent.this.setCurrent(LightsGameState.GREEN);
                LightsEvent.this.ticksPerRound -= LightsEvent.this.getRandomNumber() * 20;
                LightsEvent.this.timeLeft = System.currentTimeMillis() + LightsEvent.this.ticksPerRound / 20L * 1000L;
                Bukkit.getScheduler().cancelTask(LightsEvent.this.taskId);
                LightsEvent.this.taskId = Bukkit.getScheduler().runTaskLaterAsynchronously((Plugin)LightsEvent.this.getPlugin(), (BukkitRunnable)this, LightsEvent.this.ticksPerRound).getTaskId();
            }
            else if (LightsEvent.this.status == 1) {
                LightsEvent.this.sendMessage(ChatColor.GRAY + "[" + ChatColor.YELLOW + "RedLightGreenLight" + ChatColor.GRAY + "] " + ChatColor.YELLOW.toString() + ChatColor.BOLD + "SLOW DOWN");
                LightsEvent.this.setCurrent(LightsGameState.YELLOW);
                LightsEvent.this.status++;
                LightsEvent.this.taskId = Bukkit.getScheduler().runTaskLaterAsynchronously((Plugin)LightsEvent.this.getPlugin(), (BukkitRunnable)this, 30L).getTaskId();
            }
            else if (LightsEvent.this.status == 2) {
                LightsEvent.this.sendMessage(ChatColor.GRAY + "[" + ChatColor.YELLOW + "RedLightGreenLight" + ChatColor.GRAY + "] " + ChatColor.RED.toString() + ChatColor.BOLD + "STOP");
                LightsEvent.this.setCurrent(LightsGameState.RED);
                LightsEvent.this.status++;
                LightsEvent.this.taskId = Bukkit.getScheduler().runTaskLaterAsynchronously((Plugin)LightsEvent.this.getPlugin(), (BukkitRunnable)this, 50L).getTaskId();
            }
            else if (LightsEvent.this.status == 3) {
                LightsEvent.this.status = 0;
                LightsEvent.this.ticksPerRound = 150L;
                LightsEvent.this.taskId = Bukkit.getScheduler().runTaskLaterAsynchronously((Plugin)LightsEvent.this.getPlugin(), (BukkitRunnable)this, 10L).getTaskId();
            }
        }
    }
    
    public enum LightsGameState
    {
        GREEN, 
        YELLOW, 
        RED;
    }
}
