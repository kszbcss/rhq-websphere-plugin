package be.fgov.kszbcss.rhq.websphere;

import java.util.Properties;

import javax.management.JMException;

import com.ibm.websphere.management.AdminClient;
import com.ibm.websphere.management.exception.ConnectorException;

public class ParentProcessLocator implements ProcessLocator {
    private WebSphereServer server;

    public ParentProcessLocator(WebSphereServer server) {
        this.server = server;
    }

    public void getAdminClientProperties(Properties properties) throws JMException, ConnectorException {
        server.getProcessLocator().getAdminClientProperties(properties);
        String connectorType = properties.getProperty(AdminClient.CONNECTOR_TYPE);
        for (Properties connector : server.getMBeanClient("WebSphere:type=Discovery,*").getProxy(Discovery.class).getParent().getConnectors()) {
            if (connector.getProperty(AdminClient.CONNECTOR_TYPE).equals(connectorType)) {
                properties.setProperty(AdminClient.CONNECTOR_HOST, connector.getProperty(AdminClient.CONNECTOR_HOST));
                properties.setProperty(AdminClient.CONNECTOR_PORT, connector.getProperty(AdminClient.CONNECTOR_PORT));
            }
        }
    }
}
