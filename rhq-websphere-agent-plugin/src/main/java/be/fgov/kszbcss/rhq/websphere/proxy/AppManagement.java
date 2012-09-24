package be.fgov.kszbcss.rhq.websphere.proxy;

import java.util.Hashtable;
import java.util.Vector;

import javax.management.JMException;

import com.ibm.websphere.management.application.client.AppDeploymentTask;
import com.ibm.websphere.management.exception.ConnectorException;

/**
 * Proxy interface for the <tt>AppManagement</tt> MBean.
 */
public interface AppManagement {
    Vector<AppDeploymentTask> getApplicationInfo(String appName, Hashtable prefs, String workspaceID) throws JMException, ConnectorException;
}
