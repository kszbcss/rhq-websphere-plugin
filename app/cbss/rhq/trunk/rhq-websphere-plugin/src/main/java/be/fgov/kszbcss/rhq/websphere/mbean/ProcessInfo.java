package be.fgov.kszbcss.rhq.websphere.mbean;

import javax.management.ObjectName;

import be.fgov.kszbcss.rhq.websphere.WebSphereServer;

import com.ibm.websphere.management.AdminClient;
import com.ibm.websphere.management.exception.ConnectorException;

public class ProcessInfo {
    private final WebSphereServer server;
    private boolean initialized;
    private String cell;
    private String node;
    private String process;
    private String processType;
    
    ProcessInfo(WebSphereServer server) {
        this.server = server;
    }
    
    synchronized void init() throws ConnectorException {
        if (!initialized) {
            ObjectName serverMBean = server.getAdminClient().getServerMBean();
            cell = serverMBean.getKeyProperty("cell");
            node = serverMBean.getKeyProperty("node");
            process = serverMBean.getKeyProperty("process");
            processType = serverMBean.getKeyProperty("processType");
            initialized = true;
        }
    }

    // TODO: review this
    public WebSphereServer getServer() {
        return server;
    }

    public String getCell() throws ConnectorException {
        init();
        return cell;
    }

    public String getNode() throws ConnectorException {
        init();
        return node;
    }

    public String getProcess() throws ConnectorException {
        init();
        return process;
    }

    public String getProcessType() throws ConnectorException {
        init();
        return processType;
    }

    AdminClient getAdminClient() throws ConnectorException {
        return server.getAdminClient();
    }
}
