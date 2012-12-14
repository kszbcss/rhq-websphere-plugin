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
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.rhq.core.domain.auth.Subject;
import org.rhq.core.domain.configuration.Configuration;
import org.rhq.core.domain.configuration.Property;
import org.rhq.core.domain.configuration.PropertyMap;
import org.rhq.core.domain.configuration.PropertySimple;
import org.rhq.core.domain.configuration.ResourceConfigurationUpdate;
import org.rhq.core.domain.criteria.OperationDefinitionCriteria;
import org.rhq.core.domain.criteria.ResourceCriteria;
import org.rhq.core.domain.criteria.ResourceOperationHistoryCriteria;
import org.rhq.core.domain.measurement.AvailabilityType;
import org.rhq.core.domain.operation.OperationRequestStatus;
import org.rhq.core.domain.operation.ResourceOperationHistory;
import org.rhq.core.domain.operation.bean.ResourceOperationSchedule;
import org.rhq.core.domain.resource.Resource;
import org.rhq.core.domain.resource.composite.ResourceAvailabilitySummary;
import org.rhq.core.domain.tagging.Tag;
import org.rhq.core.domain.util.PageControl;
import org.rhq.core.domain.util.PageList;
import org.rhq.enterprise.server.configuration.ConfigurationManagerLocal;
import org.rhq.enterprise.server.operation.OperationManagerLocal;
import org.rhq.enterprise.server.plugin.pc.ScheduledJobInvocationContext;
import org.rhq.enterprise.server.plugin.pc.ServerPluginComponent;
import org.rhq.enterprise.server.plugin.pc.ServerPluginContext;
import org.rhq.enterprise.server.resource.ResourceManagerLocal;
import org.rhq.enterprise.server.tagging.TagManagerLocal;
import org.rhq.enterprise.server.util.LookupUtil;

public class WebSphereServerPlugin implements ServerPluginComponent {
    private static final Log log = LogFactory.getLog(WebSphereServerPlugin.class);
    
    private Configuration config;
    
    public void initialize(ServerPluginContext context) throws Exception {
        config = context.getPluginConfiguration();
    }

    public void start() {
    }

    public void stop() {
    }

    public void shutdown() {
    }
    
