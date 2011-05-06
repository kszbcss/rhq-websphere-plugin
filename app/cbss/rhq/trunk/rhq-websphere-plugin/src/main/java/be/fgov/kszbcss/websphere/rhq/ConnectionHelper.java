package be.fgov.kszbcss.websphere.rhq;

import java.util.Properties;

import org.mc4j.ems.connection.ConnectionFactory;
import org.mc4j.ems.connection.EmsConnection;
import org.mc4j.ems.connection.settings.ConnectionSettings;
import org.mc4j.ems.connection.support.ConnectionProvider;
import org.rhq.core.domain.configuration.Configuration;

import be.fgov.kszbcss.websphere.rhq.ems.metadata.WebsphereConnectionTypeDescriptor;

public class ConnectionHelper {
    public static EmsConnection createConnection(Configuration config) {
        ConnectionSettings connectionSettings = new ConnectionSettings();
        connectionSettings.initializeConnectionType(new WebsphereConnectionTypeDescriptor());
        connectionSettings.setServerUrl(config.getSimpleValue("host", null) + ":" + config.getSimpleValue("port", null));
        connectionSettings.setPrincipal(config.getSimpleValue("principal", null));
        connectionSettings.setCredentials(config.getSimpleValue("credentials", null));

        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.discoverServerClasses(connectionSettings);

        if (connectionSettings.getAdvancedProperties() == null) {
            connectionSettings.setAdvancedProperties(new Properties());
        }

        ConnectionProvider connectionProvider = connectionFactory.getConnectionProvider(connectionSettings);
        return connectionProvider.connect();
    }
}
