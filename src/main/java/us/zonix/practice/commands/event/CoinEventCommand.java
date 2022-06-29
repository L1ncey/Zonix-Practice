package us.zonix.practice.commands.event;

import java.util.Collection;
import us.zonix.practice.events.PracticeEvent;
import org.apache.commons.lang.math.NumberUtils;
import java.util.function.Consumer;
import java.util.Objects;
import us.zonix.practice.util.Clickable;
import org.bukkit.ChatColor;
import us.zonix.practice.events.EventState;
import org.bukkit.entity.Player;
import org.bukkit.command.CommandSender;
import me.maiko.dexter.util.CC;
import us.zonix.practice.Practice;
import org.bukkit.command.Command;

public class CoinEventCommand extends Command
{
    private final Practice plugin;
    
    public CoinEventCommand() {
        super("coineventhosting");
        this.plugin = Practice.getInstance();
        this.setDescription("Host an event.");
        this.setUsage(CC.RED + "Usage: /eventcoinbypass <event>");
    }
    
    public boolean execute(final CommandSender sender, final String alias, final String[] args) {
        if (!(sender instanceof Player)) {
            return true;
        }
        final Player player = (Player)sender;
        if (args.length < 1) {
            player.openInventory(this.plugin.getInventoryManager().getCoinseventsInventory().getCurrentPage());
            return true;
        }
        final String eventName = args[0];
        if (eventName == null) {
            return true;
        }
        if (this.plugin.getEventManager().getByName(eventName) == null) {
            player.sendMessage(CC.RED + "That event doesn't exist.");
            return true;
        }
        final PracticeEvent event = this.plugin.getEventManager().getByName(eventName);
        if (!event.isEnabled()) {
            player.sendMessage(CC.RED + "That event is currently disabled.");
        }
        if (event.getState() != EventState.UNANNOUNCED) {
            player.sendMessage(CC.RED + "There is currently an active event.");
            return true;
        }
        final boolean eventBeingHosted = this.plugin.getEventManager().getEvents().values().stream().anyMatch(e -> e.getState() != EventState.UNANNOUNCED);
        if (eventBeingHosted) {
            player.sendMessage(CC.RED + "There is currently an active event.");
            return true;
        }
        final String toSend = ChatColor.RED.toString() + ChatColor.BOLD + "[Event] " + ChatColor.WHITE + "" + event.getName() + " is starting soon. " + ChatColor.GRAY + "[Join]";
        final String toSendDonor = ChatColor.GRAY + "[" + ChatColor.BOLD + "*" + ChatColor.GRAY + "] " + ChatColor.RED.toString() + ChatColor.BOLD + player.getName() + ChatColor.WHITE + " is hosting a " + ChatColor.WHITE.toString() + ChatColor.BOLD + event.getName() + " Event. " + ChatColor.GRAY + "[Join]";
        final Clickable message = new Clickable(player.hasPermission("practice.donator") ? toSendDonor : toSend, ChatColor.GRAY + "Click to join this event.", "/join " + event.getName());
        final Collection onlinePlayers = this.plugin.getServer().getOnlinePlayers();
        final Clickable clickable = message;
        Objects.requireNonNull(clickable);
        onlinePlayers.forEach(clickable::sendToPlayer);
        if (player.hasPermission("host.limit.50")) {
            event.setLimit(50);
        }
        else if (player.hasPermission("host.limit.45")) {
            event.setLimit(45);
        }
        else if (player.hasPermission("host.limit.40")) {
            event.setLimit(40);
        }
        else if (player.hasPermission("host.limit.35")) {
            event.setLimit(35);
        }
        else {
            event.setLimit(30);
        }
        if (args.length == 2 && player.isOp()) {
            if (!NumberUtils.isNumber(args[1])) {
                player.sendMessage(CC.RED + "That's not a correct amount.");
                return true;
            }
            event.setLimit(Integer.parseInt(args[1]));
        }
        Practice.getInstance().getEventManager().hostEvent(event, player);
        return true;
    }
}
