package us.zonix.practice.events.lights;

import us.zonix.practice.events.PracticeEvent;
import java.util.UUID;
import us.zonix.practice.events.EventPlayer;

public class LightsPlayer extends EventPlayer
{
    private LightsState state;
    
    public LightsPlayer(final UUID uuid, final PracticeEvent event) {
        super(uuid, event);
        this.state = LightsState.WAITING;
    }
    
    public void setState(final LightsState state) {
        this.state = state;
    }
    
    public LightsState getState() {
        return this.state;
    }
    
    public enum LightsState
    {
        LOBBY, 
        WAITING, 
        INGAME;
    }
}
