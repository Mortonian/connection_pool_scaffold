## Mortonian solution to the OPOWER Connection Pool Homework

This is [Mortonian's](https://github.com/Mortonian/) solution to [OPOWER Connection Pool Homework](https://github.com/opower/connection_pool_scaffold)

## Overview

The basic design concept of this solution is to decouple the connection creating code from the mechanics of the ConnectionPool itself, as well as to separate the jdbc connection configuration info and the pool configuration info.  This allows us to try out new behaviours via the novel compositions of existing parts.

As of now, only one implementation of `src/main/java/com/opower/connectionpool/ConnectionPool.java` is provided: the `src/main/java/com/opower/connectionpool/pool/MortonianConnectionPool`.  There is also only one implementation of `src/main/java/com/opower/connectionpool/ConnectionCreator`: `src/main/java/com/opower/connectionpool/connection/MortonianConnectionCreator`.

However, this separation is heavily used by the unit testing code included in this solution, with a `src/test/java/com/opower/connectionpool/NonPoolingConnectionPool` being used to test individual database connections without proper pooling, and a `src/test/java/com/opower/connectionpool/MockConnectionCreator` being used to test pooling behaviour with mock connections.  

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

## Connection Configurations

Comming soon....

## Compiling, Testing, Oh My!

You compile, package, and or testing this code using the familiar maven commands:

    mvn compile      # compiles your code in src/main/java
    mvn test-compile # compile test code in src/test/java
    mvn test         # run tests in src/test/java for files named Test*.java


