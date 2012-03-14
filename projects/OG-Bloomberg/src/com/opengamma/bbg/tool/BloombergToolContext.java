/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.bbg.tool;

import com.opengamma.bbg.BloombergSecurityMaster;
import com.opengamma.bbg.ReferenceDataProvider;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesSource;

/**
 * Exposes elements of a Bloomberg market data server in a tool context.
 */
public interface BloombergToolContext {

  /**
   * Gets the Bloomberg security source.
   * 
   * @return the Bloomberg security source
   */
  BloombergSecurityMaster getBloombergSecuritySource();

  /**
   * Gets the Bloomberg reference data provider.
   * 
   * @return the Bloomberg reference data provider
   */
  ReferenceDataProvider getBloombergReferenceDataProvider();

  /**
   * Gets the Bloomberg historical time-series source.
   * 
   * @return the Bloomberg historical time-series source
   */
  HistoricalTimeSeriesSource getBloombergHistoricalTimeSeriesSource();

}
