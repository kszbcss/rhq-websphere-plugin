package be.fgov.kszbcss.rhq.websphere.component.xm4was;

import be.fgov.kszbcss.rhq.websphere.component.j2ee.web.WebModuleComponent;

public class WebModuleClassLoaderStatsComponent extends ModuleClassLoaderStatsComponent<WebModuleComponent> {
    @Override
    protected String getModuleName() {
        WebModuleComponent parent = getResourceContext().getParentResourceComponent();
        return parent.getApplicationName() + "#" + parent.getModuleName();
    }
}
