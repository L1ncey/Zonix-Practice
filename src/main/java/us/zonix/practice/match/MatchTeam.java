package us.zonix.practice.match;

import java.util.List;
import java.util.UUID;
import us.zonix.practice.team.KillableTeam;

public class MatchTeam extends KillableTeam
{
    private final int teamID;
    int matchWins;
    
    public MatchTeam(final UUID leader, final List<UUID> players, final int teamID) {
        super(leader, players);
        this.matchWins = 0;
        this.teamID = teamID;
    }
    
    public int getTeamID() {
        return this.teamID;
    }
    
    public void setMatchWins(final int matchWins) {
        this.matchWins = matchWins;
    }
    
    public int getMatchWins() {
        return this.matchWins;
    }
}
