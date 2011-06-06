package be.fgov.kszbcss.rhq.websphere;

public class StatelessSessionBeanComponent extends EnterpriseBeanComponent {
    @Override
    protected String getMBeanType() {
        return "StatelessSessionBean";
    }
}
