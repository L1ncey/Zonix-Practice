package us.zonix.practice.handler;

import org.bukkit.block.Block;
import us.zonix.practice.events.PracticeEvent;
import java.util.Iterator;
import us.zonix.practice.player.PlayerData;
import us.zonix.practice.events.parkour.ParkourPlayer;
import us.zonix.practice.events.parkour.ParkourEvent;
import us.zonix.practice.events.lights.LightsPlayer;
import us.zonix.practice.events.lights.LightsEvent;
import us.zonix.practice.events.redrover.RedroverPlayer;
import us.zonix.practice.events.redrover.RedroverEvent;
import org.bukkit.ChatColor;
import us.zonix.practice.events.oitc.OITCPlayer;
import us.zonix.practice.events.oitc.OITCEvent;
import org.bukkit.block.BlockFace;
import us.zonix.practice.events.sumo.SumoPlayer;
import us.zonix.practice.events.sumo.SumoEvent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import us.zonix.practice.util.BlockUtil;
import us.zonix.practice.match.MatchState;
import us.zonix.practice.player.PlayerState;
import net.minecraft.server.v1_8_R3.PacketPlayInFlying;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import us.zonix.practice.CustomLocation;
import java.util.UUID;
import us.zonix.practice.match.Match;
import java.util.HashMap;
import us.zonix.practice.Practice;
import net.edater.spigot.handler.MovementHandler;

public class CustomMovementHandler implements MovementHandler
{
    private final Practice plugin;
    private static HashMap<Match, HashMap<UUID, CustomLocation>> parkourCheckpoints;
    private static HashMap<Match, HashMap<UUID, Integer>> bridgesScore;
    
    public CustomMovementHandler() {
        this.plugin = Practice.getInstance();
    }
    
