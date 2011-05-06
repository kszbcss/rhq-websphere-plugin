package be.fgov.kszbcss.websphere.rhq;

import java.util.List;

import org.mc4j.ems.connection.EmsConnection;
import org.mc4j.ems.connection.bean.EmsBean;
import org.rhq.core.domain.measurement.AvailabilityType;
import org.rhq.core.pluginapi.inventory.InvalidPluginConfigurationException;
import org.rhq.core.pluginapi.inventory.ResourceContext;
import org.rhq.plugins.jmx.JMXComponent;

public class WebSphereServerComponent implements JMXComponent {
    private ResourceContext resourceContext;
    private EmsConnection connection;
    
    public void start(ResourceContext context) throws InvalidPluginConfigurationException, Exception {
        this.resourceContext = context;
    }

    public synchronized EmsConnection getEmsConnection() {
        if (connection == null) {
            connection = ConnectionHelper.createConnection(resourceContext.getPluginConfiguration());
        }
        return connection;
    }
    
    public AvailabilityType getAvailability() {
        List<EmsBean> serverBeans = getEmsConnection().queryBeans("WebSphere:type=Server,*");
        if (serverBeans.size() != 1) {
            return AvailabilityType.DOWN;
        } else {
            if (serverBeans.get(0).getOperation("getState").invoke().equals("STARTED")) {
                return AvailabilityType.UP;
            } else {
                return AvailabilityType.DOWN;
            }
        }
    }

    public void stop() {
    }
}
