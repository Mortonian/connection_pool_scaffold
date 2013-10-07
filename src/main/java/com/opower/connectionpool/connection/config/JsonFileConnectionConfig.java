package com.opower.connectionpool.connection.config;

import org.apache.log4j.Logger;

import com.opower.connectionpool.ConnectionConfig;
import com.opower.connectionpool.json.AbstractJsonFileConfigReader;

public class JsonFileConnectionConfig extends AbstractJsonFileConfigReader implements ConnectionConfig {

    private static Logger _log = Logger.getLogger(JsonFileConnectionConfig.class);
    
    protected Logger getLogger() {
        return _log;
    }
    
    @Override
    public String getDriverClass() {
        return getStringValue("driverClass");
    }

    @Override
    public String getJdbcUrl() {
        return getStringValue("jdbcUrl");
    }

    @Override
    public String getPassword() {
        return getStringValue("password");
    }

    @Override
    public String getUser() {
        return getStringValue("user");
    }
}
