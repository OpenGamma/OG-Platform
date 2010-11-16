/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.util.test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.db.DbHelper;
import com.opengamma.util.db.DbSource;
import com.opengamma.util.db.HSQLDbHelper;
import com.opengamma.util.db.PostgreSQLDbHelper;
import com.opengamma.util.test.DBTool.TableCreationCallback;

/**
 * 
 *
 */
@RunWith(Parameterized.class)
abstract public class DBTest implements TableCreationCallback {
  
  private static Map<String,String> s_databaseTypeVersion = new HashMap<String,String> ();
  
  private static final Map<String, DbHelper> s_dbHelpers = new HashMap<String, DbHelper>();

  static {
    addDbHelper("hsqldb", new HSQLDbHelper());
    addDbHelper("postgres", new PostgreSQLDbHelper());
  }
  
  private final String _databaseType;
  private final String _databaseVersion;
  private final DBTool _dbtool;
  
  protected DBTest(String databaseType, String databaseVersion) {
    ArgumentChecker.notNull(databaseType, "databaseType");
    _databaseType = databaseType;
    _dbtool = TestProperties.getDbTool(databaseType);
    _dbtool.setJdbcUrl(getDbTool().getTestDatabaseUrl());
    _databaseVersion = databaseVersion;
  }
  
  /**
   * Initialise the database to the required version. This tracks the last initialised version
   * in a static map to avoid duplicate DB operations on bigger test classes. This might not be
   * such a good idea.
   */
  @Before
  public void setUp() throws Exception {
    String prevVersion = s_databaseTypeVersion.get(getDatabaseType());
    if ((prevVersion == null) || !prevVersion.equals(getDatabaseVersion())) {
      s_databaseTypeVersion.put(getDatabaseType(), getDatabaseVersion ());
      _dbtool.setCreateVersion(getDatabaseVersion());
      _dbtool.dropTestSchema();
      _dbtool.createTestSchema();
      _dbtool.createTestTables(this);
    }
    _dbtool.clearTestTables();
  }

  @After
  public void tearDown() throws Exception {
    _dbtool.resetTestCatalog(); // avoids locking issues with Derby
  }

  protected static Collection<Object[]> getParameters (final String databaseType, final int previousVersionCount) {
    ArrayList<Object[]> returnValue = new ArrayList<Object[]>();
    for (String db : TestProperties.getDatabaseTypes(databaseType)) {
      final DBTool dbTool = TestProperties.getDbTool (db);
      final String[] versions = dbTool.getDatabaseCreatableVersions ();
      for (int i = 0; i < versions.length; i++) {
        returnValue.add (new Object[] { db, versions[i] });
        if (i >= previousVersionCount) break;
      }
    }
    return returnValue;
  }
  
  protected static Collection<Object[]> getParameters (final int previousVersionCount) {
    String databaseType = System.getProperty("test.database.type");
    if (databaseType == null) {
      databaseType = "all";
    }
    return getParameters (databaseType, previousVersionCount);
  }

  @Parameters
  public static Collection<Object[]> getParameters() {
    int previousVersionCount = getPreviousVersionCount();
    return getParameters (previousVersionCount);
  }

  protected static int getPreviousVersionCount() {
    String previousVersionCountString = System.getProperty("test.database.previousVersions");
    int previousVersionCount;
    if (previousVersionCountString == null) {
      previousVersionCount = 0; // If you run from Eclipse, use current version only
    } else {
      previousVersionCount = Integer.parseInt (previousVersionCountString);
    }
    return previousVersionCount;
  }
  
  public DBTool getDbTool() {
    return _dbtool;
  }
  
  public String getDatabaseType() {
    return _databaseType;
  }
  
  public String getDatabaseVersion () {
    return _databaseVersion;
  }
  
  public DataSourceTransactionManager getTransactionManager() {
    return getDbTool().getTransactionManager();
  }
  
  public DbSource getDbSource() {
    DefaultTransactionDefinition transactionDefinition = new DefaultTransactionDefinition();
    transactionDefinition.setIsolationLevel(TransactionDefinition.ISOLATION_SERIALIZABLE);
    transactionDefinition.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

    DbHelper dbHelper = s_dbHelpers.get(getDatabaseType());
    if (dbHelper == null) {
      throw new OpenGammaRuntimeException("config error - no DBHelper setup for " + getDatabaseType());
    }
    
    DbSource dbSource = new DbSource("DBTest", getTransactionManager().getDataSource(),
        dbHelper, null, transactionDefinition, getTransactionManager());
    return dbSource;
  }
  
  public static void addDbHelper(String dbType, DbHelper helper) {
    s_dbHelpers.put(dbType, helper);
  }

  /**
   * Override this if you wish to do something with the database while it is in its "upgrading" state - e.g. populate with test data
   * at a particular version to test the data transformations on the next version upgrades.
   */
  public void tablesCreatedOrUpgraded (final String version) {
    // No action 
  }

}
