/*
 * RHQ WebSphere Plug-in
 * Copyright (C) 2012-2013 Crossroads Bank for Social Security
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

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.rhq.core.clientapi.agent.discovery.DiscoveryAgentService;
import org.rhq.core.domain.auth.Subject;
import org.rhq.core.domain.configuration.Configuration;
import org.rhq.core.domain.configuration.Property;
import org.rhq.core.domain.configuration.PropertyMap;
import org.rhq.core.domain.configuration.PropertySimple;
import org.rhq.core.domain.configuration.ResourceConfigurationUpdate;
import org.rhq.core.domain.criteria.ResourceCriteria;
import org.rhq.core.domain.discovery.AvailabilityReport;
import org.rhq.core.domain.discovery.AvailabilityReport.Datum;
import org.rhq.core.domain.measurement.AvailabilityType;
import org.rhq.core.domain.resource.Resource;
import org.rhq.core.domain.resource.composite.ResourceAvailabilitySummary;
import org.rhq.core.domain.tagging.Tag;
import org.rhq.core.domain.util.PageControl;
import org.rhq.core.domain.util.PageList;
import org.rhq.enterprise.server.agentclient.AgentClient;
import org.rhq.enterprise.server.configuration.ConfigurationManagerLocal;
import org.rhq.enterprise.server.core.AgentManagerLocal;
import org.rhq.enterprise.server.plugin.pc.ScheduledJobInvocationContext;
import org.rhq.enterprise.server.plugin.pc.ServerPluginComponent;
import org.rhq.enterprise.server.plugin.pc.ServerPluginContext;
import org.rhq.enterprise.server.resource.ResourceManagerLocal;
import org.rhq.enterprise.server.tagging.TagManagerLocal;
import org.rhq.enterprise.server.util.LookupUtil;

public class WebSphereServerPlugin implements ServerPluginComponent {
	private static final Log LOG = LogFactory.getLog(WebSphereServerPlugin.class);
    
    private Configuration config;
    
    @Override
	public void initialize(ServerPluginContext context) throws Exception {
        config = context.getPluginConfiguration();
    }

    @Override
	public void start() {
    }

    @Override
	public void stop() {
    }

    @Override
	public void shutdown() {
    }
    
	/**
	 * auto inventory resources in two phases
	 * 
	 * The sweep phase uninventories all resources which are tagged as 'unconfigured' for a certain time. The mark phase
	 * checks for all unavailable resources if they're still configured and adds the 'unconfigured' tag if so.
	 * 
	 * @param invocation
	 *            job context
	 */
	public void autoUninventory(ScheduledJobInvocationContext invocation) {
		try {
			Subject user = LookupUtil.getSubjectManager().getOverlord();
			TagManagerLocal tagManager = LookupUtil.getTagManager();
			ResourceManagerLocal resourceManager = LookupUtil.getResourceManager();

			int uninventoryDelay =
					Integer.parseInt(invocation.getJobDefinition().getCallbackData().getProperty("uninventoryDelay"));

			// addTags either creates a new tag or returns an existing tag
			Tag unconfiguredTag =
					tagManager.addTags(user, Collections.singleton(new Tag("websphere", null, "unconfigured")))
							.iterator().next();

			// Resources to check
			LinkedList<Resource> resources = new LinkedList<Resource>();

			// Sweep - search resources that can be uninventoried
			ResourceCriteria resourceCriteria = new ResourceCriteria();
			resourceCriteria.addFilterCurrentAvailability(AvailabilityType.DISABLED);
			resourceCriteria.addFilterTag(unconfiguredTag);
			resourceCriteria.fetchTags(true);
			resourceCriteria.setPageControl(PageControl.getUnlimitedInstance());
			PageList<Resource> resourcePageList = resourceManager.findResourcesByCriteria(user, resourceCriteria);
			for (Resource resource : resourcePageList) {
				ResourceAvailabilitySummary availability =
						resourceManager.getAvailabilitySummary(user, resource.getId());
				if ((System.currentTimeMillis() - availability.getLastChange().getTime()) / 60000 > uninventoryDelay) {
					LOG.debug("Removing unconfigured tag from resource " + resource.getName() + " (" + resource.getId()
							+ ") to work around an issue in RHQ 4.5.1");
					Set<Tag> tags = resource.getTags();
					tags.remove(unconfiguredTag);
					tagManager.updateResourceTags(user, resource.getId(), tags);
					LOG.info("About to uninventory " + resource.getName() + " (" + resource.getId() + ")");
					resourceManager.uninventoryResources(user, new int[] { resource.getId() });
				} else {
					LOG.debug("Resource " + resource.getName() + " (" + resource.getId()
							+ ") is tagged as unconfigured; force configuration check");
					resources.add(resource);
				}
			}

			// Search for WebSphere resources that are down and check if they have been unconfigured
			// AvailabilityType.MISSING results seems to have been converted to DOWN at this point
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

			// check and mark - for all resources that were already marked as unconfigured and for the unavailable resources
			checkAndMarkUnconfiguredResources(resources, unconfiguredTag);
		} catch (RuntimeException e) {
			// need to catch all exceptions here because letting the exception pass will unschedule the job forevermore
			LOG.error("Exception during autoUninventory of resources", e);
		}
    }
	
	private void checkAndMarkUnconfiguredResources(LinkedList<Resource> resources, final Tag unconfiguredTag) {
		final Subject user = LookupUtil.getSubjectManager().getOverlord();
		final TagManagerLocal tagManager = LookupUtil.getTagManager();
		final ResourceManagerLocal resourceManager = LookupUtil.getResourceManager();

		// To accelerate things, we schedule a certain number of operations in parallel.
		// JBoss 6 has not yet ManagedExecutorService, so we're using unmanaged threads.
		ExecutorService executorService = Executors.newFixedThreadPool(6);
		for (final Resource resource : resources) {
			if (resource.getResourceType().isSupportsMissingAvailabilityType()) {
				executorService.submit(new Runnable() {
					@Override
					public void run() {
						try {
							// the synchronous calls to retrieve availability don't convert AvailabilityType.MISSING to
							// DOWN, so we can make use of it to detect unconfigured resources
							if (resource.getCurrentAvailability().getAvailabilityType() != AvailabilityType.DISABLED) {
								AvailabilityType currentAvailability = getCurrentAvailaibility(resource);
								if (currentAvailability == AvailabilityType.MISSING) {
									LOG.info("Tagging " + resource.getName() + " (" + resource.getId()
											+ ") as unconfigured");
									Set<Tag> tags = resource.getTags();
									tags.add(unconfiguredTag);
									// using synchronized, because at least resourceManager didn't seem threadsafe
									synchronized (tagManager) {
										tagManager.updateResourceTags(user, resource.getId(), tags);
									}
									synchronized (resourceManager) {
										resourceManager.disableResources(user, new int[] { resource.getId() });
									}
								}
							} else {
								AvailabilityType currentAvailability = getCurrentAvailaibility(resource);
								if (currentAvailability != AvailabilityType.UNKNOWN
										&& currentAvailability != AvailabilityType.MISSING) {
									LOG.info(resource.getName() + " (" + resource.getId()
											+ ") has reappeared in the WebSphere configuration; reenabling it");
									Set<Tag> tags = resource.getTags();
									tags.remove(unconfiguredTag);
									synchronized (tagManager) {
										tagManager.updateResourceTags(user, resource.getId(), tags);
									}
									synchronized (resourceManager) {
										resourceManager.enableResources(user, new int[] { resource.getId() });
									}
								}
							}
						} catch (RuntimeException e) {
							LOG.error("Exception during availability check of resource " + resource.getName() + "("
									+ resource.getId() + ")");
						}
					}
				});
			}
		}
		try {
			executorService.shutdown();
			executorService.awaitTermination(5, TimeUnit.MINUTES);
		} catch (InterruptedException e) {
			throw new IllegalStateException("Interrupted during availability check of autoUninventory", e);
		}
	}

	private AvailabilityType getCurrentAvailaibility(Resource resource) {
		Subject user = LookupUtil.getSubjectManager().getOverlord();
		AgentManagerLocal agentManager = LookupUtil.getAgentManager();
		AgentClient agentClient = agentManager.getAgentClient(user, resource.getId());
		DiscoveryAgentService discoveryAgentService = agentClient.getDiscoveryAgentService();
		AvailabilityReport currentAvailability = discoveryAgentService.getCurrentAvailability(resource, false);
		// the discoveryAgentService returns the live availability of the requested resource and the last
		// known availabilities of any child resources
		List<Datum> datums = currentAvailability.getResourceAvailability();
		for(Datum datum : datums) {
			if(datum.getResourceId() == resource.getId()) {
				return datum.getAvailabilityType();
			}
		}
		throw new IllegalStateException("Agent didn't respond with requested resource's availability. Resource: "
				+ resource.toString());
	}

	public void updateDB2MonitorUsers(ScheduledJobInvocationContext invocation) {
		try {
			Subject user = LookupUtil.getSubjectManager().getOverlord();
			ResourceManagerLocal resourceManager = LookupUtil.getResourceManager();
			ConfigurationManagerLocal configurationManager = LookupUtil.getConfigurationManager();

			ResourceCriteria resourceCriteria = new ResourceCriteria();
			resourceCriteria.addFilterPluginName("WebSphere");
			resourceCriteria.addFilterResourceTypeName("DB2 Monitor");
			resourceCriteria.fetchPluginConfiguration(true);
			resourceCriteria.setPageControl(PageControl.getUnlimitedInstance());
			for (Resource resource : resourceManager.findResourcesByCriteria(user, resourceCriteria)) {
				ResourceConfigurationUpdate rcUpdate =
						configurationManager.getLatestResourceConfigurationUpdate(user, resource.getId());
				if (rcUpdate == null) {
					LOG.warn("Couldn't get latest configuration for resource " + resource.getId());
				} else {
					String primaryServer = rcUpdate.getConfiguration().getSimpleValue("primary", null);
					if (primaryServer == null) {
						LOG.warn("Unable to determine primary server for DB2 monitor " + resource.getId());
					} else {
						String database =
								primaryServer + "/" + rcUpdate.getConfiguration().getSimpleValue("databaseName");
						for (Property property : config.getList("db2MonitorUsers").getList()) {
							PropertyMap db2MonitorUser = (PropertyMap) property;
							Pattern pattern = Pattern.compile(db2MonitorUser.getSimpleValue("databasePattern", null));
							if (pattern.matcher(database).matches()) {
								String principal = db2MonitorUser.getSimpleValue("principal", null);
								String credentials = db2MonitorUser.getSimpleValue("credentials", null);
								Configuration pluginConfig = resource.getPluginConfiguration();
								PropertySimple principalProperty = pluginConfig.getSimple("principal");
								PropertySimple credentialsProperty = pluginConfig.getSimple("credentials");
								if (!principal.equals(principalProperty.getStringValue())
										|| !credentials.equals(credentialsProperty.getStringValue())) {
									LOG.info("Updating DB2 monitor user for resource " + resource.getId());
									principalProperty.setStringValue(principal);
									credentialsProperty.setStringValue(credentials);
									configurationManager
											.updatePluginConfiguration(user, resource.getId(), pluginConfig);
								}
								break;
							}
						}
					}
				}
			}
		} catch (RuntimeException e) {
			// need to catch all exceptions here because letting the exception pass will unschedule the job forevermore
			LOG.error("Exception during updateDB2MonitorUsers scheduled job", e);
		}
    }
}
