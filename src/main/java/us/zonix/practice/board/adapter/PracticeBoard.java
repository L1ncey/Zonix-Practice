package us.zonix.practice.board.adapter;

import us.zonix.practice.util.TimeUtils;
import java.time.temporal.Temporal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import us.zonix.practice.util.PlayerUtil;
import us.zonix.practice.player.EloRank;
import us.zonix.practice.pvpclasses.pvpclasses.BardClass;
import us.zonix.practice.match.Match;
import org.bukkit.scoreboard.Team;
import java.util.UUID;
import us.zonix.practice.match.MatchTeam;
import me.maiko.dexter.profile.Profile;
import me.maiko.dexter.rank.Rank;
import org.bukkit.scoreboard.Scoreboard;
import java.util.Iterator;
import us.zonix.practice.queue.QueueEntry;
import us.zonix.practice.party.Party;
import me.maiko.dexter.Dexter;
import us.zonix.practice.tournament.Tournament;
import us.zonix.practice.player.PlayerState;
import java.util.Collection;
import us.zonix.practice.util.TimeUtil;
import us.zonix.practice.util.StatusCache;
import org.bukkit.Bukkit;
import us.zonix.practice.events.PracticeEvent;
import us.zonix.practice.bots.ZonixBot;
import org.apache.commons.lang.StringUtils;
import java.util.LinkedList;
import us.zonix.practice.player.PlayerData;
import us.zonix.practice.settings.item.ProfileOptionsItemState;
import java.util.List;
import us.zonix.practice.board.Board;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import us.zonix.practice.Practice;
import us.zonix.practice.board.BoardAdapter;

public class PracticeBoard implements BoardAdapter
{
    private static final String LINE;
    private final Practice plugin;
    
    public PracticeBoard() {
        this.plugin = Practice.getInstance();
    }
    
    @Override
    public String getTitle(final Player player) {
        return ChatColor.translateAlternateColorCodes('&', "&4&lZonix &7\u2503 &fNA");
    }
    
    @Override
    public void preLoop() {
    }
    
    @Override
    public List<String> getScoreboard(final Player player, final Board board) {
        final PlayerData playerData = this.plugin.getPlayerManager().getPlayerData(player.getUniqueId());
        if (playerData == null) {
            this.plugin.getLogger().warning(player.getName() + "'s player data is null");
            return null;
        }
        if (playerData.getOptions().getScoreboard() == ProfileOptionsItemState.DISABLED) {
            return null;
        }
        switch (playerData.getPlayerState()) {
            case LOADING:
            case EDITING:
            case SPAWN:
            case EVENT: {
                return this.getLobbyBoard(player, false);
            }
            case QUEUE: {
                return this.getLobbyBoard(player, true);
            }
            case FIGHTING: {
                return this.getGameBoard(player);
            }
            case SPECTATING: {
                return this.getSpectatorBoard(player);
            }
            case FFA: {
                return this.getFFABoard(player);
            }
            case TRAINING: {
                return this.getTrainingBoard(player);
            }
            default: {
                return null;
            }
        }
    }
    
    private List<String> getTrainingBoard(final Player player) {
        final List<String> strings = new LinkedList<String>();
        if (this.plugin.getBotManager().isTraining(player)) {
            final ZonixBot zonixBot = this.plugin.getBotManager().getBotFromPlayer(player);
            if (zonixBot != null) {
                strings.add(PracticeBoard.LINE);
                strings.add("&cKit&f: &7" + zonixBot.getKit().getName());
                strings.add("&cDifficulty&f: &7" + StringUtils.capitalize(zonixBot.getBotDifficulty().name().toLowerCase()));
                strings.add(PracticeBoard.LINE);
            }
        }
        return strings;
    }
    
