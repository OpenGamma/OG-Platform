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

import com.opengamma.timeseries.db.RowStoreTimeSeriesMaster;

/**
 * Test to check RowStoreJdbcDao is properly configured
 * 
 */
public class RowStoreTimeSeriesMasterConfigTest {

  @Test(expected = IllegalArgumentException.class)
  public void missingDataSourceTransactionManager() throws Exception {

    Map<String, String> namedSQLMap = new HashMap<String, String>();

    new RowStoreTimeSeriesMaster(null, namedSQLMap) {
      @Override
      protected boolean isTriggerSupported() {
        return false;
      }
    };
  }

  @Test(expected = IllegalArgumentException.class)
  public void missingDataSource() throws Exception {
    //transaction manager with no data source
    DataSourceTransactionManager transactionManager = new DataSourceTransactionManager();

    new RowStoreTimeSeriesMaster(transactionManager, null) {
      @Override
      protected boolean isTriggerSupported() {
        return false;
      }
    };
  }

  @Test(expected = IllegalArgumentException.class)
  public void missingNamedSQLMap() throws Exception {
    DataSourceTransactionManager transactionManager = new DataSourceTransactionManager(new BasicDataSource());
    new RowStoreTimeSeriesMaster(transactionManager, null) {
      @Override
      protected boolean isTriggerSupported() {
        return false;
      }
    };
  }

  @Test(expected = IllegalArgumentException.class)
  public void invalidNamedSQLMap() throws Exception {
    DataSourceTransactionManager transactionManager = new DataSourceTransactionManager(new BasicDataSource());
    Map<String, String> namedSQLMap = new HashMap<String, String>();
    new RowStoreTimeSeriesMaster(transactionManager, namedSQLMap) {
      @Override
      protected boolean isTriggerSupported() {
        return false;
      }
    };
  }

}
