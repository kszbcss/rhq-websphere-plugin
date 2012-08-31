package be.fgov.kszbcss.rhq.websphere.component.j2ee.web;

import org.rhq.core.domain.measurement.AvailabilityType;
import org.rhq.core.pluginapi.inventory.ResourceContext;
import org.rhq.core.pluginapi.measurement.MeasurementFacet;

import be.fgov.kszbcss.rhq.websphere.component.j2ee.J2EEComponent;

import com.ibm.websphere.pmi.PmiConstants;

public class ServletComponent extends J2EEComponent<WebModuleComponent> implements MeasurementFacet {
    @Override
    protected String getPMIModule() {
        return PmiConstants.WEBAPP_MODULE;
    }

    @Override
    protected String getPMISubmodule() {
        return PmiConstants.SERVLET_SUBMODULE;
    }

    @Override
    protected boolean isConfigured(boolean immediate) throws Exception {
        ResourceContext<WebModuleComponent> context = getResourceContext();
        return context.getParentResourceComponent().getServletNames(immediate).contains(context.getResourceKey());
    }

    protected AvailabilityType doGetAvailability() {
        // The MBean representing the servlet is registered lazily (or not at all if the application is
        // configured with "Create MBeans for resources" disabled). Therefore the only check we can do is
        // to see if the servlet is declared in the deployment descriptor, which is done in the
        // isConfigured method.
        return AvailabilityType.UP;
    }
}
