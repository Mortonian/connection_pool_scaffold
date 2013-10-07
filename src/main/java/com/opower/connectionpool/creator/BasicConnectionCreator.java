package com.opower.connectionpool.creator;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.apache.log4j.Logger;

import com.opower.connectionpool.ConnectionCreator;
import com.opower.connectionpool.ConnectionConfig;

public class BasicConnectionCreator implements ConnectionCreator {

    private static Logger _log = Logger.getLogger(BasicConnectionCreator.class);
    
    @Override
    public Connection createConnection(ConnectionConfig connectionConfig) throws SQLException {

        String driverClass = connectionConfig.getDriverClass();
        String jdbcUrl = connectionConfig.getJdbcUrl();
        String user = connectionConfig.getUser();
        String password = connectionConfig.getPassword();
        
        try {            
            Class.forName(driverClass);
            _log.debug("Loaded JDBC driver class "+driverClass);
            if (password != null) {
                Connection connection = DriverManager.getConnection(jdbcUrl, user, password);
                _log.debug("Created connection with url "+jdbcUrl+" and user "+user); // no, we're not logging password!
                return connection;
            } else {
                Connection connection = DriverManager.getConnection(jdbcUrl);
                _log.debug("Created connection with url "+jdbcUrl); 
                return connection;
            }
        } catch (ClassNotFoundException e) {
            _log.error("Error creating connection", e); 
            throw new SQLException(e);
        }
    }
}