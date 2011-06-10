package be.fgov.kszbcss.rhq.websphere.xm;

import java.rmi.RemoteException;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ibm.wsspi.pmi.factory.StatsFactory;
import com.ibm.wsspi.pmi.factory.StatsFactoryException;
import com.ibm.wsspi.pmi.factory.StatsGroup;

@SuppressWarnings("serial")
public class StartUpBean implements SessionBean {
    private static final Log log = LogFactory.getLog(StartUpBean.class);
    
    private StatsGroup group;
    
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
        try {
            group = StatsFactory.createStatsGroup("OutboundConnectionCacheGroup", "/be/fgov/kszbcss/rhq/websphere/xm/OutboundConnectionCacheStats.xml", null);
            ClassLoader cl = StartUpBean.class.getClassLoader();
            // The JAX-RPC cache is part of com.ibm.ws.runtime.jar and is visible to the application class loader.
            setupOutboundConnectionCacheModule(cl, "com.ibm.ws.webservices.engine.transport.channel.OutboundConnectionCache", "JAX-RPC");
            // The JAX-WS cache is part of the Axis2 OSGi bundle, but is not exported. We get the class loader
            // from an exported class.
            try {
                setupOutboundConnectionCacheModule(cl.loadClass("com.ibm.ws.websvcs.transport.http.SOAPOverHTTPSender").getClassLoader(),
                        "com.ibm.ws.websvcs.transport.channel.OutboundConnectionCache", "JAX-WS");
            } catch (ClassNotFoundException ex) {
                // This means that there is no JAX-WS runtime; just continue.
            }
            return true;
        } catch (StatsFactoryException ex) {
            log.error("Failed to set up PMI statistics", ex);
            return false;
        }
    }
    
    private void setupOutboundConnectionCacheModule(ClassLoader classLoader, String className, String moduleName) throws StatsFactoryException {
        Class<?> outboundConnectionCacheClass;
        try {
            outboundConnectionCacheClass = classLoader.loadClass(className);
        } catch (ClassNotFoundException ex) {
            return;
        }
        OutboundConnectionCacheModule module;
        try {
            module = new OutboundConnectionCacheModule(outboundConnectionCacheClass);
        } catch (Exception ex) {
            log.error("Failed to set up PMI module for " + moduleName + " outbound connection cache", ex);
            return;
        }
        StatsFactory.createStatsInstance(moduleName, group, null, module);
    }

    public void stop() {
        if (group != null) {
            try {
                StatsFactory.removeStatsGroup(group);
            } catch (StatsFactoryException ex) {
                log.error("Failed to remove stats group", ex);
            }
        }
    }
}
