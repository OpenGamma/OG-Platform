/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.test;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;

import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.ReflectionUtils;
import com.opengamma.util.db.DbConnector;
import com.opengamma.util.db.DbConnectorFactoryBean;
import com.opengamma.util.db.DbDialect;
import com.opengamma.util.db.script.DbSchemaGroupMetadata;
import com.opengamma.util.db.tool.DbDialectUtils;
import com.opengamma.util.db.tool.DbTool;
import com.opengamma.util.db.tool.DbTool.TableCreationCallback;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 * Base DB test.
 */
public abstract class AbstractDbTest implements TableCreationCallback {

  /** Cache. */
  static final Map<String, String> s_databaseTypeVersion = new ConcurrentHashMap<>();
  /** Initialized tools. */
  private static final ConcurrentMap<Pair<String, Class<?>>, DbConnector> s_connectors = new ConcurrentHashMap<>();

  static {
    // initialize the clock
    DateUtils.initTimeZone();
  }

  private final String _databaseType;
  private final String _databaseVersion;
  private volatile DbTool _dbTool;

  //-------------------------------------------------------------------------
  /**
   * Creates an instance.
   * 
   * @param databaseType  the database type, not null
   * @param targetVersion  the target version
   */
  protected AbstractDbTest(String databaseType, String targetVersion) {
    ArgumentChecker.notNull(databaseType, "databaseType");
    _databaseType = databaseType;
    _databaseVersion = targetVersion;
  }

  //-------------------------------------------------------------------------
  /**
   * Code run before each subclass.
   * @throws Exception if an error occurs
   */
  @BeforeClass(alwaysRun = true)
  public final void setUpClass() throws Exception {
    doSetUpClass();
  }

  /**
   * Subclasses should override this where necessary and NOT declare @BeforeClass.
   * This handles TestNG behavior better.
   * @throws Exception if an error occurs
   */
  protected void doSetUpClass() throws Exception {
    // override in subclasses
  }

  /**
   * Code run before each test method.
   * Initialize the database to the required version.
   * This tracks the last initialized version in a static map to avoid duplicate
   * DB operations on bigger test classes. This might not be such a good idea.
   */
  @BeforeMethod(alwaysRun = true)
  public final void setUp() throws Exception {
    DbTool dbTool = getDbTool();
    String prevVersion = s_databaseTypeVersion.get(getDatabaseType());
    if ((prevVersion == null) || !prevVersion.equals(getDatabaseVersion())) {
      s_databaseTypeVersion.put(getDatabaseType(), getDatabaseVersion());

      String user = dbTool.getUser();
      String password = dbTool.getPassword();
      String systemUser = System.getProperty("system.user");
      String systemPassword = System.getProperty("system.password");
      if ("oracle11g".equals(getDatabaseType())) {
        if (systemUser == null) {
          systemUser = TestProperties.getTestProperties().getProperty("oracle11g.jdbc.system.username");
        }
        if (systemPassword == null) {
          systemPassword = TestProperties.getTestProperties().getProperty("oracle11g.jdbc.system.password");
        }
      }
      if (systemUser != null && systemPassword != null) {
        dbTool.setUser(systemUser);
        dbTool.setPassword(systemPassword);
      } else {
        dbTool.setUser(user);
        dbTool.setPassword(password);
      }

      dbTool.setTargetVersion(getDatabaseVersion());
      dbTool.setCreateVersion(getDatabaseVersion());
      dbTool.dropTestSchema();
      dbTool.createTestSchema();
      //
      dbTool.setUser(user);
      dbTool.setPassword(password);
      //
      dbTool.createTestTables(this);
    }
    dbTool.clearTestTables();
    doSetUp();
  }

  /**
   * Subclasses should override this where necessary and NOT declare @BeforeMethod.
   * This handles TestNG behavior better.
   * @throws Exception if an error occurs
   */
  protected void doSetUp() throws Exception {
    // override in subclasses
  }

  /**
   * Code run after each test method.
   * @throws Exception if an error occurs
   */
  @AfterMethod(alwaysRun = true)
  public final void tearDown() throws Exception {
    doTearDown();
    DbTool dbTool = _dbTool;
    if (dbTool != null) {
      dbTool.resetTestCatalog(); // avoids locking issues with Derby
    }
  }

  /**
   * Subclasses should override this where necessary and NOT declare @AfterMethod.
   * This handles TestNG behavior better.
   * 
   * @throws Exception if an error occurs
   */
  protected void doTearDown() throws Exception {
    // override in subclasses
  }

