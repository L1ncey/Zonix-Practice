package us.zonix.practice.queue;

public enum QueueType
{
    UNRANKED("Unranked"), 
    RANKED("Ranked"), 
    PREMIUM("Premium");
    
    private final String name;
    
    public boolean isRanked() {
        return this != QueueType.UNRANKED;
    }
    
    public boolean isPremium() {
        return this == QueueType.PREMIUM;
    }
    
    public boolean isBoth() {
        return this == QueueType.PREMIUM || this == QueueType.RANKED;
    }
    
    public String getName() {
        return this.name;
    }
    
    private QueueType(final String name) {
        this.name = name;
    }
}
