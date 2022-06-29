package us.zonix.practice.team;

import us.zonix.practice.player.PlayerData;
import java.util.Iterator;
import org.bukkit.Server;
import java.util.function.Predicate;
import java.util.function.Function;
import java.util.Objects;
import org.bukkit.entity.Player;
import java.util.stream.Stream;
import java.util.Collection;
import java.util.ArrayList;
import java.util.UUID;
import java.util.List;
import us.zonix.practice.Practice;

public class KillableTeam
{
    protected final Practice plugin;
    private final List<UUID> players;
    private final List<UUID> alivePlayers;
    private final String leaderName;
    private UUID leader;
    
    public KillableTeam(final UUID leader, final List<UUID> players) {
        this.plugin = Practice.getInstance();
        this.alivePlayers = new ArrayList<UUID>();
        this.leader = leader;
        this.leaderName = this.plugin.getServer().getPlayer(leader).getName();
        this.players = players;
        this.alivePlayers.addAll(players);
    }
    
    public void killPlayer(final UUID playerUUID) {
        this.alivePlayers.remove(playerUUID);
    }
    
    public Stream<Player> alivePlayers() {
        final Stream<Object> stream = this.alivePlayers.stream();
        final Server server = this.plugin.getServer();
        Objects.requireNonNull(server);
        return stream.map((Function<? super Object, ?>)server::getPlayer).filter((Predicate<? super Player>)Objects::nonNull);
    }
    
    public Stream<Player> players() {
        final Stream<Object> stream = this.players.stream();
        final Server server = this.plugin.getServer();
        Objects.requireNonNull(server);
        return stream.map((Function<? super Object, ?>)server::getPlayer).filter((Predicate<? super Player>)Objects::nonNull);
    }
    
    public int onlinePlayers() {
        int count = 0;
        for (final UUID uuid : this.players) {
            final PlayerData playerData = this.plugin.getPlayerManager().getPlayerData(uuid);
            if (playerData != null && !playerData.isLeaving()) {
                ++count;
            }
        }
        return count;
    }
    
    public void revivePlayers() {
        this.alivePlayers.clear();
        this.alivePlayers.addAll(this.players);
    }
    
    public Practice getPlugin() {
        return this.plugin;
    }
    
    public List<UUID> getPlayers() {
        return this.players;
    }
    
    public List<UUID> getAlivePlayers() {
        return this.alivePlayers;
    }
    
    public String getLeaderName() {
        return this.leaderName;
    }
    
    public UUID getLeader() {
        return this.leader;
    }
    
    public void setLeader(final UUID leader) {
        this.leader = leader;
    }
}
