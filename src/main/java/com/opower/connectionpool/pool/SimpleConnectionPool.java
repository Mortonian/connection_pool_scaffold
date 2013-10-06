package com.opower.connectionpool.pool;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

import org.apache.log4j.Logger;

import com.opower.connectionpool.ConnectionCreator;
import com.opower.connectionpool.ConnectionDescriptor;
import com.opower.connectionpool.ConnectionPool;

public class SimpleConnectionPool implements ConnectionPool {

    private static Logger _log = Logger.getLogger(SimpleConnectionPool.class);

    public ConnectionDescriptor _desciptor;
    public ConnectionCreator _creator;
    public int _poolSize = 1;
    public Set<Connection> _connectionsHandedOut = new HashSet<Connection>();
    public Queue<Connection> _connectionsAvailable  = new LinkedList<Connection>();
    
    public SimpleConnectionPool(ConnectionDescriptor desciptor, ConnectionCreator creator, int poolSize) {
        _desciptor = desciptor;
        _creator = creator;
        _poolSize = poolSize;
    }
    
    @Override
    public Connection getConnection() throws SQLException {
        if (!_connectionsAvailable.isEmpty()) {
            _log.debug("providing pre-created connection from pool");
            Connection connection = _connectionsAvailable.remove();
            _connectionsHandedOut.add(connection);
            return connection;
        } else {
            if (_connectionsHandedOut.size() < _poolSize) {
                _log.debug("pool too small.  providing newly created connection from pool");
                Connection connection = _creator.createConnection(_desciptor);
                _connectionsHandedOut.add(connection);
                return connection;
            } else {
                _log.debug("All connections handed out.  Returning null, rather than providing connection.");
                return null; // Or block, or throw exception....
            }
        }
    }

    @Override
    public void releaseConnection(Connection connection) throws SQLException {
        if (connection != null) {
            _connectionsHandedOut.remove(connection);
            _connectionsAvailable.add(connection);
        }
    }
    
    public int getNumberOfConnectionsAvailable() {
        return _connectionsAvailable.size();
    }
    
    public int getNumberOfConnectionsHandedOut() {
        return _connectionsHandedOut.size();
    }

}
