package us.zonix.practice.commands.management;

import us.zonix.practice.CustomLocation;
import org.bukkit.entity.Player;
import org.bukkit.command.CommandSender;
import org.bukkit.ChatColor;
import us.zonix.practice.Practice;
import org.bukkit.command.Command;

public class SpawnsCommand extends Command
{
    private final Practice plugin;
    
    public SpawnsCommand() {
        super("setspawn");
        this.plugin = Practice.getInstance();
        this.setDescription("Spawn command.");
        this.setUsage(ChatColor.RED + "Usage: /setspawn <subcommand>");
    }
    
    public boolean execute(final CommandSender sender, final String alias, final String[] args) {
        if (!(sender instanceof Player)) {
            return true;
        }
        final Player player = (Player)sender;
        if (!player.hasPermission("practice.admin.spawnmanager")) {
            player.sendMessage(ChatColor.RED + "You do not have permission to use that command.");
            return true;
        }
        if (args.length < 1) {
            sender.sendMessage(this.usageMessage);
            return true;
        }
        final String lowerCase = args[0].toLowerCase();
        switch (lowerCase) {
            case "spawnlocation": {
                this.plugin.getSpawnManager().setSpawnLocation(CustomLocation.fromBukkitLocation(player.getLocation()));
                this.plugin.getSpawnManager().getConfig().getConfiguration().set("SPAWN.LOCATION", (Object)CustomLocation.locationToString(CustomLocation.fromBukkitLocation(player.getLocation())));
                this.plugin.getSpawnManager().saveLocationsFile();
                player.sendMessage(ChatColor.GREEN + "Successfully set the spawn location.");
                break;
            }
            case "spawnmin": {
                this.plugin.getSpawnManager().setSpawnMin(CustomLocation.fromBukkitLocation(player.getLocation()));
                this.plugin.getSpawnManager().getConfig().getConfiguration().set("SPAWN.MIN", (Object)CustomLocation.locationToString(CustomLocation.fromBukkitLocation(player.getLocation())));
                this.plugin.getSpawnManager().saveLocationsFile();
                player.sendMessage(ChatColor.GREEN + "Successfully set the spawn min.");
                break;
            }
            case "spawnmax": {
                this.plugin.getSpawnManager().setSpawnMax(CustomLocation.fromBukkitLocation(player.getLocation()));
                this.plugin.getSpawnManager().getConfig().getConfiguration().set("SPAWN.MAX", (Object)CustomLocation.locationToString(CustomLocation.fromBukkitLocation(player.getLocation())));
                this.plugin.getSpawnManager().saveLocationsFile();
                player.sendMessage(ChatColor.GREEN + "Successfully set the spawn max.");
                break;
            }
            case "editorlocation": {
                this.plugin.getSpawnManager().setEditorLocation(CustomLocation.fromBukkitLocation(player.getLocation()));
                this.plugin.getSpawnManager().getConfig().getConfiguration().set("EDITOR.LOCATION", (Object)CustomLocation.locationToString(CustomLocation.fromBukkitLocation(player.getLocation())));
                this.plugin.getSpawnManager().saveLocationsFile();
                player.sendMessage(ChatColor.GREEN + "Successfully set the editor location.");
                break;
            }
            case "editormin": {
                this.plugin.getSpawnManager().setEditorMin(CustomLocation.fromBukkitLocation(player.getLocation()));
                this.plugin.getSpawnManager().getConfig().getConfiguration().set("EDITOR.MIN", (Object)CustomLocation.locationToString(CustomLocation.fromBukkitLocation(player.getLocation())));
                this.plugin.getSpawnManager().saveLocationsFile();
                player.sendMessage(ChatColor.GREEN + "Successfully set the editor min.");
                break;
            }
            case "editormax": {
                this.plugin.getSpawnManager().setEditorMax(CustomLocation.fromBukkitLocation(player.getLocation()));
                this.plugin.getSpawnManager().getConfig().getConfiguration().set("EDITOR.MAX", (Object)CustomLocation.locationToString(CustomLocation.fromBukkitLocation(player.getLocation())));
                this.plugin.getSpawnManager().saveLocationsFile();
                player.sendMessage(ChatColor.GREEN + "Successfully set the editor max.");
                break;
            }
            case "ffalocation": {
                this.plugin.getSpawnManager().setFfaLocation(CustomLocation.fromBukkitLocation(player.getLocation()));
                this.plugin.getSpawnManager().getConfig().getConfiguration().set("FFA.LOCATION", (Object)CustomLocation.locationToString(CustomLocation.fromBukkitLocation(player.getLocation())));
                this.plugin.getSpawnManager().saveLocationsFile();
                player.sendMessage(ChatColor.GREEN + "Successfully set the ffa location.");
                break;
            }
            case "ffamin": {
                this.plugin.getSpawnManager().setFfaMin(CustomLocation.fromBukkitLocation(player.getLocation()));
                this.plugin.getSpawnManager().getConfig().getConfiguration().set("FFA.MIN", (Object)CustomLocation.locationToString(CustomLocation.fromBukkitLocation(player.getLocation())));
                this.plugin.getSpawnManager().saveLocationsFile();
                player.sendMessage(ChatColor.GREEN + "Successfully set the ffa min.");
                break;
            }
            case "ffamax": {
                this.plugin.getSpawnManager().setFfaMax(CustomLocation.fromBukkitLocation(player.getLocation()));
                this.plugin.getSpawnManager().getConfig().getConfiguration().set("FFA.MAX", (Object)CustomLocation.locationToString(CustomLocation.fromBukkitLocation(player.getLocation())));
                this.plugin.getSpawnManager().saveLocationsFile();
                player.sendMessage(ChatColor.GREEN + "Successfully set the ffa max.");
                break;
            }
            case "sumolocation": {
                this.plugin.getSpawnManager().setSumoLocation(CustomLocation.fromBukkitLocation(player.getLocation()));
                this.plugin.getSpawnManager().getConfig().getConfiguration().set("SUMO.LOCATION", (Object)CustomLocation.locationToString(CustomLocation.fromBukkitLocation(player.getLocation())));
                this.plugin.getSpawnManager().saveLocationsFile();
                player.sendMessage(ChatColor.GREEN + "Successfully set the sumo location.");
                break;
            }
            case "sumofirst": {
                this.plugin.getSpawnManager().setSumoFirst(CustomLocation.fromBukkitLocation(player.getLocation()));
                this.plugin.getSpawnManager().getConfig().getConfiguration().set("SUMO.FIRST", (Object)CustomLocation.locationToString(CustomLocation.fromBukkitLocation(player.getLocation())));
                this.plugin.getSpawnManager().saveLocationsFile();
                player.sendMessage(ChatColor.GREEN + "Successfully set the sumo location A.");
                break;
            }
            case "sumosecond": {
                this.plugin.getSpawnManager().setSumoSecond(CustomLocation.fromBukkitLocation(player.getLocation()));
                this.plugin.getSpawnManager().getConfig().getConfiguration().set("SUMO.SECOND", (Object)CustomLocation.locationToString(CustomLocation.fromBukkitLocation(player.getLocation())));
                this.plugin.getSpawnManager().saveLocationsFile();
                player.sendMessage(ChatColor.GREEN + "Successfully set the sumo location B.");
                break;
            }
            case "sumomin": {
                this.plugin.getSpawnManager().setSumoMin(CustomLocation.fromBukkitLocation(player.getLocation()));
                this.plugin.getSpawnManager().getConfig().getConfiguration().set("SUMO.MIN", (Object)CustomLocation.locationToString(CustomLocation.fromBukkitLocation(player.getLocation())));
                this.plugin.getSpawnManager().saveLocationsFile();
                player.sendMessage(ChatColor.GREEN + "Successfully set the sumo min.");
                break;
            }
            case "sumomax": {
                this.plugin.getSpawnManager().setSumoMax(CustomLocation.fromBukkitLocation(player.getLocation()));
                this.plugin.getSpawnManager().getConfig().getConfiguration().set("SUMO.MAX", (Object)CustomLocation.locationToString(CustomLocation.fromBukkitLocation(player.getLocation())));
                this.plugin.getSpawnManager().saveLocationsFile();
                player.sendMessage(ChatColor.GREEN + "Successfully set the sumo max.");
                break;
            }
            case "oitclocation": {
                this.plugin.getSpawnManager().setOitcLocation(CustomLocation.fromBukkitLocation(player.getLocation()));
                this.plugin.getSpawnManager().getConfig().getConfiguration().set("OITC.LOCATION", (Object)CustomLocation.locationToString(CustomLocation.fromBukkitLocation(player.getLocation())));
                this.plugin.getSpawnManager().saveLocationsFile();
                player.sendMessage(ChatColor.GREEN + "Successfully set the OITC location.");
                break;
            }
            case "oitcmin": {
                this.plugin.getSpawnManager().setOitcMin(CustomLocation.fromBukkitLocation(player.getLocation()));
                this.plugin.getSpawnManager().getConfig().getConfiguration().set("OITC.MIN", (Object)CustomLocation.locationToString(CustomLocation.fromBukkitLocation(player.getLocation())));
                this.plugin.getSpawnManager().saveLocationsFile();
                player.sendMessage(ChatColor.GREEN + "Successfully set the OITC min.");
                break;
            }
            case "oitcmax": {
                this.plugin.getSpawnManager().setOitcMax(CustomLocation.fromBukkitLocation(player.getLocation()));
                this.plugin.getSpawnManager().getConfig().getConfiguration().set("OITC.MAX", (Object)CustomLocation.locationToString(CustomLocation.fromBukkitLocation(player.getLocation())));
                this.plugin.getSpawnManager().saveLocationsFile();
                player.sendMessage(ChatColor.GREEN + "Successfully set the OITC max.");
                break;
            }
            case "oitcspawnpoints": {
                this.plugin.getSpawnManager().getOitcSpawnpoints().add(CustomLocation.fromBukkitLocation(player.getLocation()));
                this.plugin.getSpawnManager().getConfig().getConfiguration().set("OITC.SPAWN_POINTS", (Object)this.plugin.getSpawnManager().fromLocations(this.plugin.getSpawnManager().getOitcSpawnpoints()));
                this.plugin.getSpawnManager().saveLocationsFile();
                player.sendMessage(ChatColor.GREEN + "Successfully set the OITC spawn-point #" + this.plugin.getSpawnManager().getOitcSpawnpoints().size() + ".");
                break;
            }
            case "parkourlocation": {
                this.plugin.getSpawnManager().setParkourLocation(CustomLocation.fromBukkitLocation(player.getLocation()));
                this.plugin.getSpawnManager().getConfig().getConfiguration().set("PARKOUR.LOCATION", (Object)CustomLocation.locationToString(CustomLocation.fromBukkitLocation(player.getLocation())));
                this.plugin.getSpawnManager().saveLocationsFile();
                player.sendMessage(ChatColor.GREEN + "Successfully set the parkour location.");
                break;
            }
            case "parkourgamelocation": {
                this.plugin.getSpawnManager().setParkourGameLocation(CustomLocation.fromBukkitLocation(player.getLocation()));
                this.plugin.getSpawnManager().getConfig().getConfiguration().set("PARKOUR.GAME_LOCATION", (Object)CustomLocation.locationToString(CustomLocation.fromBukkitLocation(player.getLocation())));
                this.plugin.getSpawnManager().saveLocationsFile();
                player.sendMessage(ChatColor.GREEN + "Successfully set the parkour Game location.");
                break;
            }
            case "parkourmax": {
                this.plugin.getSpawnManager().setParkourMax(CustomLocation.fromBukkitLocation(player.getLocation()));
                this.plugin.getSpawnManager().getConfig().getConfiguration().set("PARKOUR.MAX", (Object)CustomLocation.locationToString(CustomLocation.fromBukkitLocation(player.getLocation())));
                this.plugin.getSpawnManager().saveLocationsFile();
                player.sendMessage(ChatColor.GREEN + "Successfully set the parkour max location.");
                break;
            }
            case "parkourmin": {
                this.plugin.getSpawnManager().setParkourMin(CustomLocation.fromBukkitLocation(player.getLocation()));
                this.plugin.getSpawnManager().getConfig().getConfiguration().set("PARKOUR.MIN", (Object)CustomLocation.locationToString(CustomLocation.fromBukkitLocation(player.getLocation())));
                this.plugin.getSpawnManager().saveLocationsFile();
                player.sendMessage(ChatColor.GREEN + "Successfully set the parkour min location.");
                break;
            }
            case "waterdroplocation": {
                this.plugin.getSpawnManager().setWaterDropLocation(CustomLocation.fromBukkitLocation(player.getLocation()));
                this.plugin.getSpawnManager().getConfig().getConfiguration().set("WATERDROP.LOCATION", (Object)CustomLocation.locationToString(CustomLocation.fromBukkitLocation(player.getLocation())));
                this.plugin.getSpawnManager().saveLocationsFile();
                player.sendMessage(ChatColor.GREEN + "Successfully set the waterdrop location.");
                break;
            }
            case "waterdropjump": {
                this.plugin.getSpawnManager().setWaterDropJump(CustomLocation.fromBukkitLocation(player.getLocation()));
                this.plugin.getSpawnManager().getConfig().getConfiguration().set("WATERDROP.JUMP_LOCATION", (Object)CustomLocation.locationToString(CustomLocation.fromBukkitLocation(player.getLocation())));
                this.plugin.getSpawnManager().saveLocationsFile();
                player.sendMessage(ChatColor.GREEN + "Successfully set the waterdrop jump location.");
                break;
            }
            case "waterdropmin": {
                this.plugin.getSpawnManager().setWaterDropMin(CustomLocation.fromBukkitLocation(player.getLocation()));
                this.plugin.getSpawnManager().getConfig().getConfiguration().set("WATERDROP.MIN", (Object)CustomLocation.locationToString(CustomLocation.fromBukkitLocation(player.getLocation())));
                this.plugin.getSpawnManager().saveLocationsFile();
                player.sendMessage(ChatColor.GREEN + "Successfully set the waterdrop min location.");
                break;
            }
            case "waterdropmax": {
                this.plugin.getSpawnManager().setWaterDropMax(CustomLocation.fromBukkitLocation(player.getLocation()));
                this.plugin.getSpawnManager().getConfig().getConfiguration().set("WATERDROP.MAX", (Object)CustomLocation.locationToString(CustomLocation.fromBukkitLocation(player.getLocation())));
                this.plugin.getSpawnManager().saveLocationsFile();
                player.sendMessage(ChatColor.GREEN + "Successfully set the waterdrop max location.");
                break;
            }
            case "waterdropfirst": {
                this.plugin.getSpawnManager().setWaterDropFirst(CustomLocation.fromBukkitLocation(player.getLocation().clone().subtract(0.0, 1.0, 0.0)));
                this.plugin.getSpawnManager().getConfig().getConfiguration().set("WATERDROP.FIRST", (Object)CustomLocation.locationToString(CustomLocation.fromBukkitLocation(player.getLocation())));
                this.plugin.getSpawnManager().saveLocationsFile();
                player.sendMessage(ChatColor.GREEN + "Successfully set the waterdrop first location.");
                break;
            }
            case "waterdropsecond": {
                this.plugin.getSpawnManager().setWaterDropSecond(CustomLocation.fromBukkitLocation(player.getLocation().clone().subtract(0.0, 1.0, 0.0)));
                this.plugin.getSpawnManager().getConfig().getConfiguration().set("WATERDROP.SECOND", (Object)CustomLocation.locationToString(CustomLocation.fromBukkitLocation(player.getLocation())));
                this.plugin.getSpawnManager().saveLocationsFile();
                player.sendMessage(ChatColor.GREEN + "Successfully set the waterdrop second location.");
                break;
            }
            case "redroverlocation": {
                this.plugin.getSpawnManager().setRedroverLocation(CustomLocation.fromBukkitLocation(player.getLocation()));
                this.plugin.getSpawnManager().getConfig().getConfiguration().set("REDROVER.LOCATION", (Object)CustomLocation.locationToString(CustomLocation.fromBukkitLocation(player.getLocation())));
                this.plugin.getSpawnManager().saveLocationsFile();
                player.sendMessage(ChatColor.GREEN + "Successfully set the redrover location.");
                break;
            }
            case "redroverfirst": {
                this.plugin.getSpawnManager().setRedroverFirst(CustomLocation.fromBukkitLocation(player.getLocation()));
                this.plugin.getSpawnManager().getConfig().getConfiguration().set("REDROVER.FIRST", (Object)CustomLocation.locationToString(CustomLocation.fromBukkitLocation(player.getLocation())));
                this.plugin.getSpawnManager().saveLocationsFile();
                player.sendMessage(ChatColor.GREEN + "Successfully set the redrover location A.");
                break;
            }
            case "redroversecond": {
                this.plugin.getSpawnManager().setRedroverSecond(CustomLocation.fromBukkitLocation(player.getLocation()));
                this.plugin.getSpawnManager().getConfig().getConfiguration().set("REDROVER.SECOND", (Object)CustomLocation.locationToString(CustomLocation.fromBukkitLocation(player.getLocation())));
                this.plugin.getSpawnManager().saveLocationsFile();
                player.sendMessage(ChatColor.GREEN + "Successfully set the redrover location B.");
                break;
            }
            case "redrovermin": {
                this.plugin.getSpawnManager().setRedroverMin(CustomLocation.fromBukkitLocation(player.getLocation()));
                this.plugin.getSpawnManager().getConfig().getConfiguration().set("REDROVER.MIN", (Object)CustomLocation.locationToString(CustomLocation.fromBukkitLocation(player.getLocation())));
                this.plugin.getSpawnManager().saveLocationsFile();
                player.sendMessage(ChatColor.GREEN + "Successfully set the redrover min.");
                break;
            }
            case "redrovermax": {
                this.plugin.getSpawnManager().setRedroverMax(CustomLocation.fromBukkitLocation(player.getLocation()));
                this.plugin.getSpawnManager().getConfig().getConfiguration().set("REDROVER.MAX", (Object)CustomLocation.locationToString(CustomLocation.fromBukkitLocation(player.getLocation())));
                this.plugin.getSpawnManager().saveLocationsFile();
                player.sendMessage(ChatColor.GREEN + "Successfully set the redrover max.");
                break;
            }
            case "woollocation": {
                this.plugin.getSpawnManager().setWoolLocation(CustomLocation.fromBukkitLocation(player.getLocation()));
                this.plugin.getSpawnManager().getConfig().getConfiguration().set("WOOL.LOCATION", (Object)CustomLocation.locationToString(CustomLocation.fromBukkitLocation(player.getLocation())));
                this.plugin.getSpawnManager().saveLocationsFile();
                player.sendMessage(ChatColor.GREEN + "Successfully set the wool location.");
                break;
            }
            case "woolmax": {
                this.plugin.getSpawnManager().setWoolMax(CustomLocation.fromBukkitLocation(player.getLocation()));
                this.plugin.getSpawnManager().getConfig().getConfiguration().set("WOOL.MAX", (Object)CustomLocation.locationToString(CustomLocation.fromBukkitLocation(player.getLocation())));
                this.plugin.getSpawnManager().saveLocationsFile();
                player.sendMessage(ChatColor.GREEN + "Successfully set the wool max.");
                break;
            }
            case "woolmin": {
                this.plugin.getSpawnManager().setWoolMin(CustomLocation.fromBukkitLocation(player.getLocation()));
                this.plugin.getSpawnManager().getConfig().getConfiguration().set("WOOL.MIN", (Object)CustomLocation.locationToString(CustomLocation.fromBukkitLocation(player.getLocation())));
                this.plugin.getSpawnManager().saveLocationsFile();
                player.sendMessage(ChatColor.GREEN + "Successfully set the wool min.");
                break;
            }
            case "woolcenter": {
                this.plugin.getSpawnManager().setWoolCenter(CustomLocation.fromBukkitLocation(player.getLocation()));
                this.plugin.getSpawnManager().getConfig().getConfiguration().set("WOOL.CENTER", (Object)CustomLocation.locationToString(CustomLocation.fromBukkitLocation(player.getLocation())));
                this.plugin.getSpawnManager().saveLocationsFile();
                player.sendMessage(ChatColor.GREEN + "Successfully set the wool center.");
                break;
            }
            case "lightslocation": {
                this.plugin.getSpawnManager().setLightsLocation(CustomLocation.fromBukkitLocation(player.getLocation()));
                this.plugin.getSpawnManager().getConfig().getConfiguration().set("LIGHTS.LOCATION", (Object)CustomLocation.locationToString(CustomLocation.fromBukkitLocation(player.getLocation())));
                this.plugin.getSpawnManager().saveLocationsFile();
                player.sendMessage(ChatColor.GREEN + "Successfully set the lights spawn.");
                break;
            }
            case "lightsstart": {
                this.plugin.getSpawnManager().setLightsStart(CustomLocation.fromBukkitLocation(player.getLocation()));
                this.plugin.getSpawnManager().getConfig().getConfiguration().set("LIGHTS.START", (Object)CustomLocation.locationToString(CustomLocation.fromBukkitLocation(player.getLocation())));
                this.plugin.getSpawnManager().saveLocationsFile();
                player.sendMessage(ChatColor.GREEN + "Successfully set the lights start.");
                break;
            }
            case "lightsmax": {
                this.plugin.getSpawnManager().setLightsMax(CustomLocation.fromBukkitLocation(player.getLocation()));
                this.plugin.getSpawnManager().getConfig().getConfiguration().set("LIGHTS.MAX", (Object)CustomLocation.locationToString(CustomLocation.fromBukkitLocation(player.getLocation())));
                this.plugin.getSpawnManager().saveLocationsFile();
                player.sendMessage(ChatColor.GREEN + "Successfully set the lights max.");
                break;
            }
            case "lightsmin": {
                this.plugin.getSpawnManager().setLightsMin(CustomLocation.fromBukkitLocation(player.getLocation()));
                this.plugin.getSpawnManager().getConfig().getConfiguration().set("LIGHTS.MIN", (Object)CustomLocation.locationToString(CustomLocation.fromBukkitLocation(player.getLocation())));
                this.plugin.getSpawnManager().saveLocationsFile();
                player.sendMessage(ChatColor.GREEN + "Successfully set the lights min.");
                break;
            }
            case "tntspawn": {
                this.plugin.getSpawnManager().setTntTagSpawn(CustomLocation.fromBukkitLocation(player.getLocation()));
                this.plugin.getSpawnManager().getConfig().getConfiguration().set("TNT_TAG.SPAWN", (Object)CustomLocation.locationToString(CustomLocation.fromBukkitLocation(player.getLocation())));
                this.plugin.getSpawnManager().saveLocationsFile();
                player.sendMessage(ChatColor.GREEN + "Successfully set the tnt spawn.");
                break;
            }
            case "tntlocation": {
                this.plugin.getSpawnManager().setTntTagLocation(CustomLocation.fromBukkitLocation(player.getLocation()));
                this.plugin.getSpawnManager().getConfig().getConfiguration().set("TNT_TAG.LOCATION", (Object)CustomLocation.locationToString(CustomLocation.fromBukkitLocation(player.getLocation())));
                this.plugin.getSpawnManager().saveLocationsFile();
                player.sendMessage(ChatColor.GREEN + "Successfully set the tnt location.");
                break;
            }
        }
        return false;
    }
}
