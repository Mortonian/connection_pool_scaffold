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

    protected abstract Logger getLogger();
}