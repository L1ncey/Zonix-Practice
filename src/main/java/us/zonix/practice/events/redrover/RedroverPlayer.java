package us.zonix.practice.events.redrover;

import us.zonix.practice.events.PracticeEvent;
import java.util.UUID;
import org.bukkit.scheduler.BukkitTask;
import us.zonix.practice.events.EventPlayer;

public class RedroverPlayer extends EventPlayer
{
    private RedroverState state;
    private RedroverPlayer fightPlayer;
    private BukkitTask fightTask;
    
    public RedroverPlayer(final UUID uuid, final PracticeEvent event) {
        super(uuid, event);
        this.state = RedroverState.WAITING;
    }
    
    public void setState(final RedroverState state) {
        this.state = state;
    }
    
    public void setFightPlayer(final RedroverPlayer fightPlayer) {
        this.fightPlayer = fightPlayer;
    }
    
    public void setFightTask(final BukkitTask fightTask) {
        this.fightTask = fightTask;
    }
    
    public RedroverState getState() {
        return this.state;
    }
    
    public RedroverPlayer getFightPlayer() {
        return this.fightPlayer;
    }
    
    public BukkitTask getFightTask() {
        return this.fightTask;
    }
    
    public enum RedroverState
    {
        WAITING, 
        PREPARING, 
        FIGHTING;
    }
}
