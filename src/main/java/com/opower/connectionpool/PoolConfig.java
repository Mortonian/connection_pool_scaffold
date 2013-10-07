package com.opower.connectionpool;

public interface PoolConfig {

    public int getMaxPoolSize();
    public int getAcquireIncrement();
    public int getInitialPoolSize();
}
