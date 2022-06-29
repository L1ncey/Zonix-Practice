package us.zonix.practice.listeners;

import us.zonix.practice.events.EventState;
import us.zonix.practice.events.PracticeEvent;
import me.maiko.dexter.util.event.PreShutdownEvent;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.PotionSplashEvent;
import java.util.Map;
import us.zonix.practice.party.Party;
import net.edater.spigot.knockback.KnockbackProfile;
import us.zonix.practice.util.EloUtil;
import org.bukkit.Bukkit;
import me.maiko.dexter.util.CC;
import me.maiko.dexter.profile.Profile;
import java.util.LinkedHashMap;
import us.zonix.practice.inventory.InventorySnapshot;
import java.util.UUID;
import us.zonix.practice.util.Clickable;
import net.edater.spigot.EdaterSpigot;
import us.zonix.practice.event.match.MatchEndEvent;
import us.zonix.practice.match.MatchState;
import us.zonix.practice.match.MatchTeam;
import us.zonix.practice.event.match.MatchRestartEvent;
import org.bukkit.event.EventHandler;
import java.util.Iterator;
import us.zonix.practice.CustomLocation;
import us.zonix.practice.player.PlayerData;
import java.util.Set;
import us.zonix.practice.kit.Kit;
import us.zonix.practice.match.Match;
import org.bukkit.plugin.Plugin;
import us.zonix.practice.runnable.MatchRunnable;
import us.zonix.practice.player.PlayerState;
import us.zonix.practice.util.PlayerUtil;
import org.bukkit.entity.Player;
import java.util.HashSet;
import org.bukkit.ChatColor;
import us.zonix.practice.event.match.MatchStartEvent;
import us.zonix.practice.Practice;
import org.bukkit.event.Listener;

public class MatchListener implements Listener
{
    private final Practice plugin;
    
    public MatchListener() {
        this.plugin = Practice.getInstance();
    }
    
    @EventHandler
    public void onMatchStart(final MatchStartEvent event) {
        final Match match = event.getMatch();
        final Kit kit = match.getKit();
        if (!kit.isEnabled()) {
            match.broadcast(ChatColor.RED + "This kit is currently disabled.");
            this.plugin.getMatchManager().removeMatch(match);
            return;
        }
        if (kit.isBuild() || kit.isSpleef()) {
            if (match.getArena().getAvailableArenas().size() <= 0) {
                match.broadcast(ChatColor.RED + "There are no arenas available at this moment.");
                this.plugin.getMatchManager().removeMatch(match);
                return;
            }
            match.setStandaloneArena(match.getArena().getAvailableArena());
            this.plugin.getArenaManager().setArenaMatchUUID(match.getStandaloneArena(), match.getMatchId());
        }
        final Set<Player> matchPlayers = new HashSet<Player>();
        final Set<Player> set;
        final PlayerData playerData;
        final Match match2;
        final CustomLocation locationA;
        final CustomLocation locationB;
        final Kit kit2;
        match.getTeams().forEach(team -> team.alivePlayers().forEach(player -> {
            set.add(player);
            this.plugin.getMatchManager().removeMatchRequests(player.getUniqueId());
            playerData = this.plugin.getPlayerManager().getPlayerData(player.getUniqueId());
            player.setAllowFlight(false);
            player.setFlying(false);
            playerData.setCurrentMatchID(match2.getMatchId());
            playerData.setTeamID(team.getTeamID());
            playerData.setMissedPots(0);
            playerData.setLongestCombo(0);
            playerData.setCombo(0);
            playerData.setHits(0);
            PlayerUtil.clearPlayer(player);
            locationA = ((match2.getStandaloneArena() != null) ? match2.getStandaloneArena().getA() : match2.getArena().getA());
            locationB = ((match2.getStandaloneArena() != null) ? match2.getStandaloneArena().getB() : match2.getArena().getB());
            player.teleport((team.getTeamID() == 1) ? locationA.toBukkitLocation() : locationB.toBukkitLocation());
            if (kit2.isCombo()) {
                player.setMaximumNoDamageTicks(4);
            }
            if (!match2.isRedrover()) {
                this.plugin.getMatchManager().giveKits(player, kit2);
                playerData.setPlayerState(PlayerState.FIGHTING);
            }
            else {
                this.plugin.getMatchManager().addRedroverSpectator(player, match2);
            }
        }));
        for (final Player player2 : matchPlayers) {
            for (final Player online : this.plugin.getServer().getOnlinePlayers()) {
                online.hidePlayer(player2);
                player2.hidePlayer(online);
            }
        }
        for (final Player player2 : matchPlayers) {
            for (final Player other : matchPlayers) {
                player2.showPlayer(other);
            }
        }
        new MatchRunnable(match).runTaskTimer((Plugin)this.plugin, 20L, 20L);
    }
    
