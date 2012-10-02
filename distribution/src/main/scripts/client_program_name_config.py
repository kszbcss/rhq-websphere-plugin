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

for cluster in getConfigObjects("/ServerCluster:/"):
    clusterName = cluster.getAttribute("name")
    print "    Processing cluster %s" % clusterName
    useWasServerName = 1
    maxWasServerNameLen = 0
    maxRhqDB2ServerIdentifierLen = 0
    for member in cluster.getChildren("members"):
        nodeName = member.getAttribute("nodeName")
        serverName = member.getAttribute("memberName")
        if len(serverName) > maxWasServerNameLen:
            maxWasServerNameLen = len(serverName)
        print "      Found member %s/%s" % (nodeName, serverName)
        variableMap = getUniqueConfigObject("/Node:%s/Server:%s/VariableMap:/" % (nodeName, serverName))
        rhqDB2ServerIdentifier = None
        for entry in variableMap.getChildren("entries"):
            if entry.getAttribute("symbolicName") == "RHQ_DB2MON_SERVER_IDENTIFIER":
                rhqDB2ServerIdentifier = entry.getAttribute("value")
                useWasServerName = 0
                break
        if rhqDB2ServerIdentifier == None:
            print "        RHQ_DB2MON_SERVER_IDENTIFIER not set"
        else:
            print "        RHQ_DB2MON_SERVER_IDENTIFIER=%s" % rhqDB2ServerIdentifier
            if len(rhqDB2ServerIdentifier) > maxRhqDB2ServerIdentifierLen:
                maxRhqDB2ServerIdentifierLen = len(rhqDB2ServerIdentifier)
    if useWasServerName:
        serverIdentifierVariable = "WAS_SERVER_NAME"
        maxIdentifierLen = maxWasServerNameLen
    else:
        serverIdentifierVariable = "RHQ_DB2MON_SERVER_IDENTIFIER"
        maxIdentifierLen = maxRhqDB2ServerIdentifierLen
    print "      Server identifier variable is %s (with maximum length %d)" % (serverIdentifierVariable, maxIdentifierLen)
    for dataSource in getConfigObjects("/ServerCluster:%s/JDBCProvider:/DataSource:/" % clusterName):
        if dataSource.getAttribute("datasourceHelperClassname") == "com.ibm.websphere.rsadapter.DB2UniversalDataStoreHelper":
            jndiName = dataSource.getAttribute("jndiName")
            print "      Processing DB2 data source %s" % jndiName
            if jndiName.startswith("jdbc/"):
                baseName = jndiName[5:]
            else:
                baseName = jndiName
            baseName = baseName.lower()
            # APPL_NAME (which is the variable that clientProgramName sets on the server side)
            # is truncated at 20 characters
            if maxIdentifierLen + len(baseName) + 1 <= 20:
                suggestedClientProgramName = "${%s}:%s" % (serverIdentifierVariable, baseName)
            else:
                suggestedClientProgramName = None
            props = PropertySet(dataSource.getChild("propertySet"))
            currentClientProgramName = props.get("clientProgramName")
            if currentClientProgramName == None:
                if suggestedClientProgramName == None:
                    print "[!]     Please set clientProgramName manually"
                else:
                    print "(i)     Setting clientProgramName to %s" % suggestedClientProgramName
                    props.set("clientProgramName", suggestedClientProgramName, "Specifies the application ID for the connection.", "java.lang.String")
            else:
                if suggestedClientProgramName == None:
                    print "        clientProgramName already set manually: %s" % currentClientProgramName
                elif currentClientProgramName != suggestedClientProgramName:
                    print "[!]     Current clientProgramName (%s) doesn't match expected one (%s)" % (currentClientProgramName, suggestedClientProgramName)
                else:
                    print "        clientProgramName already configured (with expected value)"

if AdminConfig.hasChanges():
    if raw_input("Do you want to save these changes [y/n]? ").lower() == "y":
        AdminConfig.save()
else:
    print "No changes need to be saved."
