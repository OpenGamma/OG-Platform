/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.timeseries.precise.instant;

import org.threeten.bp.Instant;

import com.opengamma.timeseries.DoubleTimeSeries;
import com.opengamma.timeseries.DoubleTimeSeriesOperators.BinaryOperator;
import com.opengamma.timeseries.DoubleTimeSeriesOperators.UnaryOperator;
import com.opengamma.timeseries.precise.PreciseDoubleTimeSeries;
import com.opengamma.timeseries.precise.PreciseTimeSeries;

/**
 * A time series that stores {@code double} data values against {@code Instant} times.
 * <p>
 * The "time" key to the time-series is an {@code Instant}.
 * See {@link PreciseTimeSeries} for details about the "time" represented as a {@code long}.
 */
public interface InstantDoubleTimeSeries
    extends PreciseDoubleTimeSeries<Instant> {

  /**
   * Gets an iterator over the instant-value pairs.
   * <p>
   * Although the pairs are expressed as instances of {@code Map.Entry},
   * it is recommended to use the primitive methods on {@code InstantDoubleIterator}.
   * 
   * @return the iterator, not null
   */
  @Override  // override for covariant return type
  InstantDoubleEntryIterator iterator();

  //-------------------------------------------------------------------------
  @Override  // override for covariant return type
  InstantDoubleTimeSeries subSeries(Instant startTime, Instant endTime);

  @Override  // override for covariant return type
  InstantDoubleTimeSeries subSeries(Instant startTime, boolean includeStart, Instant endTime, boolean includeEnd);

  @Override  // override for covariant return type
  InstantDoubleTimeSeries subSeriesFast(long startTime, long endTime);

  @Override  // override for covariant return type
  InstantDoubleTimeSeries subSeriesFast(long startTime, boolean includeStart, long endTime, boolean includeEnd);

  @Override  // override for covariant return type
  InstantDoubleTimeSeries head(int numItems);

  @Override  // override for covariant return type
  InstantDoubleTimeSeries tail(int numItems);

  @Override  // override for covariant return type
  InstantDoubleTimeSeries lag(int lagCount);

  //-------------------------------------------------------------------------
  @Override  // override for covariant return type
  InstantDoubleTimeSeries operate(UnaryOperator operator);

  @Override  // override for covariant return type
  InstantDoubleTimeSeries operate(double other, BinaryOperator operator);

  @Override  // override for covariant return type
  InstantDoubleTimeSeries operate(PreciseDoubleTimeSeries<?> otherTimeSeries, BinaryOperator operator);

  @Override  // override for covariant return type
  InstantDoubleTimeSeries unionOperate(PreciseDoubleTimeSeries<?> otherTimeSeries, BinaryOperator operator);

  //-------------------------------------------------------------------------
  @Override  // override for covariant return type
  InstantDoubleTimeSeries add(double amountToAdd);

  @Override  // override for covariant return type
  InstantDoubleTimeSeries add(DoubleTimeSeries<?> other);

  @Override  // override for covariant return type
  InstantDoubleTimeSeries unionAdd(DoubleTimeSeries<?> other);

  //-------------------------------------------------------------------------
  @Override  // override for covariant return type
  InstantDoubleTimeSeries subtract(double amountToSubtract);

  @Override  // override for covariant return type
  InstantDoubleTimeSeries subtract(DoubleTimeSeries<?> other);

  @Override  // override for covariant return type
  InstantDoubleTimeSeries unionSubtract(DoubleTimeSeries<?> other);

  //-------------------------------------------------------------------------
  @Override  // override for covariant return type
  InstantDoubleTimeSeries multiply(double amountToMultiplyBy);

  @Override  // override for covariant return type
  InstantDoubleTimeSeries multiply(DoubleTimeSeries<?> other);

  @Override  // override for covariant return type
  InstantDoubleTimeSeries unionMultiply(DoubleTimeSeries<?> other);

  //-------------------------------------------------------------------------
  @Override  // override for covariant return type
  InstantDoubleTimeSeries divide(double amountToDivideBy);

  @Override  // override for covariant return type
  InstantDoubleTimeSeries divide(DoubleTimeSeries<?> other);

  @Override  // override for covariant return type
  InstantDoubleTimeSeries unionDivide(DoubleTimeSeries<?> other);

  //-------------------------------------------------------------------------
  @Override  // override for covariant return type
  InstantDoubleTimeSeries power(double power);

  @Override  // override for covariant return type
  InstantDoubleTimeSeries power(DoubleTimeSeries<?> other);

  @Override  // override for covariant return type
  InstantDoubleTimeSeries unionPower(DoubleTimeSeries<?> other);

  //-------------------------------------------------------------------------
  @Override  // override for covariant return type
  InstantDoubleTimeSeries minimum(double minValue);

  @Override  // override for covariant return type
  InstantDoubleTimeSeries minimum(DoubleTimeSeries<?> other);

  @Override  // override for covariant return type
  InstantDoubleTimeSeries unionMinimum(DoubleTimeSeries<?> other);

  //-------------------------------------------------------------------------
  @Override  // override for covariant return type
  InstantDoubleTimeSeries maximum(double maxValue);

  @Override  // override for covariant return type
  InstantDoubleTimeSeries maximum(DoubleTimeSeries<?> other);

  @Override  // override for covariant return type
  InstantDoubleTimeSeries unionMaximum(DoubleTimeSeries<?> other);

  //-------------------------------------------------------------------------
  @Override  // override for covariant return type
  InstantDoubleTimeSeries average(double value);

  @Override  // override for covariant return type
  InstantDoubleTimeSeries average(DoubleTimeSeries<?> other);

  @Override  // override for covariant return type
  InstantDoubleTimeSeries unionAverage(DoubleTimeSeries<?> other);

  //-------------------------------------------------------------------------
  @Override  // override for covariant return type
  InstantDoubleTimeSeries intersectionFirstValue(DoubleTimeSeries<?> other);

  @Override  // override for covariant return type
  InstantDoubleTimeSeries intersectionSecondValue(DoubleTimeSeries<?> other);

  @Override  // override for covariant return type
  InstantDoubleTimeSeries noIntersectionOperation(DoubleTimeSeries<?> other);

  //-------------------------------------------------------------------------
  @Override  // override for covariant return type
  InstantDoubleTimeSeries negate();

  @Override  // override for covariant return type
  InstantDoubleTimeSeries reciprocal();

  @Override  // override for covariant return type
  InstantDoubleTimeSeries log();

  @Override  // override for covariant return type
  InstantDoubleTimeSeries log10();

  @Override  // override for covariant return type
  InstantDoubleTimeSeries abs();

  //-------------------------------------------------------------------------
  @Override  // override for covariant return type
  InstantDoubleTimeSeriesBuilder toBuilder();

}
