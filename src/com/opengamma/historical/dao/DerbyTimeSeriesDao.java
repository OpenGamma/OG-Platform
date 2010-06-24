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

  /**
   * @param transactionManager the transactionManager not-null
   * @param namedSQLMap the map containing the sql queries not-null
   */
  public DerbyTimeSeriesDao(DataSourceTransactionManager transactionManager, Map<String, String> namedSQLMap) {
    super(transactionManager, namedSQLMap);
  }

  @Override
  protected boolean isTriggerSupported() {
    return false;
  }
  
}
