package us.zonix.practice.managers;

import us.zonix.practice.player.PlayerState;
import java.util.Iterator;
import java.util.List;
import org.bukkit.GameMode;
import java.util.concurrent.ThreadLocalRandom;
import java.util.Collection;
import java.util.ArrayList;
import us.zonix.practice.CustomLocation;
import us.zonix.practice.player.PlayerData;
import us.zonix.practice.events.EventState;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.WorldCreator;
import java.util.function.Consumer;
import java.util.Arrays;
import us.zonix.practice.events.tnttag.TNTTagEvent;
import us.zonix.practice.events.lights.LightsEvent;
import us.zonix.practice.events.woolmixup.WoolMixUpEvent;
import us.zonix.practice.events.waterdrop.WaterDropEvent;
import us.zonix.practice.events.redrover.RedroverEvent;
import us.zonix.practice.events.parkour.ParkourEvent;
import us.zonix.practice.events.oitc.OITCEvent;
import us.zonix.practice.events.sumo.SumoEvent;
import org.bukkit.World;
import java.util.UUID;
import java.util.HashMap;
import us.zonix.practice.Practice;
import us.zonix.practice.events.PracticeEvent;
import java.util.Map;

public class EventManager
{
    private final Map<Class<? extends PracticeEvent>, PracticeEvent> events;
    private final Practice plugin;
    private HashMap<UUID, PracticeEvent> spectators;
    private long cooldown;
    private final World eventWorld;
    
    public EventManager() {
        this.events = new HashMap<Class<? extends PracticeEvent>, PracticeEvent>();
        this.plugin = Practice.getInstance();
        Arrays.asList(SumoEvent.class, OITCEvent.class, ParkourEvent.class, RedroverEvent.class, WaterDropEvent.class, WoolMixUpEvent.class, LightsEvent.class, TNTTagEvent.class).forEach(this::addEvent);
        boolean newWorld;
        if (this.plugin.getServer().getWorld("event") == null) {
            this.eventWorld = this.plugin.getServer().createWorld(new WorldCreator("event"));
            newWorld = true;
        }
        else {
            this.eventWorld = this.plugin.getServer().getWorld("event");
            newWorld = false;
        }
        this.spectators = new HashMap<UUID, PracticeEvent>();
        this.cooldown = 0L;
        if (this.eventWorld != null) {
            if (newWorld) {
                this.plugin.getServer().getWorlds().add(this.eventWorld);
            }
            this.eventWorld.setTime(2000L);
            this.eventWorld.setGameRuleValue("doDaylightCycle", "false");
            this.eventWorld.setGameRuleValue("doMobSpawning", "false");
            this.eventWorld.setStorm(false);
            this.eventWorld.getEntities().stream().filter(entity -> !(entity instanceof Player)).forEach(Entity::remove);
        }
    }
    
    public PracticeEvent getByName(final String name) {
        return this.events.values().stream().filter(event -> event.getName().toLowerCase().equalsIgnoreCase(name.toLowerCase())).findFirst().orElse(null);
    }
    
    public void hostEvent(final PracticeEvent event, final Player host) {
        event.setState(EventState.WAITING);
        event.setHost(host);
        event.startCountdown();
    }
    
    private void addEvent(final Class<? extends PracticeEvent> clazz) {
        PracticeEvent event = null;
        try {
            event = (PracticeEvent)clazz.newInstance();
        }
        catch (InstantiationException | IllegalAccessException ex2) {
            final ReflectiveOperationException ex;
            final ReflectiveOperationException e = ex;
            e.printStackTrace();
        }
        this.events.put(clazz, event);
    }
    
    public void addSpectatorRedrover(final Player player, final PlayerData playerData, final RedroverEvent event) {
        this.addSpectator(player, playerData, event);
        if (event.getSpawnLocations().size() == 1) {
            player.teleport(event.getSpawnLocations().get(0).toBukkitLocation());
        }
        else {
            final List<CustomLocation> spawnLocations = new ArrayList<CustomLocation>(event.getSpawnLocations());
            player.teleport(spawnLocations.remove(ThreadLocalRandom.current().nextInt(spawnLocations.size())).toBukkitLocation());
        }
        for (final Player eventPlayer : event.getBukkitPlayers()) {
            player.showPlayer(eventPlayer);
        }
        player.setGameMode(GameMode.SURVIVAL);
        player.setAllowFlight(true);
        player.setFlying(true);
    }
    
    public void addSpectatorSumo(final Player player, final PlayerData playerData, final SumoEvent event) {
        this.addSpectator(player, playerData, event);
        if (event.getSpawnLocations().size() == 1) {
            player.teleport(event.getSpawnLocations().get(0).toBukkitLocation());
        }
        else {
            final List<CustomLocation> spawnLocations = new ArrayList<CustomLocation>(event.getSpawnLocations());
            player.teleport(spawnLocations.remove(ThreadLocalRandom.current().nextInt(spawnLocations.size())).toBukkitLocation());
        }
        for (final Player eventPlayer : event.getBukkitPlayers()) {
            player.showPlayer(eventPlayer);
        }
        player.setGameMode(GameMode.SURVIVAL);
        player.setAllowFlight(true);
        player.setFlying(true);
    }
    