    private List<String> getLobbyBoard(final Player player, final boolean queuing) {
        final List<String> strings = new LinkedList<String>();
        strings.add(PracticeBoard.LINE);
        final Party party = this.plugin.getPartyManager().getParty(player.getUniqueId());
        PracticeEvent event = this.plugin.getEventManager().getEventPlaying(player);
        if (this.plugin.getEventManager().getSpectators().containsKey(player.getUniqueId())) {
            event = this.plugin.getEventManager().getSpectators().get(player.getUniqueId());
        }
        final PlayerData playerData = this.plugin.getPlayerManager().getPlayerData(player.getUniqueId());
        if (event == null) {
            strings.add("&cOnline&7: &f" + Bukkit.getOnlinePlayers().size());
            strings.add("&cFighting&7: &f" + StatusCache.getInstance().getFighting());
            if (System.currentTimeMillis() < this.plugin.getEventManager().getCooldown()) {
                strings.add("&cCooldown&7: &f" + TimeUtil.convertToFormat(this.plugin.getEventManager().getCooldown()));
            }
        }
        if (queuing) {
            strings.add(PracticeBoard.LINE);
            final QueueEntry queueEntry = (party == null) ? this.plugin.getQueueManager().getQueueEntry(player.getUniqueId()) : this.plugin.getQueueManager().getQueueEntry(party.getLeader());
            strings.add("&4In Queue" + (queueEntry.isBestOfThree() ? " &7(Best of 5)" : ""));
            strings.add("&c * &f" + queueEntry.getKitName() + " " + queueEntry.getQueueType().getName());
        }
        if (party != null) {
            strings.add(PracticeBoard.LINE);
            strings.add("&4Party &7(" + party.getMembers().size() + " Player" + ((party.getMembers().size() == 1) ? "" : "s") + ")");
            strings.add(" &c* &fLeader&7: " + Bukkit.getPlayer(party.getLeader()).getName());
        }
        if (event != null) {
            strings.add(ChatColor.DARK_RED + "Event " + ChatColor.GRAY + "(" + event.getName() + ")");
            strings.addAll(event.getScoreboardLines(player));
        }
        if (playerData.getPlayerState() != PlayerState.EVENT && this.plugin.getTournamentManager().getTournaments().size() >= 1) {
            for (final Tournament tournament : this.plugin.getTournamentManager().getTournaments().values()) {
                strings.add(ChatColor.GRAY.toString() + ChatColor.STRIKETHROUGH + "-------------------");
                strings.add(ChatColor.DARK_RED + "Tournament " + ChatColor.GRAY + "[" + tournament.getTeamSize() + "v" + tournament.getTeamSize() + "]");
                strings.add(" &c* &fLadder§7: " + tournament.getKitName());
                strings.add(" &c* &fStage§7: Round #" + tournament.getCurrentRound());
                strings.add(" &c* &fPlayers§7: " + tournament.getPlayers().size() + "/" + tournament.getSize());
                strings.add(" &c* &fID§7: " + tournament.getId());
                final int countdown = tournament.getCountdown();
                if (countdown > 0 && countdown <= 30) {
                    strings.add(ChatColor.RED.toString() + ChatColor.BOLD + " * " + ChatColor.RED + "Starting§7: " + ChatColor.WHITE + countdown + "s");
                }
            }
        }
        if (player.hasMetadata("modmode")) {
            strings.add(ChatColor.DARK_RED + "Silent Mode");
        }
        if (Dexter.getInstance().getShutdownTask() != null) {
            strings.add(ChatColor.RED.toString() + "Reboot§7: " + ChatColor.WHITE + Dexter.getInstance().getShutdownTask().getSecondsUntilShutdown() + "s");
        }
        strings.add(ChatColor.GRAY.toString() + ChatColor.STRIKETHROUGH + "-------------------");
        return strings;
    }
    
