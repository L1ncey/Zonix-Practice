package us.zonix.practice.managers;

import us.zonix.practice.match.MatchTeam;
import us.zonix.practice.match.Match;
import java.util.stream.Stream;
import java.util.function.Predicate;
import java.util.Objects;
import us.zonix.practice.party.Party;
import us.zonix.practice.tournament.TournamentState;
import java.util.Iterator;
import org.bukkit.Bukkit;
import us.zonix.practice.team.KillableTeam;
import us.zonix.practice.util.TeamUtil;
import us.zonix.practice.tournament.TournamentTeam;
import me.maiko.dexter.profile.Profile;
import org.bukkit.entity.Player;
import org.bukkit.ChatColor;
import org.bukkit.plugin.Plugin;
import us.zonix.practice.runnable.TournamentRunnable;
import org.bukkit.command.CommandSender;
import java.util.HashMap;
import org.bukkit.scheduler.BukkitRunnable;
import us.zonix.practice.tournament.Tournament;
import java.util.UUID;
import java.util.Map;
import us.zonix.practice.Practice;

public class TournamentManager
{
    private final Practice plugin;
    private final Map<UUID, Integer> players;
    private final Map<UUID, Integer> matches;
    private final Map<Integer, Tournament> tournaments;
    private final Map<Tournament, BukkitRunnable> runnables;
    
    public TournamentManager() {
        this.plugin = Practice.getInstance();
        this.players = new HashMap<UUID, Integer>();
        this.matches = new HashMap<UUID, Integer>();
        this.tournaments = new HashMap<Integer, Tournament>();
        this.runnables = new HashMap<Tournament, BukkitRunnable>();
    }
    
    public boolean isInTournament(final UUID uuid) {
        return this.players.containsKey(uuid);
    }
    
    public Tournament getTournament(final UUID uuid) {
        final Integer id = this.players.get(uuid);
        if (id == null) {
            return null;
        }
        return this.tournaments.get(id);
    }
    
    public Tournament getTournamentFromMatch(final UUID uuid) {
        final Integer id = this.matches.get(uuid);
        if (id == null) {
            return null;
        }
        return this.tournaments.get(id);
    }
    
    public void createTournament(final CommandSender commandSender, final int id, final int teamSize, final int size, final String kitName) {
        final Tournament tournament = new Tournament(id, teamSize, size, kitName);
        this.tournaments.put(id, tournament);
        final BukkitRunnable bukkitRunnable = new TournamentRunnable(tournament);
        bukkitRunnable.runTaskTimerAsynchronously((Plugin)this.plugin, 20L, 20L);
        this.runnables.put(tournament, bukkitRunnable);
        commandSender.sendMessage(ChatColor.WHITE + "Successfully created tournament.");
        if (commandSender instanceof Player) {
            final Player player = (Player)commandSender;
            player.performCommand("tournament alert " + id);
        }
    }
    
    private void playerLeft(final Tournament tournament, final Player player) {
        final TournamentTeam team = tournament.getPlayerTeam(player.getUniqueId());
        tournament.removePlayer(player.getUniqueId());
        player.sendMessage(ChatColor.RED.toString() + "[Tournament] " + ChatColor.GRAY + "You left the tournament.");
        this.players.remove(player.getUniqueId());
        this.plugin.getPlayerManager().sendToSpawnAndReset(player);
        tournament.broadcast(ChatColor.RED.toString() + "[Tournament] " + Profile.getByUuidIfAvailable(player.getUniqueId()).getRank().getGameColor() + "" + player.getName() + ChatColor.WHITE + " left the tournament. [" + tournament.getPlayers().size() + "/" + tournament.getSize() + "]");
        if (team != null) {
            team.killPlayer(player.getUniqueId());
            if (team.getAlivePlayers().size() == 0) {
                tournament.killTeam(team);
                if (tournament.getAliveTeams().size() == 1) {
                    final TournamentTeam tournamentTeam = tournament.getAliveTeams().get(0);
                    final String names = TeamUtil.getNames(tournamentTeam);
                    final String announce = ChatColor.DARK_RED + names + ChatColor.WHITE + " has won our " + ChatColor.DARK_RED + tournament.getKitName() + ChatColor.WHITE + " tournament!";
                    Bukkit.broadcastMessage(announce);
                    for (final UUID playerUUID : tournamentTeam.getAlivePlayers()) {
                        this.players.remove(playerUUID);
                        final Player tournamentPlayer = this.plugin.getServer().getPlayer(playerUUID);
                        this.plugin.getPlayerManager().sendToSpawnAndReset(tournamentPlayer);
                    }
                    this.plugin.getTournamentManager().removeTournament(tournament.getId(), false);
                }
            }
            else if (team.getLeader().equals(player.getUniqueId())) {
                team.setLeader(team.getAlivePlayers().get(0));
            }
        }
    }
    
