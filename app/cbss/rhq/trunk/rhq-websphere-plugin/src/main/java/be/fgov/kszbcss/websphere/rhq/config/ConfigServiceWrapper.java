package be.fgov.kszbcss.websphere.rhq.config;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.management.JMException;
import javax.management.ObjectName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import be.fgov.kszbcss.websphere.rhq.mbean.MBeanClient;

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
    private final ReadWriteLock sessionLock = new ReentrantReadWriteLock();
    private boolean destroyed;
    private Session session;
    
    ConfigServiceWrapper(MBeanClient configServiceMBeanClient, ConfigRepository configRepository) {
        // TODO: do this in a smarter way; we should be able to get back from the proxy to the MBeanClient
        this.configServiceMBeanClient = configServiceMBeanClient;
        this.configService = configServiceMBeanClient.getProxy(ConfigService.class);
        this.configRepository = configRepository;
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
    
    public ObjectName[] resolve(final String containmentPath) throws JMException, ConnectorException {
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
        discardSession(false);
    }
    
    void destroy() {
        discardSession(true);
    }
}
