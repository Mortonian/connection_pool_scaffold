package com.opower.connectionpool.pool;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;

import org.apache.log4j.Logger;

import com.opower.connectionpool.ConnectionCreator;
import com.opower.connectionpool.ConnectionDescriptor;
import com.opower.connectionpool.ConnectionPool;

public class SimpleConnectionPool implements ConnectionPool {

    private static Logger _log = Logger.getLogger(SimpleConnectionPool.class);

    private ConnectionDescriptor _desciptor;
    private ConnectionCreator _creator;
    private int _poolSize = 1;
    private Set<ConnectionPoolEntry> _connectionsHandedOut = new HashSet<ConnectionPoolEntry>();
    private Queue<ConnectionPoolEntry> _connectionsAvailable  = new LinkedList<ConnectionPoolEntry>();
    private String _poolGuid = UUID.randomUUID().toString();
    
    public SimpleConnectionPool(ConnectionDescriptor desciptor, ConnectionCreator creator, int poolSize) {
        _desciptor = desciptor;
        _creator = creator;
        _poolSize = poolSize;
    }
    
    @Override
    public Connection getConnection() throws SQLException {
        if (!_connectionsAvailable.isEmpty()) {
            _log.debug("providing pre-created connection from pool");
            ConnectionPoolEntry poolEntry = _connectionsAvailable.remove();
            _connectionsHandedOut.add(poolEntry);
            return buildProxiedConnection(poolEntry);
        } else {
            if (_connectionsHandedOut.size() < _poolSize) {
                _log.debug("pool too small.  providing newly created connection from pool");
                ConnectionPoolEntry poolEntry = createConnectionPoolEntry();
                _connectionsHandedOut.add(poolEntry);
                return buildProxiedConnection(poolEntry);
            } else {
                _log.debug("All connections handed out.  Returning null, rather than providing connection.");
                return null; // Or block, or throw exception, or ....
            }
        }
    }

    public class ConnectionInvocationHandler implements InvocationHandler {
        
        private ConnectionPoolEntry _poolEntry;
        private PooledConnectionInfo _pooledConnectionInfo;

        public ConnectionInvocationHandler(final ConnectionPoolEntry poolEntry) {
            _poolEntry = poolEntry;
            _pooledConnectionInfo = buildPooledConnectionInfo(poolEntry);
        }
        
        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            
            if (PooledConnectionInfo.class.equals(method.getDeclaringClass())) {
                return method.invoke(_pooledConnectionInfo, args);
            } else if (_pooledConnectionInfo.isLeaseValid()) {
                return method.invoke(_poolEntry.getRawConnection(), args);
            } else {
                String message = "Lease on Connection "+_pooledConnectionInfo.getConnectionUuid()+" no longer valid";
                _log.error(message);
                throw new RuntimeException(message);
            }
        }
        
    }

    private PooledConnectionInfo buildPooledConnectionInfo(final ConnectionPoolEntry poolEntry) {
        return new PooledConnectionInfo() {

            @Override
            public String getConnectionPoolUuid() {
                return _poolGuid;
            }

            @Override
            public String getConnectionUuid() {
                return poolEntry.getConnectionUuid();
            }

            @Override
            public void invalidateLease() {
                poolEntry.setLeased(false);
            }

            @Override
            public boolean isLeaseValid() {
                return poolEntry.isLeased();
            }
            
        };
    }
    
    public class ConnectionPoolEntry {

        private boolean _isLeased;
        private String _connectionUuid = UUID.randomUUID().toString(); 
        private Connection _rawConnection;
        
        public ConnectionPoolEntry(Connection rawConnection) {
            _rawConnection = rawConnection;
        }

        public String getConnectionUuid() {
            return _connectionUuid;
        }

        public boolean isLeased() {
            return _isLeased;
        }
        
        public void setLeased(boolean leaseValid) {
            _isLeased = leaseValid;
        }
        
        public Connection getRawConnection() {
            return _rawConnection;
        }
        
    }

    private ConnectionPoolEntry createConnectionPoolEntry() throws SQLException {
        Connection rawConnection = _creator.createConnection(_desciptor);
        ConnectionPoolEntry enrty = new ConnectionPoolEntry(rawConnection);
        enrty.setLeased(true);
        return enrty;
    }

    private Connection buildProxiedConnection(ConnectionPoolEntry poolEntry) {
        return (Connection)Proxy.newProxyInstance(this.getClass().getClassLoader(), new Class[] { Connection.class, PooledConnectionInfo.class }, new ConnectionInvocationHandler(poolEntry));
    }

    @Override
    public void releaseConnection(Connection connection) throws SQLException {
        if (connection != null) {
            PooledConnectionInfo connectionInfo = (PooledConnectionInfo) connection;
            connectionInfo.invalidateLease();
            for (ConnectionPoolEntry connectionHandedOut : _connectionsHandedOut) {
                if (connectionHandedOut.getConnectionUuid().equals(connectionInfo.getConnectionUuid())) {
                    _connectionsHandedOut.remove(connectionHandedOut);
                    _connectionsAvailable.add(connectionHandedOut);
                }
            }
        }
    }
    
    public int getNumberOfConnectionsAvailable() {
        return _connectionsAvailable.size();
    }
    
    public int getNumberOfConnectionsHandedOut() {
        return _connectionsHandedOut.size();
    }

}