    private void teamEliminated(final Tournament tournament, final TournamentTeam winnerTeam, final TournamentTeam losingTeam) {
        for (final UUID playerUUID : losingTeam.getAlivePlayers()) {
            final Player player = this.plugin.getServer().getPlayer(playerUUID);
            tournament.removePlayer(player.getUniqueId());
            player.sendMessage(ChatColor.RED.toString() + "[Tournament] " + ChatColor.GRAY + "You have been eliminated. " + ChatColor.GRAY);
            this.players.remove(player.getUniqueId());
        }
        final String word = (losingTeam.getAlivePlayers().size() > 1) ? "have" : "has";
        final boolean isParty = tournament.getTeamSize() > 1;
        final String announce = ChatColor.RED + "[Tournament] " + ChatColor.RED + (isParty ? (losingTeam.getLeaderName() + "'s Party") : losingTeam.getLeaderName()) + ChatColor.GRAY + " " + word + " been eliminated by " + ChatColor.WHITE + (isParty ? (winnerTeam.getLeaderName() + "'s Party") : winnerTeam.getLeaderName()) + ".";
        final String alive = ChatColor.RED + "[Tournament] " + ChatColor.GRAY + "Players: (" + tournament.getPlayers().size() + "/" + tournament.getSize() + ")";
        tournament.broadcast(announce);
        tournament.broadcast(alive);
    }
    
    public void leaveTournament(final Player player) {
        final Tournament tournament = this.getTournament(player.getUniqueId());
        if (tournament == null) {
            return;
        }
        final Party party = this.plugin.getPartyManager().getParty(player.getUniqueId());
        if (party != null && tournament.getTournamentState() != TournamentState.FIGHTING) {
            if (this.plugin.getPartyManager().isLeader(player.getUniqueId())) {
                for (final UUID memberUUID : party.getMembers()) {
                    final Player member = this.plugin.getServer().getPlayer(memberUUID);
                    this.playerLeft(tournament, member);
                }
            }
            else {
                player.sendMessage(ChatColor.RED + "You are not the leader of the party.");
            }
        }
        else {
            this.playerLeft(tournament, player);
        }
    }
    
    private void playerJoined(final Tournament tournament, final Player player, final boolean party) {
        if (Practice.getInstance().isRegionLock() && !party) {
            final Profile profile = Profile.getByUuid(player.getUniqueId());
            if (profile != null && profile.hasVpnData()) {
                final Stream<Object> stream = Practice.getInstance().getAllowedRegions().stream();
                final String continentCode = profile.getVpnData().getContinentCode();
                Objects.requireNonNull(continentCode);
                if (stream.noneMatch((Predicate<? super Object>)continentCode::equalsIgnoreCase)) {
                    player.sendMessage(ChatColor.RED + "Your region does not allow you to join premium/ranked queues on this practice sub-server. Make sure you are on the right proxy and sub-server.");
                    return;
                }
            }
            else {
                Bukkit.getOnlinePlayers().parallelStream().filter(online -> online.hasPermission("core.superadmin")).forEach(online -> online.sendMessage(ChatColor.RED + "[!] Couldn't find " + player.getName() + "'s region! Make sure the AntiVPN is working correctly, if not please use /regionlock toggle off to disable region-lock."));
            }
        }
        tournament.addPlayer(player.getUniqueId());
        this.players.put(player.getUniqueId(), tournament.getId());
        this.plugin.getPlayerManager().sendToSpawnAndReset(player);
        tournament.broadcast(ChatColor.RED.toString() + "[Tournament] " + Profile.getByUuidIfAvailable(player.getUniqueId()).getRank().getGameColor() + "" + player.getName() + ChatColor.WHITE + " joined the tournament. [" + tournament.getPlayers().size() + "/" + tournament.getSize() + "]");
    }
    
