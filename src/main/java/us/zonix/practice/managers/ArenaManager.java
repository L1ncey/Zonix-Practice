package us.zonix.practice.managers;

import java.util.Collection;
import org.bukkit.configuration.ConfigurationSection;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.ArrayList;
import us.zonix.practice.kit.Kit;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import us.zonix.practice.util.ItemUtil;
import org.bukkit.Material;
import us.zonix.practice.util.inventory.InventoryUI;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import java.util.Iterator;
import org.bukkit.configuration.file.FileConfiguration;
import us.zonix.practice.CustomLocation;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.HashMap;
import java.util.UUID;
import us.zonix.practice.arena.StandaloneArena;
import us.zonix.practice.arena.Arena;
import java.util.Map;
import us.zonix.practice.file.ConfigFile;
import us.zonix.practice.Practice;

public class ArenaManager
{
    private final Practice plugin;
    private final ConfigFile arenasFile;
    private final Map<String, Arena> arenas;
    private final Map<StandaloneArena, UUID> arenaMatchUUIDs;
    private int generatingArenaRunnables;
    
    public ArenaManager() {
        this.plugin = Practice.getInstance();
        this.arenas = new HashMap<String, Arena>();
        this.arenaMatchUUIDs = new HashMap<StandaloneArena, UUID>();
        this.arenasFile = new ConfigFile(this.plugin, "arenas");
        this.loadArenas();
    }
    
    private void loadArenas() {
        // 
        // This method could not be decompiled.
        // 
        // Original Bytecode:
        // 
        //     0: aload_0         /* this */
        //     1: getfield        us/zonix/practice/managers/ArenaManager.arenasFile:Lus/zonix/practice/file/ConfigFile;
        //     4: invokevirtual   us/zonix/practice/file/ConfigFile.getConfiguration:()Lorg/bukkit/configuration/file/YamlConfiguration;
        //     7: astore_1        /* fileConfig */
        //     8: aload_1         /* fileConfig */
        //     9: ldc             "arenas"
        //    11: invokevirtual   org/bukkit/configuration/file/FileConfiguration.getConfigurationSection:(Ljava/lang/String;)Lorg/bukkit/configuration/ConfigurationSection;
        //    14: astore_2        /* arenaSection */
        //    15: aload_2         /* arenaSection */
        //    16: ifnonnull       20
        //    19: return         
        //    20: aload_2         /* arenaSection */
        //    21: iconst_0       
        //    22: invokeinterface org/bukkit/configuration/ConfigurationSection.getKeys:(Z)Ljava/util/Set;
        //    27: aload_0         /* this */
        //    28: aload_2         /* arenaSection */
        //    29: invokedynamic   accept:(Lus/zonix/practice/managers/ArenaManager;Lorg/bukkit/configuration/ConfigurationSection;)Ljava/util/function/Consumer;
        //    34: invokeinterface java/util/Set.forEach:(Ljava/util/function/Consumer;)V
        //    39: return         
        //    LocalVariableTable:
        //  Start  Length  Slot  Name          Signature
        //  -----  ------  ----  ------------  -------------------------------------------------
        //  0      40      0     this          Lus/zonix/practice/managers/ArenaManager;
        //  8      32      1     fileConfig    Lorg/bukkit/configuration/file/FileConfiguration;
        //  15     25      2     arenaSection  Lorg/bukkit/configuration/ConfigurationSection;
        // 
        // The error that occurred was:
        // 
        // java.lang.IllegalStateException: Could not infer any expression.
        //     at com.strobel.decompiler.ast.TypeAnalysis.runInference(TypeAnalysis.java:374)
        //     at com.strobel.decompiler.ast.TypeAnalysis.run(TypeAnalysis.java:96)
        //     at com.strobel.decompiler.ast.AstOptimizer.optimize(AstOptimizer.java:344)
        //     at com.strobel.decompiler.ast.AstOptimizer.optimize(AstOptimizer.java:42)
        //     at com.strobel.decompiler.languages.java.ast.AstMethodBodyBuilder.createMethodBody(AstMethodBodyBuilder.java:214)
        //     at com.strobel.decompiler.languages.java.ast.AstMethodBodyBuilder.createMethodBody(AstMethodBodyBuilder.java:99)
        //     at com.strobel.decompiler.languages.java.ast.AstBuilder.createMethodBody(AstBuilder.java:757)
        //     at com.strobel.decompiler.languages.java.ast.AstBuilder.createMethod(AstBuilder.java:655)
        //     at com.strobel.decompiler.languages.java.ast.AstBuilder.addTypeMembers(AstBuilder.java:532)
        //     at com.strobel.decompiler.languages.java.ast.AstBuilder.createTypeCore(AstBuilder.java:499)
        //     at com.strobel.decompiler.languages.java.ast.AstBuilder.createTypeNoCache(AstBuilder.java:141)
        //     at com.strobel.decompiler.languages.java.ast.AstBuilder.createType(AstBuilder.java:130)
        //     at com.strobel.decompiler.languages.java.ast.AstBuilder.addType(AstBuilder.java:105)
        //     at com.strobel.decompiler.languages.java.JavaLanguage.buildAst(JavaLanguage.java:71)
        //     at com.strobel.decompiler.languages.java.JavaLanguage.decompileType(JavaLanguage.java:59)
        //     at com.strobel.decompiler.DecompilerDriver.decompileType(DecompilerDriver.java:317)
        //     at com.strobel.decompiler.DecompilerDriver.decompileJar(DecompilerDriver.java:238)
        //     at com.strobel.decompiler.DecompilerDriver.main(DecompilerDriver.java:123)
        // 
        throw new IllegalStateException("An error occurred while decompiling this method.");
    }
    
