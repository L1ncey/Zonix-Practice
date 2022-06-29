package us.zonix.practice.managers;

import java.util.function.Consumer;
import java.util.List;
import java.util.Collection;
import java.util.ArrayList;
import java.util.stream.Stream;
import java.util.function.Function;
import java.util.Objects;
import us.zonix.practice.party.Party;
import us.zonix.practice.arena.Arena;
import us.zonix.practice.kit.Kit;
import java.util.Iterator;
import us.zonix.practice.match.Match;
import us.zonix.practice.match.MatchTeam;
import java.util.Collections;
import org.apache.commons.lang.StringUtils;
import us.zonix.practice.player.EloRank;
import org.bukkit.ChatColor;
import us.zonix.practice.player.PlayerState;
import us.zonix.practice.queue.QueueType;
import us.zonix.practice.player.PlayerData;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import us.zonix.practice.Practice;
import us.zonix.practice.queue.QueueEntry;
import java.util.UUID;
import java.util.Map;

public class QueueManager
{
    private final Map<UUID, QueueEntry> queued;
    private final Map<UUID, Long> playerQueueTime;
    private final Practice plugin;
    private boolean rankedEnabled;
    
    public QueueManager() {
        this.queued = new ConcurrentHashMap<UUID, QueueEntry>();
        this.playerQueueTime = new HashMap<UUID, Long>();
        this.plugin = Practice.getInstance();
        this.rankedEnabled = true;
        this.plugin.getServer().getScheduler().runTaskTimer((Plugin)this.plugin, () -> this.queued.forEach((key, value) -> {
            if (value.isParty()) {
                this.findMatch(this.plugin.getPartyManager().getParty(key), value.getKitName(), value.getElo(), value.getQueueType(), value.isBestOfThree());
            }
            else {
                this.findMatch(this.plugin.getServer().getPlayer(key), value.getKitName(), value.getElo(), value.getQueueType(), value.isBestOfThree());
            }
        }), 20L, 20L);
    }
    
    public void addPlayerToQueue(final Player player, final PlayerData playerData, final String kitName, final QueueType type, final boolean bestOfThree) {
        if (type != QueueType.UNRANKED && !this.rankedEnabled) {
            player.closeInventory();
            return;
        }
        playerData.setPlayerState(PlayerState.QUEUE);
        final int elo = type.isBoth() ? playerData.getElo(kitName) : 0;
        final QueueEntry entry = new QueueEntry(type, kitName, bestOfThree, elo, false);
        this.queued.put(playerData.getUniqueId(), entry);
        this.giveQueueItems(player);
        final String unrankedMessage = ChatColor.YELLOW + "You have been added to the " + ChatColor.GOLD + "Unranked " + kitName + ChatColor.YELLOW + " queue.";
        final String[] eloRank = EloRank.getRankByElo(elo).name().split("_");
        final String eloRankText = StringUtils.capitalize(eloRank[0].toLowerCase()) + " " + eloRank[1];
        final String rankedMessage = ChatColor.YELLOW + "You have been added to the " + ChatColor.GOLD + "Ranked " + kitName + ChatColor.YELLOW + " queue. " + ChatColor.GRAY + "[" + ChatColor.WHITE + eloRankText + ChatColor.GRAY + " \u2758 " + ChatColor.GREEN + elo + ChatColor.GRAY + "]";
        final String premiumMessage = ChatColor.YELLOW + "You have been added to the " + ChatColor.GOLD + "Premium " + kitName + ChatColor.YELLOW + " queue. " + ChatColor.GRAY + "[" + ChatColor.WHITE + eloRankText + ChatColor.GRAY + " \u2758 " + ChatColor.GREEN + elo + ChatColor.GRAY + "]";
        player.sendMessage((type == QueueType.UNRANKED) ? unrankedMessage : ((type == QueueType.PREMIUM) ? premiumMessage : rankedMessage));
        this.playerQueueTime.put(player.getUniqueId(), System.currentTimeMillis());
    }
    
    private void giveQueueItems(final Player player) {
        player.closeInventory();
        player.getInventory().setContents(this.plugin.getItemManager().getQueueItems());
        player.updateInventory();
    }
    
    public QueueEntry getQueueEntry(final UUID uuid) {
        return this.queued.get(uuid);
    }
    
    public long getPlayerQueueTime(final UUID uuid) {
        return this.playerQueueTime.get(uuid);
    }
    
    public int getQueueSize(final String ladder, final QueueType type) {
        return (int)this.queued.entrySet().stream().filter(entry -> entry.getValue().getQueueType() == type).filter(entry -> entry.getValue().getKitName().equals(ladder)).count();
    }
    
