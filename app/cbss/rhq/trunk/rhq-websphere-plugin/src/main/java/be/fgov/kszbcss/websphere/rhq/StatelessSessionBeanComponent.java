package be.fgov.kszbcss.websphere.rhq;

public class StatelessSessionBeanComponent extends EnterpriseBeanComponent {
    @Override
    protected String getMBeanType() {
        return "StatelessSessionBean";
    }
}
