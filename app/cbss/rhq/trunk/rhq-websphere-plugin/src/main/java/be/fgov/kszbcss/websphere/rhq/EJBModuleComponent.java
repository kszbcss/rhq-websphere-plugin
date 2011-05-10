package be.fgov.kszbcss.websphere.rhq;

public class EJBModuleComponent extends ModuleComponent {
    @Override
    protected String getMBeanType() {
        return "EJBModule";
    }
}
