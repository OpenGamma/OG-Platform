/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;

import javax.sql.DataSource;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.io.FileUtils;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;
import org.hibernate.dialect.Dialect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jolbox.bonecp.BoneCPDataSource;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.db.management.DbManagement;
import com.opengamma.util.db.management.DbManagementUtils;

/**
 * Command-line interface to create or clear databases.
 */
public class DbTool extends Task {

  /**
   * During installation, INFO level messages will be reported to the user as progress.
   */
  private static final Logger s_logger = LoggerFactory.getLogger(DbTool.class);

  /**
   */
  public interface TableCreationCallback {
    void tablesCreatedOrUpgraded(final String version, final String prefix);
  }

  // What to do - should be set once
  private String _catalog;
  private String _schema;
  private boolean _create;
  private boolean _drop;
  private boolean _clear;
  private boolean _createTestDb;
  private boolean _createTables;
  private String _testDbType;
  private String _testPropertiesDir;
  private Collection<String> _dbScriptDirs = new ArrayList<String>();
  private Integer _targetVersion;
  private Integer _createVersion;

  // What to do it on - can change
  private DbManagement _dialect;
  private String _jdbcUrl;
  private String _dbServerHost;
  private String _user;
  private String _password;
  private BoneCPDataSource _dataSource;

  /**
   * Static as the parameterized JUnit test runner seems to create a new DbTool instance
   * for each DBTest test case. This is clearly a hack.
   * All strings will be lower case
   */
  private static final Collection<String> s_tablesThatShouldNotBeCleared = new HashSet<String>();

  public DbTool() {
  }

  public DbTool(String dbServerHost, String user, String password) {
    setDbServerHost(dbServerHost);
    setUser(user);
    setPassword(password);
  }

  public void initialize() {
    if (_dbServerHost == null) {
      // Parse the server host and catalog from a JDBC URL
      if (_jdbcUrl != null) {

        int lastSlash = _jdbcUrl.lastIndexOf('/');
        if (lastSlash == -1 || lastSlash == _jdbcUrl.length() - 1) {
          throw new OpenGammaRuntimeException("JDBC URL must contain a slash separating the server host and the database name");
        }

        _dbServerHost = _jdbcUrl.substring(0, lastSlash);
        _catalog = _jdbcUrl.substring(lastSlash + 1);

      } else {
        throw new OpenGammaRuntimeException("No DB server specified.");
      }
    }

    if (_dbServerHost == null || _user == null || _password == null) {
      throw new OpenGammaRuntimeException("Server/user/password not initialised");
    }
    _dialect = DbManagementUtils.getDbManagement(_dbServerHost);
    _dialect.initialise(_dbServerHost, _user, _password);
  }

  //-------------------------------------------------------------------------

  /**
   * The data-source is created once per instance of the tool.
   *
   * @return the data source, not null
   */
  public synchronized DataSource getDataSource() {
    BoneCPDataSource dataSource = _dataSource;
    if (dataSource == null) {
      dataSource = new BoneCPDataSource();
      dataSource.setPoolName("DbTool");
      dataSource.setJdbcUrl(getJdbcUrl());
      dataSource.setUsername(getUser());
      dataSource.setPassword(getPassword());
      dataSource.setAcquireIncrement(1);
      dataSource.setPartitionCount(1);
      dataSource.setMaxConnectionsPerPartition(1);
      _dataSource = dataSource;
    }
    return dataSource;
  }

  /**
   * Close the data-source if it was created.
   */
  public synchronized void close() {
    if (_dataSource != null) {
      _dataSource.close();
      _dataSource = null;
    }
  }

  //-------------------------------------------------------------------------
  public void resetTestCatalog() {
    _dialect.reset(getTestCatalog());
  }

