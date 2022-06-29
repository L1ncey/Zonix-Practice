package us.zonix.practice.events.tnttag;

import java.util.Collection;
import java.util.Arrays;
import org.bukkit.entity.Entity;
import org.bukkit.Sound;
import org.bukkit.Effect;
import org.apache.commons.lang.time.DurationFormatUtils;
import org.bukkit.scheduler.BukkitRunnable;
import java.util.Collections;
import org.bukkit.Bukkit;
import me.maiko.dexter.util.CC;
import me.maiko.dexter.profile.Profile;
import org.bukkit.ChatColor;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import us.zonix.practice.events.EventPlayer;
import us.zonix.practice.events.EventState;
import java.util.function.Consumer;
import java.util.Iterator;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import us.zonix.practice.Practice;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import us.zonix.practice.util.ItemBuilder;
import org.bukkit.Material;
import us.zonix.practice.events.EventCountdownTask;
import us.zonix.practice.CustomLocation;
import java.util.List;
import java.util.UUID;
import java.util.Map;
import us.zonix.practice.events.PracticeEvent;

public class TNTTagEvent extends PracticeEvent<TNTTagPlayer>
{
    private final Map<UUID, TNTTagPlayer> players;
    private final List<CustomLocation> spawnLocations;
    private final EventCountdownTask countdownTask;
    private TNTTagState tntTagState;
    private TNTTagTask task;
    private int round;
    
    public TNTTagEvent() {
        super("TNTTag", new ItemBuilder(Material.TNT).name("&cTNT Tag event").build(), true);
        this.players = (Map<UUID, TNTTagPlayer>)Maps.newHashMap();
        this.spawnLocations = (List<CustomLocation>)Lists.newArrayList();
        this.countdownTask = new EventCountdownTask(this);
        this.tntTagState = TNTTagState.NOT_STARTED;
        this.round = 0;
    }
    
    @Override
    public void onStart() {
        this.tntTagState = TNTTagState.PREPARING;
        this.task = new TNTTagTask(this);
        this.round = 0;
        this.task.runTaskTimer((Plugin)Practice.getInstance(), 20L, 20L);
        for (final Player bukkitPlayer : this.getBukkitPlayers()) {
            bukkitPlayer.teleport(Practice.getInstance().getSpawnManager().getTntTagSpawn().toBukkitLocation());
        }
    }
    
    @Override
    public Consumer<Player> onJoin() {
        return player -> this.players.put(player.getUniqueId(), new TNTTagPlayer(player.getUniqueId(), this));
    }
    
    @Override
    public Consumer<Player> onDeath() {
        final boolean wasTagged;
        final List<TNTTagPlayer> tntTagPlayers;
        final Profile profile;
        final Player winner;
        final String announce;
        final Profile winnerProfile;
        return player -> {
            if (this.getState() == EventState.STARTED) {
                wasTagged = this.getPlayer(player).isTagged();
                this.players.remove(player.getUniqueId());
                tntTagPlayers = this.getPlayers().values().stream().filter(EventPlayer::playerExists).filter(TNTTagPlayer::isTagged).collect((Collector<? super TNTTagPlayer, ?, List<TNTTagPlayer>>)Collectors.toList());
                if (wasTagged && tntTagPlayers.size() == 0) {
                    this.setTntTagState(TNTTagState.PREPARING);
                    this.task.time = 5;
                }
                this.sendMessage("&c" + player.getName() + " &ehas exploded!");
                player.sendMessage(ChatColor.RED + "You have exploded!");
                profile = Profile.getByUuid(player.getUniqueId());
                profile.awardCoins(player, 5);
                player.sendMessage(CC.GOLD + "You earn 5 coins for participating in the event.");
                this.getPlugin().getServer().getScheduler().runTaskLater((Plugin)this.getPlugin(), () -> {
                    this.getPlugin().getPlayerManager().sendToSpawnAndReset(player);
                    if (this.getPlayers().size() >= 2) {
                        this.getPlugin().getEventManager().addSpectatorTntTag(player, this.getPlugin().getPlayerManager().getPlayerData(player.getUniqueId()), this);
                    }
                    return;
                }, 20L);
                if (this.players.size() == 1) {
                    winner = this.players.values().stream().findFirst().get().getPlayer();
                    announce = ChatColor.DARK_RED + winner.getName() + ChatColor.WHITE + " has won our " + ChatColor.DARK_RED + "TNT Tag Event!";
                    Bukkit.broadcastMessage(announce);
                    winnerProfile = Profile.getByUuid(winner.getUniqueId());
                    winnerProfile.awardCoins(winner, 15);
                    winner.sendMessage(CC.GOLD + "You earn 15 coins for winning the event!");
                    Bukkit.broadcastMessage(announce);
                    this.end();
                }
            }
        };
    }
    
