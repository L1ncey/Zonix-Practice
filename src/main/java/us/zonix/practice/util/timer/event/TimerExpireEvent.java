package us.zonix.practice.util.timer.event;

import us.zonix.practice.Practice;
import java.util.Objects;
import org.bukkit.entity.Player;
import us.zonix.practice.util.timer.Timer;
import java.util.UUID;
import java.util.Optional;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Event;

public class TimerExpireEvent extends Event
{
    private static final HandlerList HANDLERS;
    private final Optional<UUID> userUUID;
    private final Timer timer;
    private Optional<Player> player;
    
    public TimerExpireEvent(final Timer timer) {
        this.userUUID = Optional.empty();
        this.timer = timer;
    }
    
    public TimerExpireEvent(final UUID userUUID, final Timer timer) {
        this.userUUID = Optional.ofNullable(userUUID);
        this.timer = timer;
    }
    
    public TimerExpireEvent(final Player player, final Timer timer) {
        Objects.requireNonNull(player);
        this.player = Optional.of(player);
        this.userUUID = Optional.of(player.getUniqueId());
        this.timer = timer;
    }
    
    public static HandlerList getHandlerList() {
        return TimerExpireEvent.HANDLERS;
    }
    
    public Optional<Player> getPlayer() {
        if (this.player == null) {
            this.player = (this.userUUID.isPresent() ? Optional.of(Practice.getInstance().getServer().getPlayer((UUID)this.userUUID.get())) : Optional.empty());
        }
        return this.player;
    }
    
    public Optional<UUID> getUserUUID() {
        return this.userUUID;
    }
    
    public Timer getTimer() {
        return this.timer;
    }
    
    public HandlerList getHandlers() {
        return TimerExpireEvent.HANDLERS;
    }
    
    static {
        HANDLERS = new HandlerList();
    }
}
