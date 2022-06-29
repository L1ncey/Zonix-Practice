package us.zonix.practice.runnable;

import us.zonix.practice.managers.MatchManager;
import java.util.stream.Stream;
import us.zonix.practice.managers.PlayerManager;
import org.bukkit.event.Event;
import us.zonix.practice.event.match.MatchEndEvent;
import us.zonix.practice.match.MatchTeam;
import org.bukkit.plugin.Plugin;
import org.bukkit.entity.Player;
import java.util.function.Consumer;
import java.util.Objects;
import org.bukkit.entity.Entity;
import java.util.Date;
import org.bukkit.ChatColor;
import us.zonix.practice.match.MatchState;
import us.zonix.practice.match.Match;
import us.zonix.practice.Practice;
import org.bukkit.scheduler.BukkitRunnable;

public class MatchRunnable extends BukkitRunnable
{
    private final Practice plugin;
    private final Match match;
    
    public void run() {
        switch (this.match.getMatchState()) {
            case STARTING: {
                if (this.match.decrementCountdown() == 0) {
                    this.match.setMatchState(MatchState.FIGHTING);
                    this.match.broadcast(ChatColor.GREEN + "The match has started.");
                    if (this.match.getKit().isBuild()) {
                        this.match.broadcast(ChatColor.DARK_RED.toString() + ChatColor.BOLD + "WARNING " + ChatColor.RED + "Stacking Blocks will result in a ban.");
                    }
                    if (this.match.isRedrover()) {
                        this.plugin.getMatchManager().pickPlayer(this.match);
                    }
                    this.match.setStartTime(new Date());
                    break;
                }
                this.match.broadcast(ChatColor.YELLOW + "The match starts in " + ChatColor.GREEN + this.match.getCountdown() + ChatColor.YELLOW + "...");
                break;
            }
            case SWITCHING: {
                if (this.match.decrementCountdown() == 0) {
                    this.match.getEntitiesToRemove().forEach(Entity::remove);
                    this.match.clearEntitiesToRemove();
                    this.match.setMatchState(MatchState.FIGHTING);
                    this.plugin.getMatchManager().pickPlayer(this.match);
                    break;
                }
                break;
            }
            case RESTARTING: {
                if (this.match.decrementCountdown() != 0) {
                    this.match.broadcast(ChatColor.YELLOW + "The match starts in " + ChatColor.GREEN + this.match.getCountdown() + ChatColor.YELLOW + "...");
                    break;
                }
                this.match.setMatchState(MatchState.FIGHTING);
                this.match.broadcast(ChatColor.GREEN + "The match has started.");
                if (this.match.getKit().isBuild()) {
                    this.match.broadcast(ChatColor.DARK_RED.toString() + ChatColor.BOLD + "WARNING " + ChatColor.RED + "Stacking Blocks will result in a ban.");
                }
                if (this.match.isRedrover()) {
                    this.plugin.getMatchManager().pickPlayer(this.match);
                    break;
                }
                break;
            }
            case ENDING: {
                if (this.match.decrementCountdown() == 0) {
                    this.plugin.getTournamentManager().removeTournamentMatch(this.match);
                    this.match.getRunnables().forEach(id -> this.plugin.getServer().getScheduler().cancelTask((int)id));
                    this.match.getEntitiesToRemove().forEach(Entity::remove);
                    final PlayerManager playerManager;
                    final Stream<Player> stream;
                    this.match.getTeams().forEach(team -> {
                        team.alivePlayers();
                        this.plugin.getPlayerManager();
                        Objects.requireNonNull(playerManager);
                        stream.forEach(playerManager::sendToSpawnAndReset);
                        return;
                    });
                    final Stream<Player> spectatorPlayers = this.match.spectatorPlayers();
                    final MatchManager matchManager = this.plugin.getMatchManager();
                    Objects.requireNonNull(matchManager);
                    spectatorPlayers.forEach(matchManager::removeSpectator);
                    if (this.match.getKit().isBuild() || this.match.getKit().isSpleef()) {
                        new MatchResetRunnable(this.match).runTask((Plugin)this.plugin);
                    }
                    this.plugin.getMatchManager().removeMatch(this.match);
                    this.cancel();
                    break;
                }
                break;
            }
            case FIGHTING: {
                final int remainingOne = (this.match.getTeams().get(0) == null) ? 0 : ((int)this.match.getTeams().get(0).alivePlayers().count());
                final int remainingTwo = (this.match.getTeams().get(1) == null) ? 0 : ((int)this.match.getTeams().get(1).alivePlayers().count());
                if (remainingOne == 0) {
                    this.plugin.getServer().getPluginManager().callEvent((Event)new MatchEndEvent(this.match, this.match.getTeams().get(1), this.match.getTeams().get(0)));
                    break;
                }
                if (remainingTwo == 0) {
                    this.plugin.getServer().getPluginManager().callEvent((Event)new MatchEndEvent(this.match, this.match.getTeams().get(0), this.match.getTeams().get(1)));
                    break;
                }
                break;
            }
        }
    }
    
    public MatchRunnable(final Match match) {
        this.plugin = Practice.getInstance();
        this.match = match;
    }
}
