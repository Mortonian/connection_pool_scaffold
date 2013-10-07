package com.opower.connectionpool.json;

import java.io.FileReader;

import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public abstract class AbstractJsonFileConfigReader {

    private String _fileName;
    private JSONObject _parsedFile;

    public AbstractJsonFileConfigReader() {
        super();
    }

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
                getLogger().error("Could not read from file "+_fileName, e);
                throw new RuntimeException(e);
            }
        }
        return _parsedFile;
    }

    protected String getStringValue(String propertyName) {
        return (String) getOrReadJson().get(propertyName);
    }

    protected Boolean getBooleanValueWithDefault(String propertyName, boolean defaultValue) {
        String boolAsString = (String) getOrReadJson().get(propertyName);
        try {
            defaultValue = Boolean.valueOf(boolAsString);
        } catch (Exception e) {
            getLogger().error("Trouble reading boolean from "+propertyName+", value was "+boolAsString, e);            
        }
        return defaultValue;
    }
    
    protected int getIntWithDefault(String propertyName, int defaultValue) {
        String intAsString = (String) getOrReadJson().get(propertyName);
        try {
            defaultValue = Integer.valueOf(intAsString);
        } catch (Exception e) {
            getLogger().error("Trouble reading int from "+propertyName+", value was "+intAsString, e);            
        }
        return defaultValue;
    }

    protected abstract Logger getLogger();
}