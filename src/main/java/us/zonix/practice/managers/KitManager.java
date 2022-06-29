package us.zonix.practice.managers;

import java.util.Collection;
import org.bukkit.configuration.file.FileConfiguration;
import java.util.Optional;
import org.bukkit.configuration.ConfigurationSection;
import net.edater.spigot.knockback.KnockbackProfile;
import net.edater.spigot.EdaterSpigot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.ArrayList;
import java.util.HashMap;
import us.zonix.practice.file.ConfigFile;
import java.util.List;
import us.zonix.practice.kit.Kit;
import java.util.Map;
import us.zonix.practice.Practice;

public class KitManager
{
    private final Practice plugin;
    private final Map<String, Kit> kits;
    private final List<String> rankedKits;
    private final ConfigFile kitsFile;
    
    public KitManager() {
        this.plugin = Practice.getInstance();
        this.kits = new HashMap<String, Kit>();
        this.rankedKits = new ArrayList<String>();
        this.kitsFile = new ConfigFile(this.plugin, "kits");
        this.loadKits();
        this.kits.entrySet().stream().filter(kit -> kit.getValue().isEnabled()).filter(kit -> kit.getValue().isRanked()).forEach(kit -> this.rankedKits.add(kit.getKey()));
    }
    
    private void loadKits() {
        final ConfigurationSection kitSection = this.kitsFile.getConfiguration().getConfigurationSection("kits");
        if (kitSection == null) {
            return;
        }
        final ConfigurationSection configurationSection;
        final ItemStack[] contents;
        final ItemStack[] armor;
        final ItemStack[] kitEditContents;
        final List excludedArenas;
        final List arenaWhiteList;
        final ItemStack icon;
        final boolean enabled;
        final boolean ranked;
        final boolean combo;
        final boolean sumo;
        final boolean build;
        final boolean spleef;
        final boolean parkour;
        final boolean hcteams;
        final boolean bestOfThree;
        final boolean premium;
        final int priority;
        final Optional knockback;
        final Kit kit;
        kitSection.getKeys(false).forEach(name -> {
            contents = ((List)configurationSection.get(name + ".contents")).toArray(new ItemStack[0]);
            armor = ((List)configurationSection.get(name + ".armor")).toArray(new ItemStack[0]);
            kitEditContents = ((List)configurationSection.get(name + ".kitEditContents")).toArray(new ItemStack[0]);
            excludedArenas = configurationSection.getStringList(name + ".excludedArenas");
            arenaWhiteList = configurationSection.getStringList(name + ".arenaWhitelist");
            icon = (ItemStack)configurationSection.get(name + ".icon");
            enabled = configurationSection.getBoolean(name + ".enabled");
            ranked = configurationSection.getBoolean(name + ".ranked");
            combo = configurationSection.getBoolean(name + ".combo");
            sumo = configurationSection.getBoolean(name + ".sumo");
            build = configurationSection.getBoolean(name + ".build");
            spleef = configurationSection.getBoolean(name + ".spleef");
            parkour = configurationSection.getBoolean(name + ".parkour");
            hcteams = configurationSection.getBoolean(name + ".hcteams");
            bestOfThree = configurationSection.getBoolean(name + ".bestOfThree");
            premium = configurationSection.getBoolean(name + ".premium");
            priority = configurationSection.getInt(name + ".priority");
            knockback = EdaterSpigot.INSTANCE.getKnockbackHandler().getProfileByName(configurationSection.getString(name + ".knockback"));
            kit = new Kit(name, contents, armor, kitEditContents, icon, excludedArenas, arenaWhiteList, enabled, ranked, combo, sumo, build, spleef, parkour, hcteams, premium, bestOfThree, priority, knockback.orElseGet(() -> EdaterSpigot.INSTANCE.getKnockbackHandler().getActiveProfile()));
            this.kits.put(name, kit);
        });
    }
    
    public void saveKits() {
        final FileConfiguration fileConfig = (FileConfiguration)this.kitsFile.getConfiguration();
        fileConfig.set("kits", (Object)null);
        final FileConfiguration fileConfiguration;
        this.kits.forEach((kitName, kit) -> {
            if (kit.getIcon() != null && kit.getContents() != null && kit.getArmor() != null) {
                fileConfiguration.set("kits." + kitName + ".contents", (Object)kit.getContents());
                fileConfiguration.set("kits." + kitName + ".armor", (Object)kit.getArmor());
                fileConfiguration.set("kits." + kitName + ".kitEditContents", (Object)kit.getKitEditContents());
                fileConfiguration.set("kits." + kitName + ".icon", (Object)kit.getIcon());
                fileConfiguration.set("kits." + kitName + ".excludedArenas", (Object)kit.getExcludedArenas());
                fileConfiguration.set("kits." + kitName + ".arenaWhitelist", (Object)kit.getArenaWhiteList());
                fileConfiguration.set("kits." + kitName + ".enabled", (Object)kit.isEnabled());
                fileConfiguration.set("kits." + kitName + ".ranked", (Object)kit.isRanked());
                fileConfiguration.set("kits." + kitName + ".combo", (Object)kit.isCombo());
                fileConfiguration.set("kits." + kitName + ".sumo", (Object)kit.isSumo());
                fileConfiguration.set("kits." + kitName + ".build", (Object)kit.isBuild());
                fileConfiguration.set("kits." + kitName + ".spleef", (Object)kit.isSpleef());
                fileConfiguration.set("kits." + kitName + ".parkour", (Object)kit.isParkour());
                fileConfiguration.set("kits." + kitName + ".hcteams", (Object)kit.isHcteams());
                fileConfiguration.set("kits." + kitName + ".bestOfThree", (Object)kit.isBestOfThree());
                fileConfiguration.set("kits." + kitName + ".premium", (Object)kit.isPremium());
                fileConfiguration.set("kits." + kitName + ".priority", (Object)kit.getPriority());
                fileConfiguration.set("kits." + kitName + ".knockback", (Object)kit.getKnockbackProfile().getName());
            }
            return;
        });
        this.kitsFile.save();
    }
    
    public void deleteKit(final String name) {
        this.kits.remove(name);
    }
    
    public void createKit(final String name) {
        this.kits.put(name, new Kit(name));
    }
    
    public Collection<Kit> getKits() {
        return this.kits.values();
    }
    
    public Kit getKit(final String name) {
        return this.kits.get(name);
    }
    
    public List<String> getRankedKits() {
        return this.rankedKits;
    }
}
