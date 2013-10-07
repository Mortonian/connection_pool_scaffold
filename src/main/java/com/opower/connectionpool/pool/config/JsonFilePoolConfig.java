package com.opower.connectionpool.pool.config;

import org.apache.log4j.Logger;

import com.opower.connectionpool.PoolConfig;
import com.opower.connectionpool.connection.config.JsonFileConnectionConfig;
import com.opower.connectionpool.json.AbstractJsonFileConfigReader;

public class JsonFilePoolConfig extends AbstractJsonFileConfigReader implements PoolConfig {

    private static Logger _log = Logger.getLogger(JsonFileConnectionConfig.class);

    protected Logger getLogger() {
        return _log;
    }
    
    @Override
    public int getMaxPoolSize() {
        return getIntWithDefault("maxPoolSize", 1);
    }

    @Override
    public int getAcquireIncrement() {
        return getIntWithDefault("acquireIncrement", 0);
    }

    @Override
    public int getInitialPoolSize() {
        return getIntWithDefault("initialPoolSize", 0);
    }

    @Override
    public boolean getAutoCommit() {
        return getBooleanValueWithDefault("autoCommit", false);
    }

    @Override
    public int getRetryAttempts() {
        return getIntWithDefault("retryAttempts", 0);
    }

    @Override
    public int getRetryWaitTimeInMillis() {
        return getIntWithDefault("retryWaitTimeInMillis", 300);
    }
}
