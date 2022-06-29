package us.zonix.practice;

import java.util.concurrent.CompletableFuture;
import java.util.Objects;
import org.bukkit.inventory.Recipe;
import net.minecraft.server.v1_8_R3.MinecraftServer;
import us.zonix.practice.util.timer.Timer;
import us.zonix.practice.util.timer.impl.EnderpearlTimer;
import us.zonix.practice.listeners.InventoryListener;
import us.zonix.practice.settings.ProfileOptionsListeners;
import us.zonix.practice.util.inventory.UIListener;
import us.zonix.practice.listeners.EnderpearlListener;
import us.zonix.practice.listeners.WorldListener;
import us.zonix.practice.listeners.MatchListener;
import us.zonix.practice.listeners.PlayerListener;
import us.zonix.practice.listeners.EntityListener;
import org.bukkit.event.Listener;
import java.util.Arrays;
import us.zonix.practice.commands.RegionLockCommand;
import us.zonix.practice.commands.management.TournamentCommand;
import us.zonix.practice.commands.LeaderboardCommand;
import us.zonix.practice.commands.VisibilityCommand;
import us.zonix.practice.commands.warp.WarpCommand;
import us.zonix.practice.commands.management.SpawnsCommand;
import us.zonix.practice.commands.InvCommand;
import us.zonix.practice.commands.event.SpectateEventCommand;
import us.zonix.practice.commands.StatsCommand;
import us.zonix.practice.commands.time.DayCommand;
import us.zonix.practice.commands.duel.SpectateCommand;
import us.zonix.practice.commands.duel.DuelCommand;
import us.zonix.practice.commands.PartyCommand;
import us.zonix.practice.commands.FlyCommand;
import us.zonix.practice.commands.time.NightCommand;
import us.zonix.practice.commands.management.KitCommand;
import us.zonix.practice.commands.management.ArenaCommand;
import us.zonix.practice.commands.SilentCommand;
import us.zonix.practice.commands.event.CoinEventCommand;
import us.zonix.practice.commands.time.SunsetCommand;
import us.zonix.practice.commands.duel.AcceptCommand;
import us.zonix.practice.commands.event.EventManagerCommand;
import us.zonix.practice.commands.event.HostCommand;
import us.zonix.practice.commands.event.StatusEventCommand;
import us.zonix.practice.commands.event.LeaveEventCommand;
import us.zonix.practice.commands.event.JoinEventCommand;
import us.zonix.practice.commands.management.ResetStatsCommand;
import us.zonix.practice.commands.toggle.SettingsCommand;
import org.bukkit.command.Command;
import net.edater.spigot.handler.PacketHandler;
import us.zonix.practice.util.StatusCache;
import us.zonix.practice.runnable.ItemDespawnRunnable;
import us.zonix.practice.runnable.ExpBarRunnable;
import us.zonix.practice.runnable.SaveDataRunnable;
import us.zonix.practice.board.BoardAdapter;
import us.zonix.practice.board.adapter.PracticeBoard;
import org.bukkit.Material;
import net.edater.spigot.handler.MovementHandler;
import us.zonix.practice.handler.CustomMovementHandler;
import net.edater.spigot.EdaterSpigot;
import java.util.Iterator;
import us.zonix.practice.mongo.PracticeMongo;
import org.bukkit.entity.Entity;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.plugin.Plugin;
import us.zonix.practice.runnable.MatchResetRunnable;
import org.bukkit.Bukkit;
import us.zonix.practice.match.Match;
import us.zonix.practice.player.PlayerData;
import java.util.List;
import us.zonix.practice.player.CPSHandler;
import us.zonix.practice.pvpclasses.PvPClassHandler;
import us.zonix.practice.managers.BotManager;
import us.zonix.practice.board.BoardManager;
import us.zonix.practice.util.timer.TimerManager;
import us.zonix.practice.managers.ChunkManager;
import us.zonix.practice.managers.TournamentManager;
import us.zonix.practice.managers.LocationManager;
import us.zonix.practice.ffa.FFAManager;
import us.zonix.practice.managers.KitManager;
import us.zonix.practice.managers.ItemManager;
import us.zonix.practice.managers.EventManager;
import us.zonix.practice.managers.QueueManager;
import us.zonix.practice.managers.PartyManager;
import us.zonix.practice.managers.MatchManager;
import us.zonix.practice.managers.ArenaManager;
import us.zonix.practice.managers.PlayerManager;
import us.zonix.practice.managers.EditorManager;
import us.zonix.practice.managers.InventoryManager;
import us.zonix.practice.file.ConfigFile;
import org.bukkit.plugin.java.JavaPlugin;

