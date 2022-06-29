package us.zonix.practice.commands;

import org.bukkit.Bukkit;
import java.util.Optional;
import me.maiko.dexter.profile.Profile;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.Collection;
import java.util.ArrayList;
import us.zonix.practice.Practice;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.command.CommandSender;
import java.util.Arrays;
import java.util.List;
import org.bukkit.command.Command;

public class RegionLockCommand extends Command
{
    private static final List<String> ALL_REGIONS;
    
    public RegionLockCommand() {
        super("regionlock", "Zonix's Region Lock command..", "/regionlock", (List)Arrays.asList("continentlock", "region"));
    }
    
    public boolean execute(final CommandSender commandSender, final String label, final String[] args) {
        if (commandSender instanceof Player && !commandSender.isOp()) {
            return true;
        }
        if (args.length != 2) {
            return this.sendHelp(commandSender);
        }
        final String lowerCase = args[0].toLowerCase();
        switch (lowerCase) {
            case "add": {
                final String continent = args[1].toUpperCase();
                if (RegionLockCommand.ALL_REGIONS.stream().noneMatch(region -> region.equalsIgnoreCase(continent))) {
                    commandSender.sendMessage(new String[] { ChatColor.RED + "That continent is not a valid continent.", ChatColor.YELLOW + "Valid continents: " + ChatColor.RED + String.join(", ", RegionLockCommand.ALL_REGIONS) });
                    return true;
                }
                final List<String> allowedContinents = new ArrayList<String>(Practice.getInstance().getAllowedRegions());
                if (allowedContinents.contains(continent)) {
                    commandSender.sendMessage(ChatColor.RED + "That continent is already on the continent list.");
                    return true;
                }
                allowedContinents.add(continent);
                Practice.getInstance().setAllowedRegions(allowedContinents);
                commandSender.sendMessage(ChatColor.GREEN + String.format("Successfully added %s to the continent list.", continent));
                return true;
            }
            case "remove": {
                final String continent = args[1].toUpperCase();
                final List<String> allowedContinents = new ArrayList<String>(Practice.getInstance().getAllowedRegions());
                if (!allowedContinents.contains(continent)) {
                    commandSender.sendMessage(ChatColor.RED + "That continent is not on the continent list.");
                    return true;
                }
                allowedContinents.remove(continent);
                Practice.getInstance().setAllowedRegions(allowedContinents);
                commandSender.sendMessage(ChatColor.GREEN + String.format("Successfully removed %s from the continent list.", continent));
                return true;
            }
            case "list": {
                if (!args[1].equalsIgnoreCase("allowed") && !args[1].equalsIgnoreCase("banned")) {
                    return this.sendHelp(commandSender);
                }
                final List<String> allowedContinents2 = new ArrayList<String>(Practice.getInstance().getAllowedRegions());
                final boolean allowedList = args[1].equals("allowed");
                if (allowedList) {
                    commandSender.sendMessage(ChatColor.YELLOW + "Allowed Continents: " + ChatColor.RED + String.join(", ", allowedContinents2));
                    return true;
                }
                commandSender.sendMessage(ChatColor.YELLOW + "Disallowed Continents: " + ChatColor.RED + RegionLockCommand.ALL_REGIONS.stream().filter(s -> !allowedContinents2.contains(s)).collect((Collector<? super Object, ?, String>)Collectors.joining(", ")));
                return true;
            }
            case "toggle": {
                if (!args[1].equalsIgnoreCase("on") && !args[1].equalsIgnoreCase("off")) {
                    return true;
                }
                final boolean on = args[1].equalsIgnoreCase("on");
                if (on) {
                    Practice.getInstance().setRegionLock(true);
                    commandSender.sendMessage(ChatColor.GREEN + "Successfully toggled region-lock on.");
                    return true;
                }
                Practice.getInstance().setRegionLock(false);
                commandSender.sendMessage(ChatColor.GREEN + "Successfully toggled region-lock off.");
                return true;
            }
            case "check": {
                final Optional<Player> player = this.parsePlayer(args[1]);
                if (!player.isPresent()) {
                    return this.playerNotFound(commandSender, args[1]);
                }
                final Profile profile = Profile.getByUuid(player.get().getUniqueId());
                commandSender.sendMessage(profile.hasVpnData() ? (ChatColor.GREEN + String.format("%s's continent is: %s", player.get().getName(), profile.getVpnData().getContinent())) : (ChatColor.RED + "That player's region is not defined."));
                return true;
            }
            default: {
                return this.sendHelp(commandSender);
            }
        }
    }
    
    private boolean sendHelp(final CommandSender sender) {
        sender.sendMessage(new String[] { ChatColor.RED + "Zonix Region Lock", ChatColor.GRAY + "/regionlock add <region> - Add a region.", ChatColor.GRAY + "/regionlock remove <region> - Remove a region.", ChatColor.GRAY + "/regionlock toggle <on:off> - Toggle regionlock.", ChatColor.GRAY + "/regionlock list <allowed:banned>", ChatColor.GRAY + "/regionlock check <player> - Check a player's continent" });
        return true;
    }
    
    private boolean playerNotFound(final CommandSender sender, final String name) {
        sender.sendMessage(ChatColor.RED + String.format("The player with name %s could not be found.", name));
        return true;
    }
    
    private Optional<Player> parsePlayer(final String name) {
        return Optional.ofNullable(Bukkit.getPlayer(name));
    }
    
    static {
        ALL_REGIONS = Arrays.asList("EU", "SA", "NA", "AS", "OC", "AF", "AN");
    }
}
