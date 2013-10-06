package com.opower.connectionpool;

import java.sql.Connection;
import java.sql.SQLException;

import junit.framework.Assert;

import org.apache.log4j.Logger;
import org.easymock.EasyMock;
import org.junit.Test;

import com.opower.connectionpool.pool.SimpleConnectionPool;

public class TestSimpleConnectionPool {

    private static Logger _log = Logger.getLogger(TestSimpleConnectionPool.class);
    
    @Test
    public void testZeroSizedConnectionPool() {
        ConnectionDescriptor mockConnectionDescriptor = EasyMock.createMock(ConnectionDescriptor.class);
        ConnectionCreator mockConnectionCreator = new MockConnectionCreator();
        SimpleConnectionPool connecionPool = new SimpleConnectionPool(mockConnectionDescriptor, mockConnectionCreator, 0);
        
        Connection connection;
        try {
            Assert.assertEquals("Number of connections handed out should be 0", 0, connecionPool.getNumberOfConnectionsHandedOut());
            Assert.assertEquals("Number of connections available should be 0", 0, connecionPool.getNumberOfConnectionsAvailable());
            connection = connecionPool.getConnection();
            Assert.assertNull("Connection returned from a zero-sized pool should be null", connection);
            Assert.assertEquals("Number of connections handed out after get connection should be 0", 0, connecionPool.getNumberOfConnectionsHandedOut());
            Assert.assertEquals("Number of connections available after get connection should be 0", 0, connecionPool.getNumberOfConnectionsAvailable());
            connecionPool.releaseConnection(connection);
            Assert.assertEquals("Number of connections handed out after release connection should be 0", 0, connecionPool.getNumberOfConnectionsHandedOut());
            Assert.assertEquals("Number of connections available after release connection should be 0", 0, connecionPool.getNumberOfConnectionsAvailable());
        } catch (SQLException e) {
            _log.error("Error testing zero-sized connection pool", e);
        }  
    }

    @Test
    public void testOneSizedConnectionPool() {
        ConnectionDescriptor mockConnectionDescriptor = EasyMock.createMock(ConnectionDescriptor.class);
        ConnectionCreator mockConnectionCreator = new MockConnectionCreator();
        SimpleConnectionPool connecionPool = new SimpleConnectionPool(mockConnectionDescriptor, mockConnectionCreator, 1);
        
        Connection connection;
        try {
            Assert.assertEquals("Number of connections handed out should be 0", 0, connecionPool.getNumberOfConnectionsHandedOut());
            Assert.assertEquals("Number of connections available should be 0", 0, connecionPool.getNumberOfConnectionsAvailable());
            connection = connecionPool.getConnection();
            Assert.assertNotNull("Connection returned from a zero-sized pool should not be null", connection);
            Assert.assertEquals("Number of connections handed out after get connection should be 1", 1, connecionPool.getNumberOfConnectionsHandedOut());
            Assert.assertEquals("Number of connections available after get connection should be 0", 0, connecionPool.getNumberOfConnectionsAvailable());
            connecionPool.releaseConnection(connection);
            Assert.assertEquals("Number of connections handed out after release connection should be 0", 0, connecionPool.getNumberOfConnectionsHandedOut());
            Assert.assertEquals("Number of connections available after release connection should be 1", 1, connecionPool.getNumberOfConnectionsAvailable());
            connecionPool.releaseConnection(connection);
            Assert.assertEquals("Number of connections handed out after release connection should be 0", 0, connecionPool.getNumberOfConnectionsHandedOut());
            Assert.assertEquals("Number of connections available after release connection should be 1", 1, connecionPool.getNumberOfConnectionsAvailable());
            Connection connection2 = connecionPool.getConnection();
            Assert.assertEquals("Should be the same connection", connection, connection2);
        } catch (SQLException e) {
            _log.error("Error testing zero-sized connection pool", e);
        }  
    }
    
    public static class MockConnectionCreator implements ConnectionCreator {

        @Override
        public Connection createConnection(ConnectionDescriptor connectionDescriptor) throws SQLException {
            Connection mockConnection = EasyMock.createMock(Connection.class);
            return mockConnection;
        }
    }
}
