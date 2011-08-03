package be.fgov.kszbcss.rhq.websphere.xm.logging;

import java.util.Hashtable;
import java.util.logging.Logger;

import javax.management.JMException;
import javax.management.MBeanParameterInfo;
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

import be.fgov.kszbcss.rhq.websphere.xm.MBeanRegistrar;
import be.fgov.kszbcss.rhq.websphere.xm.Module;

public class ExtendedLoggingModule implements Module {
    private static final Log log = LogFactory.getLog(ExtendedLoggingModule.class);
    
    private ExtendedLoggingService service;
    
    public boolean start(MBeanRegistrar mbeanRegistrar) {
        service = new ExtendedLoggingService();
        if (log.isDebugEnabled()) {
            log.debug("Registering handler on root logger");
        }
        Logger.getLogger("").addHandler(service);
        
        try {
            RequiredModelMBean mbean = new RequiredModelMBean();
            mbean.setModelMBeanInfo(new ModelMBeanInfoSupport(
                    ExtendedLoggingService.class.getName(),
                    "Provides advanced logging services; alternative to RasLoggingService.",
                    new ModelMBeanAttributeInfo[] {
                            new ModelMBeanAttributeInfo(
                                    "nextSequence",
                                    "long",
                                    "The sequence number of the next expected log message",
                                    true,
                                    false,
                                    false,
                                    new DescriptorSupport(new String[] {
                                            "name=nextSequence",
                                            "descriptorType=attribute",
                                            "getMethod=getNextSequence"}))
                    },
                    new ModelMBeanConstructorInfo[0],
                    new ModelMBeanOperationInfo[] {
                            new ModelMBeanOperationInfo(
                                    "getNextSequence",
                                    "Get the sequence number of the next expected log message",
                                    new MBeanParameterInfo[0],
                                    "long",
                                    ModelMBeanOperationInfo.INFO),
                            new ModelMBeanOperationInfo(
                                    "getMessages",
                                    "Get the buffered messages starting with a given sequence",
                                    new MBeanParameterInfo[] {
                                            new MBeanParameterInfo("startSequence", "long",
                                                    "The sequence number of the first message to return")
                                    },
                                    "void",
                                    ModelMBeanOperationInfo.INFO),
                    },
                    new ModelMBeanNotificationInfo[0]));
            try {
                mbean.setManagedResource(service, "ObjectReference");
            } catch (InvalidTargetObjectTypeException ex) {
                // Should never happen
                throw new RuntimeException(ex);
            }
            
            Hashtable<String,String> keyProperties = new Hashtable<String,String>();
            keyProperties.put("type", "ExtendedLoggingService");
            keyProperties.put("name", "ExtendedLoggingService");
            mbeanRegistrar.register(mbean, keyProperties);
            return true;
        } catch (JMException ex) {
            log.error("Unable to register MBean", ex);
            return false;
        }
    }

    public void stop() {
        if (service != null) {
            if (log.isDebugEnabled()) {
                log.debug("Removing handler from root logger");
            }
            Logger.getLogger("").removeHandler(service);
            service = null;
        }
    }
}
