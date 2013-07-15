/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.master.historicaltimeseries.impl;

import java.util.HashMap;
import java.util.Map;

import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesAdjuster;
import com.opengamma.util.ArgumentChecker;

/**
 * Contains field adjustments for a data source.
 */
public class HistoricalTimeSeriesFieldAdjustmentMap {

  private final String _dataSource;
  private final Map<String, HistoricalTimeSeriesFieldAdjustment> _fieldAdjustments = new HashMap<String, HistoricalTimeSeriesFieldAdjustment>();
  
  public HistoricalTimeSeriesFieldAdjustmentMap(String dataSource) {
    ArgumentChecker.notNull(dataSource, "dataSource");
    _dataSource = dataSource;
  }
  
  /**
   * Gets the data source name.
   * 
   * @return the data source name, not null
   */
  public String getDataSource() {
    return _dataSource;
  }

  /**
   * Gets any field adjustment for a given requested field.
   * 
   * @param requestedField  the requested field, not null
   * @return the field adjustment, or null if no adjustment applies
   */
  public HistoricalTimeSeriesFieldAdjustment getFieldAdjustment(String requestedField) {
    return _fieldAdjustments.get(requestedField);
  }
  
  /**
   * Adds a field adjustment to the map.
   * 
   * @param requestedField  the requested field, not null
   * @param underlyingProvider  the underlying provider, null for any
   * @param underlyingField  the underlying field, not null
   * @param adjuster  the adjuster, null for none
   */
  public void addFieldAdjustment(String requestedField, String underlyingProvider, String underlyingField, HistoricalTimeSeriesAdjuster adjuster) {
    _fieldAdjustments.put(requestedField, new HistoricalTimeSeriesFieldAdjustment(underlyingProvider, underlyingField, adjuster));
  }
  
}
