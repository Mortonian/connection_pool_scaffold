package com.opower.connectionpool.connectionconfig;

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
        return (String) getOrReadJson().get("driverClass");
    }

    @Override
    public String getJdbcUrl() {
        return (String) getOrReadJson().get("jdbcUrl");
    }

    @Override
    public String getPassword() {
        return (String) getOrReadJson().get("password");
    }

    @Override
    public String getUser() {
        return (String) getOrReadJson().get("user");
    }

}
