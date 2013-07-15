/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.timeseries.precise.instant;

import java.util.Map;

import org.threeten.bp.Instant;

import com.opengamma.timeseries.precise.PreciseDoubleTimeSeries;
import com.opengamma.timeseries.precise.PreciseDoubleTimeSeriesBuilder;
import com.opengamma.timeseries.precise.PreciseTimeSeries;

/**
 * A builder of time-series that stores {@code double} data values against {@code Instant} times.
 * <p>
 * The "time" key to the time-series is an {@code Instant}.
 * See {@link PreciseTimeSeries} for details about the "time" represented as a {@code long}.
 */
public interface InstantDoubleTimeSeriesBuilder extends PreciseDoubleTimeSeriesBuilder<Instant> {

  @Override  // override for covariant return type
  InstantDoubleEntryIterator iterator();

  //-------------------------------------------------------------------------
  @Override  // override for covariant return type
  InstantDoubleTimeSeriesBuilder put(Instant time, double value);

  @Override  // override for covariant return type
  InstantDoubleTimeSeriesBuilder put(long time, double value);

  @Override  // override for covariant return type
  InstantDoubleTimeSeriesBuilder putAll(Instant[] times, double[] values);

  @Override  // override for covariant return type
  InstantDoubleTimeSeriesBuilder putAll(long[] times, double[] values);

  //-------------------------------------------------------------------------
  @Override  // override for covariant return type
  InstantDoubleTimeSeriesBuilder putAll(PreciseDoubleTimeSeries<?> timeSeries);

  @Override  // override for covariant return type
  InstantDoubleTimeSeriesBuilder putAll(PreciseDoubleTimeSeries<?> timeSeries, int startPos, int endPos);

  @Override  // override for covariant return type
  InstantDoubleTimeSeriesBuilder putAll(Map<Instant, Double> timeSeriesMap);

  //-------------------------------------------------------------------------
  @Override  // override for covariant return type
  InstantDoubleTimeSeriesBuilder clear();

  //-------------------------------------------------------------------------
  @Override  // override for covariant return type
  InstantDoubleTimeSeries build();

}
