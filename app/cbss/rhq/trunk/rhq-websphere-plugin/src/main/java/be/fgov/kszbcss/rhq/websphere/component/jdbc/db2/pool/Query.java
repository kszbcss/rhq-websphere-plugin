package be.fgov.kszbcss.rhq.websphere.component.jdbc.db2.pool;

import java.sql.Connection;
import java.sql.SQLException;

public interface Query<T> {
    T execute(Connection connection) throws SQLException;
}
