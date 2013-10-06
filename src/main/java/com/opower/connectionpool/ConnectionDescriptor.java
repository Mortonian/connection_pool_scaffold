package com.opower.connectionpool;

public interface ConnectionDescriptor {
    
    public String getDriverClass(); 
    public String getJdbcUrl(); 
    public String getUser();
    public String getPassword();
}
