package us.zonix.practice.events;

import org.bukkit.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import java.util.UUID;

public class EventPlayer
{
    private final UUID uuid;
    private final PracticeEvent event;
    
    public Player getPlayer() {
        return Bukkit.getPlayer(this.uuid);
    }
    
    public boolean playerExists() {
        return this.getPlayer() != null;
    }
    
    public void sendMessage(final String... strings) {
        final Player player = this.getPlayer();
        if (player != null) {
            for (final String string : strings) {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', string));
            }
        }
    }
    
    public UUID getUuid() {
        return this.uuid;
    }
    
    public PracticeEvent getEvent() {
        return this.event;
    }
    
    public EventPlayer(final UUID uuid, final PracticeEvent event) {
        this.uuid = uuid;
        this.event = event;
    }
}
