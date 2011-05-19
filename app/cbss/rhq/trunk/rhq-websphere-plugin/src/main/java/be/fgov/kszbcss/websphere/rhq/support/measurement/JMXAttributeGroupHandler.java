package be.fgov.kszbcss.websphere.rhq.support.measurement;

import java.util.Arrays;
import java.util.Map;

import javax.management.Attribute;
import javax.management.AttributeList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.rhq.core.domain.measurement.MeasurementReport;
import org.rhq.core.domain.measurement.MeasurementScheduleRequest;

import be.fgov.kszbcss.websphere.rhq.WebSphereServer;
import be.fgov.kszbcss.websphere.rhq.mbean.MBeanClient;

public class JMXAttributeGroupHandler implements MeasurementGroupHandler {
    private static final Log log = LogFactory.getLog(JMXAttributeGroupHandler.class);
    
    private final MBeanClient mbean;
    
    public JMXAttributeGroupHandler(MBeanClient mbean) {
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
            JMXMeasurementDataUtils.addData(report, requests.get(attributes[i]), ((Attribute)list.get(i)).getValue());
        }
    }
}