    @EventHandler
    public void onMatchRestart(final MatchRestartEvent event) {
        final Match match = event.getMatch();
        for (final MatchTeam team2 : match.getTeams()) {
            team2.revivePlayers();
        }
        final Set<Player> matchPlayers = new HashSet<Player>();
        final Set<Player> set;
        final PlayerData playerData;
        final Match match2;
        final CustomLocation locationA;
        final CustomLocation locationB;
        final CustomLocation customLocation;
        final CustomLocation customLocation2;
        match.getTeams().forEach(team -> team.alivePlayers().forEach(player -> {
            set.add(player);
            this.plugin.getMatchManager().removeMatchRequests(player.getUniqueId());
            playerData = this.plugin.getPlayerManager().getPlayerData(player.getUniqueId());
            this.plugin.getMatchManager().giveKits(player, match2.getKit());
            player.setAllowFlight(false);
            player.setFlying(false);
            playerData.setCurrentMatchID(match2.getMatchId());
            playerData.setTeamID(team.getTeamID());
            playerData.setMissedPots(0);
            playerData.setLongestCombo(0);
            playerData.setCombo(0);
            playerData.setHits(0);
            PlayerUtil.clearPlayer(player);
            playerData.setPlayerState(PlayerState.FIGHTING);
            this.plugin.getMatchManager().removeDeathSpectator(match2, player);
            locationA = ((match2.getStandaloneArena() != null) ? match2.getStandaloneArena().getA() : match2.getArena().getA());
            locationB = ((match2.getStandaloneArena() != null) ? match2.getStandaloneArena().getB() : match2.getArena().getB());
            this.plugin.getServer().getScheduler().runTaskLater((Plugin)this.plugin, () -> player.teleport((team.getTeamID() == 1) ? customLocation.toBukkitLocation() : customLocation2.toBukkitLocation()), 5L);
        }));
        for (final Player player2 : matchPlayers) {
            for (final Player online : this.plugin.getServer().getOnlinePlayers()) {
                online.hidePlayer(player2);
                player2.hidePlayer(online);
            }
        }
        for (final Player player2 : matchPlayers) {
            for (final Player other : matchPlayers) {
                player2.showPlayer(other);
            }
        }
        match.broadcast(ChatColor.GREEN + "Starting next round.");
        match.setMatchState(MatchState.RESTARTING);
        match.setCountdown(6);
        final Iterator<Player> iterator6;
        Player matchPlayer;
        this.plugin.getServer().getScheduler().runTaskLater((Plugin)this.plugin, () -> match.spectatorPlayers().forEach(spectator -> {
            matchPlayers.iterator();
            while (iterator6.hasNext()) {
                matchPlayer = iterator6.next();
                spectator.showPlayer(matchPlayer);
            }
        }), 45L);
    }
    
