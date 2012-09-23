package be.fgov.kszbcss.rhq.websphere.proxy;

import javax.management.JMException;

import com.ibm.websphere.management.exception.ConnectorException;
import com.ibm.websphere.pmi.PmiModuleConfig;
import com.ibm.websphere.pmi.stat.MBeanLevelSpec;
import com.ibm.websphere.pmi.stat.MBeanStatDescriptor;
import com.ibm.websphere.pmi.stat.WSStats;

public interface Perf {
    MBeanLevelSpec[] getInstrumentationLevel(MBeanStatDescriptor msd, Boolean recursive) throws JMException, ConnectorException;
    void setInstrumentationLevel(MBeanLevelSpec mbeanLevelSpec, Boolean recursive) throws JMException, ConnectorException;
    WSStats getStatsObject(MBeanStatDescriptor msd, Boolean recursive) throws JMException, ConnectorException;
    PmiModuleConfig[] getConfigs() throws JMException, ConnectorException;
    PmiModuleConfig getConfig(String objectName) throws JMException, ConnectorException;
}
