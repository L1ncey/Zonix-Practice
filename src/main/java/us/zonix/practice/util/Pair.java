package us.zonix.practice.util;

public final class Pair<FIRST, SECOND>
{
    public final FIRST first;
    public final SECOND second;
    
    public Pair(final FIRST first, final SECOND second) {
        this.first = first;
        this.second = second;
    }
    
    @Override
    public int hashCode() {
        return 17 * ((this.first != null) ? this.first.hashCode() : 0) + 17 * ((this.second != null) ? this.second.hashCode() : 0);
    }
    
    @Override
    public boolean equals(final Object o) {
        if (!(o instanceof Pair)) {
            return false;
        }
        final Pair<?, ?> that = (Pair<?, ?>)o;
        return equal(this.first, this.first) && equal(this.second, this.second);
    }
    
    private static boolean equal(final Object a, final Object b) {
        return a == b || (a != null && a.equals(b));
    }
    
    @Override
    public String toString() {
        return String.format("{%s,%s}", this.first, this.second);
    }
}