    public void joinTournament(final Integer id, final Player player) {
        final Tournament tournament = this.tournaments.get(id);
        final Party party = this.plugin.getPartyManager().getParty(player.getUniqueId());
        if (party != null) {
            if (this.plugin.getPartyManager().isLeader(player.getUniqueId())) {
                if (party.getMembers().size() + tournament.getPlayers().size() <= tournament.getSize()) {
                    if (party.getMembers().size() != tournament.getTeamSize() || party.getMembers().size() == 1) {
                        player.sendMessage(ChatColor.RED + "The party size must be of " + tournament.getTeamSize() + " players.");
                    }
                    else {
                        for (final UUID memberUUID : party.getMembers()) {
                            final Player member = this.plugin.getServer().getPlayer(memberUUID);
                            this.playerJoined(tournament, member, true);
                        }
                    }
                }
                else {
                    player.sendMessage(ChatColor.RED + "Sorry! The tournament is already full.");
                }
            }
            else {
                player.sendMessage(ChatColor.RED + "You are not the leader of the party.");
            }
        }
        else {
            this.playerJoined(tournament, player, false);
        }
        if (tournament.getPlayers().size() == tournament.getSize()) {
            tournament.setTournamentState(TournamentState.STARTING);
        }
    }
    
    public Tournament getTournament(final Integer id) {
        return this.tournaments.get(id);
    }
    
    public void removeTournament(final Integer id, final boolean force) {
        final Tournament tournament = this.tournaments.get(id);
        if (tournament == null) {
            return;
        }
        if (force) {
            final Iterator<UUID> players = this.players.keySet().iterator();
            while (players.hasNext()) {
                final UUID uuid = players.next();
                final Player player = Bukkit.getPlayer(uuid);
                if (player != null) {
                    player.sendMessage(ChatColor.RED + "The tournament has force ended.");
                    final Tournament tournament2;
                    final Player player2;
                    this.plugin.getServer().getScheduler().runTaskLater((Plugin)this.plugin, () -> {
                        if (tournament2.getTournamentState() == TournamentState.FIGHTING) {
                            this.plugin.getMatchManager().removeFighter(player2, this.plugin.getPlayerManager().getPlayerData(player2.getUniqueId()), false);
                        }
                        this.plugin.getPlayerManager().sendToSpawnAndReset(player2);
                        return;
                    }, 2L);
                }
                players.remove();
            }
        }
        if (this.runnables.containsKey(tournament)) {
            this.runnables.get(tournament).cancel();
        }
        this.tournaments.remove(id);
    }
    
    public void addTournamentMatch(final UUID matchId, final Integer tournamentId) {
        this.matches.put(matchId, tournamentId);
    }
    
    public void removeTournamentMatch(final Match match) {
        final Tournament tournament = this.getTournamentFromMatch(match.getMatchId());
        if (tournament == null) {
            return;
        }
        tournament.removeMatch(match.getMatchId());
        this.matches.remove(match.getMatchId());
        final MatchTeam losingTeam = (match.getWinningTeamId() == 0) ? match.getTeams().get(1) : match.getTeams().get(0);
        final TournamentTeam losingTournamentTeam = tournament.getPlayerTeam(losingTeam.getPlayers().get(0));
        tournament.killTeam(losingTournamentTeam);
        final MatchTeam winningTeam = match.getTeams().get(match.getWinningTeamId());
        final TournamentTeam winningTournamentTeam = tournament.getPlayerTeam(winningTeam.getAlivePlayers().get(0));
        this.teamEliminated(tournament, winningTournamentTeam, losingTournamentTeam);
        if (tournament.getMatches().size() == 0) {
            if (tournament.getAliveTeams().size() > 1) {
                tournament.setTournamentState(TournamentState.STARTING);
                tournament.setCurrentRound(tournament.getCurrentRound() + 1);
                tournament.setCountdown(16);
            }
            else {
                final String names = TeamUtil.getNames(winningTournamentTeam);
                final String announce = ChatColor.DARK_RED + names + ChatColor.WHITE + " has won our " + ChatColor.DARK_RED + tournament.getKitName() + ChatColor.WHITE + " tournament!";
                Bukkit.broadcastMessage(announce);
                for (final UUID playerUUID : winningTournamentTeam.getAlivePlayers()) {
                    this.players.remove(playerUUID);
                    final Player tournamentPlayer = this.plugin.getServer().getPlayer(playerUUID);
                    this.plugin.getPlayerManager().sendToSpawnAndReset(tournamentPlayer);
                }
                this.plugin.getTournamentManager().removeTournament(tournament.getId(), false);
            }
        }
    }
    
    public Map<Integer, Tournament> getTournaments() {
        return this.tournaments;
    }
}