    @EventHandler
    public void onMatchEnd(final MatchEndEvent event) {
        final KnockbackProfile knockbackProfile = EdaterSpigot.INSTANCE.getKnockbackHandler().getActiveProfile();
        final Match match = event.getMatch();
        final Clickable winnerClickable = new Clickable(ChatColor.GREEN + "Winner: ");
        final Clickable loserClickable = new Clickable(ChatColor.RED + "Loser: ");
        match.broadcast(ChatColor.DARK_GRAY.toString() + ChatColor.STRIKETHROUGH + "----------------------------------------------------");
        match.broadcast(ChatColor.GOLD + "Match Results: " + ChatColor.GRAY + "(Clickable Inventories)");
        match.setMatchState(MatchState.ENDING);
        match.setWinningTeamId(event.getWinningTeam().getTeamID());
        match.setCountdown(2);
        if (match.isPartyMatch() && !match.isFFA() && match.getKit().isHcteams()) {
            final Party partyOne = this.plugin.getPartyManager().getParty(match.getTeams().get(0).getLeader());
            final Party partyTwo = this.plugin.getPartyManager().getParty(match.getTeams().get(1).getLeader());
            partyOne.getBards().parallelStream().forEach(bardUuid -> this.plugin.getPlayerManager().getPlayerData(bardUuid).incrementPlayedBard());
            partyTwo.getBards().parallelStream().forEach(bardUuid -> this.plugin.getPlayerManager().getPlayerData(bardUuid).incrementPlayedBard());
            partyOne.getArchers().parallelStream().forEach(archerUuid -> this.plugin.getPlayerManager().getPlayerData(archerUuid).incrementPlayedArcher());
            partyTwo.getArchers().parallelStream().forEach(archerUuid -> this.plugin.getPlayerManager().getPlayerData(archerUuid).incrementPlayedArcher());
            event.getLosingTeam().players().forEach(player -> player.setKnockbackProfile(knockbackProfile));
            event.getWinningTeam().players().forEach(player -> player.setKnockbackProfile(knockbackProfile));
        }
        if (match.isFFA()) {
            final Player winner = this.plugin.getServer().getPlayer((UUID)event.getWinningTeam().getAlivePlayers().get(0));
            final KnockbackProfile knockbackProfile2;
            final Match match2;
            final Player player2;
            final Clickable clickable;
            final Clickable clickable2;
            event.getWinningTeam().players().forEach(player -> {
                player.setKnockbackProfile(knockbackProfile2);
                if (!match2.hasSnapshot(player.getUniqueId())) {
                    match2.addSnapshot(player);
                }
                if (player.getUniqueId() == player2.getUniqueId()) {
                    clickable.add(ChatColor.GRAY + player.getName() + " ", ChatColor.GRAY + "Click to view inventory", "/inventory " + match2.getSnapshot(player.getUniqueId()).getSnapshotId());
                }
                else {
                    clickable2.add(ChatColor.GRAY + player.getName() + " ", ChatColor.GRAY + "Click to view inventory", "/inventory " + match2.getSnapshot(player.getUniqueId()).getSnapshotId());
                }
                return;
            });
            for (final InventorySnapshot snapshot : match.getSnapshots().values()) {
                this.plugin.getInventoryManager().addSnapshot(snapshot);
            }
            match.broadcast(winnerClickable);
            match.broadcast(loserClickable);
            match.broadcast(ChatColor.DARK_GRAY.toString() + ChatColor.STRIKETHROUGH + "----------------------------------------------------");
        }
        else if (match.isRedrover()) {
            match.broadcast(ChatColor.GREEN + event.getWinningTeam().getLeaderName() + ChatColor.GRAY + " has won the redrover!");
            event.getLosingTeam().players().forEach(player -> player.setKnockbackProfile(knockbackProfile));
            event.getWinningTeam().players().forEach(player -> player.setKnockbackProfile(knockbackProfile));
        }
        else {
            final Map<UUID, InventorySnapshot> inventorySnapshotMap = new LinkedHashMap<UUID, InventorySnapshot>();
            final Match match3;
            final KnockbackProfile knockbackProfile3;
            final Profile profile;
            final boolean onWinningTeam;
            final Map<UUID, InventorySnapshot> map;
            final Clickable clickable3;
            final Clickable clickable4;
            match.getTeams().forEach(team -> team.players().forEach(player -> {
                if (!match3.hasSnapshot(player.getUniqueId())) {
                    match3.addSnapshot(player);
                }
                player.setKnockbackProfile(knockbackProfile3);
                profile = Profile.getByUuid(player.getUniqueId());
                onWinningTeam = (this.plugin.getPlayerManager().getPlayerData(player.getUniqueId()).getTeamID() == event.getWinningTeam().getTeamID());
                if (profile != null) {
                    if (match3.getType().isRanked() && onWinningTeam) {
                        profile.awardCoins(player, 35);
                        player.sendMessage(CC.PRIMARY + "You have earned 35 coins for playing a rank match!");
                    }
                    else if (match3.getType().isRanked() && !onWinningTeam) {
                        profile.awardCoins(player, 10);
                        player.sendMessage(CC.PRIMARY + "You have earned 10 coins for competing in a ranked match!");
                    }
                }
                if (profile != null) {
                    if (!match3.getType().isRanked() && onWinningTeam) {
                        profile.awardCoins(player, 10);
                        player.sendMessage(CC.PRIMARY + "You have earned 10 coins for playing a unranked match!");
                    }
                    else if (!match3.getType().isRanked() && !onWinningTeam) {
                        profile.awardCoins(player, 5);
                        player.sendMessage(CC.PRIMARY + "You have earned 5 coins for competing in a unranked match!");
                    }
                }
                map.put(player.getUniqueId(), match3.getSnapshot(player.getUniqueId()));
                if (onWinningTeam) {
                    clickable3.add(ChatColor.GRAY + player.getName() + " ", ChatColor.GRAY + "Click to view inventory", "/inventory " + match3.getSnapshot(player.getUniqueId()).getSnapshotId());
                }
                else {
                    clickable4.add(ChatColor.GRAY + player.getName() + " ", ChatColor.GRAY + "Click to view inventory", "/inventory " + match3.getSnapshot(player.getUniqueId()).getSnapshotId());
                }
                player.setMaximumNoDamageTicks(20);
            }));
            for (final InventorySnapshot snapshot : match.getSnapshots().values()) {
                this.plugin.getInventoryManager().addSnapshot(snapshot);
            }
            match.broadcast(winnerClickable);
            match.broadcast(loserClickable);
            final String kitName = match.getKit().getName();
            final Player winnerLeader = this.plugin.getServer().getPlayer((UUID)event.getWinningTeam().getPlayers().get(0));
            final PlayerData winnerLeaderData = this.plugin.getPlayerManager().getPlayerData(winnerLeader.getUniqueId());
            final Player loserLeader = this.plugin.getServer().getPlayer((UUID)event.getLosingTeam().getPlayers().get(0));
            final PlayerData loserLeaderData = this.plugin.getPlayerManager().getPlayerData(loserLeader.getUniqueId());
            if (match.getType().isBoth()) {
                final int[] preElo = new int[2];
                final int[] newElo = new int[2];
                int winnerElo = 0;
                int loserElo = 0;
                int newWinnerElo = 0;
                int newLoserElo = 0;
                String eloMessage;
                if (event.getWinningTeam().getPlayers().size() == 2) {
                    final UUID winnerUUID = (Bukkit.getPlayer(event.getWinningTeam().getLeader()) == null) ? event.getWinningTeam().getPlayers().get(0) : event.getWinningTeam().getLeader();
                    final Player winnerMember = this.plugin.getServer().getPlayer(winnerUUID);
                    final PlayerData winnerMemberData = this.plugin.getPlayerManager().getPlayerData(winnerMember.getUniqueId());
                    final UUID loserUUID = (Bukkit.getPlayer(event.getLosingTeam().getLeader()) == null) ? event.getLosingTeam().getPlayers().get(0) : event.getLosingTeam().getLeader();
                    final Player loserMember = this.plugin.getServer().getPlayer(loserUUID);
                    final PlayerData loserMemberData = this.plugin.getPlayerManager().getPlayerData(loserMember.getUniqueId());
                    winnerElo = winnerMemberData.getPartyElo(kitName);
                    loserElo = loserMemberData.getPartyElo(kitName);
                    preElo[0] = winnerElo;
                    preElo[1] = loserElo;
                    newWinnerElo = EloUtil.getNewRating(winnerElo, loserElo, true);
                    newLoserElo = EloUtil.getNewRating(loserElo, winnerElo, false);
                    newElo[0] = newWinnerElo;
                    newElo[1] = newLoserElo;
                    winnerMemberData.setPartyElo(kitName, newWinnerElo);
                    loserMemberData.setPartyElo(kitName, newLoserElo);
                    winnerLeaderData.incrementRanked();
                    loserLeaderData.incrementRanked();
                    eloMessage = ChatColor.GRAY + "Elo Changes: " + ChatColor.GREEN + winnerLeader.getName() + ", " + winnerMember.getName() + " +" + (newWinnerElo - winnerElo) + " (" + newWinnerElo + ") " + ChatColor.RED + loserLeader.getName() + ", " + loserMember.getName() + "  " + (newLoserElo - loserElo) + " (" + newLoserElo + ")";
                }
                else {
                    winnerElo = winnerLeaderData.getElo(kitName);
                    loserElo = loserLeaderData.getElo(kitName);
                    preElo[0] = winnerElo;
                    preElo[1] = loserElo;
                    newWinnerElo = EloUtil.getNewRating(winnerElo, loserElo, true);
                    newLoserElo = EloUtil.getNewRating(loserElo, winnerElo, false);
                    newElo[0] = newWinnerElo;
                    newElo[1] = newLoserElo;
                    winnerLeaderData.incrementRanked();
                    loserLeaderData.incrementRanked();
                    eloMessage = ChatColor.GRAY + "Elo Changes: " + ChatColor.GREEN + winnerLeader.getName() + " +" + (newWinnerElo - winnerElo) + " (" + newWinnerElo + ") " + ChatColor.RED + loserLeader.getName() + " " + (newLoserElo - loserElo) + " (" + newLoserElo + ")";
                    winnerLeaderData.setElo(kitName, newWinnerElo);
                    loserLeaderData.setElo(kitName, newLoserElo);
                    winnerLeaderData.setWins(kitName, winnerLeaderData.getWins(kitName) + 1);
                    loserLeaderData.setLosses(kitName, loserLeaderData.getLosses(kitName) + 1);
                }
                match.broadcast(eloMessage);
            }
            winnerLeaderData.incrementUnrankedWins();
            winnerLeaderData.incrementUnranked();
            loserLeaderData.incrementUnranked();
            match.broadcast(ChatColor.DARK_GRAY.toString() + ChatColor.STRIKETHROUGH + "----------------------------------------------------");
            this.plugin.getMatchManager().saveRematches(match);
        }
    }
    
    @EventHandler
    void onPotionSplash(final PotionSplashEvent event) {
        if (event.getEntity().getShooter() instanceof Player) {
            final Player shooter = (Player)event.getEntity().getShooter();
            if (shooter.isSprinting() && event.getIntensity((LivingEntity)shooter) > 0.5) {
                event.setIntensity((LivingEntity)shooter, 1.0);
            }
        }
    }
    
    @EventHandler
    public void onPreStopEvent(final PreShutdownEvent event) {
        for (final Match match : Practice.getInstance().getMatchManager().getMatches().values()) {
            match.setMatchState(MatchState.ENDING);
            match.setCountdown(3);
        }
        for (final PracticeEvent practiceEvent : Practice.getInstance().getEventManager().getEvents().values()) {
            if (practiceEvent.getState() == EventState.STARTED) {
                practiceEvent.end();
            }
        }
        for (final Integer tournamentId : Practice.getInstance().getTournamentManager().getTournaments().keySet()) {
            Practice.getInstance().getTournamentManager().removeTournament(tournamentId, true);
        }
    }
}
