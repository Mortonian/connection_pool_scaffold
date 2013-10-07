package com.opower.connectionpool;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import junit.framework.Assert;

import org.apache.log4j.Logger;
import org.easymock.EasyMock;
import org.junit.Test;

import com.opower.connectionpool.pool.PooledConnectionInfo;
import com.opower.connectionpool.pool.SimpleConnectionPool;

public class TestSimpleConnectionPool {

    private static Logger _log = Logger.getLogger(TestSimpleConnectionPool.class);
    
    @Test
    public void testZeroSizedConnectionPool() {
        ConnectionDescriptor mockConnectionDescriptor = EasyMock.createMock(ConnectionDescriptor.class);
        Connection mockConnection = EasyMock.createMock(Connection.class);
        ConnectionCreator mockConnectionCreator = new MockConnectionCreator(mockConnection);
        SimpleConnectionPool connecionPool = new SimpleConnectionPool(mockConnectionDescriptor, mockConnectionCreator, 0);
        
        Connection connection;
        try {
            Assert.assertEquals("Number of connections handed out should be 0", 0, connecionPool.getNumberOfConnectionsLeased());
            Assert.assertEquals("Number of connections available should be 0", 0, connecionPool.getNumberOfConnectionsAvailable());
            connection = connecionPool.getConnection();
            Assert.assertNull("Connection returned from a zero-sized pool should be null", connection);
            Assert.assertEquals("Number of connections handed out after get connection should be 0", 0, connecionPool.getNumberOfConnectionsLeased());
            Assert.assertEquals("Number of connections available after get connection should be 0", 0, connecionPool.getNumberOfConnectionsAvailable());
            connecionPool.releaseConnection(connection);
            Assert.assertEquals("Number of connections handed out after release connection should be 0", 0, connecionPool.getNumberOfConnectionsLeased());
            Assert.assertEquals("Number of connections available after release connection should be 0", 0, connecionPool.getNumberOfConnectionsAvailable());
        } catch (SQLException e) {
            _log.error("Error testing zero-sized connection pool", e);
        }  
    }

    @Test
    public void testOneSizedConnectionPool() {
        ConnectionDescriptor mockConnectionDescriptor = EasyMock.createMock(ConnectionDescriptor.class);
        Connection mockConnection = EasyMock.createMock(Connection.class);
        ConnectionCreator mockConnectionCreator = new MockConnectionCreator(mockConnection);
        SimpleConnectionPool connecionPool = new SimpleConnectionPool(mockConnectionDescriptor, mockConnectionCreator, 1);
        
        Connection connection;
        try {
            Assert.assertEquals("Number of connections handed out should be 0", 0, connecionPool.getNumberOfConnectionsLeased());
            Assert.assertEquals("Number of connections available should be 0", 0, connecionPool.getNumberOfConnectionsAvailable());
            connection = connecionPool.getConnection();
            Assert.assertNotNull("Connection returned from a zero-sized pool should not be null", connection);
            Assert.assertEquals("Number of connections handed out after get connection should be 1", 1, connecionPool.getNumberOfConnectionsLeased());
            Assert.assertEquals("Number of connections available after get connection should be 0", 0, connecionPool.getNumberOfConnectionsAvailable());
            connecionPool.releaseConnection(connection);
            Assert.assertEquals("Number of connections handed out after release connection should be 0", 0, connecionPool.getNumberOfConnectionsLeased());
            Assert.assertEquals("Number of connections available after release connection should be 1", 1, connecionPool.getNumberOfConnectionsAvailable());
            connecionPool.releaseConnection(connection);
            Assert.assertEquals("Number of connections handed out after release connection should be 0", 0, connecionPool.getNumberOfConnectionsLeased());
            Assert.assertEquals("Number of connections available after release connection should be 1", 1, connecionPool.getNumberOfConnectionsAvailable());
            Connection connection2 = connecionPool.getConnection();
            String uuid1 = ((PooledConnectionInfo)connection).getConnectionUuid();
            String uuid2 = ((PooledConnectionInfo)connection2).getConnectionUuid();
            Assert.assertEquals("Should be the same connection, got UUIDs: "+uuid1+", "+uuid2, uuid1, uuid2);
        } catch (SQLException e) {
            _log.error("Error testing zero-sized connection pool", e);
        }  
    }