    public void saveArenas() {
        final FileConfiguration fileConfig = (FileConfiguration)this.arenasFile.getConfiguration();
        fileConfig.set("arenas", (Object)null);
        final String a;
        final String b;
        final String min;
        final String max;
        final String arenaRoot;
        final FileConfiguration fileConfiguration;
        int i;
        final Iterator<StandaloneArena> iterator;
        StandaloneArena saArena;
        String saA;
        String saB;
        String saMin;
        String saMax;
        String standAloneRoot;
        this.arenas.forEach((arenaName, arena) -> {
            a = CustomLocation.locationToString(arena.getA());
            b = CustomLocation.locationToString(arena.getB());
            min = CustomLocation.locationToString(arena.getMin());
            max = CustomLocation.locationToString(arena.getMax());
            arenaRoot = "arenas." + arenaName;
            fileConfiguration.set(arenaRoot + ".a", (Object)a);
            fileConfiguration.set(arenaRoot + ".b", (Object)b);
            fileConfiguration.set(arenaRoot + ".min", (Object)min);
            fileConfiguration.set(arenaRoot + ".max", (Object)max);
            fileConfiguration.set(arenaRoot + ".enabled", (Object)arena.isEnabled());
            fileConfiguration.set(arenaRoot + ".standaloneArenas", (Object)null);
            i = 0;
            if (arena.getStandaloneArenas() != null) {
                arena.getStandaloneArenas().iterator();
                while (iterator.hasNext()) {
                    saArena = iterator.next();
                    saA = CustomLocation.locationToString(saArena.getA());
                    saB = CustomLocation.locationToString(saArena.getB());
                    saMin = CustomLocation.locationToString(saArena.getMin());
                    saMax = CustomLocation.locationToString(saArena.getMax());
                    standAloneRoot = arenaRoot + ".standaloneArenas." + i;
                    fileConfiguration.set(standAloneRoot + ".a", (Object)saA);
                    fileConfiguration.set(standAloneRoot + ".b", (Object)saB);
                    fileConfiguration.set(standAloneRoot + ".min", (Object)saMin);
                    fileConfiguration.set(standAloneRoot + ".max", (Object)saMax);
                    ++i;
                }
            }
            return;
        });
        this.arenasFile.save();
    }
    
    public void reloadArenas() {
        this.saveArenas();
        this.arenas.clear();
        this.loadArenas();
    }
    
