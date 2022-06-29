package us.zonix.practice.commands.management;

import java.util.Collection;
import us.zonix.practice.tournament.Tournament;
import us.zonix.practice.tournament.TournamentState;
import java.util.function.Consumer;
import java.util.Objects;
import org.bukkit.Bukkit;
import us.zonix.practice.util.Clickable;
import me.maiko.dexter.util.CC;
import org.bukkit.entity.Player;
import org.bukkit.command.CommandSender;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import us.zonix.practice.util.ItemUtil;
import us.zonix.practice.kit.Kit;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.ChatColor;
import us.zonix.practice.util.inventory.InventoryUI;
import us.zonix.practice.Practice;
import org.bukkit.command.Command;

public class TournamentCommand extends Command
{
    private final Practice plugin;
    private static final String[] HELP_ADMIN_MESSAGE;
    private static final String[] HELP_REGULAR_MESSAGE;
    private final InventoryUI inventoryUI;
    
    public TournamentCommand() {
        super("tournament");
        this.plugin = Practice.getInstance();
        this.inventoryUI = new InventoryUI(ChatColor.GRAY + "Start a tournament", 2);
        this.setUsage(ChatColor.RED + "Usage: /tournament [args]");
        new BukkitRunnable() {
            public void run() {
                TournamentCommand.this.setItems();
            }
        }.runTaskLater((Plugin)this.plugin, 30L);
    }
    
    private void setItems() {
        this.plugin.getKitManager().getKits().forEach(kit -> {
            if (kit.isEnabled()) {
                this.inventoryUI.setItem(kit.getPriority(), new InventoryUI.ClickableItem() {
                    private final ItemStack def;
                    private ItemStack itemStack;
                    final /* synthetic */ Kit val$kit;
                    
                    {
                        this.def = ItemUtil.renameItem(ItemUtil.reloreItem(this.val$kit.getIcon(), ChatColor.GRAY + "Click to host tournament"), ChatColor.GRAY + this.val$kit.getName());
                        this.itemStack = this.def.clone();
                    }
                    
                    @Override
                    public void onClick(final InventoryClickEvent event) {
                        final InventoryUI inventoryUI = new InventoryUI(ChatColor.GRAY + "Select team size", 1);
                        for (int i = 1; i < 5; ++i) {
                            final int finalI = i;
                            inventoryUI.addItem(new InventoryUI.ClickableItem() {
                                private final ItemStack def;
                                private ItemStack itemStack;
                                final /* synthetic */ int val$finalI;
                                
                                {
                                    this.def = ItemUtil.renameItem(ItemUtil.reloreItem(new ItemStack(Material.NAME_TAG), ChatColor.GRAY + "Teamsize: " + this.val$finalI), ChatColor.GRAY + ClickableItem.this.val$kit.getName());
                                    this.itemStack = this.def.clone();
                                }
                                
                                @Override
                                public void onClick(final InventoryClickEvent event) {
                                    if (!TournamentCommand.this.plugin.getTournamentManager().getTournaments().isEmpty()) {
                                        event.getWhoClicked().sendMessage(ChatColor.RED + "There already is an ongoing tournament.");
                                        event.getWhoClicked().closeInventory();
                                        return;
                                    }
                                    TournamentCommand.this.plugin.getTournamentManager().createTournament((CommandSender)event.getWhoClicked(), 10, this.val$finalI, 150, ClickableItem.this.val$kit.getName());
                                }
                                
                                @Override
                                public ItemStack getItemStack() {
                                    return this.itemStack;
                                }
                                
                                @Override
                                public void setItemStack(final ItemStack itemStack) {
                                    this.itemStack = itemStack;
                                }
                                
                                @Override
                                public ItemStack getDefaultItemStack() {
                                    return this.def;
                                }
                            });
                        }
                        event.getWhoClicked().openInventory(inventoryUI.getCurrentPage());
                    }
                    
                    @Override
                    public ItemStack getItemStack() {
                        return this.itemStack;
                    }
                    
                    @Override
                    public void setItemStack(final ItemStack itemStack) {
                        this.itemStack = itemStack;
                    }
                    
                    @Override
                    public ItemStack getDefaultItemStack() {
                        return this.def;
                    }
                });
            }
        });
    }
    
