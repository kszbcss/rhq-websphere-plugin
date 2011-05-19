package be.fgov.kszbcss.websphere.rhq;

import com.ibm.websphere.pmi.PmiModuleConfig;
import com.ibm.websphere.pmi.stat.MBeanLevelSpec;
import com.ibm.websphere.pmi.stat.MBeanStatDescriptor;
import com.ibm.websphere.pmi.stat.WSStats;

public interface Perf {
    MBeanLevelSpec[] getInstrumentationLevel(MBeanStatDescriptor msd, Boolean recursive);
    void setInstrumentationLevel(MBeanLevelSpec mbeanLevelSpec, Boolean recursive);
    WSStats getStatsObject(MBeanStatDescriptor msd, Boolean recursive);
    PmiModuleConfig[] getConfigs();
}
