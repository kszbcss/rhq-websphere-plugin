package be.fgov.kszbcss.rhq.websphere.component.sib;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.management.JMException;

import org.rhq.core.domain.measurement.AvailabilityType;
import org.rhq.core.domain.measurement.MeasurementReport;
import org.rhq.core.domain.measurement.MeasurementScheduleRequest;
import org.rhq.core.pluginapi.inventory.InvalidPluginConfigurationException;
import org.rhq.core.pluginapi.measurement.MeasurementFacet;

import com.ibm.websphere.management.exception.ConnectorException;

import be.fgov.kszbcss.rhq.websphere.ManagedServer;
import be.fgov.kszbcss.rhq.websphere.component.InDoubtTransactionsMeasurementHandler;
import be.fgov.kszbcss.rhq.websphere.component.WebSphereServiceComponent;
import be.fgov.kszbcss.rhq.websphere.proxy.SIBMessagingEngine;
import be.fgov.kszbcss.rhq.websphere.support.measurement.MeasurementFacetSupport;
import be.fgov.kszbcss.rhq.websphere.support.measurement.PMIMeasurementHandler;

public class TransactionsComponent extends WebSphereServiceComponent<SIBMessagingEngineComponent> implements MeasurementFacet {
    private MeasurementFacetSupport measurementFacetSupport;
    
    @Override
    protected void start() throws InvalidPluginConfigurationException, Exception {
        ManagedServer server = getServer();
        measurementFacetSupport = new MeasurementFacetSupport(this);
        measurementFacetSupport.addHandler("stats", new PMIMeasurementHandler(server.getServerMBean(),
                "SIB Service", "SIB Messaging Engines", getResourceContext().getParentResourceComponent().getName(),
                "Storage Management", "Transactions"));
        final SIBMessagingEngine sibMessagingEngine = getResourceContext().getParentResourceComponent().getSibMessagingEngine();
        measurementFacetSupport.addHandler("IndoubtTransactions", new InDoubtTransactionsMeasurementHandler() {
            @Override
            protected Set<String> getTransactionIds() throws JMException, ConnectorException {
                Set<String> ids = new HashSet<String>();
                for (Iterator<?> it = sibMessagingEngine.getPreparedTransactions().iterator(); it.hasNext(); ) {
                    ids.add((String)it.next());
                }
                return ids;
            }
        });
    }

    @Override
    protected boolean isConfigured() throws Exception {
        // The SIB messaging engine cache is always configured
        return true;
    }

    protected AvailabilityType doGetAvailability() {
        return AvailabilityType.UP;
    }

    public void getValues(MeasurementReport report, Set<MeasurementScheduleRequest> requests) throws Exception {
        if (getResourceContext().getParentResourceComponent().isActive()) {
            measurementFacetSupport.getValues(report, requests);
        }
    }

    public void stop() {
    }
}
