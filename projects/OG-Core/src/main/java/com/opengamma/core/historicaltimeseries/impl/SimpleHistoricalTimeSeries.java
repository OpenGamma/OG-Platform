/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.historicaltimeseries.impl;

import java.io.Serializable;

import com.opengamma.core.historicaltimeseries.HistoricalTimeSeries;
import com.opengamma.id.UniqueId;
import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.timeseries.date.localdate.LocalDateDoubleTimeSeries;
import com.opengamma.util.ArgumentChecker;

/**
 * Simple immutable implementation of {@code HistoricalTimeSeries}.
 * <p>
 * This is the an immutable implementation of the {@link HistoricalTimeSeries} interface.
 * <p>
 * This class is immutable and thread-safe providing the time-series is immutable.
 * It is intended to be used in the engine via the read-only {@code HistoricalTimeSeries} interface.
 */
public final class SimpleHistoricalTimeSeries
    implements HistoricalTimeSeries, UniqueIdentifiable, Serializable {

  /** Serialization version. */
  private static final long serialVersionUID = 1L;

  /**
   * The unique identifier of the time-series.
   */
  private final UniqueId _uniqueId;
  /**
   * The time-series itself.
   */
  private final LocalDateDoubleTimeSeries _timeSeries;

  /**
   * Creates a historical time-series.
   * 
   * @param uniqueId  the unique identifier, not null
   * @param timeSeries  the time-series, not null
   */
  public SimpleHistoricalTimeSeries(UniqueId uniqueId, LocalDateDoubleTimeSeries timeSeries) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");
    ArgumentChecker.notNull(timeSeries, "timeSeries");
    _uniqueId = uniqueId;
    _timeSeries = timeSeries;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the unique identifier of the time-series.
   * 
   * @return the unique identifier, not null
   */
  @Override
  public UniqueId getUniqueId() {
    return _uniqueId;
  }

  /**
   * Gets the time-series data points.
   * 
   * @return the time-series data points, not null
   */
  @Override
  public LocalDateDoubleTimeSeries getTimeSeries() {
    return _timeSeries;
  }

  /**
   * Returns a copy of this time-series with a new set of data points.
   * <p>
   * This instance is immutable and unaffected by this method call.
   * 
   * @param timeSeries  the new time-series data points, not null
   * @return a time-series based on this one with different data points, not null
   */
  public SimpleHistoricalTimeSeries withTimeSeries(LocalDateDoubleTimeSeries timeSeries) {
    return new SimpleHistoricalTimeSeries(getUniqueId(), timeSeries);
  }

  //-------------------------------------------------------------------------
  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj instanceof SimpleHistoricalTimeSeries) {
      SimpleHistoricalTimeSeries other = (SimpleHistoricalTimeSeries) obj;
      return getUniqueId().equals(other.getUniqueId()) &&
              getTimeSeries().equals(other.getTimeSeries());
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hashCode = 7;
    hashCode += hashCode * 31 + getUniqueId().hashCode();
    hashCode += hashCode * 31 + getTimeSeries().hashCode();
    return hashCode;
  }

  @Override
  public String toString() {
    return "HistoricalTimeSeries[" + getUniqueId() + "]";
  }

}
