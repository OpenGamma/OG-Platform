/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.timeseries;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.dbcp.BasicDataSource;
import org.junit.Test;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import com.opengamma.masterdb.timeseries.LocalDateDbTimeSeriesMaster;
import com.opengamma.util.db.DbSource;
import com.opengamma.util.db.MockDbHelper;

/**
 * Test to check DbTimeSeriesMaster is properly configured.
 */
public class DbTimeSeriesMasterConfigTest {

  DbSource _dbSource = new DbSource("Foo", 
      new BasicDataSource(), 
      new MockDbHelper(), 
      null, 
      new DefaultTransactionDefinition(), 
      new DataSourceTransactionManager(new BasicDataSource()));

  @Test(expected = IllegalArgumentException.class)
  public void missingDataSourceTransactionManager() throws Exception {
    Map<String, String> namedSQLMap = new HashMap<String, String>();
    new LocalDateDbTimeSeriesMaster(null, namedSQLMap, false);
  }

  @Test(expected = IllegalArgumentException.class)
  public void missingDataSource() throws Exception {
    //transaction manager with no data source
    new LocalDateDbTimeSeriesMaster(_dbSource, null, false);
  }

  @Test(expected = IllegalArgumentException.class)
  public void missingNamedSQLMap() throws Exception {
    new LocalDateDbTimeSeriesMaster(_dbSource, null, false);
  }

  @Test(expected = IllegalArgumentException.class)
  public void invalidNamedSQLMap() throws Exception {
    Map<String, String> namedSQLMap = new HashMap<String, String>();
    new LocalDateDbTimeSeriesMaster(_dbSource, namedSQLMap, false);
  }

}
