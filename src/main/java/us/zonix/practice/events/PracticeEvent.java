package us.zonix.practice.events;

import java.util.function.Consumer;
import java.util.Map;
import com.google.common.collect.Lists;
import com.sk89q.worldedit.EditSession;
import java.util.UUID;
import us.zonix.practice.events.tnttag.TNTTagEvent;
import us.zonix.practice.events.lights.LightsEvent;
import org.bukkit.Bukkit;
import us.zonix.practice.events.woolmixup.WoolMixUpEvent;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.Vector;
import org.bukkit.block.Block;
import com.boydti.fawe.util.EditSessionBuilder;
import com.boydti.fawe.util.TaskManager;
import us.zonix.practice.events.waterdrop.WaterDropEvent;
import us.zonix.practice.events.parkour.ParkourEvent;
import us.zonix.practice.events.redrover.RedroverPlayer;
import us.zonix.practice.events.redrover.RedroverEvent;
import us.zonix.practice.events.sumo.SumoPlayer;
import us.zonix.practice.events.sumo.SumoEvent;
import us.zonix.practice.event.EventStartEvent;
import us.zonix.practice.events.oitc.OITCPlayer;
import us.zonix.practice.events.oitc.OITCEvent;
import java.util.Iterator;
import java.util.List;
import us.zonix.practice.player.PlayerData;
import java.util.concurrent.ThreadLocalRandom;
import java.util.Collection;
import java.util.ArrayList;
import us.zonix.practice.CustomLocation;
import us.zonix.practice.util.PlayerUtil;
import us.zonix.practice.player.PlayerState;
import org.bukkit.Server;
import java.util.stream.Stream;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.function.Function;
import java.util.Objects;
import java.util.Set;
import org.bukkit.ChatColor;
import org.bukkit.plugin.Plugin;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import us.zonix.practice.Practice;

public abstract class PracticeEvent<K extends EventPlayer>
{
    private final Practice plugin;
    private final String name;
    private final ItemStack item;
    private final boolean enabled;
    private int limit;
    private Player host;
    private EventState state;
    
    public void startCountdown() {
        if (this.getCountdownTask().isEnded()) {
            this.getCountdownTask().setTimeUntilStart(this.getCountdownTask().getCountdownTime());
            this.getCountdownTask().setEnded(false);
        }
        else {
            this.getCountdownTask().runTaskTimerAsynchronously((Plugin)this.plugin, 20L, 20L);
        }
    }
    
    public void sendMessage(final String... messages) {
        for (final String message : messages) {
            this.getBukkitPlayers().forEach(player -> player.sendMessage(ChatColor.translateAlternateColorCodes('&', message)));
        }
    }
    
    public Set<Player> getBukkitPlayers() {
        final Stream<Object> filter = this.getPlayers().keySet().stream().filter(uuid -> this.plugin.getServer().getPlayer(uuid) != null);
        final Server server = this.plugin.getServer();
        Objects.requireNonNull(server);
        return filter.map((Function<? super Object, ?>)server::getPlayer).collect((Collector<? super Object, ?, Set<Player>>)Collectors.toSet());
    }
    
    public void join(final Player player) {
        if (this.getPlayers().size() >= this.limit && !player.hasPermission("practice.bypass.join")) {
            return;
        }
        final PlayerData playerData = this.plugin.getPlayerManager().getPlayerData(player.getUniqueId());
        playerData.setPlayerState(PlayerState.EVENT);
        PlayerUtil.clearPlayer(player);
        if (this.onJoin() != null) {
            this.onJoin().accept(player);
        }
        if (this.getSpawnLocations().size() == 1) {
            player.teleport(this.getSpawnLocations().get(0).toBukkitLocation());
        }
        else {
            final List<CustomLocation> spawnLocations = new ArrayList<CustomLocation>(this.getSpawnLocations());
            player.teleport(spawnLocations.remove(ThreadLocalRandom.current().nextInt(spawnLocations.size())).toBukkitLocation());
        }
        this.plugin.getPlayerManager().giveLobbyItems(player);
        for (final Player other : this.getBukkitPlayers()) {
            other.showPlayer(player);
            player.showPlayer(other);
        }
        this.sendMessage("&c" + player.getName() + " &ehas joined the event. &7(" + this.getPlayers().size() + "/" + this.getLimit() + ")");
    }
    
    public void leave(final Player player) {
        if (this instanceof OITCEvent) {
            final OITCEvent oitcEvent = (OITCEvent)this;
            final OITCPlayer oitcPlayer = oitcEvent.getPlayer(player);
            oitcPlayer.setState(OITCPlayer.OITCState.ELIMINATED);
        }
        if (this.onDeath() != null) {
            this.onDeath().accept(player);
        }
        this.getPlayers().remove(player.getUniqueId());
        this.plugin.getPlayerManager().sendToSpawnAndReset(player);
    }
    
