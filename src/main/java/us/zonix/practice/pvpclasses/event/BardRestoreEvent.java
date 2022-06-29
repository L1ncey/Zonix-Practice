package us.zonix.practice.pvpclasses.event;

import us.zonix.practice.pvpclasses.PvPClass;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Event;

public class BardRestoreEvent extends Event
{
    private static final HandlerList handlers;
    private Player player;
    private PvPClass.SavedPotion potions;
    
    public BardRestoreEvent(final Player player, final PvPClass.SavedPotion potions) {
        this.player = player;
        this.potions = potions;
    }
    
    public Player getPlayer() {
        return this.player;
    }
    
    public PvPClass.SavedPotion getPotions() {
        return this.potions;
    }
    
    public HandlerList getHandlers() {
        return BardRestoreEvent.handlers;
    }
    
    public static HandlerList getHandlerList() {
        return BardRestoreEvent.handlers;
    }
    
    static {
        handlers = new HandlerList();
    }
}
