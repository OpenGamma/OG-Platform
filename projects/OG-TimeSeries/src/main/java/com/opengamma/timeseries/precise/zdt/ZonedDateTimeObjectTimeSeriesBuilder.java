/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.timeseries.precise.zdt;

import java.util.Map;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.timeseries.precise.PreciseObjectTimeSeries;
import com.opengamma.timeseries.precise.PreciseObjectTimeSeriesBuilder;
import com.opengamma.timeseries.precise.PreciseTimeSeries;

/**
 * A builder of time-series that stores {@code Object} data values against {@code ZonedDateTime} times.
 * <p>
 * The "time" key to the time-series is an {@code ZonedDateTime}.
 * See {@link PreciseTimeSeries} for details about the "time" represented as a {@code long}.
 * 
 * @param <V>  the value being viewed over time
 */
public interface ZonedDateTimeObjectTimeSeriesBuilder<V> extends PreciseObjectTimeSeriesBuilder<ZonedDateTime, V> {

  @Override  // override for covariant return type
  ZonedDateTimeObjectEntryIterator<V> iterator();

  //-------------------------------------------------------------------------
  @Override  // override for covariant return type
  ZonedDateTimeObjectTimeSeriesBuilder<V> put(ZonedDateTime time, V value);

  @Override  // override for covariant return type
  ZonedDateTimeObjectTimeSeriesBuilder<V> put(long time, V value);

  @Override  // override for covariant return type
  ZonedDateTimeObjectTimeSeriesBuilder<V> putAll(ZonedDateTime[] times, V[] values);

  @Override  // override for covariant return type
  ZonedDateTimeObjectTimeSeriesBuilder<V> putAll(long[] times, V[] values);

  //-------------------------------------------------------------------------
  @Override  // override for covariant return type
  ZonedDateTimeObjectTimeSeriesBuilder<V> putAll(PreciseObjectTimeSeries<?, V> timeSeries);

  @Override  // override for covariant return type
  ZonedDateTimeObjectTimeSeriesBuilder<V> putAll(PreciseObjectTimeSeries<?, V> timeSeries, int startPos, int endPos);

  @Override  // override for covariant return type
  ZonedDateTimeObjectTimeSeriesBuilder<V> putAll(Map<ZonedDateTime, V> timeSeriesMap);

  //-------------------------------------------------------------------------
  @Override  // override for covariant return type
  ZonedDateTimeObjectTimeSeriesBuilder<V> clear();

  //-------------------------------------------------------------------------
  @Override  // override for covariant return type
  ZonedDateTimeObjectTimeSeries<V> build();

}
