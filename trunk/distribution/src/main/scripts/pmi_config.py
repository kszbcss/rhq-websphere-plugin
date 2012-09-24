import sys
sys.modules['AdminConfig'] = AdminConfig

from wsadminlib import *

def enable_pmi_stats(module, id):
    enable = module.getAttribute("enable")
    enabled = []
    if len(enable) > 0:
        enabled = enable.split(",")
    if not id in enabled:
        print "Enabling statistic " + id + " for PMI module " + module.getAttribute("moduleName")
        enabled.append(id)
        module.setAttribute("enable", ",".join(enabled))
    for subModule in module.getChildren("pmimodules"):
        enable_pmi_stats(subModule, id)

nodeName = sys.argv[0]
serverName = sys.argv[1]

getUniqueConfigObject("/Node:" + nodeName + "/Server:" + serverName + "/PMIService:/").setAttribute("statisticSet", "custom")
pmiRoot = getUniqueConfigObject("/Node:" + nodeName + "/Server:" + serverName + "/PMIModule:/")
for module in pmiRoot.getChildren("pmimodules"):
    moduleName = module.getAttribute("moduleName")
    if moduleName == "threadPoolModule":
        enable_pmi_stats(module, "3") # ActiveCount
    elif moduleName == "webAppModule":
        enable_pmi_stats(module, "12") # ConcurrentRequests
    elif moduleName == "beanModule":
        enable_pmi_stats(module, "18") # ActiveMethodCount

AdminConfig.save()
