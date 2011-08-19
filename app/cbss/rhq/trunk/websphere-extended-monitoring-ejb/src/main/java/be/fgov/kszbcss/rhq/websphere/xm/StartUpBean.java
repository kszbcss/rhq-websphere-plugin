package be.fgov.kszbcss.rhq.websphere.xm;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.management.JMException;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.modelmbean.RequiredModelMBean;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import be.fgov.kszbcss.rhq.websphere.xm.logging.ExtendedLoggingModule;
import be.fgov.kszbcss.rhq.websphere.xm.occ.OutboundConnectionCacheModule;
import be.fgov.kszbcss.rhq.websphere.xm.proc.ProcStatsModule;

import com.ibm.websphere.management.AdminServiceFactory;

@SuppressWarnings("serial")
public class StartUpBean implements SessionBean, MBeanRegistrar {
    private static final String DOMAIN = "be.fgov.kszbcss.rhq.websphere.xm";
    
    private static final Log log = LogFactory.getLog(StartUpBean.class);
    
    private List<Module> modules;
    private MBeanServer mbs;
    private List<ObjectName> registeredMBeans;
    
    public void ejbCreate() throws CreateException {
    }
    
    public void ejbActivate() throws EJBException, RemoteException {
    }

    public void ejbPassivate() throws EJBException, RemoteException {
    }

    public void ejbRemove() throws EJBException, RemoteException {
    }

    public void setSessionContext(SessionContext sessionContext) throws EJBException, RemoteException {
    }

    public boolean start() {
        modules = new ArrayList<Module>();
        modules.add(new OutboundConnectionCacheModule());
        modules.add(new ExtendedLoggingModule());
        modules.add(new ProcStatsModule());
        mbs = AdminServiceFactory.getMBeanFactory().getMBeanServer();
        registeredMBeans = new ArrayList<ObjectName>();
        for (Module module : modules) {
            log.info("Starting " + module.getClass().getName());
            if (!module.start(this)) {
                return false;
            }
        }
        return true;
    }
    
    public ObjectName register(RequiredModelMBean mbean, Hashtable<String,String> keyProperties) throws JMException {
        if (log.isDebugEnabled()) {
            log.debug("Attempting to register MBean with key properties " + keyProperties);
        }
        ObjectName objectName = mbs.registerMBean(mbean, new ObjectName(DOMAIN, keyProperties)).getObjectName();
        if (log.isDebugEnabled()) {
            log.debug("MBean registered with object name " + objectName);
        }
        registeredMBeans.add(objectName);
        return objectName;
    }

    public void stop() {
        for (Module module : modules) {
            log.info("Stopping " + module.getClass().getName());
            module.stop();
        }
        if (registeredMBeans != null) {
            for (ObjectName name : registeredMBeans) {
                try {
                    if (log.isDebugEnabled()) {
                        log.debug("Unregistering MBean " + name);
                    }
                    mbs.unregisterMBean(name);
                } catch (JMException ex) {
                    log.error("Failed to unregister MBean " + name, ex);
                }
            }
            registeredMBeans = null;
        }
        mbs = null;
        modules = null;
    }
}
