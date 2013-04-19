/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.timeseries.filter;

import org.apache.commons.lang.ObjectUtils;

import com.opengamma.timeseries.date.localdate.LocalDateDoubleTimeSeries;
import com.opengamma.util.ArgumentChecker;

/**
 * A partioned time-series, storing both the filtered and rejected series.
 */
public class FilteredTimeSeries {

  /**
   * The filtered/accepted time-series.
   */
  private final LocalDateDoubleTimeSeries _filteredTS;
  /**
   * The rejected time-series.
   */
  private final LocalDateDoubleTimeSeries _rejectedTS;

  /**
   * Creates an instance.
   * 
   * @param filteredTS  the filtered series, not null
   * @param rejectedTS  the rejected series, not null
   */
  public FilteredTimeSeries(final LocalDateDoubleTimeSeries filteredTS, final LocalDateDoubleTimeSeries rejectedTS) {
    ArgumentChecker.notNull(filteredTS, "filteredTS");
    ArgumentChecker.notNull(rejectedTS, "rejectedTS");
    _filteredTS = filteredTS;
    _rejectedTS = rejectedTS;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the filtered/accepted time-series.
   * 
   * @return the series, not null
   */
  public LocalDateDoubleTimeSeries getFilteredTS() {
    return _filteredTS;
  }

  /**
   * Gets the rejected time-series.
   * 
   * @return the series, not null
   */
  public LocalDateDoubleTimeSeries getRejectedTS() {
    return _rejectedTS;
  }

  //-------------------------------------------------------------------------
  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final FilteredTimeSeries other = (FilteredTimeSeries) obj;
    return ObjectUtils.equals(_filteredTS, other._filteredTS) && ObjectUtils.equals(_rejectedTS, other._rejectedTS);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + (_filteredTS == null ? 0 : _filteredTS.hashCode());
    result = prime * result + (_rejectedTS == null ? 0 : _rejectedTS.hashCode());
    return result;
  }

}
