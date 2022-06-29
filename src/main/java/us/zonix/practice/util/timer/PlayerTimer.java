package us.zonix.practice.util.timer;

import us.zonix.practice.util.timer.event.TimerExtendEvent;
import us.zonix.practice.util.timer.event.TimerStartEvent;
import java.util.function.Predicate;
import org.bukkit.Bukkit;
import us.zonix.practice.util.timer.event.TimerPauseEvent;
import org.bukkit.event.Event;
import us.zonix.practice.util.timer.event.TimerClearEvent;
import us.zonix.practice.Practice;
import java.util.Objects;
import org.bukkit.entity.Player;
import java.util.concurrent.ConcurrentHashMap;
import java.util.UUID;
import java.util.Map;

public abstract class PlayerTimer extends Timer
{
    protected final Map<UUID, TimerCooldown> cooldowns;
    
    public PlayerTimer(final String name, final long defaultCooldown) {
        super(name, defaultCooldown);
        this.cooldowns = new ConcurrentHashMap<UUID, TimerCooldown>();
    }
    
    protected void handleExpiry(final Player player, final UUID playerUUID) {
        this.cooldowns.remove(playerUUID);
    }
    
    public TimerCooldown clearCooldown(final UUID uuid) {
        return this.clearCooldown(null, uuid);
    }
    
    public TimerCooldown clearCooldown(final Player player) {
        Objects.requireNonNull(player);
        return this.clearCooldown(player, player.getUniqueId());
    }
    
    public TimerCooldown clearCooldown(final Player player, final UUID playerUUID) {
        final TimerCooldown runnable = this.cooldowns.remove(playerUUID);
        if (runnable != null) {
            runnable.cancel();
            if (player == null) {
                Practice.getInstance().getServer().getPluginManager().callEvent((Event)new TimerClearEvent(playerUUID, this));
            }
            else {
                Practice.getInstance().getServer().getPluginManager().callEvent((Event)new TimerClearEvent(player, this));
            }
        }
        return runnable;
    }
    
    public boolean isPaused(final Player player) {
        return this.isPaused(player.getUniqueId());
    }
    
    public boolean isPaused(final UUID playerUUID) {
        final TimerCooldown runnable = this.cooldowns.get(playerUUID);
        return runnable != null && runnable.isPaused();
    }
    
    public void setPaused(final UUID playerUUID, final boolean paused) {
        final TimerCooldown runnable = this.cooldowns.get(playerUUID);
        if (runnable != null && runnable.isPaused() != paused) {
            final TimerPauseEvent event = new TimerPauseEvent(playerUUID, this, paused);
            Bukkit.getPluginManager().callEvent((Event)event);
            if (!event.isCancelled()) {
                runnable.setPaused(paused);
            }
        }
    }
    
    public long getRemaining(final Player player) {
        return this.getRemaining(player.getUniqueId());
    }
    
    public long getRemaining(final UUID playerUUID) {
        final TimerCooldown runnable = this.cooldowns.get(playerUUID);
        return (runnable == null) ? 0L : runnable.getRemaining();
    }
    
    public boolean setCooldown(final Player player, final UUID playerUUID) {
        return this.setCooldown(player, playerUUID, this.defaultCooldown, false);
    }
    
    public boolean setCooldown(final Player player, final UUID playerUUID, final long duration, final boolean overwrite) {
        return this.setCooldown(player, playerUUID, duration, overwrite, null);
    }
    
    public boolean setCooldown(final Player player, final UUID playerUUID, final long duration, final boolean overwrite, final Predicate<Long> currentCooldownPredicate) {
        TimerCooldown runnable = (duration > 0L) ? this.cooldowns.get(playerUUID) : this.clearCooldown(player, playerUUID);
        if (runnable == null) {
            Practice.getInstance().getServer().getPluginManager().callEvent((Event)new TimerStartEvent(player, playerUUID, this, duration));
            runnable = new TimerCooldown(this, playerUUID, duration);
            this.cooldowns.put(playerUUID, runnable);
            return true;
        }
        final long remaining = runnable.getRemaining();
        if (!overwrite && remaining > 0L && duration <= remaining) {
            return false;
        }
        final TimerExtendEvent event = new TimerExtendEvent(player, playerUUID, this, remaining, duration);
        Practice.getInstance().getServer().getPluginManager().callEvent((Event)event);
        if (event.isCancelled()) {
            return false;
        }
        boolean flag = true;
        if (currentCooldownPredicate != null) {
            flag = currentCooldownPredicate.test(remaining);
        }
        if (flag) {
            runnable.setRemaining(duration);
        }
        return flag;
    }
    
    public Map<UUID, TimerCooldown> getCooldowns() {
        return this.cooldowns;
    }
}
