/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.util.test;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.io.FileUtils;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.hibernate.dialect.Dialect;

import com.opengamma.OpenGammaRuntimeException;

/**
 * Command-line interface to create or clear databases.
 *
 * @author pietari
 */
public class DBTool extends Task {
  
  // What to do - should be set once
  private String _catalog;
  private String _schema;
  private boolean _create = false;
  private boolean _drop = false;
  private boolean _clear = false;
  private boolean _createTestDb = false;
  private String _testDbType;
  
  // What to do it on - can change
  private DBDialect _dialect;
  private String _dbServerHost;
  private String _user;
  private String _password;
  
  
  public DBTool() {
  }
  
  public DBTool(String dbServerHost,
      String user,
      String password) {
    setDbServerHost(dbServerHost);
    setUser(user);
    setPassword(password);
  }
  
  public void initialise() {
    if (_dbServerHost == null || _user == null || _password == null) {
      throw new OpenGammaRuntimeException("Server/user/password not initialised");
    }
    
    Map<String, DBDialect> url2Dialect = new HashMap<String, DBDialect>(); // add new supported DB types to this Map
    url2Dialect.put("jdbc:postgresql", PostgresDialect.getInstance());
    url2Dialect.put("jdbc:derby", DerbyDialect.getInstance());  
    
    String dbUrlLowercase = _dbServerHost.toLowerCase();
    for (Map.Entry<String, DBDialect> entry : url2Dialect.entrySet()) {
      if (dbUrlLowercase.indexOf(entry.getKey()) != -1) {
        _dialect = entry.getValue();        
        break;
      }
    }
    
    if (_dialect == null) {
      throw new OpenGammaRuntimeException("Database " + _dbServerHost + " not supported. The database URL must contain one of: " + url2Dialect.entrySet());
    }

    _dialect.initialise(_dbServerHost, _user, _password);
  }
  
  public void shutdown() {
    _dialect.shutdown();
  }
  
  
  
  
  public void setDbServerHost(String dbServerHost) {
    _dbServerHost = dbServerHost;
  }

  public void setUser(String user) {
    _user = user;
  }

  public void setPassword(String password) {
    _password = password;
  }

  public String getDbServerHost() {
    return _dbServerHost;
  }

  public String getUser() {
    return _user;
  }

  public String getPassword() {
    return _password;
  }
  
  
  
  
  public String getCatalog() {
    return _catalog;
  }

  public void setCatalog(String catalog) {
    _catalog = catalog;
  }

  public String getSchema() {
    return _schema;
  }

  public void setSchema(String schema) {
    _schema = schema;
  }

  public void setCreate(boolean create) {
    _create = create;
  }
  
  public void setCreate(String create) {
    setCreate(create.equalsIgnoreCase("true"));
  }

  public void setDrop(boolean drop) {
    _drop = drop;
  }
  
  public void setDrop(String drop) {
    setDrop(drop.equalsIgnoreCase("true"));
  }

  public void setClear(boolean clear) {
    _clear = clear;
  }
  
  public void setClear(String clear) {
    setClear(clear.equalsIgnoreCase("true"));
  }

  public void setCreateTestDb(String testDbType) {
    _createTestDb = (testDbType != null);
    _testDbType = testDbType;
  }


  

  public void createTestSchema() {
    createSchema(getTestCatalog(), getTestSchema());
  }
  
  public void dropTestSchema() {
    dropSchema(getTestCatalog(), getTestSchema());
  }
  
  public void clearTestTables() {
    clearTables(getTestCatalog(), getTestSchema());
  }


 
  public void createSchema(String catalog, String schema) {
    _dialect.createSchema(catalog, schema);
  }
  
  public void dropSchema(String catalog, String schema) {
    _dialect.dropSchema(catalog, schema);
  }
  
  public void clearTables(String catalog, String schema) {
    _dialect.clearTables(catalog, schema);    
  }
  
  
  
  public static String getTestCatalogStatic() {
    return "test_" + System.getProperty("user.name");
  }
  
  public String getTestCatalog() {
    return getTestCatalogStatic();    
  }
  
  public String getTestSchema() {
    return null; // use default    
  }
  
  public String getTestDatabaseURL() {
    return _dbServerHost + "/" + getTestCatalog();         
  }
  
  public Dialect getHibernateDialect() {
    return _dialect.getHibernateDialect();
  }
  
