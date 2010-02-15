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
import org.hibernate.dialect.Dialect;

import com.opengamma.OpenGammaRuntimeException;

/**
 * Command-line interface to create or clear databases.
 *
 * @author pietari
 */
public class DBTool {
  
  private DBDialect _dialect;
  private final String _dbServerHost;
  private final String _user;
  private final String _password;
  
  public DBTool(String dbServerHost,
      String user,
      String password) {
    
    _dbServerHost = dbServerHost;
    _user = user;
    _password = password;
    
    Map<String, DBDialect> url2Dialect = new HashMap<String, DBDialect>();
    url2Dialect.put("jdbc:postgresql", PostgresDialect.getInstance());  // add new supported DB types to this Map
    url2Dialect.put("jdbc:derby", DerbyDialect.getInstance());  // add new supported DB types to this Map
    
    String dbUrlLowercase = dbServerHost.toLowerCase();
    for (Map.Entry<String, DBDialect> entry : url2Dialect.entrySet()) {
      if (dbUrlLowercase.indexOf(entry.getKey()) != -1) {
        _dialect = entry.getValue();        
        break;
      }
    }
    
    if (_dialect == null) {
      throw new OpenGammaRuntimeException("Database " + dbServerHost + " not supported. The database URL must contain one of: " + url2Dialect.entrySet());
    }
  }
  
  
  
  public void initialise() {
    _dialect.initialise(_dbServerHost, _user, _password);
  }
  
  
  public String getUser() {
    return _user;
  }

  public String getPassword() {
    return _password;
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
    
    String dbUrl = line.getOptionValue("server");
    String user = line.getOptionValue("user");
    String password = line.getOptionValue("password");
    String catalog = line.getOptionValue("database");
    String schema = line.getOptionValue("schema");
    boolean create = line.hasOption("create");
    boolean drop = line.hasOption("drop");
    boolean clear = line.hasOption("clear");
    boolean createTestDb = line.hasOption("createtestdb");
    String testDbType = line.getOptionValue("createtestdb");
    
    if (!createTestDb) {
      if (dbUrl == null) {
        System.out.println("No DB server specified.");
        usage(options);
        System.exit(-1);
      }
      
      if (catalog == null) {
        System.out.println("No database on the DB server specified.");
        usage(options);
        System.exit(-1);
      }
    }
    
    if (!create && !drop && !clear && !createTestDb) {
      System.out.println("Nothing to do.");
      usage(options);
      System.exit(-1);
    }
    
    if (clear) {
      System.out.println("Clearing tables...");
      DBTool dbtool = new DBTool(dbUrl, user, password);
      dbtool.initialise();
      dbtool.clearTables(catalog, schema);
    }
    
    if (drop) {
      System.out.println("Dropping schema...");
      DBTool dbtool = new DBTool(dbUrl, user, password);
      dbtool.initialise();
      dbtool.dropSchema(catalog, schema);
    }

    if (create) {
      System.out.println("Creating schema...");
      DBTool dbtool = new DBTool(dbUrl, user, password);
      dbtool.initialise();
      dbtool.createSchema(catalog, schema);      
    }
    
    if (createTestDb) {
      for (String dbType : TestProperties.getDatabaseTypes(testDbType)) {
        System.out.println("Creating " + dbType + " test database...");
        
        dbUrl = TestProperties.getDbHost(dbType);
        user = TestProperties.getDbUsername(dbType);
        password = TestProperties.getDbPassword(dbType);
        
        DBTool dbtool = new DBTool(dbUrl, user, password);
        dbtool.initialise();
        dbtool.dropTestSchema(); // make sure it's empty if it already existed
        dbtool.createTestSchema();
        dbtool.createTestTables();
      }
    }
    
    System.out.println("All tasks succeeded.");
    System.exit(0);
  }

}