    private boolean findMatch(final Player player, final String kitName, final int elo, final QueueType type, final boolean bestOfThree) {
        final long queueTime = System.currentTimeMillis() - this.playerQueueTime.get(player.getUniqueId());
        final PlayerData playerData = this.plugin.getPlayerManager().getPlayerData(player.getUniqueId());
        if (playerData == null) {
            this.plugin.getLogger().warning(player.getName() + "'s player data is null");
            return false;
        }
        int eloRange = playerData.getEloRange();
        final EloRank eloRank = EloRank.getRankByElo(elo);
        int pingRange = player.hasPermission("practice.pingmatching") ? playerData.getPingRange() : -1;
        final int seconds = Math.round(queueTime / 1000L);
        if (seconds > 5 && type != QueueType.UNRANKED) {
            if (pingRange != -1) {
                pingRange += (seconds - 5) * 25;
            }
            if (eloRange != -1) {
                eloRange += seconds * 50;
                if (eloRange >= eloRank.getEloRange()) {
                    eloRange = eloRank.getEloRange();
                }
            }
        }
        if (eloRange == -1) {
            eloRange = Integer.MAX_VALUE;
        }
        if (pingRange == -1) {
            pingRange = Integer.MAX_VALUE;
        }
        final int ping = 0;
        for (final UUID opponent : this.queued.keySet()) {
            if (opponent == player.getUniqueId()) {
                continue;
            }
            final QueueEntry queueEntry = this.queued.get(opponent);
            if (!queueEntry.getKitName().equals(kitName)) {
                continue;
            }
            if (queueEntry.getQueueType() != type) {
                continue;
            }
            if (queueEntry.isBestOfThree() != bestOfThree) {
                continue;
            }
            if (queueEntry.isParty()) {
                continue;
            }
            final Player opponentPlayer = this.plugin.getServer().getPlayer(opponent);
            final PlayerData opponentData = this.plugin.getPlayerManager().getPlayerData(opponent);
            if (opponentData.getPlayerState() == PlayerState.FIGHTING) {
                continue;
            }
            if (playerData.getPlayerState() == PlayerState.FIGHTING) {
                continue;
            }
            final EloRank opponentEloRank = EloRank.getRankByElo(opponentData.getElo(kitName));
            final int eloDiff = Math.abs(queueEntry.getElo() - elo);
            if (type.isBoth()) {
                if (eloDiff > eloRange) {
                    continue;
                }
                final long opponentQueueTime = System.currentTimeMillis() - this.playerQueueTime.get(opponentPlayer.getUniqueId());
                int opponentEloRange = opponentData.getEloRange();
                int opponentPingRange = player.hasPermission("practice.pingmatching") ? opponentData.getPingRange() : -1;
                final int opponentSeconds = Math.round(opponentQueueTime / 1000L);
                if (opponentSeconds > 5) {
                    if (opponentPingRange != -1) {
                        opponentPingRange += (opponentSeconds - 5) * 25;
                    }
                    if (opponentEloRange != -1) {
                        opponentEloRange += opponentSeconds * 50;
                        if (opponentEloRange >= opponentEloRank.getEloRange()) {
                            opponentEloRange = opponentEloRank.getEloRange();
                        }
                    }
                }
                if (opponentEloRange == -1) {
                    opponentEloRange = Integer.MAX_VALUE;
                }
                if (opponentPingRange == -1) {
                    opponentPingRange = Integer.MAX_VALUE;
                }
                if (eloDiff > opponentEloRange) {
                    continue;
                }
                final int pingDiff = Math.abs(0 - ping);
                if (type == QueueType.RANKED || type == QueueType.PREMIUM) {
                    if (pingDiff > opponentPingRange) {
                        continue;
                    }
                    if (pingDiff > pingRange) {
                        continue;
                    }
                }
            }
            final Kit kit = this.plugin.getKitManager().getKit(kitName);
            final Arena arena = this.plugin.getArenaManager().getRandomArena(kit);
            String playerFoundMatchMessage;
            String matchedFoundMatchMessage;
            if (type.isBoth()) {
                final String[] playerEloRank = EloRank.getRankByElo(this.queued.get(player.getUniqueId()).getElo()).name().split("_");
                final String playerEloRankText = StringUtils.capitalize(playerEloRank[0].toLowerCase()) + " " + playerEloRank[1];
                playerFoundMatchMessage = ChatColor.RED + player.getName() + ChatColor.GRAY + " [" + ChatColor.WHITE + playerEloRankText + " \u2758 " + this.queued.get(player.getUniqueId()).getElo() + ChatColor.GRAY + "]";
                final String[] matchedEloRank = EloRank.getRankByElo(this.queued.get(opponentPlayer.getUniqueId()).getElo()).name().split("_");
                final String matchedEloRankText = StringUtils.capitalize(matchedEloRank[0].toLowerCase()) + " " + matchedEloRank[1];
                matchedFoundMatchMessage = ChatColor.RED + opponentPlayer.getName() + ChatColor.GRAY + " [" + ChatColor.WHITE + matchedEloRankText + " \u2758 " + this.queued.get(opponentPlayer.getUniqueId()).getElo() + ChatColor.GRAY + "]";
            }
            else {
                playerFoundMatchMessage = ChatColor.RED + player.getName() + ".";
                matchedFoundMatchMessage = ChatColor.RED + opponentPlayer.getName() + ".";
            }
            player.sendMessage(ChatColor.YELLOW + "Starting duel against " + matchedFoundMatchMessage);
            opponentPlayer.sendMessage(ChatColor.YELLOW + "Starting duel against " + playerFoundMatchMessage);
            final MatchTeam teamA = new MatchTeam(player.getUniqueId(), Collections.singletonList(player.getUniqueId()), 0);
            final MatchTeam teamB = new MatchTeam(opponentPlayer.getUniqueId(), Collections.singletonList(opponentPlayer.getUniqueId()), 1);
            final Match match = new Match(arena, kit, type, bestOfThree, new MatchTeam[] { teamA, teamB });
            this.plugin.getMatchManager().createMatch(match);
            this.queued.remove(player.getUniqueId());
            this.queued.remove(opponentPlayer.getUniqueId());
            this.playerQueueTime.remove(player.getUniqueId());
            return true;
        }
        return false;
    }
    