    @Override
    public List<CustomLocation> getSpawnLocations() {
        return Collections.singletonList(this.getPlugin().getSpawnManager().getTntTagLocation());
    }
    
    public List<CustomLocation> getGameLocation() {
        return Collections.singletonList(this.getPlugin().getSpawnManager().getTntTagLocation());
    }
    
    @Override
    public List<String> getScoreboardLines(final Player player) {
        final List<String> strings = (List<String>)Lists.newArrayList();
        strings.add(" &c* &fPlayers&7: " + this.players.size() + "/" + this.getLimit());
        final int countdown = this.countdownTask.getTimeUntilStart();
        if (countdown > 0 && countdown <= 60) {
            strings.add(" &c* &fStarting&7: " + countdown + "s");
        }
        if (this.round != 0) {
            strings.add(" &c* &fRound&7: " + this.round);
        }
        if (this.tntTagState == TNTTagState.PREPARING && this.task.time <= 3) {
            strings.add(" &c* &fNext Round&7: " + this.task.getTime() + "s");
        }
        else if (this.tntTagState == TNTTagState.RUNNING) {
            strings.add(" &c* &fTime Left&7: " + this.task.getTime() + "s");
        }
        return strings;
    }
    
    @Override
    public Map<UUID, TNTTagPlayer> getPlayers() {
        return this.players;
    }
    
    @Override
    public EventCountdownTask getCountdownTask() {
        return this.countdownTask;
    }
    
    public TNTTagState getTntTagState() {
        return this.tntTagState;
    }
    
    public TNTTagTask getTask() {
        return this.task;
    }
    
    public int getRound() {
        return this.round;
    }
    
    public void setTntTagState(final TNTTagState tntTagState) {
        this.tntTagState = tntTagState;
    }
    
    public void setTask(final TNTTagTask task) {
        this.task = task;
    }
    
    public void setRound(final int round) {
        this.round = round;
    }
    
    public static class TNTTagTask extends BukkitRunnable
    {
        public static final Map<Integer, Integer> TIME_MAP;
        public static final List<Integer> BROADCAST_TIMES;
        public static final int BLOW_UP_RADIUS = 1;
        public final TNTTagEvent event;
        public int previousTime;
        public int time;
        
