package com.opower.connectionpool;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import junit.framework.Assert;

import org.apache.log4j.Logger;
import org.easymock.EasyMock;
import org.junit.Test;

import com.opower.connectionpool.ConnectionPool;

public class TestConnectionPool {
    
    private static final String HOSTNAME = "hostname";
    private static final String DATABASE = "database";
    private static final String USERNAME = "username";

    private static final String BASIC_COUNT_QUERY = "select count(*) from table";

    private static Logger _log = Logger.getLogger(TestConnectionPool.class);
    
    @Test
    public void testBasicConnection() {        
        try {
            String url = "jdbc:postgresql://"+HOSTNAME+"/"+DATABASE+"?"+ "user="+USERNAME;

            Class.forName("org.postgresql.Driver");
            DumbConnectionPool innerClass = new DumbConnectionPool();
            innerClass.setUrl(url);
            Connection connect = innerClass.getConnection();
            Statement statement = connect.createStatement();
            ResultSet resultSet = statement.executeQuery(BASIC_COUNT_QUERY);
            resultSet.next();
            String count = resultSet.getString("count");
            Assert.assertTrue("Count should be greater than 0", Integer.valueOf(count) > 0);
            _log.info("Found "+count+" items");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static class DumbConnectionPool implements ConnectionPool {
        
        private String _url;
        
        public void setUrl(String url) {
            _url = url;
        }

        @Override
        public Connection getConnection() throws SQLException {
            
            Connection mockConnection = EasyMock.createMock(Connection.class);
            Statement mockStatement = EasyMock.createMock(Statement.class);
            ResultSet mockResultSet = EasyMock.createMock(ResultSet.class);
            EasyMock.expect(mockConnection.createStatement()).andReturn(mockStatement);
            
            EasyMock.expect(mockStatement.executeQuery(BASIC_COUNT_QUERY)).andReturn(mockResultSet);
            
            EasyMock.expect(mockResultSet.next()).andReturn(true);
            EasyMock.expect(mockResultSet.getString("count")).andReturn("1");
            
            EasyMock.replay(mockConnection);
            EasyMock.replay(mockStatement);
            EasyMock.replay(mockResultSet);
    
            return mockConnection;
        }

        @Override
        public void releaseConnection(Connection connection) throws SQLException {
            //  TODO
        }
    }
}