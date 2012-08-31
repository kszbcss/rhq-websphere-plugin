package be.fgov.kszbcss.rhq.websphere.component.j2ee.ejb;

public class MessageDrivenBeanDiscoveryComponent extends EnterpriseBeanDiscoveryComponent {
    @Override
    protected EnterpriseBeanType getType() {
        return EnterpriseBeanType.MESSAGE_DRIVEN;
    }

    @Override
    protected String getDescription() {
        return "A message driven bean.";
    }
}
