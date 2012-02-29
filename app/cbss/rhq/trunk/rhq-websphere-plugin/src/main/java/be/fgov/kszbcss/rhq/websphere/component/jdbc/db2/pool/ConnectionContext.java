package be.fgov.kszbcss.rhq.websphere.component.jdbc.db2.pool;

import java.sql.SQLException;

import com.ibm.db2.jcc.DB2ClientRerouteServerList;

public final class ConnectionContext {
    private ConnectionContextImpl impl;
    
    ConnectionContext(ConnectionContextImpl impl) {
        this.impl = impl;
    }

    public void testConnection() throws SQLException {
        impl.testConnection();
    }
    
    public DB2ClientRerouteServerList getClientRerouteServerList() throws SQLException {
        return impl.getClientRerouteServerList();
    }
    
    public <T> T execute(Query<T> query) throws SQLException {
        return execute(query);
    }
    
    public void release() {
        if (impl == null) {
            throw new IllegalStateException("ConnectionContext already released");
        } else {
            ConnectionContextPool.release(impl);
            impl = null;
        }
    }
}
