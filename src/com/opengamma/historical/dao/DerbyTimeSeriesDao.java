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
 * @author yomi
 */
public class DerbyTimeSeriesDao extends RowStoreJdbcDao {
  
  private Map<String, String> _namedSQLMap;

  /**
   * @param dataSource
   */
  public DerbyTimeSeriesDao(DataSourceTransactionManager transactionManager) {
    super(transactionManager);
  }

  @Override
  protected boolean isTriggerSupported() {
    return false;
  }
  
  public void setNamedSQLMap(Map<String, String> namedSQLMap) {
    _namedSQLMap = namedSQLMap;
  }

  @Override
  protected Map<String, String> getSqlQueries() {
    return _namedSQLMap;
  }
  
}
