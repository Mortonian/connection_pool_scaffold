package com.opower.connectionpool;

/**
 * This class represents the basic information needed for JDBC to connect to the database.
 *
 */
public interface ConnectionConfig {
    
    /**
     * @return The JDBC driver class we should use to connect to the database
     */
    public String getDriverClass(); 
    
    /**
     * @return the JDBC Url we should use to connect to the database
     */
    public String getJdbcUrl(); 
    
    /**
     * @return the username we should use to connect to the database.  If null, only the {@link #getJdbcUrl()} is used 
     */
    public String getUser();
    
    /**
     * @return the password we should use to connect to the database.  Not used if {@link #getUser()} is null.
     */
    public String getPassword();
}
