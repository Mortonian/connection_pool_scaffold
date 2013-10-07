package com.opower.connectionpool.pool;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.log4j.Logger;

import com.opower.connectionpool.ConnectionCreator;
import com.opower.connectionpool.ConnectionConfig;
import com.opower.connectionpool.ConnectionPool;
import com.opower.connectionpool.PoolConfig;

public class SimpleConnectionPool implements ConnectionPool {

    private static Logger _log = Logger.getLogger(SimpleConnectionPool.class);

    private ConnectionConfig _connectionConfig;
    private ConnectionCreator _connectionCreator;
    private PoolConfig _poolConfig;
    private Map<String, ConnectionPoolEntry> _createdConnections = new ConcurrentHashMap<String, ConnectionPoolEntry>();
    private Queue<String> _connectionsAvailable  = new ConcurrentLinkedQueue<String>();
    private String _poolGuid = UUID.randomUUID().toString();
    
    public SimpleConnectionPool(ConnectionConfig connectionConfig, ConnectionCreator creator, PoolConfig poolConfig) {
        _connectionConfig = connectionConfig;
        _connectionCreator = creator;
        _poolConfig = poolConfig;
        
        int initialPoolSize = poolConfig.getInitialPoolSize();
        if (initialPoolSize > 0) {
            for (int i = 0; i < initialPoolSize; i++) {
                try {
                    creatNewConnectionEntryAndInitializeConnection();
                } catch (SQLException e) {
                    _log.error("Error provisioning initial connections "+e,e);
                    throw new RuntimeException(e);
                }
            }
        }
    }
    
    @Override
    public Connection getConnection() throws SQLException {
        ConnectionPoolEntry connectionEntry = getConnectionEntry();
        if (null == connectionEntry) {
            return null;
        }
        if (_connectionsAvailable.size() == 0) {
            while (shouldMoreBeProactivelyAcquired()) {
                creatNewConnectionEntryAndInitializeConnection();
            }
        }
        return buildProxiedConnection(connectionEntry); 
    }

    private synchronized boolean shouldMoreBeProactivelyAcquired() {
        boolean shouldMoreBeProactivelyAcquired = (_createdConnections.size() < _poolConfig.getMaxPoolSize()) && (_connectionsAvailable.size() < _poolConfig.getAcquireIncrement());
        _log.debug("testing if we should proactively acquire connections.  Number created is "+_createdConnections.size()
                +", max pool size is "+_poolConfig.getMaxPoolSize()
                +", connections available is "+_connectionsAvailable.size()
                +", and acquire increment is "+_poolConfig.getAcquireIncrement()
                +" answer is "+shouldMoreBeProactivelyAcquired);
        return shouldMoreBeProactivelyAcquired;
    }

    private ConnectionPoolEntry getConnectionEntry() throws SQLException {
        ConnectionPoolEntry connectionEntry = null;
        
        synchronized (this) {
            if (!_connectionsAvailable.isEmpty()) {
                _log.debug("providing pre-created connection from pool");
                String availableConnectionUuid = _connectionsAvailable.remove();
                connectionEntry = _createdConnections.get(availableConnectionUuid);
                connectionEntry.setLeased(true);
            } else {
                if (_createdConnections.size() < _poolConfig.getMaxPoolSize()) {
                    _log.debug("pool too small.  providing newly created connection from pool");
                    connectionEntry = createNewConnectionEntry(true);
                } else {
                    // Or block, or throw exception, or ....
                    _log.debug("All connections handed out.  Returning null, rather than providing connection.");
                }
            }
        }
        
        if (null != connectionEntry && null == connectionEntry.getRawConnection()) {
            connectionEntry.setRawConnection(_connectionCreator.createConnection(_connectionConfig));
        }
        return connectionEntry;
    }

    private void creatNewConnectionEntryAndInitializeConnection() throws SQLException {
        ConnectionPoolEntry newConnectionEntry = createNewConnectionEntry(false);
        newConnectionEntry.setRawConnection(_connectionCreator.createConnection(_connectionConfig));
    }

    private synchronized ConnectionPoolEntry createNewConnectionEntry(boolean lease) {
        ConnectionPoolEntry connectionEntry = new ConnectionPoolEntry();
        _createdConnections.put(connectionEntry.getConnectionUuid(), connectionEntry);
        connectionEntry.setLeased(lease);
        if (!lease) {
            _connectionsAvailable.add(connectionEntry.getConnectionUuid());
        }
        return connectionEntry;
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
            
            private boolean isLeaseValid = poolEntry.isLeased();

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
                isLeaseValid = false;
            }

            @Override
            public boolean isLeaseValid() {
                return isLeaseValid;
            }
            
        };
    }
    
    public class ConnectionPoolEntry {

        private boolean _isLeased = true;
        private String _connectionUuid = UUID.randomUUID().toString(); 
        private Connection _rawConnection = null;
        
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
        
        public void setRawConnection(Connection rawConnection) {
            _rawConnection = rawConnection;
        }
    }

    private Connection buildProxiedConnection(ConnectionPoolEntry poolEntry) {
        return (Connection)Proxy.newProxyInstance(this.getClass().getClassLoader(), new Class[] { Connection.class, PooledConnectionInfo.class }, new ConnectionInvocationHandler(poolEntry));
    }

    @Override
    public void releaseConnection(Connection connection) throws SQLException {
        if (connection != null) {
            synchronized (this) { 
                PooledConnectionInfo connectionInfo = (PooledConnectionInfo) connection;
                connectionInfo.invalidateLease();
                
                String connectionUuid = connectionInfo.getConnectionUuid();
                ConnectionPoolEntry connectionPoolEntry = _createdConnections.get(connectionUuid);
                connectionPoolEntry.setLeased(false);
                if (!_connectionsAvailable.contains(connectionUuid)) {
                    _connectionsAvailable.add(connectionUuid);
                }
            }
        }
    }
    
    public synchronized int getNumberOfConnectionsAvailable() {
        // probably doesn't need to be synchronized, 
        // since every method on ConcurrentLinkedQueue should be atomic
        // but, better safe than sorry
        return _connectionsAvailable.size();
    }
    
    public synchronized int getNumberOfConnectionsLeased() {
        int count = 0;
        for (ConnectionPoolEntry entry : _createdConnections.values()) {
            if (entry.isLeased()) {
                count++;
            }   
        }
        return count;
    }

}
