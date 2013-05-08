/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.db.DbConnector;
import com.opengamma.util.db.DbConnectorFactoryBean;
import com.opengamma.util.db.DbDialect;
import com.opengamma.util.db.HSQLDbDialect;
import com.opengamma.util.db.PostgresDbDialect;
import com.opengamma.util.db.SqlServer2008DbDialect;
import com.opengamma.util.test.DbTool.TableCreationCallback;
import com.opengamma.util.time.DateUtils;

/**
 * Base DB test.
 */
public abstract class DbTest implements TableCreationCallback {

  /** Cache. */
  protected static Map<String, String> s_databaseTypeVersion = new HashMap<>();
  /** Known dialects. */
  private static final Map<String, DbDialect> s_dbDialects = new HashMap<>();

  static {
    // initialize the clock
    DateUtils.initTimeZone();
    
    // setup the known databases
    addDbDialect("hsqldb", new HSQLDbDialect());
    addDbDialect("postgres", new PostgresDbDialect());
    addDbDialect("sqlserver2008", new SqlServer2008DbDialect());
  }

  private final String _databaseType;
  private final String _targetVersion;
  private final String _createVersion;
  private volatile DbTool _dbtool;

  //-------------------------------------------------------------------------
  /**
   * Creates an instance.
   * 
   * @param databaseType  the database type
   * @param targetVersion  the target version
   * @param createVersion  the create version
   */
  protected DbTest(String databaseType, String targetVersion, String createVersion) {
    ArgumentChecker.notNull(databaseType, "databaseType");
    _databaseType = databaseType;
    _targetVersion = targetVersion;
    _createVersion = createVersion;
  }

  //-------------------------------------------------------------------------
  /**
   * Initializes the DBTool outside the constructor.
   * This works better with TestNG and Maven, where the constructor is called
   * even if the test is never run.
   */
  private DbTool ensureDbTool() {
    if (_dbtool == null) {
      synchronized (this) {
        if (_dbtool == null) {
          ArgumentChecker.notNull(_databaseType, "_databaseType");
          _dbtool = DbTestProperties.getDbTool(_databaseType);
          _dbtool.setJdbcUrl(getDbTool().getTestDatabaseUrl());
          _dbtool.addDbScriptDirectories(DbScripts.getSqlScriptDir());
        }
      }
    }
    return _dbtool;
  }

  /**
   * Initialize the database to the required version.
   * This tracks the last initialized version in a static map to avoid duplicate
   * DB operations on bigger test classes. This might not be such a good idea.
   */
  @BeforeMethod(groups = {TestGroup.UNIT_DB, TestGroup.INTEGRATION})
  public void setUp() throws Exception {
    DbTool dbTool = ensureDbTool();
    String prevVersion = s_databaseTypeVersion.get(getDatabaseType());
    if ((prevVersion == null) || !prevVersion.equals(getTargetVersion())) {
      s_databaseTypeVersion.put(getDatabaseType(), getTargetVersion());
      dbTool.setTargetVersion(getTargetVersion());
      dbTool.setCreateVersion(getCreateVersion());
      dbTool.dropTestSchema();
      dbTool.createTestSchema();
      dbTool.createTestTables(this);
    }
    dbTool.clearTestTables();
  }

  //-------------------------------------------------------------------------
  @AfterMethod(groups = {TestGroup.UNIT_DB, TestGroup.INTEGRATION})
  public void tearDown() throws Exception {
    DbTool dbTool = _dbtool;
    if (dbTool != null) {
      _dbtool.resetTestCatalog(); // avoids locking issues with Derby
    }
  }

  @AfterClass(groups = {TestGroup.UNIT_DB, TestGroup.INTEGRATION})
  public void tearDownClass() throws Exception {
    DbTool dbTool = _dbtool;
    if (dbTool != null) {
      _dbtool.close();
    }
  }

  @AfterSuite(groups = {TestGroup.UNIT_DB, TestGroup.INTEGRATION})
  public static void cleanUp() {
    DbScripts.deleteSqlScriptDir();
  }

