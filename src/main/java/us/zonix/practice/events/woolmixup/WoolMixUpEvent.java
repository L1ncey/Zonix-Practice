package us.zonix.practice.events.woolmixup;

import us.zonix.practice.util.PlayerUtil;
import org.bukkit.scheduler.BukkitRunnable;
import java.util.Collection;
import java.text.DecimalFormat;
import us.zonix.practice.util.DateUtil;
import com.google.common.collect.Lists;
import org.bukkit.block.Block;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.Vector;
import com.boydti.fawe.util.EditSessionBuilder;
import com.boydti.fawe.util.TaskManager;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.function.Function;
import us.zonix.practice.events.EventPlayer;
import java.util.Iterator;
import us.zonix.practice.player.PlayerData;
import org.bukkit.Bukkit;
import us.zonix.practice.Practice;
import me.maiko.dexter.util.CC;
import me.maiko.dexter.profile.Profile;
import org.bukkit.entity.Player;
import java.util.function.Consumer;
import java.util.ArrayList;
import org.bukkit.plugin.Plugin;
import java.util.Collections;
import us.zonix.practice.CustomLocation;
import us.zonix.practice.util.ItemUtil;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import java.util.Random;
import java.util.List;
import us.zonix.practice.events.EventCountdownTask;
import org.bukkit.Location;
import java.util.HashMap;
import java.util.UUID;
import java.util.Map;
import us.zonix.practice.events.PracticeEvent;

public class WoolMixUpEvent extends PracticeEvent<WoolMixUpPlayer>
{
    private final Map<UUID, WoolMixUpPlayer> players;
    private Map<Integer, String> colors;
    private HashMap<Location, Integer> blocks;
    private HashMap<Location, Integer> blocksRegen;
    private WoolMixUpGameTask gameTask;
    private final EventCountdownTask countdownTask;
    private List<UUID> visibility;
    private int countdown;
    private int taskId;
    private int currentColor;
    private long ticksPerRound;
    private int status;
    private boolean isShuffling;
    private int round;
    private long timeLeft;
    private Random random;
    
    public WoolMixUpEvent() {
        super("BlockParty", ItemUtil.createItem(Material.WOOL, ChatColor.RED + "BlockParty Event"), true);
        this.players = new HashMap<UUID, WoolMixUpPlayer>();
        this.colors = new HashMap<Integer, String>() {
            {
                this.put(11, ChatColor.BLUE.toString() + ChatColor.BOLD + "BLUE");
                this.put(14, ChatColor.RED.toString() + ChatColor.BOLD + "RED");
                this.put(9, ChatColor.DARK_AQUA.toString() + ChatColor.BOLD + "CYAN");
                this.put(15, ChatColor.BLACK.toString() + ChatColor.BOLD + "BLACK");
                this.put(13, ChatColor.GREEN.toString() + ChatColor.BOLD + "GREEN");
                this.put(4, ChatColor.YELLOW.toString() + ChatColor.BOLD + "YELLOW");
                this.put(1, ChatColor.GOLD.toString() + ChatColor.BOLD + "ORANGE");
                this.put(10, ChatColor.DARK_PURPLE.toString() + ChatColor.BOLD + "PURPLE");
                this.put(0, ChatColor.WHITE.toString() + ChatColor.BOLD + "WHITE");
                this.put(6, ChatColor.LIGHT_PURPLE.toString() + ChatColor.BOLD + "PINK");
            }
        };
        this.blocks = new HashMap<Location, Integer>();
        this.blocksRegen = new HashMap<Location, Integer>();
        this.gameTask = null;
        this.countdownTask = new EventCountdownTask(this);
        this.currentColor = -1;
        this.random = new Random();
    }
    
    @Override
    public Map<UUID, WoolMixUpPlayer> getPlayers() {
        return this.players;
    }
    
    @Override
    public EventCountdownTask getCountdownTask() {
        return this.countdownTask;
    }
    
    @Override
    public List<CustomLocation> getSpawnLocations() {
        return Collections.singletonList(this.getPlugin().getSpawnManager().getWoolLocation());
    }
    
