package be.fgov.kszbcss.websphere.rhq.measurement;

import java.util.Arrays;
import java.util.Map;

import javax.management.Attribute;
import javax.management.AttributeList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.rhq.core.domain.measurement.MeasurementDataTrait;
import org.rhq.core.domain.measurement.MeasurementReport;
import org.rhq.core.domain.measurement.MeasurementScheduleRequest;

import be.fgov.kszbcss.websphere.rhq.MBean;
import be.fgov.kszbcss.websphere.rhq.WebSphereServer;

public class JMXMeasurementHandler implements MeasurementHandler {
    private static final Log log = LogFactory.getLog(JMXMeasurementHandler.class);
    
    private final MBean mbean;
    
    public JMXMeasurementHandler(MBean mbean) {
        this.mbean = mbean;
    }
    
    public void getValues(WebSphereServer server, MeasurementReport report, Map<String,MeasurementScheduleRequest> requests) {
        String[] attributes = requests.keySet().toArray(new String[requests.size()]);
        AttributeList list;
        try {
            list = mbean.getAttributes(attributes);
        } catch (Exception ex) {
            log.error("Failed to get values for attributes " + requests.keySet());
            // TODO: shouldn't we fall back to fetching each attribute individually?
            return;
        }
        if (log.isDebugEnabled()) {
            log.debug("Fetched attributes from " + mbean + ": " + Arrays.asList(attributes) + "=" + list);
        }
        for (int i=0; i<attributes.length; i++) {
            MeasurementScheduleRequest request = requests.get(attributes[i]);
            Attribute attribute = (Attribute)list.get(i);
            Object value = attribute.getValue();
            switch (request.getDataType()) {
                case TRAIT:
                    if (log.isDebugEnabled()) {
                        log.debug("Adding measurement (trait) for " + request.getName() + "; value=" + value);
                    }
                    report.addData(new MeasurementDataTrait(request, String.valueOf(value)));
                    break;
                default:
                    log.error("Data type " + request.getDataType() + " not supported");
            }
        }
    }
}
