package us.zonix.practice.event.match;

import us.zonix.practice.match.Match;

public class MatchStartEvent extends MatchEvent
{
    public MatchStartEvent(final Match match) {
        super(match);
    }
}