    @Override
    public void onStart() {
        (this.gameTask = new WoolMixUpGameTask()).runTaskTimerAsynchronously((Plugin)this.getPlugin(), 0L, 20L);
        this.taskId = this.gameTask.getTaskId();
        this.visibility = new ArrayList<UUID>();
        this.blocks.clear();
        this.blocksRegen.clear();
        this.setupAllBlocks();
        this.currentColor = -1;
        this.countdown = 0;
        this.ticksPerRound = 200L;
        this.status = -1;
        this.isShuffling = false;
        this.round = 0;
        this.timeLeft = 0L;
    }
    
    @Override
    public Consumer<Player> onJoin() {
        return player -> {
            this.players.put(player.getUniqueId(), new WoolMixUpPlayer(player.getUniqueId(), this));
            this.getPlayer(player).setState(WoolMixUpPlayer.WoolMixUpState.LOBBY);
        };
    }
    
    @Override
    public Consumer<Player> onDeath() {
        final WoolMixUpPlayer data;
        final Profile profile;
        final Player winner;
        final PlayerData winnerData;
        final String announce;
        final Profile winnerProfile;
        return player -> {
            data = this.getPlayer(player);
            if (data.getState() != WoolMixUpPlayer.WoolMixUpState.LOBBY) {
                this.getPlayers().remove(player.getUniqueId());
                profile = Profile.getByUuid(player.getUniqueId());
                profile.awardCoins(player, 5);
                player.sendMessage(CC.GOLD + "You earn 5 coins for participating in the event.");
                this.sendMessage(ChatColor.GRAY + "[" + ChatColor.YELLOW + "Round " + this.round + ChatColor.GRAY + "] " + ChatColor.RED + player.getName() + " has been eliminated from the game.");
                this.getPlugin().getServer().getScheduler().runTaskLater((Plugin)this.getPlugin(), () -> {
                    this.getPlugin().getPlayerManager().sendToSpawnAndReset(player);
                    if (this.getPlayers().size() >= 2) {
                        this.getPlugin().getEventManager().addSpectatorWoolMixUp(player, this.getPlugin().getPlayerManager().getPlayerData(player.getUniqueId()), this);
                    }
                    return;
                }, 20L);
                if (this.getPlayers().size() == 1) {
                    winner = this.players.values().stream().findFirst().get().getPlayer();
                    winnerData = Practice.getInstance().getPlayerManager().getPlayerData(winner.getUniqueId());
                    winnerData.setWaterDropEventWins(winnerData.getWaterDropEventWins() + 1);
                    announce = ChatColor.DARK_RED + winner.getName() + ChatColor.WHITE + " has won our " + ChatColor.DARK_RED + "BlockParty" + ChatColor.WHITE + " event!";
                    winnerProfile = Profile.getByUuid(winner.getUniqueId());
                    winnerProfile.awardCoins(winner, 15);
                    winner.sendMessage(CC.GOLD + "You earn 15 coins for winning the event!");
                    Bukkit.broadcastMessage(announce);
                    this.isShuffling = false;
                    this.end();
                }
            }
        };
    }
    
    public void setCurrentColor(final int currentColor) {
        this.currentColor = currentColor;
    }
    
    public void setTaskId(final int taskId) {
        this.taskId = taskId;
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
        Bukkit.getServer().getScheduler().runTask((Plugin)this.getPlugin(), () -> player.teleport(this.getPlugin().getSpawnManager().getWoolCenter().toBukkitLocation().clone().add(0.0, 2.0, 0.0)));
    }
    
    private void giveItems(final Player player) {
        int i;
        this.getPlugin().getServer().getScheduler().runTask((Plugin)this.getPlugin(), () -> {
            if (this.currentColor >= 0 && this.isShuffling) {
                for (i = 0; i <= 6; ++i) {
                    player.getInventory().setItem(i, ItemUtil.createItem(Material.WOOL, this.colors.get(this.currentColor), 1, (short)this.currentColor));
                }
            }
            player.getInventory().setItem(7, ItemUtil.createItem(Material.FIREBALL, ChatColor.GREEN.toString() + "Toggle Visibility"));
            player.getInventory().setItem(8, ItemUtil.createItem(Material.NETHER_STAR, ChatColor.RED.toString() + "Leave Event"));
            player.updateInventory();
        });
    }
    
