var resourceCriteria = new ResourceCriteria()
resourceCriteria.addFilterCurrentAvailability(AvailabilityType.DOWN)
resourceCriteria.setPageControl(PageControl.getUnlimitedInstance())
var resources = new java.util.LinkedList()
var resourcePageList = ResourceManager.findResourcesByCriteria(resourceCriteria)
for (var i = 0; i < resourcePageList.size(); i++) {
    var resource = resourcePageList.get(i)
    if (!resources.contains(resource.parentResource)) {
        resources.add(resource)
    }
}

var schedules = new java.util.ArrayList()
while (!resources.empty || !schedules.empty) {
    // Process resources in chunks to avoid server overload and OOM on the client side
    while (!resources.empty && schedules.size() < 6) {
        var resource = resources.removeFirst()
        var opDefCriteria = new OperationDefinitionCriteria()
        opDefCriteria.setPageControl(PageControl.getUnlimitedInstance())
        opDefCriteria.addFilterResourceTypeId(resource.resourceType.id)
        opDefCriteria.addFilterName("checkConfiguration")
        if (OperationManager.findOperationDefinitionsByCriteria(opDefCriteria).size() > 0) {
            println("Scheduling checkConfiguration for " + resource.name + " (" + resource.id + ")")
            schedules.add(OperationManager.scheduleResourceOperation(resource.id, "checkConfiguration", 0, 0, 0, 0, new Configuration(), "Scheduled by uninventory_undeployed_resources.js"))
        } else {
            println("checkConfiguration operation not supported on " + resource.name + " (" + resource.id + ")")
        }
    }
    var deferred = new java.util.ArrayList()
    for (var i = 0; i < schedules.size(); i++) {
        var schedule = schedules.get(i)
        var historyCriteria = new ResourceOperationHistoryCriteria()
        historyCriteria.setPageControl(PageControl.getUnlimitedInstance())
        historyCriteria.addFilterJobId(schedule.jobId)
        historyCriteria.fetchResults(true)
        var historyList = OperationManager.findResourceOperationHistoriesByCriteria(historyCriteria)
        if (historyList.size() == 1) {
            var history = historyList.get(0)
            if (history.status == OperationRequestStatus.SUCCESS) {
                if (history.results == null) {
                    println("No results available for operation on " + schedule.resource.name + " (" + schedule.resource.id + ")")
                } else {
                    if (!history.results.getSimple("isConfigured").booleanValue.booleanValue()) {
                        println("About to uninventory " + schedule.resource.name + " (" + schedule.resource.id + ")")
                        ResourceManager.uninventoryResources([schedule.resource.id])
                    }
                    OperationManager.deleteOperationHistory(history.id, false)
                }
            } else if (history.status == OperationRequestStatus.INPROGRESS) {
                println("Deferring operation on " + schedule.resource.name + " (" + schedule.resource.id + "): still in progress")
                deferred.add(schedule)
            } else {
                println("Operation didn't succeed on " + schedule.resource.name + " (" + schedule.resource.id + "): " + history.status)
            }
        } else {
            println("Unexpected result from findResourceOperationHistoriesByCriteria for " + schedule.resource.name + " (" + schedule.resource.id + ")")
        }
    }
    schedules = deferred
}
