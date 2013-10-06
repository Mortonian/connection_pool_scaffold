package com.opower.connectionpool.pool;

import java.sql.Connection;
import java.sql.SQLException;

import com.opower.connectionpool.ConnectionCreator;
import com.opower.connectionpool.ConnectionDescriptor;
import com.opower.connectionpool.ConnectionPool;

public class NonPoolingConnectionPool implements ConnectionPool {
    
    public ConnectionDescriptor _desciptor;
    public ConnectionCreator _creator;
    
    public NonPoolingConnectionPool(ConnectionDescriptor desciptor, ConnectionCreator creator) {
        _desciptor = desciptor;
        _creator = creator;
    }

    @Override
    public Connection getConnection() throws SQLException {
        return _creator.createConnection(_desciptor);
    }

    @Override
    public void releaseConnection(Connection connection) throws SQLException {
        connection.close();
    }
    
}