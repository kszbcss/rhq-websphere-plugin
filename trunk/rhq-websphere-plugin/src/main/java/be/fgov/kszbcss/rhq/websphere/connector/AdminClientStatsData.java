package be.fgov.kszbcss.rhq.websphere.connector;

public class AdminClientStatsData {
    private final String destination;
    private long min = -1;
    private long max;
    private long total;
    private long count;
    
    public AdminClientStatsData(String destination) {
        this.destination = destination;
    }
    
    public synchronized void addData(long nanos) {
        if (min == -1 || nanos < min) {
            min = nanos;
        }
        if (nanos > max) {
            max = nanos;
        }
        total += nanos;
        count++;
    }

    public String getDestination() {
        return destination;
    }

    public double getMin() {
        return ((double)min)/1000000d;
    }

    public double getMax() {
        return ((double)max)/1000000d;
    }

    public double getTotal() {
        return ((double)total)/1000000d;
    }

    public long getCount() {
        return count;
    }
}