    public void handleUpdateLocation(final Player player, final Location to, final Location from, final PacketPlayInFlying packetPlayInFlying) {
        final PlayerData playerData = this.plugin.getPlayerManager().getPlayerData(player.getUniqueId());
        if (playerData == null) {
            this.plugin.getLogger().warning(player.getName() + "'s player data is null");
            return;
        }
        if (playerData.getPlayerState() == PlayerState.FIGHTING) {
            final Match match = this.plugin.getMatchManager().getMatch(player.getUniqueId());
            if (match == null) {
                return;
            }
            if (match.getKit().isSpleef() || match.getKit().isSumo()) {
                if (match.getMatchState() == MatchState.FIGHTING && (BlockUtil.isOnLiquid(to, 0) || BlockUtil.isOnLiquid(to, 1))) {
                    this.plugin.getMatchManager().removeFighter(player, playerData, true);
                }
                if ((to.getX() != from.getX() || to.getZ() != from.getZ()) && (match.getMatchState() == MatchState.STARTING || match.getMatchState() == MatchState.RESTARTING)) {
                    player.teleport(from);
                }
            }
            if (match.getKit().isParkour()) {
                if (BlockUtil.isStandingOn(player, Material.GOLD_PLATE)) {
                    for (final UUID uuid : this.plugin.getMatchManager().getOpponents(match, player)) {
                        final Player opponent = Bukkit.getPlayer(uuid);
                        if (opponent != null) {
                            this.plugin.getMatchManager().removeFighter(opponent, this.plugin.getPlayerManager().getPlayerData(opponent.getUniqueId()), true);
                        }
                    }
                    CustomMovementHandler.parkourCheckpoints.remove(match);
                }
                else if (BlockUtil.isStandingOn(player, Material.WATER) || BlockUtil.isStandingOn(player, Material.STATIONARY_WATER)) {
                    this.teleportToSpawnOrCheckpoint(match, player);
                }
                else if (BlockUtil.isStandingOn(player, Material.STONE_PLATE) || BlockUtil.isStandingOn(player, Material.IRON_PLATE) || BlockUtil.isStandingOn(player, Material.WOOD_PLATE)) {
                    boolean checkpoint = false;
                    if (!CustomMovementHandler.parkourCheckpoints.containsKey(match)) {
                        checkpoint = true;
                        CustomMovementHandler.parkourCheckpoints.put(match, new HashMap<UUID, CustomLocation>());
                    }
                    if (!CustomMovementHandler.parkourCheckpoints.get(match).containsKey(player.getUniqueId())) {
                        checkpoint = true;
                        CustomMovementHandler.parkourCheckpoints.get(match).put(player.getUniqueId(), CustomLocation.fromBukkitLocation(player.getLocation()));
                    }
                    else if (CustomMovementHandler.parkourCheckpoints.get(match).containsKey(player.getUniqueId()) && !BlockUtil.isSameLocation(player.getLocation(), CustomMovementHandler.parkourCheckpoints.get(match).get(player.getUniqueId()).toBukkitLocation())) {
                        checkpoint = true;
                        CustomMovementHandler.parkourCheckpoints.get(match).put(player.getUniqueId(), CustomLocation.fromBukkitLocation(player.getLocation()));
                    }
                    if (checkpoint) {}
                }
                if ((to.getX() != from.getX() || to.getZ() != from.getZ()) && (match.getMatchState() == MatchState.STARTING || match.getMatchState() == MatchState.RESTARTING)) {
                    player.teleport(from);
                }
            }
        }
        final PracticeEvent event = this.plugin.getEventManager().getEventPlaying(player);
        if (event != null) {
            if (event instanceof SumoEvent) {
                final SumoEvent sumoEvent = (SumoEvent)event;
                if (sumoEvent.getPlayer(player).getFighting() != null && sumoEvent.getPlayer(player).getState() == SumoPlayer.SumoState.PREPARING) {
                    player.teleport(from);
                    return;
                }
                if (sumoEvent.getPlayers().size() <= 1) {
                    return;
                }
                if (sumoEvent.getPlayer(player) != null && sumoEvent.getPlayer(player).getState() != SumoPlayer.SumoState.FIGHTING) {
                    return;
                }
                final Block legs = player.getLocation().getBlock();
                final Block head = legs.getRelative(BlockFace.UP);
                if (legs.getType() == Material.WATER || legs.getType() == Material.STATIONARY_WATER || head.getType() == Material.WATER || head.getType() == Material.STATIONARY_WATER) {
                    sumoEvent.onDeath().accept(player);
                }
            }
            else if (event instanceof OITCEvent) {
                final OITCEvent oitcEvent = (OITCEvent)event;
                if (oitcEvent.getPlayer(player).getState() == OITCPlayer.OITCState.FIGHTING && player.getLocation().getBlockY() >= 90) {
                    oitcEvent.teleportNextLocation(player);
                    player.sendMessage(ChatColor.RED + "You have been teleported back to the arena.");
                }
            }
            else if (event instanceof RedroverEvent) {
                final RedroverEvent redroverEvent = (RedroverEvent)event;
                if (redroverEvent.getPlayer(player).getFightTask() != null && redroverEvent.getPlayer(player).getState() == RedroverPlayer.RedroverState.PREPARING) {
                    player.teleport(from);
                }
            }
            else if (event instanceof LightsEvent) {
                final LightsEvent lightsEvent = (LightsEvent)event;
                if (lightsEvent.getPlayer(player) != null && lightsEvent.getPlayer(player).getState() == LightsPlayer.LightsState.INGAME && lightsEvent.getCurrent() == LightsEvent.LightsGameState.RED) {
                    if (lightsEvent.getMovingPlayers().contains(player.getUniqueId()) && (to.getX() != from.getX() || to.getZ() != from.getZ())) {
                        player.teleport(from);
                    }
                    if (from.distance(to) >= 0.04 && !lightsEvent.getMovingPlayers().contains(player.getUniqueId())) {
                        lightsEvent.getMovingPlayers().add(player.getUniqueId());
                        lightsEvent.teleportToSpawn(player);
                    }
                }
                if (lightsEvent.getPlayer(player) != null && lightsEvent.getPlayer(player).getState() == LightsPlayer.LightsState.INGAME && BlockUtil.isStandingOn(player, Material.GOLD_PLATE)) {
                    final String announce = ChatColor.DARK_RED + player.getName() + ChatColor.WHITE + " has won our " + ChatColor.DARK_RED + "RedLightGreenLight" + ChatColor.WHITE + " event!";
                    Bukkit.broadcastMessage(announce);
                    lightsEvent.end();
                }
            }
            else if (event instanceof ParkourEvent) {
                final ParkourEvent parkourEvent = (ParkourEvent)event;
                if (parkourEvent.getPlayers().size() <= 1) {
                    return;
                }
                if (parkourEvent.getPlayer(player) != null && parkourEvent.getPlayer(player).getState() != ParkourPlayer.ParkourState.INGAME) {
                    return;
                }
                if (BlockUtil.isStandingOn(player, Material.WATER) || BlockUtil.isStandingOn(player, Material.STATIONARY_WATER)) {
                    parkourEvent.teleportToSpawnOrCheckpoint(player);
                }
                else if (BlockUtil.isStandingOn(player, Material.STONE_PLATE) || BlockUtil.isStandingOn(player, Material.IRON_PLATE) || BlockUtil.isStandingOn(player, Material.WOOD_PLATE)) {
                    final ParkourPlayer parkourPlayer = parkourEvent.getPlayer(player.getUniqueId());
                    if (parkourPlayer != null) {
                        boolean checkpoint2 = false;
                        if (parkourPlayer.getLastCheckpoint() == null) {
                            checkpoint2 = true;
                            parkourPlayer.setLastCheckpoint(CustomLocation.fromBukkitLocation(player.getLocation()));
                        }
                        else if (parkourPlayer.getLastCheckpoint() != null && !BlockUtil.isSameLocation(player.getLocation(), parkourPlayer.getLastCheckpoint().toBukkitLocation())) {
                            checkpoint2 = true;
                            parkourPlayer.setLastCheckpoint(CustomLocation.fromBukkitLocation(player.getLocation()));
                        }
                        if (checkpoint2) {
                            parkourPlayer.setCheckpointId(parkourPlayer.getCheckpointId() + 1);
                        }
                    }
                }
                else if (BlockUtil.isStandingOn(player, Material.GOLD_PLATE)) {
                    final String announce = ChatColor.DARK_RED + player.getName() + ChatColor.WHITE + " has won our " + ChatColor.DARK_RED + "Parkour" + ChatColor.WHITE + " event!";
                    Bukkit.broadcastMessage(announce);
                    parkourEvent.end();
                }
            }
        }
    }
    
