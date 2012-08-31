package be.fgov.kszbcss.rhq.websphere.component;

import com.ibm.websphere.pmi.stat.WSRangeStatistic;

import be.fgov.kszbcss.rhq.websphere.mbean.MBeanClient;
import be.fgov.kszbcss.rhq.websphere.support.measurement.PMIMeasurementHandler;
import be.fgov.kszbcss.rhq.websphere.support.measurement.PMIModuleSelector;

public class ThreadPoolPMIMeasurementHandler extends PMIMeasurementHandler {
    public ThreadPoolPMIMeasurementHandler(MBeanClient mbean, String... path) {
        super(mbean, path);
    }

    public ThreadPoolPMIMeasurementHandler(MBeanClient mbean, PMIModuleSelector moduleSelector) {
        super(mbean, moduleSelector);
    }

    @Override
    protected double getValue(String name, WSRangeStatistic statistic) {
        if (name.equals("PercentMaxed")) {
            return ((double)statistic.getCurrent())/100;
        } else {
            return super.getValue(name, statistic);
        }
    }
}
