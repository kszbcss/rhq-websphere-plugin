package be.fgov.kszbcss.websphere.rhq;

import org.mc4j.ems.connection.bean.EmsBeanName;
import org.rhq.plugins.jmx.JMXComponent;

public class ApplicationDiscoveryComponent extends WebSphereMBeanResourceDiscoveryComponent<JMXComponent> {
    @Override
    protected String getResourceKey(EmsBeanName objectName) {
        return objectName.getKeyProperty("name");
    }
}
