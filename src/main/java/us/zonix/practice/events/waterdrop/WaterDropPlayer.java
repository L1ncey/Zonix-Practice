package us.zonix.practice.events.waterdrop;

import us.zonix.practice.events.PracticeEvent;
import java.util.UUID;
import us.zonix.practice.events.EventPlayer;

public class WaterDropPlayer extends EventPlayer
{
    private WaterDropState state;
    
    public WaterDropPlayer(final UUID uuid, final PracticeEvent event) {
        super(uuid, event);
        this.state = WaterDropState.LOBBY;
    }
    
    public void setState(final WaterDropState state) {
        this.state = state;
    }
    
    public WaterDropState getState() {
        return this.state;
    }
    
    public enum WaterDropState
    {
        LOBBY, 
        JUMPING, 
        NEXT_ROUND, 
        ELIMINATED;
    }
}
