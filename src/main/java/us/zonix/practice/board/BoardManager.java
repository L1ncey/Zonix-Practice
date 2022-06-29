package us.zonix.practice.board;

import java.util.HashMap;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import java.util.Iterator;
import org.bukkit.scoreboard.DisplaySlot;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Collections;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import us.zonix.practice.Practice;
import java.util.UUID;
import java.util.Map;

public class BoardManager implements Runnable
{
    private final Map<UUID, Board> playerBoards;
    private final BoardAdapter adapter;
    
    @Override
    public void run() {
        this.adapter.preLoop();
        for (final Player player : Practice.getInstance().getServer().getOnlinePlayers()) {
            final Board board = this.playerBoards.get(player.getUniqueId());
            if (board == null) {
                continue;
            }
            try {
                final Scoreboard scoreboard = board.getScoreboard();
                final List<String> scores = this.adapter.getScoreboard(player, board);
                if (scores != null) {
                    for (int i = 0; i < scores.size(); ++i) {
                        if (scores.get(i) != null) {
                            scores.set(i, ChatColor.translateAlternateColorCodes('&', (String)scores.get(i)));
                        }
                    }
                }
                if (scores != null) {
                    Collections.reverse(scores);
                    final Objective objective = board.getObjective();
                    if (!objective.getDisplayName().equals(this.adapter.getTitle(player))) {
                        objective.setDisplayName(ChatColor.translateAlternateColorCodes('&', this.adapter.getTitle(player)));
                    }
                    if (scores.isEmpty()) {
                        final Iterator<BoardEntry> iter = board.getEntries().iterator();
                        while (iter.hasNext()) {
                            final BoardEntry boardEntry = iter.next();
                            boardEntry.remove();
                            iter.remove();
                        }
                        continue;
                    }
                    int j = 0;
                Label_0280:
                    while (j < scores.size()) {
                        final String text = scores.get(j);
                        final int position = j + 1;
                        while (true) {
                            for (final BoardEntry boardEntry2 : new LinkedList<BoardEntry>(board.getEntries())) {
                                final Score score = objective.getScore(boardEntry2.getKey());
                                if (score != null && boardEntry2.getText().equals(text) && score.getScore() == position) {
                                    ++j;
                                    continue Label_0280;
                                }
                            }
                            Iterator<BoardEntry> iter2 = board.getEntries().iterator();
                            while (iter2.hasNext()) {
                                final BoardEntry boardEntry2 = iter2.next();
                                final int entryPosition = scoreboard.getObjective(DisplaySlot.SIDEBAR).getScore(boardEntry2.getKey()).getScore();
                                if (entryPosition > scores.size()) {
                                    boardEntry2.remove();
                                    iter2.remove();
                                }
                            }
                            final int positionToSearch = position - 1;
                            final BoardEntry entry = board.getByPosition(positionToSearch);
                            if (entry == null) {
                                new BoardEntry(board, text).send(position);
                            }
                            else {
                                entry.setText(text).setup().send(position);
                            }
                            if (board.getEntries().size() > scores.size()) {
                                iter2 = board.getEntries().iterator();
                                while (iter2.hasNext()) {
                                    final BoardEntry boardEntry3 = iter2.next();
                                    if (!scores.contains(boardEntry3.getText()) || Collections.frequency(board.getBoardEntriesFormatted(), boardEntry3.getText()) > 1) {
                                        boardEntry3.remove();
                                        iter2.remove();
                                    }
                                }
                            }
                            continue;
                        }
                    }
                }
                else if (!board.getEntries().isEmpty()) {
                    board.getEntries().forEach(BoardEntry::remove);
                    board.getEntries().clear();
                }
                this.adapter.onScoreboardCreate(player, scoreboard);
                player.setScoreboard(scoreboard);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    public Map<UUID, Board> getPlayerBoards() {
        return this.playerBoards;
    }
    
    public BoardAdapter getAdapter() {
        return this.adapter;
    }
    
    public BoardManager(final BoardAdapter adapter) {
        this.playerBoards = new HashMap<UUID, Board>();
        this.adapter = adapter;
    }
}
