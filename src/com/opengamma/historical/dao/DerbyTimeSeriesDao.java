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
public class DerbyTimeSeriesDao extends RowStoreJdbcDao {

  /**
   * @param dataSource
   */
  public DerbyTimeSeriesDao(DataSourceTransactionManager transactionManager) {
    super(transactionManager);
  }

}
