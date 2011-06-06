package be.fgov.kszbcss.rhq.websphere;

public class EJBModuleComponent extends ModuleComponent {
    @Override
    protected String getMBeanType() {
        return "EJBModule";
    }
}
