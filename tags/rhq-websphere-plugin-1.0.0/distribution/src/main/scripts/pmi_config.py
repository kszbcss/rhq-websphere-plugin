#
# RHQ WebSphere Plug-in
# Copyright (C) 2012 Crossroads Bank for Social Security
# All rights reserved.
#
# This program is free software; you can redistribute it and/or modify
# it under the terms of the GNU General Public License, version 2, as
# published by the Free Software Foundation, and/or the GNU Lesser
# General Public License, version 2.1, also as published by the Free
# Software Foundation.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
# GNU General Public License and the GNU Lesser General Public License
# for more details.
#
# You should have received a copy of the GNU General Public License
# and the GNU Lesser General Public License along with this program;
# if not, write to the Free Software Foundation, Inc.,
# 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
#

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
