package com.opower.connectionpool.descriptor;

import java.io.FileReader;

import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.opower.connectionpool.ConnectionDescriptor;

public class JsonFileConnectionDescriptor implements ConnectionDescriptor {

    private static Logger _log = Logger.getLogger(JsonFileConnectionDescriptor.class);
    
    private String _fileName;
    private JSONObject _parsedFile;
    
    public void setFile(String fileName) {
        _fileName = fileName;
    }

    public String getFile() {
        return _fileName;
    }

    public JSONObject getOrReadJson() {
        if (_parsedFile == null) {
            try {
                _parsedFile = (JSONObject) (new JSONParser()).parse(new FileReader(_fileName));
            } catch (Exception e) {
                _log.error("Could not read from file "+_fileName, e);
                throw new RuntimeException(e);
            }
        }
        return _parsedFile;
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
