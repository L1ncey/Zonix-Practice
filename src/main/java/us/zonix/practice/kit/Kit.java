package us.zonix.practice.kit;

import net.edater.spigot.EdaterSpigot;
import java.util.ArrayList;
import us.zonix.practice.bots.ZonixBot;
import org.bukkit.entity.Player;
import net.edater.spigot.knockback.KnockbackProfile;
import java.util.List;
import org.bukkit.inventory.ItemStack;

public class Kit
{
    private final String name;
    private ItemStack[] contents;
    private ItemStack[] armor;
    private ItemStack[] kitEditContents;
    private ItemStack icon;
    private List<String> excludedArenas;
    private List<String> arenaWhiteList;
    private boolean enabled;
    private boolean ranked;
    private boolean combo;
    private boolean sumo;
    private boolean build;
    private boolean spleef;
    private boolean parkour;
    private boolean hcteams;
    private boolean premium;
    private boolean bestOfThree;
    private int priority;
    private KnockbackProfile knockbackProfile;
    
    public void applyToPlayer(final Player player) {
        player.getInventory().setContents(this.contents);
        player.getInventory().setArmorContents(this.armor);
        player.updateInventory();
        player.setKnockbackProfile(this.knockbackProfile);
    }
    
    public void applyToNPC(final ZonixBot bot) {
        bot.getBukkitEntity().getInventory().setContents(this.contents);
        bot.getBukkitEntity().getInventory().setArmorContents(this.armor);
        bot.getBukkitEntity().updateInventory();
        bot.getBukkitEntity().setKnockbackProfile(this.knockbackProfile);
    }
    
    public void whitelistArena(final String arena) {
        if (!this.arenaWhiteList.remove(arena)) {
            this.arenaWhiteList.add(arena);
        }
    }
    
    public void excludeArena(final String arena) {
        if (!this.excludedArenas.remove(arena)) {
            this.excludedArenas.add(arena);
        }
    }
    
    public String getName() {
        return this.name;
    }
    
    public ItemStack[] getContents() {
        return this.contents;
    }
    
    public ItemStack[] getArmor() {
        return this.armor;
    }
    
    public ItemStack[] getKitEditContents() {
        return this.kitEditContents;
    }
    
    public ItemStack getIcon() {
        return this.icon;
    }
    
    public List<String> getExcludedArenas() {
        return this.excludedArenas;
    }
    
    public List<String> getArenaWhiteList() {
        return this.arenaWhiteList;
    }
    
    public boolean isEnabled() {
        return this.enabled;
    }
    
    public boolean isRanked() {
        return this.ranked;
    }
    
    public boolean isCombo() {
        return this.combo;
    }
    
    public boolean isSumo() {
        return this.sumo;
    }
    
    public boolean isBuild() {
        return this.build;
    }
    
    public boolean isSpleef() {
        return this.spleef;
    }
    
    public boolean isParkour() {
        return this.parkour;
    }
    
    public boolean isHcteams() {
        return this.hcteams;
    }
    
    public boolean isPremium() {
        return this.premium;
    }
    
    public boolean isBestOfThree() {
        return this.bestOfThree;
    }
    
    public int getPriority() {
        return this.priority;
    }
    
    public KnockbackProfile getKnockbackProfile() {
        return this.knockbackProfile;
    }
    
    public void setContents(final ItemStack[] contents) {
        this.contents = contents;
    }
    
    public void setArmor(final ItemStack[] armor) {
        this.armor = armor;
    }
    
    public void setKitEditContents(final ItemStack[] kitEditContents) {
        this.kitEditContents = kitEditContents;
    }
    
    public void setIcon(final ItemStack icon) {
        this.icon = icon;
    }
    
    public void setExcludedArenas(final List<String> excludedArenas) {
        this.excludedArenas = excludedArenas;
    }
    
    public void setArenaWhiteList(final List<String> arenaWhiteList) {
        this.arenaWhiteList = arenaWhiteList;
    }
    
    public void setEnabled(final boolean enabled) {
        this.enabled = enabled;
    }
    
    public void setRanked(final boolean ranked) {
        this.ranked = ranked;
    }
    
    public void setCombo(final boolean combo) {
        this.combo = combo;
    }
    
    public void setSumo(final boolean sumo) {
        this.sumo = sumo;
    }
    
    public void setBuild(final boolean build) {
        this.build = build;
    }
    
    public void setSpleef(final boolean spleef) {
        this.spleef = spleef;
    }
    
    public void setParkour(final boolean parkour) {
        this.parkour = parkour;
    }
    
    public void setHcteams(final boolean hcteams) {
        this.hcteams = hcteams;
    }
    
    public void setPremium(final boolean premium) {
        this.premium = premium;
    }
    
    public void setBestOfThree(final boolean bestOfThree) {
        this.bestOfThree = bestOfThree;
    }
    
    public void setPriority(final int priority) {
        this.priority = priority;
    }
    
    public void setKnockbackProfile(final KnockbackProfile knockbackProfile) {
        this.knockbackProfile = knockbackProfile;
    }
    
    public Kit(final String name, final ItemStack[] contents, final ItemStack[] armor, final ItemStack[] kitEditContents, final ItemStack icon, final List<String> excludedArenas, final List<String> arenaWhiteList, final boolean enabled, final boolean ranked, final boolean combo, final boolean sumo, final boolean build, final boolean spleef, final boolean parkour, final boolean hcteams, final boolean premium, final boolean bestOfThree, final int priority, final KnockbackProfile knockbackProfile) {
        this.contents = new ItemStack[36];
        this.armor = new ItemStack[4];
        this.kitEditContents = new ItemStack[36];
        this.excludedArenas = new ArrayList<String>();
        this.arenaWhiteList = new ArrayList<String>();
        this.enabled = false;
        this.ranked = false;
        this.combo = false;
        this.sumo = false;
        this.build = false;
        this.spleef = false;
        this.parkour = false;
        this.hcteams = false;
        this.premium = false;
        this.bestOfThree = false;
        this.priority = 0;
        this.knockbackProfile = EdaterSpigot.INSTANCE.getKnockbackHandler().getActiveProfile();
        this.name = name;
        this.contents = contents;
        this.armor = armor;
        this.kitEditContents = kitEditContents;
        this.icon = icon;
        this.excludedArenas = excludedArenas;
        this.arenaWhiteList = arenaWhiteList;
        this.enabled = enabled;
        this.ranked = ranked;
        this.combo = combo;
        this.sumo = sumo;
        this.build = build;
        this.spleef = spleef;
        this.parkour = parkour;
        this.hcteams = hcteams;
        this.premium = premium;
        this.bestOfThree = bestOfThree;
        this.priority = priority;
        this.knockbackProfile = knockbackProfile;
    }
    
    public Kit(final String name) {
        this.contents = new ItemStack[36];
        this.armor = new ItemStack[4];
        this.kitEditContents = new ItemStack[36];
        this.excludedArenas = new ArrayList<String>();
        this.arenaWhiteList = new ArrayList<String>();
        this.enabled = false;
        this.ranked = false;
        this.combo = false;
        this.sumo = false;
        this.build = false;
        this.spleef = false;
        this.parkour = false;
        this.hcteams = false;
        this.premium = false;
        this.bestOfThree = false;
        this.priority = 0;
        this.knockbackProfile = EdaterSpigot.INSTANCE.getKnockbackHandler().getActiveProfile();
        this.name = name;
    }
}