    @Test
    public void testConnectionReleasing() {
        
        ConnectionDescriptor mockConnectionDescriptor = EasyMock.createMock(ConnectionDescriptor.class);
        Connection mockConnection = EasyMock.createMock(Connection.class);
        Statement mockStatement = EasyMock.createMock(Statement.class);
        ConnectionCreator mockConnectionCreator = new MockConnectionCreator(mockConnection);
        SimpleConnectionPool connecionPool = new SimpleConnectionPool(mockConnectionDescriptor, mockConnectionCreator, 1);

        Connection connection;
        try {

            EasyMock.expect(mockConnection.createStatement()).andReturn(mockStatement);
            EasyMock.replay(mockConnection);
            
            connection = connecionPool.getConnection();
            Statement statement = connection.createStatement();
            Assert.assertTrue("Connection should still be valid", ((PooledConnectionInfo)connection).isLeaseValid());
            Assert.assertNotNull("Valid connections should return non-null statements", statement);
            
            connecionPool.releaseConnection(connection);
            Assert.assertFalse("Connection should no longer be valid", ((PooledConnectionInfo)connection).isLeaseValid());
            try {
                connection.createStatement();
                Assert.fail("Released connections should not allow you to call methods");
            } catch (Exception e) { }
            
            EasyMock.verify(mockConnection);
        } catch (SQLException e) {
            _log.error("Error testing zero-sized connection pool", e);
        } 
    }

    @Test
    public void testTwoSizedConnectionPool() {
        ConnectionDescriptor mockConnectionDescriptor = EasyMock.createMock(ConnectionDescriptor.class);
        Connection mockConnection = EasyMock.createMock(Connection.class);
        ConnectionCreator mockConnectionCreator = new MockConnectionCreator(mockConnection);
        SimpleConnectionPool connecionPool = new SimpleConnectionPool(mockConnectionDescriptor, mockConnectionCreator, 2);
        
        Connection connection;
        try {
            Assert.assertEquals("Number of connections handed out should be 0", 0, connecionPool.getNumberOfConnectionsLeased());
            Assert.assertEquals("Number of connections available should be 0", 0, connecionPool.getNumberOfConnectionsAvailable());
            connection = connecionPool.getConnection();
            Assert.assertNotNull("Connection returned from a zero-sized pool should not be null", connection);
            Assert.assertEquals("Number of connections handed out after get connection should be 1", 1, connecionPool.getNumberOfConnectionsLeased());
            Assert.assertEquals("Number of connections available after get connection should be 0", 0, connecionPool.getNumberOfConnectionsAvailable());
            connecionPool.releaseConnection(connection);
            Assert.assertEquals("Number of connections handed out after release connection should be 0", 0, connecionPool.getNumberOfConnectionsLeased());
            Assert.assertEquals("Number of connections available after release connection should be 1", 1, connecionPool.getNumberOfConnectionsAvailable());
            
            Connection connection2 = connecionPool.getConnection();
            Assert.assertNotNull("Connection returned from a zero-sized pool should not be null", connection2);
            Assert.assertEquals("Number of connections handed out after get connection should be 1", 1, connecionPool.getNumberOfConnectionsLeased());
            Assert.assertEquals("Number of connections available after get connection should be 0", 0, connecionPool.getNumberOfConnectionsAvailable());
            
            String uuid1 = ((PooledConnectionInfo)connection).getConnectionUuid();
            String uuid2 = ((PooledConnectionInfo)connection2).getConnectionUuid();
            Assert.assertEquals("Should be the same connection, got UUIDs: "+uuid1+", "+uuid2, uuid1, uuid2);
            
            Connection connection3 = connecionPool.getConnection();
            Assert.assertNotNull("Connection returned from a zero-sized pool should not be null", connection3);
            Assert.assertEquals("Number of connections handed out after get connection should be 1", 2, connecionPool.getNumberOfConnectionsLeased());
            Assert.assertEquals("Number of connections available after get connection should be 0", 0, connecionPool.getNumberOfConnectionsAvailable());
            
            String uuid3 = ((PooledConnectionInfo)connection3).getConnectionUuid();
            Assert.assertFalse("Should not be the same connection, got UUIDs: "+uuid1+", "+uuid3, uuid1.equals(uuid3));
            
            connecionPool.releaseConnection(connection3);
            Assert.assertEquals("Number of connections handed out after release connection should be 1", 1, connecionPool.getNumberOfConnectionsLeased());
            Assert.assertEquals("Number of connections available after release connection should be 1", 1, connecionPool.getNumberOfConnectionsAvailable());
            
            Assert.assertFalse("Release connection should not be valid", ((PooledConnectionInfo)connection).isLeaseValid());
            Assert.assertTrue("not-yet-released connection should still be valid", ((PooledConnectionInfo)connection2).isLeaseValid());
            Assert.assertFalse("Release connection should not be valid", ((PooledConnectionInfo)connection3).isLeaseValid());
            
        } catch (SQLException e) {
            _log.error("Error testing zero-sized connection pool", e);
        }  
    }
}
