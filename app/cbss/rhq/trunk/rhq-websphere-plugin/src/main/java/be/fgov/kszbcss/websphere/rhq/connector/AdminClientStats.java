package be.fgov.kszbcss.websphere.rhq.connector;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class AdminClientStats {
    private final Map<String,AdminClientStatsData> map = new HashMap<String,AdminClientStatsData>();
    private final Date beginTime;
    private Date endTime;
    
    public AdminClientStats() {
        beginTime = new Date();
    }
    
    public void addData(String destination, long nanos) {
        AdminClientStatsData data;
        synchronized (map) {
            data = map.get(destination);
            if (data == null) {
                data = new AdminClientStatsData(destination);
                map.put(destination, data);
            }
        }
        data.addData(nanos);
    }
    
    public Date getBeginTime() {
        return beginTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public Collection<AdminClientStatsData> getData() {
        return map.values();
    }
    
    public void end() {
        endTime = new Date();
    }
}
