/*
 * RHQ WebSphere Plug-in
 * Copyright (C) 2012 Crossroads Bank for Social Security
 * All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License, version 2, as
 * published by the Free Software Foundation, and/or the GNU Lesser
 * General Public License, version 2.1, also as published by the Free
 * Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License and the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU General Public License
 * and the GNU Lesser General Public License along with this program;
 * if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package be.fgov.kszbcss.rhq.websphere.support.measurement;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.rhq.core.domain.measurement.MeasurementReport;
import org.rhq.core.domain.measurement.MeasurementScheduleRequest;
import org.rhq.core.pluginapi.measurement.MeasurementFacet;

import be.fgov.kszbcss.rhq.websphere.WebSphereServer;
import be.fgov.kszbcss.rhq.websphere.component.WebSphereComponent;

public class MeasurementFacetSupport implements MeasurementFacet {
    private static final Log log = LogFactory.getLog(MeasurementFacetSupport.class);
    
    private final WebSphereComponent<?> component;
    private final Map<String,MeasurementHandler> handlers = new HashMap<String,MeasurementHandler>();
    private final Map<String,MeasurementGroupHandler> groupHandlers = new HashMap<String,MeasurementGroupHandler>();
    private MeasurementGroupHandler defaultHandler;
    
    public MeasurementFacetSupport(WebSphereComponent<?> component) {
        this.component = component;
    }
    
    public void addHandler(String name, MeasurementHandler handler) {
        handlers.put(name, handler);
    }
    
    public void addHandler(String prefix, MeasurementGroupHandler handler) {
        groupHandlers.put(prefix, handler);
    }
    
    public void setDefaultHandler(MeasurementGroupHandler defaultHandler) {
        this.defaultHandler = defaultHandler;
    }

    public void getValues(MeasurementReport report, Set<MeasurementScheduleRequest> requests) throws Exception {
        WebSphereServer server = component.getServer();
        
        if (log.isDebugEnabled()) {
            log.debug("Starting to process the following measurement requests: " + requests);
        }
        
        Map<String,Map<String,MeasurementScheduleRequest>> namedRequestMap = new HashMap<String,Map<String,MeasurementScheduleRequest>>();
        Map<String,MeasurementScheduleRequest> defaultRequests = new HashMap<String,MeasurementScheduleRequest>();
        for (MeasurementScheduleRequest request : requests) {
            String name = request.getName();
            MeasurementHandler handler = handlers.get(name);
            if (handler != null) {
                if (log.isDebugEnabled()) {
                    log.debug("Processing " + name + " using handler " + handler);
                }
                handler.getValue(server, report, request);
            } else {
                int idx = name.indexOf('.');
                if (idx == -1) {
                    defaultRequests.put(name, request);
                } else {
                    String prefix = name.substring(0, idx);
                    if (groupHandlers.containsKey(prefix)) {
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
        }
        
        for (Map.Entry<String,Map<String,MeasurementScheduleRequest>> entry : namedRequestMap.entrySet()) {
            MeasurementGroupHandler groupHandler = groupHandlers.get(entry.getKey());
            if (log.isDebugEnabled()) {
                log.debug("Processing " + entry.getValue().keySet() + " using group handler " + groupHandler + " for group " + entry.getKey());
            }
            groupHandler.getValues(server, report, entry.getValue());
        }
        if (!defaultRequests.isEmpty()) {
            if (defaultHandler == null) {
                log.error("The following measurements could not be collected because no default handler is defined: " + defaultRequests.keySet());
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("Processing " + defaultRequests.keySet() + " using default handler " + defaultHandler);
                }
                defaultHandler.getValues(server, report, defaultRequests);
            }
        }
    }
}
