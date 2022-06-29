package us.zonix.practice.events.sumo;

import java.util.Arrays;
import org.bukkit.scheduler.BukkitRunnable;
import java.util.StringJoiner;
import org.apache.commons.lang.StringUtils;
import com.google.common.collect.Lists;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.function.Function;
import us.zonix.practice.events.EventPlayer;
import java.util.concurrent.ThreadLocalRandom;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.plugin.Plugin;
import java.util.function.Consumer;
import org.bukkit.entity.Player;
import java.util.Iterator;
import us.zonix.practice.util.PlayerUtil;
import org.bukkit.Bukkit;
import java.util.Collections;
import us.zonix.practice.CustomLocation;
import java.util.List;
import java.util.HashMap;
import us.zonix.practice.util.ItemUtil;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import us.zonix.practice.events.EventCountdownTask;
import java.util.HashSet;
import java.util.UUID;
import java.util.Map;
import us.zonix.practice.events.PracticeEvent;

public class SumoEvent extends PracticeEvent<SumoPlayer>
{
    private final Map<UUID, SumoPlayer> players;
    final HashSet<String> fighting;
    private final EventCountdownTask countdownTask;
    int round;
    
    public SumoEvent() {
        super("Sumo", ItemUtil.createItem(Material.LEASH, ChatColor.RED + "Sumo Event"), true);
        this.players = new HashMap<UUID, SumoPlayer>();
        this.fighting = new HashSet<String>();
        this.countdownTask = new EventCountdownTask(this);
    }
    
    @Override
    public Map<UUID, SumoPlayer> getPlayers() {
        return this.players;
    }
    
    @Override
    public EventCountdownTask getCountdownTask() {
        return this.countdownTask;
    }
    
    @Override
    public List<CustomLocation> getSpawnLocations() {
        return Collections.singletonList(this.getPlugin().getSpawnManager().getSumoLocation());
    }
    
    @Override
    public void onStart() {
        this.selectPlayers();
        for (final UUID playerUUID : this.players.keySet()) {
            final Player player = Bukkit.getPlayer(playerUUID);
            if (player != null) {
                PlayerUtil.clearPlayer(player);
            }
        }
    }
    
    @Override
    public Consumer<Player> onJoin() {
        return player -> this.players.put(player.getUniqueId(), new SumoPlayer(player.getUniqueId(), this));
    }
    
    @Override
    public Consumer<Player> onDeath() {
        final SumoPlayer data;
        final SumoPlayer killerData;
        final Player killer;
        final String[] messages;
        String string;
        final Object o;
        final StringBuilder sb;
        final Player winner;
        final String announce;
        return player -> {
            data = this.getPlayer(player);
            if (data == null || data.getFighting() == null) {
                System.out.println("data is null");
            }
            else if (data.getState() == SumoPlayer.SumoState.FIGHTING || data.getState() == SumoPlayer.SumoState.PREPARING) {
                killerData = data.getFighting();
                killer = this.getPlugin().getServer().getPlayer(killerData.getUuid());
                data.getFightTask().cancel();
                killerData.getFightTask().cancel();
                data.setState(SumoPlayer.SumoState.ELIMINATED);
                killerData.setState(SumoPlayer.SumoState.WAITING);
                PlayerUtil.clearPlayer(player);
                this.getPlugin().getPlayerManager().giveLobbyItems(player);
                PlayerUtil.clearPlayer(killer);
                this.getPlugin().getPlayerManager().giveLobbyItems(killer);
                if (this.getSpawnLocations().size() == 1) {
                    player.teleport(this.getSpawnLocations().get(0).toBukkitLocation());
                    killer.teleport(this.getSpawnLocations().get(0).toBukkitLocation());
                }
                messages = new String[] { null };
                new StringBuilder().append(ChatColor.RED).append("[Event] ").append(ChatColor.RED).append(player.getName()).append(ChatColor.GRAY).append(" has been eliminated");
                if (killer == null) {
                    string = ".";
                }
                else {
                    string = " by " + ChatColor.GREEN + killer.getName();
                }
                messages[o] = sb.append(string).toString();
                this.sendMessage(messages);
                if (this.getByState(SumoPlayer.SumoState.WAITING).size() == 1) {
                    winner = Bukkit.getPlayer((UUID)this.getByState(SumoPlayer.SumoState.WAITING).stream().findFirst().get());
                    if (winner != null) {
                        announce = ChatColor.DARK_RED + winner.getName() + ChatColor.WHITE + " has won our " + ChatColor.DARK_RED + "Sumo" + ChatColor.WHITE + " event!";
                        Bukkit.broadcastMessage(announce);
                    }
                    this.fighting.clear();
                    this.end();
                }
                else {
                    this.getPlugin().getServer().getScheduler().runTaskLater((Plugin)this.getPlugin(), () -> this.selectPlayers(), 60L);
                }
            }
        };
    }
    
