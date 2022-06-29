package us.zonix.practice.commands;

import org.bukkit.metadata.MetadataValue;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.entity.Player;
import org.bukkit.command.CommandSender;
import java.util.List;
import java.util.Arrays;
import org.bukkit.ChatColor;
import us.zonix.practice.Practice;
import org.bukkit.command.Command;

public class SilentCommand extends Command
{
    private final Practice plugin;
    
    public SilentCommand() {
        super("silent");
        this.plugin = Practice.getInstance();
        this.setDescription("Toggles silent mdoe.");
        this.setUsage(ChatColor.RED + "Usage: /silent");
        this.setAliases((List)Arrays.asList("flight"));
    }
    
    public boolean execute(final CommandSender sender, final String alias, final String[] args) {
        if (!(sender instanceof Player)) {
            return true;
        }
        final Player player = (Player)sender;
        if (!player.hasPermission("core.staff")) {
            player.sendMessage(ChatColor.RED + "You do not have permission to use that command.");
            return true;
        }
        if (player.hasMetadata("modmode")) {
            player.removeMetadata("modmode", (Plugin)this.plugin);
            player.removeMetadata("invisible", (Plugin)this.plugin);
            player.sendMessage(ChatColor.GREEN + "You have disabled silent mode.");
        }
        else {
            player.setMetadata("modmode", (MetadataValue)new FixedMetadataValue((Plugin)this.plugin, (Object)true));
            player.setMetadata("invisible", (MetadataValue)new FixedMetadataValue((Plugin)this.plugin, (Object)true));
            player.sendMessage(ChatColor.GREEN + "You have enabled silent mode.");
        }
        return true;
    }
}
