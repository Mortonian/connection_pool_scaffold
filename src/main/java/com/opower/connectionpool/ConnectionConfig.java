package com.opower.connectionpool;

public interface ConnectionConfig {
    
    public String getDriverClass(); 
    public String getJdbcUrl(); 
    public String getUser();
    public String getPassword();
}
