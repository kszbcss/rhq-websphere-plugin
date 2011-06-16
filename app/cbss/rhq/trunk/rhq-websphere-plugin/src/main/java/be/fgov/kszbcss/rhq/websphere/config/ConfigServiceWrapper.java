package be.fgov.kszbcss.rhq.websphere.config;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.management.JMException;
import javax.management.ObjectName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import be.fgov.kszbcss.rhq.websphere.mbean.MBeanClient;
import be.fgov.kszbcss.rhq.websphere.proxy.ConfigService;

import com.ibm.websphere.management.Session;
import com.ibm.websphere.management.exception.ConnectorException;

public class ConfigServiceWrapper {
    private interface ConfigServiceAction<T> {
        T execute(ConfigService configService, Session session) throws JMException, ConnectorException;
    };
    
    private static final Log log = LogFactory.getLog(ConfigServiceWrapper.class);
    
    private final MBeanClient configServiceMBeanClient;
    private final ConfigService configService;
    private final ConfigRepository configRepository;
    private final Path root;
    private final ReadWriteLock sessionLock = new ReentrantReadWriteLock();
    private boolean destroyed;
    private Session session;
    
    ConfigServiceWrapper(MBeanClient configServiceMBeanClient, ConfigRepository configRepository) {
        // TODO: do this in a smarter way; we should be able to get back from the proxy to the MBeanClient
        this.configServiceMBeanClient = configServiceMBeanClient;
        this.configService = configServiceMBeanClient.getProxy(ConfigService.class);
        this.configRepository = configRepository;
        root = new RootPath(this);
    }
    
    /**
     * Get the WebSphere version. The returned value is actually the version number of the deployment
     * manager (which may differ from the version number of the application servers).
     * 
     * @return
     * @throws JMException
     * @throws ConnectorException
     */
    public String getWebSphereVersion() throws JMException, ConnectorException {
        return configServiceMBeanClient.getObjectName(false).getKeyProperty("version");
    }
    
    private <T> T execute(ConfigServiceAction<T> action) throws JMException, ConnectorException {
        // Note: a read lock can't be upgraded to a write lock, so we need to acquire a write
        // lock first.
        Lock readLock = sessionLock.readLock();
        Lock writeLock = sessionLock.writeLock();
        try {
            writeLock.lockInterruptibly();
            try {
                if (destroyed) {
                    throw new IllegalStateException("Object already destroyed; not accepting any new requests");
                }
                if (session == null) {
                    session = new Session("rhq-websphere-plugin", false);
                    if (log.isDebugEnabled()) {
                        log.debug("New session created: " + session);
                    }
                }
                readLock.lockInterruptibly();
            } finally {
                writeLock.unlock();
            }
            try {
                return action.execute(configService, session);
            } finally {
                readLock.unlock();
            }
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new ConnectorException("Interrupted"); // TODO: probably we should define a proper exception type to be used by this class
        }
    }
    
    public Path path(String type, String name) {
        return root.path(type, name);
    }
    
    public Path path(String type) {
        return root.path(type);
    }
    
    public Path cell() {
        // TODO: we may want to insert the cell name here
        return path("Cell");
    }
    
    public Path node(String nodeName) {
        return cell().path("Node", nodeName);
    }
    
    public Path server(String nodeName, String serverName) {
        return node(nodeName).path("Server", serverName);
    }
    
    public Path allScopes(String nodeName, String serverName) throws JMException, ConnectorException {
        Path cell = cell();
        Path node = cell.path("Node", nodeName);
        Path server = node.path("Server", serverName);
        ObjectName serverObject = server.resolveSingle();
        Path cluster = cell.path("ServerCluster", (String)getAttribute(serverObject, "clusterName"));
        // Order is important here: we return objects with higher precedence first
        return new PathGroup(server, cluster, node, cell);
    }
    
    ObjectName[] resolve(final String containmentPath) throws JMException, ConnectorException {
        return execute(new ConfigServiceAction<ObjectName[]>() {
            public ObjectName[] execute(ConfigService configService, Session session) throws JMException, ConnectorException {
                return configService.resolve(session, containmentPath);
            }
        });
    }
    
    public Object getAttribute(final ObjectName parent, final String attributeName) throws JMException, ConnectorException {
        return execute(new ConfigServiceAction<Object>() {
            public Object execute(ConfigService configService, Session session) throws JMException, ConnectorException {
                return configService.getAttribute(session, parent, attributeName);
            }
        });
    }
    
    public String[] listResourceNames(String parent, int type, int depth) throws JMException, ConnectorException {
        return configRepository.listResourceNames(parent, type, depth);
    }

    public byte[] extract(String docURI) throws JMException, ConnectorException {
        return configRepository.extract(docURI);
    }
    
    private void discardSession(boolean destroy) {
        Lock writeLock = sessionLock.writeLock();
        writeLock.lock();
        try {
            if (session != null) {
                if (log.isDebugEnabled()) {
                    log.debug("Discarding session " + session);
                }
                try {
                    configService.discard(session);
                } catch (Exception ex) {
                    log.warn("Unexpected exception when discarding workspace " + session, ex);
                }
            }
            if (destroy) {
                destroyed = true;
            }
        } finally {
            writeLock.unlock();
        }
    }
    
    void refresh() {
        // There seems to be no JMX operation that allows to refresh the workspace (I'm wondering
        // how the admin console actually does this...), so we simply discard the session. Next time
        // a ConfigService method is called, a new session will be created automatically.
        discardSession(false);
    }
    
    void destroy() {
        discardSession(true);
    }
}