    public void start() {
        new EventStartEvent(this).call();
        this.setState(EventState.STARTED);
        this.onStart();
        this.plugin.getEventManager().setCooldown(0L);
    }
    
    public void end() {
        this.plugin.getServer().getScheduler().runTaskLater((Plugin)this.plugin, () -> this.plugin.getPlayerManager().getPlayersByState(PlayerState.EVENT).forEach(player -> this.plugin.getPlayerManager().sendToSpawnAndReset(player)), 2L);
        this.plugin.getEventManager().setCooldown(System.currentTimeMillis() + 300000L);
        if (this instanceof SumoEvent) {
            final SumoEvent sumoEvent = (SumoEvent)this;
            for (final SumoPlayer sumoPlayer : sumoEvent.getPlayers().values()) {
                if (sumoPlayer.getFightTask() != null) {
                    sumoPlayer.getFightTask().cancel();
                }
            }
        }
        else if (this instanceof OITCEvent) {
            final OITCEvent oitcEvent = (OITCEvent)this;
            if (oitcEvent.getGameTask() != null) {
                oitcEvent.getGameTask().cancel();
            }
        }
        else if (this instanceof RedroverEvent) {
            final RedroverEvent redroverEvent = (RedroverEvent)this;
            for (final RedroverPlayer redroverPlayer : redroverEvent.getPlayers().values()) {
                if (redroverPlayer.getFightTask() != null) {
                    redroverPlayer.getFightTask().cancel();
                }
            }
            if (redroverEvent.getGameTask() != null) {
                redroverEvent.getGameTask().cancel();
            }
        }
        else if (this instanceof ParkourEvent) {
            final ParkourEvent parkourEvent = (ParkourEvent)this;
            if (parkourEvent.getGameTask() != null) {
                parkourEvent.getGameTask().cancel();
            }
        }
        else if (this instanceof WaterDropEvent) {
            final WaterDropEvent waterDropEvent = (WaterDropEvent)this;
            if (waterDropEvent.getGameTask() != null) {
                waterDropEvent.getGameTask().cancel();
            }
            if (waterDropEvent.getWaterCheckTask() != null) {
                waterDropEvent.getWaterCheckTask().cancel();
            }
            final WaterDropEvent waterDropEvent2;
            final EditSession editSession;
            final Iterator<Block> iterator4;
            Block entry;
            TaskManager.IMP.async(() -> {
                if (waterDropEvent2.getCuboid() != null) {
                    editSession = new EditSessionBuilder(waterDropEvent2.getCuboid().getWorld().getName()).fastmode(true).allowedRegionsEverywhere().autoQueue(false).limitUnlimited().build();
                    waterDropEvent2.getCuboid().iterator();
                    while (iterator4.hasNext()) {
                        entry = iterator4.next();
                        try {
                            editSession.setBlock(new Vector((double)entry.getLocation().getBlockX(), (double)entry.getLocation().getBlockY(), entry.getLocation().getZ()), new BaseBlock(35, 0));
                        }
                        catch (Exception ex) {}
                    }
                }
                return;
            });
        }
        else if (this instanceof WoolMixUpEvent) {
            final WoolMixUpEvent woolMixUpEvent = (WoolMixUpEvent)this;
            Bukkit.getScheduler().cancelTask(woolMixUpEvent.getTaskId());
            woolMixUpEvent.setTaskId(-1);
            woolMixUpEvent.setCurrentColor(-1);
            final WoolMixUpEvent woolMixUpEvent2;
            Bukkit.getScheduler().runTaskLaterAsynchronously((Plugin)this.plugin, () -> woolMixUpEvent2.regenerateArena(woolMixUpEvent2.getBlocksRegen()), 20L);
        }
        else if (this instanceof LightsEvent) {
            final LightsEvent lightsEvent = (LightsEvent)this;
            Bukkit.getScheduler().cancelTask(lightsEvent.getTaskId());
            lightsEvent.setTaskId(-1);
        }
        else if (this instanceof TNTTagEvent) {
            final TNTTagEvent tagEvent = (TNTTagEvent)this;
            if (tagEvent.getTask() != null) {
                tagEvent.getTask().cancel();
            }
            tagEvent.setRound(0);
            tagEvent.setTntTagState(TNTTagEvent.TNTTagState.NOT_STARTED);
        }
        this.getPlayers().clear();
        this.setState(EventState.UNANNOUNCED);
        final Iterator<UUID> iterator = this.plugin.getEventManager().getSpectators().keySet().iterator();
        while (iterator.hasNext()) {
            final UUID spectatorUUID = iterator.next();
            final Player spectator = Bukkit.getPlayer(spectatorUUID);
            if (spectator != null) {
                this.plugin.getServer().getScheduler().runTask((Plugin)this.plugin, () -> this.plugin.getPlayerManager().sendToSpawnAndReset(spectator));
                iterator.remove();
            }
        }
        this.plugin.getEventManager().getSpectators().clear();
        this.getCountdownTask().setEnded(true);
    }
    
