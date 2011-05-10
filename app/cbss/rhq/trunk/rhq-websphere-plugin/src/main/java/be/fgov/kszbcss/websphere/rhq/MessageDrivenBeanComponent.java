package be.fgov.kszbcss.websphere.rhq;

public class MessageDrivenBeanComponent extends EnterpriseBeanComponent {
    @Override
    protected String getMBeanType() {
        return "MessageDrivenBean";
    }
}