  /**
   * Code run after each subclass.
   * @throws Exception if an error occurs
   */
  @AfterClass(alwaysRun = true)
  public void tearDownClass() throws Exception {
    doTearDownClass();
    DbTool dbTool = _dbTool;
    if (dbTool != null) {
      dbTool.resetTestCatalog(); // avoids locking issues with Derby
    }
    _dbTool = null;  // do not close as we want to retain the data source
  }

  /**
   * Subclasses should override this where necessary and NOT declare @AfterClass.
   * This handles TestNG behavior better.
   * 
   * @throws Exception if an error occurs
   */
  protected void doTearDownClass() throws Exception {
    // override in subclasses
  }

  /**
   * Code run after entire suite.
   * @throws Exception if an error occurs
   */
  @AfterSuite(groups = {TestGroup.UNIT_DB, TestGroup.INTEGRATION })
  public static final void tearDownSuite() throws Exception {
    for (DbConnector connector : s_connectors.values()) {
      ReflectionUtils.close(connector);
    }
  }

  //-------------------------------------------------------------------------
  protected String getDatabaseType() {
    return _databaseType;
  }

  protected String getDatabaseVersion() {
    return _databaseVersion;
  }

  //-------------------------------------------------------------------------
  protected DbTool getDbTool() {
    return initDbTool();
  }

  protected DataSourceTransactionManager getTransactionManager() {
    return new DataSourceTransactionManager(getDbTool().getDataSource());
  }

  protected DbConnector getDbConnector() {
    return initConnector();
  }

  /**
   * Override this when enhancing the connector factory.
   * 
   * @return a class key to indicate the scope of the enhancement, not null
   */
  protected Class<?> dbConnectorScope() {
    // for subclasses
    return Object.class;
  }

  /**
   * Override this to enhance the connector factory.
   * 
   * @param factory  the factory to populate, not null
   */
  protected void initDbConnectorFactory(DbConnectorFactoryBean factory) {
    // for subclasses
  }

  /**
   * Initializes the DBTool outside the constructor.
   * This works better with TestNG and Maven, where the constructor is called
   * even if the test is never run.
   */
  private DbTool initDbTool() {
    DbTool dbTool = _dbTool;
    if (dbTool == null) {
      synchronized (this) {
        dbTool = _dbTool;
        if (dbTool == null) {
          DbConnector connector = s_connectors.get(Pairs.of(_databaseType, dbConnectorScope()));
          _dbTool = dbTool = DbTest.createDbTool(_databaseType, connector);  // CSIGNORE
        }
      }
    }
    return dbTool;
  }

  private DbConnector initConnector() {
    Class<?> scope = dbConnectorScope();
    Pair<String, Class<?>> key = Pairs.<String, Class<?>>of(_databaseType, scope);
    DbConnector connector = s_connectors.get(key);
    if (connector == null) {
      synchronized (this) {
        connector = s_connectors.get(key);
        if (connector == null) {
          DbDialect dbDialect = DbDialectUtils.getSupportedDbDialect(getDatabaseType());
          DbConnectorFactoryBean factory = new DbConnectorFactoryBean();
          factory.setName("DbTest-" + dbDialect.getName() + (scope != null ? "-" + scope.getSimpleName() : ""));
          factory.setDialect(dbDialect);
          factory.setDataSource(getDbTool().getDataSource());
          factory.setTransactionIsolationLevelName("ISOLATION_READ_COMMITTED");
          factory.setTransactionPropagationBehaviorName("PROPAGATION_REQUIRED");
          initDbConnectorFactory(factory);
          connector = factory.createObject();
          s_connectors.put(key, connector);
        }
      }
    }
    return connector;
  }

  //-------------------------------------------------------------------------
  /**
   * Override this if you wish to do something with the database while it is
   * in its "upgrading" state - e.g. populate with test data at a particular
   * version to test the data transformations on the next version upgrades.
   * 
   * @param version  the version
   * @param schemaGroupMetadata  the database schema group metadata, not null
   */
  public void tablesCreatedOrUpgraded(final int version, final DbSchemaGroupMetadata schemaGroupMetadata) {
    // No action 
  }

  //-------------------------------------------------------------------------
  @Override
  public String toString() {
    return getDatabaseType() + ":" + getDatabaseVersion();
  }

}
