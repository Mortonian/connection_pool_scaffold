# Mortonian solution to the OPOWER Connection Pool Homework

This is [Mortonian's](https://github.com/Mortonian/) solution to [OPOWER Connection Pool Homework](https://github.com/opower/connection_pool_scaffold)

## Overview

The basic design concept is the decoupling of the Connection Creation code from the mechanics of the Connection Pool itself, as well as the Connection configuration info and the Pool configuration info.  This allows new behavior to emerge from the novel compositions of these parts.

As of now, only one implementation of `src/main/java/com/opower/connectionpool/ConnectionPool.java` is provided (i.e., `src/main/java/com/opower/connectionpool/pool/MortonianConnectionPool`), as well as only one implementation of `src/main/java/com/opower/connectionpool/ConnectionCreator` (`src/main/java/com/opower/connectionpool/connection/MortonianConnectionCreator`)

However, this separating is heavily used by the unit testing code included in this solution, with a `src/test/java/com/opower/connectionpool/NonPoolingConnectionPool` being used to test individual database connections without proper pooling, and with a `src/test/java/com/opower/connectionpool/MockConnectionCreator` being used to test pooling behaviour with mock connections.  

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


## Instructions

Please clone the repository and deliver your solution via an archive format of your choice, including all project files, within 1 calendar week.

Write a connection pool class that implements this interface (it is also located in `src/main/java/com/opower/connectionpool/ConnectionPool.java`):

    public interface ConnectionPool {
        java.sql.Connection getConnection() throws java.sql.SQLException;
        void releaseConnection(java.sql.Connection con) throws java.sql.SQLException;
    }

While we know there are many production-ready implementations of connection pools, this assignment allows for a variety of solutions to a real-world problem.  Your solution will be reviewed by the engineers you would be working with if you joined OPOWER.  We are interested in seeing your real-world design, coding, and testing skills.

## Using this scaffold

This scaffold is provided to help you (and us) build your homework code. 
We've included a `pom.xml`, which is a file used by [maven][maven] to build the project and run other commands.   It also contains
information on downloading dependent jars needed by your project.  This one contains JUnit, EasyMock and Log4J already, but feel free
to change it as you see fit.

    mvn compile      # compiles your code in src/main/java
    mvn test-compile # compile test code in src/test/java
    mvn test         # run tests in src/test/java for files named Test*.java


[maven]:http://maven.apache.org/

