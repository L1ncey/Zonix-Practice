package us.zonix.practice.commands.management;

import org.bukkit.Location;
import us.zonix.practice.arena.Arena;
import org.bukkit.plugin.Plugin;
import us.zonix.practice.runnable.ArenaCommandRunnable;
import us.zonix.practice.CustomLocation;
import org.bukkit.entity.Player;
import org.bukkit.command.CommandSender;
import org.bukkit.ChatColor;
import us.zonix.practice.Practice;
import org.bukkit.command.Command;

public class ArenaCommand extends Command
{
    private static final String NO_ARENA;
    private final Practice plugin;
    
    public ArenaCommand() {
        super("arena");
        this.plugin = Practice.getInstance();
        this.setDescription("Arenas command.");
        this.setUsage(ChatColor.RED + "Usage: /arena <subcommand> [args]");
    }
    
    public boolean execute(final CommandSender sender, final String alias, final String[] args) {
        if (!(sender instanceof Player)) {
            return true;
        }
        final Player player = (Player)sender;
        if (!player.hasPermission("practice.admin.arena")) {
            player.sendMessage(ChatColor.RED + "You do not have permission to use that command.");
            return true;
        }
        if (args.length < 2) {
            sender.sendMessage(this.usageMessage);
            return true;
        }
        final Arena arena = this.plugin.getArenaManager().getArena(args[1]);
        final String lowerCase = args[0].toLowerCase();
        switch (lowerCase) {
            case "create": {
                if (arena == null) {
                    this.plugin.getArenaManager().createArena(args[1]);
                    sender.sendMessage(ChatColor.GREEN + "Successfully created arena " + args[1] + ".");
                    break;
                }
                sender.sendMessage(ChatColor.RED + "That arena already exists!");
                break;
            }
            case "delete": {
                if (arena != null) {
                    this.plugin.getArenaManager().deleteArena(args[1]);
                    sender.sendMessage(ChatColor.GREEN + "Successfully deleted arena " + args[1] + ".");
                    break;
                }
                sender.sendMessage(ArenaCommand.NO_ARENA);
                break;
            }
            case "a": {
                if (arena != null) {
                    final Location location = player.getLocation();
                    if (args.length < 3 || !args[2].equalsIgnoreCase("-e")) {
                        location.setX(location.getBlockX() + 0.5);
                        location.setY(location.getBlockY() + 3.0);
                        location.setZ(location.getBlockZ() + 0.5);
                    }
                    arena.setA(CustomLocation.fromBukkitLocation(location));
                    sender.sendMessage(ChatColor.GREEN + "Successfully set position A for arena " + args[1] + ".");
                    break;
                }
                sender.sendMessage(ArenaCommand.NO_ARENA);
                break;
            }
            case "b": {
                if (arena != null) {
                    final Location location = player.getLocation();
                    if (args.length < 3 || !args[2].equalsIgnoreCase("-e")) {
                        location.setX(location.getBlockX() + 0.5);
                        location.setY(location.getBlockY() + 3.0);
                        location.setZ(location.getBlockZ() + 0.5);
                    }
                    arena.setB(CustomLocation.fromBukkitLocation(location));
                    sender.sendMessage(ChatColor.GREEN + "Successfully set position B for arena " + args[1] + ".");
                    break;
                }
                sender.sendMessage(ArenaCommand.NO_ARENA);
                break;
            }
            case "min": {
                if (arena != null) {
                    arena.setMin(CustomLocation.fromBukkitLocation(player.getLocation()));
                    sender.sendMessage(ChatColor.GREEN + "Successfully set minimum position for arena " + args[1] + ".");
                    break;
                }
                sender.sendMessage(ArenaCommand.NO_ARENA);
                break;
            }
            case "max": {
                if (arena != null) {
                    arena.setMax(CustomLocation.fromBukkitLocation(player.getLocation()));
                    sender.sendMessage(ChatColor.GREEN + "Successfully set maximum position for arena " + args[1] + ".");
                    break;
                }
                sender.sendMessage(ArenaCommand.NO_ARENA);
                break;
            }
            case "disable":
            case "enable": {
                if (arena != null) {
                    arena.setEnabled(!arena.isEnabled());
                    sender.sendMessage(arena.isEnabled() ? (ChatColor.GREEN + "Successfully enabled arena " + args[1] + ".") : (ChatColor.RED + "Successfully disabled arena " + args[1] + "."));
                    break;
                }
                sender.sendMessage(ArenaCommand.NO_ARENA);
                break;
            }
            case "generate": {
                if (args.length == 3) {
                    final int arenas = Integer.parseInt(args[2]);
                    this.plugin.getServer().getScheduler().runTask((Plugin)this.plugin, (Runnable)new ArenaCommandRunnable(this.plugin, arena, arenas));
                    this.plugin.getArenaManager().setGeneratingArenaRunnables(this.plugin.getArenaManager().getGeneratingArenaRunnables() + 1);
                    break;
                }
                sender.sendMessage(ChatColor.RED + "Usage: /arena generate <arena> <arenas>");
                break;
            }
            case "save": {
                this.plugin.getArenaManager().reloadArenas();
                sender.sendMessage(ChatColor.GREEN + "Successfully reloaded the arenas.");
                break;
            }
            case "manage": {
                this.plugin.getArenaManager().openArenaSystemUI(player);
                break;
            }
            default: {
                sender.sendMessage(this.usageMessage);
                break;
            }
        }
        return true;
    }
    
    static {
        NO_ARENA = ChatColor.RED + "That arena doesn't exist!";
    }
}
