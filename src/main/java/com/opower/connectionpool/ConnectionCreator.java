package com.opower.connectionpool;

import java.sql.Connection;
import java.sql.SQLException;

public interface ConnectionCreator {

    Connection createConnection(ConnectionDescriptor connectionDescriptor) throws SQLException;
}