    public void addSpectatorOITC(final Player player, final PlayerData playerData, final OITCEvent event) {
        this.addSpectator(player, playerData, event);
        if (event.getSpawnLocations().size() == 1) {
            player.teleport(event.getSpawnLocations().get(0).toBukkitLocation());
        }
        else {
            final List<CustomLocation> spawnLocations = new ArrayList<CustomLocation>(event.getSpawnLocations());
            player.teleport(spawnLocations.remove(ThreadLocalRandom.current().nextInt(spawnLocations.size())).toBukkitLocation());
        }
        for (final Player eventPlayer : event.getBukkitPlayers()) {
            player.showPlayer(eventPlayer);
        }
        player.setGameMode(GameMode.SURVIVAL);
        player.setAllowFlight(true);
        player.setFlying(true);
    }
    
    public void addSpectatorParkour(final Player player, final PlayerData playerData, final ParkourEvent event) {
        this.addSpectator(player, playerData, event);
        player.teleport(this.plugin.getSpawnManager().getParkourGameLocation().toBukkitLocation());
        for (final Player eventPlayer : event.getBukkitPlayers()) {
            player.showPlayer(eventPlayer);
        }
        player.setGameMode(GameMode.SURVIVAL);
        player.setAllowFlight(true);
        player.setFlying(true);
    }
    
    public void addSpectatorWoolMixUp(final Player player, final PlayerData playerData, final WoolMixUpEvent event) {
        this.addSpectator(player, playerData, event);
        player.teleport(this.plugin.getSpawnManager().getWoolCenter().toBukkitLocation());
        for (final Player eventPlayer : event.getBukkitPlayers()) {
            player.showPlayer(eventPlayer);
        }
        player.setGameMode(GameMode.SURVIVAL);
        player.setAllowFlight(true);
        player.setFlying(true);
    }
    
    public void addSpectatorLights(final Player player, final PlayerData playerData, final LightsEvent event) {
        this.addSpectator(player, playerData, event);
        player.teleport(this.plugin.getSpawnManager().getLightsStart().toBukkitLocation());
        for (final Player eventPlayer : event.getBukkitPlayers()) {
            player.showPlayer(eventPlayer);
        }
        player.setGameMode(GameMode.SURVIVAL);
        player.setAllowFlight(true);
        player.setFlying(true);
    }
    
    public void addSpectatorTntTag(final Player player, final PlayerData playerData, final TNTTagEvent event) {
        this.addSpectator(player, playerData, event);
        player.teleport(this.plugin.getSpawnManager().getTntTagSpawn().toBukkitLocation());
        for (final Player eventPlayer : event.getBukkitPlayers()) {
            player.showPlayer(eventPlayer);
        }
        player.setGameMode(GameMode.SURVIVAL);
        player.setAllowFlight(true);
        player.setFlying(true);
    }
    
    private void addSpectator(final Player player, final PlayerData playerData, final PracticeEvent event) {
        playerData.setPlayerState(PlayerState.SPECTATING);
        this.spectators.put(player.getUniqueId(), event);
        player.getInventory().setContents(this.plugin.getItemManager().getSpecItems());
        player.updateInventory();
        this.plugin.getServer().getOnlinePlayers().forEach(online -> {
            online.hidePlayer(player);
            player.hidePlayer(online);
        });
    }
    
    public void removeSpectator(final Player player) {
        this.getSpectators().remove(player.getUniqueId());
        this.plugin.getPlayerManager().sendToSpawnAndReset(player);
    }
    
    public boolean isPlaying(final Player player, final PracticeEvent event) {
        return event.getPlayers().containsKey(player.getUniqueId());
    }
    
    public PracticeEvent getSpectatingEvent(final UUID uuid) {
        return this.spectators.get(uuid);
    }
    
    public PracticeEvent getEventPlaying(final Player player) {
        return this.events.values().stream().filter(event -> this.isPlaying(player, event)).findFirst().orElse(null);
    }
    
    public Map<Class<? extends PracticeEvent>, PracticeEvent> getEvents() {
        return this.events;
    }
    
    public Practice getPlugin() {
        return this.plugin;
    }
    
    public HashMap<UUID, PracticeEvent> getSpectators() {
        return this.spectators;
    }
    
    public long getCooldown() {
        return this.cooldown;
    }
    
    public World getEventWorld() {
        return this.eventWorld;
    }
    
    public void setCooldown(final long cooldown) {
        this.cooldown = cooldown;
    }
}
