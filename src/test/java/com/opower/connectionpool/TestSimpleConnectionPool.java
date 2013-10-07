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
        SimpleConnectionPool connectionPool = new SimpleConnectionPool(mockConnectionDescriptor, mockConnectionCreator, 0);
        
        Connection connection;
        try {
            Assert.assertEquals("Number of connections handed out should be 0", 0, connectionPool.getNumberOfConnectionsLeased());
            Assert.assertEquals("Number of connections available should be 0", 0, connectionPool.getNumberOfConnectionsAvailable());
            connection = connectionPool.getConnection();
            Assert.assertNull("Connection returned from a zero-sized pool should be null", connection);
            Assert.assertEquals("Number of connections handed out after get connection should be 0", 0, connectionPool.getNumberOfConnectionsLeased());
            Assert.assertEquals("Number of connections available after get connection should be 0", 0, connectionPool.getNumberOfConnectionsAvailable());
            connectionPool.releaseConnection(connection);
            Assert.assertEquals("Number of connections handed out after release connection should be 0", 0, connectionPool.getNumberOfConnectionsLeased());
            Assert.assertEquals("Number of connections available after release connection should be 0", 0, connectionPool.getNumberOfConnectionsAvailable());
        } catch (SQLException e) {
            _log.error("Error testing zero-sized connection pool", e);
            Assert.fail("Exception: "+e);
        }  
    }

    @Test
    public void testOneSizedConnectionPool() {
        ConnectionDescriptor mockConnectionDescriptor = EasyMock.createMock(ConnectionDescriptor.class);
        Connection mockConnection = EasyMock.createMock(Connection.class);
        ConnectionCreator mockConnectionCreator = new MockConnectionCreator(mockConnection);
        SimpleConnectionPool connectionPool = new SimpleConnectionPool(mockConnectionDescriptor, mockConnectionCreator, 1);
        
        Connection connection;
        try {
            Assert.assertEquals("Number of connections handed out should be 0", 0, connectionPool.getNumberOfConnectionsLeased());
            Assert.assertEquals("Number of connections available should be 0", 0, connectionPool.getNumberOfConnectionsAvailable());
            connection = connectionPool.getConnection();
            Assert.assertNotNull("Connection returned from a zero-sized pool should not be null", connection);
            Assert.assertEquals("Number of connections handed out after get connection should be 1", 1, connectionPool.getNumberOfConnectionsLeased());
            Assert.assertEquals("Number of connections available after get connection should be 0", 0, connectionPool.getNumberOfConnectionsAvailable());
            connectionPool.releaseConnection(connection);
            Assert.assertEquals("Number of connections handed out after release connection should be 0", 0, connectionPool.getNumberOfConnectionsLeased());
            Assert.assertEquals("Number of connections available after release connection should be 1", 1, connectionPool.getNumberOfConnectionsAvailable());
            connectionPool.releaseConnection(connection);
            Assert.assertEquals("Number of connections handed out after release connection should be 0", 0, connectionPool.getNumberOfConnectionsLeased());
            Assert.assertEquals("Number of connections available after release connection should be 1", 1, connectionPool.getNumberOfConnectionsAvailable());
            Connection connection2 = connectionPool.getConnection();
            String uuid1 = ((PooledConnectionInfo)connection).getConnectionUuid();
            String uuid2 = ((PooledConnectionInfo)connection2).getConnectionUuid();
            Assert.assertEquals("Should be the same connection, got UUIDs: "+uuid1+", "+uuid2, uuid1, uuid2);
        } catch (SQLException e) {
            _log.error("Error testing zero-sized connection pool", e);
            Assert.fail("Exception: "+e);
        }  
    }

    @Test
    public void testConnectionReleasing() {
        
        ConnectionDescriptor mockConnectionDescriptor = EasyMock.createMock(ConnectionDescriptor.class);
        Connection mockConnection = EasyMock.createMock(Connection.class);
        Statement mockStatement = EasyMock.createMock(Statement.class);
        ConnectionCreator mockConnectionCreator = new MockConnectionCreator(mockConnection);
        SimpleConnectionPool connectionPool = new SimpleConnectionPool(mockConnectionDescriptor, mockConnectionCreator, 1);

        Connection connection;
        try {

            EasyMock.expect(mockConnection.createStatement()).andReturn(mockStatement);
            EasyMock.replay(mockConnection);
            
            connection = connectionPool.getConnection();
            Statement statement = connection.createStatement();
            Assert.assertTrue("Connection should still be valid", ((PooledConnectionInfo)connection).isLeaseValid());
            Assert.assertNotNull("Valid connections should return non-null statements", statement);
            
            connectionPool.releaseConnection(connection);
            Assert.assertFalse("Connection should no longer be valid", ((PooledConnectionInfo)connection).isLeaseValid());
            try {
                connection.createStatement();
                Assert.fail("Released connections should not allow you to call methods");
            } catch (Exception e) { }
            
            EasyMock.verify(mockConnection);
        } catch (SQLException e) {
            _log.error("Error testing zero-sized connection pool", e);
            Assert.fail("Exception: "+e);
        } 
    }

    @Test
    public void testTwoSizedConnectionPool() {
        ConnectionDescriptor mockConnectionDescriptor = EasyMock.createMock(ConnectionDescriptor.class);
        Connection mockConnection = EasyMock.createMock(Connection.class);
        ConnectionCreator mockConnectionCreator = new MockConnectionCreator(mockConnection);
        SimpleConnectionPool connectionPool = new SimpleConnectionPool(mockConnectionDescriptor, mockConnectionCreator, 2);
        
        Connection connection;
        try {
            Assert.assertEquals("Number of connections handed out should be 0", 0, connectionPool.getNumberOfConnectionsLeased());
            Assert.assertEquals("Number of connections available should be 0", 0, connectionPool.getNumberOfConnectionsAvailable());
            connection = connectionPool.getConnection();
            Assert.assertNotNull("Connection returned from a zero-sized pool should not be null", connection);
            Assert.assertEquals("Number of connections handed out after get connection should be 1", 1, connectionPool.getNumberOfConnectionsLeased());
            Assert.assertEquals("Number of connections available after get connection should be 0", 0, connectionPool.getNumberOfConnectionsAvailable());
            connectionPool.releaseConnection(connection);
            Assert.assertEquals("Number of connections handed out after release connection should be 0", 0, connectionPool.getNumberOfConnectionsLeased());
            Assert.assertEquals("Number of connections available after release connection should be 1", 1, connectionPool.getNumberOfConnectionsAvailable());
            
            Connection connection2 = connectionPool.getConnection();
            Assert.assertNotNull("Connection returned from a zero-sized pool should not be null", connection2);
            Assert.assertEquals("Number of connections handed out after get connection should be 1", 1, connectionPool.getNumberOfConnectionsLeased());
            Assert.assertEquals("Number of connections available after get connection should be 0", 0, connectionPool.getNumberOfConnectionsAvailable());
            
            String uuid1 = ((PooledConnectionInfo)connection).getConnectionUuid();
            String uuid2 = ((PooledConnectionInfo)connection2).getConnectionUuid();
            Assert.assertEquals("Should be the same connection, got UUIDs: "+uuid1+", "+uuid2, uuid1, uuid2);
            
            Connection connection3 = connectionPool.getConnection();
            Assert.assertNotNull("Connection returned from a zero-sized pool should not be null", connection3);
            Assert.assertEquals("Number of connections handed out after get connection should be 1", 2, connectionPool.getNumberOfConnectionsLeased());
            Assert.assertEquals("Number of connections available after get connection should be 0", 0, connectionPool.getNumberOfConnectionsAvailable());
            
            String uuid3 = ((PooledConnectionInfo)connection3).getConnectionUuid();
            Assert.assertFalse("Should not be the same connection, got UUIDs: "+uuid1+", "+uuid3, uuid1.equals(uuid3));
            
            connectionPool.releaseConnection(connection3);
            Assert.assertEquals("Number of connections handed out after release connection should be 1", 1, connectionPool.getNumberOfConnectionsLeased());
            Assert.assertEquals("Number of connections available after release connection should be 1", 1, connectionPool.getNumberOfConnectionsAvailable());
            
            Assert.assertFalse("Release connection should not be valid", ((PooledConnectionInfo)connection).isLeaseValid());
            Assert.assertTrue("not-yet-released connection should still be valid", ((PooledConnectionInfo)connection2).isLeaseValid());
            Assert.assertFalse("Release connection should not be valid", ((PooledConnectionInfo)connection3).isLeaseValid());
            
        } catch (SQLException e) {
            _log.error("Error testing zero-sized connection pool", e);
            Assert.fail("Exception: "+e);
        }  
    }

    @Test
    public void testConnectionPoolReturnsNullWhenMaxed() {
        ConnectionDescriptor mockConnectionDescriptor = EasyMock.createMock(ConnectionDescriptor.class);
        Connection mockConnection = EasyMock.createMock(Connection.class);
        ConnectionCreator mockConnectionCreator = new MockConnectionCreator(mockConnection);
        SimpleConnectionPool connectionPool = new SimpleConnectionPool(mockConnectionDescriptor, mockConnectionCreator, 10);
        
        for (int i = 0; i < 10; i++) {
            Connection connection = null;
            try {
                connection = connectionPool.getConnection();
            } catch (SQLException e) {
                _log.error("Error getting connection", e);
                Assert.fail("Exception: "+e);
            }
            Assert.assertTrue("connection should be valid", ((PooledConnectionInfo)connection).isLeaseValid());
        }
        
        Connection connection = null;
        try {
            connection = connectionPool.getConnection();
        } catch (SQLException e) {
            _log.error("Error getting connection", e);
            Assert.fail("Exception: "+e);
        }
        Assert.assertNull("11th connection should null", connection);
    }
}
