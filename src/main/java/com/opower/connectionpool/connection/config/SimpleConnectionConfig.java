package com.opower.connectionpool.connection.config;

import com.opower.connectionpool.ConnectionConfig;

public class SimpleConnectionConfig implements ConnectionConfig {

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
    
    public void setUser(String user) {
        _user = user;
    }
}