package com.opower.connectionpool.pool.config;

import com.opower.connectionpool.PoolConfig;

public class SimplePoolConfig implements PoolConfig {

    private int _maxPoolSize = 1;
    
    @Override
    public int getMaxPoolSize() {
        return _maxPoolSize;
    }
    
    public void setMaxPoolSize(int maxPoolSize) {
        _maxPoolSize = maxPoolSize;
    }

}