    public void handleUpdateRotation(final Player player, final Location location, final Location location1, final PacketPlayInFlying packetPlayInFlying) {
    }
    
    private void teleportToSpawnOrCheckpoint(final Match match, final Player player) {
        if (!CustomMovementHandler.parkourCheckpoints.containsKey(match)) {
            player.sendMessage(ChatColor.GRAY + "Teleporting back to the beginning.");
            player.teleport(match.getArena().getA().toBukkitLocation());
            return;
        }
        if (!CustomMovementHandler.parkourCheckpoints.get(match).containsKey(player.getUniqueId())) {
            player.sendMessage(ChatColor.GRAY + "Teleporting back to the beginning.");
            player.teleport(match.getArena().getA().toBukkitLocation());
            return;
        }
        player.teleport(CustomMovementHandler.parkourCheckpoints.get(match).get(player.getUniqueId()).toBukkitLocation());
        player.sendMessage(ChatColor.GRAY + "Teleporting back to last checkpoint.");
    }
    
    public static HashMap<Match, HashMap<UUID, CustomLocation>> getParkourCheckpoints() {
        return CustomMovementHandler.parkourCheckpoints;
    }
    
    static {
        CustomMovementHandler.parkourCheckpoints = new HashMap<Match, HashMap<UUID, CustomLocation>>();
        CustomMovementHandler.bridgesScore = new HashMap<Match, HashMap<UUID, Integer>>();
    }
}
