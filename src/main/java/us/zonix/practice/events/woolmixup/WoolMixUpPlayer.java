package us.zonix.practice.events.woolmixup;

import us.zonix.practice.events.PracticeEvent;
import java.util.UUID;
import us.zonix.practice.events.EventPlayer;

public class WoolMixUpPlayer extends EventPlayer
{
    private WoolMixUpState state;
    
    public WoolMixUpPlayer(final UUID uuid, final PracticeEvent event) {
        super(uuid, event);
        this.state = WoolMixUpState.WAITING;
    }
    
    public void setState(final WoolMixUpState state) {
        this.state = state;
    }
    
    public WoolMixUpState getState() {
        return this.state;
    }
    
    public enum WoolMixUpState
    {
        LOBBY, 
        WAITING, 
        INGAME;
    }
}
