package be.fgov.kszbcss.rhq.websphere.component.j2ee.ejb;

public class StatelessSessionBeanDiscoveryComponent extends EnterpriseBeanDiscoveryComponent {
    @Override
    protected EnterpriseBeanType getType() {
        return EnterpriseBeanType.STATELESS_SESSION;
    }

    @Override
    protected String getDescription() {
        return "A stateless session bean.";
    }
}
