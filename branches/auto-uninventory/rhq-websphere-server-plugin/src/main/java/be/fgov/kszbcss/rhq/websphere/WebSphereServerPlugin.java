package be.fgov.kszbcss.rhq.websphere;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

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
import org.rhq.core.domain.util.PageControl;
import org.rhq.core.domain.util.PageList;
import org.rhq.enterprise.server.operation.OperationManagerLocal;
import org.rhq.enterprise.server.operation.ResourceOperationSchedule;
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
    
    public void autoUninventory() throws Exception {
        Subject user = LookupUtil.getSubjectManager().getOverlord();
        ResourceManagerLocal resourceManager = LookupUtil.getResourceManager();
        OperationManagerLocal operationManager = LookupUtil.getOperationManager();
        
        ResourceCriteria resourceCriteria = new ResourceCriteria();
        resourceCriteria.addFilterCurrentAvailability(AvailabilityType.DOWN);
        resourceCriteria.addFilterPluginName("WebSphere");
        resourceCriteria.fetchParentResource(true);
        resourceCriteria.setPageControl(PageControl.getUnlimitedInstance());
        LinkedList<Resource> resources = new LinkedList<Resource>();
        PageList<Resource> resourcePageList = resourceManager.findResourcesByCriteria(user, resourceCriteria);
        for (Resource resource : resourcePageList) {
            if (!resourcePageList.contains(resource.getParentResource())) {
                resources.add(resource);
            }
        }
        
        List<ResourceOperationSchedule> schedules = new ArrayList<ResourceOperationSchedule>();
        while (!resources.isEmpty() || !schedules.isEmpty()) {
            // Process resources in chunks to avoid server overload and OOM on the client side
            while (!resources.isEmpty() && schedules.size() < 6) {
                Resource resource = resources.removeFirst();
                OperationDefinitionCriteria opDefCriteria = new OperationDefinitionCriteria();
                opDefCriteria.setPageControl(PageControl.getUnlimitedInstance());
                opDefCriteria.addFilterResourceTypeId(resource.getResourceType().getId());
                opDefCriteria.addFilterName("checkConfiguration");
                if (operationManager.findOperationDefinitionsByCriteria(user, opDefCriteria).size() > 0) {
                    log.info("Scheduling checkConfiguration for " + resource.getName() + " (" + resource.getId() + ")");
                    schedules.add(operationManager.scheduleResourceOperation(user, resource.getId(), "checkConfiguration", 0, 0, 0, 0, new Configuration(), "Scheduled by uninventory_undeployed_resources.js"));
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
                            log.info("No results available for operation on " + schedule.getResource().getName() + " (" + schedule.getResource().getId() + ")");
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
                } else {
                    log.error("Unexpected result from findResourceOperationHistoriesByCriteria for " + schedule.getResource().getName() + " (" + schedule.getResource().getId() + ")");
                }
            }
            schedules = deferred;
        }
    }
}
