/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.timeseries.db;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.dbcp.BasicDataSource;
import org.junit.Test;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import com.opengamma.financial.timeseries.db.LocalDateRowStoreTimeSeriesMaster;
import com.opengamma.util.db.DbSource;
import com.opengamma.util.db.MockDbHelper;

/**
 * Test to check RowStoreJdbcDao is properly configured
 * 
 */
public class RowStoreTimeSeriesMasterConfigTest {
  
  DbSource _dbSource = new DbSource("Foo", 
      new BasicDataSource(), 
      new MockDbHelper(), 
      null, 
      new DefaultTransactionDefinition(), 
      new DataSourceTransactionManager(new BasicDataSource()));

  @Test(expected = IllegalArgumentException.class)
  public void missingDataSourceTransactionManager() throws Exception {
    Map<String, String> namedSQLMap = new HashMap<String, String>();
    new LocalDateRowStoreTimeSeriesMaster(null, namedSQLMap, false);
  }

  @Test(expected = IllegalArgumentException.class)
  public void missingDataSource() throws Exception {
    //transaction manager with no data source
    new LocalDateRowStoreTimeSeriesMaster(_dbSource, null, false);
  }

  @Test(expected = IllegalArgumentException.class)
  public void missingNamedSQLMap() throws Exception {
    new LocalDateRowStoreTimeSeriesMaster(_dbSource, null, false);
  }

  @Test(expected = IllegalArgumentException.class)
  public void invalidNamedSQLMap() throws Exception {
    Map<String, String> namedSQLMap = new HashMap<String, String>();
    new LocalDateRowStoreTimeSeriesMaster(_dbSource, namedSQLMap, false);
  }

}
