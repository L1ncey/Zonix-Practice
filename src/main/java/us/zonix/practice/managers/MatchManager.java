package us.zonix.practice.managers;

import java.util.List;
import us.zonix.practice.inventory.InventorySnapshot;
import us.zonix.practice.runnable.RematchRunnable;
import org.bukkit.entity.Entity;
import me.maiko.dexter.profile.Profile;
import java.util.stream.Stream;
import net.minecraft.server.v1_8_R3.EntityPlayer;
import org.bukkit.plugin.Plugin;
import java.util.function.Consumer;
import java.util.Objects;
import java.util.function.Function;
import org.bukkit.potion.PotionEffect;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import us.zonix.practice.util.PlayerUtil;
import us.zonix.practice.player.PlayerState;
import us.zonix.practice.party.Party;
import java.util.Collection;
import us.zonix.practice.util.ItemUtil;
import us.zonix.practice.kit.PlayerKit;
import us.zonix.practice.kit.Kit;
import us.zonix.practice.handler.CustomMovementHandler;
import org.bukkit.inventory.ItemStack;
import us.zonix.practice.event.match.MatchRestartEvent;
import us.zonix.practice.event.match.MatchEndEvent;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.ChatColor;
import us.zonix.practice.match.MatchState;
import org.bukkit.event.Event;
import us.zonix.practice.event.match.MatchStartEvent;
import us.zonix.practice.player.PlayerData;
import java.util.HashSet;
import us.zonix.practice.arena.Arena;
import org.bukkit.entity.Player;
import us.zonix.practice.queue.QueueType;
import java.util.Iterator;
import us.zonix.practice.match.MatchTeam;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import us.zonix.practice.util.TtlHashMap;
import java.util.concurrent.TimeUnit;
import us.zonix.practice.Practice;
import us.zonix.practice.match.Match;
import us.zonix.practice.match.MatchRequest;
import java.util.Set;
import java.util.UUID;
import java.util.Map;

public class MatchManager
{
    private final Map<UUID, Set<MatchRequest>> matchRequests;
    private final Map<UUID, UUID> rematchUUIDs;
    private final Map<UUID, UUID> rematchInventories;
    private final Map<UUID, UUID> spectators;
    private final Map<UUID, Match> matches;
    private final Practice plugin;
    
    public MatchManager() {
        this.matchRequests = new TtlHashMap<UUID, Set<MatchRequest>>(TimeUnit.SECONDS, 30L);
        this.rematchUUIDs = new TtlHashMap<UUID, UUID>(TimeUnit.SECONDS, 30L);
        this.rematchInventories = new TtlHashMap<UUID, UUID>(TimeUnit.SECONDS, 30L);
        this.spectators = new ConcurrentHashMap<UUID, UUID>();
        this.matches = new HashMap<UUID, Match>();
        this.plugin = Practice.getInstance();
    }
    
    public int getFighters() {
        int i = 0;
        for (final Match match : this.matches.values()) {
            for (final MatchTeam matchTeam : match.getTeams()) {
                i += matchTeam.getAlivePlayers().size();
            }
        }
        return i;
    }
    
    public int getFighters(final String ladder, final QueueType type) {
        return (int)this.matches.entrySet().stream().filter(match -> match.getValue().getType() == type).filter(match -> match.getValue().getKit().getName().equals(ladder)).count();
    }
    
    public void createMatchRequest(final Player requester, final Player requested, final Arena arena, final String kitName, final boolean party, final boolean bestOfThree) {
        final MatchRequest request = new MatchRequest(requester.getUniqueId(), requested.getUniqueId(), arena, kitName, party, bestOfThree);
        this.matchRequests.computeIfAbsent(requested.getUniqueId(), k -> new HashSet()).add(request);
    }
    
    public MatchRequest getMatchRequest(final UUID requester, final UUID requested) {
        final Set<MatchRequest> requests = this.matchRequests.get(requested);
        if (requests == null) {
            return null;
        }
        return requests.stream().filter(req -> req.getRequester().equals(requester)).findAny().orElse(null);
    }
    
