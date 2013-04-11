/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.timeseries.precise;

import com.opengamma.timeseries.ObjectTimeSeries;

/**
 * A time series that stores {@code Object} data values against instants.
 * <p>
 * The "time" key to the time-series is an instant.
 * See {@link PreciseTimeSeries} for details about the "time" represented as a {@code long}.
 * 
 * @param <T>  the instant type
 * @param <V>  the value being viewed over time
 */
public interface PreciseObjectTimeSeries<T, V>
    extends ObjectTimeSeries<T, V>, PreciseTimeSeries<T, V> {

  @Override  // override for covariant return type
  PreciseObjectTimeSeries<T, V> subSeries(T startTime, boolean includeStart, T endTime, boolean includeEnd);

  @Override  // override for covariant return type
  PreciseObjectTimeSeries<T, V> subSeries(T startTime, T endTime);

  @Override  // override for covariant return type
  PreciseObjectTimeSeries<T, V> head(int numItems);

  @Override  // override for covariant return type
  PreciseObjectTimeSeries<T, V> tail(int numItems);

  @Override  // override for covariant return type
  PreciseObjectTimeSeries<T, V> lag(final int lagCount);

}
