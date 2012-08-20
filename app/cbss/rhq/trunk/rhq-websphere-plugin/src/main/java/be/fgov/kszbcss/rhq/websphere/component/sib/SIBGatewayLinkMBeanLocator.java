package be.fgov.kszbcss.rhq.websphere.component.sib;

import java.util.Map;

import javax.management.JMException;

import com.ibm.websphere.management.AdminClient;
import com.ibm.websphere.management.exception.ConnectorException;

import be.fgov.kszbcss.rhq.websphere.mbean.DynamicMBeanObjectNamePatternLocator;
import be.fgov.kszbcss.rhq.websphere.mbean.ProcessInfo;

public class SIBGatewayLinkMBeanLocator extends DynamicMBeanObjectNamePatternLocator {
    private final SIBMessagingEngineComponent me;
    private final String name;

    public SIBGatewayLinkMBeanLocator(SIBMessagingEngineComponent me, String name) {
        super("WebSphere", true);
        this.me = me;
        this.name = name;
    }

    @Override
    protected void applyKeyProperties(ProcessInfo processInfo, AdminClient adminClient, Map<String,String> props) throws JMException, ConnectorException, InterruptedException {
        // Note: mbeanIdentifier is present in both WAS 6.1 and 7.0, but 6.1 doesn't have targetUuid and SIBMessagingEngine
        props.put("mbeanIdentifier", me.getInfo(false).getGatewayLinkId(name).replace('|', '/'));
    }
}