        public void run() {
            if (this.event.getTntTagState() == TNTTagState.NOT_STARTED) {
                return;
            }
            if (this.event.getTntTagState() != TNTTagState.PREPARING) {
                if (TNTTagTask.BROADCAST_TIMES.contains(this.time)) {
                    this.event.sendMessage("&eAll tagged players will explode in &c" + DurationFormatUtils.formatDurationWords((long)(this.time * 1000), true, true) + "&e.");
                }
                if (this.time == 0) {
                    final List<TNTTagPlayer> tagged = this.event.getPlayers().values().stream().filter(TNTTagPlayer::isTagged).collect((Collector<? super TNTTagPlayer, ?, List<TNTTagPlayer>>)Collectors.toList());
                    final Consumer<Player> deathConsumer = this.event.onDeath();
                    this.event.sendMessage("&c" + tagged.size() + " &eplayers have been removed from the game.");
                    this.event.setTntTagState(TNTTagState.PREPARING);
                    this.time = 5;
                    final Player bukkitPlayer;
                    final Consumer<Player> consumer;
                    final Player nearbyPlayer;
                    final Consumer<Player> consumer2;
                    tagged.stream().filter(EventPlayer::playerExists).forEach(player -> {
                        bukkitPlayer = player.getPlayer();
                        bukkitPlayer.getWorld().playEffect(bukkitPlayer.getLocation(), Effect.EXPLOSION_LARGE, 1, 1);
                        bukkitPlayer.getWorld().playSound(bukkitPlayer.getLocation(), Sound.EXPLODE, 1.0f, 1.0f);
                        consumer.accept(bukkitPlayer);
                        if (this.event.players.size() <= 1) {
                            return;
                        }
                        else {
                            bukkitPlayer.getNearbyEntities(1.0, 1.0, 1.0).stream().filter(entity -> this.event.getPlayers().containsKey(entity.getUniqueId())).map(entity -> this.event.getPlayers().get(entity.getUniqueId())).forEach(nearby -> {
                                nearbyPlayer = nearby.getPlayer();
                                nearbyPlayer.getWorld().playEffect(nearbyPlayer.getLocation(), Effect.EXPLOSION_LARGE, 1, 1);
                                nearbyPlayer.getWorld().playSound(nearbyPlayer.getLocation(), Sound.EXPLODE, 1.0f, 1.0f);
                                consumer2.accept(nearbyPlayer);
                                if (this.event.players.size() > 1) {
                                    nearbyPlayer.sendMessage(ChatColor.RED + "You died because you were to close to another tagged player.");
                                }
                            });
                            return;
                        }
                    });
                }
                --this.time;
                return;
            }
            if (this.time == 0) {
                final List<TNTTagPlayer> players = this.event.getPlayers().values().stream().filter(EventPlayer::playerExists).filter(player -> !player.isTagged()).collect((Collector<? super TNTTagPlayer, ?, List<TNTTagPlayer>>)Collectors.toList());
                final int size = (int)Math.round(players.size() / 2.5);
                this.event.setRound(this.event.getRound() + 1);
                this.event.sendMessage("&eRound &c" + this.event.getRound() + " &ehas started!", "&f" + size + " &7players have been tagged!");
                Collections.shuffle(players);
                for (int i = 0; i < players.size() && i < size; ++i) {
                    final TNTTagPlayer tagPlayer = players.get(i);
                    tagPlayer.setTagged(true);
                }
                players.forEach(player -> {
                    player.update();
                    if (this.event.getRound() > 1 && this.event.getPlayers().size() <= 8) {
                        player.getPlayer().teleport(Practice.getInstance().getSpawnManager().getTntTagSpawn().toBukkitLocation());
                    }
                    return;
                });
                this.event.setTntTagState(TNTTagState.RUNNING);
                this.time = TNTTagTask.TIME_MAP.getOrDefault(this.previousTime, 5);
                this.previousTime = this.time;
                return;
            }
            this.event.sendMessage("&eRound &c" + (this.event.getRound() + 1) + " &ewill start in &a" + this.time + " &eseconds.");
            --this.time;
        }
        
        public TNTTagEvent getEvent() {
            return this.event;
        }
        
        public int getPreviousTime() {
            return this.previousTime;
        }
        
        public int getTime() {
            return this.time;
        }
        
        public TNTTagTask(final TNTTagEvent event) {
            this.previousTime = 90;
            this.time = 3;
            this.event = event;
        }
        
        static {
            TIME_MAP = Maps.newHashMap();
            BROADCAST_TIMES = Lists.newArrayList();
            TNTTagTask.TIME_MAP.put(90, 60);
            TNTTagTask.TIME_MAP.put(60, 50);
            TNTTagTask.TIME_MAP.put(50, 30);
            TNTTagTask.TIME_MAP.put(30, 20);
            TNTTagTask.TIME_MAP.put(20, 15);
            TNTTagTask.TIME_MAP.put(15, 10);
            TNTTagTask.TIME_MAP.put(10, 5);
            TNTTagTask.TIME_MAP.put(5, 5);
            TNTTagTask.BROADCAST_TIMES.addAll(Arrays.asList(60, 30, 20, 15, 10, 5, 4, 3, 2, 1));
        }
    }
    
    public enum TNTTagState
    {
        RUNNING, 
        PREPARING, 
        NOT_STARTED;
    }
}
