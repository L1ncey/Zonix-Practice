package us.zonix.practice.party;

import org.bukkit.Server;
import java.util.function.Predicate;
import java.util.function.Function;
import java.util.Objects;
import java.util.stream.Stream;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import java.util.Iterator;
import us.zonix.practice.match.MatchTeam;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.Set;
import java.util.List;
import java.util.UUID;
import us.zonix.practice.Practice;
import us.zonix.practice.party.selection.ClassSelectionMenu;

public class Party
{
    public static final ClassSelectionMenu CLASS_SELECTION_MENU;
    private final Practice plugin;
    private final UUID leader;
    private final List<UUID> members;
    private int limit;
    private boolean open;
    private final Set<UUID> bards;
    private final Set<UUID> archers;
    
    public Party(final UUID leader) {
        this.plugin = Practice.getInstance();
        this.members = new ArrayList<UUID>();
        this.limit = 50;
        this.bards = new HashSet<UUID>();
        this.archers = new HashSet<UUID>();
        this.leader = leader;
        this.members.add(leader);
    }
    
    public void addMember(final UUID uuid) {
        this.members.add(uuid);
    }
    
    public void removeMember(final UUID uuid) {
        this.members.remove(uuid);
    }
    
    public void broadcast(final String message) {
        this.members().forEach(member -> member.sendMessage(message));
    }
    
    public MatchTeam[] split() {
        final List<UUID> teamA = new ArrayList<UUID>();
        final List<UUID> teamB = new ArrayList<UUID>();
        for (final UUID member : this.members) {
            if (teamA.size() == teamB.size()) {
                teamA.add(member);
            }
            else {
                teamB.add(member);
            }
        }
        return new MatchTeam[] { new MatchTeam(teamA.get(0), teamA, 0), new MatchTeam(teamB.get(0), teamB, 1) };
    }
    
    public List<Player> diamonds() {
        final List<Player> available = new ArrayList<Player>();
        for (final UUID uuid : this.members) {
            if (!this.archers.contains(uuid)) {
                if (this.bards.contains(uuid)) {
                    continue;
                }
                if (Bukkit.getPlayer(uuid) == null) {
                    continue;
                }
                available.add(Bukkit.getPlayer(uuid));
            }
        }
        return available;
    }
    
    public void addArcher(final Player player) {
        this.archers.add(player.getUniqueId());
    }
    
    public void addBard(final Player player) {
        this.bards.add(player.getUniqueId());
    }
    
    public Stream<Player> members() {
        final Stream<Object> stream = this.members.stream();
        final Server server = this.plugin.getServer();
        Objects.requireNonNull(server);
        return stream.map((Function<? super Object, ?>)server::getPlayer).filter((Predicate<? super Player>)Objects::nonNull);
    }
    
    public int getMaxArchers() {
        if (this.members.size() <= 9) {
            return 1;
        }
        if (this.members.size() <= 15) {
            return 2;
        }
        if (this.members.size() > 16) {
            return 3;
        }
        return 0;
    }
    
    public int getMaxBards() {
        if (this.members.size() <= 8) {
            return 1;
        }
        if (this.members.size() <= 9) {
            return 2;
        }
        if (this.members.size() > 10) {
            return 3;
        }
        return 0;
    }
    
    public Practice getPlugin() {
        return this.plugin;
    }
    
    public UUID getLeader() {
        return this.leader;
    }
    
    public List<UUID> getMembers() {
        return this.members;
    }
    
    public int getLimit() {
        return this.limit;
    }
    
    public boolean isOpen() {
        return this.open;
    }
    
    public Set<UUID> getBards() {
        return this.bards;
    }
    
    public Set<UUID> getArchers() {
        return this.archers;
    }
    
    public void setLimit(final int limit) {
        this.limit = limit;
    }
    
    public void setOpen(final boolean open) {
        this.open = open;
    }
    
    static {
        CLASS_SELECTION_MENU = new ClassSelectionMenu();
    }
}