    public List<UUID> getByState(final WoolMixUpPlayer.WoolMixUpState state) {
        return this.players.values().stream().filter(player -> player.getState() == state).map((Function<? super WoolMixUpPlayer, ?>)EventPlayer::getUuid).collect((Collector<? super Object, ?, List<UUID>>)Collectors.toList());
    }
    
    public int getRound() {
        return this.round;
    }
    
    public HashMap<Location, Integer> getBlocksRegen() {
        return this.blocksRegen;
    }
    
    public int getTaskId() {
        return this.taskId;
    }
    
    public void generateArena(final Location location) {
        final int x = location.getBlockX() - 32;
        final int y = location.getBlockY();
        final int y_ = location.getBlockY() - 4;
        final int z = location.getBlockZ() - 32;
        int current = 0;
        final List<Integer> data = this.getDataFromWool(location);
        for (int i = 0; i < 16; ++i) {
            for (int j = 0; j < 16; ++j) {
                final int x_ = x + i * 4;
                final int z_ = z + j * 4;
                int newCurrent = this.getRandomColor();
                if (current == newCurrent) {
                    if (newCurrent > 0) {
                        --newCurrent;
                    }
                    else {
                        newCurrent += 2;
                    }
                }
                if (data.size() > 15 && data.get(data.size() - 16) == newCurrent) {
                    if (newCurrent > 0) {
                        --newCurrent;
                    }
                    else {
                        newCurrent += 2;
                    }
                }
                current = newCurrent;
                data.add(current);
                final int finalCurrent = current;
                final EditSession editSession;
                int i_;
                int j_;
                final int n;
                final int n2;
                final int n3;
                Block b;
                final int n4;
                Block b_;
                final int n5;
                TaskManager.IMP.async(() -> {
                    editSession = new EditSessionBuilder(location.getWorld().getName()).fastmode(true).allowedRegionsEverywhere().autoQueue(false).limitUnlimited().build();
                    for (i_ = 0; i_ < 4; ++i_) {
                        for (j_ = 0; j_ < 4; ++j_) {
                            b = location.getWorld().getBlockAt(new Location(location.getWorld(), (double)(n + i_), (double)n2, (double)(n3 + j_)));
                            b_ = location.getWorld().getBlockAt(new Location(location.getWorld(), (double)(n + i_), (double)n4, (double)(n3 + j_)));
                            try {
                                editSession.setBlock(new Vector((double)b.getLocation().getBlockX(), (double)b.getLocation().getBlockY(), b.getLocation().getZ()), new BaseBlock(35, n5));
                                editSession.setBlock(new Vector((double)b_.getLocation().getBlockX(), (double)b_.getLocation().getBlockY(), b_.getLocation().getZ()), new BaseBlock(7, 0));
                            }
                            catch (MaxChangedBlocksException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    editSession.flushQueue();
                    return;
                });
            }
        }
    }
    
    private void setupAllBlocks() {
        final int minX = this.getPlugin().getSpawnManager().getWoolMin().toBukkitLocation().getBlockX();
        final int minY = this.getPlugin().getSpawnManager().getWoolMin().toBukkitLocation().getBlockY();
        final int minZ = this.getPlugin().getSpawnManager().getWoolMin().toBukkitLocation().getBlockZ();
        final int maxX = this.getPlugin().getSpawnManager().getWoolMax().toBukkitLocation().getBlockX();
        final int maxY = this.getPlugin().getSpawnManager().getWoolMax().toBukkitLocation().getBlockY();
        final int maxZ = this.getPlugin().getSpawnManager().getWoolMax().toBukkitLocation().getBlockZ();
        for (int x = minX; x <= maxX; ++x) {
            for (int y = minY; y <= maxY; ++y) {
                for (int z = minZ; z <= maxZ; ++z) {
                    final Location loc = new Location(this.getPlugin().getSpawnManager().getWoolCenter().toBukkitWorld(), (double)x, (double)y, (double)z);
                    if (loc.getBlock().getType() == Material.WOOL) {
                        this.blocks.put(loc.getBlock().getLocation(), (int)loc.getBlock().getData());
                    }
                }
            }
        }
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
        if (this.getPlayer(player) != null && this.getPlayer(player).getState() == WoolMixUpPlayer.WoolMixUpState.INGAME) {
            final String color = this.getCurrentColor();
            final String timeLeft = this.getTimeLeft();
            strings.add(ChatColor.RED.toString() + ChatColor.BOLD + " * " + ChatColor.WHITE + "Round§7: " + this.getRound());
            strings.add(ChatColor.RED.toString() + ChatColor.BOLD + " * " + ChatColor.WHITE + "Color§7: " + color);
            if (!timeLeft.contains("-")) {
                strings.add(ChatColor.RED.toString() + ChatColor.BOLD + " * " + ChatColor.WHITE + "Time Left§7: " + timeLeft);
            }
        }
        return strings;
    }
    
    private void removeAllExceptOne(final int currentColor) {
        final HashMap<Location, Integer> blocksToRemove = new HashMap<Location, Integer>();
        for (final Map.Entry<Location, Integer> entry : this.blocks.entrySet()) {
            if (entry.getKey().getBlock().getType() == Material.WOOL && entry.getValue() != currentColor) {
                blocksToRemove.put(entry.getKey().getBlock().getLocation(), entry.getValue());
            }
        }
        this.blocksRegen.clear();
        this.blocksRegen.putAll(blocksToRemove);
        final EditSession editSession;
        final HashMap<Location, V> hashMap;
        final Iterator<Location> iterator2;
        Location block;
        TaskManager.IMP.async(() -> {
            editSession = new EditSessionBuilder(this.getPlugin().getSpawnManager().getWoolCenter().getWorld()).fastmode(true).allowedRegionsEverywhere().autoQueue(false).limitUnlimited().build();
            hashMap.keySet().iterator();
            while (iterator2.hasNext()) {
                block = iterator2.next();
                try {
                    editSession.setBlock(new Vector((double)block.getBlockX(), (double)block.getBlockY(), block.getZ()), new BaseBlock(0, 0));
                }
                catch (MaxChangedBlocksException e) {
                    e.printStackTrace();
                }
            }
            editSession.flushQueue();
        });
    }
    
    public String getCurrentColor() {
        if (this.currentColor == -1) {
            return ChatColor.GRAY.toString() + ChatColor.BOLD + "None";
        }
        return this.colors.get(this.currentColor);
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
    
    public HashMap<Location, Integer> getBlocks() {
        return this.blocks;
    }
    
    public void regenerateArena(final HashMap<Location, Integer> blocksToAdd) {
        final EditSession editSession;
        final Iterator<Location> iterator;
        Location block;
        TaskManager.IMP.async(() -> {
            editSession = new EditSessionBuilder(this.getPlugin().getSpawnManager().getWoolCenter().getWorld()).fastmode(true).allowedRegionsEverywhere().autoQueue(false).limitUnlimited().build();
            try {
                blocksToAdd.keySet().iterator();
                while (iterator.hasNext()) {
                    block = iterator.next();
                    editSession.setBlock(new Vector(block.getBlockX(), block.getBlockY(), block.getBlockZ()), new BaseBlock(35, (int)blocksToAdd.get(block)));
                }
            }
            catch (MaxChangedBlocksException e) {
                e.printStackTrace();
            }
            editSession.flushQueue();
        });
    }
    
    private List<Integer> getDataFromWool(final Location location) {
        final List<Integer> woolData = new ArrayList<Integer>();
        final int x = location.getBlockX() - 32;
        final int y = location.getBlockY();
        final int z = location.getBlockZ() - 32;
        for (int i = 0; i < 16; ++i) {
            for (int j = 0; j < 16; ++j) {
                final int x_ = x + i * 4;
                final int z_ = z + j * 4;
                final Block b = location.getWorld().getBlockAt(new Location(location.getWorld(), (double)x_, (double)y, (double)z_));
                woolData.add((int)b.getData());
            }
        }
        return woolData;
    }
    
    private int getRandomColor() {
        final List<Integer> integers = new ArrayList<Integer>();
        if (this.colors != null) {
            integers.addAll(this.colors.keySet());
            Collections.shuffle(integers);
        }
        return integers.get(this.random.nextInt(integers.size()));
    }
    
    public WoolMixUpGameTask getGameTask() {
        return this.gameTask;
    }
    
    public class WoolMixUpGameTask extends BukkitRunnable
    {
        public WoolMixUpGameTask() {
            WoolMixUpEvent.this.ticksPerRound = 200L;
            WoolMixUpEvent.this.status = -1;
            WoolMixUpEvent.this.round = 0;
            WoolMixUpEvent.this.countdown = 3;
        }
        
        public void run() {
            if (WoolMixUpEvent.this.getPlayers().size() == 1) {
                final Player winner = WoolMixUpEvent.this.players.values().stream().findFirst().get().getPlayer();
                if (winner != null) {
                    final PlayerData winnerData = Practice.getInstance().getPlayerManager().getPlayerData(winner.getUniqueId());
                    winnerData.setWaterDropEventWins(winnerData.getWaterDropEventWins() + 1);
                    final String announce = ChatColor.DARK_RED + winner.getName() + ChatColor.WHITE + " has won our " + ChatColor.DARK_RED + "BlockParty" + ChatColor.WHITE + " event!";
                    final Profile winnerProfile = Profile.getByUuid(winner.getUniqueId());
                    winnerProfile.awardCoins(winner, 15);
                    winner.sendMessage(CC.GOLD + "You earn 15 coins for winning the event!");
                    Bukkit.broadcastMessage(announce);
                }
                WoolMixUpEvent.this.isShuffling = false;
                WoolMixUpEvent.this.end();
                this.cancel();
                return;
            }
            if (WoolMixUpEvent.this.status == -1) {
                if (WoolMixUpEvent.this.countdown == 3) {
                    PlayerUtil.sendMessage(ChatColor.YELLOW + "The game starts in " + ChatColor.GREEN + 3 + ChatColor.YELLOW + "...", WoolMixUpEvent.this.getBukkitPlayers());
                }
                else if (WoolMixUpEvent.this.countdown == 2) {
                    PlayerUtil.sendMessage(ChatColor.YELLOW + "The game starts in " + ChatColor.GREEN + 2 + ChatColor.YELLOW + "...", WoolMixUpEvent.this.getBukkitPlayers());
                }
                else if (WoolMixUpEvent.this.countdown == 1) {
                    PlayerUtil.sendMessage(ChatColor.YELLOW + "The game starts in " + ChatColor.GREEN + 1 + ChatColor.YELLOW + "...", WoolMixUpEvent.this.getBukkitPlayers());
                }
                else if (WoolMixUpEvent.this.countdown == 0) {
                    PlayerUtil.sendMessage(ChatColor.GREEN + "The game has started, good luck!", WoolMixUpEvent.this.getBukkitPlayers());
                    WoolMixUpEvent.this.status++;
                    for (final WoolMixUpPlayer player : WoolMixUpEvent.this.getPlayers().values()) {
                        player.setState(WoolMixUpPlayer.WoolMixUpState.INGAME);
                    }
                    for (final Player online : WoolMixUpEvent.this.getBukkitPlayers()) {
                        WoolMixUpEvent.this.teleportToSpawn(online);
                        WoolMixUpEvent.this.giveItems(online);
                    }
                    Bukkit.getScheduler().cancelTask(WoolMixUpEvent.this.taskId);
                    WoolMixUpEvent.this.taskId = Bukkit.getScheduler().runTaskLaterAsynchronously((Plugin)WoolMixUpEvent.this.getPlugin(), (BukkitRunnable)this, 10L).getTaskId();
                }
                WoolMixUpEvent.this.countdown--;
            }
            else if (WoolMixUpEvent.this.status == 0) {
                WoolMixUpEvent.this.status++;
                WoolMixUpEvent.this.round++;
                WoolMixUpEvent.this.countdown = 3;
                WoolMixUpEvent.this.currentColor = -1;
                WoolMixUpEvent.this.taskId = Bukkit.getScheduler().runTaskTimerAsynchronously((Plugin)WoolMixUpEvent.this.getPlugin(), (BukkitRunnable)this, 0L, 20L).getTaskId();
            }
            else if (WoolMixUpEvent.this.status == 1) {
                WoolMixUpEvent.this.sendMessage(ChatColor.GRAY + "[" + ChatColor.YELLOW + "Round " + WoolMixUpEvent.this.round + ChatColor.GRAY + "] " + ChatColor.GRAY + "Choosing color in " + ChatColor.RED.toString() + ChatColor.BOLD + WoolMixUpEvent.this.countdown);
                WoolMixUpEvent.this.countdown--;
                if (WoolMixUpEvent.this.countdown == 0) {
                    WoolMixUpEvent.this.status++;
                    Bukkit.getScheduler().cancelTask(WoolMixUpEvent.this.taskId);
                    WoolMixUpEvent.this.taskId = Bukkit.getScheduler().runTaskLaterAsynchronously((Plugin)WoolMixUpEvent.this.getPlugin(), (BukkitRunnable)this, 10L).getTaskId();
                }
            }
            else if (WoolMixUpEvent.this.status == 2) {
                WoolMixUpEvent.this.currentColor = WoolMixUpEvent.this.getRandomColor();
                WoolMixUpEvent.this.sendMessage(ChatColor.GRAY + "[" + ChatColor.YELLOW + "Round " + WoolMixUpEvent.this.round + ChatColor.GRAY + "] " + ChatColor.GRAY + "The next color is " + WoolMixUpEvent.this.colors.get(WoolMixUpEvent.this.currentColor));
                WoolMixUpEvent.this.isShuffling = true;
                for (final Player online : WoolMixUpEvent.this.getBukkitPlayers()) {
                    WoolMixUpEvent.this.giveItems(online);
                }
                if (WoolMixUpEvent.this.round > 1 && WoolMixUpEvent.this.round <= 8) {
                    WoolMixUpEvent.this.ticksPerRound -= (long)(WoolMixUpEvent.this.ticksPerRound * 0.3);
                }
                WoolMixUpEvent.this.timeLeft = System.currentTimeMillis() + WoolMixUpEvent.this.ticksPerRound / 20L * 1000L;
                WoolMixUpEvent.this.status++;
                WoolMixUpEvent.this.taskId = Bukkit.getScheduler().runTaskLaterAsynchronously((Plugin)WoolMixUpEvent.this.getPlugin(), (BukkitRunnable)this, WoolMixUpEvent.this.ticksPerRound).getTaskId();
            }
            else if (WoolMixUpEvent.this.status == 3) {
                WoolMixUpEvent.this.isShuffling = false;
                for (final Player online : WoolMixUpEvent.this.getBukkitPlayers()) {
                    WoolMixUpEvent.this.giveItems(online);
                }
                WoolMixUpEvent.this.removeAllExceptOne(WoolMixUpEvent.this.currentColor);
                WoolMixUpEvent.this.status++;
                WoolMixUpEvent.this.taskId = Bukkit.getScheduler().runTaskLaterAsynchronously((Plugin)WoolMixUpEvent.this.getPlugin(), (BukkitRunnable)this, 60L).getTaskId();
            }
            else if (WoolMixUpEvent.this.status == 4) {
                WoolMixUpEvent.this.regenerateArena(WoolMixUpEvent.this.blocksRegen);
                WoolMixUpEvent.this.status = 0;
                WoolMixUpEvent.this.taskId = Bukkit.getScheduler().runTaskLaterAsynchronously((Plugin)WoolMixUpEvent.this.getPlugin(), (BukkitRunnable)this, 5L).getTaskId();
            }
        }
    }
}
