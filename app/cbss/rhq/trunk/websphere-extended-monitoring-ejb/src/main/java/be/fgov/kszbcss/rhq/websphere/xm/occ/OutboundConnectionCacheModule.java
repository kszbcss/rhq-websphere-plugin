package be.fgov.kszbcss.rhq.websphere.xm.occ;

import java.util.Hashtable;

import javax.management.JMException;
import javax.management.MBeanParameterInfo;
import javax.management.ObjectName;
import javax.management.modelmbean.DescriptorSupport;
import javax.management.modelmbean.InvalidTargetObjectTypeException;
import javax.management.modelmbean.ModelMBeanAttributeInfo;
import javax.management.modelmbean.ModelMBeanConstructorInfo;
import javax.management.modelmbean.ModelMBeanInfoSupport;
import javax.management.modelmbean.ModelMBeanNotificationInfo;
import javax.management.modelmbean.ModelMBeanOperationInfo;
import javax.management.modelmbean.RequiredModelMBean;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ibm.wsspi.pmi.factory.StatsFactory;
import com.ibm.wsspi.pmi.factory.StatsFactoryException;
import com.ibm.wsspi.pmi.factory.StatsGroup;

import be.fgov.kszbcss.rhq.websphere.xm.MBeanRegistrar;
import be.fgov.kszbcss.rhq.websphere.xm.Module;
import be.fgov.kszbcss.rhq.websphere.xm.StartUpBean;

public class OutboundConnectionCacheModule implements Module {
    private static final Log log = LogFactory.getLog(OutboundConnectionCacheModule.class);
    
    private StatsGroup group;

    public boolean start(MBeanRegistrar mbeanRegistrar) {
        try {
            group = StatsFactory.createStatsGroup("OutboundConnectionCache", "/be/fgov/kszbcss/rhq/websphere/xm/OutboundConnectionCacheStats.xml", null);
            ClassLoader cl = StartUpBean.class.getClassLoader();
            // The JAX-RPC cache is part of com.ibm.ws.runtime.jar and is visible to the application class loader.
            setupOutboundConnectionCacheMonitor(mbeanRegistrar, cl, "com.ibm.ws.webservices.engine.transport.channel.OutboundConnectionCache", "JAX-RPC");
            // The JAX-WS cache is part of the Axis2 OSGi bundle, but is not exported. We get the class loader
            // from an exported class.
            try {
                setupOutboundConnectionCacheMonitor(mbeanRegistrar, cl.loadClass("com.ibm.ws.websvcs.transport.http.SOAPOverHTTPSender").getClassLoader(),
                        "com.ibm.ws.websvcs.transport.channel.OutboundConnectionCache", "JAX-WS");
            } catch (ClassNotFoundException ex) {
                // This means that there is no JAX-WS runtime; just continue.
            }
            return true;
        } catch (JMException ex) {
            log.error("Failed to register MBeans", ex);
            return false;
        } catch (StatsFactoryException ex) {
            log.error("Failed to set up PMI statistics", ex);
            return false;
        }
    }

    private void setupOutboundConnectionCacheMonitor(MBeanRegistrar mbeanRegistrar, ClassLoader classLoader, String className, String moduleName) throws JMException, StatsFactoryException {
        // Create the monitor component
        Class<?> outboundConnectionCacheClass;
        try {
            outboundConnectionCacheClass = classLoader.loadClass(className);
        } catch (ClassNotFoundException ex) {
            return;
        }
        OutboundConnectionCacheMonitor monitor;
        try {
            monitor = new OutboundConnectionCacheMonitor(outboundConnectionCacheClass);
        } catch (Exception ex) {
            log.error("Failed to set up PMI module for " + moduleName + " outbound connection cache", ex);
            return;
        }
        
        // Expose it as an MBean
        RequiredModelMBean mbean = new RequiredModelMBean();
        mbean.setModelMBeanInfo(new ModelMBeanInfoSupport(
                OutboundConnectionCacheMonitor.class.getName(),
                "Provides information about outbound HTTP connection caches for JAX-RPC and JAX-WS",
                new ModelMBeanAttributeInfo[] {
                        new ModelMBeanAttributeInfo(
                                "maxConnection",
                                "int",
                                "The configured maximum number of connections in the pool",
                                true,
                                false,
                                false,
                                new DescriptorSupport(new String[] {
                                        "name=maxConnection",
                                        "descriptorType=attribute",
                                        "getMethod=maxConnection"})),
                        new ModelMBeanAttributeInfo(
                                "connTimeout",
                                "int",
                                "The configured connection timeout",
                                true,
                                false,
                                false,
                                new DescriptorSupport(new String[] {
                                        "name=connTimeout",
                                        "descriptorType=attribute",
                                        "getMethod=connTimeout"}))
                },
                new ModelMBeanConstructorInfo[0],
                new ModelMBeanOperationInfo[] {
                        new ModelMBeanOperationInfo(
                                "maxConnection",
                                "Get the configured maximum number of connections in the pool",
                                new MBeanParameterInfo[0],
                                "int",
                                ModelMBeanOperationInfo.INFO),
                        new ModelMBeanOperationInfo(
                                "connTimeout",
                                "Get the configured connection timeout",
                                new MBeanParameterInfo[0],
                                "int",
                                ModelMBeanOperationInfo.INFO),
                },
                new ModelMBeanNotificationInfo[0]));
        try {
            mbean.setManagedResource(monitor, "ObjectReference");
        } catch (InvalidTargetObjectTypeException ex) {
            // Should never happen
            throw new RuntimeException(ex);
        }
        
        Hashtable<String,String> keyProperties = new Hashtable<String,String>();
        keyProperties.put("type", "OutboundConnectionCache");
        keyProperties.put("name", moduleName);
        keyProperties.put("cacheClass", className);
        ObjectName mbeanName = mbeanRegistrar.register(mbean, keyProperties);
        
        // Create a PMI module
        StatsFactory.createStatsInstance(moduleName, group, mbeanName, monitor);
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
