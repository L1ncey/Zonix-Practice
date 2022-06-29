package us.zonix.practice.runnable;

import org.bukkit.Location;
import java.util.Iterator;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.Vector;
import org.bukkit.block.BlockState;
import com.boydti.fawe.util.EditSessionBuilder;
import com.boydti.fawe.util.TaskManager;
import com.boydti.fawe.util.task.Task;
import com.boydti.fawe.util.task.TaskBuilder;
import us.zonix.practice.match.Match;
import us.zonix.practice.Practice;
import org.bukkit.scheduler.BukkitRunnable;

public class MatchResetRunnable extends BukkitRunnable
{
    private final Practice plugin;
    private final Match match;
    
    public void run() {
        if ((this.match.getKit().isBuild() || this.match.getKit().isSpleef()) && this.match.getPlacedBlockLocations().size() > 0) {
            new TaskBuilder().async(previousResult -> {
                final EditSession editSession = new EditSessionBuilder(this.match.getArena().getA().getWorld()).fastmode(true).allowedRegionsEverywhere().autoQueue(false).limitUnlimited().build();
                for (final Location location : this.match.getPlacedBlockLocations()) {
                    try {
                        editSession.setBlock(new Vector((double)location.getBlockX(), (double)location.getBlockY(), location.getZ()), new BaseBlock(0));
                    }
                    catch (Exception ex) {}
                }
                editSession.flushQueue();
                return null;
            }).sync((Task)new Task() {
                public Object run(final Object previousResult) {
                    MatchResetRunnable.this.match.getPlacedBlockLocations().clear();
                    MatchResetRunnable.this.match.getArena().addAvailableArena(MatchResetRunnable.this.match.getStandaloneArena());
                    MatchResetRunnable.this.plugin.getArenaManager().removeArenaMatchUUID(MatchResetRunnable.this.match.getStandaloneArena());
                    MatchResetRunnable.this.cancel();
                    return null;
                }
            }).build();
        }
        else if (this.match.getOriginalBlockChanges().size() > 0) {
            final EditSession editSession;
            final Iterator<BlockState> iterator;
            BlockState blockState;
            TaskManager.IMP.async(() -> {
                editSession = new EditSessionBuilder(this.match.getArena().getA().getWorld()).fastmode(true).allowedRegionsEverywhere().autoQueue(false).limitUnlimited().build();
                this.match.getOriginalBlockChanges().iterator();
                while (iterator.hasNext()) {
                    blockState = iterator.next();
                    try {
                        editSession.setBlock(new Vector((double)blockState.getLocation().getBlockX(), (double)blockState.getLocation().getBlockY(), blockState.getLocation().getZ()), new BaseBlock(blockState.getTypeId(), (int)blockState.getRawData()));
                    }
                    catch (Exception ex) {}
                }
                editSession.flushQueue();
                TaskManager.IMP.task(() -> {
                    if (this.match.getKit().isSpleef()) {
                        this.match.getOriginalBlockChanges().clear();
                        this.match.getArena().addAvailableArena(this.match.getStandaloneArena());
                        this.plugin.getArenaManager().removeArenaMatchUUID(this.match.getStandaloneArena());
                    }
                    this.cancel();
                });
            });
        }
        else {
            this.cancel();
        }
    }
    
    public MatchResetRunnable(final Match match) {
        this.plugin = Practice.getInstance();
        this.match = match;
    }
}