    private CustomLocation[] getSumoLocations() {
        final CustomLocation[] array = { this.getPlugin().getSpawnManager().getSumoFirst(), this.getPlugin().getSpawnManager().getSumoSecond() };
        return array;
    }
    
    private void selectPlayers() {
        if (this.getByState(SumoPlayer.SumoState.WAITING).size() == 1) {
            final Player winner = Bukkit.getPlayer((UUID)this.getByState(SumoPlayer.SumoState.WAITING).get(0));
            final String announce = ChatColor.DARK_RED + winner.getName() + ChatColor.WHITE + " has won our " + ChatColor.DARK_RED + "Sumo" + ChatColor.WHITE + " event!";
            Bukkit.broadcastMessage(announce);
            this.fighting.clear();
            this.end();
            return;
        }
        this.sendMessage(ChatColor.RED + "[Event] " + ChatColor.GRAY + "Selecting random players...");
        this.fighting.clear();
        final Player picked1 = this.getRandomPlayer();
        final Player picked2 = this.getRandomPlayer();
        final SumoPlayer picked1Data = this.getPlayer(picked1);
        final SumoPlayer picked2Data = this.getPlayer(picked2);
        picked1Data.setFighting(picked2Data);
        picked2Data.setFighting(picked1Data);
        this.fighting.add(picked1.getName());
        this.fighting.add(picked2.getName());
        PlayerUtil.clearPlayer(picked1);
        PlayerUtil.clearPlayer(picked2);
        picked1.teleport(this.getSumoLocations()[0].toBukkitLocation());
        picked2.teleport(this.getSumoLocations()[1].toBukkitLocation());
        for (final Player other : this.getBukkitPlayers()) {
            if (other != null) {
                other.showPlayer(picked1);
                other.showPlayer(picked2);
            }
        }
        for (final UUID spectatorUUID : this.getPlugin().getEventManager().getSpectators().keySet()) {
            final Player spectator = Bukkit.getPlayer(spectatorUUID);
            if (spectatorUUID != null) {
                spectator.showPlayer(picked1);
                spectator.showPlayer(picked2);
            }
        }
        picked1.showPlayer(picked2);
        picked2.showPlayer(picked1);
        this.sendMessage(ChatColor.YELLOW + "Starting event match. " + ChatColor.GREEN + "(" + picked1.getName() + " vs " + picked2.getName() + ")");
        ++this.round;
        final BukkitTask task = new SumoFightTask(picked1, picked2, picked1Data, picked2Data).runTaskTimer((Plugin)this.getPlugin(), 0L, 20L);
        picked1Data.setFightTask(task);
        picked2Data.setFightTask(task);
    }
    
    private Player getRandomPlayer() {
        final List<UUID> waiting = this.getByState(SumoPlayer.SumoState.WAITING);
        Collections.shuffle(waiting);
        final UUID uuid = waiting.get(ThreadLocalRandom.current().nextInt(waiting.size()));
        final SumoPlayer data = this.getPlayer(uuid);
        data.setState(SumoPlayer.SumoState.PREPARING);
        return this.getPlugin().getServer().getPlayer(uuid);
    }
    
    public List<UUID> getByState(final SumoPlayer.SumoState state) {
        return this.players.values().stream().filter(player -> player.getState() == state).map((Function<? super SumoPlayer, ?>)EventPlayer::getUuid).collect((Collector<? super Object, ?, List<UUID>>)Collectors.toList());
    }
    
    @Override
    public List<String> getScoreboardLines(final Player player) {
        final List<String> strings = (List<String>)Lists.newArrayList();
        strings.add(" &c* &fPlayers&7: " + this.players.size() + "/" + this.getLimit());
        final int countdown = this.countdownTask.getTimeUntilStart();
        if (countdown > 0 && countdown <= 60) {
            strings.add(" &c* &fStarting&7: " + countdown + "s");
        }
        if (this.getPlayer(player) != null) {
            final SumoPlayer sumoPlayer = this.getPlayer(player);
            strings.add(" &c* &fState&7: " + StringUtils.capitalize(sumoPlayer.getState().name().toLowerCase()));
        }
        if (this.getFighting().size() > 0) {
            final StringJoiner nameJoiner = new StringJoiner(ChatColor.WHITE + " vs " + ChatColor.WHITE);
            final StringJoiner pingJoiner = new StringJoiner(" \u2503 ");
            final StringJoiner cpsJoiner = new StringJoiner(" \u2503 ");
            for (final String fighterName : this.getFighting()) {
                nameJoiner.add("&f" + fighterName);
                final Player fighter = Bukkit.getPlayer(fighterName);
                if (fighter != null) {
                    pingJoiner.add(ChatColor.GRAY + "(" + ChatColor.RED + PlayerUtil.getPing(fighter) + "ms" + ChatColor.GRAY + ")");
                    cpsJoiner.add(ChatColor.GRAY + "(" + ChatColor.RED + this.getPlugin().getPlayerManager().getPlayerData(fighter.getUniqueId()).getCps() + "CPS" + ChatColor.GRAY + ")");
                }
            }
            strings.add(ChatColor.GRAY.toString() + ChatColor.STRIKETHROUGH + "-------------------");
            strings.add(ChatColor.RED + nameJoiner.toString());
            strings.add(pingJoiner.toString());
            strings.add(cpsJoiner.toString());
        }
        return strings;
    }
    
