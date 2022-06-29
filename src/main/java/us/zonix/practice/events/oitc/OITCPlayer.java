package us.zonix.practice.events.oitc;

import us.zonix.practice.events.PracticeEvent;
import java.util.UUID;
import org.bukkit.scheduler.BukkitTask;
import us.zonix.practice.events.EventPlayer;

public class OITCPlayer extends EventPlayer
{
    private OITCState state;
    private int score;
    private int lives;
    private BukkitTask respawnTask;
    private OITCPlayer lastKiller;
    
    public OITCPlayer(final UUID uuid, final PracticeEvent event) {
        super(uuid, event);
        this.state = OITCState.WAITING;
        this.score = 0;
        this.lives = 5;
    }
    
    public void setState(final OITCState state) {
        this.state = state;
    }
    
    public void setScore(final int score) {
        this.score = score;
    }
    
    public void setLives(final int lives) {
        this.lives = lives;
    }
    
    public void setRespawnTask(final BukkitTask respawnTask) {
        this.respawnTask = respawnTask;
    }
    
    public void setLastKiller(final OITCPlayer lastKiller) {
        this.lastKiller = lastKiller;
    }
    
    public OITCState getState() {
        return this.state;
    }
    
    public int getScore() {
        return this.score;
    }
    
    public int getLives() {
        return this.lives;
    }
    
    public BukkitTask getRespawnTask() {
        return this.respawnTask;
    }
    
    public OITCPlayer getLastKiller() {
        return this.lastKiller;
    }
    
    public enum OITCState
    {
        WAITING, 
        PREPARING, 
        FIGHTING, 
        RESPAWNING, 
        ELIMINATED;
    }
}
