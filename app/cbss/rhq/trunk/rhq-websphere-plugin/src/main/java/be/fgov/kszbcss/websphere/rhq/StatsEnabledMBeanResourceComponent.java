package be.fgov.kszbcss.websphere.rhq;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.beanutils.PropertyUtilsBean;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mc4j.ems.connection.bean.EmsBean;
import org.rhq.core.domain.measurement.MeasurementDataNumeric;
import org.rhq.core.domain.measurement.MeasurementReport;
import org.rhq.core.domain.measurement.MeasurementScheduleRequest;
import org.rhq.plugins.jmx.MBeanResourceComponent;

public class StatsEnabledMBeanResourceComponent extends MBeanResourceComponent {
    private final Log log = LogFactory.getLog(StatsEnabledMBeanResourceComponent.class);
    
    @Override
    protected void getValues(MeasurementReport report, Set requests, EmsBean bean) {
        Set<MeasurementScheduleRequest> simpleRequests = new HashSet<MeasurementScheduleRequest>();
        // We create a new PropertyUtilsBean every time in order to avoid keeping references
        // to class loaders used by EMS (which could create a leak).
        PropertyUtilsBean propUtils = new PropertyUtilsBean();
        StatsDynaBean stats = null;
        for (MeasurementScheduleRequest request : (Set<MeasurementScheduleRequest>)requests) {
            String name = request.getName();
            if (name.startsWith("stats.")) {
                if (stats == null) {
                    stats = new StatsDynaBean(bean.getAttribute("stats").getValue());
                }
                String propertyName = name.substring(6);
                Long value;
                try {
                    value = (Long)propUtils.getNestedProperty(stats, propertyName);
                } catch (Exception ex) {
                    log.error("Failed to get the " + propertyName + " from the Stats object", ex);
                    continue;
                }
                report.addData(new MeasurementDataNumeric(request, new Double(value)));
            } else {
                simpleRequests.add(request);
            }
        }
        super.getValues(report, simpleRequests, bean);
    }

}
