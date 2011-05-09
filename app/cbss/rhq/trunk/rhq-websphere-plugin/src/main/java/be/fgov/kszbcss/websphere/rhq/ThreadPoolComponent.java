package be.fgov.kszbcss.websphere.rhq;

import com.ibm.websphere.pmi.stat.WSRangeStatistic;

public class ThreadPoolComponent extends StatsEnabledMBeanResourceComponent<WebSphereServerComponent> {
    @Override
    protected double getValue(String name, WSRangeStatistic statistic) {
        if (name.equals("PercentMaxed")) {
            return ((double)statistic.getCurrent())/100;
        } else {
            return super.getValue(name, statistic);
        }
    }
}