    public MatchRequest getMatchRequest(final UUID requester, final UUID requested, final String kitName) {
        final Set<MatchRequest> requests = this.matchRequests.get(requested);
        if (requests == null) {
            return null;
        }
        return requests.stream().filter(req -> req.getRequester().equals(requester) && req.getKitName().equals(kitName)).findAny().orElse(null);
    }
    
    public Match getMatch(final PlayerData playerData) {
        return this.matches.get(playerData.getCurrentMatchID());
    }
    
    public Match getMatch(final UUID uuid) {
        final PlayerData playerData = this.plugin.getPlayerManager().getPlayerData(uuid);
        return this.getMatch(playerData);
    }
    
    public Match getMatchFromUUID(final UUID uuid) {
        return this.matches.get(uuid);
    }
    
    public Match getSpectatingMatch(final UUID uuid) {
        return this.matches.get(this.spectators.get(uuid));
    }
    
    public void removeMatchRequests(final UUID uuid) {
        this.matchRequests.remove(uuid);
    }
    
    public void createMatch(final Match match) {
        this.matches.put(match.getMatchId(), match);
        this.plugin.getServer().getPluginManager().callEvent((Event)new MatchStartEvent(match));
    }
    
    public void removeFighter(final Player player, final PlayerData playerData, final boolean spectateDeath) {
        final Match match = this.matches.get(playerData.getCurrentMatchID());
        final Player killer = player.getKiller();
        final MatchTeam entityTeam = match.getTeams().get(playerData.getTeamID());
        final MatchTeam winningTeam = match.isFFA() ? entityTeam : match.getTeams().get((entityTeam.getTeamID() == 0) ? 1 : 0);
        if (match.getMatchState() == MatchState.ENDING || (spectateDeath && match.getMatchState() == MatchState.RESTARTING)) {
            return;
        }
        final String deathMessage = ChatColor.GOLD.toString() + ChatColor.BOLD + "\u272a " + ChatColor.RED + player.getName() + ChatColor.GRAY + " has " + (match.isBestOfThree() ? "lost this round" : "been eliminated") + ((killer == null) ? "." : (match.isBestOfThree() ? (". Eliminated by " + ChatColor.GREEN + killer.getName()) : (" by " + ChatColor.GREEN + killer.getName())));
        match.broadcast(deathMessage);
        if (match.isRedrover()) {
            if (match.getMatchState() != MatchState.SWITCHING) {
                match.setMatchState(MatchState.SWITCHING);
                match.setCountdown(4);
            }
        }
        else {
            match.addSnapshot(player);
        }
        entityTeam.killPlayer(player.getUniqueId());
        final int remaining = entityTeam.getAlivePlayers().size();
        if (remaining != 0) {
            final Set<Item> items = new HashSet<Item>();
            for (final ItemStack item : player.getInventory().getContents()) {
                if (item != null && item.getType() != Material.AIR) {
                    items.add(player.getWorld().dropItemNaturally(player.getLocation(), item));
                }
            }
            for (final ItemStack item : player.getInventory().getArmorContents()) {
                if (item != null && item.getType() != Material.AIR) {
                    items.add(player.getWorld().dropItemNaturally(player.getLocation(), item));
                }
            }
            this.plugin.getMatchManager().addDroppedItems(match, items);
        }
        if (spectateDeath) {
            this.addDeathSpectator(player, playerData, match);
        }
        final int remainingWinning = winningTeam.onlinePlayers();
        final int remainingLoosing = entityTeam.onlinePlayers();
        if (remainingLoosing == 0) {
            this.plugin.getServer().getPluginManager().callEvent((Event)new MatchEndEvent(match, winningTeam, entityTeam));
            return;
        }
        if (remainingWinning == 0) {
            this.plugin.getServer().getPluginManager().callEvent((Event)new MatchEndEvent(match, entityTeam, winningTeam));
            return;
        }
        if ((match.isFFA() && remaining == 1) || remaining == 0) {
            if (!match.isBestOfThree()) {
                this.plugin.getServer().getPluginManager().callEvent((Event)new MatchEndEvent(match, winningTeam, entityTeam));
                return;
            }
            match.setRounds(match.getRounds() + 1);
            winningTeam.setMatchWins(winningTeam.getMatchWins() + 1);
            if (winningTeam.getMatchWins() == 3) {
                this.plugin.getServer().getPluginManager().callEvent((Event)new MatchEndEvent(match, winningTeam, entityTeam));
                return;
            }
            this.plugin.getServer().getPluginManager().callEvent((Event)new MatchRestartEvent(match, winningTeam, entityTeam));
        }
    }
    
