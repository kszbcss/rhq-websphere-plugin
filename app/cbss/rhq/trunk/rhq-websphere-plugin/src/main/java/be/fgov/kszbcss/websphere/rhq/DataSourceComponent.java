package be.fgov.kszbcss.websphere.rhq;

import javax.management.j2ee.statistics.JDBCConnectionPoolStats;
import javax.management.j2ee.statistics.JDBCStats;
import javax.management.j2ee.statistics.Stats;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mc4j.ems.connection.bean.EmsBean;
import org.mc4j.ems.connection.bean.attribute.EmsAttribute;
import org.rhq.plugins.jmx.MBeanResourceComponent;

public class DataSourceComponent extends StatsEnabledMBeanResourceComponent<MBeanResourceComponent> {
    private static final Log log = LogFactory.getLog(DataSourceComponent.class);
    
    @Override
    protected Stats getStats() {
        EmsBean bean = getEmsBean();
        EmsAttribute jndiNameAttribute = bean.getAttribute("jndiName");
        if (jndiNameAttribute == null) {
            log.error("jndiName attribute not found on " + bean.getBeanName());
            return null;
        }
        String jndiName = (String)jndiNameAttribute.getValue();
        EmsBean providerBean = getResourceContext().getParentResourceComponent().getEmsBean();
        JDBCStats stats = (JDBCStats)StatsHelper.getStats(providerBean);
        if (log.isDebugEnabled()) {
            log.debug("Attempt to get nested statistic for " + jndiName + " on MBean " + providerBean.getBeanName());
        }
        for (JDBCConnectionPoolStats nestedStats : stats.getConnectionPools()) {
            if (nestedStats.getJdbcDataSource().equals(jndiName)) {
                return nestedStats;
            }
        }
        return null;
    }
}
