package be.fgov.kszbcss.rhq.websphere.component.j2ee.ejb;

import java.util.HashSet;
import java.util.Set;

import org.w3c.dom.Element;

import be.fgov.kszbcss.rhq.websphere.Utils;
import be.fgov.kszbcss.rhq.websphere.component.j2ee.ModuleComponent;

public class EJBModuleComponent extends ModuleComponent {
    @Override
    protected String getMBeanType() {
        return "EJBModule";
    }
    
    public Set<String> getBeanNames(EnterpriseBeanType type, boolean immediate) throws InterruptedException {
        Set<String> names = new HashSet<String>();
        Element beans = Utils.getFirstElement(getModuleInfo(immediate).getDeploymentDescriptor().getDocumentElement(), "enterprise-beans");
        if (beans != null) {
            if (type == EnterpriseBeanType.MESSAGE_DRIVEN) {
                for (Element bean : Utils.getElements(beans, "message-driven")) {
                    names.add(Utils.getFirstElement(bean, "ejb-name").getTextContent());
                }
            } else {
                String typeString = type == EnterpriseBeanType.STATEFUL_SESSION ? "Stateful" : "Stateless";
                for (Element bean : Utils.getElements(beans, "session")) {
                    Element sessionType = Utils.getFirstElement(bean, "session-type");
                    if (sessionType == null || sessionType.getTextContent().equals(typeString)) {
                        names.add(Utils.getFirstElement(bean, "ejb-name").getTextContent());
                    }
                }
            }
        }
        return names;
    }
}
