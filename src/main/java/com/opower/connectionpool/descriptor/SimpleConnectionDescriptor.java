package com.opower.connectionpool.descriptor;

import com.opower.connectionpool.ConnectionDescriptor;

public class SimpleConnectionDescriptor implements ConnectionDescriptor {

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