    public void autoUninventory(ScheduledJobInvocationContext invocation) throws Exception {
        Subject user = LookupUtil.getSubjectManager().getOverlord();
        TagManagerLocal tagManager = LookupUtil.getTagManager();
        ResourceManagerLocal resourceManager = LookupUtil.getResourceManager();
        OperationManagerLocal operationManager = LookupUtil.getOperationManager();
        
        int uninventoryDelay = Integer.parseInt(invocation.getJobDefinition().getCallbackData().getProperty("uninventoryDelay"));
        
        // addTags either creates a new tag or returns an existing tag
        Tag unconfiguredTag = tagManager.addTags(user, Collections.singleton(new Tag("websphere", null, "unconfigured"))).iterator().next();
        
        // Resources to check
        LinkedList<Resource> resources = new LinkedList<Resource>();
        
        // Search resources that can be uninventoried
        ResourceCriteria resourceCriteria = new ResourceCriteria();
        resourceCriteria.addFilterCurrentAvailability(AvailabilityType.DISABLED);
        resourceCriteria.addFilterTag(unconfiguredTag);
        resourceCriteria.fetchTags(true);
        resourceCriteria.setPageControl(PageControl.getUnlimitedInstance());
        PageList<Resource> resourcePageList = resourceManager.findResourcesByCriteria(user, resourceCriteria);
        for (Resource resource : resourcePageList) {
            ResourceAvailabilitySummary availability = resourceManager.getAvailabilitySummary(user, resource.getId());
            if ((System.currentTimeMillis() - availability.getLastChange().getTime())/60000 > uninventoryDelay) {
                log.info("About to uninventory " + resource.getName() + " (" + resource.getId() + ")");
                resourceManager.uninventoryResources(user, new int[] { resource.getId() });
            } else {
                log.debug("Resource " + resource.getName() + " (" + resource.getId() + ") is tagged as unconfigured; force configuration check");
                resources.add(resource);
            }
        }
        
        // Now search for WebSphere resources that are down and check if they have been unconfigured
        resourceCriteria = new ResourceCriteria();
        resourceCriteria.addFilterCurrentAvailability(AvailabilityType.DOWN);
        resourceCriteria.addFilterPluginName("WebSphere");
        resourceCriteria.fetchParentResource(true);
        resourceCriteria.fetchTags(true);
        resourceCriteria.setPageControl(PageControl.getUnlimitedInstance());
        resourcePageList = resourceManager.findResourcesByCriteria(user, resourceCriteria);
        for (Resource resource : resourcePageList) {
            if (!resourcePageList.contains(resource.getParentResource())) {
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
                    ResourceOperationSchedule schedule = operationManager.scheduleResourceOperation(user, resource.getId(), "checkConfiguration", 0, 0, 0, 0, new Configuration(), "Scheduled by RHQ WebSphere Server Plugin");
                    // Set the original Resource object because we need the set of tags (which is not loaded by scheduleResourceOperation)
                    schedule.setResource(resource);
                    schedules.add(schedule);
                } else {
                    log.info("checkConfiguration operation not supported on " + resource.getName() + " (" + resource.getId() + ")");
                }
            }
            Thread.sleep(1000);
            List<ResourceOperationSchedule> deferred = new ArrayList<ResourceOperationSchedule>();
            for (ResourceOperationSchedule schedule : schedules) {
                Resource resource = schedule.getResource();
                ResourceOperationHistoryCriteria historyCriteria = new ResourceOperationHistoryCriteria();
                historyCriteria.setPageControl(PageControl.getUnlimitedInstance());
                historyCriteria.addFilterJobId(schedule.getJobId());
                historyCriteria.fetchResults(true);
                PageList<ResourceOperationHistory> historyList = operationManager.findResourceOperationHistoriesByCriteria(user, historyCriteria);
                if (historyList.size() == 1) {
                    ResourceOperationHistory history = historyList.get(0);
                    boolean delete;
                    if (history.getStatus() == OperationRequestStatus.SUCCESS) {
                        if (history.getResults() == null) {
                            // This may happen if the checkConfiguration operation is declared on the resource type,
                            // but not correctly implemented by the resource component.
                            log.error("No results available for operation on " + resource.getName() + " (" + resource.getId() + ")");
                            delete = true;
                        } else {
                            boolean isConfigured = history.getResults().getSimple("isConfigured").getBooleanValue();
                            if (resource.getCurrentAvailability().getAvailabilityType() == AvailabilityType.DOWN && !isConfigured) {
                                log.info("Tagging " + resource.getName() + " (" + resource.getId() + ") as unconfigured");
                                Set<Tag> tags = resource.getTags();
                                tags.add(unconfiguredTag);
                                tagManager.updateResourceTags(user, resource.getId(), tags);
                                resourceManager.disableResources(user, new int[] { resource.getId() });
                            } else if (resource.getCurrentAvailability().getAvailabilityType() == AvailabilityType.DISABLED && isConfigured) {
                                log.info(resource.getName() + " (" + resource.getId() + ") has reappeared in the WebSphere configuration; reenabling it");
                                Set<Tag> tags = resource.getTags();
                                tags.remove(unconfiguredTag);
                                tagManager.updateResourceTags(user, resource.getId(), tags);
                                resourceManager.enableResources(user, new int[] { resource.getId() });
                            }
                            delete = true;
                        }
                    } else if (history.getStatus() == OperationRequestStatus.INPROGRESS) {
                        log.info("Deferring operation on " + resource.getName() + " (" + resource.getId() + "): still in progress");
                        deferred.add(schedule);
                        delete = false;
                    } else {
                        log.error("Operation didn't succeed on " + resource.getName() + " (" + resource.getId() + "): " + history.getStatus());
                        delete = true;
                    }
                    if (delete) {
                        if (log.isDebugEnabled()) {
                            log.debug("Deleting operation history " + history);
                        }
                        operationManager.deleteOperationHistory(user, history.getId(), false);
                    }
                } else if (historyList.size() == 0) {
                    // If we get here, then the ResourceOperationHistory has not been created yet (it is created asynchronously) 
                    log.info("ResourceOperationHistory for " + resource.getName() + " (" + resource.getId() + ") not found yet");
                    deferred.add(schedule);
                } else {
                    log.error("Unexpected result from findResourceOperationHistoriesByCriteria for " + resource.getName() + " (" + resource.getId() + ")");
                }
            }
            schedules = deferred;
        }
    }
    
    public void updateDB2MonitorUsers(ScheduledJobInvocationContext invocation) throws Exception {
        Subject user = LookupUtil.getSubjectManager().getOverlord();
        ResourceManagerLocal resourceManager = LookupUtil.getResourceManager();
        ConfigurationManagerLocal configurationManager = LookupUtil.getConfigurationManager();
        
        ResourceCriteria resourceCriteria = new ResourceCriteria();
        resourceCriteria.addFilterPluginName("WebSphere");
        resourceCriteria.addFilterResourceTypeName("DB2 Monitor");
        resourceCriteria.fetchPluginConfiguration(true);
        resourceCriteria.setPageControl(PageControl.getUnlimitedInstance());
        for (Resource resource : resourceManager.findResourcesByCriteria(user, resourceCriteria)) {
            ResourceConfigurationUpdate rcUpdate = configurationManager.getLatestResourceConfigurationUpdate(user, resource.getId());
            if (rcUpdate == null) {
                log.warn("Couldn't get latest configuration for resource " + resource.getId());
            } else {
                String primaryServer = rcUpdate.getConfiguration().getSimpleValue("primary", null);
                if (primaryServer == null) {
                    log.warn("Unable to determine primary server for DB2 monitor " + resource.getId());
                } else {
                    String database = primaryServer + "/" + rcUpdate.getConfiguration().getSimpleValue("databaseName");
                    for (Property property : config.getList("db2MonitorUsers").getList()) {
                        PropertyMap db2MonitorUser = (PropertyMap)property;
                        Pattern pattern = Pattern.compile(db2MonitorUser.getSimpleValue("databasePattern", null));
                        if (pattern.matcher(database).matches()) {
                            String principal = db2MonitorUser.getSimpleValue("principal", null);
                            String credentials = db2MonitorUser.getSimpleValue("credentials", null);
                            Configuration pluginConfig = resource.getPluginConfiguration();
                            PropertySimple principalProperty = pluginConfig.getSimple("principal");
                            PropertySimple credentialsProperty = pluginConfig.getSimple("credentials");
                            if (!principal.equals(principalProperty.getStringValue()) || !credentials.equals(credentialsProperty.getStringValue())) {
                                log.info("Updating DB2 monitor user for resource " + resource.getId());
                                principalProperty.setStringValue(principal);
                                credentialsProperty.setStringValue(credentials);
                                configurationManager.updatePluginConfiguration(user, resource.getId(), pluginConfig);
                            }
                            break;
                        }
                    }
                }
            }
        }
    }
}
