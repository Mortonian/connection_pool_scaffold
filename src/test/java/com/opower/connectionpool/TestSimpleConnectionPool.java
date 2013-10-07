package com.opower.connectionpool;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import junit.framework.Assert;

import org.apache.log4j.Logger;
import org.easymock.EasyMock;
import org.junit.Test;

import com.opower.connectionpool.pool.PooledConnectionInfo;
import com.opower.connectionpool.pool.MortonianConnectionPool;
import com.opower.connectionpool.pool.config.SimplePoolConfig;

public class TestSimpleConnectionPool {

    private static Logger _log = Logger.getLogger(TestSimpleConnectionPool.class);
    
    @Test
    public void testZeroSizedConnectionPool() {
        ConnectionConfig mockConnectionConfig = EasyMock.createMock(ConnectionConfig.class);
        Connection mockConnection = EasyMock.createMock(Connection.class);
        ConnectionCreator mockConnectionCreator = new MockConnectionCreator(mockConnection);
        SimplePoolConfig poolConfig = new SimplePoolConfig();
        poolConfig.setMaxPoolSize(0);
        MortonianConnectionPool connectionPool = new MortonianConnectionPool(mockConnectionConfig, mockConnectionCreator, poolConfig);
        
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
        ConnectionConfig mockConnectionConfig = EasyMock.createMock(ConnectionConfig.class);
        Connection mockConnection = EasyMock.createMock(Connection.class);
        ConnectionCreator mockConnectionCreator = new MockConnectionCreator(mockConnection);
        SimplePoolConfig poolConfig = new SimplePoolConfig();
        poolConfig.setMaxPoolSize(1);
        MortonianConnectionPool connectionPool = new MortonianConnectionPool(mockConnectionConfig, mockConnectionCreator, poolConfig);
        
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
        
        ConnectionConfig mockConnectionConfig = EasyMock.createMock(ConnectionConfig.class);
        Connection mockConnection = EasyMock.createMock(Connection.class);
        Statement mockStatement = EasyMock.createMock(Statement.class);
        ConnectionCreator mockConnectionCreator = new MockConnectionCreator(mockConnection);
        SimplePoolConfig poolConfig = new SimplePoolConfig();
        poolConfig.setMaxPoolSize(1);
        MortonianConnectionPool connectionPool = new MortonianConnectionPool(mockConnectionConfig, mockConnectionCreator, poolConfig);

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
        ConnectionConfig mockConnectionConfig = EasyMock.createMock(ConnectionConfig.class);
        Connection mockConnection = EasyMock.createMock(Connection.class);
        ConnectionCreator mockConnectionCreator = new MockConnectionCreator(mockConnection);
        SimplePoolConfig poolConfig = new SimplePoolConfig();
        poolConfig.setMaxPoolSize(2);
        MortonianConnectionPool connectionPool = new MortonianConnectionPool(mockConnectionConfig, mockConnectionCreator, poolConfig);
        
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
    public void testAcquireIncrementZero() {
        ConnectionConfig mockConnectionConfig = EasyMock.createMock(ConnectionConfig.class);
        Connection mockConnection = EasyMock.createMock(Connection.class);
        ConnectionCreator mockConnectionCreator = new MockConnectionCreator(mockConnection);
        SimplePoolConfig poolConfig = new SimplePoolConfig();
        poolConfig.setMaxPoolSize(5);
        poolConfig.setAcquireIncrement(0);
        MortonianConnectionPool connectionPool = new MortonianConnectionPool(mockConnectionConfig, mockConnectionCreator, poolConfig);
        
        Connection connection;
        try {
            Assert.assertEquals("Number of connections available should be 0", 0, connectionPool.getNumberOfConnectionsAvailable());
            connection = connectionPool.getConnection();
            
            Assert.assertEquals("Number of connections available should be 0", 0, connectionPool.getNumberOfConnectionsAvailable());
            connection = connectionPool.getConnection();
            
            Assert.assertEquals("Number of connections available should be 0", 0, connectionPool.getNumberOfConnectionsAvailable());
        } catch (SQLException e) {
            _log.error("Error testing zero-sized connection pool", e);
            Assert.fail("Exception: "+e);
        } 
        
    }

    @Test
    public void testAcquireIncrementTwo() {
        ConnectionConfig mockConnectionConfig = EasyMock.createMock(ConnectionConfig.class);
        Connection mockConnection = EasyMock.createMock(Connection.class);
        ConnectionCreator mockConnectionCreator = EasyMock.createMock(ConnectionCreator.class);
        SimplePoolConfig poolConfig = new SimplePoolConfig();
        poolConfig.setMaxPoolSize(6);
        poolConfig.setAcquireIncrement(2);

        EasyMock.replay(mockConnectionCreator);
        
        MortonianConnectionPool connectionPool = new MortonianConnectionPool(mockConnectionConfig, mockConnectionCreator, poolConfig);

        EasyMock.verify(mockConnectionCreator);
        
        Connection connection;
        try {
            Assert.assertEquals("Number of connections handed out after get connection should be 0", 0, connectionPool.getNumberOfConnectionsLeased());
            Assert.assertEquals("Number of connections available should be 0", 0, connectionPool.getNumberOfConnectionsAvailable());
            
            _log.info("first getting connection");

            EasyMock.reset(mockConnectionCreator);
            EasyMock.expect(mockConnectionCreator.createConnection(mockConnectionConfig)).andReturn(mockConnection).times(3);
            EasyMock.replay(mockConnectionCreator);
            
            connection = connectionPool.getConnection();

            EasyMock.verify(mockConnectionCreator);
            
            Assert.assertEquals("Number of connections handed out after get connection should be 1", 1, connectionPool.getNumberOfConnectionsLeased());
            Assert.assertEquals("Number of connections available should be 2", 2, connectionPool.getNumberOfConnectionsAvailable());
            
            _log.info("second getting connection");

            EasyMock.reset(mockConnectionCreator);
            EasyMock.replay(mockConnectionCreator);
            
            connection = connectionPool.getConnection();

            EasyMock.verify(mockConnectionCreator);
            
            Assert.assertEquals("Number of connections handed out after get connection should be 2", 2, connectionPool.getNumberOfConnectionsLeased());
            Assert.assertEquals("Number of connections available should be 1", 1, connectionPool.getNumberOfConnectionsAvailable());

            _log.info("third getting connection");
            
            EasyMock.reset(mockConnectionCreator);
            EasyMock.expect(mockConnectionCreator.createConnection(mockConnectionConfig)).andReturn(mockConnection).times(2);
            EasyMock.replay(mockConnectionCreator);
            
            connection = connectionPool.getConnection();

            EasyMock.verify(mockConnectionCreator);
            
            Assert.assertEquals("Number of connections handed out after get connection should be 3", 3, connectionPool.getNumberOfConnectionsLeased());
            Assert.assertEquals("Number of connections available should be 2", 2, connectionPool.getNumberOfConnectionsAvailable());
        } catch (SQLException e) {
            _log.error("Error testing zero-sized connection pool", e);
            Assert.fail("Exception: "+e);
        } 
        
    }    
    
    @Test
    public void testAcquireIncrementTwoWithInitialSizeTwo() {
        ConnectionConfig mockConnectionConfig = EasyMock.createMock(ConnectionConfig.class);
        Connection mockConnection = EasyMock.createMock(Connection.class);
        ConnectionCreator mockConnectionCreator = EasyMock.createMock(ConnectionCreator.class);
        SimplePoolConfig poolConfig = new SimplePoolConfig();
        poolConfig.setMaxPoolSize(6);
        poolConfig.setAcquireIncrement(2);
        poolConfig.setInitialPoolSize(2);

        Connection connection;
        try {

            EasyMock.expect(mockConnectionCreator.createConnection(mockConnectionConfig)).andReturn(mockConnection).times(2);
            EasyMock.replay(mockConnectionCreator);
            
            MortonianConnectionPool connectionPool = new MortonianConnectionPool(mockConnectionConfig, mockConnectionCreator, poolConfig);

            EasyMock.verify(mockConnectionCreator);
            
            Assert.assertEquals("Number of connections handed out after get connection should be 0", 0, connectionPool.getNumberOfConnectionsLeased());
            Assert.assertEquals("Number of connections available should be 2", 2, connectionPool.getNumberOfConnectionsAvailable());
            
            _log.info("first getting connection");

            EasyMock.reset(mockConnectionCreator);
            EasyMock.replay(mockConnectionCreator);
            
            connection = connectionPool.getConnection();

            EasyMock.verify(mockConnectionCreator);
            
            Assert.assertEquals("Number of connections handed out after get connection should be 1", 1, connectionPool.getNumberOfConnectionsLeased());
            Assert.assertEquals("Number of connections available should be 1", 1, connectionPool.getNumberOfConnectionsAvailable());

            _log.info("second getting connection");
            
            EasyMock.reset(mockConnectionCreator);
            EasyMock.expect(mockConnectionCreator.createConnection(mockConnectionConfig)).andReturn(mockConnection).times(2);
            EasyMock.replay(mockConnectionCreator);
            
            connection = connectionPool.getConnection();

            EasyMock.verify(mockConnectionCreator);
            
            Assert.assertEquals("Number of connections handed out after get connection should be 2", 2, connectionPool.getNumberOfConnectionsLeased());
            Assert.assertEquals("Number of connections available should be 2", 2, connectionPool.getNumberOfConnectionsAvailable());

            _log.info("third getting connection");
            
            EasyMock.reset(mockConnectionCreator);
            EasyMock.replay(mockConnectionCreator);
            
            connection = connectionPool.getConnection();

            EasyMock.verify(mockConnectionCreator);
            
            Assert.assertEquals("Number of connections handed out after get connection should be 3", 3, connectionPool.getNumberOfConnectionsLeased());
            Assert.assertEquals("Number of connections available should be 1", 1, connectionPool.getNumberOfConnectionsAvailable());
        } catch (SQLException e) {
            _log.error("Error testing zero-sized connection pool", e);
            Assert.fail("Exception: "+e);
        } 
        
    }
    
    @Test
    public void testConnectionPoolReturnsNullWhenMaxed() {
        ConnectionConfig mockConnectionConfig = EasyMock.createMock(ConnectionConfig.class);
        Connection mockConnection = EasyMock.createMock(Connection.class);
        ConnectionCreator mockConnectionCreator = new MockConnectionCreator(mockConnection);
        SimplePoolConfig poolConfig = new SimplePoolConfig();
        poolConfig.setMaxPoolSize(10);
        MortonianConnectionPool connectionPool = new MortonianConnectionPool(mockConnectionConfig, mockConnectionCreator, poolConfig);
        
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

    @Test
    public void testReleaseConnectionToWrongPool() {

        ConnectionConfig mockConnectionConfig = EasyMock.createMock(ConnectionConfig.class);
        Connection mockConnection = EasyMock.createMock(Connection.class);
        ConnectionCreator mockConnectionCreator = new MockConnectionCreator(mockConnection);
        SimplePoolConfig poolConfig = new SimplePoolConfig();
        MortonianConnectionPool connectionPool1 = new MortonianConnectionPool(mockConnectionConfig, mockConnectionCreator, poolConfig);
        MortonianConnectionPool connectionPool2 = new MortonianConnectionPool(mockConnectionConfig, mockConnectionCreator, poolConfig);
        
        try {
            Connection connection1 = connectionPool1.getConnection();
            Connection connection2 = connectionPool2.getConnection();

            Assert.assertTrue("connection1 should be valid", ((PooledConnectionInfo)connection1).isLeaseValid());
            Assert.assertTrue("connection2 should be valid", ((PooledConnectionInfo)connection2).isLeaseValid());
            
            connectionPool1.releaseConnection(connection1);

            Assert.assertFalse("connection1 should not be valid", ((PooledConnectionInfo)connection1).isLeaseValid());

            try {
                connectionPool1.releaseConnection(connection2);
                Assert.fail("should not be able to release connection2 to connectionpool1");
            } catch (Exception e) {
                
            }
            
        } catch (SQLException e) {
            _log.error("Error getting connection", e);
            Assert.fail("Exception: "+e);
        }        
    }

    @Test
    public void testAutoCommitDefaultOff() {

        ConnectionConfig mockConnectionConfig = EasyMock.createMock(ConnectionConfig.class);
        Connection mockConnection = EasyMock.createMock(Connection.class);
        ConnectionCreator mockConnectionCreator = new MockConnectionCreator(mockConnection);
        SimplePoolConfig poolConfig = new SimplePoolConfig();
        MortonianConnectionPool connectionPool = new MortonianConnectionPool(mockConnectionConfig, mockConnectionCreator, poolConfig);
        
        try {
            Connection connection = connectionPool.getConnection();
            
            EasyMock.replay(mockConnection);
            
            connectionPool.releaseConnection(connection);
            
            EasyMock.verify(mockConnection);
            
        } catch (SQLException e) {
            _log.error("Error getting connection", e);
            Assert.fail("Exception: "+e);
        }
    }


    @Test
    public void testAutoCommitOff() {

        ConnectionConfig mockConnectionConfig = EasyMock.createMock(ConnectionConfig.class);
        Connection mockConnection = EasyMock.createMock(Connection.class);
        ConnectionCreator mockConnectionCreator = new MockConnectionCreator(mockConnection);
        SimplePoolConfig poolConfig = new SimplePoolConfig();
        poolConfig.setAutoCommit(false);
        MortonianConnectionPool connectionPool = new MortonianConnectionPool(mockConnectionConfig, mockConnectionCreator, poolConfig);
        
        try {
            Connection connection = connectionPool.getConnection();
            
            EasyMock.replay(mockConnection);
            
            connectionPool.releaseConnection(connection);
            
            EasyMock.verify(mockConnection);
            
        } catch (SQLException e) {
            _log.error("Error getting connection", e);
            Assert.fail("Exception: "+e);
        }
    }


    @Test
    public void testAutoCommitOn() {

        ConnectionConfig mockConnectionConfig = EasyMock.createMock(ConnectionConfig.class);
        Connection mockConnection = EasyMock.createMock(Connection.class);
        ConnectionCreator mockConnectionCreator = new MockConnectionCreator(mockConnection);
        SimplePoolConfig poolConfig = new SimplePoolConfig();
        poolConfig.setAutoCommit(true);
        MortonianConnectionPool connectionPool = new MortonianConnectionPool(mockConnectionConfig, mockConnectionCreator, poolConfig);
        
        try {
            Connection connection = connectionPool.getConnection();
            
            mockConnection.commit();
            EasyMock.expectLastCall();
            EasyMock.replay(mockConnection);
            
            connectionPool.releaseConnection(connection);
            
            EasyMock.verify(mockConnection);
            
        } catch (SQLException e) {
            _log.error("Error getting connection", e);
            Assert.fail("Exception: "+e);
        }
    }
    

    @Test
    public void testNoRetries() {

        ConnectionConfig mockConnectionConfig = EasyMock.createMock(ConnectionConfig.class);
        Connection mockConnection = EasyMock.createMock(Connection.class);
        ConnectionCreator mockConnectionCreator = new MockConnectionCreator(mockConnection);
        SimplePoolConfig poolConfig = new SimplePoolConfig();
        poolConfig.setMaxPoolSize(1);
        poolConfig.setRetryAttempts(0);
        poolConfig.setRetryWaitTimeInMillis(1000);
        MortonianConnectionPool connectionPool = new MortonianConnectionPool(mockConnectionConfig, mockConnectionCreator, poolConfig);
        
        try {
            Connection connection1 = connectionPool.getConnection();
            Assert.assertNotNull("I should get one connection", connection1);
            
            long beforeTime = System.currentTimeMillis();

            Connection connection2 = connectionPool.getConnection();
            
            long afterTime = System.currentTimeMillis();
            

            Assert.assertNull("I should not get a secon connection", connection2);
            Assert.assertTrue("Should not be retrying", afterTime - beforeTime < 10);
            
        } catch (SQLException e) {
            _log.error("Error getting connection", e);
            Assert.fail("Exception: "+e);
        }
        
    }


    @Test
    public void testOneRetry() {

        ConnectionConfig mockConnectionConfig = EasyMock.createMock(ConnectionConfig.class);
        Connection mockConnection = EasyMock.createMock(Connection.class);
        ConnectionCreator mockConnectionCreator = new MockConnectionCreator(mockConnection);
        SimplePoolConfig poolConfig = new SimplePoolConfig();
        int retryAttempts = 1;
        int retryWaitTimeMillis = 1000;
        poolConfig.setMaxPoolSize(1);
        poolConfig.setRetryAttempts(retryAttempts);
        poolConfig.setRetryWaitTimeInMillis(retryWaitTimeMillis);
        MortonianConnectionPool connectionPool = new MortonianConnectionPool(mockConnectionConfig, mockConnectionCreator, poolConfig);
        
        try {
            Connection connection1 = connectionPool.getConnection();
            Assert.assertNotNull("I should get one connection", connection1);
            
            long beforeTime = System.currentTimeMillis();

            Connection connection2 = connectionPool.getConnection();
            
            long afterTime = System.currentTimeMillis();
            

            Assert.assertNull("I should not get a secon connection", connection2);
            Assert.assertTrue("Should retry at least once", afterTime - beforeTime >= retryWaitTimeMillis);
            Assert.assertTrue("Should retry only once", afterTime - beforeTime < (retryAttempts * retryWaitTimeMillis) + (0.5 * retryWaitTimeMillis));
            
        } catch (SQLException e) {
            _log.error("Error getting connection", e);
            Assert.fail("Exception: "+e);
        }
    }

    @Test
    public void testManyRetries() {

        ConnectionConfig mockConnectionConfig = EasyMock.createMock(ConnectionConfig.class);
        Connection mockConnection = EasyMock.createMock(Connection.class);
        ConnectionCreator mockConnectionCreator = new MockConnectionCreator(mockConnection);
        SimplePoolConfig poolConfig = new SimplePoolConfig();
        int retryAttempts = 10;
        int retryWaitTimeMillis = 1000;
        poolConfig.setMaxPoolSize(1);
        poolConfig.setRetryAttempts(retryAttempts);
        poolConfig.setRetryWaitTimeInMillis(retryWaitTimeMillis);
        MortonianConnectionPool connectionPool = new MortonianConnectionPool(mockConnectionConfig, mockConnectionCreator, poolConfig);
        
        try {
            Connection connection1 = connectionPool.getConnection();
            Assert.assertNotNull("I should get one connection", connection1);
            
            long beforeTime = System.currentTimeMillis();

            Connection connection2 = connectionPool.getConnection();
            
            long afterTime = System.currentTimeMillis();
            

            Assert.assertNull("I should not get a secon connection", connection2);
            Assert.assertTrue("Should retry at least once", afterTime - beforeTime >= retryWaitTimeMillis);
            Assert.assertTrue("Should retry only once", afterTime - beforeTime < (retryAttempts * retryWaitTimeMillis) + (0.5 * retryWaitTimeMillis));
            
        } catch (SQLException e) {
            _log.error("Error getting connection", e);
            Assert.fail("Exception: "+e);
        }
    }  

    @Test
    public void testShutdownOnReleasedConnections() {

        ConnectionConfig mockConnectionConfig = EasyMock.createMock(ConnectionConfig.class);
        Connection mockConnection = EasyMock.createMock(Connection.class);
        ConnectionCreator mockConnectionCreator = new MockConnectionCreator(mockConnection);
        SimplePoolConfig poolConfig = new SimplePoolConfig();
        poolConfig.setMaxPoolSize(1);
        poolConfig.setAutoCommit(true);
        MortonianConnectionPool connectionPool = new MortonianConnectionPool(mockConnectionConfig, mockConnectionCreator, poolConfig);
        
        try {
            Connection connection = connectionPool.getConnection();
            
            mockConnection.commit();
            EasyMock.expectLastCall();
            EasyMock.replay(mockConnection);
            
            connectionPool.releaseConnection(connection);

            Assert.assertFalse("Connection Pools should not be shutdown", connectionPool.isShutdown());
            Assert.assertFalse("Connection should no longer be valid", ((PooledConnectionInfo)connection).isLeaseValid());

            EasyMock.verify(mockConnection);
            EasyMock.reset(mockConnection);
            EasyMock.expect(mockConnection.isClosed()).andReturn(false);
            mockConnection.close();
            EasyMock.expectLastCall();
            EasyMock.replay(mockConnection);
            
            connectionPool.shutdown();

            Assert.assertTrue("Connection Pools should be shutdown", connectionPool.isShutdown());
            Assert.assertFalse("Connection should no longer be valid", ((PooledConnectionInfo)connection).isLeaseValid());

            EasyMock.verify(mockConnection);
            
            try {
                connection.createStatement();
                Assert.fail("Should not be able to operate on a connection from a shutdown connection pool");
            } catch (Exception e) {
                _log.debug("Correctly got error working with connection post shutdown:"+e);
            }
            
        } catch (SQLException e) {
            _log.error("Error getting connection", e);
            Assert.fail("Exception: "+e);
        }   
    }

    @Test
    public void testShutdownOnUnreleasedConnections() {

        ConnectionConfig mockConnectionConfig = EasyMock.createMock(ConnectionConfig.class);
        Connection mockConnection = EasyMock.createMock(Connection.class);
        ConnectionCreator mockConnectionCreator = new MockConnectionCreator(mockConnection);
        SimplePoolConfig poolConfig = new SimplePoolConfig();
        poolConfig.setMaxPoolSize(1);
        poolConfig.setAutoCommit(true);
        MortonianConnectionPool connectionPool = new MortonianConnectionPool(mockConnectionConfig, mockConnectionCreator, poolConfig);
        
        try {
            Connection connection = connectionPool.getConnection();
            
            EasyMock.replay(mockConnection);

            Assert.assertFalse("Connection Pools should not be shutdown", connectionPool.isShutdown());
            Assert.assertTrue("Connection should still be valid", ((PooledConnectionInfo)connection).isLeaseValid());

            EasyMock.verify(mockConnection);
            EasyMock.reset(mockConnection);

            mockConnection.commit();
            EasyMock.expectLastCall();

            EasyMock.expect(mockConnection.isClosed()).andReturn(false);
            mockConnection.close();
            EasyMock.expectLastCall();
            
            EasyMock.replay(mockConnection);
            
            connectionPool.shutdown();

            Assert.assertTrue("Connection Pools should be shutdown", connectionPool.isShutdown());

            EasyMock.verify(mockConnection);
            
            try {
                connection.createStatement();
                Assert.fail("Should not be able to operate on a connection from a shutdown connection pool");
            } catch (Exception e) {
                _log.debug("Correctly got error working with connection post shutdown:"+e);
            }
            
        } catch (SQLException e) {
            _log.error("Error getting connection", e);
            Assert.fail("Exception: "+e);
        }   
    }

    @Test
    public void testMaxConnectionAge() {

        ConnectionConfig mockConnectionConfig = EasyMock.createMock(ConnectionConfig.class);
        Connection mockConnection = EasyMock.createMock(Connection.class);
        Connection mockConnection2 = EasyMock.createMock(Connection.class);
        ConnectionCreator mockConnectionCreator = EasyMock.createMock(ConnectionCreator.class);
        SimplePoolConfig poolConfig = new SimplePoolConfig();
        poolConfig.setMaxPoolSize(2);
        int maxConnectionAgeInMilis = 1000;
        poolConfig.setMaxConnectionAgeInMilis(maxConnectionAgeInMilis);
        poolConfig.setAutoCommit(true);
        MortonianConnectionPool connectionPool = new MortonianConnectionPool(mockConnectionConfig, mockConnectionCreator, poolConfig);
        
        try {
            
            EasyMock.expect(mockConnectionCreator.createConnection(mockConnectionConfig)).andReturn(mockConnection);
            EasyMock.replay(mockConnectionCreator);
            
            Connection connection = connectionPool.getConnection();

            EasyMock.verify(mockConnectionCreator);

            Assert.assertTrue("First connection should still be valid ", ((PooledConnectionInfo)connection).isLeaseValid());
            
            connectionPool.releaseConnection(connection);

            try {
                Thread.sleep(maxConnectionAgeInMilis / 2);
            } catch (InterruptedException e1) {
                _log.error("Woke up from nap");
            }

            EasyMock.reset(mockConnectionCreator);
            EasyMock.replay(mockConnectionCreator);
            
            Connection connection2 = connectionPool.getConnection();

            EasyMock.verify(mockConnectionCreator);

            Assert.assertFalse("First connection should no longer be valid ", ((PooledConnectionInfo)connection).isLeaseValid());
            Assert.assertTrue("Second connection should still be valid ", ((PooledConnectionInfo)connection2).isLeaseValid());

            connectionPool.releaseConnection(connection2);
            
            try {
                Thread.sleep(10 + (maxConnectionAgeInMilis / 2));
            } catch (InterruptedException e1) {
                _log.error("Woke up from nap");
            }

            EasyMock.reset(mockConnectionCreator);
            EasyMock.expect(mockConnectionCreator.createConnection(mockConnectionConfig)).andReturn(mockConnection2);
            EasyMock.replay(mockConnectionCreator);
            
            Connection connection3 = connectionPool.getConnection();

            EasyMock.verify(mockConnectionCreator);

            Assert.assertFalse("First connection should no longer be valid ", ((PooledConnectionInfo)connection).isLeaseValid());
            Assert.assertFalse("Second connection should no longer be valid ", ((PooledConnectionInfo)connection2).isLeaseValid());
            Assert.assertTrue("Third connection should still be valid ", ((PooledConnectionInfo)connection3).isLeaseValid());
            
        } catch (SQLException e) {
            _log.error("Error getting connection", e);
            Assert.fail("Exception: "+e);
        }   
    }

    @Test
    public void testMaxIdleTime() {

        ConnectionConfig mockConnectionConfig = EasyMock.createMock(ConnectionConfig.class);
        Connection mockConnection = EasyMock.createMock(Connection.class);
        Connection mockConnection2 = EasyMock.createMock(Connection.class);
        ConnectionCreator mockConnectionCreator = EasyMock.createMock(ConnectionCreator.class);
        SimplePoolConfig poolConfig = new SimplePoolConfig();
        poolConfig.setMaxPoolSize(2);
        int maxIdleTimeInMilis = 1000;
        poolConfig.setMaxIdleTimeInMilis(maxIdleTimeInMilis);
        poolConfig.setMaxConnectionAgeInMilis(maxIdleTimeInMilis * 10);
        poolConfig.setAutoCommit(true);
        MortonianConnectionPool connectionPool = new MortonianConnectionPool(mockConnectionConfig, mockConnectionCreator, poolConfig);
        
        try {
            
            EasyMock.expect(mockConnectionCreator.createConnection(mockConnectionConfig)).andReturn(mockConnection);
            EasyMock.replay(mockConnectionCreator);
            
            Connection connection = connectionPool.getConnection();

            EasyMock.verify(mockConnectionCreator);

            Assert.assertTrue("First connection should still be valid ", ((PooledConnectionInfo)connection).isLeaseValid());
            
            connectionPool.releaseConnection(connection);

            try {
                Thread.sleep(maxIdleTimeInMilis / 2);
            } catch (InterruptedException e1) {
                _log.error("Woke up from nap");
            }

            EasyMock.reset(mockConnectionCreator);
            EasyMock.replay(mockConnectionCreator);
            
            Connection connection2 = connectionPool.getConnection();

            EasyMock.verify(mockConnectionCreator);

            Assert.assertFalse("First connection should no longer be valid ", ((PooledConnectionInfo)connection).isLeaseValid());
            Assert.assertTrue("Second connection should still be valid ", ((PooledConnectionInfo)connection2).isLeaseValid());

            connectionPool.releaseConnection(connection2);
            
            try {
                Thread.sleep(10 + (maxIdleTimeInMilis / 2));
            } catch (InterruptedException e1) {
                _log.error("Woke up from nap");
            }

            EasyMock.reset(mockConnectionCreator);
            EasyMock.replay(mockConnectionCreator);
            
            Connection connection3 = connectionPool.getConnection();

            EasyMock.verify(mockConnectionCreator);

            Assert.assertFalse("First connection should no longer be valid ", ((PooledConnectionInfo)connection).isLeaseValid());
            Assert.assertFalse("Second connection should no longer be valid ", ((PooledConnectionInfo)connection2).isLeaseValid());
            Assert.assertTrue("Third connection should still be valid ", ((PooledConnectionInfo)connection3).isLeaseValid());

            connectionPool.releaseConnection(connection3);
            
            try {
                Thread.sleep(10 + (maxIdleTimeInMilis / 2));
            } catch (InterruptedException e1) {
                _log.error("Woke up from nap");
            }

            EasyMock.reset(mockConnectionCreator);
            EasyMock.replay(mockConnectionCreator);
            
            Connection connection4 = connectionPool.getConnection();

            Assert.assertFalse("First connection should no longer be valid ", ((PooledConnectionInfo)connection).isLeaseValid());
            Assert.assertFalse("Second connection should no longer be valid ", ((PooledConnectionInfo)connection2).isLeaseValid());
            Assert.assertFalse("Third connection should no longer be valid ", ((PooledConnectionInfo)connection3).isLeaseValid());
            Assert.assertTrue("Fourth connection should still be valid ", ((PooledConnectionInfo)connection4).isLeaseValid());

            connectionPool.releaseConnection(connection4);
            
            try {
                Thread.sleep(10 + maxIdleTimeInMilis);
            } catch (InterruptedException e1) {
                _log.error("Woke up from nap");
            }

            EasyMock.reset(mockConnectionCreator);
            EasyMock.expect(mockConnectionCreator.createConnection(mockConnectionConfig)).andReturn(mockConnection2);
            EasyMock.replay(mockConnectionCreator);
            
            Connection connection5 = connectionPool.getConnection();

            Assert.assertFalse("First connection should no longer be valid ", ((PooledConnectionInfo)connection).isLeaseValid());
            Assert.assertFalse("Second connection should no longer be valid ", ((PooledConnectionInfo)connection2).isLeaseValid());
            Assert.assertFalse("Third connection should no longer be valid ", ((PooledConnectionInfo)connection3).isLeaseValid());
            Assert.assertFalse("Fourth connection should no longer be valid ", ((PooledConnectionInfo)connection4).isLeaseValid());
            Assert.assertTrue("Fifth connection should still be valid ", ((PooledConnectionInfo)connection5).isLeaseValid());
            
            
            
        } catch (SQLException e) {
            _log.error("Error getting connection", e);
            Assert.fail("Exception: "+e);
        }   
    }

    @Test
    public void testDefaultMaxConnectionAgeAndMaxIdleTime() {

        ConnectionConfig mockConnectionConfig = EasyMock.createMock(ConnectionConfig.class);
        Connection mockConnection = EasyMock.createMock(Connection.class);
        ConnectionCreator mockConnectionCreator = EasyMock.createMock(ConnectionCreator.class);
        SimplePoolConfig poolConfig = new SimplePoolConfig();
        poolConfig.setMaxPoolSize(1);
        int randomWaitTime = 1000;
        poolConfig.setAutoCommit(true);
        MortonianConnectionPool connectionPool = new MortonianConnectionPool(mockConnectionConfig, mockConnectionCreator, poolConfig);
        
        try {
            
            EasyMock.expect(mockConnectionCreator.createConnection(mockConnectionConfig)).andReturn(mockConnection);
            EasyMock.replay(mockConnectionCreator);
            
            Connection connection = connectionPool.getConnection();

            EasyMock.verify(mockConnectionCreator);

            Assert.assertTrue("First connection should still be valid ", ((PooledConnectionInfo)connection).isLeaseValid());
            
            connectionPool.releaseConnection(connection);

            try {
                Thread.sleep(randomWaitTime);
            } catch (InterruptedException e1) {
                _log.error("Woke up from nap");
            }

            EasyMock.reset(mockConnectionCreator);
            EasyMock.replay(mockConnectionCreator);
            
            Connection connection2 = connectionPool.getConnection();

            EasyMock.verify(mockConnectionCreator);

            Assert.assertFalse("First connection should no longer be valid ", ((PooledConnectionInfo)connection).isLeaseValid());
            Assert.assertTrue("Second connection should still be valid ", ((PooledConnectionInfo)connection2).isLeaseValid());

            connectionPool.releaseConnection(connection2);
            
            try {
                Thread.sleep(10 + (randomWaitTime));
            } catch (InterruptedException e1) {
                _log.error("Woke up from nap");
            }

            EasyMock.reset(mockConnectionCreator);
            EasyMock.replay(mockConnectionCreator);
            
            Connection connection3 = connectionPool.getConnection();

            EasyMock.verify(mockConnectionCreator);

            Assert.assertFalse("First connection should no longer be valid ", ((PooledConnectionInfo)connection).isLeaseValid());
            Assert.assertFalse("Second connection should no longer be valid ", ((PooledConnectionInfo)connection2).isLeaseValid());
            Assert.assertTrue("Third connection should still be valid ", ((PooledConnectionInfo)connection3).isLeaseValid());
            
        } catch (SQLException e) {
            _log.error("Error getting connection", e);
            Assert.fail("Exception: "+e);
        }   
    }
}
