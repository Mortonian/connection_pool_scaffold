package com.opower.connectionpool;

/**
 * An object of this class specifies the pooling behavior of a {@link ConnectionPool}
 *
 */
public interface PoolConfig {

    /**
     * @return The maximum size of connections that a pool can make.  Default value is 1.
     */
    public int getMaxPoolSize();
    
    /**
     * @return When the number of unleased connections reaches zero, allocate this many more before another call to {@link ConnectionPool#getConnection()} is made.  
     *         Default value is 0.  (<B>Note:</B> may create less than this number so as to avoid violating {@link #getMaxPoolSize()})
     */
    public int getAcquireIncrement();
    
    /**
     * @return When the pool is initially constructed, create this many connections before users can begin to call {@link ConnectionPool#getConnection()}.  Default value is 0.
     */
    public int getInitialPoolSize();
    
    /**
     * @return Should {@link java.sql.Connection.commit()} be called on all leased connections before release or shutdown.  Default value is false.
     */
    public boolean getAutoCommit();
    
    /**
     * @return If all connections are leased, how long to wait before trying again.  Default value is 300ms.  
     * <B>Note:</B> The wait is accomplished by Thread.sleep, which is interruptible and blocks the thread.  
     * This is clearly the wrong way to do this, but it's the best I can do for now.  Consider yourself warned.
     */
    public int getRetryWaitTimeInMillis();
    
    /**
     * @return If all connections are leased, how many more times to try before returning null.  Default value is 0. 
     */
    public int getRetryAttempts();

    /**
     * <B>Experimental</B>
     * 
     * Maximum age in milliseconds that a connection will be alive before it is no longer deemed usable, and hence will be closed.  
     * <B>Note:</B> actively leased connections will not be closed.  Only available, but unleased, connections in the pool will be closed.
     * <B>Also Note:</B> We've not yet implemented a timer thread, so these will be closed out when they are inspected during a call to {@link ConnectionPool.#getConnection()}.
     * This is probably invalid, but for now it is what it is. 
     *         
     * @return Maximum age in milliseconds that a connection will be alive before it is no longer deemed usage, and hence will be closed.  
     *         Any value less than or equal to zero will be interpretted as "infinity".  Default value is -1.  
     * 
     */
    public int getMaxConnectionAgeInMillis();

    /**
     * <B>Experimental</B>
     * 
     * Maximum time in milliseconds since a connection was released before it is no longer deemed usable, and hence will be closed.  
     * <B>Note:</B> actively leased connections will not be closed.  Only available, but unleased, connections in the pool will be closed.
     * <B>Also Note:</B> We've not yet implemented a timer thread, so these will be closed out when they are inspected during a call to {@link ConnectionPool.#getConnection()}.
     * This is probably invalid, but for now it is what it is. 
     *         
     * @return Maximum age in milliseconds that a connection will be alive before it is no longer deemed usage, and hence will be closed.  
     *         Any value less than or equal to zero will be interpretted as "infinity".  Default value is -1.  
     * 
     */
    public int getMaxIdleTimeInMillis();
}
