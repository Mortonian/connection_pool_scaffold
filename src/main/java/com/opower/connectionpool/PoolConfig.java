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
     *         Default value is 0.  (Note: may create less than this number so as to avoid violating {@link #getMaxPoolSize()})
     */
    public int getAcquireIncrement();
    
    /**
     * @return When the pool is initially constructed, create this many connections before users can begin to call {@link ConnectionPool#getConnection()}
     */
    public int getInitialPoolSize();
}
