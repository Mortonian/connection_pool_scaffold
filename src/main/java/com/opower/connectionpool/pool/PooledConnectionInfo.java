package com.opower.connectionpool.pool;

/**
 * All connections returned by {@link com.opower.connectionpool.ConnectionPool#releaseConnection(java.sql.Connection connection)} will be proxies that implement 
 * both {@link java.sql.Connection} as well as this class.This class supplies metadata around the underlying connection resources, as well as the proxy object 
 * that the client receives as a result of a call to {@link com.opower.connectionpool.ConnectionPool#releaseConnection(java.sql.Connection connection)}
 *
 */
public interface PooledConnectionInfo {

    /**
     * @return a unique identifier for this connection.  The identifier will be unique to the underlying {@link java.sql.Connection} that is connected to the database, 
     *         but not necessarily unique to the proxy object that the client code receives.  
     */
    public String getConnectionPoolUuid();

    /**
     * @return a unique identifier for the connection pool that this connection is part of.
     */
    public String getConnectionUuid();

    /**
     * @return whether or not this connection proxy object is still connected to the underlying connection resource, or if it has been disconnected 
     *         so that the resource can be leased to another client.  This method will only return false after the connection has been released, 
     *         or "invalidateLease" has been called.  Once the lease has been invalidated, all methods on the connection will result in an Exception being thrown.
     */
    public boolean isLeaseValid();

    /**
     *  This method disconnects the connection proxy from the underlying reusable database connection resource.  This does NOT return the connection 
     *  resource to the connection pool to be reused.  Only {@link com.opower.connectionpool.ConnectionPool#releaseConnection(java.sql.Connection connection)} does that.  
     *  Once the lease has been invalidated, all methods on the connection will result in an Exception being thrown.
     * 
     */
    public void invalidateLease();

    /**
     * @return The time at which the underlying {@link java.sql.Connection} resources was created, in milliseconds into the unix epoch.
     */
    public Long getTimeStampCreated();

    /**
     * @return The time at which this connection proxy was created and leased to the client, in milliseconds into the unix epoch.
     */
    public Long getTimeStampLeased();
}
