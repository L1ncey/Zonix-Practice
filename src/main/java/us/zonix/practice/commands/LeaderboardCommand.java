package us.zonix.practice.commands;

import java.util.HashMap;
import java.util.Iterator;
import me.maiko.dexter.profile.Profile;
import java.util.UUID;
import java.util.Map;
import us.zonix.practice.util.ItemBuilder;
import us.zonix.practice.kit.Kit;
import org.bukkit.entity.Player;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import java.util.List;
import java.util.Arrays;
import org.bukkit.ChatColor;
import us.zonix.practice.util.inventory.InventoryUI;
import us.zonix.practice.Practice;
import org.bukkit.command.Command;

public class LeaderboardCommand extends Command
{
    private final Practice plugin;
    private final InventoryUI leaderboardInventory;
    
    public LeaderboardCommand() {
        super("leaderboard");
        this.plugin = Practice.getInstance();
        this.leaderboardInventory = new InventoryUI(ChatColor.DARK_GRAY + "Leaderboards", true, 2);
        this.setAliases((List)Arrays.asList("lb", "leaderboards"));
        this.setUsage(ChatColor.RED + "Usage: /leaderboard");
        new BukkitRunnable() {
            public void run() {
                LeaderboardCommand.this.updateInventory();
            }
        }.runTaskTimerAsynchronously((Plugin)this.plugin, 0L, 1200L);
    }
    
    public boolean execute(final CommandSender sender, final String s, final String[] args) {
        if (!(sender instanceof Player)) {
            return true;
        }
        final Player player = (Player)sender;
        player.openInventory(this.leaderboardInventory.getCurrentPage());
        return true;
    }
    
    private void updateInventory() {
        for (final Kit kit : this.plugin.getKitManager().getKits()) {
            if (!kit.isEnabled()) {
                continue;
            }
            if (!kit.isRanked()) {
                continue;
            }
            if (!kit.isPremium()) {
                continue;
            }
            final ItemBuilder kitItemBuilder = new ItemBuilder(kit.getIcon().getType()).durability(kit.getIcon().getDurability()).name(ChatColor.RED + kit.getName());
            final HashMap<String, Integer> eloMap = this.plugin.getPlayerManager().findTopEloByKit(kit.getName(), 10);
            int position = 1;
            for (final Map.Entry<String, Integer> entry : eloMap.entrySet()) {
                final String username = Profile.getNameByUUID(UUID.fromString(entry.getKey()));
                if (username != null) {
                    switch (position) {
                        case 1: {
                            kitItemBuilder.lore("&a1) &f" + username + " &7(" + entry.getValue() + " ELO)");
                            break;
                        }
                        case 2: {
                            kitItemBuilder.lore("&f2) &f" + username + " &7(" + entry.getValue() + " ELO)");
                            break;
                        }
                        case 3: {
                            kitItemBuilder.lore("&63) &f" + username + " &7(" + entry.getValue() + " ELO)");
                            break;
                        }
                        default: {
                            kitItemBuilder.lore("&7" + position + ") &f" + username + " &7(" + entry.getValue() + " ELO)");
                            break;
                        }
                    }
                    ++position;
                }
            }
            this.leaderboardInventory.setItem(kit.getPriority(), new InventoryUI.EmptyClickableItem(kitItemBuilder.build()));
        }
    }
}
