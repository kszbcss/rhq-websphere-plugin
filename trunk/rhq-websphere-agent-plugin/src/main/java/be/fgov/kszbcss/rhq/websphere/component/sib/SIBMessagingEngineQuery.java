package be.fgov.kszbcss.rhq.websphere.component.sib;

import java.util.ArrayList;
import java.util.List;

import javax.management.JMException;

import be.fgov.kszbcss.rhq.websphere.config.ConfigObject;
import be.fgov.kszbcss.rhq.websphere.config.ConfigQuery;
import be.fgov.kszbcss.rhq.websphere.config.CellConfiguration;

import com.ibm.websphere.management.exception.ConnectorException;

public class SIBMessagingEngineQuery implements ConfigQuery<SIBMessagingEngineInfo[]> {
    private static final long serialVersionUID = 4836748290502191854L;
    
    private final String node;
    private final String server;
    
    public SIBMessagingEngineQuery(String node, String server) {
        this.node = node;
        this.server = server;
    }

    public SIBMessagingEngineInfo[] execute(CellConfiguration config) throws JMException, ConnectorException, InterruptedException {
        List<SIBMessagingEngineInfo> result = new ArrayList<SIBMessagingEngineInfo>();
        for (ConfigObject me : config.allScopes(node, server).path("SIBMessagingEngine").resolve()) {
            List<SIBLocalizationPointInfo> localizationPoints = new ArrayList<SIBLocalizationPointInfo>();
            for (ConfigObject localizationPoint : me.getChildren("localizationPoints")) {
                String identifier = (String)localizationPoint.getAttribute("identifier");
                localizationPoints.add(new SIBLocalizationPointInfo(
                        localizationPoint.getType().equals("SIBQueueLocalizationPoint") ? SIBLocalizationPointType.QUEUE : SIBLocalizationPointType.TOPIC,
                        identifier.substring(0, identifier.indexOf('@'))));
            }
            List<SIBGatewayLinkInfo> gatewayLinks = new ArrayList<SIBGatewayLinkInfo>();
            for (ConfigObject gatewayLink : me.getChildren("gatewayLink")) {
                gatewayLinks.add(new SIBGatewayLinkInfo(gatewayLink.getId(), (String)gatewayLink.getAttribute("name"), (String)gatewayLink.getAttribute("targetUuid")));
            }
            result.add(new SIBMessagingEngineInfo((String)me.getAttribute("name"), (String)me.getAttribute("busName"),
                    localizationPoints.toArray(new SIBLocalizationPointInfo[localizationPoints.size()]),
                    gatewayLinks.toArray(new SIBGatewayLinkInfo[gatewayLinks.size()])));
        }
        return result.toArray(new SIBMessagingEngineInfo[result.size()]);
    }

    @Override
    public int hashCode() {
        return 31*node.hashCode() + server.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof SIBMessagingEngineQuery) {
            SIBMessagingEngineQuery other = (SIBMessagingEngineQuery)obj;
            return other.node.equals(node) && other.server.equals(server);
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" + node + "," + server + ")";
    }
}
