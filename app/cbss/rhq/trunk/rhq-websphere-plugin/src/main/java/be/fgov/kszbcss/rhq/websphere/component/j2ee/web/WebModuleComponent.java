package be.fgov.kszbcss.rhq.websphere.component.j2ee.web;

import java.util.HashSet;
import java.util.Set;

import javax.management.JMException;

import org.w3c.dom.Element;

import be.fgov.kszbcss.rhq.websphere.Utils;
import be.fgov.kszbcss.rhq.websphere.component.j2ee.ModuleComponent;

import com.ibm.websphere.management.exception.ConnectorException;

public class WebModuleComponent extends ModuleComponent {
    @Override
    protected String getMBeanType() {
        return "WebModule";
    }
    
    public Set<String> getServletNames() throws JMException, ConnectorException {
        Set<String> result = new HashSet<String>();
        for (Element servlet : Utils.getElements(getModuleInfo().getDeploymentDescriptor().getDocumentElement(), "servlet")) {
            result.add(Utils.getFirstElement(servlet, "servlet-name").getTextContent());
        }
        return result;
    }
}
