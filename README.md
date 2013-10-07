## Mortonian solution to the OPOWER Connection Pool Homework

This is [Mortonian's](https://github.com/Mortonian/) solution to [OPOWER Connection Pool Homework](https://github.com/opower/connection_pool_scaffold)

## Overview

The basic design concept of this solution is to decouple the connection creating code from the mechanics of the ConnectionPool itself, as well as to separate the jdbc connection configuration info and the pool configuration info.  This allows us to try out new behaviours via the novel compositions of existing parts.

As of now, only one implementation of `src/main/java/com/opower/connectionpool/ConnectionPool.java` is provided: [com.opower.connectionpool.pool.MortonianConnectionPool](https://github.com/Mortonian/connection_pool_scaffold/blob/master/src/main/java/com/opower/connectionpool/pool/MortonianConnectionPool.java).  There is also only one implementation of [com.opower.connectionpool.ConnectionCreator](https://github.com/Mortonian/connection_pool_scaffold/blob/master/src/main/java/com/opower/connectionpool/ConnectionCreator.java): [MortonianConnectionCreator](https://github.com/Mortonian/connection_pool_scaffold/blob/master/src/main/java/com/opower/connectionpool/connection/MortonianConnectionCreator.java).

However, this separation is heavily used by the unit testing code included in this solution, with a [NonPoolingConnectionPool](https://github.com/Mortonian/connection_pool_scaffold/blob/master/src/test/java/com/opower/connectionpool/NonPoolingConnectionPool.java) being used to test individual database connections without proper pooling, and a [MockConnectionCreator](https://github.com/Mortonian/connection_pool_scaffold/blob/master/src/test/java/com/opower/connectionpool/MockConnectionCreator.java) being used to test pooling behaviour with mock connections.

The MortonianConnectionPool supports configurable pool sizes (initial, max, and acquire increment), configurable retry behavior (may retries and wait time in millisecons), auto-commit on release, and other, "experiemental", features.  And all of these features are are verified with unit tests that you can find in [the test package](https://github.com/Mortonian/connection_pool_scaffold/tree/master/src/test/java/com/opower/connectionpool).   

## Basic Usage

You can create a basic connection pool like so:

    SimpleConnectionConfig connectionConfig = new SimpleConnectionConfig();
    connectionConfig.setDriverClass("org.postgresql.Driver");
    connectionConfig.setJdbcUrl("jdbc:postgresql://hostname/database");
    connectionConfig.setUser("user");
    connectionConfig.setPassword("password");
    
    SimplePoolConfig poolConfig = new SimplePoolConfig();
    poolConfig.setInitialPoolSize(10);
    poolConfig.setMaxPoolSize(20);
    
    ConnectionCreator connectionCreator = new MortonianConnectionCreator();
    
    MortonianConnectionPool connectionPool = new MortonianConnectionPool(connectionConfig, connectionCreator, poolConfig);
    
    Connection connection = connectionPool.getConnection();
    
    Statement createStatement = connection.createStatement();
    
    //your code here
    
    connectionPool.releaseConnection(connection);
    connectionPool.shutdown(); 
    
You can also use config files with JSON encoded data:

    JsonFileConnectionConfig connectionConfig = new JsonFileConnectionConfig();
    connectionConfig.setFile("/tmp/connectionConfig.json");
    
    JsonFilePoolConfig poolConfig = new JsonFilePoolConfig();
    connectionConfig.setFile("/tmp/poolConfig.json");
    
    ConnectionCreator connectionCreator = new MortonianConnectionCreator();
    
    MortonianConnectionPool connectionPool = new MortonianConnectionPool(connectionConfig, connectionCreator, poolConfig);
    
    Connection connection = connectionPool.getConnection();
    
    Statement createStatement = connection.createStatement();
    
    //your code here
    
    connectionPool.releaseConnection(connection);
    connectionPool.shutdown(); 

The Json file format looks like you would expect.  Here is an example:
    
    {
        "driverClass": "org.postgresql.Driver",
        "jdbcUrl": "jdbc:postgresql://hostname/database",
        "user": "user",
        "password": "password" 
    }

## Connection Pool Configurations

These are the properties that you can specify with a [com.opower.connectionpool.PoolConfig](https://github.com/Mortonian/connection_pool_scaffold/blob/master/src/main/java/com/opower/connectionpool/PoolConfig.java).

Property| What it does
---|---|---
maxPoolSize|The maximum size of connections that a pool can make.  Default value is 1.
acquireIncrement|When the number of unleased connections reaches zero, allocate this many more before another call to ConnectionPool#getConnection() is made.  Default value is 0.  (**Note:** may create less than this number so as to avoid violating maxPoolSize.)
initialPoolSize|When the pool is initially constructed, create this many connections before users can begin to call ConnectionPool#getConnection().  Default value is 0.
autoCommit|Should Connection.commit() be called on all leased connections before release or shutdown.  Default value is false.
retryWaitTimeInMillis|If all connections are leased, how long to wait before trying again.  Default value is 300ms. <BR/><BR/> **Note:** The wait is accomplished by Thread.sleep, which is interruptible and blocks the thread.  This is clearly the wrong way to do this, but it is what it is for now.  Consider yourself warned.
retryAttempts|If all connections are leased, how many more times to try before returning null.  Default value is 0.
maxConnectionAgeInMillis|**Experimental** <BR/><BR/> Maximum age in milliseconds that a connection will be alive before it is no longer deemed usable, and hence will be closed. <BR/><BR/> **Note:** actively leased connections will not be closed.  Only available, but unleased, connections in the pool will be closed. <BR/> **Also Note:** We've not yet implemented a timer thread, so these will be closed out when they are inspected during a call to ConnectionPool.#getConnection().  This is probably invalid, but for now it is what it is. <BR/><BR/> Default value is -1. 
maxIdleTimeInMillis|**Experimental** <BR/><BR/> Maximum time in milliseconds since a connection was released before it is no longer deemed usable, and hence will be closed. <BR/><BR/> **Note:** actively leased connections will not be closed.  Only available, but unleased, connections in the pool will be closed. <BR/> **Also Note:** We've not yet implemented a timer thread, so these will be closed out when they are inspected during a call to ConnectionPool.#getConnection().  This is probably invalid, but for now it is what it is. <BR/><BR/> Default value is -1.

Full javadocs for these pool configurations are available [here](https://github.com/Mortonian/connection_pool_scaffold/blob/master/src/main/java/com/opower/connectionpool/PoolConfig.java).

## Compiling, Testing, Oh My!

You compile, package, and or testing this code using the familiar maven commands:

    mvn compile      # compiles your code in src/main/java
    mvn test-compile # compile test code in src/test/java
    mvn test         # run tests in src/test/java for files named Test*.java