    public void removePlayerFromQueue(final Player player) {
        final QueueEntry entry = this.queued.get(player.getUniqueId());
        this.queued.remove(player.getUniqueId());
        this.plugin.getPlayerManager().sendToSpawnAndReset(player);
        player.sendMessage(ChatColor.RED + "You have left the " + entry.getQueueType().getName() + " " + entry.getKitName() + " queue.");
    }
    
    public void addPartyToQueue(final Player leader, final Party party, final String kitName, final QueueType type, final boolean bestOfThree) {
        if (type.isRanked() && !this.rankedEnabled) {
            leader.closeInventory();
        }
        else if (party.getMembers().size() != 2) {
            leader.sendMessage(ChatColor.RED + "There must be at least 2 players in your party to do this.");
            leader.closeInventory();
        }
        else {
            final Stream<Object> stream = party.getMembers().stream();
            final PlayerManager playerManager = this.plugin.getPlayerManager();
            Objects.requireNonNull(playerManager);
            stream.map((Function<? super Object, ?>)playerManager::getPlayerData).forEach(member -> member.setPlayerState(PlayerState.QUEUE));
            final PlayerData playerData = this.plugin.getPlayerManager().getPlayerData(leader.getUniqueId());
            final int elo = type.isRanked() ? playerData.getPartyElo(kitName) : -1;
            this.queued.put(playerData.getUniqueId(), new QueueEntry(type, kitName, bestOfThree, elo, true));
            this.giveQueueItems(leader);
            final String unrankedMessage = ChatColor.YELLOW + "Your party has been added to the " + ChatColor.GREEN + "Unranked 2v2 " + kitName + ChatColor.YELLOW + " queue.";
            final String rankedMessage = ChatColor.YELLOW + "Your party has been added to the " + ChatColor.GREEN + "Ranked 2v2 " + kitName + ChatColor.YELLOW + " queue with " + ChatColor.GREEN + elo + " elo" + ChatColor.YELLOW + ".";
            party.broadcast(type.isRanked() ? rankedMessage : unrankedMessage);
            this.playerQueueTime.put(party.getLeader(), System.currentTimeMillis());
            this.findMatch(party, kitName, elo, type, bestOfThree);
        }
    }
    
