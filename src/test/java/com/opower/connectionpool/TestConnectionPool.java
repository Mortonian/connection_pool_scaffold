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

            SimpleConnectionDescriptor descriptor = new SimpleConnectionDescriptor();
            Class.forName("org.postgresql.Driver");
            descriptor.setJdbcUrl(url);
            DumbConnectionCreator connectionCreator = new DumbConnectionCreator();
            NonPoolingConnectionPool pool = new NonPoolingConnectionPool(descriptor, connectionCreator);
            Connection connect = pool.getConnection();
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
    
    public static class NonPoolingConnectionPool implements ConnectionPool {
        
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
    
    public static class SimpleConnectionDescriptor implements ConnectionDescriptor {

        private String _driverClass;
        private String _jdbcUrl;
        private String _password;
        private String _user;

        @Override
        public String getDriverClass() {
            return _driverClass;
        }

        public void setDriverClass(String driverClass) {
            _driverClass = driverClass;
        }

        @Override
        public String getJdbcUrl() {
            return _jdbcUrl;
        }
        
        public void setJdbcUrl(String url) {
            _jdbcUrl = url;
        }

        @Override
        public String getPassword() {
            return _password;
        }
        
        public void setPassword(String password) {
            _password = password;
        }

        @Override
        public String getUser() {
            return _user;
        }
        
        public void getUser(String user) {
            _user = user;
        }
    }
    
    public static class DumbConnectionCreator implements ConnectionCreator {

        @Override
        public Connection createConnection(ConnectionDescriptor connectionDescriptor) throws SQLException {
            
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
    }
}