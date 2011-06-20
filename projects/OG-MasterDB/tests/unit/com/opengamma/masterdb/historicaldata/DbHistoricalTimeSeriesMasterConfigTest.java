/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.historicaldata;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.dbcp.BasicDataSource;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.testng.annotations.Test;

import com.opengamma.masterdb.historicaldata.LocalDateDbHistoricalTimeSeriesMaster;
import com.opengamma.util.db.DbSource;
import com.opengamma.util.db.MockDbHelper;

/**
 * Test to check master is properly configured.
 */
public class DbHistoricalTimeSeriesMasterConfigTest {

  DbSource _dbSource = new DbSource("Foo", 
      new BasicDataSource(), 
      new MockDbHelper(), 
      null, 
      new DefaultTransactionDefinition(), 
      new DataSourceTransactionManager(new BasicDataSource()));

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void missingDataSourceTransactionManager() throws Exception {
    Map<String, String> namedSQLMap = new HashMap<String, String>();
    new LocalDateDbHistoricalTimeSeriesMaster(null, namedSQLMap, false);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void missingDataSource() throws Exception {
    //transaction manager with no data source
    new LocalDateDbHistoricalTimeSeriesMaster(_dbSource, null, false);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void missingNamedSQLMap() throws Exception {
    new LocalDateDbHistoricalTimeSeriesMaster(_dbSource, null, false);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void invalidNamedSQLMap() throws Exception {
    Map<String, String> namedSQLMap = new HashMap<String, String>();
    new LocalDateDbHistoricalTimeSeriesMaster(_dbSource, namedSQLMap, false);
  }

}
