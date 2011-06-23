package be.fgov.kszbcss.rhq.websphere.xm;

import java.util.Hashtable;

import javax.management.JMException;
import javax.management.ObjectName;
import javax.management.modelmbean.RequiredModelMBean;

public interface MBeanRegistrar {
    ObjectName register(RequiredModelMBean mbean, Hashtable<String,String> keyProperties) throws JMException;
}
