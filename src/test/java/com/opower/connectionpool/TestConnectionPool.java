package com.opower.connectionpool;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.junit.Test;

import com.opower.connectionpool.ConnectionPool;

public class TestConnectionPool {
    
    private static final String HOSTNAME = "hostname";
    private static final String DATABASE = "database";
    private static final String USERNAME = "username";

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
    
            Connection connect = DriverManager.getConnection(_url);
            return connect;
        }

        @Override
        public void releaseConnection(Connection connection) throws SQLException {
            //  TODO
        }
    }
}