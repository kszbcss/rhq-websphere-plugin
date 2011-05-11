package be.fgov.kszbcss.websphere.rhq.measurement;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.rhq.core.domain.measurement.MeasurementReport;
import org.rhq.core.domain.measurement.MeasurementScheduleRequest;
import org.rhq.core.pluginapi.measurement.MeasurementFacet;

import be.fgov.kszbcss.websphere.rhq.WebSphereServer;

public class MeasurementFacetSupport implements MeasurementFacet {
    private static final Log log = LogFactory.getLog(MeasurementFacetSupport.class);
    
    private final WebSphereServer server;
    private final Map<String,MeasurementHandler> namedHandlers = new HashMap<String,MeasurementHandler>();
    private MeasurementHandler defaultHandler;
    
    public MeasurementFacetSupport(WebSphereServer server) {
        this.server = server;
    }
    
    public void addHandler(String prefix, MeasurementHandler handler) {
        namedHandlers.put(prefix, handler);
    }
    
    public void setDefaultHandler(MeasurementHandler defaultHandler) {
        this.defaultHandler = defaultHandler;
    }

    public void getValues(MeasurementReport report, Set<MeasurementScheduleRequest> requests) throws Exception {
        Map<String,Map<String,MeasurementScheduleRequest>> namedRequestMap = new HashMap<String,Map<String,MeasurementScheduleRequest>>();
        Map<String,MeasurementScheduleRequest> defaultRequests = new HashMap<String,MeasurementScheduleRequest>();
        for (MeasurementScheduleRequest request : requests) {
            String name = request.getName();
            int idx = name.indexOf('.');
            if (idx == -1) {
                defaultRequests.put(name, request);
            } else {
                String prefix = name.substring(0, idx);
                if (namedHandlers.containsKey(prefix)) {
                    Map<String,MeasurementScheduleRequest> namedRequests = namedRequestMap.get(prefix);
                    if (namedRequests == null) {
                        namedRequests = new HashMap<String,MeasurementScheduleRequest>();
                        namedRequestMap.put(prefix, namedRequests);
                    }
                    namedRequests.put(name.substring(idx+1), request);
                } else {
                    defaultRequests.put(name, request);
                }
            }
        }
        
        for (Map.Entry<String,Map<String,MeasurementScheduleRequest>> entry : namedRequestMap.entrySet()) {
            namedHandlers.get(entry.getKey()).getValues(server, report, entry.getValue());
        }
        if (!defaultRequests.isEmpty()) {
            if (defaultHandler == null) {
                log.error("The following measurements could not be collected because no default handler is defined: " + defaultRequests.keySet());
            } else {
                defaultHandler.getValues(server, report, defaultRequests);
            }
        }
    }
}
