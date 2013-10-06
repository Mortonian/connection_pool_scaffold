package com.opower.connectionpool;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import org.junit.Test;

public class TestConnectionPool {
    
    private static final String HOSTNAME = "hostname";
    private static final String DATABASE = "database";
    private static final String USERNAME = "username";

    @Test
    public void testStuff() {        
        try {
            Class.forName("org.postgresql.Driver");
            Connection connect = DriverManager.getConnection("jdbc:postgresql://"+HOSTNAME+"/"+DATABASE+"?"+ "user="+USERNAME);
            Statement statement = connect.createStatement();
            ResultSet resultSet = statement.executeQuery("select count(*) from table");
            resultSet.next();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}