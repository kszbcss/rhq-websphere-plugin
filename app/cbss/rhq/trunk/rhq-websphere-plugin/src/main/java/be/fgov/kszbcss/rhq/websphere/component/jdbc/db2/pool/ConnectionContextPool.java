package be.fgov.kszbcss.rhq.websphere.component.jdbc.db2.pool;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import be.fgov.kszbcss.rhq.websphere.component.jdbc.db2.DB2MonitorComponent;

/**
 * Connection pool for {@link DB2MonitorComponent}. Note that it is not a usual connection pool
 * because it will manage a single connection per connection configuration. This allows multiple
 * {@link DB2MonitorComponent} instances to share connections. It is not possible to execute queries
 * concurrently but for monitoring purposes this is not necessary (and probably not even desirable).
 */
public final class ConnectionContextPool {
    private static final Log log = LogFactory.getLog(ConnectionContextPool.class);
    
    private static final Map<Map<String,Object>,ConnectionContextImpl> contexts = new HashMap<Map<String,Object>,ConnectionContextImpl>();
    
    public synchronized static ConnectionContext getConnectionContext(Map<String,Object> properties) {
        ConnectionContextImpl impl = contexts.get(properties);
        if (impl == null) {
            impl = new ConnectionContextImpl(properties);
            contexts.put(properties, impl);
            log.info("Created connection context for properties " + properties);
        }
        impl.refCounter++;
        dump();
        return new ConnectionContext(impl);
    }
    
    synchronized static void release(ConnectionContextImpl impl) {
        if (--impl.refCounter == 0) {
            impl.destroy();
            boolean removed = false;
            for (Iterator<Map.Entry<Map<String,Object>,ConnectionContextImpl>> it = contexts.entrySet().iterator(); it.hasNext(); ) {
                Map.Entry<Map<String,Object>,ConnectionContextImpl> entry = it.next();
                if (entry.getValue() == impl) {
                    it.remove();
                    log.info("Destroyed connection context for properties " + entry.getKey());
                    removed = true;
                    break;
                }
            }
            if (!removed) {
                throw new IllegalStateException("Failed to remove connection context");
            }
        }
        dump();
    }
    
    private static void dump() {
        if (log.isDebugEnabled()) {
            StringBuilder buffer = new StringBuilder("Dump of connection context pool:");
            int i = 0;
            for (Map.Entry<Map<String,Object>,ConnectionContextImpl> entry : contexts.entrySet()) {
                buffer.append("\n[" + i++ + "] properties=" + entry.getKey() + ", refCounter=" + entry.getValue().refCounter);
            }
            log.debug(buffer.toString());
        }
    }
}
