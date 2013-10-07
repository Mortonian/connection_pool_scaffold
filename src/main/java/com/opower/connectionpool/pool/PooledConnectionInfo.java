package com.opower.connectionpool.pool;

public interface PooledConnectionInfo {

    public String getConnectionPoolUuid();
    public String getConnectionUuid();
    public boolean isLeaseValid();
    public void invalidateLease();
}