  public Class<?> getJDBCDriverClass() {
    return _dialect.getJDBCDriverClass();
  }
  
  
  
  public void createTestTables() {
    createTables(getTestCatalog());
  }
  
  public void createTables(String catalog) {
    File file = new File("db/" + _dialect.getDatabaseName() + "/create-db.sql");
    String sql;
    try {
      sql = FileUtils.readFileToString(file);
    } catch (IOException e) {
      throw new OpenGammaRuntimeException("Cannot read file " + file.getAbsolutePath(), e);      
    }
    executeSql(catalog, sql);
  }
  
  public void executeSql(String catalog, String sql) {
    _dialect.executeSql(catalog, sql);    
  }
  
  
  
  
  @Override
  public void execute() throws BuildException {
    if (!_createTestDb) {
      if (_dbServerHost == null) {
        throw new BuildException("No DB server specified.");
      }
      
      if (_catalog == null) {
        throw new BuildException("No database on the DB server specified.");
      }
    }
    
    if (!_create && !_drop && !_clear && !_createTestDb) {
      throw new BuildException("Nothing to do.");
    }
    
    if (_clear) {
      System.out.println("Clearing tables...");
      initialise();
      clearTables(_catalog, _schema);
    }
    
    if (_drop) {
      System.out.println("Dropping schema...");
      initialise();
      dropSchema(_catalog, _schema);
    }

    if (_create) {
      System.out.println("Creating schema...");
      initialise();
      createSchema(_catalog, _schema);      
    }
    
    if (_createTestDb) {
      for (String dbType : TestProperties.getDatabaseTypes(_testDbType)) {
        System.out.println("Creating " + dbType + " test database...");
        
        String dbUrl = TestProperties.getDbHost(dbType);
        String user = TestProperties.getDbUsername(dbType);
        String password = TestProperties.getDbPassword(dbType);
        
        setDbServerHost(dbUrl);
        setUser(user);
        setPassword(password);
        
        initialise();
        dropTestSchema(); // make sure it's empty if it already existed
        createTestSchema();
        createTestTables();
        shutdown();
      }
    }
  }
  
  

  public static void usage(Options options) {
    HelpFormatter formatter = new HelpFormatter();
    formatter.printHelp("java com.opengamma.util.test.DBTool [args]", options);
  }
  
  public static void main(String[] args) {
    
    Options options = new Options();
    options.addOption("server", "server", true, "DB server URL (no database at the end) - for example, jdbc:postgresql://localhost:1234");
    options.addOption("user", "user", true, "User name to the DB");
    options.addOption("password", "password", true, "Password to the DB");
    options.addOption("database", "database", true, "Name of database on the DB server - for example, OpenGammaTests");
    options.addOption("schema", "schema", true, "Name of schema within database. Optional. If not specified, the default schema for the database is used.");
    options.addOption("create", "create", false, "Creates the given database/schema");
    options.addOption("drop", "drop", false, "Drops all tables and sequences within the given database/schema");
    options.addOption("clear", "clear", false, "Clears all tables within the given database/schema");
    options.addOption("createtestdb", "createtestdb", true, "Drops schema in database test_<user.name> and recreates it. " +
        "{dbtype} should be one of derby, postgres, all. Connection parameters are read from test.properties so you do not need " +
        "to specify --server, --user, or --password.");
    
    CommandLineParser parser = new PosixParser();
    CommandLine line = null;
    try {
      line = parser.parse(options, args);
    } catch (ParseException e) {
      e.printStackTrace();
      usage(options);
      System.exit(-1);
    }
    
    DBTool tool = new DBTool();
    tool.setDbServerHost(line.getOptionValue("server"));
    tool.setUser(line.getOptionValue("user"));
    tool.setPassword(line.getOptionValue("password"));
    tool.setCatalog(line.getOptionValue("database"));
    tool.setSchema(line.getOptionValue("schema"));
    tool.setCreate(line.hasOption("create"));
    tool.setDrop(line.hasOption("drop"));
    tool.setClear(line.hasOption("clear"));
    tool.setCreateTestDb(line.getOptionValue("createtestdb"));
    
    try {
      tool.execute();
    } catch (BuildException e) {
      System.out.println(e.getMessage());
      usage(options);
      System.exit(-1);
    }
    
    System.out.println("All tasks succeeded.");
    System.exit(0);
  }

}
