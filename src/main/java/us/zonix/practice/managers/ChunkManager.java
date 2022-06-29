package us.zonix.practice.managers;

import java.util.Iterator;
import org.bukkit.Chunk;
import us.zonix.practice.CustomLocation;
import us.zonix.practice.arena.StandaloneArena;
import us.zonix.practice.arena.Arena;
import org.bukkit.plugin.Plugin;
import us.zonix.practice.Practice;

public class ChunkManager
{
    private final Practice plugin;
    private boolean chunksLoaded;
    
    public ChunkManager() {
        this.plugin = Practice.getInstance();
        this.plugin.getServer().getScheduler().runTaskLater((Plugin)this.plugin, this::loadChunks, 1L);
    }
    
    private void loadChunks() {
        this.plugin.getLogger().info("Started loading all the chunks...");
        final CustomLocation spawnMin = this.plugin.getSpawnManager().getSpawnMin();
        final CustomLocation spawnMax = this.plugin.getSpawnManager().getSpawnMax();
        if (spawnMin != null && spawnMax != null) {
            int spawnMinX = spawnMin.toBukkitLocation().getBlockX() >> 4;
            int spawnMinZ = spawnMin.toBukkitLocation().getBlockZ() >> 4;
            int spawnMaxX = spawnMax.toBukkitLocation().getBlockX() >> 4;
            int spawnMaxZ = spawnMax.toBukkitLocation().getBlockZ() >> 4;
            if (spawnMinX > spawnMaxX) {
                final int lastSpawnMinX = spawnMinX;
                spawnMinX = spawnMaxX;
                spawnMaxX = lastSpawnMinX;
            }
            if (spawnMinZ > spawnMaxZ) {
                final int lastSpawnMinZ = spawnMinZ;
                spawnMinZ = spawnMaxZ;
                spawnMaxZ = lastSpawnMinZ;
            }
            for (int x = spawnMinX; x <= spawnMaxX; ++x) {
                for (int z = spawnMinZ; z <= spawnMaxZ; ++z) {
                    final Chunk chunk = spawnMin.toBukkitWorld().getChunkAt(x, z);
                    if (!chunk.isLoaded()) {
                        chunk.load();
                    }
                }
            }
        }
        final CustomLocation editorMin = this.plugin.getSpawnManager().getEditorMin();
        final CustomLocation editorMax = this.plugin.getSpawnManager().getEditorMax();
        if (editorMin != null && editorMax != null) {
            int editorMinX = editorMin.toBukkitLocation().getBlockX() >> 4;
            int editorMinZ = editorMin.toBukkitLocation().getBlockZ() >> 4;
            int editorMaxX = editorMax.toBukkitLocation().getBlockX() >> 4;
            int editorMaxZ = editorMax.toBukkitLocation().getBlockZ() >> 4;
            if (editorMinX > editorMaxX) {
                final int lastEditorMinX = editorMinX;
                editorMinX = editorMaxX;
                editorMaxX = lastEditorMinX;
            }
            if (editorMinZ > editorMaxZ) {
                final int lastEditorMinZ = editorMinZ;
                editorMinZ = editorMaxZ;
                editorMaxZ = lastEditorMinZ;
            }
            for (int x2 = editorMinX; x2 <= editorMaxX; ++x2) {
                for (int z2 = editorMinZ; z2 <= editorMaxZ; ++z2) {
                    final Chunk chunk2 = editorMin.toBukkitWorld().getChunkAt(x2, z2);
                    if (!chunk2.isLoaded()) {
                        chunk2.load();
                    }
                }
            }
        }
        final CustomLocation ffaMin = this.plugin.getSpawnManager().getFfaMin();
        final CustomLocation ffaMax = this.plugin.getSpawnManager().getFfaMax();
        if (ffaMin != null && ffaMax != null) {
            int ffaMinX = ffaMin.toBukkitLocation().getBlockX() >> 4;
            int ffaMinZ = ffaMin.toBukkitLocation().getBlockZ() >> 4;
            int ffaMaxX = ffaMax.toBukkitLocation().getBlockX() >> 4;
            int ffaMaxZ = ffaMax.toBukkitLocation().getBlockZ() >> 4;
            if (ffaMinX > ffaMaxX) {
                final int lastFfaMinX = ffaMinX;
                ffaMinX = ffaMaxX;
                ffaMaxX = lastFfaMinX;
            }
            if (ffaMinZ > ffaMaxZ) {
                final int lastFfaMinZ = ffaMinZ;
                ffaMinZ = ffaMaxZ;
                ffaMaxZ = lastFfaMinZ;
            }
            for (int x3 = ffaMinX; x3 <= ffaMaxX; ++x3) {
                for (int z3 = ffaMinZ; z3 <= ffaMaxZ; ++z3) {
                    final Chunk chunk3 = ffaMin.toBukkitWorld().getChunkAt(x3, z3);
                    if (!chunk3.isLoaded()) {
                        chunk3.load();
                    }
                }
            }
        }
        final CustomLocation sumoMin = this.plugin.getSpawnManager().getSumoMin();
        final CustomLocation sumoMax = this.plugin.getSpawnManager().getSumoMax();
        if (sumoMin != null && sumoMax != null) {
            int sumoMinX = sumoMin.toBukkitLocation().getBlockX() >> 4;
            int sumoMinZ = sumoMin.toBukkitLocation().getBlockZ() >> 4;
            int sumoMaxX = sumoMax.toBukkitLocation().getBlockX() >> 4;
            int sumoMaxZ = sumoMax.toBukkitLocation().getBlockZ() >> 4;
            if (sumoMinX > sumoMaxX) {
                final int lastSumoMinX = sumoMinX;
                sumoMinX = sumoMaxX;
                sumoMaxX = lastSumoMinX;
            }
            if (sumoMinZ > sumoMaxZ) {
                final int lastSumoMaxZ = sumoMinZ;
                sumoMinZ = sumoMaxZ;
                sumoMaxZ = lastSumoMaxZ;
            }
            for (int x4 = sumoMinX; x4 <= sumoMaxX; ++x4) {
                for (int z4 = sumoMinZ; z4 <= sumoMaxZ; ++z4) {
                    final Chunk chunk4 = sumoMin.toBukkitWorld().getChunkAt(x4, z4);
                    if (!chunk4.isLoaded()) {
                        chunk4.load();
                    }
                }
            }
        }
        final CustomLocation oitcMin = this.plugin.getSpawnManager().getOitcMin();
        final CustomLocation oitcMax = this.plugin.getSpawnManager().getOitcMax();
        if (oitcMin != null && oitcMax != null) {
            int oitcMinX = oitcMin.toBukkitLocation().getBlockX() >> 4;
            int oitcMinZ = oitcMin.toBukkitLocation().getBlockZ() >> 4;
            int oitcMaxX = oitcMax.toBukkitLocation().getBlockX() >> 4;
            int oitcMaxZ = oitcMax.toBukkitLocation().getBlockZ() >> 4;
            if (oitcMinX > oitcMaxX) {
                final int lastOitcMinX = oitcMinX;
                oitcMinX = oitcMaxX;
                oitcMaxX = lastOitcMinX;
            }
            if (oitcMinZ > oitcMaxZ) {
                final int lastOitcMaxZ = oitcMinZ;
                oitcMinZ = oitcMaxZ;
                oitcMaxZ = lastOitcMaxZ;
            }
            for (int x5 = oitcMinX; x5 <= oitcMaxX; ++x5) {
                for (int z5 = oitcMinZ; z5 <= oitcMaxZ; ++z5) {
                    final Chunk chunk5 = oitcMin.toBukkitWorld().getChunkAt(x5, z5);
                    if (!chunk5.isLoaded()) {
                        chunk5.load();
                    }
                }
            }
        }
        final CustomLocation parkourMin = this.plugin.getSpawnManager().getParkourMin();
        final CustomLocation parkourMax = this.plugin.getSpawnManager().getParkourMax();
        if (parkourMin != null && parkourMax != null) {
            int parkourMinX = parkourMin.toBukkitLocation().getBlockX() >> 4;
            int parkourMinZ = parkourMin.toBukkitLocation().getBlockZ() >> 4;
            int parkourMaxX = parkourMax.toBukkitLocation().getBlockX() >> 4;
            int parkourMaxZ = parkourMax.toBukkitLocation().getBlockZ() >> 4;
            if (parkourMinX > parkourMaxX) {
                final int lastParkourMinX = parkourMinX;
                parkourMinX = parkourMaxX;
                parkourMaxX = lastParkourMinX;
            }
            if (parkourMinZ > parkourMaxZ) {
                final int lastParkourMaxZ = parkourMinZ;
                parkourMinZ = parkourMaxZ;
                parkourMaxZ = lastParkourMaxZ;
            }
            for (int x6 = parkourMinX; x6 <= parkourMaxX; ++x6) {
                for (int z6 = parkourMinZ; z6 <= parkourMaxZ; ++z6) {
                    final Chunk chunk6 = parkourMin.toBukkitWorld().getChunkAt(x6, z6);
                    if (!chunk6.isLoaded()) {
                        chunk6.load();
                    }
                }
            }
        }
        for (final Arena arena : this.plugin.getArenaManager().getArenas().values()) {
            if (!arena.isEnabled()) {
                continue;
            }
            int arenaMinX = arena.getMin().toBukkitLocation().getBlockX() >> 4;
            int arenaMinZ = arena.getMin().toBukkitLocation().getBlockZ() >> 4;
            int arenaMaxX = arena.getMax().toBukkitLocation().getBlockX() >> 4;
            int arenaMaxZ = arena.getMax().toBukkitLocation().getBlockZ() >> 4;
            if (arenaMinX > arenaMaxX) {
                final int lastArenaMinX = arenaMinX;
                arenaMinX = arenaMaxX;
                arenaMaxX = lastArenaMinX;
            }
            if (arenaMinZ > arenaMaxZ) {
                final int lastArenaMinZ = arenaMinZ;
                arenaMinZ = arenaMaxZ;
                arenaMaxZ = lastArenaMinZ;
            }
            for (int x7 = arenaMinX; x7 <= arenaMaxX; ++x7) {
                for (int z7 = arenaMinZ; z7 <= arenaMaxZ; ++z7) {
                    final Chunk chunk7 = arena.getMin().toBukkitWorld().getChunkAt(x7, z7);
                    if (!chunk7.isLoaded()) {
                        chunk7.load();
                    }
                }
            }
            for (final StandaloneArena saArena : arena.getStandaloneArenas()) {
                arenaMinX = saArena.getMin().toBukkitLocation().getBlockX() >> 4;
                arenaMinZ = saArena.getMin().toBukkitLocation().getBlockZ() >> 4;
                arenaMaxX = saArena.getMax().toBukkitLocation().getBlockX() >> 4;
                arenaMaxZ = saArena.getMax().toBukkitLocation().getBlockZ() >> 4;
                if (arenaMinX > arenaMaxX) {
                    final int lastArenaMinX2 = arenaMinX;
                    arenaMinX = arenaMaxX;
                    arenaMaxX = lastArenaMinX2;
                }
                if (arenaMinZ > arenaMaxZ) {
                    final int lastArenaMinZ2 = arenaMinZ;
                    arenaMinZ = arenaMaxZ;
                    arenaMaxZ = lastArenaMinZ2;
                }
                for (int x8 = arenaMinX; x8 <= arenaMaxX; ++x8) {
                    for (int z8 = arenaMinZ; z8 <= arenaMaxZ; ++z8) {
                        final Chunk chunk8 = saArena.getMin().toBukkitWorld().getChunkAt(x8, z8);
                        if (!chunk8.isLoaded()) {
                            chunk8.load();
                        }
                    }
                }
            }
        }
        this.plugin.getLogger().info("Finished loading all the chunks!");
        this.chunksLoaded = true;
    }
    
    public boolean isChunksLoaded() {
        return this.chunksLoaded;
    }
}
