package be.fgov.kszbcss.rhq.websphere.component.sib;

import java.util.Map;

import javax.management.JMException;

import com.ibm.websphere.management.exception.ConnectorException;

import be.fgov.kszbcss.rhq.websphere.WebSphereServer;
import be.fgov.kszbcss.rhq.websphere.mbean.DynamicMBeanObjectNamePatternLocator;

public class SIBLinkTransmitterMBeanLocator extends DynamicMBeanObjectNamePatternLocator {
    private final SIBMessagingEngineComponent me;
    private final String name;

    public SIBLinkTransmitterMBeanLocator(SIBMessagingEngineComponent me, String name) {
        super("WebSphere", true);
        this.me = me;
        this.name = name;
    }

    @Override
    protected void applyKeyProperties(WebSphereServer server, Map<String,String> props) throws JMException, ConnectorException, InterruptedException {
        props.put("type", "SIBLinkTransmitter");
        props.put("SIBMessagingEngine", me.getName());
        props.put("targetUuid", me.getInfo(false).getTargetUUIDForGatewayLink(name));
    }
}
