package be.fgov.kszbcss.rhq.websphere;

import javax.management.ObjectName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ibm.websphere.management.AdminClient;
import com.ibm.websphere.management.exception.ConnectorException;

import be.fgov.kszbcss.rhq.websphere.connector.AdminClientProvider;

class ProcessIdentityValidator implements AdminClientProvider {
    private static final Log log = LogFactory.getLog(ProcessIdentityValidator.class);
    
    private final AdminClientProvider parent;
    private AdminClient adminClient;
    private boolean initialized;
    private String cell;
    private String node;
    private String server;
    private String processType;
    
    ProcessIdentityValidator(AdminClientProvider parent, String cell,
            String node, String server, String processType) {
        this.parent = parent;
        this.cell = cell;
        this.node = node;
        this.server = server;
        this.processType = processType;
    }

    private synchronized void init() throws ConnectorException {
        if (adminClient == null) {
            adminClient = parent.createAdminClient();
        }
        if (!initialized) {
            ObjectName serverMBean = adminClient.getServerMBean();
            String actualCell = serverMBean.getKeyProperty("cell");
            String actualNode = serverMBean.getKeyProperty("node");
            String actualServer = serverMBean.getKeyProperty("process");
            String actualProcessType = serverMBean.getKeyProperty("processType");
            compare("cell name", cell, actualCell);
            compare("node name", node, actualNode);
            compare("server name", server, actualServer);
            compare("process type", processType, actualProcessType);
            cell = actualCell;
            node = actualNode;
            server = actualServer;
            processType = actualProcessType;
            initialized = true;
        }
    }
    
    private static void compare(String property, String expected, String actual) throws ConnectorException {
        if (log.isDebugEnabled()) {
            log.debug("Checking " + property + ": expected=" + (expected == null ? "<unspecified>" : expected) + ", actual=" + actual);
        }
        if (expected != null && !actual.equals(expected)) {
            throw new ConnectorException("The WebSphere process doesn't have the expected " + property + ": expected=" + expected + ", actual=" + actual);
        }
    }

    String getCell() throws ConnectorException {
        init();
        return cell;
    }

    String getNode() throws ConnectorException {
        init();
        return node;
    }

    String getServer() throws ConnectorException {
        init();
        return server;
    }

    String getProcessType() throws ConnectorException {
        init();
        return processType;
    }

    public AdminClient createAdminClient() throws ConnectorException {
        init();
        return adminClient;
    }
}