  //-------------------------------------------------------------------------
  protected static Object[][] getParametersForSeparateMasters(int prevVersionCount) {
    String testDatabaseType = System.getProperty("test.database.type");
    Collection<String> databaseTypes;
    if (testDatabaseType == null) {
      databaseTypes = new ArrayList<>(s_dbDialects.keySet());
    } else {
      if (s_dbDialects.containsKey(testDatabaseType) == false) {
        throw new IllegalArgumentException("Unknown database: " + testDatabaseType);
      }
      databaseTypes = Collections.singleton(testDatabaseType);
    }
    ArrayList<Object[]> parameters = new ArrayList<Object[]>();
    for (String databaseType : databaseTypes) {
      DbScripts scripts = DbScripts.of(DbScripts.getSqlScriptDir(), databaseType);
      Map<String, SortedMap<Integer, DbScriptPair>> scriptPairs = scripts.getScriptPairs();
      for (String schema : scriptPairs.keySet()) {
        Set<Integer> versions = scriptPairs.get(schema).keySet();
        int max = Collections.max(versions);
        int min = Collections.min(versions);
        for (int v = max; v >= Math.max(max - prevVersionCount, min); v--) {
          parameters.add(new Object[]{databaseType, schema, "" + max /*target_version*/, "" + v /*migrate_from_version*/});
        }
      }
    }
    Object[][] array = new Object[parameters.size()][];
    parameters.toArray(array);
    return array;
  }

  protected static Object[][] getParameters() {
    String databaseType = System.getProperty("test.database.type");
    if (databaseType == null) {
      databaseType = "all";
    }
    Collection<String> databaseTypes = DbTestProperties.getDatabaseTypes(databaseType);
    ArrayList<Object[]> parameters = new ArrayList<Object[]>();
    for (String dbType : databaseTypes) {
      parameters.add(new Object[]{dbType, "latest"});
    }
    Object[][] array = new Object[parameters.size()][];
    parameters.toArray(array);
    return array;
  }

  public static Object[][] getParametersForDatabase(final String databaseType) {
    ArrayList<Object[]> parameters = new ArrayList<Object[]>();
    for (String db : DbTestProperties.getDatabaseTypes(databaseType)) {
      parameters.add(new Object[]{db, "latest"});
    }
    Object[][] array = new Object[parameters.size()][];
    parameters.toArray(array);
    return array;
  }

  @DataProvider(name = "localDatabase")
  public static Object[][] data_localDatabase() {
    return getParametersForDatabase("hsqldb");
  }

  @DataProvider(name = "databases")
  public static Object[][] data_databases() {
    try {
      return getParameters();
    } catch (Exception ex) {
      System.out.println(ex.getMessage());
      return null;
    }
  }

  @DataProvider(name = "databasesVersionsForSeparateMasters")
  public static Object[][] data_databasesVersionsForSeparateMasters() {
    return getParametersForSeparateMasters(3);
  }

  protected static int getPreviousVersionCount() {
    String previousVersionCountString = System.getProperty("test.database.previousVersions");
    int previousVersionCount;
    if (previousVersionCountString == null) {
      previousVersionCount = 0; // If you run from Eclipse, use current version only
    } else {
      previousVersionCount = Integer.parseInt(previousVersionCountString);
    }
    return previousVersionCount;
  }

  //-------------------------------------------------------------------------
  public DbTool getDbTool() {
    return ensureDbTool();
  }

  public String getDatabaseType() {
    return _databaseType;
  }

  public String getCreateVersion() {
    return _createVersion;
  }

  public String getTargetVersion() {
    return _targetVersion;
  }

  public DataSourceTransactionManager getTransactionManager() {
    return new DataSourceTransactionManager(getDbTool().getDataSource());
  }

  public DbConnector getDbConnector() {
    DbDialect dbDialect = s_dbDialects.get(getDatabaseType());
    if (dbDialect == null) {
      throw new OpenGammaRuntimeException("config error - no DBHelper setup for " + getDatabaseType());
    }
    DbConnectorFactoryBean factory = new DbConnectorFactoryBean();
    factory.setName("DbTest");
    factory.setDialect(dbDialect);
    factory.setDataSource(getDbTool().getDataSource());
    factory.setTransactionIsolationLevelName("ISOLATION_READ_COMMITTED");
    factory.setTransactionPropagationBehaviorName("PROPAGATION_REQUIRED");
    return factory.createObject();
  }

  /**
   * Adds a dialect to the map of known.
   *
   * @param dbType  the database type, not null
   * @param dialect  the dialect, not null
   */
  public static void addDbDialect(String dbType, DbDialect dialect) {
    s_dbDialects.put(dbType, dialect);
  }

  /**
   * Override this if you wish to do something with the database while it is in its "upgrading" state - e.g. populate with test data
   * at a particular version to test the data transformations on the next version upgrades.
   */
  public void tablesCreatedOrUpgraded(final String version, final String prefix) {
    // No action 
  }

  //-------------------------------------------------------------------------
  @Override
  public String toString() {
    return getDatabaseType() + ":" + getTargetVersion();
  }

}
