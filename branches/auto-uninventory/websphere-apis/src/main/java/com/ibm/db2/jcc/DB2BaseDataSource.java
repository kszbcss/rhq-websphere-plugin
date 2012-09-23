package com.ibm.db2.jcc;

import java.io.PrintWriter;
import java.sql.SQLException;

public class DB2BaseDataSource {
    public int getLoginTimeout() throws SQLException {
        return 0;
    }

    public PrintWriter getLogWriter() throws SQLException {
        return null;
    }

    public void setLoginTimeout(int seconds) throws SQLException {
    }

    public void setLogWriter(PrintWriter out) throws SQLException {
    }
    
    public DB2ClientRerouteServerList getClientRerouteServerList() {
        return null;
    }
}
