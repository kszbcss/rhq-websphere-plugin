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
package be.fgov.kszbcss.rhq.websphere;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.rhq.core.domain.auth.Subject;
import org.rhq.core.domain.configuration.Configuration;
import org.rhq.core.domain.criteria.OperationDefinitionCriteria;
import org.rhq.core.domain.criteria.ResourceCriteria;
import org.rhq.core.domain.criteria.ResourceOperationHistoryCriteria;
import org.rhq.core.domain.measurement.AvailabilityType;
import org.rhq.core.domain.operation.OperationRequestStatus;
import org.rhq.core.domain.operation.ResourceOperationHistory;
import org.rhq.core.domain.resource.Resource;
import org.rhq.core.domain.resource.composite.ResourceAvailabilitySummary;
import org.rhq.core.domain.util.PageControl;
import org.rhq.core.domain.util.PageList;
import org.rhq.enterprise.server.operation.OperationManagerLocal;
import org.rhq.enterprise.server.operation.ResourceOperationSchedule;
import org.rhq.enterprise.server.plugin.pc.ScheduledJobInvocationContext;
import org.rhq.enterprise.server.plugin.pc.ServerPluginComponent;
import org.rhq.enterprise.server.plugin.pc.ServerPluginContext;
import org.rhq.enterprise.server.resource.ResourceManagerLocal;
import org.rhq.enterprise.server.util.LookupUtil;

public class WebSphereServerPlugin implements ServerPluginComponent {
    private static final Log log = LogFactory.getLog(WebSphereServerPlugin.class);
    
    public void initialize(ServerPluginContext context) throws Exception {
    }

    public void start() {
    }

    public void stop() {
    }

    public void shutdown() {
    }
    
    public void autoUninventory(ScheduledJobInvocationContext invocation) throws Exception {
        Subject user = LookupUtil.getSubjectManager().getOverlord();
        ResourceManagerLocal resourceManager = LookupUtil.getResourceManager();
        OperationManagerLocal operationManager = LookupUtil.getOperationManager();
        
        int uninventoryDelay = Integer.parseInt(invocation.getJobDefinition().getCallbackData().getProperty("uninventoryDelay"));
        
        ResourceCriteria resourceCriteria = new ResourceCriteria();
        resourceCriteria.addFilterCurrentAvailability(AvailabilityType.DOWN);
        resourceCriteria.addFilterPluginName("WebSphere");
        resourceCriteria.fetchParentResource(true);
        resourceCriteria.setPageControl(PageControl.getUnlimitedInstance());
        LinkedList<Resource> resources = new LinkedList<Resource>();
        PageList<Resource> resourcePageList = resourceManager.findResourcesByCriteria(user, resourceCriteria);
        for (Resource resource : resourcePageList) {
            if (!resourcePageList.contains(resource.getParentResource())) {
                if (uninventoryDelay > 0) {
                    ResourceAvailabilitySummary availability = resourceManager.getAvailabilitySummary(user, resource.getId());
                    if ((System.currentTimeMillis() - availability.getLastChange().getTime())/60000 < uninventoryDelay) {
                        continue;
                    }
                }
                resources.add(resource);
            }
        }
        
        Map<Integer,Boolean> hasCheckConfigurationByResourceTypeId = new HashMap<Integer,Boolean>();
        
        // RHQ has no API to invoke operations synchronously. Therefore we need to go through the normal
        // APIs, schedule an operation and poll for the result. To accelerate things, we schedule a certain
        // number of operations in parallel.
        List<ResourceOperationSchedule> schedules = new ArrayList<ResourceOperationSchedule>();
        while (!resources.isEmpty() || !schedules.isEmpty()) {
            while (!resources.isEmpty() && schedules.size() < 6) {
                Resource resource = resources.removeFirst();
                int resourceTypeId = resource.getResourceType().getId();
                Boolean hasCheckConfiguration = hasCheckConfigurationByResourceTypeId.get(resourceTypeId);
                if (hasCheckConfiguration == null) {
                    OperationDefinitionCriteria opDefCriteria = new OperationDefinitionCriteria();
                    opDefCriteria.setPageControl(PageControl.getUnlimitedInstance());
                    opDefCriteria.addFilterResourceTypeId(resource.getResourceType().getId());
                    opDefCriteria.addFilterName("checkConfiguration");
                    hasCheckConfiguration = operationManager.findOperationDefinitionsByCriteria(user, opDefCriteria).size() > 0;
                    hasCheckConfigurationByResourceTypeId.put(resourceTypeId, hasCheckConfiguration);
                }
                if (hasCheckConfiguration) {
                    log.info("Scheduling checkConfiguration for " + resource.getName() + " (" + resource.getId() + ")");
                    schedules.add(operationManager.scheduleResourceOperation(user, resource.getId(), "checkConfiguration", 0, 0, 0, 0, new Configuration(), "Scheduled by RHQ WebSphere Server Plugin"));
                } else {
                    log.info("checkConfiguration operation not supported on " + resource.getName() + " (" + resource.getId() + ")");
                }
            }
            Thread.sleep(1000);
            List<ResourceOperationSchedule> deferred = new ArrayList<ResourceOperationSchedule>();
            for (ResourceOperationSchedule schedule : schedules) {
                ResourceOperationHistoryCriteria historyCriteria = new ResourceOperationHistoryCriteria();
                historyCriteria.setPageControl(PageControl.getUnlimitedInstance());
                historyCriteria.addFilterJobId(schedule.getJobId());
                historyCriteria.fetchResults(true);
                PageList<ResourceOperationHistory> historyList = operationManager.findResourceOperationHistoriesByCriteria(user, historyCriteria);
                if (historyList.size() == 1) {
                    ResourceOperationHistory history = historyList.get(0);
                    if (history.getStatus() == OperationRequestStatus.SUCCESS) {
                        if (history.getResults() == null) {
                            // This may happen if the checkConfiguration operation is declared on the resource type,
                            // but not correctly implemented by the resource component.
                            log.error("No results available for operation on " + schedule.getResource().getName() + " (" + schedule.getResource().getId() + ")");
                        } else {
                            if (!history.getResults().getSimple("isConfigured").getBooleanValue()) {
                                log.info("About to uninventory " + schedule.getResource().getName() + " (" + schedule.getResource().getId() + ")");
                                resourceManager.uninventoryResources(user, new int[] { schedule.getResource().getId() });
                            }
                            operationManager.deleteOperationHistory(user, history.getId(), false);
                        }
                    } else if (history.getStatus() == OperationRequestStatus.INPROGRESS) {
                        log.info("Deferring operation on " + schedule.getResource().getName() + " (" + schedule.getResource().getId() + "): still in progress");
                        deferred.add(schedule);
                    } else {
                        log.error("Operation didn't succeed on " + schedule.getResource().getName() + " (" + schedule.getResource().getId() + "): " + history.getStatus());
                    }
                } else if (historyList.size() == 0) {
                    // If we get here, then the ResourceOperationHistory has not been created yet (it is created asynchronously) 
                    log.info("ResourceOperationHistory for " + schedule.getResource().getName() + " (" + schedule.getResource().getId() + ") not found yet");
                    deferred.add(schedule);
                } else {
                    log.error("Unexpected result from findResourceOperationHistoriesByCriteria for " + schedule.getResource().getName() + " (" + schedule.getResource().getId() + ")");
                }
            }
            schedules = deferred;
        }
    }
}
