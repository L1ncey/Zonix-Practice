package us.zonix.practice.event;

import us.zonix.practice.player.PlayerData;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Event;

public class PlayerDataRetrieveEvent extends Event
{
    private static final HandlerList HANDLERS;
    private final PlayerData playerData;
    
    public static HandlerList getHandlerList() {
        return PlayerDataRetrieveEvent.HANDLERS;
    }
    
    public HandlerList getHandlers() {
        return PlayerDataRetrieveEvent.HANDLERS;
    }
    
    public PlayerData getPlayerData() {
        return this.playerData;
    }
    
    public PlayerDataRetrieveEvent(final PlayerData playerData) {
        this.playerData = playerData;
    }
    
    static {
        HANDLERS = new HandlerList();
    }
}
