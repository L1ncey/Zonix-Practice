package us.zonix.practice.player;

import java.util.Iterator;
import us.zonix.practice.Practice;
import java.util.HashMap;
import us.zonix.practice.arena.Arena;
import us.zonix.practice.queue.QueueType;
import us.zonix.practice.kit.Kit;
import us.zonix.practice.settings.ProfileOptions;
import us.zonix.practice.inventory.InventorySnapshot;
import java.util.UUID;
import us.zonix.practice.kit.PlayerKit;
import java.util.Map;

public class PlayerData
{
    public static final int DEFAULT_ELO = 1000;
    private final Map<String, Map<Integer, PlayerKit>> playerKits;
    private final Map<String, Integer> rankedLosses;
    private final Map<String, Integer> rankedWins;
    private final Map<String, Integer> rankedElo;
    private final Map<String, Integer> partyElo;
    private final UUID uniqueId;
    private PlayerState playerState;
    private UUID currentMatchID;
    private InventorySnapshot lastSnapshot;
    private UUID duelSelecting;
    private ProfileOptions options;
    private int eloRange;
    private int pingRange;
    private int teamID;
    private int rematchID;
    private int missedPots;
    private int longestCombo;
    private int combo;
    private int hits;
    private boolean bestOfThreeDuel;
    private Kit bestOfThreeKit;
    private QueueType bestOfThreeQueueType;
    private Arena bestOfThreeArena;
    private boolean leaving;
    private int oitcEventWins;
    private int sumoEventWins;
    private int waterDropEventWins;
    private int parkourEventWins;
    private int redroverEventWins;
    private int playedArcher;
    private int playedBard;
    private int playedRanked;
    private int playedUnranked;
    private int unrankedWins;
    private int currentCps;
    private int cps;
    
    public int getWins(final String kitName) {
        return this.rankedWins.computeIfAbsent(kitName, k -> 0);
    }
    
    public void setWins(final String kitName, final int wins) {
        this.rankedWins.put(kitName, wins);
    }
    
    public int getLosses(final String kitName) {
        return this.rankedLosses.computeIfAbsent(kitName, k -> 0);
    }
    
    public void setLosses(final String kitName, final int losses) {
        this.rankedLosses.put(kitName, losses);
    }
    
    public int getElo(final String kitName) {
        return this.rankedElo.computeIfAbsent(kitName, k -> 1000);
    }
    
    public void setElo(final String kitName, final int elo) {
        this.rankedElo.put(kitName, elo);
    }
    
    public int getPartyElo(final String kitName) {
        return this.partyElo.computeIfAbsent(kitName, k -> 1000);
    }
    
    public void incrementPlayedBard() {
        ++this.playedBard;
    }
    
    public void incrementPlayedArcher() {
        ++this.playedBard;
    }
    
    public void incrementRanked() {
        ++this.playedRanked;
    }
    
    public void incrementUnranked() {
        ++this.playedUnranked;
    }
    
    public void incrementUnrankedWins() {
        ++this.unrankedWins;
    }
    
    public void setPartyElo(final String kitName, final int elo) {
        this.partyElo.put(kitName, elo);
    }
    
    public void addPlayerKit(final int index, final PlayerKit playerKit) {
        this.getPlayerKits(playerKit.getName()).put(index, playerKit);
    }
    
    public Map<Integer, PlayerKit> getPlayerKits(final String kitName) {
        return this.playerKits.computeIfAbsent(kitName, k -> new HashMap());
    }
    
    public int getGlobalStats(final String type) {
        int i = 0;
        int count = 0;
        for (final Kit kit : Practice.getInstance().getKitManager().getKits()) {
            final String upperCase = type.toUpperCase();
            switch (upperCase) {
                case "ELO": {
                    i += this.getElo(kit.getName());
                    break;
                }
                case "WINS": {
                    i += this.getWins(kit.getName());
                    break;
                }
                case "LOSSES": {
                    i += this.getLosses(kit.getName());
                    break;
                }
            }
            ++count;
        }
        if (i == 0) {
            i = 0;
        }
        if (count == 0) {
            count = 1;
        }
        return type.toUpperCase().equalsIgnoreCase("ELO") ? Math.round(i / count) : i;
    }
    
    public Map<String, Map<Integer, PlayerKit>> getPlayerKits() {
        return this.playerKits;
    }
    
    public Map<String, Integer> getRankedLosses() {
        return this.rankedLosses;
    }
    
    public Map<String, Integer> getRankedWins() {
        return this.rankedWins;
    }
    
    public Map<String, Integer> getRankedElo() {
        return this.rankedElo;
    }
    
    public Map<String, Integer> getPartyElo() {
        return this.partyElo;
    }
    
    public int getPlayedArcher() {
        return this.playedArcher;
    }
    
    public int getPlayedBard() {
        return this.playedBard;
    }
    
    public int getPlayedRanked() {
        return this.playedRanked;
    }
    
    public int getPlayedUnranked() {
        return this.playedUnranked;
    }
    
    public int getUnrankedWins() {
        return this.unrankedWins;
    }
    
    public int getCurrentCps() {
        return this.currentCps;
    }
    
    public int getCps() {
        return this.cps;
    }
    
    public void setPlayerState(final PlayerState playerState) {
        this.playerState = playerState;
    }
    
    public void setCurrentMatchID(final UUID currentMatchID) {
        this.currentMatchID = currentMatchID;
    }
    
    public void setLastSnapshot(final InventorySnapshot lastSnapshot) {
        this.lastSnapshot = lastSnapshot;
    }
    
