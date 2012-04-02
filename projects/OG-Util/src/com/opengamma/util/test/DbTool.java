/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.test;

import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Sets.difference;
import static com.google.common.collect.Sets.newTreeSet;
import static com.opengamma.util.RegexUtils.extract;
import static com.opengamma.util.RegexUtils.matches;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

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
import com.opengamma.util.tuple.Pair;

/**
 * Command-line interface to create or clear databases.
 */
public class DbTool extends Task {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(DbTool.class);

  private static final String DATABASE_INSTALL_FOLDER = "install/db";
  private static final Pattern DATABASE_SCRIPT_FOLDER_PATTERN = Pattern.compile("(.+)_db");

  private static final String DATABASE_CREATE_FOLDER = DATABASE_INSTALL_FOLDER + File.separatorChar + "create";
  private static final String DATABASE_MIGRATE_FOLDER = DATABASE_INSTALL_FOLDER + File.separatorChar + "migrate";
  private static final String DATABASE_CRAETE_SCRIPT_PREFIX = "V_";
  private static final String DATABASE_MIGRATE_SCRIPT_PREFIX = "V_";

  private static final Pattern CREATE_SCRIPT_PATTERN = Pattern.compile("^" + DATABASE_CRAETE_SCRIPT_PREFIX + "([0-9]+)__create_(.+?)\\.sql");
  private static final Pattern MIGRATE_SCRIPT_PATTERN = Pattern.compile("^" + DATABASE_MIGRATE_SCRIPT_PREFIX + "([0-9]+)__(.+?)\\.sql");

  private static final Map<String, DbManagement> s_url2Management = new ConcurrentHashMap<String, DbManagement>();

  /**
   * Adds a db management to the map of known.
   * 
   * @param jdbcUrlPrefix  the JDBC prefix, not null
   * @param management  the management, not null
   */
  public static void addDbManagement(String jdbcUrlPrefix, DbManagement management) {
    s_url2Management.put(jdbcUrlPrefix, management);
  }

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
    
    addDbManagement("jdbc:postgresql", PostgresDbManagement.getInstance());
    addDbManagement("jdbc:derby", DerbyDbManagement.getInstance());
    addDbManagement("jdbc:hsqldb", HSQLDbManagement.getInstance());
    
    String dbUrlLowercase = _dbServerHost.toLowerCase();
    for (Map.Entry<String, DbManagement> entry : s_url2Management.entrySet()) {
      if (dbUrlLowercase.indexOf(entry.getKey()) != -1) {
        _dialect = entry.getValue();        
        break;
      }
    }
    
    if (_dialect == null) {
      throw new OpenGammaRuntimeException("Database " + _dbServerHost + " not supported. The database URL must contain one of: " + s_url2Management.entrySet());
    }

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

  public void setCreateVersion(final String createVersion) {
    _createVersion = createVersion != null ? Integer.parseInt(createVersion) : null;
  }

  public void setCreateVersion(final Integer createVersion) {
    _createVersion = createVersion;
  }

  public Integer getCreateVersion() {
    return _createVersion;
  }

