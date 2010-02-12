/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.historical.dao;

import org.springframework.jdbc.datasource.DataSourceTransactionManager;

/**
 * 
 *
 * @author yomi
 */
public class PostgresTimeSeriesDao extends RowStoreJdbcDao {

  /**
   * @param transactionManager
   */
  public PostgresTimeSeriesDao(DataSourceTransactionManager transactionManager) {
    super(transactionManager);
  }

}
