package be.fgov.kszbcss.rhq.websphere.component.j2ee.web;

import be.fgov.kszbcss.rhq.websphere.component.j2ee.ModuleDiscoveryComponent;
import be.fgov.kszbcss.rhq.websphere.component.j2ee.ModuleType;

public class WebModuleDiscoveryComponent extends ModuleDiscoveryComponent {
    @Override
    protected ModuleType getModuleType() {
        return ModuleType.WEB;
    }

    @Override
    protected String getDescription(String moduleName) {
        return "A Web module.";
    }
}
