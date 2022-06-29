package us.zonix.practice.managers;

import us.zonix.practice.util.timer.impl.EnderpearlTimer;
import us.zonix.practice.util.PlayerUtil;
import us.zonix.practice.queue.QueueType;
import us.zonix.practice.arena.Arena;
import java.util.function.Supplier;
import java.util.LinkedHashMap;
import java.util.function.Function;
import java.util.Comparator;
import com.mongodb.client.model.Sorts;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import us.zonix.practice.util.ItemUtil;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import us.zonix.practice.events.sumo.SumoEvent;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.List;
import java.util.Collection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.FindIterable;
import java.util.HashMap;
import us.zonix.practice.kit.Kit;
import us.zonix.practice.kit.PlayerKit;
import org.bukkit.plugin.java.JavaPlugin;
import us.zonix.practice.file.Config;
import com.mongodb.client.model.ReplaceOptions;
import us.zonix.practice.player.EloRank;
import org.bukkit.plugin.Plugin;
import com.mongodb.client.model.Filters;
import us.zonix.practice.mongo.PracticeMongo;
import org.bson.Document;
import us.zonix.practice.player.PlayerState;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import java.util.concurrent.ConcurrentHashMap;
import us.zonix.practice.player.PlayerData;
import java.util.UUID;
import java.util.Map;
import us.zonix.practice.Practice;

public class PlayerManager
{
    private final Practice plugin;
    private final Map<UUID, PlayerData> playerData;
    
    public PlayerManager() {
        this.plugin = Practice.getInstance();
        this.playerData = new ConcurrentHashMap<UUID, PlayerData>();
    }
    
    public void createPlayerData(final Player player) {
        final PlayerData data = new PlayerData(player.getUniqueId());
        this.playerData.put(data.getUniqueId(), data);
        this.loadData(data);
    }
    
