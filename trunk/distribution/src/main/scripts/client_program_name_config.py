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

AdminConfig.save()
