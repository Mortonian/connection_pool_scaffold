package com.opower.connectionpool.poolconfig;

import org.apache.log4j.Logger;

import com.opower.connectionpool.PoolConfig;
import com.opower.connectionpool.connectionconfig.JsonFileConnectionConfig;
import com.opower.connectionpool.json.AbstractJsonFileConfigReader;

public class JsonFilePoolConfig extends AbstractJsonFileConfigReader implements PoolConfig {

    private static Logger _log = Logger.getLogger(JsonFileConnectionConfig.class);

    protected Logger getLogger() {
        return _log;
    }
    
    @Override
    public int getMaxPoolSize() {
        int maxPoolSize = 1;
        String maxPoolSizeString = (String) getOrReadJson().get("maxPoolSize");
        try {
            maxPoolSize = Integer.valueOf(maxPoolSizeString);
        } catch (Exception e) {
            _log.error("Trouble reading int from "+maxPoolSizeString, e);            
        }
        return maxPoolSize;
    }

}