    public boolean execute(final CommandSender commandSender, final String s, final String[] args) {
        if (!(commandSender instanceof Player)) {
            return true;
        }
        final Player player = (Player)commandSender;
        if (args.length == 0) {
            commandSender.sendMessage(player.isOp() ? TournamentCommand.HELP_ADMIN_MESSAGE : TournamentCommand.HELP_REGULAR_MESSAGE);
            return true;
        }
        final String lowerCase = args[0].toLowerCase();
        switch (lowerCase) {
            case "host": {
                if (player.hasPermission("practice.tournament.host")) {
                    player.openInventory(this.inventoryUI.getCurrentPage());
                }
                else {
                    player.sendMessage(CC.RED + "No permission.");
                }
                return true;
            }
            case "start": {
                if (args.length == 5) {
                    try {
                        final int id = Integer.parseInt(args[1]);
                        final int teamSize = Integer.parseInt(args[3]);
                        final int size = Integer.parseInt(args[4]);
                        final String kitName = args[2];
                        if (size % teamSize != 0) {
                            commandSender.sendMessage(ChatColor.RED + "Tournament size & team sizes are invalid. Please try again.");
                            return true;
                        }
                        if (this.plugin.getTournamentManager().getTournament(Integer.valueOf(id)) != null) {
                            commandSender.sendMessage(ChatColor.RED + "This tournament already exists.");
                            return true;
                        }
                        final Kit kit = this.plugin.getKitManager().getKit(kitName);
                        if (kit == null) {
                            commandSender.sendMessage(ChatColor.RED + "That kit does not exist.");
                            return true;
                        }
                        this.plugin.getTournamentManager().createTournament(commandSender, id, teamSize, size, kitName);
                    }
                    catch (NumberFormatException e) {
                        commandSender.sendMessage(ChatColor.RED + "Usage: /tournament start <id> <kit> <team size> <tournament size>");
                    }
                    break;
                }
                commandSender.sendMessage(ChatColor.RED + "Usage: /tournament start <id> <kit> <team size> <tournament size>");
                break;
            }
            case "stop": {
                if (args.length == 2) {
                    final int id = Integer.parseInt(args[1]);
                    final Tournament tournament = this.plugin.getTournamentManager().getTournament(Integer.valueOf(id));
                    if (tournament != null) {
                        this.plugin.getTournamentManager().removeTournament(id, true);
                        commandSender.sendMessage(ChatColor.RED + "Successfully removed tournament " + id + ".");
                    }
                    commandSender.sendMessage(ChatColor.RED + "This tournament does not exist.");
                    break;
                }
                commandSender.sendMessage(ChatColor.RED + "Usage: /tournament stop <id>");
                break;
            }
            case "alert": {
                if (args.length == 2) {
                    final int id = Integer.parseInt(args[1]);
                    final Tournament tournament = this.plugin.getTournamentManager().getTournament(Integer.valueOf(id));
                    if (tournament != null) {
                        final String toSend = ChatColor.RED.toString() + ChatColor.BOLD + tournament.getKitName() + ChatColor.RED + " [" + tournament.getTeamSize() + "v" + tournament.getTeamSize() + "]" + ChatColor.WHITE + " is starting soon. " + ChatColor.GRAY + "[Click to Join]";
                        final Clickable message = new Clickable(toSend, ChatColor.GRAY + "Click to join this tournament.", "/join " + id);
                        final Collection onlinePlayers = Bukkit.getServer().getOnlinePlayers();
                        final Clickable clickable = message;
                        Objects.requireNonNull(clickable);
                        onlinePlayers.forEach(clickable::sendToPlayer);
                    }
                    break;
                }
                commandSender.sendMessage(ChatColor.RED + "Usage: /tournament alert <id>");
                break;
            }
            case "forcestart": {
                if (args.length == 2) {
                    final int id = Integer.parseInt(args[1]);
                    final Tournament tournament = this.plugin.getTournamentManager().getTournament(Integer.valueOf(id));
                    if (tournament != null) {
                        if (tournament.getTournamentState() == TournamentState.FIGHTING) {
                            commandSender.sendMessage(ChatColor.RED + "Tournament already started.");
                            return true;
                        }
                        tournament.setTournamentState(TournamentState.STARTING);
                        tournament.setCountdown(5);
                    }
                    break;
                }
                commandSender.sendMessage(ChatColor.RED + "Usage: /tournament forcestart <id>");
                break;
            }
            default: {
                commandSender.sendMessage(player.isOp() ? TournamentCommand.HELP_ADMIN_MESSAGE : TournamentCommand.HELP_REGULAR_MESSAGE);
                break;
            }
        }
        return false;
    }
    
    static {
        HELP_ADMIN_MESSAGE = new String[] { ChatColor.DARK_GRAY.toString() + ChatColor.STRIKETHROUGH + "----------------------------------------------------", ChatColor.RED + "Tournament Commands:", ChatColor.GOLD + "(*) /tournament start " + ChatColor.GRAY + "- Start a Tournament", ChatColor.GOLD + "(*) /tournament stop " + ChatColor.GRAY + "- Stop a Tournament", ChatColor.GOLD + "(*) /tournament alert " + ChatColor.GRAY + "- Alert a Tournament", ChatColor.GOLD + "(*) /tournament host " + ChatColor.GRAY + "- Open tournament GUI", ChatColor.GOLD + "(*) /tournament forcestart " + ChatColor.GRAY + "- Force start a tournament", ChatColor.DARK_GRAY.toString() + ChatColor.STRIKETHROUGH + "----------------------------------------------------" };
        HELP_REGULAR_MESSAGE = new String[] { ChatColor.DARK_GRAY.toString() + ChatColor.STRIKETHROUGH + "----------------------------------------------------", ChatColor.RED + "Tournament Commands:", ChatColor.GOLD + "(*) /join <id> " + ChatColor.GRAY + "- Join a Tournament", ChatColor.GOLD + "(*) /leave " + ChatColor.GRAY + "- Leave a Tournament", ChatColor.GOLD + "(*) /status " + ChatColor.GRAY + "- Status of a Tournament", ChatColor.DARK_GRAY.toString() + ChatColor.STRIKETHROUGH + "----------------------------------------------------" };
    }
}
