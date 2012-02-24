package be.fgov.kszbcss.rhq.websphere.component.jdbc.db2;

import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.rhq.core.domain.configuration.Configuration;
import org.rhq.core.domain.measurement.AvailabilityType;
import org.rhq.core.domain.measurement.MeasurementReport;
import org.rhq.core.domain.measurement.MeasurementScheduleRequest;
import org.rhq.core.pluginapi.inventory.InvalidPluginConfigurationException;
import org.rhq.core.pluginapi.inventory.ResourceContext;
import org.rhq.core.pluginapi.measurement.MeasurementFacet;

import be.fgov.kszbcss.rhq.websphere.component.WebSphereServiceComponent;
import be.fgov.kszbcss.rhq.websphere.component.jdbc.DataSourceComponent;
import be.fgov.kszbcss.rhq.websphere.proxy.AdminOperations;
import be.fgov.kszbcss.rhq.websphere.support.measurement.MeasurementFacetSupport;

public class DB2MonitorComponent extends WebSphereServiceComponent<DataSourceComponent> implements MeasurementFacet {
    private static final Log log = LogFactory.getLog(DB2MonitorComponent.class);
    
    private DataSourceComponent dataSourceComponent;
    private AdminOperations adminOperations;
    private String principal;
    private String credentials;
    private ConnectionContext connectionContext;
    private MeasurementFacetSupport measurementFacetSupport;
    
    protected void start() throws InvalidPluginConfigurationException, Exception {
        ResourceContext<DataSourceComponent> context = getResourceContext();
        dataSourceComponent = context.getParentResourceComponent();
        adminOperations = dataSourceComponent.getServer().getMBeanClient("WebSphere:type=AdminOperations,*").getProxy(AdminOperations.class);
        Configuration config = context.getPluginConfiguration();
        principal = config.getSimpleValue("principal", null);
        credentials = config.getSimpleValue("credentials", null);
        try {
            Class.forName(Constants.DATASOURCE_CLASS_NAME);
        } catch (ClassNotFoundException ex) {
            log.error("DB2 monitor unavailable: JDBC driver not present in the class path");
            throw ex;
        }
        measurementFacetSupport = new MeasurementFacetSupport(this);
        measurementFacetSupport.setDefaultHandler(new SnapshotMeasurementGroupHandler(this, adminOperations));
    }

    @Override
    protected AvailabilityType doGetAvailability() {
        // TODO Auto-generated method stub
        return AvailabilityType.UP;
    }

    @Override
    protected boolean isConfigured() throws Exception {
        return true;
    }

    public ConnectionContext getConnectionContext() throws Exception {
        Map<String,Object> dataSourceProperties = dataSourceComponent.getConnectionFactoryInfo().getProperties();
        if (connectionContext == null || !connectionContext.getDataSourceProperties().equals(dataSourceProperties)) {
            if (connectionContext != null) {
                connectionContext.destroy();
            }
            connectionContext = new ConnectionContext(dataSourceProperties, principal, credentials);
        }
        return connectionContext;
    }
    
    public void getValues(MeasurementReport report, Set<MeasurementScheduleRequest> requests) throws Exception {
        measurementFacetSupport.getValues(report, requests);
    }

    public void stop() {
        if (connectionContext != null) {
            connectionContext.destroy();
        }
    }
}
