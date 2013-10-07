package com.opower.connectionpool;

import java.sql.Connection;
import java.sql.SQLException;

public class MockConnectionCreator implements ConnectionCreator {
    
    private Connection _mockConnection;
    
    public MockConnectionCreator(Connection mockConnection) {
        _mockConnection = mockConnection;
    }

    @Override
    public Connection createConnection(ConnectionDescriptor connectionDescriptor) throws SQLException {
        return _mockConnection;
    }
}