  public void setTargetVersion(final String targetVersion) {
    _targetVersion = targetVersion != null ? Integer.parseInt(targetVersion) : null;
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
    return _dbServerHost + "/" + getTestCatalog();         
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

  private void executeCreateScript(String catalog, String schema, File file) {
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

  private int getScriptVersion(File script, Pattern scriptVersionPattern) {
    return Integer.parseInt(extract(script.getName(), scriptVersionPattern, 1));
  }

  /**
   * Creates map from versions to sql scripts
   * @param dbCreateSuperDir the directory holding versioned scripts
   * @return the map
   */
  private Map<File, Map<Integer, File>> getScripts(File dbCreateSuperDir, final Pattern scriptPattern) {
    ArgumentChecker.isTrue(dbCreateSuperDir.exists(), "Directory " + dbCreateSuperDir.getAbsolutePath() + " does not exist");
    ArgumentChecker.isTrue(dbCreateSuperDir.isDirectory(), "dbCreateSuperDir must be directory not a regular file");
    final File[] dbCreateScriptDirs = dbCreateSuperDir.listFiles(new FileFilter() {
      @Override
      public boolean accept(File pathname) {
        return matches(pathname.getName(), DATABASE_SCRIPT_FOLDER_PATTERN);
      }
    });
    //
    Map<File, Map<Integer, File>> dbFolder2versionedScripts = newHashMap();
    //
    for (File dbCreateScriptDir : dbCreateScriptDirs) {
      final File[] scripts = dbCreateScriptDir.listFiles(new FileFilter() {
        @Override
        public boolean accept(File pathname) {
          return pathname.isFile()
            && matches(pathname.getName(), scriptPattern);
        }
      });
      //

      Map<Integer, File> versionedScripts = newHashMap();
      for (File script : scripts) {
        Integer version = getScriptVersion(script, scriptPattern);
        versionedScripts.put(version, script);
      }

      dbFolder2versionedScripts.put(dbCreateScriptDir, versionedScripts);

    }
    return dbFolder2versionedScripts;
  }


  /**
   *
   * @return db_name => version_number => (create_script, migrate_script)
   */
  private Map<String, Map<Integer, Pair<File, File>>> getScriptDirs() {

    Map<String, ConcurrentHashMap<Integer, File>> createScripts = new ConcurrentHashMap<String, ConcurrentHashMap<Integer, File>>() {
      private static final long serialVersionUID = 1L;
      @Override
      public ConcurrentHashMap<Integer, File> get(Object key) {
        super.putIfAbsent((String) key, new ConcurrentHashMap<Integer, File>());
        return super.get(key);
      }
    };

    Map<String, ConcurrentHashMap<Integer, File>> migrateScripts = new ConcurrentHashMap<String, ConcurrentHashMap<Integer, File>>() {
      private static final long serialVersionUID = 1L;
      @Override
      public ConcurrentHashMap<Integer, File> get(Object key) {
        super.putIfAbsent((String) key, new ConcurrentHashMap<Integer, File>());
        return super.get(key);
      }
    };

    for (String scriptDir : _dbScriptDirs) {

      Map<File, Map<Integer, File>> scripts1 = getScripts(new File(scriptDir, DATABASE_CREATE_FOLDER + File.separatorChar + _dialect.getDatabaseName()), CREATE_SCRIPT_PATTERN);
      for (Map.Entry<File, Map<Integer, File>> dbFolder2versionedScripts : scripts1.entrySet()) {
        File dbFolder = dbFolder2versionedScripts.getKey();
        createScripts.get(dbFolder.getName()); // creates empty slot for dbFolder.getName() 
        Map<Integer, File> versionedScripts = dbFolder2versionedScripts.getValue();
        for (Map.Entry<Integer, File> version2script : versionedScripts.entrySet()) {
          Integer version = version2script.getKey();
          File script = version2script.getValue();

          ConcurrentHashMap<Integer, File> createDbScripts = createScripts.get(dbFolder.getName());

          File prev = createDbScripts.putIfAbsent(version, script);
          if (prev != null) {
            throw new OpenGammaRuntimeException("Can't add " + script.getAbsolutePath() + " script. Version " + version + " already added by " + prev.getAbsolutePath() + " script.");
          }
        }
      }

      Map<File, Map<Integer, File>> scripts2 = getScripts(new File(scriptDir, DATABASE_MIGRATE_FOLDER + File.separatorChar + _dialect.getDatabaseName()), MIGRATE_SCRIPT_PATTERN);
      for (Map.Entry<File, Map<Integer, File>> dbFolder2versionedScripts : scripts2.entrySet()) {
        File dbFolder = dbFolder2versionedScripts.getKey();
        migrateScripts.get(dbFolder.getName()); // creates empty slot for dbFolder.getName()
        Map<Integer, File> versionedScripts = dbFolder2versionedScripts.getValue();
        for (Map.Entry<Integer, File> version2script : versionedScripts.entrySet()) {
          Integer version = version2script.getKey();
          File script = version2script.getValue();

          ConcurrentHashMap<Integer, File> migrateDbScripts = migrateScripts.get(dbFolder.getName());

          File prev = migrateDbScripts.putIfAbsent(version, script);
          if (prev != null) {
            throw new OpenGammaRuntimeException("Can't add " + script.getAbsolutePath() + " script. Version " + version + " already added by " + prev.getAbsolutePath() + " script.");
          }
        }
      }

    }

    Set<String> migrateDbDirs = migrateScripts.keySet();

    Set<String> createDbDirs = createScripts.keySet();      

    Set<String> unmatchedCreateDbDirs = difference(migrateDbDirs, createDbDirs);
    if (unmatchedCreateDbDirs.size() > 0) {
      StringBuilder errorMessage = new StringBuilder();
      for (String unmatchedCreateDbDir : unmatchedCreateDbDirs) {
        errorMessage.append("There is no corresponding create db directory for migrate one: " + unmatchedCreateDbDir + "\n");
      }
      throw new OpenGammaRuntimeException(errorMessage.toString());
    }

    Set<String> unmatchedMigrateDbDirs = difference(createDbDirs, migrateDbDirs);
    if (unmatchedMigrateDbDirs.size() > 0) {
      StringBuilder errorMessage = new StringBuilder();
      for (String unmatchedMigrateDbDir : unmatchedMigrateDbDirs) {
        errorMessage.append("There is no corresponding migrate db directory for create one: " + unmatchedMigrateDbDir + "\n");
      }
      throw new OpenGammaRuntimeException(errorMessage.toString());
    }

    final Map<String, Map<Integer, Pair<File, File>>> scripts = new ConcurrentHashMap<String, Map<Integer, Pair<File, File>>>() {
      private static final long serialVersionUID = 1L;
      @Override
      public Map<Integer, Pair<File, File>> get(Object key) {
        super.putIfAbsent((String) key, new ConcurrentHashMap<Integer, Pair<File, File>>());
        return super.get(key);
      }
    };

    for (String dir : migrateDbDirs) {
      
      ConcurrentHashMap<Integer, File> versionedCreateScripts = createScripts.get(dir);
      ConcurrentHashMap<Integer, File> versionedMigrateScripts = migrateScripts.get(dir);

      Set<Integer> migrateVersions = versionedCreateScripts.keySet();
//      Set<Integer> createVersions = versionedMigrateScripts.keySet();
//
//      Set<Integer> unmatchedCreateVersions = difference(migrateVersions, createVersions);
//      if (unmatchedCreateVersions.size() > 0) {
//        StringBuilder errorMessage = new StringBuilder();
//        for (Integer unmatchedCreateVersion : unmatchedCreateVersions) {
//          errorMessage.append("There is no corresponding version of create script for the migrate one: " + DATABASE_CRAETE_SCRIPT_PREFIX + unmatchedCreateVersion + "\n");
//        }
//        throw new OpenGammaRuntimeException(errorMessage.toString());
//      }
//
//      Set<Integer> unmatchedMigrateVersions = difference(createVersions, migrateVersions);
//      if (unmatchedMigrateVersions.size() > 0) {
//        StringBuilder errorMessage = new StringBuilder();
//        for (Integer unmatchedMigrateVersion : unmatchedMigrateVersions) {
//          errorMessage.append("There is no corresponding version of migrate script for the create one: " + DATABASE_MIGRATE_SCRIPT_PREFIX + unmatchedMigrateVersion + "\n");
//        }
//        throw new OpenGammaRuntimeException(errorMessage.toString());
//      }

      for (Integer version : migrateVersions) {
        File createScript = versionedCreateScripts.get(version);
        File migrateScript = versionedMigrateScripts.get(version);
        scripts.get(dir).put(version, Pair.of(createScript, migrateScript));        
      }
    }

    if (scripts.isEmpty()) {
      throw new OpenGammaRuntimeException("No script directories found: " + _dbScriptDirs);
    }
    return scripts;
  }

  public void createTables(String catalog, String schema, final TableCreationCallback callback) {
    final Map<String, Map<Integer, Pair<File, File>>> scriptDirs = getScriptDirs();
    for (String dbDir : scriptDirs.keySet()) {
      String db = extract(dbDir, DATABASE_SCRIPT_FOLDER_PATTERN, 1);
      createTables(db, scriptDirs.get(dbDir), catalog, schema, callback);
    }
  }

  public void createTables(String database, Map<Integer, Pair<File, File>> dbScripts, String catalog, String schema, final TableCreationCallback callback) {
    int highestVersion = Collections.max(dbScripts.keySet());
    int targetVersion = getTargetVersion() == null ? highestVersion : getTargetVersion();
    int createVersion = getCreateVersion() == null ? targetVersion : getCreateVersion();    
    createTables(database, dbScripts, highestVersion, targetVersion, createVersion, catalog, schema, callback);
  }

  public void createTables(String database, Map<Integer, Pair<File, File>> dbScripts, int version, int targetVersion, int createVersion, String catalog, String schema,
      final TableCreationCallback callback) {
    if (version < 1) {
      throw new IllegalArgumentException("Invalid creation or target version (" + createVersion + "/" + targetVersion + ")");
    }

    if (targetVersion >= version) {
      final File createScript = dbScripts.get(version).getFirst();
      if (createVersion >= version && createScript.exists()) {
        s_logger.info("Creating DB version " + version);
        s_logger.info("Executing create script " + createScript);
        executeCreateScript(catalog, schema, createScript);
        if (callback != null) {
          callback.tablesCreatedOrUpgraded(Integer.toString(version), database);
        }
      } else {
        createTables(database, dbScripts, version - 1, targetVersion, createVersion, catalog, schema, callback);
        final File migrateScript = dbScripts.get(version).getSecond();
        if (migrateScript.exists()) {
          s_logger.info("Upgrading to DB version " + version);
          s_logger.info("Executing upgrade script " + migrateScript);
          executeCreateScript(catalog, schema, migrateScript);
          if (callback != null) {
            callback.tablesCreatedOrUpgraded(Integer.toString(version), database);
          }
        }
      }
    } else {
      createTables(database, dbScripts, version - 1, targetVersion, createVersion, catalog, schema, callback);
    }

  }

  /**
   * Returns version numbers of any that have a "create" script and the most recent (in descending order).
   * @return FIXME
   */
  public String[] getDatabaseCreatableVersions() {
    Map<String, Map<Integer, Pair<File, File>>> scriptDirs = getScriptDirs();

    SortedSet<Integer> allVersions = newTreeSet(new Comparator<Integer>() {
      @Override
      public int compare(Integer o1, Integer o2) {
        return o2.compareTo(o1);
      }
    });

    for (Map<Integer, Pair<File, File>> versionedScripts : scriptDirs.values()) {
      allVersions.addAll(versionedScripts.keySet());
    }

    String[] versionsStrings = new String[allVersions.size()];

    int i = 0;
    for (Integer version : allVersions) {
      versionsStrings[i] = version.toString();
      i += 1;
    }

    return versionsStrings;
  }

  public void executeSql(String catalog, String schema, String sql) {
    _dialect.executeSql(catalog, schema, sql);
  }

  @Override
  public void execute() throws BuildException {
    if (!_createTestDb) {
      if (_catalog == null) {
        throw new BuildException("No database on the DB server specified.");
      }
    }

    if (!_create && !_drop && !_clear && !_createTestDb && !_createTables) {
      throw new BuildException("Nothing to do.");
    }

    if (_clear) {
      s_logger.info("Clearing tables...");
      initialize();
      clearTables(_catalog, _schema);
    }

    if (_drop) {
      s_logger.info("Dropping schema...");
      initialize();
      dropSchema(_catalog, _schema);
    }

    if (_create) {
      s_logger.info("Creating schema...");
      initialize();
      createSchema(_catalog, _schema);
    }

    if (_createTables) {
      s_logger.info("Creating tables...");
      initialize();
      createTables(_catalog, null, null);
      shutdown(_catalog);
    }

    if (_createTestDb) {
      TestProperties.setBaseDir(_testPropertiesDir);

      for (String dbType : TestProperties.getDatabaseTypes(_testDbType)) {
        s_logger.info("Creating " + dbType + " test database...");

        String dbUrl = TestProperties.getDbHost(dbType);
        String user = TestProperties.getDbUsername(dbType);
        String password = TestProperties.getDbPassword(dbType);

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

    s_logger.info("All tasks succeeded.");
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
    options.addOption("createtables", "createtables", true, "Runs {dbscriptbasedir}/db/{dbtype}/scripts_<latest version>/create-db.sql.");
    options.addOption("dbscriptbasedir", "dbscriptbasedir", true, "Directory for reading db create scripts. " +
      "Optional. If not specified, the working directory is used.");
    options.addOption("targetversion", "targetversion", true, "Version number for the end result database. Optional. If not specified, assumes latest version.");
    options.addOption("createversion", "createversion", true, "Version number to run the creation script from. Optional. If not specified, defaults to {targetversion}.");
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
      s_logger.info(e.getMessage());
      usage(options);
      System.exit(-1);
    }

    System.exit(0);
  }

}