    public void removeMatch(final Match match) {
        this.matches.remove(match.getMatchId());
        CustomMovementHandler.getParkourCheckpoints().remove(match);
    }
    
    public void giveKits(final Player player, final Kit kit) {
        final PlayerData playerData = this.plugin.getPlayerManager().getPlayerData(player.getUniqueId());
        final Collection<PlayerKit> playerKits = playerData.getPlayerKits(kit.getName()).values();
        if (kit.isHcteams()) {
            final Party party = this.plugin.getPartyManager().getParty(player.getUniqueId());
            if (party != null) {
                if (party.getBards().contains(player.getUniqueId())) {
                    this.plugin.getKitManager().getKit("HCBard").applyToPlayer(player);
                }
                else if (party.getArchers().contains(player.getUniqueId())) {
                    this.plugin.getKitManager().getKit("HCArcher").applyToPlayer(player);
                }
                else {
                    this.plugin.getKitManager().getKit("NoDebuff").applyToPlayer(player);
                }
            }
            return;
        }
        if (playerKits.size() == 0) {
            kit.applyToPlayer(player);
        }
        else {
            player.getInventory().setItem(8, this.plugin.getItemManager().getDefaultBook());
            int slot = -1;
            for (final PlayerKit playerKit : playerKits) {
                player.getInventory().setItem(++slot, ItemUtil.createItem(Material.ENCHANTED_BOOK, ChatColor.YELLOW.toString() + ChatColor.BOLD + playerKit.getDisplayName()));
            }
            player.updateInventory();
        }
    }
    
    private void addDeathSpectator(final Player player, final PlayerData playerData, final Match match) {
        this.spectators.put(player.getUniqueId(), match.getMatchId());
        playerData.setPlayerState(PlayerState.SPECTATING);
        PlayerUtil.clearPlayer(player);
        final CraftPlayer playerCp = (CraftPlayer)player;
        final EntityPlayer playerEp = playerCp.getHandle();
        playerEp.getDataWatcher().watch(6, (Object)0.0f);
        playerEp.setFakingDeath(true);
        match.addSpectator(player.getUniqueId());
        match.getTeams().forEach(team -> team.alivePlayers().forEach(member -> member.hidePlayer(player)));
        match.spectatorPlayers().forEach(member -> member.hidePlayer(player));
        final Stream map = player.getActivePotionEffects().stream().map(PotionEffect::getType);
        Objects.requireNonNull(player);
        map.forEach(player::removePotionEffect);
        player.setWalkSpeed(0.2f);
        player.setAllowFlight(true);
        if (match.isRedrover()) {
            for (final MatchTeam team2 : match.getTeams()) {
                for (final UUID alivePlayerUUID : team2.getAlivePlayers()) {
                    final Player alivePlayer = this.plugin.getServer().getPlayer(alivePlayerUUID);
                    if (alivePlayer != null) {
                        player.showPlayer(alivePlayer);
                    }
                }
            }
        }
        player.setWalkSpeed(0.0f);
        if (match.isParty() || match.isFFA()) {
            this.plugin.getServer().getScheduler().runTaskLater((Plugin)this.plugin, () -> player.getInventory().setContents(this.plugin.getItemManager().getPartySpecItems()), 1L);
        }
        player.updateInventory();
    }
    
    public void addRedroverSpectator(final Player player, final Match match) {
        this.spectators.put(player.getUniqueId(), match.getMatchId());
        player.setAllowFlight(true);
        player.setFlying(true);
        player.getInventory().setContents(this.plugin.getItemManager().getPartySpecItems());
        player.updateInventory();
        final PlayerData playerData = this.plugin.getPlayerManager().getPlayerData(player.getUniqueId());
        playerData.setPlayerState(PlayerState.SPECTATING);
    }
    
