/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.timeseries.date;

import com.opengamma.timeseries.ObjectTimeSeries;

/**
 * A time series that stores {@code Object} data values against dates.
 * <p>
 * The "time" key to the time-series is a date.
 * See {@link DateTimeSeries} for details about the "time" represented as an {@code int}.
 * 
 * @param <T>  the date type
 * @param <V>  the value being viewed over time
 */
public interface DateObjectTimeSeries<T, V>
    extends ObjectTimeSeries<T, V>, DateTimeSeries<T, V> {

  @Override  // override for covariant return type
  DateObjectTimeSeries<T, V> subSeries(T startTime, T endTime);

  @Override  // override for covariant return type
  DateObjectTimeSeries<T, V> subSeries(T startTime, boolean includeStart, T endTime, boolean includeEnd);

  @Override  // override for covariant return type
  DateObjectTimeSeries<T, V> head(int numItems);

  @Override  // override for covariant return type
  DateObjectTimeSeries<T, V> tail(int numItems);

  @Override  // override for covariant return type
  DateObjectTimeSeries<T, V> lag(final int lagCount);

}
