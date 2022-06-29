package us.zonix.practice.util.inventory;

import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.Listener;

public class UIListener implements Listener
{
    @EventHandler
    public void onClick(final InventoryClickEvent event) {
        if (event.getInventory() == null) {
            return;
        }
        if (!(event.getInventory().getHolder() instanceof InventoryUI.InventoryUIHolder)) {
            return;
        }
        if (event.getCurrentItem() == null) {
            return;
        }
        final InventoryUI.InventoryUIHolder inventoryUIHolder = (InventoryUI.InventoryUIHolder)event.getInventory().getHolder();
        event.setCancelled(true);
        if (event.getClickedInventory() == null || !event.getInventory().equals(event.getClickedInventory())) {
            return;
        }
        final InventoryUI ui = inventoryUIHolder.getInventoryUI();
        final InventoryUI.ClickableItem item = ui.getCurrentUI().getItem(event.getSlot());
        if (item == null) {
            return;
        }
        item.onClick(event);
    }
}
