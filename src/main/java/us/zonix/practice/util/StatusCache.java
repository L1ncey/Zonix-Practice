package us.zonix.practice.util;

import java.util.Iterator;
import us.zonix.practice.player.PlayerState;
import us.zonix.practice.player.PlayerData;
import us.zonix.practice.Practice;
import org.bukkit.scheduler.BukkitRunnable;

public class StatusCache extends BukkitRunnable
{
    private static StatusCache instance;
    private int fighting;
    private int queueing;
    
    public StatusCache() {
        StatusCache.instance = this;
    }
    
    public void run() {
        int fighting = 0;
        int queueing = 0;
        for (final PlayerData playerData : Practice.getInstance().getPlayerManager().getAllData()) {
            if (playerData.getPlayerState() == PlayerState.FIGHTING) {
                ++fighting;
            }
            if (playerData.getPlayerState() == PlayerState.QUEUE) {
                ++queueing;
            }
        }
        this.fighting = fighting;
        this.queueing = queueing;
    }
    
    public int getFighting() {
        return this.fighting;
    }
    
    public int getQueueing() {
        return this.queueing;
    }
    
    public void setFighting(final int fighting) {
        this.fighting = fighting;
    }
    
    public void setQueueing(final int queueing) {
        this.queueing = queueing;
    }
    
    public boolean equals(final Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof StatusCache)) {
            return false;
        }
        final StatusCache other = (StatusCache)o;
        return other.canEqual(this) && this.getFighting() == other.getFighting() && this.getQueueing() == other.getQueueing();
    }
    
    protected boolean canEqual(final Object other) {
        return other instanceof StatusCache;
    }
    
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        result = result * 59 + this.getFighting();
        result = result * 59 + this.getQueueing();
        return result;
    }
    
    public String toString() {
        return "StatusCache(fighting=" + this.getFighting() + ", queueing=" + this.getQueueing() + ")";
    }
    
    public static StatusCache getInstance() {
        return StatusCache.instance;
    }
}
