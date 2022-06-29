package us.zonix.practice.arena;

import us.zonix.practice.CustomLocation;

public class StandaloneArena
{
    private CustomLocation a;
    private CustomLocation b;
    private CustomLocation min;
    private CustomLocation max;
    
    public CustomLocation getA() {
        return this.a;
    }
    
    public CustomLocation getB() {
        return this.b;
    }
    
    public CustomLocation getMin() {
        return this.min;
    }
    
    public CustomLocation getMax() {
        return this.max;
    }
    
    public void setA(final CustomLocation a) {
        this.a = a;
    }
    
    public void setB(final CustomLocation b) {
        this.b = b;
    }
    
    public void setMin(final CustomLocation min) {
        this.min = min;
    }
    
    public void setMax(final CustomLocation max) {
        this.max = max;
    }
    
    public StandaloneArena(final CustomLocation a, final CustomLocation b, final CustomLocation min, final CustomLocation max) {
        this.a = a;
        this.b = b;
        this.min = min;
        this.max = max;
    }
    
    public StandaloneArena() {
    }
}
