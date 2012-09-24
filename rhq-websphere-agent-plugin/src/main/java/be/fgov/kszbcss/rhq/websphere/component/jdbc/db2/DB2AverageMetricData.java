package be.fgov.kszbcss.rhq.websphere.component.jdbc.db2;

public class DB2AverageMetricData {
    private final long total;
    private final long count;
    
    public DB2AverageMetricData(long total, long count) {
        this.total = total;
        this.count = count;
    }

    public long getTotal() {
        return total;
    }
    
    public long getCount() {
        return count;
    }
}
