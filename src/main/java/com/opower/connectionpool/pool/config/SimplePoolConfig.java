package com.opower.connectionpool.pool.config;

import com.opower.connectionpool.PoolConfig;

public class SimplePoolConfig implements PoolConfig {

    private int _maxPoolSize = 1;
    private int _acquireIncrement = 0;
    private int _initialPoolSize = 0;
    private boolean _autoCommit = false;
    private int _retryAttempts = 0;
    private int _retryWaitTimeMillis = 300;
    private int _maxConnectionAgeInMillis = -1;
    private int _maxIdleTimeInMillis = -1;
    
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

    @Override
    public boolean getAutoCommit() {
        return _autoCommit;
    }

    public void setAutoCommit(boolean autoCommit) {
        _autoCommit = autoCommit;
    }

    @Override
    public int getRetryAttempts() {
        return _retryAttempts;
    }

    public void setRetryAttempts(int retryAttempts) {
        _retryAttempts = retryAttempts;
    }

    @Override
    public int getRetryWaitTimeInMillis() {
        return _retryWaitTimeMillis;
    }

    public void setRetryWaitTimeInMillis(int retryWaitTimeMillis) {
        _retryWaitTimeMillis = retryWaitTimeMillis;
    }

    @Override
    public int getMaxConnectionAgeInMillis() {
        return _maxConnectionAgeInMillis;
    }

    public void setMaxConnectionAgeInMillis(int maxConnectionAgeInMillis) {
        _maxConnectionAgeInMillis = maxConnectionAgeInMillis;
    }

    @Override
    public int getMaxIdleTimeInMillis() {
        return _maxIdleTimeInMillis;
    }
    
    public void setMaxIdleTimeInMillis(int maxIdleTimeInMillis) {
        _maxIdleTimeInMillis = maxIdleTimeInMillis;
    }
}
