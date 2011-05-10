package be.fgov.kszbcss.websphere.rhq;

import java.util.ArrayList;
import java.util.List;

import javax.management.JMException;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.mc4j.ems.connection.EmsException;
import org.mc4j.ems.connection.bean.EmsBean;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.ibm.websphere.pmi.stat.MBeanStatDescriptor;
import com.ibm.websphere.pmi.stat.StatDescriptor;

public class Utils {
    private Utils() {}
    
    public static ObjectName createObjectName(String s) {
        try {
            return new ObjectName(s);
        } catch (MalformedObjectNameException ex) {
            throw new IllegalArgumentException(ex.getMessage());
        }
    }
    
    public static MBeanStatDescriptor getMBeanStatDescriptor(EmsBean bean, String... path) {
        try {
            ObjectName mbean = new ObjectName(bean.getBeanName().toString());
            return path.length == 0 ? new MBeanStatDescriptor(mbean) : new MBeanStatDescriptor(mbean, new StatDescriptor(path));
        } catch (JMException ex) {
            throw new EmsException(ex);
        }
    }
    
    public static List<Element> getElements(Element parent, String localName) {
        List<Element> result = new ArrayList<Element>();
        Node child = parent.getFirstChild();
        while (child != null) {
            if (child instanceof Element && child.getLocalName().equals(localName)) {
                result.add((Element)child);
            }
            child = child.getNextSibling();
        }
        return result;
    }
    
    public static Element getFirstElement(Element parent, String localName) {
        Node child = parent.getFirstChild();
        while (child != null) {
            if (child instanceof Element && child.getLocalName().equals(localName)) {
                return (Element)child;
            }
            child = child.getNextSibling();
        }
        return null;
    }
}
