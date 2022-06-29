package us.zonix.practice.listeners;

import org.bukkit.entity.EntityType;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import java.util.Iterator;
import us.zonix.practice.ffa.killstreak.KillStreak;
import us.zonix.practice.events.oitc.OITCPlayer;
import org.bukkit.plugin.Plugin;
import us.zonix.practice.util.PlayerUtil;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.EventPriority;
import us.zonix.practice.events.oitc.OITCEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.entity.Entity;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import java.util.Map;
import org.bukkit.inventory.PlayerInventory;
import us.zonix.practice.kit.Kit;
import org.bukkit.inventory.Inventory;
import us.zonix.practice.events.woolmixup.WoolMixUpEvent;
import us.zonix.practice.events.waterdrop.WaterDropEvent;
import us.zonix.practice.events.parkour.ParkourEvent;
import org.bukkit.command.CommandSender;
import me.maiko.dexter.Dexter;
import us.zonix.practice.kit.PlayerKit;
import org.bukkit.potion.Potion;
import us.zonix.practice.match.MatchState;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.ChatColor;
import org.bukkit.block.Sign;
import org.bukkit.GameMode;
import org.bukkit.event.player.PlayerKickEvent;
import us.zonix.practice.events.PracticeEvent;
import us.zonix.practice.party.Party;
import org.bukkit.event.player.PlayerQuitEvent;
import me.maiko.dexter.util.StringUtil;
import me.maiko.dexter.util.CC;
import us.zonix.practice.board.Board;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import us.zonix.practice.player.PlayerData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import us.zonix.practice.player.PlayerState;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.entity.Player;
import org.bukkit.Material;
import org.bukkit.event.player.PlayerInteractEvent;
import us.zonix.practice.CustomLocation;
import java.util.UUID;
import us.zonix.practice.match.Match;
import java.util.HashMap;
import us.zonix.practice.Practice;
import org.bukkit.event.Listener;

public class PlayerListener implements Listener
{
    private final Practice plugin;
    private static HashMap<Match, HashMap<UUID, CustomLocation>> parkourCheckpoints;
    private HashMap<UUID, Long> waterCooldown;
    
    public PlayerListener() {
        this.plugin = Practice.getInstance();
        this.waterCooldown = new HashMap<UUID, Long>();
    }
    
    @EventHandler
    public void onPlayerInteractSoup(final PlayerInteractEvent event) {
        final Player player = event.getPlayer();
        if (!player.isDead() && player.getItemInHand().getType() == Material.MUSHROOM_SOUP && player.getHealth() < 19.0) {
            final double newHealth = (player.getHealth() + 7.0 > 20.0) ? 20.0 : (player.getHealth() + 7.0);
            player.setHealth(newHealth);
            player.getItemInHand().setType(Material.BOWL);
            player.updateInventory();
        }
    }
    
