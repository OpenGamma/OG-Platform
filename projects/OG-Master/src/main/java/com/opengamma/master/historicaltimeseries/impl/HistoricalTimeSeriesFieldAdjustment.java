/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.master.historicaltimeseries.impl;

import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesAdjuster;
import com.opengamma.util.ArgumentChecker;

/**
 * Represents an historical time-series field adjustment.
 */
public class HistoricalTimeSeriesFieldAdjustment {

  private final String _underlyingDataProvider;
  private final String _underlyingDataField;
  private final HistoricalTimeSeriesAdjuster _adjuster;
  
  public HistoricalTimeSeriesFieldAdjustment(String underlyingDataProvider, String underlyingDataField, HistoricalTimeSeriesAdjuster adjuster) {
    ArgumentChecker.notNull(underlyingDataField, "underlyingDataField");
    _underlyingDataProvider = underlyingDataProvider;
    _underlyingDataField = underlyingDataField;
    _adjuster = adjuster;
  }
  
  /**
   * Gets the underlying data provider name.
   * 
   * @return the underlying data provider name, null for any
   */
  public String getUnderlyingDataProvider() {
    return _underlyingDataProvider;
  }

  /**
   * Gets the underlying data field name.
   * 
   * @return the underlying data field name, not null
   */
  public String getUnderlyingDataField() {
    return _underlyingDataField;
  }

  /**
   * Gets the adjuster to apply.
   * 
   * @return the adjuster to apply, not null
   */
  public HistoricalTimeSeriesAdjuster getAdjuster() {
    return _adjuster;
  }
    
}
