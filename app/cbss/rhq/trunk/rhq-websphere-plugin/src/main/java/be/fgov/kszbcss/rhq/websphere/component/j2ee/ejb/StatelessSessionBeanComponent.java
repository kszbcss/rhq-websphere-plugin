package be.fgov.kszbcss.rhq.websphere.component.j2ee.ejb;


public class StatelessSessionBeanComponent extends EnterpriseBeanComponent {
    @Override
    protected String getMBeanType() {
        return "StatelessSessionBean";
    }
}