    @EventHandler
    public void onPlayerItemConsume(final PlayerItemConsumeEvent event) {
        if (event.getItem().getType() == Material.GOLDEN_APPLE) {
            if (!event.getItem().hasItemMeta() || !event.getItem().getItemMeta().getDisplayName().contains("Golden Head")) {
                return;
            }
            final PlayerData playerData = this.plugin.getPlayerManager().getPlayerData(event.getPlayer().getUniqueId());
            if (playerData.getPlayerState() == PlayerState.FIGHTING) {
                final Player player = event.getPlayer();
                event.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 200, 1));
                event.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 2400, 0));
                player.setFoodLevel(Math.min(player.getFoodLevel() + 6, 20));
            }
        }
    }
    
    @EventHandler
    public void onRegenerate(final EntityRegainHealthEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        if (event.getRegainReason() != EntityRegainHealthEvent.RegainReason.SATIATED) {
            return;
        }
        final Player player = (Player)event.getEntity();
        final PlayerData playerData = this.plugin.getPlayerManager().getPlayerData(player.getUniqueId());
        if (playerData.getPlayerState() == PlayerState.FIGHTING) {
            final Match match = this.plugin.getMatchManager().getMatch(player.getUniqueId());
            if (match.getKit().isBuild()) {
                event.setCancelled(true);
            }
        }
    }
    
    @EventHandler
    public void onPlayerJoin(final PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        if (Practice.getInstance().getBoardManager() != null) {
            Practice.getInstance().getBoardManager().getPlayerBoards().put(player.getUniqueId(), new Board(player, Practice.getInstance().getBoardManager().getAdapter()));
        }
        this.plugin.getPlayerManager().createPlayerData(player);
        this.plugin.getPlayerManager().sendToSpawnAndReset(player);
        final String[] message = { StringUtil.getBorderLine(CC.DARK_RED + CC.S), StringUtil.center(CC.RED + " " + CC.DARK_RED + CC.BOLD + "NA Practice"), " ", CC.RED + "NA Practice Season One Started on (" + Practice.getInstance().getConfig().getString("Release-Day") + ")", " ", CC.GRAY + "Duel your enemies, play events, tournaments, and much more! ", CC.GRAY + "To get started join a queue! Compete to be number one!", " ", CC.DARK_RED + CC.BOLD + "Ranked Prizes", CC.GREEN + "#1 " + CC.GRAY + "-" + CC.WHITE + " $50.00 PayPal", CC.WHITE + "#2 " + CC.GRAY + "-" + CC.WHITE + " $30.00 PayPal", CC.GOLD + "#3 " + CC.GRAY + "-" + CC.WHITE + " $20.00 PayPal", StringUtil.getBorderLine(CC.DARK_RED + CC.S) };
        player.sendMessage(message);
    }
    
    @EventHandler
    public void onPlayerQuit(final PlayerQuitEvent event) {
        final Player player = event.getPlayer();
        if (Practice.getInstance().getBoardManager() != null) {
            Practice.getInstance().getBoardManager().getPlayerBoards().remove(player.getUniqueId());
        }
        final Party party = this.plugin.getPartyManager().getParty(player.getUniqueId());
        final PlayerData playerData = this.plugin.getPlayerManager().getPlayerData(player.getUniqueId());
        if (playerData == null) {
            return;
        }
        switch (playerData.getPlayerState()) {
            case FIGHTING: {
                playerData.setLeaving(true);
                this.plugin.getMatchManager().removeFighter(player, playerData, false);
                break;
            }
            case SPECTATING: {
                if (this.plugin.getEventManager().getSpectators().containsKey(player.getUniqueId())) {
                    this.plugin.getEventManager().removeSpectator(player);
                    break;
                }
                this.plugin.getMatchManager().removeSpectator(player);
                break;
            }
            case EDITING: {
                this.plugin.getEditorManager().removeEditor(player.getUniqueId());
                break;
            }
            case QUEUE: {
                if (party == null) {
                    this.plugin.getQueueManager().removePlayerFromQueue(player);
                    break;
                }
                if (this.plugin.getPartyManager().isLeader(player.getUniqueId())) {
                    this.plugin.getQueueManager().removePartyFromQueue(party);
                    break;
                }
                break;
            }
            case FFA: {
                this.plugin.getFfaManager().removePlayer(player);
                break;
            }
            case EVENT: {
                final PracticeEvent practiceEvent = this.plugin.getEventManager().getEventPlaying(player);
                if (practiceEvent != null) {
                    practiceEvent.leave(player);
                    break;
                }
                break;
            }
            case TRAINING: {
                this.plugin.getBotManager().forceRemoveMatch(player);
                break;
            }
        }
        this.plugin.getTournamentManager().leaveTournament(player);
        this.plugin.getPartyManager().leaveParty(player);
        this.plugin.getMatchManager().removeMatchRequests(player.getUniqueId());
        this.plugin.getPartyManager().removePartyInvites(player.getUniqueId());
        this.plugin.getPlayerManager().removePlayerData(player.getUniqueId());
    }
    
    @EventHandler
    public void onPlayerKick(final PlayerKickEvent event) {
        final Player player = event.getPlayer();
        final Party party = this.plugin.getPartyManager().getParty(player.getUniqueId());
        final PlayerData playerData = this.plugin.getPlayerManager().getPlayerData(player.getUniqueId());
        if (playerData == null) {
            return;
        }
        switch (playerData.getPlayerState()) {
            case FIGHTING: {
                playerData.setLeaving(true);
                this.plugin.getMatchManager().removeFighter(player, playerData, false);
                break;
            }
            case SPECTATING: {
                if (this.plugin.getEventManager().getSpectators().containsKey(player.getUniqueId())) {
                    this.plugin.getEventManager().removeSpectator(player);
                    break;
                }
                this.plugin.getMatchManager().removeSpectator(player);
                break;
            }
            case EDITING: {
                this.plugin.getEditorManager().removeEditor(player.getUniqueId());
                break;
            }
            case QUEUE: {
                if (party == null) {
                    this.plugin.getQueueManager().removePlayerFromQueue(player);
                    break;
                }
                if (this.plugin.getPartyManager().isLeader(player.getUniqueId())) {
                    this.plugin.getQueueManager().removePartyFromQueue(party);
                    break;
                }
                break;
            }
            case FFA: {
                this.plugin.getFfaManager().removePlayer(player);
                break;
            }
            case EVENT: {
                final PracticeEvent practiceEvent = this.plugin.getEventManager().getEventPlaying(player);
                if (practiceEvent != null) {
                    practiceEvent.leave(player);
                    break;
                }
                break;
            }
            case TRAINING: {
                this.plugin.getBotManager().forceRemoveMatch(player);
                break;
            }
        }
        this.plugin.getTournamentManager().leaveTournament(player);
        this.plugin.getPartyManager().leaveParty(player);
        this.plugin.getMatchManager().removeMatchRequests(player.getUniqueId());
        this.plugin.getPartyManager().removePartyInvites(player.getUniqueId());
        this.plugin.getPlayerManager().removePlayerData(player.getUniqueId());
    }
    
    @EventHandler
    public void onPlayerInteract(final PlayerInteractEvent event) {
        final Player player = event.getPlayer();
        if (player.getGameMode() == GameMode.CREATIVE) {
            return;
        }
        final PlayerData playerData = this.plugin.getPlayerManager().getPlayerData(player.getUniqueId());
        if (playerData.getPlayerState() == PlayerState.SPECTATING) {
            event.setCancelled(true);
        }
        if (event.getAction().name().endsWith("_BLOCK")) {
            if (event.getClickedBlock().getType().name().contains("SIGN") && event.getClickedBlock().getState() instanceof Sign) {
                final Sign sign = (Sign)event.getClickedBlock().getState();
                if (ChatColor.stripColor(sign.getLine(1)).equals("[Soup]")) {
                    event.setCancelled(true);
                    final Inventory inventory = this.plugin.getServer().createInventory((InventoryHolder)null, 54, ChatColor.DARK_GRAY + "Soup Refill");
                    for (int i = 0; i < 54; ++i) {
                        inventory.setItem(i, new ItemStack(Material.MUSHROOM_SOUP));
                    }
                    event.getPlayer().openInventory(inventory);
                }
            }
            if (event.getClickedBlock().getType() == Material.CHEST || event.getClickedBlock().getType() == Material.ENDER_CHEST) {
                event.setCancelled(true);
            }
            if (event.getClickedBlock().getType().name().contains("DOOR")) {
                event.setCancelled(true);
            }
            if (event.getClickedBlock().getType().name().contains("FENCE")) {
                event.setCancelled(true);
            }
        }
        Label_2019: {
            if (event.getAction().name().startsWith("RIGHT_")) {
                final ItemStack item = event.getItem();
                final Party party = this.plugin.getPartyManager().getParty(player.getUniqueId());
                Label_1801: {
                    switch (playerData.getPlayerState()) {
                        case LOADING: {
                            player.sendMessage(ChatColor.RED + "Please wait until your player data is loaded.");
                            break;
                        }
                        case FIGHTING: {
                            if (item == null) {
                                return;
                            }
                            final Match match = this.plugin.getMatchManager().getMatch(playerData);
                            switch (item.getType()) {
                                case POTION: {
                                    if (match.getMatchState() != MatchState.STARTING) {
                                        break;
                                    }
                                    if (!Potion.fromItemStack(item).isSplash()) {
                                        break;
                                    }
                                    event.setCancelled(true);
                                    player.sendMessage(ChatColor.RED + "You can't throw pots in your current state!");
                                    player.updateInventory();
                                    break;
                                }
                                case ENDER_PEARL: {
                                    if (match.getMatchState() == MatchState.STARTING) {
                                        event.setCancelled(true);
                                        player.sendMessage(ChatColor.RED + "You can't throw enderpearls in your current state!");
                                        player.updateInventory();
                                        break;
                                    }
                                    break;
                                }
                                case ENCHANTED_BOOK: {
                                    final Kit kit = match.getKit();
                                    final PlayerInventory inventory2 = player.getInventory();
                                    final int kitIndex = inventory2.getHeldItemSlot();
                                    if (kitIndex == 8) {
                                        kit.applyToPlayer(player);
                                        break;
                                    }
                                    final Map<Integer, PlayerKit> kits = playerData.getPlayerKits(kit.getName());
                                    final PlayerKit playerKit = kits.get(kitIndex + 1);
                                    if (playerKit != null) {
                                        playerKit.applyToPlayer(player);
                                        break;
                                    }
                                    break;
                                }
                            }
                            break;
                        }
                        case SPAWN: {
                            if (item == null) {
                                return;
                            }
                            if (Dexter.getInstance().getShutdownTask() != null) {
                                player.sendMessage(ChatColor.RED + "Server is about to restart.");
                                return;
                            }
                            switch (item.getType()) {
                                case DIAMOND_SWORD: {
                                    if (party != null && !this.plugin.getPartyManager().isLeader(player.getUniqueId())) {
                                        player.sendMessage(ChatColor.RED + "You are not the leader of this party.");
                                        return;
                                    }
                                    if (!player.hasPermission("practice.ranked.bypass") && playerData.getUnrankedWins() < 10) {
                                        player.sendMessage(ChatColor.RED + "You must win " + (10 - playerData.getUnrankedWins()) + " Unranked Matches to join this queue.");
                                        return;
                                    }
                                    player.openInventory(this.plugin.getInventoryManager().getRankedInventory().getCurrentPage());
                                    break;
                                }
                                case GOLD_SWORD: {
                                    if (party != null) {
                                        return;
                                    }
                                    player.openInventory(this.plugin.getInventoryManager().getPremiumInventory().getCurrentPage());
                                    break;
                                }
                                case IRON_SWORD: {
                                    if (party != null && !this.plugin.getPartyManager().isLeader(player.getUniqueId())) {
                                        player.sendMessage(ChatColor.RED + "You are not the leader of this party.");
                                        return;
                                    }
                                    player.openInventory(this.plugin.getInventoryManager().getUnrankedInventory().getCurrentPage());
                                    break;
                                }
                                case INK_SACK: {
                                    final UUID rematching = this.plugin.getMatchManager().getRematcher(player.getUniqueId());
                                    final Player rematcher = this.plugin.getServer().getPlayer(rematching);
                                    if (rematcher == null) {
                                        player.sendMessage(ChatColor.RED + "That player is not online.");
                                        return;
                                    }
                                    if (this.plugin.getMatchManager().getMatchRequest(rematcher.getUniqueId(), player.getUniqueId()) != null) {
                                        this.plugin.getServer().dispatchCommand((CommandSender)player, "accept " + rematcher.getName());
                                        break;
                                    }
                                    this.plugin.getServer().dispatchCommand((CommandSender)player, "duel " + rematcher.getName());
                                    break;
                                }
                                case IRON_AXE: {
                                    this.plugin.getFfaManager().addPlayer(player);
                                    break;
                                }
                                case NAME_TAG: {
                                    this.plugin.getPartyManager().createParty(player);
                                    break;
                                }
                                case EMERALD: {
                                    player.performCommand("leaderboard");
                                    break;
                                }
                                case BOOK: {
                                    player.openInventory(this.plugin.getInventoryManager().getEditorInventory().getCurrentPage());
                                    break;
                                }
                                case SKULL_ITEM: {
                                    player.performCommand("party info");
                                    break;
                                }
                                case WATCH: {
                                    player.performCommand("settings");
                                    break;
                                }
                                case GOLD_AXE: {
                                    if (party != null && !this.plugin.getPartyManager().isLeader(player.getUniqueId())) {
                                        player.sendMessage(ChatColor.RED + "You are not the leader of this party.");
                                        return;
                                    }
                                    player.openInventory(this.plugin.getInventoryManager().getPartyEventInventory().getCurrentPage());
                                    break;
                                }
                                case PAPER: {
                                    if (party == null) {
                                        player.performCommand("inventory " + playerData.getLastSnapshot().getSnapshotId().toString());
                                        return;
                                    }
                                    if (!this.plugin.getPartyManager().isLeader(player.getUniqueId())) {
                                        player.sendMessage(ChatColor.RED + "You are not the leader of this party.");
                                        return;
                                    }
                                    player.openInventory(this.plugin.getInventoryManager().getPartyInventory().getCurrentPage());
                                    break;
                                }
                                case NETHER_STAR: {
                                    this.plugin.getPartyManager().leaveParty(player);
                                    this.plugin.getTournamentManager().leaveTournament(player);
                                    break;
                                }
                            }
                            break;
                        }
                        case QUEUE: {
                            if (item == null) {
                                return;
                            }
                            if (item.getType() != Material.REDSTONE) {
                                break;
                            }
                            if (party == null) {
                                this.plugin.getQueueManager().removePlayerFromQueue(player);
                                break;
                            }
                            this.plugin.getQueueManager().removePartyFromQueue(party);
                            break;
                        }
                        case EVENT: {
                            if (item == null) {
                                return;
                            }
                            final PracticeEvent practiceEvent = this.plugin.getEventManager().getEventPlaying(player);
                            if (item.getType() == Material.NETHER_STAR) {
                                if (practiceEvent != null) {
                                    practiceEvent.leave(player);
                                    break;
                                }
                                break;
                            }
                            else {
                                if (item.getType() != Material.FIREBALL) {
                                    break;
                                }
                                if (practiceEvent != null && practiceEvent instanceof ParkourEvent) {
                                    ((ParkourEvent)practiceEvent).toggleVisibility(player);
                                    break;
                                }
                                if (practiceEvent != null && practiceEvent instanceof WaterDropEvent) {
                                    ((WaterDropEvent)practiceEvent).toggleVisibility(player);
                                    break;
                                }
                                if (practiceEvent != null && practiceEvent instanceof WoolMixUpEvent) {
                                    ((WoolMixUpEvent)practiceEvent).toggleVisibility(player);
                                    break;
                                }
                                break;
                            }
                            break;
                        }
                        case SPECTATING: {
                            if (item == null) {
                                return;
                            }
                            if (item.getType() != Material.NETHER_STAR) {
                                break Label_1801;
                            }
                            if (this.plugin.getEventManager().getSpectators().containsKey(player.getUniqueId())) {
                                this.plugin.getEventManager().removeSpectator(player);
                                break Label_1801;
                            }
                            if (party == null) {
                                this.plugin.getMatchManager().removeSpectator(player);
                                break Label_1801;
                            }
                            this.plugin.getPartyManager().leaveParty(player);
                            break Label_1801;
                        }
                        case EDITING: {
                            if (event.getClickedBlock() == null) {
                                return;
                            }
                            switch (event.getClickedBlock().getType()) {
                                case WALL_SIGN:
                                case SIGN:
                                case SIGN_POST: {
                                    this.plugin.getEditorManager().removeEditor(player.getUniqueId());
                                    this.plugin.getPlayerManager().sendToSpawnAndReset(player);
                                    break Label_2019;
                                }
                                case CHEST: {
                                    final Kit kit2 = this.plugin.getKitManager().getKit(this.plugin.getEditorManager().getEditingKit(player.getUniqueId()));
                                    if (!kit2.getName().contains("Debuff")) {
                                        return;
                                    }
                                    if (kit2.getKitEditContents()[0] != null) {
                                        final Inventory editorInventory = this.plugin.getServer().createInventory((InventoryHolder)null, 36);
                                        editorInventory.setContents(kit2.getKitEditContents());
                                        player.openInventory(editorInventory);
                                        event.setCancelled(true);
                                        break Label_2019;
                                    }
                                    break Label_2019;
                                }
                                case ANVIL: {
                                    player.openInventory(this.plugin.getInventoryManager().getEditingKitInventory(player.getUniqueId()).getCurrentPage());
                                    event.setCancelled(true);
                                    break Label_2019;
                                }
                            }
                            break;
                        }
                    }
                }
            }
        }
    }
    
    @EventHandler
    public void onPlayerDropItem(final PlayerDropItemEvent event) {
        final Player player = event.getPlayer();
        final PlayerData playerData = this.plugin.getPlayerManager().getPlayerData(player.getUniqueId());
        final Material drop = event.getItemDrop().getItemStack().getType();
        switch (playerData.getPlayerState()) {
            case FFA: {
                if (drop == Material.DIAMOND_SWORD || drop == Material.IRON_AXE || drop == Material.DIAMOND_SPADE || drop == Material.BOW) {
                    event.setCancelled(true);
                    break;
                }
                event.getItemDrop().remove();
                break;
            }
            case FIGHTING: {
                if (drop == Material.GLASS_BOTTLE) {
                    event.getItemDrop().remove();
                    break;
                }
                final Match match = this.plugin.getMatchManager().getMatch(event.getPlayer().getUniqueId());
                this.plugin.getMatchManager().addDroppedItem(match, event.getItemDrop());
                break;
            }
            default: {
                event.setCancelled(true);
                break;
            }
        }
    }
    
    @EventHandler
    public void onPlayerPickupItem(final PlayerPickupItemEvent event) {
        final Player player = event.getPlayer();
        final PlayerData playerData = this.plugin.getPlayerManager().getPlayerData(player.getUniqueId());
        if (playerData.getPlayerState() == PlayerState.FIGHTING) {
            final Match match = this.plugin.getMatchManager().getMatch(player.getUniqueId());
            if (match.getEntitiesToRemove().contains(event.getItem())) {
                match.removeEntityToRemove((Entity)event.getItem());
            }
            else {
                event.setCancelled(true);
            }
        }
        else if (playerData.getPlayerState() != PlayerState.FFA) {
            event.setCancelled(true);
        }
    }
    
    @EventHandler(ignoreCancelled = true)
    public void onAsyncPlayerChat(final AsyncPlayerChatEvent event) {
        final Player player = event.getPlayer();
        final Party party = this.plugin.getPartyManager().getParty(player.getUniqueId());
        final String chatMessage = event.getMessage();
        if (party != null) {
            if (chatMessage.startsWith("!") || chatMessage.startsWith("@")) {
                event.setCancelled(true);
                final String message = ChatColor.GOLD + "[Party] " + ChatColor.WHITE + player.getName() + ChatColor.GRAY + ": " + chatMessage.replaceFirst("!", "").replaceFirst("@", "");
                party.broadcast(message);
            }
        }
        else {
            final PlayerKit kitRenaming = this.plugin.getEditorManager().getRenamingKit(player.getUniqueId());
            if (kitRenaming != null) {
                kitRenaming.setDisplayName(ChatColor.translateAlternateColorCodes('&', chatMessage));
                event.setCancelled(true);
                event.getPlayer().sendMessage(ChatColor.YELLOW + "Successfully set kit " + ChatColor.GREEN + kitRenaming.getIndex() + ChatColor.YELLOW + "'s name to " + ChatColor.GREEN + kitRenaming.getDisplayName());
                this.plugin.getEditorManager().removeRenamingKit(event.getPlayer().getUniqueId());
            }
        }
    }
    
    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerRespawn(final PlayerRespawnEvent event) {
        final Player player = event.getPlayer();
        final PlayerData playerData = this.plugin.getPlayerManager().getPlayerData(player.getUniqueId());
        switch (playerData.getPlayerState()) {
            case EVENT: {
                final PracticeEvent currentEvent = this.plugin.getEventManager().getEventPlaying(player);
                if (currentEvent != null && currentEvent instanceof OITCEvent) {
                    event.setRespawnLocation(player.getLocation());
                    currentEvent.onDeath().accept(player);
                    break;
                }
                break;
            }
        }
    }
    
    @EventHandler
    public void onPlayerDeath(final PlayerDeathEvent event) {
        final Player player = event.getEntity();
        final PlayerData playerData = this.plugin.getPlayerManager().getPlayerData(player.getUniqueId());
        switch (playerData.getPlayerState()) {
            case FIGHTING: {
                this.plugin.getMatchManager().removeFighter(player, playerData, true);
                break;
            }
            case TRAINING: {
                PlayerUtil.respawnPlayer(event);
                this.plugin.getServer().getScheduler().runTaskLater((Plugin)this.plugin, () -> this.plugin.getBotManager().removeMatch(player, false), 1L);
                break;
            }
            case EVENT: {
                final PracticeEvent currentEvent = this.plugin.getEventManager().getEventPlaying(player);
                if (currentEvent == null) {
                    break;
                }
                if (currentEvent instanceof OITCEvent) {
                    final OITCEvent oitcEvent = (OITCEvent)currentEvent;
                    final OITCPlayer oitcKiller = oitcEvent.getPlayer(player.getKiller());
                    final OITCPlayer oitcPlayer = oitcEvent.getPlayer(player);
                    oitcPlayer.setLastKiller(oitcKiller);
                    PlayerUtil.respawnPlayer(event);
                    break;
                }
                currentEvent.onDeath().accept(player);
                break;
            }
            case FFA: {
                for (final ItemStack item : player.getInventory().getContents()) {
                    if (item != null && item.getType() == Material.POTION) {
                        this.plugin.getFfaManager().getItemTracker().put(player.getWorld().dropItemNaturally(player.getLocation(), item), System.currentTimeMillis());
                    }
                }
                this.plugin.getFfaManager().getKillStreakTracker().put(player.getUniqueId(), 0);
                StringBuilder deathMessage;
                if (player.getKiller() == null) {
                    deathMessage = new StringBuilder(ChatColor.GOLD.toString() + ChatColor.BOLD + "\u272a " + ChatColor.RED + player.getName() + ChatColor.GRAY + " has been eliminated.");
                }
                else {
                    deathMessage = new StringBuilder(ChatColor.GOLD.toString() + ChatColor.BOLD + "\u272a " + ChatColor.RED + player.getName() + ChatColor.GRAY + " has been eliminated by " + ChatColor.GREEN + player.getKiller().getName());
                    final int ks = this.plugin.getFfaManager().getKillStreakTracker().compute(player.getKiller().getUniqueId(), (k, v) -> ((v == null) ? 0 : v) + 1);
                    for (final KillStreak killStreak : this.plugin.getFfaManager().getKillStreaks()) {
                        if (killStreak.getStreaks().contains(ks)) {
                            killStreak.giveKillStreak(player.getKiller());
                            deathMessage.append("\n").append(ChatColor.GOLD.toString()).append(ChatColor.BOLD).append("\u272a ").append(ChatColor.RED).append(player.getKiller().getName()).append(ChatColor.GRAY).append(" has a (").append(ChatColor.GREEN.toString()).append(ChatColor.BOLD).append(ks).append(ChatColor.GRAY).append(") kill-streak.");
                        }
                    }
                }
                for (final Player online : this.plugin.getServer().getOnlinePlayers()) {
                    if (this.plugin.getPlayerManager().getPlayerData(online.getUniqueId()).getPlayerState() == PlayerState.FFA) {
                        online.sendMessage(deathMessage.toString());
                    }
                }
                this.plugin.getServer().getScheduler().runTask((Plugin)this.plugin, () -> this.plugin.getFfaManager().removePlayer(player));
                break;
            }
        }
        event.setDroppedExp(0);
        event.setDeathMessage((String)null);
        event.getDrops().clear();
    }
    
    @EventHandler
    public void onFoodLevelChange(final FoodLevelChangeEvent event) {
        final Player player = (Player)event.getEntity();
        final PlayerData playerData = this.plugin.getPlayerManager().getPlayerData(player.getUniqueId());
        if (playerData.getPlayerState() == PlayerState.FIGHTING) {
            final Match match = this.plugin.getMatchManager().getMatch(player.getUniqueId());
            if (match.getKit().isParkour() || match.getKit().isSumo() || this.plugin.getEventManager().getEventPlaying(player) != null) {
                event.setCancelled(true);
            }
        }
        else {
            event.setCancelled(true);
        }
    }
    
    @EventHandler
    public void onProjectileLaunch(final ProjectileLaunchEvent event) {
        if (event.getEntity().getShooter() instanceof Player) {
            final Player shooter = (Player)event.getEntity().getShooter();
            final PlayerData shooterData = this.plugin.getPlayerManager().getPlayerData(shooter.getUniqueId());
            if (shooterData.getPlayerState() == PlayerState.FIGHTING) {
                final Match match = this.plugin.getMatchManager().getMatch(shooter.getUniqueId());
                match.addEntityToRemove((Entity)event.getEntity());
            }
        }
    }
    
    @EventHandler
    public void onProjectileHit(final ProjectileHitEvent event) {
        if (event.getEntity().getShooter() instanceof Player) {
            final Player shooter = (Player)event.getEntity().getShooter();
            final PlayerData shooterData = this.plugin.getPlayerManager().getPlayerData(shooter.getUniqueId());
            if (shooterData != null && shooterData.getPlayerState() == PlayerState.FIGHTING) {
                final Match match = this.plugin.getMatchManager().getMatch(shooter.getUniqueId());
                match.removeEntityToRemove((Entity)event.getEntity());
                if (event.getEntityType() == EntityType.ARROW) {
                    event.getEntity().remove();
                }
            }
        }
    }
    
    @EventHandler
    public void onFoodLevelChangeEvent(final FoodLevelChangeEvent event) {
        if (event.getEntity() instanceof Player) {
            final Player player = (Player)event.getEntity();
            player.setSaturation(4.0f);
            player.setExhaustion(2.0f);
        }
    }
    
    static {
        PlayerListener.parkourCheckpoints = new HashMap<Match, HashMap<UUID, CustomLocation>>();
    }
}