    public void addSpectator(final Player player, final PlayerData playerData, final Player target, final Match targetMatch) {
        this.spectators.put(player.getUniqueId(), targetMatch.getMatchId());
        if (targetMatch.getMatchState() != MatchState.ENDING && !targetMatch.haveSpectated(player.getUniqueId())) {
            final String spectatorName = Profile.getByUuidIfAvailable(player.getUniqueId()).getRank().getGameColor() + player.getName();
            final String spectatorMessage = ChatColor.WHITE.toString() + ChatColor.BOLD + "* " + ChatColor.GREEN + spectatorName + ChatColor.GRAY + " is spectating your match.";
            if (!player.hasMetadata("modmode")) {
                targetMatch.broadcast(spectatorMessage);
            }
        }
        targetMatch.addSpectator(player.getUniqueId());
        playerData.setPlayerState(PlayerState.SPECTATING);
        player.teleport((Entity)target);
        player.setAllowFlight(true);
        player.setFlying(true);
        player.getInventory().setContents(this.plugin.getItemManager().getSpecItems());
        player.updateInventory();
        this.plugin.getServer().getOnlinePlayers().forEach(online -> {
            online.hidePlayer(player);
            player.hidePlayer(online);
            return;
        });
        final Stream<Player> stream;
        targetMatch.getTeams().forEach(team -> {
            team.alivePlayers();
            Objects.requireNonNull(player);
            stream.forEach(player::showPlayer);
        });
    }
    
    public void addDroppedItem(final Match match, final Item item) {
        match.addEntityToRemove((Entity)item);
        match.addRunnable(this.plugin.getServer().getScheduler().runTaskLater((Plugin)this.plugin, () -> {
            match.removeEntityToRemove((Entity)item);
            item.remove();
        }, 100L).getTaskId());
    }
    
    public void addDroppedItems(final Match match, final Set<Item> items) {
        for (final Item item : items) {
            match.addEntityToRemove((Entity)item);
        }
        final Iterator<Item> iterator2;
        Item item2;
        match.addRunnable(this.plugin.getServer().getScheduler().runTaskLater((Plugin)this.plugin, () -> {
            items.iterator();
            while (iterator2.hasNext()) {
                item2 = iterator2.next();
                match.removeEntityToRemove((Entity)item2);
                item2.remove();
            }
        }, 100L).getTaskId());
    }
    
    public void removeDeathSpectator(final Match match, final Player player) {
        match.removeSpectator(player.getUniqueId());
        this.spectators.remove(player.getUniqueId());
    }
    
    public void removeSpectator(final Player player) {
        final Match match = this.matches.get(this.spectators.get(player.getUniqueId()));
        match.removeSpectator(player.getUniqueId());
        final PlayerData playerData = this.plugin.getPlayerManager().getPlayerData(player.getUniqueId());
        if (match.getTeams().size() > playerData.getTeamID() && playerData.getTeamID() >= 0) {
            final MatchTeam entityTeam = match.getTeams().get(playerData.getTeamID());
            if (entityTeam != null) {
                entityTeam.killPlayer(player.getUniqueId());
            }
        }
        if (match.getMatchState() != MatchState.ENDING && !match.haveSpectated(player.getUniqueId())) {
            match.addHaveSpectated(player.getUniqueId());
        }
        this.spectators.remove(player.getUniqueId());
        this.plugin.getPlayerManager().sendToSpawnAndReset(player);
    }
    
