package be.fgov.kszbcss.rhq.websphere.proxy;

import javax.management.JMException;

import com.ibm.websphere.hamanager.jmx.GroupMemberData;
import com.ibm.websphere.management.exception.ConnectorException;
import com.ibm.wsspi.hamanager.GroupName;

public interface HAManager {
    GroupName createGroupName(String groupNameCSV) throws JMException, ConnectorException;
    GroupMemberData[] retrieveGroupMembers(GroupName groupName) throws JMException, ConnectorException;
}
