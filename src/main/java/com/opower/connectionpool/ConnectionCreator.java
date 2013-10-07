package com.opower.connectionpool;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * This class is used to create direct connections to the database.  This class will be used by the {@link ConnectionPool}, and should not return proxies, 
 * but raw {@link Connection} objects.
 * 
 * This class is broken out from the result of the Connection Pool to primarily facilitate unit testing.  The main implementation is {@link com.opower.connectionpool.connection.MortonianConnectionCreator},
 * but alternate implementations can be used as mocks for unit testing.  
 *
 */
public interface ConnectionCreator {

    /**
     * @param connectionConfig a {@link ConnectionConfig} specifying how to connect to the database 
     * @return a raw {@link Connection} (not a proxy)
     * @throws SQLException if the connection cannot be made.
     */
    public Connection createConnection(ConnectionConfig connectionConfig) throws SQLException;
}
