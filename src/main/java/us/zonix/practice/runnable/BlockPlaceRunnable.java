package us.zonix.practice.runnable;

import com.sk89q.worldedit.EditSession;
import java.util.Objects;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.Vector;
import com.boydti.fawe.util.EditSessionBuilder;
import com.boydti.fawe.util.TaskManager;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.Iterator;
import org.bukkit.block.Block;
import org.bukkit.Location;
import java.util.concurrent.ConcurrentMap;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitRunnable;

public abstract class BlockPlaceRunnable extends BukkitRunnable
{
    private World world;
    private final ConcurrentMap<Location, Block> blocks;
    private final int totalBlocks;
    private final Iterator<Location> iterator;
    private int blockIndex;
    private int blocksPlaced;
    private boolean completed;
    
    public BlockPlaceRunnable(final World world, final Map<Location, Block> blocks) {
        this.blockIndex = 0;
        this.blocksPlaced = 0;
        this.completed = false;
        this.world = world;
        (this.blocks = new ConcurrentHashMap<Location, Block>()).putAll((Map<?, ?>)blocks);
        this.totalBlocks = blocks.keySet().size();
        this.iterator = blocks.keySet().iterator();
    }
    
    public void run() {
        if (this.blocks.isEmpty() || !this.iterator.hasNext()) {
            this.finish();
            this.completed = true;
            this.cancel();
            return;
        }
        final EditSession editSession;
        final Iterator<Map.Entry<Object, Object>> iterator;
        Map.Entry<Object, Object> entry;
        final TaskManager imp;
        final ConcurrentMap<Location, Block> blocks;
        TaskManager.IMP.async(() -> {
            editSession = new EditSessionBuilder(this.world.getName()).fastmode(true).allowedRegionsEverywhere().autoQueue(false).limitUnlimited().build();
            this.blocks.entrySet().iterator();
            while (iterator.hasNext()) {
                entry = iterator.next();
                try {
                    editSession.setBlock(new Vector((double)entry.getKey().getBlockX(), (double)entry.getKey().getBlockY(), entry.getKey().getZ()), new BaseBlock(entry.getValue().getTypeId(), (int)entry.getValue().getData()));
                }
                catch (Exception ex) {}
            }
            editSession.flushQueue();
            imp = TaskManager.IMP;
            blocks = this.blocks;
            Objects.requireNonNull(blocks);
            imp.task(blocks::clear);
        });
    }
    
    public abstract void finish();
    
    public World getWorld() {
        return this.world;
    }
    
    public ConcurrentMap<Location, Block> getBlocks() {
        return this.blocks;
    }
    
    public int getTotalBlocks() {
        return this.totalBlocks;
    }
    
    public Iterator<Location> getIterator() {
        return this.iterator;
    }
    
    public int getBlockIndex() {
        return this.blockIndex;
    }
    
    public int getBlocksPlaced() {
        return this.blocksPlaced;
    }
    
    public boolean isCompleted() {
        return this.completed;
    }
}
