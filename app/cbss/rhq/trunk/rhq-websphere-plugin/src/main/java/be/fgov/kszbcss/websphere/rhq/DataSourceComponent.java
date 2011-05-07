package be.fgov.kszbcss.websphere.rhq;

import javax.management.j2ee.statistics.JDBCConnectionPoolStats;
import javax.management.j2ee.statistics.JDBCStats;
import javax.management.j2ee.statistics.Stats;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mc4j.ems.connection.bean.EmsBean;
import org.rhq.plugins.jmx.MBeanResourceComponent;

public class DataSourceComponent extends StatsEnabledMBeanResourceComponent<MBeanResourceComponent> {
    private static final Log log = LogFactory.getLog(DataSourceComponent.class);
    
    @Override
    protected Stats getStats() {
        EmsBean bean = getEmsBean();
        String dataSourceName = bean.getBeanName().toString();
        EmsBean providerBean = getResourceContext().getParentResourceComponent().getEmsBean();
        JDBCStats stats = (JDBCStats)StatsHelper.getStats(providerBean);
        for (JDBCConnectionPoolStats poolStats : stats.getConnectionPools()) {
            if (poolStats.getJdbcDataSource().equals(dataSourceName)) {
                return poolStats;
            }
        }
        StringBuilder message = new StringBuilder();
        message.append("Unable to retrieve JDBCConnectionPoolStats for ");
        message.append(dataSourceName);
        message.append("; available data sources name are:");
        for (JDBCConnectionPoolStats poolStats : stats.getConnectionPools()) {
            message.append("\n  ");
            message.append(poolStats.getJdbcDataSource());
        }
        log.error(message.toString());
        return null;
    }
}
