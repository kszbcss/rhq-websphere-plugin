package be.fgov.kszbcss.rhq.websphere;

import java.util.Properties;

import javax.management.JMException;

import be.fgov.kszbcss.rhq.websphere.proxy.Discovery;

import com.ibm.websphere.management.AdminClient;
import com.ibm.websphere.management.exception.ConnectorException;
import com.ibm.ws.management.discovery.ServerInfo;

public class ParentProcessLocator implements ProcessLocator {
    private WebSphereServer server;

    public ParentProcessLocator(WebSphereServer server) {
        this.server = server;
    }

    public void getAdminClientProperties(Properties properties) throws JMException, ConnectorException {
        server.getProcessLocator().getAdminClientProperties(properties);
        String connectorType = properties.getProperty(AdminClient.CONNECTOR_TYPE);
        ServerInfo parent = server.getMBeanClient("WebSphere:type=Discovery,*").getProxy(Discovery.class).getParent();
        if (parent == null) {
            throw new JMException("Parent process not available");
        }
        for (Properties connector : parent.getConnectors()) {
            if (connector.getProperty(AdminClient.CONNECTOR_TYPE).equals(connectorType)) {
                properties.setProperty(AdminClient.CONNECTOR_HOST, connector.getProperty(AdminClient.CONNECTOR_HOST));
                properties.setProperty(AdminClient.CONNECTOR_PORT, connector.getProperty(AdminClient.CONNECTOR_PORT));
            }
        }
    }
}