    @Override
    public void onScoreboardCreate(final Player player, final Scoreboard scoreboard) {
        Team red = scoreboard.getTeam("red");
        if (red == null) {
            red = scoreboard.registerNewTeam("red");
        }
        Team green = scoreboard.getTeam("green");
        if (green == null) {
            green = scoreboard.registerNewTeam("green");
        }
        for (final Rank rank : Dexter.getInstance().getRankManager().getRanks()) {
            Team rankTeam = scoreboard.getTeam(rank.getId());
            if (rankTeam == null) {
                rankTeam = scoreboard.registerNewTeam(rank.getId());
            }
            rankTeam.setPrefix(rank.getGameColor());
        }
        red.setPrefix(ChatColor.RED.toString());
        green.setPrefix(ChatColor.GREEN.toString());
        final PlayerData playerData = this.plugin.getPlayerManager().getPlayerData(player.getUniqueId());
        if (playerData.getPlayerState() != PlayerState.FIGHTING) {
            for (final String entry : red.getEntries()) {
                red.removeEntry(entry);
            }
            for (final String entry : green.getEntries()) {
                green.removeEntry(entry);
            }
            for (final Rank rank2 : Dexter.getInstance().getRankManager().getRanks()) {
                final Team rankTeam2 = scoreboard.getTeam(rank2.getId());
                for (final Player online : Bukkit.getOnlinePlayers()) {
                    final Profile onlineProfile = Profile.getByUuidIfAvailable(online.getUniqueId());
                    if (onlineProfile != null && onlineProfile.getRank() == rank2 && !rankTeam2.hasEntry(online.getName())) {
                        rankTeam2.addEntry(online.getName());
                    }
                }
            }
            return;
        }
        final Match match = this.plugin.getMatchManager().getMatch(player.getUniqueId());
        for (final MatchTeam team : match.getTeams()) {
            for (final UUID teamUUID : team.getAlivePlayers()) {
                final Player teamPlayer = this.plugin.getServer().getPlayer(teamUUID);
                if (teamPlayer != null) {
                    final String teamPlayerName = teamPlayer.getName();
                    if (team.getTeamID() == playerData.getTeamID() && !match.isFFA()) {
                        if (green.hasEntry(teamPlayerName)) {
                            continue;
                        }
                        green.addEntry(teamPlayerName);
                    }
                    else {
                        if (red.hasEntry(teamPlayerName)) {
                            continue;
                        }
                        red.addEntry(teamPlayerName);
                    }
                }
            }
        }
        if (playerData.getPlayerState() != PlayerState.FIGHTING) {
            for (final Rank rank3 : Dexter.getInstance().getRankManager().getRanks()) {
                final Team rankTeam3 = scoreboard.getTeam(rank3.getId());
                for (final Player online2 : Bukkit.getOnlinePlayers()) {
                    final Profile onlineProfile2 = Profile.getByUuidIfAvailable(online2.getUniqueId());
                    if (onlineProfile2 != null && onlineProfile2.getRank() == rank3 && !rankTeam3.hasEntry(online2.getName())) {
                        rankTeam3.addEntry(online2.getName());
                    }
                }
            }
        }
    }
    
