package com.opower.connectionpool;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * TODO: This class has one implementation, the {@link com.opower.connectionpool.pool.MortonianConnectionPool}.
 * 
 * That implementation uses a {@link ConnectionConfig}, {@link ConnectionCreator}, and a {@link PoolConfig} 
 * together to defined the expected connection pooling behavior. 
 */
public interface ConnectionPool {

    /**
     * Gets a connection from the connection pool.
     * 
     * @return a valid connection from the pool.
     */
    Connection getConnection() throws SQLException;

    /**
     * Releases a connection back into the connection pool.
     * 
     * @param connection the connection to return to the pool
     * @throws java.sql.SQLException
     */
    void releaseConnection(Connection connection) throws SQLException;
}
