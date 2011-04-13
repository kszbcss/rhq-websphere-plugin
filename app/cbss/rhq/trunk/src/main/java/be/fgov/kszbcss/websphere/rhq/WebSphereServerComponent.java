package be.fgov.kszbcss.websphere.rhq;

import java.util.Properties;

import org.mc4j.ems.connection.ConnectionFactory;
import org.mc4j.ems.connection.EmsConnection;
import org.mc4j.ems.connection.settings.ConnectionSettings;
import org.mc4j.ems.connection.support.ConnectionProvider;
import org.mc4j.ems.connection.support.metadata.ConnectionTypeDescriptor;
import org.mc4j.ems.connection.support.metadata.InternalVMTypeDescriptor;
import org.rhq.core.domain.configuration.Configuration;
import org.rhq.core.domain.measurement.AvailabilityType;
import org.rhq.core.pluginapi.inventory.InvalidPluginConfigurationException;
import org.rhq.core.pluginapi.inventory.ResourceContext;
import org.rhq.plugins.jbossas.helper.MainDeployer;
import org.rhq.plugins.jmx.JMXComponent;
import org.rhq.plugins.jmx.JMXDiscoveryComponent;

public class WebSphereServerComponent implements JMXComponent {
    private EmsConnection connection;

    public synchronized EmsConnection getEmsConnection() {
        if (this.connection == null) {
            try {
                Configuration pluginConfig = resourceContext.getPluginConfiguration();
                String wasHomeDir = pluginConfig.getSimpleValue(Constants.WAS_HOME_DIR_PROP, null);

                ConnectionSettings connectionSettings = new ConnectionSettings();

                String connectionTypeDescriptorClass = pluginConfig.getSimple(JMXDiscoveryComponent.CONNECTION_TYPE)
                    .getStringValue();
                connectionSettings.initializeConnectionType((ConnectionTypeDescriptor) Class.forName(
                    connectionTypeDescriptorClass).newInstance());
                connectionSettings.setServerUrl(pluginConfig.getSimpleValue(Constants.HOST_PROP, null) + ":" + pluginConfig.getSimpleValue(Constants.PORT_PROP, null));
                connectionSettings.setPrincipal(pluginConfig.getSimpleValue(PRINCIPAL_CONFIG_PROP, null));
                connectionSettings.setCredentials(pluginConfig.getSimpleValue(CREDENTIALS_CONFIG_PROP, null));
                connectionSettings.setLibraryURI(wasHomeDir);

                ConnectionFactory connectionFactory = new ConnectionFactory();
                connectionFactory.discoverServerClasses(connectionSettings);

                if (connectionSettings.getAdvancedProperties() == null) {
                    connectionSettings.setAdvancedProperties(new Properties());
                }

                connectionSettings.getAdvancedProperties().setProperty(JNP_DISABLE_DISCOVERY_JNP_INIT_PROP, "true");

                // Make sure the timeout always happens, even if the JBoss server is hung.
                connectionSettings.getAdvancedProperties().setProperty("jnp.timeout", String.valueOf(JNP_TIMEOUT));
                connectionSettings.getAdvancedProperties().setProperty("jnp.sotimeout", String.valueOf(JNP_SO_TIMEOUT));

                // Tell EMS to make copies of jar files so that the ems classloader doesn't lock
                // application files (making us unable to update them)  Bug: JBNADM-670
                // TODO GH: turn this off in the embedded case
                connectionSettings.getControlProperties().setProperty(ConnectionFactory.COPY_JARS_TO_TEMP,
                    String.valueOf(Boolean.TRUE));

                // But tell it to put them in a place that we clean up when shutting down the agent
                connectionSettings.getControlProperties().setProperty(ConnectionFactory.JAR_TEMP_DIR,
                    resourceContext.getTemporaryDirectory().getAbsolutePath());

                connectionSettings.getAdvancedProperties().setProperty(InternalVMTypeDescriptor.DEFAULT_DOMAIN_SEARCH,
                    "jboss");

                log.info("Loading JBoss connection [" + connectionSettings.getServerUrl() + "] with install path ["
                    + connectionSettings.getLibraryURI() + "]...");

                ConnectionProvider connectionProvider = connectionFactory.getConnectionProvider(connectionSettings);
                this.connection = connectionProvider.connect();

                this.connection.loadSynchronous(false); // this loads all the MBeans

                this.consecutiveConnectionErrors = 0;

                try {
                    this.mainDeployer = new MainDeployer(this.connection);
                } catch (Exception e) {
                    log.error("Unable to access MainDeployer MBean required for creation and deletion of managed "
                        + "resources - this should never happen. Cause: " + e);
                }

                if (log.isDebugEnabled())
                    log.debug("Successfully made connection to the AS instance for resource ["
                        + this.resourceContext.getResourceKey() + "]");
            } catch (Exception e) {

                // The connection will be established even in the case that the principal cannot be authenticated,
                // but the connection will not work. That failure seems to come from the call to loadSynchronous after
                // the connection is established. If we get to this point that an exception was thrown, close any
                // connection that was made and null it out so we can try to establish it again.
                if (connection != null) {
                    if (log.isDebugEnabled())
                        log.debug("Connection created but an exception was thrown. Closing the connection.", e);
                    connection.close();
                    connection = null;
                }

                // Since the connection is attempted each time it's used, failure to connect could result in log
                // file spamming. Log it once for every 10 consecutive times it's encountered.
                if (consecutiveConnectionErrors % 10 == 0) {
                    log.warn("Could not establish connection to the JBoss AS instance ["
                        + (consecutiveConnectionErrors + 1) + "] times for resource ["
                        + resourceContext.getResourceKey() + "]", e);
                }

                if (log.isDebugEnabled())
                    log.debug("Could not connect to the JBoss AS instance for resource ["
                        + resourceContext.getResourceKey() + "]", e);

                consecutiveConnectionErrors++;

                throw e;
            }
        }

        return connection;
    }

    public void start(ResourceContext context)
            throws InvalidPluginConfigurationException, Exception {
        // TODO Auto-generated method stub
        
    }

    public void stop() {
        // TODO Auto-generated method stub
        
    }

    public AvailabilityType getAvailability() {
        // TODO Auto-generated method stub
        return null;
    }

}