    private void findMatch(final Party partyA, final String kitName, final int elo, final QueueType type, final boolean bestOfThree) {
        if (!this.playerQueueTime.containsKey(partyA.getLeader())) {
            System.out.println("Is not contained found..");
            return;
        }
        final long queueTime = System.currentTimeMillis() - this.playerQueueTime.get(partyA.getLeader());
        final PlayerData playerData = this.plugin.getPlayerManager().getPlayerData(partyA.getLeader());
        if (playerData == null) {
            System.out.println("Null was found..");
            return;
        }
        int eloRange = playerData.getEloRange();
        final int seconds = Math.round(queueTime / 1000L);
        if (seconds > 5 && type.isRanked()) {
            eloRange += seconds * 50;
            if (eloRange >= 1000) {
                eloRange = 1000;
            }
        }
        final int finalEloRange = eloRange;
        final int n;
        final UUID opponent = this.queued.entrySet().stream().filter(entry -> entry.getKey() != partyA.getLeader()).filter(entry -> this.plugin.getPlayerManager().getPlayerData(entry.getKey()).getPlayerState() == PlayerState.QUEUE).filter(entry -> entry.getValue().isParty()).filter(entry -> entry.getValue().getQueueType() == type).filter(entry -> !type.isRanked() || Math.abs(entry.getValue().getElo() - elo) < n).filter(entry -> entry.getValue().getKitName().equals(kitName)).map((Function<? super Object, ? extends UUID>)Map.Entry::getKey).findFirst().orElse(null);
        if (opponent == null) {
            System.out.println("None found..");
            return;
        }
        final PlayerData opponentData = this.plugin.getPlayerManager().getPlayerData(opponent);
        if (opponentData.getPlayerState() == PlayerState.FIGHTING) {
            return;
        }
        if (playerData.getPlayerState() == PlayerState.FIGHTING) {
            return;
        }
        final Player leaderA = this.plugin.getServer().getPlayer(partyA.getLeader());
        final Player leaderB = this.plugin.getServer().getPlayer(opponent);
        final Party partyB = this.plugin.getPartyManager().getParty(opponent);
        final Kit kit = this.plugin.getKitManager().getKit(kitName);
        final Arena arena = this.plugin.getArenaManager().getRandomArena(kit);
        String partyAFoundMatchMessage;
        String partyBFoundMatchMessage;
        if (type.isRanked()) {
            partyAFoundMatchMessage = ChatColor.GREEN + leaderB.getName() + "'s Party" + ChatColor.YELLOW + " with " + ChatColor.GREEN + "" + this.queued.get(leaderB.getUniqueId()).getElo() + " elo";
            partyBFoundMatchMessage = ChatColor.GREEN + leaderA.getName() + "'s Party" + ChatColor.YELLOW + " with " + ChatColor.GREEN + "" + this.queued.get(leaderA.getUniqueId()).getElo() + " elo";
        }
        else {
            partyAFoundMatchMessage = ChatColor.GREEN + leaderB.getName() + ChatColor.YELLOW + "'s Party.";
            partyBFoundMatchMessage = ChatColor.GREEN + leaderA.getName() + ChatColor.YELLOW + "'s Party.";
        }
        partyA.broadcast(ChatColor.YELLOW + "Starting duel against " + partyAFoundMatchMessage);
        partyB.broadcast(ChatColor.YELLOW + "Starting duel against " + partyBFoundMatchMessage);
        final List<UUID> playersA = new ArrayList<UUID>(partyA.getMembers());
        final List<UUID> playersB = new ArrayList<UUID>(partyB.getMembers());
        final MatchTeam teamA = new MatchTeam(leaderA.getUniqueId(), playersA, 0);
        final MatchTeam teamB = new MatchTeam(leaderB.getUniqueId(), playersB, 1);
        final Match match = new Match(arena, kit, type, bestOfThree, new MatchTeam[] { teamA, teamB });
        this.plugin.getMatchManager().createMatch(match);
        this.queued.remove(partyA.getLeader());
        this.queued.remove(partyB.getLeader());
    }
    
    public void removePartyFromQueue(final Party party) {
        final QueueEntry entry = this.queued.get(party.getLeader());
        this.queued.remove(party.getLeader());
        final Stream<Player> members = party.members();
        final PlayerManager playerManager = this.plugin.getPlayerManager();
        Objects.requireNonNull(playerManager);
        members.forEach(playerManager::sendToSpawnAndReset);
        final String type = entry.getQueueType().isRanked() ? "Ranked" : "Unranked";
        party.broadcast(ChatColor.GREEN.toString() + ChatColor.BOLD + "[*] " + ChatColor.YELLOW + "You party has left the " + type + " " + entry.getKitName() + " queue.");
    }
    
    public boolean isRankedEnabled() {
        return this.rankedEnabled;
    }
    
    public void setRankedEnabled(final boolean rankedEnabled) {
        this.rankedEnabled = rankedEnabled;
    }
}