    private List<String> getGameBoard(final Player player) {
        final List<String> strings = new LinkedList<String>();
        Match match = null;
        if (this.plugin.getPlayerManager().getPlayerData(player.getUniqueId()).getPlayerState() == PlayerState.SPECTATING) {
            match = this.plugin.getMatchManager().getSpectatingMatch(player.getUniqueId());
        }
        else if (this.plugin.getPlayerManager().getPlayerData(player.getUniqueId()).getPlayerState() == PlayerState.FIGHTING) {
            match = this.plugin.getMatchManager().getMatch(player.getUniqueId());
        }
        if (match == null) {
            strings.add(ChatColor.GRAY.toString() + ChatColor.STRIKETHROUGH + "-------------------");
            strings.add(ChatColor.RED.toString() + "Finding match info...");
            strings.add(ChatColor.GRAY.toString() + ChatColor.STRIKETHROUGH + "-------------------");
            return strings;
        }
        strings.add(ChatColor.GRAY.toString() + ChatColor.STRIKETHROUGH + "-------------------");
        strings.add(ChatColor.RED.toString() + "Ladder§7: " + ChatColor.WHITE + ((match.getKit() == null) ? "Unknown" : match.getKit().getName()));
        final PlayerData playerData = this.plugin.getPlayerManager().getPlayerData(player.getUniqueId());
        Player opponentPlayer = null;
        if (!match.isPartyMatch() && !match.isFFA()) {
            opponentPlayer = ((match.getTeams().get(0).getPlayers().get(0) == player.getUniqueId()) ? this.plugin.getServer().getPlayer((UUID)match.getTeams().get(1).getPlayers().get(0)) : this.plugin.getServer().getPlayer((UUID)match.getTeams().get(0).getPlayers().get(0)));
            if (opponentPlayer == null) {
                return this.getLobbyBoard(player, false);
            }
            final MatchTeam opposingTeam = (match.getTeams().get(0).getPlayers().get(0) == player.getUniqueId()) ? match.getTeams().get(1) : match.getTeams().get(0);
            final MatchTeam playerTeam = (match.getTeams().get(0).getPlayers().get(0) == player.getUniqueId()) ? match.getTeams().get(0) : match.getTeams().get(1);
            strings.add(ChatColor.RED.toString() + "Opponent§7: " + ChatColor.RESET.toString() + ChatColor.WHITE + opponentPlayer.getName());
            if (match.isBestOfThree()) {
                strings.add(ChatColor.RED.toString() + "Your Wins§7: " + ChatColor.WHITE + playerTeam.getMatchWins());
                strings.add(ChatColor.RED.toString() + "Opponent Wins§7: " + ChatColor.WHITE + opposingTeam.getMatchWins());
            }
        }
        else if (match.isPartyMatch() && !match.isFFA()) {
            final MatchTeam opposingTeam = match.isFFA() ? match.getTeams().get(0) : ((playerData.getTeamID() == 0) ? match.getTeams().get(1) : match.getTeams().get(0));
            final MatchTeam playerTeam = match.getTeams().get(playerData.getTeamID());
            strings.add(ChatColor.GREEN.toString() + "Your Team§7: " + ChatColor.WHITE + playerTeam.getAlivePlayers().size() + " alive");
            strings.add(ChatColor.DARK_RED.toString() + "Opponent§7: " + ChatColor.WHITE + opposingTeam.getAlivePlayers().size() + " alive");
            if (match.isBestOfThree()) {
                strings.add(ChatColor.RED.toString() + "Team Wins§7: " + ChatColor.WHITE + playerTeam.getMatchWins());
                strings.add(ChatColor.RED.toString() + "Opponent Wins§7: " + ChatColor.WHITE + opposingTeam.getMatchWins());
            }
            if (match.getKit().isHcteams() && this.plugin.getPartyManager().getParty(player.getUniqueId()) != null && this.plugin.getPartyManager().getParty(player.getUniqueId()).getBards().contains(player.getUniqueId()) && playerTeam.getAlivePlayers().contains(player.getUniqueId())) {
                strings.add(ChatColor.RED.toString() + "Bard Energy§7: " + ChatColor.RESET.toString() + ChatColor.WHITE + BardClass.getEnergy().get(player.getName()));
            }
        }
        else if (match.isFFA()) {
            final int alive = match.getTeams().get(0).getAlivePlayers().size() - 1;
            strings.add(ChatColor.RED.toString() + "Remaining§7: " + ChatColor.WHITE + match.getTeams().get(0).getAlivePlayers().size() + " player" + ((alive == 1) ? "" : "s"));
        }
        if (opponentPlayer != null && !match.isPartyMatch() && !match.isFFA() && match.getType().isBoth()) {
            final PlayerData opponentData = this.plugin.getPlayerManager().getPlayerData(opponentPlayer.getUniqueId());
            if (opponentData != null) {
                final String[] oppEloRank = EloRank.getRankByElo(opponentData.getElo(match.getKit().getName())).name().split("_");
                final String oppEloRankText = StringUtils.capitalize(oppEloRank[0].toLowerCase()) + " " + oppEloRank[1];
                strings.add(ChatColor.RED.toString() + "Elo§7: " + ChatColor.WHITE + oppEloRankText);
            }
        }
        if (playerData != null && playerData.getOptions().getScoreboard() == ProfileOptionsItemState.SHOW_PING && opponentPlayer != null && !match.isPartyMatch() && !match.isFFA()) {
            final PlayerData opponentData = this.plugin.getPlayerManager().getPlayerData(opponentPlayer.getUniqueId());
            if (opponentData != null) {
                strings.add(" ");
                strings.add(ChatColor.RED.toString() + "Ping§7: " + ChatColor.GREEN + PlayerUtil.getPing(player) + "ms" + ChatColor.GRAY + " \u2503 " + ChatColor.RED + PlayerUtil.getPing(opponentPlayer) + "ms");
            }
        }
        if (match.getStartTime() != null) {
            final String duration = TimeUtils.formatLongIntoMMSS(ChronoUnit.SECONDS.between(match.getStartTime().toInstant(), Instant.now()));
            strings.add(ChatColor.RED.toString() + "Duration§7: " + ChatColor.WHITE + duration);
        }
        strings.add(ChatColor.GRAY.toString() + ChatColor.STRIKETHROUGH + "-------------------");
        return strings;
    }
    
