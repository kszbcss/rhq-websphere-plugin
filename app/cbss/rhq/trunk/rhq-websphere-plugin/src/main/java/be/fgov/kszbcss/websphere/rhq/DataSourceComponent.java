package be.fgov.kszbcss.websphere.rhq;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mc4j.ems.connection.bean.EmsBean;
import org.mc4j.ems.connection.bean.attribute.EmsAttribute;

import com.ibm.websphere.pmi.stat.WSStats;

public class DataSourceComponent extends StatsEnabledMBeanResourceComponent<StatsEnabledMBeanResourceComponent<?>> {
    private static final Log log = LogFactory.getLog(DataSourceComponent.class);
    
    @Override
    protected WSStats getStats() {
        EmsBean bean = getEmsBean();
        EmsAttribute jndiNameAttribute = bean.getAttribute("jndiName");
        if (jndiNameAttribute == null) {
            log.error("jndiName attribute not found on " + bean.getBeanName());
            return null;
        }
        String jndiName = (String)jndiNameAttribute.getValue();
        EmsBean providerBean = getResourceContext().getParentResourceComponent().getEmsBean();
        WSStats stats = getServer().getWSStats(providerBean);
        WSStats subStats = stats.getStats(jndiName);
        if (subStats == null) {
            StringBuilder message = new StringBuilder();
            message.append("Unable to retrieve statistics for ");
            message.append(jndiName);
            message.append("; available data sources are:");
            for (WSStats s : stats.getSubStats()) {
                message.append("\n  ");
                message.append(s.getName());
            }
            log.error(message.toString());
        }
        return subStats;
    }
}