  public void shutdown(String catalog) {
    _dialect.shutdown(catalog);
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

  public String getJdbcUrl() {
    return _jdbcUrl;
  }

  public void setJdbcUrl(String jdbcUrl) {
    _jdbcUrl = jdbcUrl;
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

  public String getTestPropertiesDir() {
    return _testPropertiesDir;
  }

  public void setTestPropertiesDir(String testPropertiesDir) {
    _testPropertiesDir = testPropertiesDir;
  }

  public Collection<String> getDbScriptDirs() {
    return _dbScriptDirs;
  }

  public void setDbScriptDirs(Collection<String> dirs) {
    _dbScriptDirs = dirs;
  }

  public static String getWorkingDirectory() {
    return System.getProperty("user.dir");
  }

  /**
   * Primarily for ant
   *
   * @param directories  comma separated list of directory
   */
  public void setDbScriptDir(String directories) {
    if (directories != null) {
      for (String directory : directories.split(",")) {
        _dbScriptDirs.add(directory);
      }
    } else {
      _dbScriptDirs.add(getWorkingDirectory());
    }
  }

  /**
   * @param directory If null -> working directory
   */
  public void addDbScriptDirectory(String directory) {
    if (directory == null) {
      directory = getWorkingDirectory();
    }
    _dbScriptDirs.add(directory);
  }

  /**
   * Adds a collection of directories to the set.
   * 
   * @param directories  the directories, not null
   */
  public void addDbScriptDirectories(Iterable<File> directories) {
    ArgumentChecker.notNull(directories, "directories");
    for (File dir : directories) {
      _dbScriptDirs.add(dir.getAbsolutePath());
    }
  }

  public void setCreateVersion(final String createVersion) {
    try {
      _createVersion = Integer.parseInt(createVersion);
    } catch (NumberFormatException e) {
      _createVersion = null;
    }
  }

  public void setCreateVersion(final Integer createVersion) {
    _createVersion = createVersion;
  }

  public Integer getCreateVersion() {
    return _createVersion;
  }

  public void setTargetVersion(final String targetVersion) {
    try {
      _targetVersion = Integer.parseInt(targetVersion);
    } catch (NumberFormatException e) {
      _targetVersion = null;
    }
  }

  public void setTargetVersion(final Integer targetVersion) {
    _targetVersion = targetVersion;
  }

  public Integer getTargetVersion() {
    return _targetVersion;
  }

  public void setCreateTables(boolean create) {
    _createTables = create;
  }

  public void setCreateTables(String create) {
    setCreateTables(create.equalsIgnoreCase("true"));
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
    _dialect.clearTables(catalog, schema, s_tablesThatShouldNotBeCleared);
  }

  public String describeDatabase() {
    return _dialect.describeDatabase(getTestCatalog());
  }

  public String describeDatabase(String prefix) {
    return _dialect.describeDatabase(getTestCatalog(), prefix);
  }

  public String getTestCatalog() {
    return _dialect.getTestCatalog();
  }

  public String getTestSchema() {
    return _dialect.getTestSchema();
  }

  public String getTestDatabaseUrl() {
    return _dialect.getCatalogToConnectTo(getTestCatalog());
    //return _dbServerHost + "/" + getTestCatalog();         
  }

  public Dialect getHibernateDialect() {
    return _dialect.getHibernateDialect();
  }

  public Class<?> getJDBCDriverClass() {
    return _dialect.getJDBCDriverClass();
  }

  public DbManagement getDbManagement() {
    return _dialect;
  }

  public Configuration getHibernateConfiguration() {
    Configuration configuration = new Configuration();
    configuration.setProperty(Environment.DRIVER, getJDBCDriverClass().getName());
    configuration.setProperty(Environment.URL, getJdbcUrl());
    configuration.setProperty(Environment.USER, getUser());
    configuration.setProperty(Environment.PASS, getPassword());
    configuration.setProperty(Environment.DIALECT, getHibernateDialect().getClass().getName());
    configuration.setProperty(Environment.SHOW_SQL, "false");
    configuration.setProperty(Environment.CURRENT_SESSION_CONTEXT_CLASS, "thread");
    configuration.setProperty(Environment.TRANSACTION_STRATEGY, "org.hibernate.transaction.JDBCTransactionFactory");
    return configuration;
  }

  public Configuration getTestHibernateConfiguration() {
    Configuration configuration = getHibernateConfiguration();
    if (getTestSchema() != null) {
      configuration.setProperty(Environment.DEFAULT_SCHEMA, getTestSchema());
    }
    return configuration;
  }

  public void createTestTables(final TableCreationCallback callback) {
    createTables(getTestCatalog(), getTestSchema(), callback);
  }

  private void executeSQLScript(String catalog, String schema, File file) {
    String sql;
    try {
      sql = FileUtils.readFileToString(file);
    } catch (IOException e) {
      throw new OpenGammaRuntimeException("Cannot read file " + file.getAbsolutePath(), e);
    }
    executeSql(catalog, schema, sql);

    // -- DBTOOLDONOTCLEAR
    // create table rsk_computation_target_type (
    //
    // -> extract rsk_computation_target_type

    final String doNotClear = "DBTOOLDONOTCLEAR";

    int doNotClearIndex = sql.indexOf(doNotClear);
    while (doNotClearIndex != -1) {
      int createTableOpenParenthesis = sql.indexOf('(', doNotClearIndex);
      if (createTableOpenParenthesis == -1) {
        throw new IllegalArgumentException("There is no CREATE TABLE xxx ( after " + doNotClear);
      }
      String[] createTableSqls = sql.substring(
        doNotClearIndex + doNotClear.length(),
        createTableOpenParenthesis).split("\r\n|\r|\n| ");
      List<String> filteredCreateTableSqls = new ArrayList<String>();
      for (String createTableSql : createTableSqls) {
        if (!createTableSql.isEmpty()) {
          filteredCreateTableSqls.add(createTableSql);
        }
      }
      if (filteredCreateTableSqls.size() != 3) {
        throw new IllegalArgumentException("There is no CREATE TABLE xxx ( after " + doNotClear);
      }

      String tableName = filteredCreateTableSqls.get(2);
      s_tablesThatShouldNotBeCleared.add(tableName.toLowerCase());

      doNotClearIndex = sql.indexOf(doNotClear, doNotClearIndex + doNotClear.length());
    }
  }

  /**
   * Gets the scripts.
   *
   * @return schema => version_number => (create_script, migrate_script)
   */
  public Map<String, SortedMap<Integer, DbScriptPair>> getScriptDirs() {
    Collection<File> dirs = new ArrayList<>();
    for (String dir : _dbScriptDirs) {
      dirs.add(new File(dir));
    }
    DbScripts scripts = DbScripts.of(dirs, _dialect.getDatabaseName());
    return scripts.getScriptPairs();
  }

  public Map<String, Integer> getLatestVersions() {
    Collection<File> dirs = new ArrayList<>();
    for (String dir : _dbScriptDirs) {
      dirs.add(new File(dir));
    }
    return DbScripts.getLatestVersions(dirs);
  }

  public void createTables(String catalog, String schema, final TableCreationCallback callback) {
    final Map<String, SortedMap<Integer, DbScriptPair>> dbScripts = getScriptDirs();
    for (String tableSet : dbScripts.keySet()) {
      int targetVersion = getTargetVersion() != null ? getTargetVersion() : Collections.max(dbScripts.get(tableSet).keySet());
      int migrateFromVersion = getCreateVersion() != null ? getCreateVersion() : targetVersion;
      createTables(tableSet, catalog, schema, targetVersion, migrateFromVersion, callback);
    }
  }

  public void createTables(String tableSet, String catalog, String schema, int targetVersion, int migrateFromVersion, final TableCreationCallback callback) {
    final SortedMap<Integer, DbScriptPair> dbScripts = getScriptDirs().get(tableSet);
    // create
    final File createScript = dbScripts.get(migrateFromVersion).getCreateScript();
    if (createScript == null || !createScript.exists()) {
      throw new OpenGammaRuntimeException("The " + migrateFromVersion + " create script is missing (" + createScript + ")");
    }
    s_logger.debug("Creating {} DB version {}", tableSet, migrateFromVersion);
    s_logger.debug("Executing create script {}", dbScripts.get(migrateFromVersion).getCreateScript());
    executeSQLScript(catalog, schema, createScript);
    if (callback != null) {
      callback.tablesCreatedOrUpgraded(Integer.toString(migrateFromVersion), tableSet);
    }
    // migrates
    for (int v = migrateFromVersion + 1; v <= targetVersion; v++) {
      final File migrateScript = dbScripts.get(v).getMigrateScript();
      if (migrateScript == null || !migrateScript.exists()) {
        throw new OpenGammaRuntimeException("The " + v + " migrate script is missing (" + migrateScript + ")");
      }
      s_logger.debug("Migrating DB from version {} to {}", (v - 1), v);
      s_logger.debug("Executing migrate script {}", dbScripts.get(v).getCreateScript());
      executeSQLScript(catalog, schema, migrateScript);
      if (callback != null) {
        callback.tablesCreatedOrUpgraded(Integer.toString(v), tableSet);
      }
    }
  }

  public void executeSql(String catalog, String schema, String sql) {
    _dialect.executeSql(catalog, schema, sql);
  }

  @Override
  public void execute() throws BuildException {
    // NOTE: The catalog field generally has to be set, but if you do not
    // set the jdbcHost (normally the case) then the catalog is overridden
    // with one derived from the URL in initialize() called from here.
    // All very confusing...
    
    if (!_createTestDb) {
      if (_catalog == null) {
        throw new BuildException("No database on the DB server specified.");
      }
    }

    if (!_create && !_drop && !_clear && !_createTestDb && !_createTables) {
      throw new BuildException("Nothing to do.");
    }

    if (_clear) {
      s_logger.info("Clearing database tables at {}", getJdbcUrl());
      initialize();
      clearTables(_catalog, _schema);
    }

    if (_drop) {
      s_logger.info("Dropping existing database schema at {}", getJdbcUrl());
      initialize();
      dropSchema(_catalog, _schema);
    }

    if (_create) {
      s_logger.info("Creating new database schema at {}", getJdbcUrl());
      initialize();
      createSchema(_catalog, _schema);
    }

    if (_createTables) {
      s_logger.info("Creating database tables at {}", getJdbcUrl());
      initialize();
      createTables(_catalog, null, null);
      shutdown(_catalog);
    }

    if (_createTestDb) {
      // used to try to use _testPropertiesDir here, but value was always ignored
      for (String dbType : DbTestProperties.getDatabaseTypes(_testDbType)) {
        s_logger.debug("Creating " + dbType + " test database...");

        String dbUrl = DbTestProperties.getDbHost(dbType);
        String user = DbTestProperties.getDbUsername(dbType);
        String password = DbTestProperties.getDbPassword(dbType);

        setDbServerHost(dbUrl);
        setUser(user);
        setPassword(password);

        initialize();
        dropTestSchema(); // make sure it's empty if it already existed
        createTestSchema();
        createTestTables(null);
        shutdown(getTestCatalog());
      }
    }
    s_logger.info("OpenGamma database created at {}", getJdbcUrl());
  }

  public static void usage(Options options) {
    HelpFormatter formatter = new HelpFormatter();
    formatter.printHelp("java com.opengamma.util.test.DbTool [args]", options);
  }

  public static void main(String[] args) { // CSIGNORE
    Options options = new Options();
    options.addOption("jdbcUrl", "jdbcUrl", true, "DB server URL + database - for example, jdbc:postgresql://localhost:1234/OpenGammaTests. You can use" +
      " either this option or specify server and database separately.");
    options.addOption("server", "server", true, "DB server URL (no database at the end) - for example, jdbc:postgresql://localhost:1234");
    options.addOption("database", "database", true, "Name of database on the DB server - for example, OpenGammaTests");
    options.addOption("user", "user", true, "User name to the DB");
    options.addOption("password", "password", true, "Password to the DB");
    options.addOption("schema", "schema", true, "Name of schema within database. Optional. If not specified, the default schema for the database is used.");
    options.addOption("create", "create", false, "Creates the given database/schema. The database will be empty.");
    options.addOption("drop", "drop", false, "Drops all tables and sequences within the given database/schema");
    options.addOption("clear", "clear", false, "Clears all tables within the given database/schema");
    options.addOption("createtestdb", "createtestdb", true, "Drops schema in database test_<user.name> and recreates it (including tables). " +
      "{dbtype} should be one of derby, postgres, all. Connection parameters are read from test.properties so you do not need " +
      "to specify server, user, or password.");
    options.addOption("createtables", "createtables", true, "Runs {dbscriptbasedir}/db/create/{dbtype}/<for-all-masters>/<latest version>__create-<master>.sql.");
    options.addOption("dbscriptbasedir", "dbscriptbasedir", true, "Directory for reading db create scripts. " +
      "Optional. If not specified, the working directory is used.");
    options.addOption("targetversion", "targetversion", true,
        "Version number for the end result database. 0 means latest. 1 means last but one etc. Optional. If not specified, assumes latest version.");
    options.addOption("createversion", "createversion", true,
        "Version number to run the creation script from. 0 means latest. 1 means last but one etc. Optional. If not specified, defaults to {targetversion}.");
    options.addOption("testpropertiesdir", "testpropertiesdir", true, "Directory for reading test.properties. Only used with the --createstdb option. " +
      "Optional. If not specified, the working directory is used.");

    CommandLineParser parser = new PosixParser();
    CommandLine line = null;
    try {
      line = parser.parse(options, args);
    } catch (ParseException e) {
      e.printStackTrace();
      usage(options);
      System.exit(-1);
    }

    DbTool tool = new DbTool();
    tool.setJdbcUrl(line.getOptionValue("jdbcUrl"));
    tool.setDbServerHost(line.getOptionValue("server"));
    tool.setUser(line.getOptionValue("user"));
    tool.setPassword(line.getOptionValue("password"));
    tool.setCatalog(line.getOptionValue("database"));
    tool.setSchema(line.getOptionValue("schema"));
    tool.setCreate(line.hasOption("create"));
    tool.setDrop(line.hasOption("drop"));
    tool.setClear(line.hasOption("clear"));
    tool.setCreateTestDb(line.getOptionValue("createtestdb"));
    tool.setCreateTables(line.getOptionValue("createtables"));
    tool.setTestPropertiesDir(line.getOptionValue("testpropertiesdir"));
    tool.addDbScriptDirectory(line.getOptionValue("dbscriptbasedir"));
    tool.setTargetVersion(line.getOptionValue("targetversion"));
    tool.setCreateVersion(line.getOptionValue("createversion"));

    try {
      tool.execute();
    } catch (BuildException e) {
      s_logger.error(e.getMessage());
      usage(options);
      System.exit(-1);
    }
  }

}
