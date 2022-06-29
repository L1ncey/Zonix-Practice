package us.zonix.practice.util;

public class KFactor
{
    private final int startIndex;
    private final int endIndex;
    private final double value;
    
    public int getStartIndex() {
        return this.startIndex;
    }
    
    public int getEndIndex() {
        return this.endIndex;
    }
    
    public double getValue() {
        return this.value;
    }
    
    public KFactor(final int startIndex, final int endIndex, final double value) {
        this.startIndex = startIndex;
        this.endIndex = endIndex;
        this.value = value;
    }
}
