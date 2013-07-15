/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.timeseries.precise.zdt;

import java.util.Map;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.timeseries.precise.PreciseDoubleTimeSeries;
import com.opengamma.timeseries.precise.PreciseDoubleTimeSeriesBuilder;
import com.opengamma.timeseries.precise.PreciseTimeSeries;

/**
 * A builder of time-series that stores {@code double} data values against {@code ZonedDateTime} times.
 * <p>
 * The "time" key to the time-series is an {@code ZonedDateTime}.
 * See {@link PreciseTimeSeries} for details about the "time" represented as a {@code long}.
 */
public interface ZonedDateTimeDoubleTimeSeriesBuilder extends PreciseDoubleTimeSeriesBuilder<ZonedDateTime> {

  @Override  // override for covariant return type
  ZonedDateTimeDoubleEntryIterator iterator();

  //-------------------------------------------------------------------------
  @Override  // override for covariant return type
  ZonedDateTimeDoubleTimeSeriesBuilder put(ZonedDateTime time, double value);

  @Override  // override for covariant return type
  ZonedDateTimeDoubleTimeSeriesBuilder put(long time, double value);

  @Override  // override for covariant return type
  ZonedDateTimeDoubleTimeSeriesBuilder putAll(ZonedDateTime[] times, double[] values);

  @Override  // override for covariant return type
  ZonedDateTimeDoubleTimeSeriesBuilder putAll(long[] times, double[] values);

  //-------------------------------------------------------------------------
  @Override  // override for covariant return type
  ZonedDateTimeDoubleTimeSeriesBuilder putAll(PreciseDoubleTimeSeries<?> timeSeries);

  @Override  // override for covariant return type
  ZonedDateTimeDoubleTimeSeriesBuilder putAll(PreciseDoubleTimeSeries<?> timeSeries, int startPos, int endPos);

  @Override  // override for covariant return type
  ZonedDateTimeDoubleTimeSeriesBuilder putAll(Map<ZonedDateTime, Double> timeSeriesMap);

  //-------------------------------------------------------------------------
  @Override  // override for covariant return type
  ZonedDateTimeDoubleTimeSeriesBuilder clear();

  //-------------------------------------------------------------------------
  @Override  // override for covariant return type
  ZonedDateTimeDoubleTimeSeries build();

}
