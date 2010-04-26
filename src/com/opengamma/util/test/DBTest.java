/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.util.test;

import java.util.ArrayList;
import java.util.Collection;

import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.opengamma.util.ArgumentChecker;

/**
 * 
 *
 * @author pietari
 */
@RunWith(Parameterized.class)
abstract public class DBTest {
  
  private final String _databaseType;
  private final DBTool _dbtool;
  
  protected DBTest(String databaseType) {
    ArgumentChecker.notNull(databaseType, "Database type");
    _databaseType = databaseType;
    _dbtool = TestProperties.getDbTool(databaseType);
  }
  
  @Parameters
  public static Collection<Object[]> getDatabaseTypes() {
    String databaseType = System.getProperty("test.database.type");
    if (databaseType == null) {
      databaseType = "derby"; // If you run from Eclipse, use Derby only
    }
    
    ArrayList<Object[]> returnValue = new ArrayList<Object[]>();
    for (String db : TestProperties.getDatabaseTypes(databaseType)) {
      returnValue.add(new Object[] { db });      
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

}
