package com.opower.connectionpool;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import junit.framework.Assert;

import org.apache.log4j.Logger;
import org.junit.Test;

import com.opower.connectionpool.ConnectionPool;

public class TestConnectionPool {
    
    private static final String HOSTNAME = "hostname";
    private static final String DATABASE = "database";
    private static final String USERNAME = "username";

    private static Logger _log = Logger.getLogger(TestConnectionPool.class);
    
    @Test
    public void testStuff() {        
        try {
            String url = "jdbc:postgresql://"+HOSTNAME+"/"+DATABASE+"?"+ "user="+USERNAME;

            Class.forName("org.postgresql.Driver");
            DumbConnectionPool innerClass = new DumbConnectionPool();
            innerClass.setUrl(url);
            Connection connect = innerClass.getConnection();
            Statement statement = connect.createStatement();
            ResultSet resultSet = statement.executeQuery("select count(*) from table");
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
            return DriverManager.getConnection(_url);
        }

        @Override
        public void releaseConnection(Connection connection) throws SQLException {
            //  TODO
        }
    }
}