package be.fgov.kszbcss.rhq.websphere.component.j2ee.ejb;

import com.ibm.websphere.pmi.PmiConstants;

public class StatelessSessionBeanComponent extends EnterpriseBeanComponent {
    @Override
    protected EnterpriseBeanType getType() {
        return EnterpriseBeanType.STATELESS_SESSION;
    }

    @Override
    protected String getMBeanType() {
        return "StatelessSessionBean";
    }

    @Override
    protected String getPMISubmodule() {
        return PmiConstants.EJB_STATELESS;
    }
}