    public void openArenaSystemUI(final Player player) {
        if (this.arenas.size() == 0) {
            player.sendMessage(ChatColor.RED + "There's no arenas.");
            return;
        }
        final InventoryUI inventory = new InventoryUI("Arena System", true, 6);
        for (final Arena arena : this.arenas.values()) {
            final ItemStack item = ItemUtil.createItem(Material.PAPER, ChatColor.YELLOW + arena.getName() + ChatColor.GRAY + " (" + (arena.isEnabled() ? (ChatColor.GREEN.toString() + ChatColor.BOLD + "ENABLED") : (ChatColor.RED.toString() + ChatColor.BOLD + "DISABLED")) + ChatColor.GRAY + ")");
            ItemUtil.reloreItem(item, ChatColor.GRAY + "Arenas: " + ChatColor.GREEN + ((arena.getStandaloneArenas().size() == 0) ? "Single Arena (Invisible Players)" : (arena.getStandaloneArenas().size() + " Arenas")), ChatColor.GRAY + "Standalone Arenas: " + ChatColor.GREEN + ((arena.getAvailableArenas().size() == 0) ? "None" : (arena.getAvailableArenas().size() + " Arenas Available")), "", ChatColor.YELLOW.toString() + ChatColor.BOLD + "LEFT CLICK " + ChatColor.GRAY + "Teleport to Arena", ChatColor.YELLOW.toString() + ChatColor.BOLD + "RIGHT CLICK " + ChatColor.GRAY + "Generate Standalone Arenas");
            inventory.addItem(new InventoryUI.AbstractClickableItem(item) {
                @Override
                public void onClick(final InventoryClickEvent event) {
                    final Player player = (Player)event.getWhoClicked();
                    if (event.getClick() == ClickType.LEFT) {
                        player.teleport(arena.getA().toBukkitLocation());
                    }
                    else {
                        final InventoryUI generateInventory = new InventoryUI("Generate Arenas", true, 1);
                        final int[] array;
                        final int[] batches = array = new int[] { 10, 20, 30, 40, 50, 60, 70, 80, 90, 100, 110, 120, 130, 140, 150 };
                        for (final int batch : array) {
                            final ItemStack item = ItemUtil.createItem(Material.PAPER, ChatColor.RED.toString() + ChatColor.BOLD + batch + " ARENAS");
                            generateInventory.addItem(new InventoryUI.AbstractClickableItem(item) {
                                @Override
                                public void onClick(final InventoryClickEvent event) {
                                    final Player player = (Player)event.getWhoClicked();
                                    player.performCommand("arena generate " + arena.getName() + " " + batch);
                                    player.sendMessage(ChatColor.GREEN + "Generating " + batch + " arenas, please check console for progress.");
                                    player.closeInventory();
                                }
                            });
                        }
                        player.openInventory(generateInventory.getCurrentPage());
                    }
                }
            });
        }
        player.openInventory(inventory.getCurrentPage());
    }
    
    public void createArena(final String name) {
        this.arenas.put(name, new Arena(name));
    }
    
    public void deleteArena(final String name) {
        this.arenas.remove(name);
    }
    
    public Arena getArena(final String name) {
        return this.arenas.get(name);
    }
    
    public Arena getRandomArena(final Kit kit) {
        final List<Arena> enabledArenas = new ArrayList<Arena>();
        for (final Arena arena : this.arenas.values()) {
            if (!arena.isEnabled()) {
                continue;
            }
            if (kit.getExcludedArenas().contains(arena.getName())) {
                continue;
            }
            if (kit.getArenaWhiteList().size() > 0 && !kit.getArenaWhiteList().contains(arena.getName())) {
                continue;
            }
            enabledArenas.add(arena);
        }
        if (enabledArenas.size() == 0) {
            return null;
        }
        return enabledArenas.get(ThreadLocalRandom.current().nextInt(enabledArenas.size()));
    }
    
    public void removeArenaMatchUUID(final StandaloneArena arena) {
        this.arenaMatchUUIDs.remove(arena);
    }
    
    public UUID getArenaMatchUUID(final StandaloneArena arena) {
        return this.arenaMatchUUIDs.get(arena);
    }
    
    public void setArenaMatchUUID(final StandaloneArena arena, final UUID matchUUID) {
        this.arenaMatchUUIDs.put(arena, matchUUID);
    }
    
    public Map<String, Arena> getArenas() {
        return this.arenas;
    }
    
    public Map<StandaloneArena, UUID> getArenaMatchUUIDs() {
        return this.arenaMatchUUIDs;
    }
    
    public int getGeneratingArenaRunnables() {
        return this.generatingArenaRunnables;
    }
    
    public void setGeneratingArenaRunnables(final int generatingArenaRunnables) {
        this.generatingArenaRunnables = generatingArenaRunnables;
    }
}