    public void pickPlayer(final Match match) {
        final Player playerA = this.plugin.getServer().getPlayer((UUID)match.getTeams().get(0).getAlivePlayers().get(0));
        final PlayerData playerDataA = this.plugin.getPlayerManager().getPlayerData(playerA.getUniqueId());
        if (playerDataA.getPlayerState() != PlayerState.FIGHTING) {
            playerA.teleport(match.getArena().getA().toBukkitLocation());
            PlayerUtil.clearPlayer(playerA);
            if (match.getKit().isCombo()) {
                playerA.setMaximumNoDamageTicks(4);
            }
            this.plugin.getMatchManager().giveKits(playerA, match.getKit());
            playerDataA.setPlayerState(PlayerState.FIGHTING);
        }
        final Player playerB = this.plugin.getServer().getPlayer((UUID)match.getTeams().get(1).getAlivePlayers().get(0));
        final PlayerData playerDataB = this.plugin.getPlayerManager().getPlayerData(playerB.getUniqueId());
        if (playerDataB.getPlayerState() != PlayerState.FIGHTING) {
            playerB.teleport(match.getArena().getB().toBukkitLocation());
            PlayerUtil.clearPlayer(playerB);
            if (match.getKit().isCombo()) {
                playerB.setMaximumNoDamageTicks(4);
            }
            this.plugin.getMatchManager().giveKits(playerB, match.getKit());
            playerDataB.setPlayerState(PlayerState.FIGHTING);
        }
        for (final MatchTeam team : match.getTeams()) {
            for (final UUID uuid : team.getAlivePlayers()) {
                final Player player = this.plugin.getServer().getPlayer(uuid);
                if (player != null && !playerA.equals(player) && !playerB.equals(player)) {
                    playerA.hidePlayer(player);
                    playerB.hidePlayer(player);
                }
            }
        }
        playerA.showPlayer(playerB);
        playerB.showPlayer(playerA);
        match.broadcast(ChatColor.YELLOW + "Starting duel. " + ChatColor.GREEN + "(" + playerA.getName() + " vs " + playerB.getName() + ") in " + match.getArena().getName());
    }
    
    public void saveRematches(final Match match) {
        if (match.isParty() || match.isFFA()) {
            return;
        }
        final UUID playerOne = match.getTeams().get(0).getLeader();
        final UUID playerTwo = match.getTeams().get(1).getLeader();
        final PlayerData dataOne = this.plugin.getPlayerManager().getPlayerData(playerOne);
        final PlayerData dataTwo = this.plugin.getPlayerManager().getPlayerData(playerTwo);
        if (dataOne != null) {
            this.rematchUUIDs.put(playerOne, playerTwo);
            final InventorySnapshot snapshot = match.getSnapshot(playerTwo);
            if (snapshot != null) {
                dataOne.setLastSnapshot(snapshot);
                this.rematchInventories.put(playerOne, snapshot.getSnapshotId());
            }
            if (dataOne.getRematchID() > -1) {
                this.plugin.getServer().getScheduler().cancelTask(dataOne.getRematchID());
            }
            dataOne.setRematchID(this.plugin.getServer().getScheduler().scheduleSyncDelayedTask((Plugin)this.plugin, (Runnable)new RematchRunnable(playerOne), 600L));
        }
        if (dataTwo != null) {
            this.rematchUUIDs.put(playerTwo, playerOne);
            final InventorySnapshot snapshot = match.getSnapshot(playerOne);
            if (snapshot != null) {
                dataTwo.setLastSnapshot(snapshot);
                this.rematchInventories.put(playerTwo, snapshot.getSnapshotId());
            }
            if (dataTwo.getRematchID() > -1) {
                this.plugin.getServer().getScheduler().cancelTask(dataTwo.getRematchID());
            }
            dataTwo.setRematchID(this.plugin.getServer().getScheduler().scheduleSyncDelayedTask((Plugin)this.plugin, (Runnable)new RematchRunnable(playerTwo), 600L));
        }
    }
    
    public void removeRematch(final UUID uuid) {
        this.rematchUUIDs.remove(uuid);
        this.rematchInventories.remove(uuid);
    }
    
    public List<UUID> getOpponents(final Match match, final Player player) {
        for (final MatchTeam team : match.getTeams()) {
            if (team.getPlayers().contains(player.getUniqueId())) {
                continue;
            }
            return team.getPlayers();
        }
        return null;
    }
    
    public UUID getRematcher(final UUID uuid) {
        return this.rematchUUIDs.get(uuid);
    }
    
    public UUID getRematcherInventory(final UUID uuid) {
        return this.rematchInventories.get(uuid);
    }
    
    public boolean isRematching(final UUID uuid) {
        return this.rematchUUIDs.containsKey(uuid);
    }
    
    public Map<UUID, Match> getMatches() {
        return this.matches;
    }
}