    private List<String> getSpectatorBoard(final Player spectator) {
        final List<String> strings = new LinkedList<String>();
        final Match match = this.plugin.getMatchManager().getSpectatingMatch(spectator.getUniqueId());
        if (match != null) {
            strings.add(ChatColor.GRAY.toString() + ChatColor.STRIKETHROUGH + "-------------------");
            strings.add(ChatColor.RED.toString() + "Ladder§7: " + ChatColor.WHITE + ((match.getKit() == null) ? "Unknown" : match.getKit().getName()));
            strings.add(" ");
            Player player = null;
            Player opponentPlayer = null;
            if (!match.isPartyMatch() && !match.isFFA()) {
                player = this.plugin.getServer().getPlayer((UUID)match.getTeams().get(0).getPlayers().get(0));
                opponentPlayer = this.plugin.getServer().getPlayer((UUID)match.getTeams().get(1).getPlayers().get(0));
                strings.add(Profile.getByUuidIfAvailable(player.getUniqueId()).getRank().getGameColor() + player.getName() + ChatColor.RED + " (" + PlayerUtil.getPing(player) + "ms)");
                strings.add(ChatColor.RED + "vs");
                strings.add(Profile.getByUuidIfAvailable(opponentPlayer.getUniqueId()).getRank().getGameColor() + opponentPlayer.getName() + ChatColor.RED + " (" + PlayerUtil.getPing(opponentPlayer) + "ms)");
            }
            else if (match.isPartyMatch() && !match.isFFA()) {
                player = this.plugin.getServer().getPlayer((UUID)match.getTeams().get(0).getPlayers().get(0));
                opponentPlayer = this.plugin.getServer().getPlayer((UUID)match.getTeams().get(1).getPlayers().get(0));
                strings.add(Profile.getByUuidIfAvailable(player.getUniqueId()).getRank().getGameColor() + player.getName() + "'s Team" + ChatColor.RED + " (" + PlayerUtil.getPing(player) + "ms)");
                strings.add(ChatColor.RED + "vs");
                strings.add(Profile.getByUuidIfAvailable(opponentPlayer.getUniqueId()).getRank().getGameColor() + opponentPlayer.getName() + "'s Team" + ChatColor.RED + " (" + PlayerUtil.getPing(opponentPlayer) + "ms)");
            }
            else if (match.isFFA()) {
                final int alive = match.getTeams().get(0).getAlivePlayers().size() - 1;
                strings.add(ChatColor.RED.toString() + "Remaining§7: " + ChatColor.WHITE + match.getTeams().get(0).getAlivePlayers().size() + " player" + ((alive == 1) ? "" : "s"));
            }
            if (match.getStartTime() != null) {
                strings.add(" ");
                final String duration = TimeUtils.formatLongIntoMMSS(ChronoUnit.SECONDS.between(match.getStartTime().toInstant(), Instant.now()));
                strings.add(ChatColor.RED.toString() + "Duration§7: " + ChatColor.WHITE + duration);
            }
            strings.add(ChatColor.GRAY.toString() + ChatColor.STRIKETHROUGH + "-------------------");
            return strings;
        }
        final PracticeEvent<?> practiceEvent = (PracticeEvent<?>)this.plugin.getEventManager().getSpectatingEvent(spectator.getUniqueId());
        if (practiceEvent == null) {
            strings.add(ChatColor.GRAY.toString() + ChatColor.STRIKETHROUGH + "-------------------");
            strings.add(ChatColor.RED.toString() + "Finding event info...");
            strings.add(ChatColor.GRAY.toString() + ChatColor.STRIKETHROUGH + "-------------------");
            return strings;
        }
        strings.add(ChatColor.GRAY.toString() + ChatColor.STRIKETHROUGH + "-------------------");
        strings.add(ChatColor.DARK_RED + "Event " + ChatColor.GRAY + "(" + practiceEvent.getName() + ")");
        strings.addAll(practiceEvent.getScoreboardSpectator(spectator));
        strings.add(ChatColor.GRAY.toString() + ChatColor.STRIKETHROUGH + "-------------------");
        return strings;
    }
    
    private List<String> getFFABoard(final Player player) {
        final List<String> strings = new LinkedList<String>();
        strings.add(ChatColor.GRAY.toString() + ChatColor.STRIKETHROUGH + "-------------------");
        strings.add(ChatColor.RED.toString() + "Players§7: " + ChatColor.WHITE + this.plugin.getFfaManager().getTotalPlaying());
        strings.add(ChatColor.RED.toString() + "Kills§7: " + ChatColor.WHITE + this.plugin.getFfaManager().getKillStreakTracker().getOrDefault(player.getUniqueId(), 0));
        strings.add(ChatColor.GRAY.toString() + ChatColor.STRIKETHROUGH + "-------------------");
        return strings;
    }
    
    @Override
    public long getInterval() {
        return 1L;
    }
    
    static {
        LINE = ChatColor.GRAY.toString() + ChatColor.STRIKETHROUGH + "-------------------";
    }
}
