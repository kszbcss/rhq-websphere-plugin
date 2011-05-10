package be.fgov.kszbcss.websphere.rhq;

import javax.management.JMException;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.mc4j.ems.connection.EmsException;
import org.mc4j.ems.connection.bean.EmsBean;

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
}
