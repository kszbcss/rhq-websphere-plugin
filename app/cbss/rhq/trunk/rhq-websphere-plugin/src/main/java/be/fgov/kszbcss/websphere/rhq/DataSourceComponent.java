package be.fgov.kszbcss.websphere.rhq;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mc4j.ems.connection.bean.EmsBean;
import org.mc4j.ems.connection.bean.attribute.EmsAttribute;

import com.ibm.websphere.pmi.stat.MBeanStatDescriptor;

public class DataSourceComponent extends StatsEnabledMBeanResourceComponent<StatsEnabledMBeanResourceComponent<?>> {
    private static final Log log = LogFactory.getLog(DataSourceComponent.class);
    
    @Override
    protected MBeanStatDescriptor getMBeanStatDescriptor() {
        EmsBean bean = getEmsBean();
        EmsAttribute jndiNameAttribute = bean.getAttribute("jndiName");
        if (jndiNameAttribute == null) {
            log.error("jndiName attribute not found on " + bean.getBeanName());
            return null;
        }
        String jndiName = (String)jndiNameAttribute.getValue();
        EmsBean providerBean = getResourceContext().getParentResourceComponent().getEmsBean();
        return Utils.getMBeanStatDescriptor(providerBean, jndiName);
    }
}
