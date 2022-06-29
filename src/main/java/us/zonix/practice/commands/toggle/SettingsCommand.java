package us.zonix.practice.commands.toggle;

import us.zonix.practice.player.PlayerData;
import org.bukkit.entity.Player;
import org.bukkit.command.CommandSender;
import java.util.List;
import java.util.Arrays;
import org.bukkit.ChatColor;
import us.zonix.practice.Practice;
import org.bukkit.command.Command;

public class SettingsCommand extends Command
{
    private final Practice plugin;
    
    public SettingsCommand() {
        super("settings");
        this.plugin = Practice.getInstance();
        this.setDescription("Toggles multiple settings.");
        this.setUsage(ChatColor.RED + "Usage: /settings");
        this.setAliases((List)Arrays.asList("options", "toggle"));
    }
    
    public boolean execute(final CommandSender sender, final String alias, final String[] args) {
        if (!(sender instanceof Player)) {
            return true;
        }
        final Player player = (Player)sender;
        final PlayerData playerData = this.plugin.getPlayerManager().getPlayerData(player.getUniqueId());
        player.openInventory(playerData.getOptions().getInventory());
        return true;
    }
}
