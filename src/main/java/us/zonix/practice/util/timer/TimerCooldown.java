package us.zonix.practice.util.timer;

import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.event.Event;
import us.zonix.practice.util.timer.event.TimerExpireEvent;
import us.zonix.practice.Practice;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import java.util.UUID;

public class TimerCooldown
{
    private final Timer timer;
    private final UUID owner;
    private BukkitTask eventNotificationTask;
    private long expiryMillis;
    private long pauseMillis;
    
    protected TimerCooldown(final Timer timer, final long duration) {
        this.owner = null;
        this.timer = timer;
        this.setRemaining(duration);
    }
    
    protected TimerCooldown(final Timer timer, final UUID playerUUID, final long duration) {
        this.timer = timer;
        this.owner = playerUUID;
        this.setRemaining(duration);
    }
    
    public long getRemaining() {
        return this.getRemaining(false);
    }
    
    protected void setRemaining(final long milliseconds) throws IllegalStateException {
        if (milliseconds <= 0L) {
            this.cancel();
            return;
        }
        final long expiryMillis = System.currentTimeMillis() + milliseconds;
        if (expiryMillis != this.expiryMillis) {
            this.expiryMillis = expiryMillis;
            if (this.eventNotificationTask != null) {
                this.eventNotificationTask.cancel();
            }
            final long ticks = milliseconds / 50L;
            this.eventNotificationTask = new BukkitRunnable() {
                public void run() {
                    if (TimerCooldown.this.timer instanceof PlayerTimer && TimerCooldown.this.owner != null) {
                        ((PlayerTimer)TimerCooldown.this.timer).handleExpiry(Practice.getInstance().getServer().getPlayer(TimerCooldown.this.owner), TimerCooldown.this.owner);
                    }
                    Practice.getInstance().getServer().getPluginManager().callEvent((Event)new TimerExpireEvent(TimerCooldown.this.owner, TimerCooldown.this.timer));
                }
            }.runTaskLaterAsynchronously((Plugin)JavaPlugin.getProvidingPlugin((Class)this.getClass()), ticks);
        }
    }
    
    protected long getRemaining(final boolean ignorePaused) {
        if (!ignorePaused && this.pauseMillis != 0L) {
            return this.pauseMillis;
        }
        return this.expiryMillis - System.currentTimeMillis();
    }
    
    protected boolean isPaused() {
        return this.pauseMillis != 0L;
    }
    
    public void setPaused(final boolean paused) {
        if (paused != this.isPaused()) {
            if (paused) {
                this.pauseMillis = this.getRemaining(true);
                this.cancel();
            }
            else {
                this.setRemaining(this.pauseMillis);
                this.pauseMillis = 0L;
            }
        }
    }
    
    protected void cancel() throws IllegalStateException {
        if (this.eventNotificationTask != null) {
            this.eventNotificationTask.cancel();
            this.eventNotificationTask = null;
        }
    }
    
    public Timer getTimer() {
        return this.timer;
    }
    
    public long getExpiryMillis() {
        return this.expiryMillis;
    }
    
    public long getPauseMillis() {
        return this.pauseMillis;
    }
    
    protected void setPauseMillis(final long pauseMillis) {
        this.pauseMillis = pauseMillis;
    }
}
