package us.zonix.practice.util.timer;

import java.util.Iterator;
import org.bukkit.plugin.Plugin;
import java.util.LinkedHashSet;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.Set;
import org.bukkit.event.Listener;

public class TimerManager implements Listener
{
    private final Set<Timer> timers;
    private final JavaPlugin plugin;
    
    public TimerManager(final JavaPlugin plugin) {
        this.timers = new LinkedHashSet<Timer>();
        this.plugin = plugin;
    }
    
    public void registerTimer(final Timer timer) {
        this.timers.add(timer);
        if (timer instanceof Listener) {
            this.plugin.getServer().getPluginManager().registerEvents((Listener)timer, (Plugin)this.plugin);
        }
    }
    
    public void unregisterTimer(final Timer timer) {
        this.timers.remove(timer);
    }
    
    public <T extends Timer> T getTimer(final Class<T> timerClass) {
        for (final Timer timer : this.timers) {
            if (timer.getClass().equals(timerClass)) {
                return (T)timer;
            }
        }
        return null;
    }
    
    public JavaPlugin getPlugin() {
        return this.plugin;
    }
    
    @Override
    public boolean equals(final Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof TimerManager)) {
            return false;
        }
        final TimerManager other = (TimerManager)o;
        if (!other.canEqual(this)) {
            return false;
        }
        final Object this$timers = this.getTimers();
        final Object other$timers = other.getTimers();
        Label_0065: {
            if (this$timers == null) {
                if (other$timers == null) {
                    break Label_0065;
                }
            }
            else if (this$timers.equals(other$timers)) {
                break Label_0065;
            }
            return false;
        }
        final Object this$plugin = this.getPlugin();
        final Object other$plugin = other.getPlugin();
        if (this$plugin == null) {
            if (other$plugin == null) {
                return true;
            }
        }
        else if (this$plugin.equals(other$plugin)) {
            return true;
        }
        return false;
    }
    
    protected boolean canEqual(final Object other) {
        return other instanceof TimerManager;
    }
    
    @Override
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $timers = this.getTimers();
        result = result * 59 + (($timers == null) ? 43 : $timers.hashCode());
        final Object $plugin = this.getPlugin();
        result = result * 59 + (($plugin == null) ? 43 : $plugin.hashCode());
        return result;
    }
    
    @Override
    public String toString() {
        return "TimerManager(timers=" + this.getTimers() + ", plugin=" + this.getPlugin() + ")";
    }
    
    public Set<Timer> getTimers() {
        return this.timers;
    }
}
