package be.fgov.kszbcss.rhq.websphere.component.j2ee.ejb;

import com.ibm.websphere.pmi.PmiConstants;

public class MessageDrivenBeanComponent extends EnterpriseBeanComponent {
    @Override
    protected EnterpriseBeanType getType() {
        return EnterpriseBeanType.MESSAGE_DRIVEN;
    }

    @Override
    protected String getMBeanType() {
        return "MessageDrivenBean";
    }

    @Override
    protected String getPMISubmodule() {
        return PmiConstants.EJB_MESSAGEDRIVEN;
    }
}