    public List<String> getScoreboardLines(final Player player) {
        return (List<String>)Lists.newArrayList();
    }
    
    public List<String> getScoreboardSpectator(final Player player) {
        return this.getScoreboardLines(player);
    }
    
    public K getPlayer(final Player player) {
        return this.getPlayer(player.getUniqueId());
    }
    
    public K getPlayer(final UUID uuid) {
        return this.getPlayers().get(uuid);
    }
    
    public abstract Map<UUID, K> getPlayers();
    
    public abstract EventCountdownTask getCountdownTask();
    
    public abstract List<CustomLocation> getSpawnLocations();
    
    public abstract void onStart();
    
    public abstract Consumer<Player> onJoin();
    
    public abstract Consumer<Player> onDeath();
    
    public PracticeEvent(final String name, final ItemStack item, final boolean enabled) {
        this.plugin = Practice.getInstance();
        this.limit = 30;
        this.state = EventState.UNANNOUNCED;
        this.name = name;
        this.item = item;
        this.enabled = enabled;
    }
    
    public Practice getPlugin() {
        return this.plugin;
    }
    
    public String getName() {
        return this.name;
    }
    
    public ItemStack getItem() {
        return this.item;
    }
    
    public boolean isEnabled() {
        return this.enabled;
    }
    
    public int getLimit() {
        return this.limit;
    }
    
    public Player getHost() {
        return this.host;
    }
    
    public EventState getState() {
        return this.state;
    }
    
    public void setLimit(final int limit) {
        this.limit = limit;
    }
    
    public void setHost(final Player host) {
        this.host = host;
    }
    
    public void setState(final EventState state) {
        this.state = state;
    }
    
    @Override
    public boolean equals(final Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof PracticeEvent)) {
            return false;
        }
        final PracticeEvent<?> other = (PracticeEvent<?>)o;
        if (!other.canEqual(this)) {
            return false;
        }
        final Object this$plugin = this.getPlugin();
        final Object other$plugin = other.getPlugin();
        Label_0065: {
            if (this$plugin == null) {
                if (other$plugin == null) {
                    break Label_0065;
                }
            }
            else if (this$plugin.equals(other$plugin)) {
                break Label_0065;
            }
            return false;
        }
        final Object this$name = this.getName();
        final Object other$name = other.getName();
        Label_0102: {
            if (this$name == null) {
                if (other$name == null) {
                    break Label_0102;
                }
            }
            else if (this$name.equals(other$name)) {
                break Label_0102;
            }
            return false;
        }
        final Object this$item = this.getItem();
        final Object other$item = other.getItem();
        Label_0139: {
            if (this$item == null) {
                if (other$item == null) {
                    break Label_0139;
                }
            }
            else if (this$item.equals(other$item)) {
                break Label_0139;
            }
            return false;
        }
        if (this.isEnabled() != other.isEnabled()) {
            return false;
        }
        if (this.getLimit() != other.getLimit()) {
            return false;
        }
        final Object this$host = this.getHost();
        final Object other$host = other.getHost();
        Label_0202: {
            if (this$host == null) {
                if (other$host == null) {
                    break Label_0202;
                }
            }
            else if (this$host.equals(other$host)) {
                break Label_0202;
            }
            return false;
        }
        final Object this$state = this.getState();
        final Object other$state = other.getState();
        if (this$state == null) {
            if (other$state == null) {
                return true;
            }
        }
        else if (this$state.equals(other$state)) {
            return true;
        }
        return false;
    }
    
    protected boolean canEqual(final Object other) {
        return other instanceof PracticeEvent;
    }
    
    @Override
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $plugin = this.getPlugin();
        result = result * 59 + (($plugin == null) ? 43 : $plugin.hashCode());
        final Object $name = this.getName();
        result = result * 59 + (($name == null) ? 43 : $name.hashCode());
        final Object $item = this.getItem();
        result = result * 59 + (($item == null) ? 43 : $item.hashCode());
        result = result * 59 + (this.isEnabled() ? 79 : 97);
        result = result * 59 + this.getLimit();
        final Object $host = this.getHost();
        result = result * 59 + (($host == null) ? 43 : $host.hashCode());
        final Object $state = this.getState();
        result = result * 59 + (($state == null) ? 43 : $state.hashCode());
        return result;
    }
    
    @Override
    public String toString() {
        return "PracticeEvent(plugin=" + this.getPlugin() + ", name=" + this.getName() + ", item=" + this.getItem() + ", enabled=" + this.isEnabled() + ", limit=" + this.getLimit() + ", host=" + this.getHost() + ", state=" + this.getState() + ")";
    }
}