public class Practice extends JavaPlugin
{
    private static Practice instance;
    private ConfigFile mainConfig;
    private InventoryManager inventoryManager;
    private EditorManager editorManager;
    private PlayerManager playerManager;
    private ArenaManager arenaManager;
    private MatchManager matchManager;
    private PartyManager partyManager;
    private QueueManager queueManager;
    private EventManager eventManager;
    private ItemManager itemManager;
    private KitManager kitManager;
    private FFAManager ffaManager;
    private LocationManager spawnManager;
    private TournamentManager tournamentManager;
    private ChunkManager chunkManager;
    private TimerManager timerManager;
    private BoardManager boardManager;
    private BotManager botManager;
    private PvPClassHandler pvpClassHandler;
    private CPSHandler cpsHandler;
    private boolean regionLock;
    private List<String> allowedRegions;
    
    public void onDisable() {
        for (final PlayerData playerData : this.playerManager.getAllData()) {
            this.playerManager.saveData(playerData);
        }
        for (final Match match : this.matchManager.getMatches().values()) {
            Bukkit.getServer().getScheduler().runTask((Plugin)this, (BukkitRunnable)new MatchResetRunnable(match));
            match.getEntitiesToRemove().forEach(Entity::remove);
        }
        this.arenaManager.saveArenas();
        this.kitManager.saveKits();
        PracticeMongo.getInstance().getClient().close();
    }
    
    public void onEnable() {
        Practice.instance = this;
        this.mainConfig = new ConfigFile(this, "config");
        EdaterSpigot.INSTANCE.addMovementHandler((MovementHandler)new CustomMovementHandler());
        this.registerCommands();
        this.registerListeners();
        this.registerManagers();
        this.removeCrafting(Material.SNOW_BLOCK);
        this.boardManager = new BoardManager(new PracticeBoard());
        this.getServer().getScheduler().runTaskTimerAsynchronously((Plugin)this, (Runnable)this.boardManager, 1L, 1L);
        new PracticeMongo();
        this.getServer().getScheduler().runTaskTimerAsynchronously((Plugin)this, (Runnable)new SaveDataRunnable(), 6000L, 6000L);
        this.getServer().getScheduler().runTaskTimerAsynchronously((Plugin)this, (Runnable)new ExpBarRunnable(), 2L, 2L);
        this.getServer().getScheduler().runTaskTimer((Plugin)this, (Runnable)new ItemDespawnRunnable(), 20L, 20L);
        this.getServer().getScheduler().runTaskTimerAsynchronously((Plugin)this, (BukkitRunnable)new StatusCache(), 20L, 20L);
        this.regionLock = this.mainConfig.getBoolean("region-lock");
        this.allowedRegions = (List<String>)this.mainConfig.getConfiguration().getStringList("allowed-continents");
        EdaterSpigot.INSTANCE.addPacketHandler((PacketHandler)(this.cpsHandler = new CPSHandler(this)));
    }
    
    private void registerCommands() {
        Arrays.asList(new SettingsCommand(), new ResetStatsCommand(), new JoinEventCommand(), new LeaveEventCommand(), new StatusEventCommand(), new HostCommand(), new EventManagerCommand(), new AcceptCommand(), new SunsetCommand(), new CoinEventCommand(), new SilentCommand(), new ArenaCommand(), new KitCommand(), new NightCommand(), new FlyCommand(), new PartyCommand(), new PartyCommand.HCTeamsCommand(), new DuelCommand(), new SpectateCommand(), new DayCommand(), new StatsCommand(), new SpectateEventCommand(), new InvCommand(), new SpawnsCommand(), new WarpCommand(), new VisibilityCommand(), new LeaderboardCommand(), new TournamentCommand(), new RegionLockCommand()).forEach(command -> this.registerCommand(command, this.getName()));
    }
    
    private void registerListeners() {
        Arrays.asList(new EntityListener(), new PlayerListener(), new MatchListener(), new WorldListener(), new EnderpearlListener(this), new UIListener(), new ProfileOptionsListeners(), new InventoryListener()).forEach(listener -> this.getServer().getPluginManager().registerEvents(listener, (Plugin)this));
    }
    