    public HashSet<String> getFighting() {
        return this.fighting;
    }
    
    public int getRound() {
        return this.round;
    }
    
    public class SumoFightTask extends BukkitRunnable
    {
        private final Player player;
        private final Player other;
        private final SumoPlayer playerSumo;
        private final SumoPlayer otherSumo;
        private int time;
        
        public void run() {
            if (this.player == null || this.other == null || !this.player.isOnline() || !this.other.isOnline()) {
                this.cancel();
                return;
            }
            if (this.time == 90) {
                SumoEvent.this.sendMessage(ChatColor.GRAY + "[" + ChatColor.YELLOW + "Round " + SumoEvent.this.round + ChatColor.GRAY + "] " + ChatColor.GOLD + "The match starts in " + ChatColor.GREEN + 3 + ChatColor.YELLOW + "...");
            }
            else if (this.time == 89) {
                SumoEvent.this.sendMessage(ChatColor.GRAY + "[" + ChatColor.YELLOW + "Round " + SumoEvent.this.round + ChatColor.GRAY + "] " + ChatColor.GOLD + "The match starts in " + ChatColor.GREEN + 2 + ChatColor.YELLOW + "...");
            }
            else if (this.time == 88) {
                SumoEvent.this.sendMessage(ChatColor.GRAY + "[" + ChatColor.YELLOW + "Round " + SumoEvent.this.round + ChatColor.GRAY + "] " + ChatColor.GOLD + "The match starts in " + ChatColor.GREEN + 1 + ChatColor.YELLOW + "...");
            }
            else if (this.time == 87) {
                SumoEvent.this.sendMessage(ChatColor.GRAY + "[" + ChatColor.YELLOW + "Round " + SumoEvent.this.round + ChatColor.GRAY + "] " + ChatColor.GOLD + "The match has started.");
                SumoEvent.this.sendMessage(ChatColor.GRAY + "[" + ChatColor.YELLOW + "Round " + SumoEvent.this.round + ChatColor.GRAY + "] " + ChatColor.GOLD + this.player.getName() + ChatColor.YELLOW + " vs " + ChatColor.GOLD + this.other.getName());
                this.otherSumo.setState(SumoPlayer.SumoState.FIGHTING);
                this.playerSumo.setState(SumoPlayer.SumoState.FIGHTING);
            }
            else if (this.time <= 0) {
                final List<Player> players = Arrays.asList(this.player, this.other);
                final Player winner = players.get(ThreadLocalRandom.current().nextInt(players.size()));
                players.stream().filter(pl -> !pl.equals(winner)).forEach(pl -> SumoEvent.this.onDeath().accept(pl));
                this.cancel();
                return;
            }
            if (Arrays.asList(30, 25, 20, 15, 10).contains(this.time)) {
                SumoEvent.this.sendMessage(ChatColor.GRAY + "[" + ChatColor.YELLOW + "Round " + SumoEvent.this.round + ChatColor.GRAY + "] " + ChatColor.GOLD + "The match ends in " + ChatColor.GREEN + this.time + ChatColor.YELLOW + "...");
            }
            else if (Arrays.asList(5, 4, 3, 2, 1).contains(this.time)) {
                SumoEvent.this.sendMessage(ChatColor.GRAY + "[" + ChatColor.YELLOW + "Round " + SumoEvent.this.round + ChatColor.GRAY + "] " + ChatColor.GOLD + "The match is ending in " + ChatColor.GREEN + this.time + ChatColor.YELLOW + "...");
            }
            --this.time;
        }
        
        public Player getPlayer() {
            return this.player;
        }
        
        public Player getOther() {
            return this.other;
        }
        
        public SumoPlayer getPlayerSumo() {
            return this.playerSumo;
        }
        
        public SumoPlayer getOtherSumo() {
            return this.otherSumo;
        }
        
        public int getTime() {
            return this.time;
        }
        
        public SumoFightTask(final Player player, final Player other, final SumoPlayer playerSumo, final SumoPlayer otherSumo) {
            this.time = 90;
            this.player = player;
            this.other = other;
            this.playerSumo = playerSumo;
            this.otherSumo = otherSumo;
        }
    }
}
