package us.zonix.practice.util.timer.event;

import javax.annotation.Nullable;
import us.zonix.practice.util.timer.Timer;
import java.util.UUID;
import org.bukkit.entity.Player;
import java.util.Optional;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;

public class TimerExtendEvent extends Event implements Cancellable
{
    private static final HandlerList HANDLERS;
    private final Optional<Player> player;
    private final Optional<UUID> userUUID;
    private final Timer timer;
    private final long previousDuration;
    private boolean cancelled;
    private long newDuration;
    
    public TimerExtendEvent(final Timer timer, final long previousDuration, final long newDuration) {
        this.player = Optional.empty();
        this.userUUID = Optional.empty();
        this.timer = timer;
        this.previousDuration = previousDuration;
        this.newDuration = newDuration;
    }
    
    public TimerExtendEvent(@Nullable final Player player, final UUID uniqueId, final Timer timer, final long previousDuration, final long newDuration) {
        this.player = Optional.ofNullable(player);
        this.userUUID = Optional.ofNullable(uniqueId);
        this.timer = timer;
        this.previousDuration = previousDuration;
        this.newDuration = newDuration;
    }
    
    public static HandlerList getHandlerList() {
        return TimerExtendEvent.HANDLERS;
    }
    
    public Optional<Player> getPlayer() {
        return this.player;
    }
    
    public Optional<UUID> getUserUUID() {
        return this.userUUID;
    }
    
    public Timer getTimer() {
        return this.timer;
    }
    
    public long getPreviousDuration() {
        return this.previousDuration;
    }
    
    public long getNewDuration() {
        return this.newDuration;
    }
    
    public void setNewDuration(final long newDuration) {
        this.newDuration = newDuration;
    }
    
    public boolean isCancelled() {
        return this.cancelled;
    }
    
    public void setCancelled(final boolean cancelled) {
        this.cancelled = cancelled;
    }
    
    public HandlerList getHandlers() {
        return TimerExtendEvent.HANDLERS;
    }
    
    static {
        HANDLERS = new HandlerList();
    }
}
