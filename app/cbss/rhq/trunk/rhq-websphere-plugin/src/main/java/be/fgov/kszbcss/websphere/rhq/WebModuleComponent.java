package be.fgov.kszbcss.websphere.rhq;

import java.util.HashSet;
import java.util.Set;

import javax.management.JMException;

import org.w3c.dom.Element;

import com.ibm.websphere.management.exception.ConnectorException;

public class WebModuleComponent extends ModuleComponent {
    @Override
    protected String getMBeanType() {
        return "WebModule";
    }
    
    public Set<String> getServletNames() throws JMException, ConnectorException {
        Set<String> result = new HashSet<String>();
        for (Element servlet : Utils.getElements(getDeploymentDescriptor().getDocumentElement(), "servlet")) {
            result.add(Utils.getFirstElement(servlet, "servlet-name").getTextContent());
        }
        return result;
    }
}
