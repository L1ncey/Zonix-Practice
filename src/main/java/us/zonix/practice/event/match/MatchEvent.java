package us.zonix.practice.event.match;

import us.zonix.practice.match.Match;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Event;

public class MatchEvent extends Event
{
    private static final HandlerList HANDLERS;
    private final Match match;
    
    public static HandlerList getHandlerList() {
        return MatchEvent.HANDLERS;
    }
    
    public HandlerList getHandlers() {
        return MatchEvent.HANDLERS;
    }
    
    public Match getMatch() {
        return this.match;
    }
    
    public MatchEvent(final Match match) {
        this.match = match;
    }
    
    static {
        HANDLERS = new HandlerList();
    }
}
