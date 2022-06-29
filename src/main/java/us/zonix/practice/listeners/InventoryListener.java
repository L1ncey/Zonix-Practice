package us.zonix.practice.listeners;

import org.bukkit.event.EventHandler;
import us.zonix.practice.player.PlayerData;
import org.bukkit.Material;
import us.zonix.practice.player.PlayerState;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import us.zonix.practice.Practice;
import org.bukkit.event.Listener;

public class InventoryListener implements Listener
{
    private final Practice plugin;
    
    public InventoryListener() {
        this.plugin = Practice.getInstance();
    }
    
    @EventHandler
    public void onInventoryClick(final InventoryClickEvent event) {
        final Player player = (Player)event.getWhoClicked();
        if (!player.getGameMode().equals((Object)GameMode.CREATIVE)) {
            final PlayerData playerData = this.plugin.getPlayerManager().getPlayerData(player.getUniqueId());
            if (playerData.getPlayerState() == PlayerState.SPAWN || (playerData.getPlayerState() == PlayerState.EVENT && player.getItemInHand() != null && player.getItemInHand().getType() == Material.COMPASS)) {
                event.setCancelled(true);
            }
        }
    }
}
