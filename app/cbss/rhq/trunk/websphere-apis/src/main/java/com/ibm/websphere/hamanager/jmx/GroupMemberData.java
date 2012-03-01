package com.ibm.websphere.hamanager.jmx;

import java.util.Map;

@SuppressWarnings("unchecked")
public interface GroupMemberData {
    String getFullyQualifiedServerName();
    String getCellName();
    String getNodeName();
    String getServerName();
    GroupMemberState getMemberState();
    Map getMemberProperties();
}