    private void registerManagers() {
        this.spawnManager = new LocationManager();
        this.arenaManager = new ArenaManager();
        this.chunkManager = new ChunkManager();
        this.editorManager = new EditorManager();
        this.itemManager = new ItemManager();
        this.kitManager = new KitManager();
        this.matchManager = new MatchManager();
        this.partyManager = new PartyManager();
        this.playerManager = new PlayerManager();
        this.queueManager = new QueueManager();
        this.eventManager = new EventManager();
        this.tournamentManager = new TournamentManager();
        this.inventoryManager = new InventoryManager();
        this.timerManager = new TimerManager(this);
        this.pvpClassHandler = new PvPClassHandler();
        if (this.timerManager.getTimer(EnderpearlTimer.class) == null) {
            this.timerManager.registerTimer(new EnderpearlTimer());
        }
        this.ffaManager = new FFAManager(this.getSpawnManager().getSpawnLocation(), this.kitManager.getKit("NoDebuff"));
    }
    
    public void registerCommand(final Command cmd, final String fallbackPrefix) {
        MinecraftServer.getServer().server.getCommandMap().register(cmd.getName(), fallbackPrefix, cmd);
    }
    
    private void registerCommand(final Command cmd) {
        this.registerCommand(cmd, this.getName());
    }
    
    private void removeCrafting(final Material material) {
        final Iterator<Recipe> iterator = (Iterator<Recipe>)this.getServer().recipeIterator();
        while (iterator.hasNext()) {
            final Recipe recipe = iterator.next();
            if (recipe != null && recipe.getResult().getType() == material) {
                iterator.remove();
            }
        }
    }
    
    public void setRegionLock(final boolean regionLock) {
        this.mainConfig.getConfiguration().set("region-lock", (Object)regionLock);
        final ConfigFile mainConfig = this.mainConfig;
        Objects.requireNonNull(mainConfig);
        CompletableFuture.runAsync(mainConfig::save);
        this.regionLock = regionLock;
    }
    
    public void setAllowedRegions(final List<String> allowedRegions) {
        this.mainConfig.getConfiguration().set("allowed-continents", (Object)allowedRegions);
        final ConfigFile mainConfig = this.mainConfig;
        Objects.requireNonNull(mainConfig);
        CompletableFuture.runAsync(mainConfig::save);
        this.allowedRegions = allowedRegions;
    }
    
    public ConfigFile getMainConfig() {
        return this.mainConfig;
    }
    
    public InventoryManager getInventoryManager() {
        return this.inventoryManager;
    }
    
    public EditorManager getEditorManager() {
        return this.editorManager;
    }
    
    public PlayerManager getPlayerManager() {
        return this.playerManager;
    }
    
    public ArenaManager getArenaManager() {
        return this.arenaManager;
    }
    
    public MatchManager getMatchManager() {
        return this.matchManager;
    }
    
    public PartyManager getPartyManager() {
        return this.partyManager;
    }
    
    public QueueManager getQueueManager() {
        return this.queueManager;
    }
    
    public EventManager getEventManager() {
        return this.eventManager;
    }
    
    public ItemManager getItemManager() {
        return this.itemManager;
    }
    
    public KitManager getKitManager() {
        return this.kitManager;
    }
    
    public FFAManager getFfaManager() {
        return this.ffaManager;
    }
    
    public LocationManager getSpawnManager() {
        return this.spawnManager;
    }
    
    public TournamentManager getTournamentManager() {
        return this.tournamentManager;
    }
    
    public ChunkManager getChunkManager() {
        return this.chunkManager;
    }
    
    public TimerManager getTimerManager() {
        return this.timerManager;
    }
    
    public BoardManager getBoardManager() {
        return this.boardManager;
    }
    
    public BotManager getBotManager() {
        return this.botManager;
    }
    
    public PvPClassHandler getPvpClassHandler() {
        return this.pvpClassHandler;
    }
    
    public CPSHandler getCpsHandler() {
        return this.cpsHandler;
    }
    
    public boolean isRegionLock() {
        return this.regionLock;
    }
    
    public List<String> getAllowedRegions() {
        return this.allowedRegions;
    }
    
    public static Practice getInstance() {
        return Practice.instance;
    }
}
