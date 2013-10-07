package com.opower.connectionpool.pool;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
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

public class MortonianConnectionPool implements ConnectionPool {

    private static Logger _log = Logger.getLogger(MortonianConnectionPool.class);

    private ConnectionConfig _connectionConfig;
    private ConnectionCreator _connectionCreator;
    private PoolConfig _poolConfig;
    private Map<String, ConnectionPoolEntry> _connectionEntries = new ConcurrentHashMap<String, ConnectionPoolEntry>();
    private Queue<String> _unleasedConnections  = new ConcurrentLinkedQueue<String>();
    private String _poolGuid = UUID.randomUUID().toString();
    
    public MortonianConnectionPool(ConnectionConfig connectionConfig, ConnectionCreator creator, PoolConfig poolConfig) {
        _connectionConfig = connectionConfig;
        _connectionCreator = creator;
        _poolConfig = poolConfig;

        int initialPoolSize = poolConfig.getInitialPoolSize();

        if (initialPoolSize > 0) {
            synchronized (this) {
                for (int i = 0; i < initialPoolSize; i++) {
                    try {
                        ConnectionPoolEntry newConnectionEntry = createNewConnectionEntry(false);
                        // we'll initialize inside the synchronized block, 
                        // because I don't want the constructor to complete till it's all initialized
                        initializeConnection(newConnectionEntry);
                    } catch (SQLException e) {
                        _log.error("Error provisioning initial connections "+e,e);
                        throw new RuntimeException(e);
                    }
                }
            }
        }
    }
    
    @Override
    public Connection getConnection() throws SQLException {
        ConnectionPoolEntry connectionEntry = getOrCreateInitializedConnectionEntry();
        if (null == connectionEntry) {
            return null;
        }
        acquireIncrementIfNecessary();
        return buildConnectionProxy(connectionEntry); 
    }

    private ConnectionPoolEntry getOrCreateInitializedConnectionEntry() throws SQLException {
        ConnectionPoolEntry connectionEntry = null;
        
        synchronized (this) {
            if (!_unleasedConnections.isEmpty()) {
                _log.debug("providing pre-created connection from pool");
                String unleasedConnectionUuid = _unleasedConnections.remove();
                connectionEntry = _connectionEntries.get(unleasedConnectionUuid);
                connectionEntry.setLeased(true);
                connectionEntry.setTimeStampLeased(System.currentTimeMillis());
            } else if (_connectionEntries.size() < _poolConfig.getMaxPoolSize()) {
                _log.debug("pool too small.  providing newly created connection from pool");
                // DO NOT initialize the connection here, because it's slow and we're in a synchronized block.
                // initialize outside of the block
                connectionEntry = createNewConnectionEntry(true);
            } else {
                // Or block, or throw exception, or ....
                _log.debug("All connections handed out.  Returning null, rather than providing connection.");
            }
        }
        
        if (null != connectionEntry && null == connectionEntry.getRawConnection()) {
            initializeConnection(connectionEntry);
        }
        return connectionEntry;
    }

    private synchronized ConnectionPoolEntry createNewConnectionEntry(boolean leased) {
        ConnectionPoolEntry connectionEntry = new ConnectionPoolEntry();
        connectionEntry.setTimeStampCreated(System.currentTimeMillis());
        _connectionEntries.put(connectionEntry.getConnectionUuid(), connectionEntry);
        connectionEntry.setLeased(leased);
        if (!leased) {
            _unleasedConnections.add(connectionEntry.getConnectionUuid());
        } else {
            connectionEntry.setTimeStampLeased(System.currentTimeMillis());
        }
        return connectionEntry;
    }

    private void initializeConnection(ConnectionPoolEntry connectionEntry) throws SQLException {
        if (null != connectionEntry && null == connectionEntry.getRawConnection()) {
            connectionEntry.setRawConnection(_connectionCreator.createConnection(_connectionConfig));
        }
    }

    private void acquireIncrementIfNecessary() throws SQLException {
        List<ConnectionPoolEntry> newEntries = new ArrayList<ConnectionPoolEntry>();
        if (_unleasedConnections.size() == 0) {
            while (shouldMoreBeProactivelyAcquired()) {
                // DO NOT initialize the connection here, because it's slow and we're in a synchronized block.
                // initialize outside of the block
                ConnectionPoolEntry createNewConnectionEntry = createNewConnectionEntry(false);
                newEntries.add(createNewConnectionEntry);
            }
        }
        for (ConnectionPoolEntry newEntry : newEntries) {
            initializeConnection(newEntry);
        }
    }
    
    private synchronized boolean shouldMoreBeProactivelyAcquired() {
        boolean shouldMoreBeProactivelyAcquired = (_connectionEntries.size() < _poolConfig.getMaxPoolSize()) && (_unleasedConnections.size() < _poolConfig.getAcquireIncrement());
        _log.debug("testing if we should proactively acquire connections.  Number created is "+_connectionEntries.size()
                +", max pool size is "+_poolConfig.getMaxPoolSize()
                +", connections available is "+_unleasedConnections.size()
                +", and acquire increment is "+_poolConfig.getAcquireIncrement()
                +" answer is "+shouldMoreBeProactivelyAcquired);
        return shouldMoreBeProactivelyAcquired;
    }

    @Override
    public void releaseConnection(Connection connection) throws SQLException {
        if (connection != null) {
            synchronized (this) { 
                PooledConnectionInfo connectionInfo = (PooledConnectionInfo) connection;
                connectionInfo.invalidateLease();
                
                String connectionUuid = connectionInfo.getConnectionUuid();
                ConnectionPoolEntry connectionPoolEntry = _connectionEntries.get(connectionUuid);
                connectionPoolEntry.setLeased(false);
                connectionPoolEntry.setTimeStampLeased(null);
                if (!_unleasedConnections.contains(connectionUuid)) {
                    _unleasedConnections.add(connectionUuid);
                }
            }
        }
    }

    private Connection buildConnectionProxy(ConnectionPoolEntry poolEntry) {
        return (Connection)Proxy.newProxyInstance(this.getClass().getClassLoader(), new Class[] { Connection.class, PooledConnectionInfo.class }, new ConnectionInvocationHandler(poolEntry));
    }

    private class ConnectionInvocationHandler implements InvocationHandler {
        
        private ConnectionPoolEntry _poolEntry;
        private PooledConnectionInfo _pooledConnectionInfo;

        protected ConnectionInvocationHandler(final ConnectionPoolEntry poolEntry) {
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
    
    private class ConnectionPoolEntry {

        private boolean _isLeased = true;
        private String _connectionUuid = UUID.randomUUID().toString(); 
        private Connection _rawConnection = null;
        private Long _timeStampCreated = null;
        private Long _timeStampLeased = null;
        
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

        public void setTimeStampCreated(Long timeStampCreated) {
            this._timeStampCreated = timeStampCreated;
        }

        public Long getTimeStampCreated() {
            return _timeStampCreated;
        }

        public void setTimeStampLeased(Long timeStampLeased) {
            this._timeStampLeased = timeStampLeased;
        }

        public Long getTimeStampLeased() {
            return _timeStampLeased;
        }
    }
    
    public synchronized int getNumberOfConnectionsAvailable() {
        // probably doesn't need to be synchronized, 
        // since every method on ConcurrentLinkedQueue should be atomic
        // but, better safe than sorry
        return _unleasedConnections.size();
    }
    
    public synchronized int getNumberOfConnectionsLeased() {
        int count = 0;
        for (ConnectionPoolEntry entry : _connectionEntries.values()) {
            if (entry.isLeased()) {
                count++;
            }   
        }
        return count;
    }
}
