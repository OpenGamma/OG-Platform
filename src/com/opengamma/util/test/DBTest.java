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

import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.test.DBTool.TableCreationCallback;

/**
 * 
 *
 * @author pietari
 */
@RunWith(Parameterized.class)
abstract public class DBTest implements TableCreationCallback {
  
  private static Map<String,String> s_databaseTypeVersion = new HashMap<String,String> ();
  
  private final String _databaseType;
  private final String _databaseVersion;
  private final DBTool _dbtool;
  
  protected DBTest(String databaseType, String databaseVersion) {
    ArgumentChecker.notNull(databaseType, "Database type");
    _databaseType = databaseType;
    _dbtool = TestProperties.getDbTool(databaseType);
    _databaseVersion = databaseVersion;
  }
  
  /**
   * Initialise the database to the required version. This tracks the last initialised version
   * in a static map to avoid duplicate DB operations on bigger test classes. This might not be
   * such a good idea.
   */
  @Before
  public void init () {
    String prevVersion = s_databaseTypeVersion.get (getDatabaseType ());
    if ((prevVersion == null) || !prevVersion.equals (getDatabaseVersion ())) {
      s_databaseTypeVersion.put (getDatabaseType (), getDatabaseVersion ());
      _dbtool.setCreateVersion (getDatabaseVersion ());
      _dbtool.dropTestSchema();
      _dbtool.createTestSchema();
      _dbtool.createTestTables(this);
    }
  }
  
  @Parameters
  public static Collection<Object[]> getDatabaseTypes() {
    String databaseType = System.getProperty("test.database.type");
    String previousVersionCountString = System.getProperty("test.database.previousVersions");
    int previousVersionCount;
    if (databaseType == null) {
      databaseType = "derby"; // If you run from Eclipse, use Derby only
    }
    if (previousVersionCountString == null) {
      previousVersionCount = 0; // If you run from Eclipse, use current version only
    } else {
      previousVersionCount = Integer.parseInt (previousVersionCountString);
    }
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
  
  @Before
  public void setUp() throws Exception {
    _dbtool.clearTestTables();
  }
  
  @After
  public void tearDown() throws Exception {
    _dbtool.shutdown(); // avoids locking issues with Derby
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
  
  /**
   * Override this if you wish to do something with the database while it is in its "upgrading" state - e.g. populate with test data
   * at a particular version to test the data transformations on the next version upgrades.
   */
  public void tablesCreatedOrUpgraded (final String version) {
    // No action 
  }

}
