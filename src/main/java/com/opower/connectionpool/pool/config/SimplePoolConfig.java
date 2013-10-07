package com.opower.connectionpool.pool.config;

import com.opower.connectionpool.PoolConfig;

public class SimplePoolConfig implements PoolConfig {

    private int _maxPoolSize = 1;
    private int _acquireIncrement = 0;
    private int _initialPoolSize = 0;
    
    @Override
    public int getMaxPoolSize() {
        return _maxPoolSize;
    }
    
    public void setMaxPoolSize(int maxPoolSize) {
        _maxPoolSize = maxPoolSize;
    }

    @Override
    public int getAcquireIncrement() {
        return _acquireIncrement;
    }

    public void setAcquireIncrement(int acquireIncrement) {
        _acquireIncrement = acquireIncrement;
    }

    @Override
    public int getInitialPoolSize() {
        return _initialPoolSize;
    }

    public void setInitialPoolSize(int initialPoolSize) {
        _initialPoolSize = initialPoolSize;
    }

}
