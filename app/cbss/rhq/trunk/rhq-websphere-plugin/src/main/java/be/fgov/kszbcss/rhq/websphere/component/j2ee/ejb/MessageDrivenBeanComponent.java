package be.fgov.kszbcss.rhq.websphere.component.j2ee.ejb;


public class MessageDrivenBeanComponent extends EnterpriseBeanComponent {
    @Override
    protected String getMBeanType() {
        return "MessageDrivenBean";
    }
}
