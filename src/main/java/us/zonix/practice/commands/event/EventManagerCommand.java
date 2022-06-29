package us.zonix.practice.commands.event;

import java.util.Iterator;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import us.zonix.practice.util.ItemUtil;
import org.bukkit.Material;
import us.zonix.practice.util.inventory.InventoryUI;
import us.zonix.practice.events.PracticeEvent;
import us.zonix.practice.events.woolmixup.WoolMixUpEvent;
import us.zonix.practice.events.EventState;
import org.bukkit.entity.Player;
import org.bukkit.command.CommandSender;
import org.bukkit.ChatColor;
import us.zonix.practice.Practice;
import org.bukkit.command.Command;

public class EventManagerCommand extends Command
{
    private final Practice plugin;
    
    public EventManagerCommand() {
        super("eventmanager");
        this.plugin = Practice.getInstance();
        this.setDescription("Manage an event.");
        this.setUsage(ChatColor.RED + "Usage: /eventmanager <start/end/status/cooldown> <event>");
    }
    
    public boolean execute(final CommandSender sender, final String alias, final String[] args) {
        if (!(sender instanceof Player)) {
            return true;
        }
        final Player player = (Player)sender;
        if (!player.hasPermission("practice.eventextra")) {
            player.sendMessage(ChatColor.RED + "You do not have permission to use that command.");
            return true;
        }
        if (args.length < 2) {
            player.openInventory(this.buildInventory().getCurrentPage());
            return true;
        }
        final String action = args[0];
        final String eventName = args[1];
        if (this.plugin.getEventManager().getByName(eventName) == null) {
            player.sendMessage(ChatColor.RED + "That event doesn't exist.");
            return true;
        }
        final PracticeEvent event = this.plugin.getEventManager().getByName(eventName);
        if (action.toUpperCase().equalsIgnoreCase("START") && event.getState() == EventState.WAITING) {
            event.getCountdownTask().setTimeUntilStart(5);
            player.sendMessage(ChatColor.RED + "Event was force started.");
        }
        else if (action.toUpperCase().equalsIgnoreCase("END") && event.getState() == EventState.STARTED) {
            event.end();
            player.sendMessage(ChatColor.RED + "Event was cancelled.");
        }
        else if (!action.toUpperCase().equalsIgnoreCase("STATUS")) {
            if (action.toUpperCase().equalsIgnoreCase("COOLDOWN")) {
                this.plugin.getEventManager().setCooldown(0L);
                player.sendMessage(ChatColor.RED + "Event cooldown was cancelled.");
            }
            else if (action.toUpperCase().equalsIgnoreCase("SETUP")) {
                if (eventName.toUpperCase().contains("WOOLMIXUP")) {
                    final WoolMixUpEvent woolMixUpEvent = (WoolMixUpEvent)event;
                    woolMixUpEvent.generateArena(player.getLocation());
                    player.sendMessage(ChatColor.GREEN + "Generating the arena.");
                }
            }
            else {
                player.sendMessage(this.usageMessage);
            }
        }
        return true;
    }
    
    private InventoryUI buildInventory() {
        final InventoryUI inventory = new InventoryUI("Event Manager", true, 1);
        inventory.addItem(new InventoryUI.AbstractClickableItem(ItemUtil.createItem(Material.NETHER_STAR, ChatColor.GREEN + "Force Start")) {
            @Override
            public void onClick(final InventoryClickEvent e) {
                final Player player = (Player)e.getWhoClicked();
                final PracticeEvent event = EventManagerCommand.this.getEventByState(EventState.WAITING);
                if (event == null) {
                    player.sendMessage(ChatColor.RED + "There is no active event.");
                    player.closeInventory();
                    return;
                }
                event.getCountdownTask().setTimeUntilStart(5);
                player.sendMessage(ChatColor.RED + "Event was force started.");
                player.closeInventory();
            }
        });
        inventory.addItem(new InventoryUI.AbstractClickableItem(ItemUtil.createItem(Material.EYE_OF_ENDER, ChatColor.GREEN + "Force End")) {
            @Override
            public void onClick(final InventoryClickEvent e) {
                final Player player = (Player)e.getWhoClicked();
                final PracticeEvent event = EventManagerCommand.this.getEventByState(EventState.STARTED);
                if (event == null) {
                    player.sendMessage(ChatColor.RED + "There is no active event.");
                    player.closeInventory();
                    return;
                }
                event.end();
                player.sendMessage(ChatColor.RED + "Event was cancelled.");
                player.closeInventory();
            }
        });
        inventory.addItem(new InventoryUI.AbstractClickableItem(ItemUtil.createItem(Material.WATCH, ChatColor.GREEN + "Remove Cooldown")) {
            @Override
            public void onClick(final InventoryClickEvent e) {
                final Player player = (Player)e.getWhoClicked();
                final boolean cooldown = System.currentTimeMillis() < EventManagerCommand.this.plugin.getEventManager().getCooldown();
                if (!cooldown) {
                    player.sendMessage(ChatColor.RED + "There is no active cooldown.");
                    player.closeInventory();
                    return;
                }
                Practice.getInstance().getEventManager().setCooldown(0L);
                player.sendMessage(ChatColor.RED + "Event cooldown was cancelled.");
                player.closeInventory();
            }
        });
        inventory.addItem(new InventoryUI.AbstractClickableItem(ItemUtil.createItem(Material.PAPER, ChatColor.GREEN + "Event Status")) {
            @Override
            public void onClick(final InventoryClickEvent e) {
                final Player player = (Player)e.getWhoClicked();
                final PracticeEvent event = EventManagerCommand.this.getEventByState(EventState.STARTED);
                if (event == null) {
                    player.sendMessage(ChatColor.RED + "There is no active event.");
                    player.closeInventory();
                    return;
                }
                final String[] message = { ChatColor.YELLOW + "Event: " + ChatColor.WHITE + event.getName(), ChatColor.YELLOW + "Host: " + ChatColor.WHITE + ((event.getHost() == null) ? "Player Left" : event.getHost().getName()), ChatColor.YELLOW + "Players: " + ChatColor.WHITE + event.getPlayers().size() + "/" + event.getLimit(), ChatColor.YELLOW + "State: " + ChatColor.WHITE + event.getState().name() };
                player.sendMessage(message);
                player.closeInventory();
            }
        });
        return inventory;
    }
    
    private PracticeEvent getEventByState(final EventState state) {
        for (final PracticeEvent event : Practice.getInstance().getEventManager().getEvents().values()) {
            if (event.getState() == state && event.getHost() != null) {
                return event;
            }
        }
        return null;
    }
}