    private void loadData(final PlayerData playerData) {
        final Document document;
        final Document statisticsDocument;
        Document ladderDocument;
        final Document partyStats;
        Bukkit.getScheduler().runTaskAsynchronously((Plugin)this.plugin, () -> {
            playerData.setPlayerState(PlayerState.SPAWN);
            document = PracticeMongo.getInstance().getPlayers().find(Filters.eq("uuid", playerData.getUniqueId().toString())).first();
            if (document == null) {
                this.saveData(playerData);
            }
            else {
                statisticsDocument = (Document)document.get("statistics");
                if (statisticsDocument != null) {
                    try {
                        statisticsDocument.keySet().forEach(key -> {
                            ladderDocument = (Document)statisticsDocument.get(key);
                            if (ladderDocument.containsKey("rankedElo")) {
                                playerData.getRankedElo().put(key, ladderDocument.getInteger("rankedElo"));
                            }
                            if (ladderDocument.containsKey("rankedWins")) {
                                playerData.getRankedWins().put(key, ladderDocument.getInteger("rankedWins"));
                            }
                            if (ladderDocument.containsKey("rankedLosses")) {
                                playerData.getRankedLosses().put(key, ladderDocument.getInteger("rankedLosses"));
                            }
                            return;
                        });
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                if (document.containsKey("totalRankedMatches")) {
                    playerData.setPlayedRanked(document.getInteger("totalRankedMatches"));
                }
                if (document.containsKey("totalUnrankedMatches")) {
                    playerData.setPlayedUnranked(document.getInteger("totalUnrankedMatches"));
                }
                if (document.containsKey("totalUnrankedWins")) {
                    playerData.setUnrankedWins(document.getInteger("totalUnrankedWins"));
                }
                if (document.containsKey("partyStatistics")) {
                    partyStats = document.get((Object)"partyStatistics", Document.class);
                    playerData.setPlayedBard(partyStats.getInteger("playedBard"));
                    playerData.setPlayedArcher(partyStats.getInteger("playedArcher"));
                }
                this.saveConfigPlayerData(playerData);
            }
        });
    }
    
    public void removePlayerData(final UUID uuid) {
        this.plugin.getServer().getScheduler().runTaskAsynchronously((Plugin)this.plugin, () -> {
            this.saveData(this.playerData.get(uuid));
            this.playerData.remove(uuid);
        });
    }
    
    public void saveData(final PlayerData playerData) {
        if (playerData == null) {
            return;
        }
        final Document document = new Document();
        final Document statisticsDocument = new Document();
        final Document document2;
        Document ladderDocument;
        playerData.getRankedWins().forEach((key, value) -> {
            if (document2.containsKey(key)) {
                ladderDocument = (Document)document2.get(key);
            }
            else {
                ladderDocument = new Document();
            }
            ladderDocument.put("rankedWins", (Object)value);
            document2.put(key, (Object)ladderDocument);
            return;
        });
        final Document document3;
        Document ladderDocument2;
        playerData.getRankedLosses().forEach((key, value) -> {
            if (document3.containsKey(key)) {
                ladderDocument2 = (Document)document3.get(key);
            }
            else {
                ladderDocument2 = new Document();
            }
            ladderDocument2.put("rankedLosses", (Object)value);
            document3.put(key, (Object)ladderDocument2);
            return;
        });
        final Document document4;
        Document ladderDocument3;
        playerData.getRankedElo().forEach((key, value) -> {
            if (document4.containsKey(key)) {
                ladderDocument3 = (Document)document4.get(key);
            }
            else {
                ladderDocument3 = new Document();
            }
            ladderDocument3.put("rankedElo", (Object)value);
            document4.put(key, (Object)ladderDocument3);
            return;
        });
        document.put("uuid", (Object)playerData.getUniqueId().toString());
        document.put("statistics", (Object)statisticsDocument);
        document.put("totalRankedMatches", (Object)playerData.getPlayedRanked());
        document.put("totalUnrankedMatches", (Object)playerData.getPlayedUnranked());
        document.put("totalUnrankedWins", (Object)playerData.getUnrankedWins());
        document.put("partyStatistics", (Object)new Document().append("playedBard", playerData.getPlayedBard()).append("playedArcher", playerData.getPlayedArcher()));
        document.put("eloRank", (Object)EloRank.getRankByElo(playerData.getGlobalStats("ELO")).name());
        PracticeMongo.getInstance().getPlayers().replaceOne(Filters.eq("uuid", playerData.getUniqueId().toString()), document, new ReplaceOptions().upsert(true));
        final Config config = new Config("/players/" + playerData.getUniqueId().toString(), this.plugin);
        final Map<Integer, PlayerKit> playerKits;
        final Config config2;
        this.plugin.getKitManager().getKits().forEach(kit -> {
            playerKits = playerData.getPlayerKits(kit.getName());
            if (playerKits != null) {
                playerKits.forEach((key, value) -> {
                    config2.getConfig().set("playerkits." + kit.getName() + "." + key + ".displayName", (Object)value.getDisplayName());
                    config2.getConfig().set("playerkits." + kit.getName() + "." + key + ".contents", (Object)value.getContents());
                });
            }
            return;
        });
        config.save();
    }
    
    public Map<String, Integer> getEloByKit(final Kit kit) {
        final Map<String, Integer> eloHash = new HashMap<String, Integer>();
        final FindIterable<Document> documents = PracticeMongo.getInstance().getPlayers().find();
        if (documents == null) {
            return null;
        }
        for (final Document document : documents) {
            final Document statisticsDocument = (Document)document.get("statistics");
            if (statisticsDocument != null) {
                final Document ladderDocument = (Document)statisticsDocument.get(kit.getName());
                if (!ladderDocument.containsKey("rankedElo")) {
                    continue;
                }
                eloHash.put(document.getString("uuid"), ladderDocument.getInteger("rankedElo"));
            }
        }
        return eloHash;
    }
    
    public Collection<PlayerData> getAllData() {
        return this.playerData.values();
    }
    
    public PlayerData getPlayerData(final UUID uuid) {
        PlayerData playerData = this.playerData.get(uuid);
        if (playerData == null) {
            final Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                this.createPlayerData(player);
                playerData = this.playerData.get(uuid);
            }
        }
        return playerData;
    }
    
    public List<Player> getPlayersByState(final PlayerState state) {
        return Bukkit.getOnlinePlayers().parallelStream().filter(player -> this.getPlayerData(player.getUniqueId()).getPlayerState().equals(state)).collect((Collector<? super Object, ?, List<Player>>)Collectors.toList());
    }
    
    public void giveLobbyItems(final Player player) {
        final boolean inParty = this.plugin.getPartyManager().getParty(player.getUniqueId()) != null;
        final boolean inTournament = this.plugin.getTournamentManager().getTournament(player.getUniqueId()) != null;
        final boolean inEvent = this.plugin.getEventManager().getEventPlaying(player) != null;
        final boolean inSumo = this.plugin.getEventManager().getEventPlaying(player) != null && this.plugin.getEventManager().getEventPlaying(player) instanceof SumoEvent;
        final boolean isRematching = this.plugin.getMatchManager().isRematching(player.getUniqueId());
        ItemStack[] items = this.plugin.getItemManager().getSpawnItems();
        if (inTournament) {
            items = this.plugin.getItemManager().getTournamentItems();
        }
        else if (inSumo) {
            items = this.plugin.getItemManager().getSumoItems();
        }
        else if (inEvent) {
            items = this.plugin.getItemManager().getEventItems();
        }
        else if (inParty) {
            items = this.plugin.getItemManager().getPartyItems();
        }
        player.getInventory().setContents(items);
        if (isRematching && !inParty && !inTournament && !inEvent) {
            player.getInventory().setItem(3, ItemUtil.createItem(Material.INK_SACK, ChatColor.GREEN + "Rematch", 1, (short)10));
            player.getInventory().setItem(5, ItemUtil.createItem(Material.PAPER, ChatColor.YELLOW + "Inventories", 1, (short)0));
        }
        player.updateInventory();
    }
    
    private void saveConfigPlayerData(final PlayerData playerData) {
        final Config config = new Config("/players/" + playerData.getUniqueId().toString(), this.plugin);
        final ConfigurationSection playerKitsSection = config.getConfig().getConfigurationSection("playerkits");
        if (playerKitsSection != null) {
            final ConfigurationSection kitSection;
            final Integer kitIndex;
            final ConfigurationSection configurationSection;
            final String displayName;
            final ItemStack[] contents;
            final PlayerKit playerKit;
            this.plugin.getKitManager().getKits().forEach(kit -> {
                kitSection = playerKitsSection.getConfigurationSection(kit.getName());
                if (kitSection != null) {
                    kitSection.getKeys(false).forEach(kitKey -> {
                        kitIndex = Integer.parseInt(kitKey);
                        displayName = configurationSection.getString(kitKey + ".displayName");
                        contents = ((List)configurationSection.get(kitKey + ".contents")).toArray(new ItemStack[0]);
                        playerKit = new PlayerKit(kit.getName(), kitIndex, contents, displayName);
                        playerData.addPlayerKit(kitIndex, playerKit);
                    });
                }
            });
        }
    }
    
    public HashMap<String, Integer> findTopEloByKit(final String kitName, final int limit) {
        HashMap<String, Integer> eloMap = new HashMap<String, Integer>();
        final FindIterable<Document> documents = PracticeMongo.getInstance().getPlayers().find().sort(Sorts.descending("statistics." + kitName + ".rankedElo")).limit(limit);
        for (final Document document : documents) {
            final Document statisticsDocument = (Document)document.get("statistics");
            if (statisticsDocument != null) {
                final Document ladderDocument = (Document)statisticsDocument.get(kitName);
                if (ladderDocument == null) {
                    continue;
                }
                eloMap.put(document.getString("uuid"), ladderDocument.getInteger("rankedElo"));
            }
        }
        eloMap = eloMap.entrySet().stream().sorted(Comparator.comparing((Function<? super Object, ? extends Comparable>)Map.Entry::getValue).reversed()).collect((Supplier<HashMap<String, Integer>>)LinkedHashMap::new, (map, e) -> map.put(e.getKey(), (Integer)e.getValue()), HashMap::putAll);
        return eloMap;
    }
    
    public void sendToSpawnAndReset(final Player player) {
        final PlayerData playerData = this.getPlayerData(player.getUniqueId());
        playerData.setPlayerState(PlayerState.SPAWN);
        playerData.setBestOfThreeDuel(false);
        playerData.setBestOfThreeKit(null);
        playerData.setBestOfThreeArena(null);
        playerData.setBestOfThreeQueueType(null);
        PlayerUtil.clearPlayer(player);
        this.plugin.getTimerManager().getTimer(EnderpearlTimer.class).clearCooldown(player.getUniqueId());
        this.giveLobbyItems(player);
        if (!player.isOnline()) {
            return;
        }
        if (this.plugin.getSpawnManager().getSpawnLocation() == null) {
            player.teleport(player.getWorld().getSpawnLocation());
        }
        else {
            player.teleport(this.plugin.getSpawnManager().getSpawnLocation().toBukkitLocation());
        }
        final PlayerData playerData2;
        final boolean playerSeen;
        final boolean pSeen;
        this.plugin.getServer().getOnlinePlayers().forEach(p -> {
            playerSeen = (playerData2.getOptions().isVisibility() && player.hasPermission("practice.visibility") && Practice.getInstance().getPlayerManager().getPlayerData(player.getUniqueId()).getPlayerState() == PlayerState.SPAWN);
            pSeen = (playerData2.getOptions().isVisibility() && player.hasPermission("practice.visibility") && Practice.getInstance().getPlayerManager().getPlayerData(p.getUniqueId()).getPlayerState() == PlayerState.SPAWN);
            if (playerSeen) {
                p.showPlayer(player);
            }
            else {
                p.hidePlayer(player);
            }
            if (pSeen) {
                player.showPlayer(p);
            }
            else {
                player.hidePlayer(p);
            }
            return;
        });
        if (player.hasPermission("practice.fly")) {
            player.setAllowFlight(true);
            player.setFlying(true);
        }
    }
}