    public void setDuelSelecting(final UUID duelSelecting) {
        this.duelSelecting = duelSelecting;
    }
    
    public void setOptions(final ProfileOptions options) {
        this.options = options;
    }
    
    public void setEloRange(final int eloRange) {
        this.eloRange = eloRange;
    }
    
    public void setPingRange(final int pingRange) {
        this.pingRange = pingRange;
    }
    
    public void setTeamID(final int teamID) {
        this.teamID = teamID;
    }
    
    public void setRematchID(final int rematchID) {
        this.rematchID = rematchID;
    }
    
    public void setMissedPots(final int missedPots) {
        this.missedPots = missedPots;
    }
    
    public void setLongestCombo(final int longestCombo) {
        this.longestCombo = longestCombo;
    }
    
    public void setCombo(final int combo) {
        this.combo = combo;
    }
    
    public void setHits(final int hits) {
        this.hits = hits;
    }
    
    public void setOitcEventWins(final int oitcEventWins) {
        this.oitcEventWins = oitcEventWins;
    }
    
    public void setSumoEventWins(final int sumoEventWins) {
        this.sumoEventWins = sumoEventWins;
    }
    
    public void setWaterDropEventWins(final int waterDropEventWins) {
        this.waterDropEventWins = waterDropEventWins;
    }
    
    public void setParkourEventWins(final int parkourEventWins) {
        this.parkourEventWins = parkourEventWins;
    }
    
    public void setRedroverEventWins(final int redroverEventWins) {
        this.redroverEventWins = redroverEventWins;
    }
    
    public void setPlayedArcher(final int playedArcher) {
        this.playedArcher = playedArcher;
    }
    
    public void setPlayedBard(final int playedBard) {
        this.playedBard = playedBard;
    }
    
    public void setPlayedRanked(final int playedRanked) {
        this.playedRanked = playedRanked;
    }
    
    public void setPlayedUnranked(final int playedUnranked) {
        this.playedUnranked = playedUnranked;
    }
    
    public void setUnrankedWins(final int unrankedWins) {
        this.unrankedWins = unrankedWins;
    }
    
    public void setCurrentCps(final int currentCps) {
        this.currentCps = currentCps;
    }
    
    public void setCps(final int cps) {
        this.cps = cps;
    }
    
    public PlayerData(final UUID uniqueId) {
        this.playerKits = new HashMap<String, Map<Integer, PlayerKit>>();
        this.rankedLosses = new HashMap<String, Integer>();
        this.rankedWins = new HashMap<String, Integer>();
        this.rankedElo = new HashMap<String, Integer>();
        this.partyElo = new HashMap<String, Integer>();
        this.playerState = PlayerState.LOADING;
        this.options = new ProfileOptions();
        this.eloRange = 50;
        this.pingRange = -1;
        this.teamID = -1;
        this.rematchID = -1;
        this.leaving = false;
        this.uniqueId = uniqueId;
    }
    
    public UUID getUniqueId() {
        return this.uniqueId;
    }
    
    public PlayerState getPlayerState() {
        return this.playerState;
    }
    
    public UUID getCurrentMatchID() {
        return this.currentMatchID;
    }
    
    public InventorySnapshot getLastSnapshot() {
        return this.lastSnapshot;
    }
    
    public UUID getDuelSelecting() {
        return this.duelSelecting;
    }
    
    public ProfileOptions getOptions() {
        return this.options;
    }
    
    public int getEloRange() {
        return this.eloRange;
    }
    
    public int getPingRange() {
        return this.pingRange;
    }
    
    public int getTeamID() {
        return this.teamID;
    }
    
    public int getRematchID() {
        return this.rematchID;
    }
    
    public int getMissedPots() {
        return this.missedPots;
    }
    
    public int getLongestCombo() {
        return this.longestCombo;
    }
    
    public int getCombo() {
        return this.combo;
    }
    
    public int getHits() {
        return this.hits;
    }
    
    public boolean isBestOfThreeDuel() {
        return this.bestOfThreeDuel;
    }
    
    public void setBestOfThreeDuel(final boolean bestOfThreeDuel) {
        this.bestOfThreeDuel = bestOfThreeDuel;
    }
    
    public Kit getBestOfThreeKit() {
        return this.bestOfThreeKit;
    }
    
    public void setBestOfThreeKit(final Kit bestOfThreeKit) {
        this.bestOfThreeKit = bestOfThreeKit;
    }
    
    public QueueType getBestOfThreeQueueType() {
        return this.bestOfThreeQueueType;
    }
    
    public void setBestOfThreeQueueType(final QueueType bestOfThreeQueueType) {
        this.bestOfThreeQueueType = bestOfThreeQueueType;
    }
    
    public Arena getBestOfThreeArena() {
        return this.bestOfThreeArena;
    }
    
    public void setBestOfThreeArena(final Arena bestOfThreeArena) {
        this.bestOfThreeArena = bestOfThreeArena;
    }
    
    public boolean isLeaving() {
        return this.leaving;
    }
    
    public void setLeaving(final boolean leaving) {
        this.leaving = leaving;
    }
    
    public int getOitcEventWins() {
        return this.oitcEventWins;
    }
    
    public int getSumoEventWins() {
        return this.sumoEventWins;
    }
    
    public int getWaterDropEventWins() {
        return this.waterDropEventWins;
    }
    
    public int getParkourEventWins() {
        return this.parkourEventWins;
    }
    
    public int getRedroverEventWins() {
        return this.redroverEventWins;
    }
}
