package be.fgov.kszbcss.rhq.websphere.component.jdbc.db2.pool;

import java.sql.SQLException;

import com.ibm.db2.jcc.DB2ClientRerouteServerList;

public final class ConnectionContext {
    private ConnectionContextImpl impl;
    
    ConnectionContext(ConnectionContextImpl impl) {
        this.impl = impl;
    }

    private synchronized ConnectionContextImpl checkState() {
        if (impl == null) {
            throw new IllegalStateException("ConnectionContext already released");
        } else {
            return impl;
        }
    }
    
    public void testConnection() throws SQLException {
        checkState().testConnection();
    }
    
    public DB2ClientRerouteServerList getClientRerouteServerList() throws SQLException {
        return checkState().getClientRerouteServerList();
    }
    
    public <T> T execute(Query<T> query) throws SQLException {
        return checkState().execute(query);
    }
    
    public synchronized void release() {
        checkState();
        ConnectionContextPool.release(impl);
        impl = null;
    }
}
