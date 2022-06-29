package us.zonix.practice.events;

import java.util.Arrays;
import java.util.Collection;
import org.bukkit.scheduler.BukkitScheduler;
import java.util.function.Consumer;
import org.bukkit.Bukkit;
import us.zonix.practice.util.Clickable;
import org.bukkit.ChatColor;
import org.bukkit.plugin.Plugin;
import java.util.Objects;
import us.zonix.practice.Practice;
import org.bukkit.scheduler.BukkitRunnable;

public class EventCountdownTask extends BukkitRunnable
{
    private static final int DEFAULT_COUNTDOWN_TIME = 60;
    private final PracticeEvent event;
    private final int countdownTime;
    private int timeUntilStart;
    private boolean ended;
    
    public EventCountdownTask(final PracticeEvent event, final int countdownTime) {
        this.event = event;
        this.countdownTime = countdownTime;
        this.timeUntilStart = countdownTime;
    }
    
    public EventCountdownTask(final PracticeEvent event) {
        this(event, 60);
    }
    
    public void run() {
        if (this.isEnded()) {
            return;
        }
        if (this.timeUntilStart <= 0) {
            if (this.canStart()) {
                final BukkitScheduler scheduler = Practice.getInstance().getServer().getScheduler();
                final Practice instance = Practice.getInstance();
                final PracticeEvent event = this.event;
                Objects.requireNonNull(event);
                scheduler.runTask((Plugin)instance, event::start);
            }
            else {
                Practice.getInstance().getServer().getScheduler().runTask((Plugin)Practice.getInstance(), this::onCancel);
            }
            this.ended = true;
            return;
        }
        if (this.shouldAnnounce(this.timeUntilStart)) {
            String toSend = "";
            String toSendDonor = "";
            toSend = ChatColor.RED.toString() + ChatColor.BOLD + "[Event] " + ChatColor.WHITE + "" + this.event.getName() + " is starting soon. " + ChatColor.GRAY + "[Join]";
            toSendDonor = ChatColor.GRAY + "[" + ChatColor.BOLD + "*" + ChatColor.GRAY + "] " + ChatColor.RED.toString() + ChatColor.BOLD + ((this.event.getHost() == null) ? "Someone" : this.event.getHost().getName()) + ChatColor.WHITE + " is hosting a " + ChatColor.WHITE.toString() + ChatColor.BOLD + this.event.getName() + " Event. " + ChatColor.GRAY + "[Join]";
            if (this.event.getHost() != null) {
                final Clickable message = new Clickable(this.event.getHost().hasPermission("practice.donator") ? toSendDonor : toSend, ChatColor.GRAY + "Click to join this event.", "/join " + this.event.getName());
                final Collection onlinePlayers = Bukkit.getServer().getOnlinePlayers();
                final Clickable clickable = message;
                Objects.requireNonNull(clickable);
                onlinePlayers.forEach(clickable::sendToPlayer);
            }
        }
        --this.timeUntilStart;
    }
    
    public boolean shouldAnnounce(final int timeUntilStart) {
        return Arrays.asList(45, 30, 15, 10, 5).contains(timeUntilStart);
    }
    
    public boolean canStart() {
        return this.event.getBukkitPlayers().size() >= 2;
    }
    
    public void onCancel() {
        this.getEvent().sendMessage("&cNot enough players joined the event. The event has been cancelled.");
        this.getEvent().end();
        this.getEvent().getPlugin().getEventManager().setCooldown(0L);
    }
    
    private String getTime(int time) {
        final StringBuilder timeStr = new StringBuilder();
        int minutes = 0;
        if (time % 60 == 0) {
            minutes = time / 60;
            time = 0;
        }
        else {
            while (time - 60 > 0) {
                ++minutes;
                time -= 60;
            }
        }
        if (minutes > 0) {
            timeStr.append(minutes).append("m");
        }
        if (time > 0) {
            timeStr.append((minutes > 0) ? " " : "").append(time).append("s");
        }
        return timeStr.toString();
    }
    
    public boolean equals(final Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof EventCountdownTask)) {
            return false;
        }
        final EventCountdownTask other = (EventCountdownTask)o;
        if (!other.canEqual(this)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        final Object this$event = this.getEvent();
        final Object other$event = other.getEvent();
        if (this$event == null) {
            if (other$event == null) {
                return this.getCountdownTime() == other.getCountdownTime() && this.getTimeUntilStart() == other.getTimeUntilStart() && this.isEnded() == other.isEnded();
            }
        }
        else if (this$event.equals(other$event)) {
            return this.getCountdownTime() == other.getCountdownTime() && this.getTimeUntilStart() == other.getTimeUntilStart() && this.isEnded() == other.isEnded();
        }
        return false;
    }
    
    protected boolean canEqual(final Object other) {
        return other instanceof EventCountdownTask;
    }
    
    public int hashCode() {
        final int PRIME = 59;
        int result = super.hashCode();
        final Object $event = this.getEvent();
        result = result * 59 + (($event == null) ? 43 : $event.hashCode());
        result = result * 59 + this.getCountdownTime();
        result = result * 59 + this.getTimeUntilStart();
        result = result * 59 + (this.isEnded() ? 79 : 97);
        return result;
    }
    
    public PracticeEvent getEvent() {
        return this.event;
    }
    
    public int getCountdownTime() {
        return this.countdownTime;
    }
    
    public int getTimeUntilStart() {
        return this.timeUntilStart;
    }
    
    public boolean isEnded() {
        return this.ended;
    }
    
    public void setTimeUntilStart(final int timeUntilStart) {
        this.timeUntilStart = timeUntilStart;
    }
    
    public void setEnded(final boolean ended) {
        this.ended = ended;
    }
    
    public String toString() {
        return "EventCountdownTask(event=" + this.getEvent() + ", countdownTime=" + this.getCountdownTime() + ", timeUntilStart=" + this.getTimeUntilStart() + ", ended=" + this.isEnded() + ")";
    }
}
