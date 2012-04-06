package be.fgov.kszbcss.rhq.websphere.component.xm;

import be.fgov.kszbcss.rhq.websphere.component.j2ee.ApplicationComponent;

public class ApplicationClassLoaderStatsComponent extends ModuleClassLoaderStatsComponent<ApplicationComponent> {
    @Override
    protected String getModuleName() {
        return getResourceContext().getParentResourceComponent().getApplicationName();
    }
}
