package com.ibm.db2.jcc;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

public class DB2SimpleDataSource extends DB2BaseDataSource implements DataSource {
    public Connection getConnection() throws SQLException {
        return null;
    }

    public Connection getConnection(String username, String password) throws SQLException {
        return null;
    }

    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return false;
    }

    public <T> T unwrap(Class<T> iface) throws SQLException {
        return null;
    }
}
