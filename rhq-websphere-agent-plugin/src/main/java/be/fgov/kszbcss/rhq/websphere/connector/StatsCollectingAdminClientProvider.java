package be.fgov.kszbcss.rhq.websphere.connector;

import com.ibm.websphere.management.AdminClient;
import com.ibm.websphere.management.exception.ConnectorException;

public class StatsCollectingAdminClientProvider implements AdminClientProvider {
    private final AdminClientProvider parent;
    private final AdminClientStatsCollector collector;

    public StatsCollectingAdminClientProvider(AdminClientProvider parent, AdminClientStatsCollector collector) {
        this.parent = parent;
        this.collector = collector;
    }

    public AdminClient createAdminClient() throws ConnectorException {
        return new StatsCollectingAdminClient(parent.createAdminClient(), collector);
    }
}
