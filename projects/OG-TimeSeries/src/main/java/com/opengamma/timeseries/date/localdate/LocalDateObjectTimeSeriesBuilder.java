/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.timeseries.date.localdate;

import java.util.Map;

import org.threeten.bp.LocalDate;

import com.opengamma.timeseries.date.DateObjectTimeSeries;
import com.opengamma.timeseries.date.DateObjectTimeSeriesBuilder;
import com.opengamma.timeseries.date.DateTimeSeries;

/**
 * A builder of time-series that stores {@code Object} data values against {@code LocalDate} dates.
 * <p>
 * The "time" key to the time-series is a {@code LocalDate}.
 * See {@link DateTimeSeries} for details about the "time" represented as an {@code int}.
 * 
 * @param <V>  the value being viewed over time
 */
public interface LocalDateObjectTimeSeriesBuilder<V> extends DateObjectTimeSeriesBuilder<LocalDate, V> {

  @Override  // override for covariant return type
  LocalDateObjectEntryIterator<V> iterator();

  //-------------------------------------------------------------------------
  @Override  // override for covariant return type
  LocalDateObjectTimeSeriesBuilder<V> put(LocalDate time, V value);

  @Override  // override for covariant return type
  LocalDateObjectTimeSeriesBuilder<V> put(int time, V value);

  @Override  // override for covariant return type
  LocalDateObjectTimeSeriesBuilder<V> putAll(LocalDate[] times, V[] values);

  @Override  // override for covariant return type
  LocalDateObjectTimeSeriesBuilder<V> putAll(int[] times, V[] values);

  //-------------------------------------------------------------------------
  @Override  // override for covariant return type
  LocalDateObjectTimeSeriesBuilder<V> putAll(DateObjectTimeSeries<?, V> timeSeries);

  @Override  // override for covariant return type
  LocalDateObjectTimeSeriesBuilder<V> putAll(DateObjectTimeSeries<?, V> timeSeries, int startPos, int endPos);

  @Override  // override for covariant return type
  LocalDateObjectTimeSeriesBuilder<V> putAll(Map<LocalDate, V> timeSeriesMap);

  //-------------------------------------------------------------------------
  @Override  // override for covariant return type
  LocalDateObjectTimeSeriesBuilder<V> clear();

  //-------------------------------------------------------------------------
  @Override  // override for covariant return type
  LocalDateObjectTimeSeries<V> build();

}
