package com.opower.connectionpool;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import junit.framework.Assert;

import org.apache.log4j.Logger;
import org.easymock.EasyMock;
import org.junit.Test;

import com.opower.connectionpool.creator.BasicConnectionCreator;
import com.opower.connectionpool.descriptor.JsonFileConnectionDescriptor;
import com.opower.connectionpool.descriptor.SimpleConnectionDescriptor;
import com.opower.connectionpool.pool.NonPoolingConnectionPool;

public class TestNonPoolingConnectionPool {
    
    private static Logger _log = Logger.getLogger(TestNonPoolingConnectionPool.class);
    
    @Test
    public void testMockedConnection() {        
        try {
            SimpleConnectionDescriptor descriptor = new SimpleConnectionDescriptor();
            descriptor.setDriverClass("org.postgresql.Driver");
            descriptor.setJdbcUrl("jdbc:postgresql://"+TestConstants.HOSTNAME+"/"+TestConstants.DATABASE+"?"+ "user="+TestConstants.USERNAME);
            ConnectionCreator connectionCreator = new MockConnectionCreator();
            NonPoolingConnectionPool pool = new NonPoolingConnectionPool(descriptor, connectionCreator);
            Connection connect = pool.getConnection();
            Statement statement = connect.createStatement();
            ResultSet resultSet = statement.executeQuery(TestConstants.BASIC_COUNT_QUERY);
            resultSet.next();
            String count = resultSet.getString("count");
            Assert.assertTrue("Count should be greater than 0", Integer.valueOf(count) > 0);
            _log.info("Found "+count+" items");
            pool.releaseConnection(connect);
            EasyMock.verify(connect);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testActualConnectionViaJsonFile() {        
        try {
            JsonFileConnectionDescriptor descriptor = new JsonFileConnectionDescriptor();
            descriptor.setFile("/tmp/dbconection.json");
            ConnectionCreator connectionCreator = new BasicConnectionCreator();
            NonPoolingConnectionPool pool = new NonPoolingConnectionPool(descriptor, connectionCreator);
            Connection connect = pool.getConnection();
            Statement statement = connect.createStatement();
            ResultSet resultSet = statement.executeQuery(TestConstants.BASIC_COUNT_QUERY);
            resultSet.next();
            String count = resultSet.getString("count");
            Assert.assertTrue("Count should be greater than 0", Integer.valueOf(count) > 0);
            _log.info("Found "+count+" items");
            Assert.assertFalse("Connection should NOT be closed before releasing", connect.isClosed());
            pool.releaseConnection(connect);
            Assert.assertTrue("Connection should be closed after realsing to a non-pooling pool", connect.isClosed());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static class MockConnectionCreator implements ConnectionCreator {

        @Override
        public Connection createConnection(ConnectionDescriptor connectionDescriptor) throws SQLException {
            
            Connection mockConnection = EasyMock.createMock(Connection.class);
            Statement mockStatement = EasyMock.createMock(Statement.class);
            ResultSet mockResultSet = EasyMock.createMock(ResultSet.class);
            EasyMock.expect(mockConnection.createStatement()).andReturn(mockStatement);
            
            EasyMock.expect(mockStatement.executeQuery(TestConstants.BASIC_COUNT_QUERY)).andReturn(mockResultSet);
            
            EasyMock.expect(mockResultSet.next()).andReturn(true);
            EasyMock.expect(mockResultSet.getString("count")).andReturn("1");
            
            mockConnection.close();
            
            EasyMock.expectLastCall();
            
            EasyMock.replay(mockConnection);
            EasyMock.replay(mockStatement);
            EasyMock.replay(mockResultSet);

            return mockConnection;
        }
    }
}