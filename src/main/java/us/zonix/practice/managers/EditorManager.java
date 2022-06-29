package us.zonix.practice.managers;

import org.bukkit.ChatColor;
import us.zonix.practice.util.PlayerUtil;
import us.zonix.practice.kit.Kit;
import org.bukkit.entity.Player;
import java.util.HashMap;
import us.zonix.practice.kit.PlayerKit;
import java.util.UUID;
import java.util.Map;
import us.zonix.practice.Practice;

public class EditorManager
{
    private final Practice plugin;
    private final Map<UUID, String> editing;
    private final Map<UUID, PlayerKit> renaming;
    
    public EditorManager() {
        this.plugin = Practice.getInstance();
        this.editing = new HashMap<UUID, String>();
        this.renaming = new HashMap<UUID, PlayerKit>();
    }
    
    public void addEditor(final Player player, final Kit kit) {
        this.editing.put(player.getUniqueId(), kit.getName());
        this.plugin.getInventoryManager().addEditingKitInventory(player, kit);
        PlayerUtil.clearPlayer(player);
        player.teleport(this.plugin.getSpawnManager().getEditorLocation().toBukkitLocation());
        player.getInventory().setContents(kit.getContents());
        player.sendMessage(ChatColor.GREEN + "You are editing kit " + ChatColor.YELLOW + kit.getName() + ChatColor.GREEN + ".");
    }
    
    public void removeEditor(final UUID editor) {
        this.renaming.remove(editor);
        this.editing.remove(editor);
        this.plugin.getInventoryManager().removeEditingKitInventory(editor);
    }
    
    public String getEditingKit(final UUID editor) {
        return this.editing.get(editor);
    }
    
    public void addRenamingKit(final UUID uuid, final PlayerKit playerKit) {
        this.renaming.put(uuid, playerKit);
    }
    
    public void removeRenamingKit(final UUID uuid) {
        this.renaming.remove(uuid);
    }
    
    public PlayerKit getRenamingKit(final UUID uuid) {
        return this.renaming.get(uuid);
    }
}
