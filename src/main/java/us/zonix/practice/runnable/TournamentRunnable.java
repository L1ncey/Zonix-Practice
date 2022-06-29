package us.zonix.practice.runnable;

import us.zonix.practice.player.PlayerData;
import us.zonix.practice.player.PlayerState;
import org.bukkit.entity.Player;
import us.zonix.practice.kit.Kit;
import us.zonix.practice.party.Party;
import java.util.Iterator;
import java.util.Set;
import org.bukkit.plugin.Plugin;
import org.bukkit.ChatColor;
import us.zonix.practice.match.Match;
import us.zonix.practice.queue.QueueType;
import us.zonix.practice.match.MatchTeam;
import java.util.Collection;
import java.util.Collections;
import java.util.ArrayList;
import java.util.List;
import us.zonix.practice.tournament.TournamentTeam;
import com.google.common.collect.Lists;
import java.util.UUID;
import com.google.common.collect.Sets;
import us.zonix.practice.tournament.TournamentState;
import us.zonix.practice.tournament.Tournament;
import us.zonix.practice.Practice;
import org.bukkit.scheduler.BukkitRunnable;

public class TournamentRunnable extends BukkitRunnable
{
    private final Practice plugin;
    private final Tournament tournament;
    
    public void run() {
        if (this.tournament.getTournamentState() == TournamentState.STARTING) {
            final int countdown = this.tournament.decrementCountdown();
            if (countdown == 0) {
                if (this.tournament.getCurrentRound() == 1) {
                    final Set<UUID> players = (Set<UUID>)Sets.newConcurrentHashSet((Iterable)this.tournament.getPlayers());
                    for (final UUID player : players) {
                        final Party party = this.plugin.getPartyManager().getParty(player);
                        if (party != null) {
                            final TournamentTeam team = new TournamentTeam(party.getLeader(), Lists.newArrayList((Iterable)party.getMembers()));
                            this.tournament.addAliveTeam(team);
                            for (final UUID member : party.getMembers()) {
                                players.remove(member);
                                this.tournament.setPlayerTeam(member, team);
                            }
                        }
                    }
                    List<UUID> currentTeam = null;
                    for (final UUID player2 : players) {
                        if (currentTeam == null) {
                            currentTeam = new ArrayList<UUID>();
                        }
                        currentTeam.add(player2);
                        if (currentTeam.size() == this.tournament.getTeamSize()) {
                            final TournamentTeam team = new TournamentTeam(currentTeam.get(0), currentTeam);
                            this.tournament.addAliveTeam(team);
                            for (final UUID teammate : team.getPlayers()) {
                                this.tournament.setPlayerTeam(teammate, team);
                            }
                            currentTeam = null;
                        }
                    }
                }
                final List<TournamentTeam> teams = this.tournament.getAliveTeams();
                Collections.shuffle(teams);
                for (int i = 0; i < teams.size(); i += 2) {
                    final TournamentTeam teamA = teams.get(i);
                    if (teams.size() > i + 1) {
                        final TournamentTeam teamB = teams.get(i + 1);
                        for (final UUID playerUUID : teamA.getAlivePlayers()) {
                            this.removeSpectator(playerUUID);
                        }
                        for (final UUID playerUUID : teamB.getAlivePlayers()) {
                            this.removeSpectator(playerUUID);
                        }
                        final MatchTeam matchTeamA = new MatchTeam(teamA.getLeader(), new ArrayList<UUID>(teamA.getAlivePlayers()), 0);
                        final MatchTeam matchTeamB = new MatchTeam(teamB.getLeader(), new ArrayList<UUID>(teamB.getAlivePlayers()), 1);
                        final Kit kit = this.plugin.getKitManager().getKit(this.tournament.getKitName());
                        final Match match = new Match(this.plugin.getArenaManager().getRandomArena(kit), kit, QueueType.UNRANKED, false, new MatchTeam[] { matchTeamA, matchTeamB });
                        final Player leaderA = this.plugin.getServer().getPlayer(teamA.getLeader());
                        final Player leaderB = this.plugin.getServer().getPlayer(teamB.getLeader());
                        match.broadcast(ChatColor.RED + "Starting tournament match. " + ChatColor.WHITE + "(" + leaderA.getName() + " vs " + leaderB.getName() + ")");
                        final Match match2;
                        this.plugin.getServer().getScheduler().runTask((Plugin)this.plugin, () -> {
                            this.plugin.getMatchManager().createMatch(match2);
                            this.tournament.addMatch(match2.getMatchId());
                            this.plugin.getTournamentManager().addTournamentMatch(match2.getMatchId(), this.tournament.getId());
                            return;
                        });
                    }
                    else {
                        for (final UUID playerUUID2 : teamA.getAlivePlayers()) {
                            final Player player3 = this.plugin.getServer().getPlayer(playerUUID2);
                            player3.sendMessage(ChatColor.DARK_GRAY.toString() + ChatColor.STRIKETHROUGH + "----------------------------------------------------");
                            player3.sendMessage(ChatColor.RED + "You have been skipped to the next round.");
                            player3.sendMessage(ChatColor.RED + "There was no matching team for you.");
                            player3.sendMessage(ChatColor.DARK_GRAY.toString() + ChatColor.STRIKETHROUGH + "----------------------------------------------------");
                        }
                    }
                }
                this.tournament.broadcast(ChatColor.DARK_GRAY.toString() + ChatColor.STRIKETHROUGH + "----------------------------------------------------");
                this.tournament.broadcast(ChatColor.RED.toString() + ChatColor.BOLD + "TOURNAMENT [" + this.tournament.getTeamSize() + "v" + this.tournament.getTeamSize() + "] " + this.tournament.getKitName());
                this.tournament.broadcast(ChatColor.GOLD.toString() + ChatColor.BOLD + "* " + ChatColor.WHITE + "Starting Round #" + this.tournament.getCurrentRound());
                this.tournament.broadcast(ChatColor.DARK_GRAY.toString() + ChatColor.STRIKETHROUGH + "----------------------------------------------------");
                this.tournament.setTournamentState(TournamentState.FIGHTING);
            }
            else if (countdown <= 5) {
                final String announce = ChatColor.RED + "[Tournament] " + ChatColor.WHITE + "Round #" + this.tournament.getCurrentRound() + " is starting in " + ChatColor.RED + countdown + ChatColor.WHITE + ".";
                this.tournament.broadcast(announce);
            }
        }
    }
    
    private void removeSpectator(final UUID playerUUID) {
        final Player player = this.plugin.getServer().getPlayer(playerUUID);
        if (player != null) {
            final PlayerData playerData = this.plugin.getPlayerManager().getPlayerData(player.getUniqueId());
            if (playerData.getPlayerState() == PlayerState.SPECTATING) {
                this.plugin.getMatchManager().removeSpectator(player);
            }
        }
    }
    
    public TournamentRunnable(final Tournament tournament) {
        this.plugin = Practice.getInstance();
        this.tournament = tournament;
    }
}
