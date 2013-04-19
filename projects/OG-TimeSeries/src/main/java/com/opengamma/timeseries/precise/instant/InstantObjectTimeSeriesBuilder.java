/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.timeseries.precise.instant;

import java.util.Map;

import org.threeten.bp.Instant;

import com.opengamma.timeseries.precise.PreciseObjectTimeSeries;
import com.opengamma.timeseries.precise.PreciseObjectTimeSeriesBuilder;
import com.opengamma.timeseries.precise.PreciseTimeSeries;

/**
 * A builder of time-series that stores {@code Object} data values against {@code Instant} times.
 * <p>
 * The "time" key to the time-series is an {@code Instant}.
 * See {@link PreciseTimeSeries} for details about the "time" represented as a {@code long}.
 * 
 * @param <V>  the value being viewed over time
 */
public interface InstantObjectTimeSeriesBuilder<V> extends PreciseObjectTimeSeriesBuilder<Instant, V> {

  @Override  // override for covariant return type
  InstantObjectEntryIterator<V> iterator();

  //-------------------------------------------------------------------------
  @Override  // override for covariant return type
  InstantObjectTimeSeriesBuilder<V> put(Instant time, V value);

  @Override  // override for covariant return type
  InstantObjectTimeSeriesBuilder<V> put(long time, V value);

  @Override  // override for covariant return type
  InstantObjectTimeSeriesBuilder<V> putAll(Instant[] times, V[] values);

  @Override  // override for covariant return type
  InstantObjectTimeSeriesBuilder<V> putAll(long[] times, V[] values);

  //-------------------------------------------------------------------------
  @Override  // override for covariant return type
  InstantObjectTimeSeriesBuilder<V> putAll(PreciseObjectTimeSeries<?, V> timeSeries);

  @Override  // override for covariant return type
  InstantObjectTimeSeriesBuilder<V> putAll(PreciseObjectTimeSeries<?, V> timeSeries, int startPos, int endPos);

  @Override  // override for covariant return type
  InstantObjectTimeSeriesBuilder<V> putAll(Map<Instant, V> timeSeriesMap);

  //-------------------------------------------------------------------------
  @Override  // override for covariant return type
  InstantObjectTimeSeriesBuilder<V> clear();

  //-------------------------------------------------------------------------
  @Override  // override for covariant return type
  InstantObjectTimeSeries<V> build();

}
