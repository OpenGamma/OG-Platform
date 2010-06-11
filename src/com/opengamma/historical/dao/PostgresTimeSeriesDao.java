/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.historical.dao;

import java.util.Map;

import org.springframework.jdbc.datasource.DataSourceTransactionManager;

/**
 * 
 *
 */
public class PostgresTimeSeriesDao extends RowStoreJdbcDao {
  
  private Map<String, String> _namedSQLMap;

  public PostgresTimeSeriesDao(DataSourceTransactionManager transactionManager) {
    super(transactionManager);
  }
  
  public void setNamedSQLMap(Map<String, String> namedSQLMap) {
    _namedSQLMap = namedSQLMap;
  }

  @Override
  protected boolean isTriggerSupported() {
    return false;
  }

  @Override
  protected Map<String, String> getSqlQueries() {
    return _namedSQLMap;
  }
  
}
