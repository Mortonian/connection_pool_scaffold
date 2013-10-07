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

/**
 * An implementation of com.opower.connectionpool.ConnectionPool.
 * This class uses a {@link ConnectionConfig}, {@link  ConnectionCreator}, and a {@link PoolConfig} 
 * together to achieve the defined connection pooling behavior. 
 *
 */
public class MortonianConnectionPool implements ConnectionPool {

    private static Logger _log = Logger.getLogger(MortonianConnectionPool.class);

    private ConnectionConfig _connectionConfig;
    private ConnectionCreator _connectionCreator;
    private boolean _shutdown = true;
    private PoolConfig _poolConfig;
    private Map<String, ConnectionPoolEntry> _connectionEntries = new ConcurrentHashMap<String, ConnectionPoolEntry>();
    private Queue<String> _unleasedConnections  = new ConcurrentLinkedQueue<String>();
    private String _poolGuid = UUID.randomUUID().toString();
    
    /**
     * @param connectionConfig specified the JDBC parameters specified how new connections to the databases should be made
     * @param creator given a {@link ConnectionConfig}, connects to the database.  Main implementation is {@link MockConnectionCreator}, 
     *                but other implementations can be used for unit testing, etc.
     * @param poolConfig contains configurations specifying the behaviour of the pool itself.
     */
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
        
        _shutdown = false;
    }

    /**
     * Gets a connection from the connection pool.  Returns null if the pool has reached its maximum size.  Ever Connection returned is actually a proxy of 
     * the underlying {@link Connection} resource, which will allow the ConnectionPool to invalidate the lease and disconnect the proxy from the database,
     * without disconnecting the underlying {@link Connection} resource.  
     * 
     * These proxies will also implement {@link PooledConnectionInfo}.
     * 
     * @return a valid connection from the pool.
     */
    @Override
    public Connection getConnection() throws SQLException {
        if (_shutdown) {
            throw new RuntimeException("Can't grant new connections ... we're shut down!");
        }
        ConnectionPoolEntry connectionEntry = getOrCreateInitializedConnectionEntry();
        if (null == connectionEntry) {
            for (int i = 0; i < _poolConfig.getRetryAttempts(); i++) {
                try {
                    Thread.sleep(_poolConfig.getRetryWaitTimeInMillis()); // ugh.  this is so not the way to do this.
                } catch (InterruptedException e) {
                    _log.error("someone has awoken my slumber", e);
                }  
                connectionEntry = getOrCreateInitializedConnectionEntry();
                if (null != connectionEntry) {
                    break;
                }
            }
            if (null == connectionEntry) {
                return null;
            }
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

    /**
     *  Release the passed Connection.  Throws an exception if the connection did not come from this pool     *  
     */
    @Override
    public void releaseConnection(Connection connection) throws SQLException {
        if (connection != null) {
            synchronized (this) { 
                PooledConnectionInfo connectionInfo = (PooledConnectionInfo) connection;
                
                if (!_poolGuid.equals(connectionInfo.getConnectionPoolUuid())) {
                    _log.error("Cannot release connection from another pool.  This pools uuid is "+_poolGuid+", but the connection's was "+connectionInfo.getConnectionPoolUuid());
                    throw new RuntimeException("Cannot release connection from another pool.  This pools uuid is "+_poolGuid+", but the connection's was "+connectionInfo.getConnectionPoolUuid());
                }
                
                if (_poolConfig.getAutoCommit()) {
                    connection.commit();
                }
                
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

    /**
     * @return whether or not this Connection Pool is shutdown
     */
    public boolean isShutdown() {
        return _shutdown;
    }
    
    /**
     * Shuts down the connection pool by releasing all leased connections, and then preventing any new connections from getting leased.
     * 
     * @throws SQLException if there are problems releasing leased connections
     */
    public synchronized void shutdown() throws SQLException {
        if (!isShutdown()) {
            for (ConnectionPoolEntry entry : _connectionEntries.values()) {
                if (entry.isLeased()) {
                    releaseConnection(buildConnectionProxy(entry));
                }
            }
            _shutdown = true;
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
            
            private boolean _isLeaseValid = poolEntry.isLeased();
            private Long _timeStampCreated = poolEntry.getTimeStampCreated();
            private Long _timeStampLeased = poolEntry.getTimeStampLeased();

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
                _isLeaseValid = false;
            }

            @Override
            public boolean isLeaseValid() {
                return _isLeaseValid;
            }

            @Override
            public Long getTimeStampCreated() {
                return _timeStampCreated;
            }

            @Override
            public Long getTimeStampLeased() {
                return _timeStampLeased;
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
    
    /**
     * @return the number of pre-built {@link Connection} resources that the pool has, ready to lease.  Note that {@link #getNumberOfConnectionsLeased()} +
     * and {@link #getNumberOfConnectionsAvailable()} does not necessarily add up to the max size.  This is only the ready-to-go, pre-built connections.
     */
    public synchronized int getNumberOfConnectionsAvailable() {
        // probably doesn't need to be synchronized, 
        // since every method on ConcurrentLinkedQueue should be atomic
        // but, better safe than sorry
        return _unleasedConnections.size();
    }
    
    /**
     * @return the number of connections that the pool has currently leased out.
     */
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
