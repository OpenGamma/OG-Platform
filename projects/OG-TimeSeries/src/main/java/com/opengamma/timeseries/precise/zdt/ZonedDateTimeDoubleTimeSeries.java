/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.timeseries.precise.zdt;

import org.threeten.bp.ZoneId;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.timeseries.DoubleTimeSeries;
import com.opengamma.timeseries.DoubleTimeSeriesOperators.BinaryOperator;
import com.opengamma.timeseries.DoubleTimeSeriesOperators.UnaryOperator;
import com.opengamma.timeseries.precise.PreciseDoubleTimeSeries;
import com.opengamma.timeseries.precise.PreciseTimeSeries;

/**
 * A time series that stores {@code double} data values against {@code ZonedDateTime} times.
 * <p>
 * The "time" key to the time-series is an {@code ZonedDateTime}.
 * See {@link PreciseTimeSeries} for details about the "time" represented as a {@code long}.
 */
public interface ZonedDateTimeDoubleTimeSeries
    extends PreciseDoubleTimeSeries<ZonedDateTime> {

  /**
   * Gets the applicable time-zone.
   * 
   * @return the time-zone, not null
   */
  ZoneId getZone();

  /**
   * Returns this time-series with a different time-zone.
   * 
   * @param zone  the time-zone, not null
   * @return the same time-series with the specified zone, not null
   */
  ZonedDateTimeDoubleTimeSeries withZone(ZoneId zone);

  //-------------------------------------------------------------------------
  /**
   * Gets an iterator over the instant-value pairs.
   * <p>
   * Although the pairs are expressed as instances of {@code Map.Entry},
   * it is recommended to use the primitive methods on {@code ZonedDateTimeDoubleIterator}.
   * 
   * @return the iterator, not null
   */
  @Override  // override for covariant return type
  ZonedDateTimeDoubleEntryIterator iterator();

  //-------------------------------------------------------------------------
  @Override  // override for covariant return type
  ZonedDateTimeDoubleTimeSeries subSeries(ZonedDateTime startTime, ZonedDateTime endTime);

  @Override  // override for covariant return type
  ZonedDateTimeDoubleTimeSeries subSeries(ZonedDateTime startTime, boolean includeStart, ZonedDateTime endTime, boolean includeEnd);

  @Override  // override for covariant return type
  ZonedDateTimeDoubleTimeSeries subSeriesFast(long startTime, long endTime);

  @Override  // override for covariant return type
  ZonedDateTimeDoubleTimeSeries subSeriesFast(long startTime, boolean includeStart, long endTime, boolean includeEnd);

  @Override  // override for covariant return type
  ZonedDateTimeDoubleTimeSeries head(int numItems);

  @Override  // override for covariant return type
  ZonedDateTimeDoubleTimeSeries tail(int numItems);

  @Override  // override for covariant return type
  ZonedDateTimeDoubleTimeSeries lag(int lagCount);

  //-------------------------------------------------------------------------
  @Override  // override for covariant return type
  ZonedDateTimeDoubleTimeSeries operate(UnaryOperator operator);

  @Override  // override for covariant return type
  ZonedDateTimeDoubleTimeSeries operate(double other, BinaryOperator operator);

  @Override  // override for covariant return type
  ZonedDateTimeDoubleTimeSeries operate(PreciseDoubleTimeSeries<?> otherTimeSeries, BinaryOperator operator);

  @Override  // override for covariant return type
  ZonedDateTimeDoubleTimeSeries unionOperate(PreciseDoubleTimeSeries<?> otherTimeSeries, BinaryOperator operator);

  //-------------------------------------------------------------------------
  @Override  // override for covariant return type
  ZonedDateTimeDoubleTimeSeries add(double amountToAdd);

  @Override  // override for covariant return type
  ZonedDateTimeDoubleTimeSeries add(DoubleTimeSeries<?> other);

  @Override  // override for covariant return type
  ZonedDateTimeDoubleTimeSeries unionAdd(DoubleTimeSeries<?> other);

  //-------------------------------------------------------------------------
  @Override  // override for covariant return type
  ZonedDateTimeDoubleTimeSeries subtract(double amountToSubtract);

  @Override  // override for covariant return type
  ZonedDateTimeDoubleTimeSeries subtract(DoubleTimeSeries<?> other);

  @Override  // override for covariant return type
  ZonedDateTimeDoubleTimeSeries unionSubtract(DoubleTimeSeries<?> other);

  //-------------------------------------------------------------------------
  @Override  // override for covariant return type
  ZonedDateTimeDoubleTimeSeries multiply(double amountToMultiplyBy);

  @Override  // override for covariant return type
  ZonedDateTimeDoubleTimeSeries multiply(DoubleTimeSeries<?> other);

  @Override  // override for covariant return type
  ZonedDateTimeDoubleTimeSeries unionMultiply(DoubleTimeSeries<?> other);

  //-------------------------------------------------------------------------
  @Override  // override for covariant return type
  ZonedDateTimeDoubleTimeSeries divide(double amountToDivideBy);

  @Override  // override for covariant return type
  ZonedDateTimeDoubleTimeSeries divide(DoubleTimeSeries<?> other);

  @Override  // override for covariant return type
  ZonedDateTimeDoubleTimeSeries unionDivide(DoubleTimeSeries<?> other);

  //-------------------------------------------------------------------------
  @Override  // override for covariant return type
  ZonedDateTimeDoubleTimeSeries power(double power);

  @Override  // override for covariant return type
  ZonedDateTimeDoubleTimeSeries power(DoubleTimeSeries<?> other);

  @Override  // override for covariant return type
  ZonedDateTimeDoubleTimeSeries unionPower(DoubleTimeSeries<?> other);

  //-------------------------------------------------------------------------
  @Override  // override for covariant return type
  ZonedDateTimeDoubleTimeSeries minimum(double minValue);

  @Override  // override for covariant return type
  ZonedDateTimeDoubleTimeSeries minimum(DoubleTimeSeries<?> other);

  @Override  // override for covariant return type
  ZonedDateTimeDoubleTimeSeries unionMinimum(DoubleTimeSeries<?> other);

  //-------------------------------------------------------------------------
  @Override  // override for covariant return type
  ZonedDateTimeDoubleTimeSeries maximum(double maxValue);

  @Override  // override for covariant return type
  ZonedDateTimeDoubleTimeSeries maximum(DoubleTimeSeries<?> other);

  @Override  // override for covariant return type
  ZonedDateTimeDoubleTimeSeries unionMaximum(DoubleTimeSeries<?> other);

  //-------------------------------------------------------------------------
  @Override  // override for covariant return type
  ZonedDateTimeDoubleTimeSeries average(double value);

  @Override  // override for covariant return type
  ZonedDateTimeDoubleTimeSeries average(DoubleTimeSeries<?> other);

  @Override  // override for covariant return type
  ZonedDateTimeDoubleTimeSeries unionAverage(DoubleTimeSeries<?> other);

  //-------------------------------------------------------------------------
  @Override  // override for covariant return type
  ZonedDateTimeDoubleTimeSeries intersectionFirstValue(DoubleTimeSeries<?> other);

  @Override  // override for covariant return type
  ZonedDateTimeDoubleTimeSeries intersectionSecondValue(DoubleTimeSeries<?> other);

  @Override  // override for covariant return type
  ZonedDateTimeDoubleTimeSeries noIntersectionOperation(DoubleTimeSeries<?> other);

  //-------------------------------------------------------------------------
  @Override  // override for covariant return type
  ZonedDateTimeDoubleTimeSeries negate();

  @Override  // override for covariant return type
  ZonedDateTimeDoubleTimeSeries reciprocal();

  @Override  // override for covariant return type
  ZonedDateTimeDoubleTimeSeries log();

  @Override  // override for covariant return type
  ZonedDateTimeDoubleTimeSeries log10();

  @Override  // override for covariant return type
  ZonedDateTimeDoubleTimeSeries abs();

  //-------------------------------------------------------------------------
  @Override  // override for covariant return type
  ZonedDateTimeDoubleTimeSeriesBuilder toBuilder();

}
