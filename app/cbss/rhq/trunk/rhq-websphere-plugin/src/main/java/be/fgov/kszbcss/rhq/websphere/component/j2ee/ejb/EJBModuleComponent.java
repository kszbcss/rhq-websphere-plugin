package be.fgov.kszbcss.rhq.websphere.component.j2ee.ejb;

import be.fgov.kszbcss.rhq.websphere.component.j2ee.ModuleComponent;

public class EJBModuleComponent extends ModuleComponent {
    @Override
    protected String getMBeanType() {
        return "EJBModule";
    }
}
