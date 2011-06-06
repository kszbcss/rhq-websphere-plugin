package be.fgov.kszbcss.rhq.websphere;

public class MessageDrivenBeanComponent extends EnterpriseBeanComponent {
    @Override
    protected String getMBeanType() {
        return "MessageDrivenBean";
    }
}
