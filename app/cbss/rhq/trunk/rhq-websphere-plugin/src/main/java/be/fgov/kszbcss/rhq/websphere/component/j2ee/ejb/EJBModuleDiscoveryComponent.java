package be.fgov.kszbcss.rhq.websphere.component.j2ee.ejb;

import be.fgov.kszbcss.rhq.websphere.component.j2ee.ModuleDiscoveryComponent;
import be.fgov.kszbcss.rhq.websphere.component.j2ee.ModuleType;

public class EJBModuleDiscoveryComponent extends ModuleDiscoveryComponent  {
    @Override
    protected ModuleType getModuleType() {
        return ModuleType.EJB;
    }

    @Override
    protected String getDescription(String moduleName) {
        return "An EJB module.";
    }
}
