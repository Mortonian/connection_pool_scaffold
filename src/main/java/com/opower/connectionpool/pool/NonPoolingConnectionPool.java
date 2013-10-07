package com.opower.connectionpool.pool;

import java.sql.Connection;
import java.sql.SQLException;

import org.apache.log4j.Logger;

import com.opower.connectionpool.ConnectionCreator;
import com.opower.connectionpool.ConnectionConfig;
import com.opower.connectionpool.ConnectionPool;

public class NonPoolingConnectionPool implements ConnectionPool {

    private static Logger _log = Logger.getLogger(NonPoolingConnectionPool.class);
    
    public ConnectionConfig _desciptor;
    public ConnectionCreator _creator;
    
    public NonPoolingConnectionPool(ConnectionConfig desciptor, ConnectionCreator creator) {
        _desciptor = desciptor;
        _creator = creator;
    }

    @Override
    public Connection getConnection() throws SQLException {
        _log.debug("Creating connection");
        return _creator.createConnection(_desciptor);
    }

    @Override
    public void releaseConnection(Connection connection) throws SQLException {
        _log.debug("Closing released connection");
        connection.close();
    }
    
}