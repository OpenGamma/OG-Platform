/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.util.timeseries.fast;

import com.opengamma.util.timeseries.FastBackedObjectTimeSeries;
import com.opengamma.util.timeseries.ObjectTimeSeries;
import com.opengamma.util.timeseries.ObjectTimeSeriesOperators.BinaryOperator;
import com.opengamma.util.timeseries.ObjectTimeSeriesOperators.UnaryOperator;
import com.opengamma.util.timeseries.fast.integer.object.FastIntObjectTimeSeries;
import com.opengamma.util.timeseries.fast.longint.object.FastLongObjectTimeSeries;

/**
 * @param <FAST_DATE_T> The type of the dates (a "fast" type - e.g., int)
 * @param <T> The type of the data
 * 
 */
public interface FastObjectTimeSeries<FAST_DATE_T, T> extends ObjectTimeSeries<FAST_DATE_T, T> {

  DateTimeNumericEncoding getEncoding();

  DateTimeResolution getDateTimeResolution();

  FastObjectTimeSeries<FAST_DATE_T, T> operate(final UnaryOperator<T> operator);

  FastObjectTimeSeries<FAST_DATE_T, T> operate(final T other, final BinaryOperator<T> operator);

  FastObjectTimeSeries<FAST_DATE_T, T> operate(final FastLongObjectTimeSeries<T> other,
      final BinaryOperator<T> operator);

  FastObjectTimeSeries<FAST_DATE_T, T> operate(final FastIntObjectTimeSeries<T> other,
      final BinaryOperator<T> operator);

  FastObjectTimeSeries<FAST_DATE_T, T> operate(final FastBackedObjectTimeSeries<?, T> other,
      final BinaryOperator<T> operator);

  FastObjectTimeSeries<FAST_DATE_T, T> unionOperate(final FastLongObjectTimeSeries<T> other,
      final BinaryOperator<T> operator);

  FastObjectTimeSeries<FAST_DATE_T, T> unionOperate(
      final FastIntObjectTimeSeries<T> other, final BinaryOperator<T> operator);

  FastObjectTimeSeries<FAST_DATE_T, T> unionOperate(
      final FastBackedObjectTimeSeries<?, T> other, final BinaryOperator<T> operator);

  FastObjectTimeSeries<FAST_DATE_T, T> intersectionFirstValue(FastBackedObjectTimeSeries<?, T> other);

  FastObjectTimeSeries<FAST_DATE_T, T> intersectionFirstValue(FastLongObjectTimeSeries<T> other);

  FastObjectTimeSeries<FAST_DATE_T, T> intersectionFirstValue(FastIntObjectTimeSeries<T> other);

  FastObjectTimeSeries<FAST_DATE_T, T> intersectionSecondValue(FastBackedObjectTimeSeries<?, T> other);

  FastObjectTimeSeries<FAST_DATE_T, T> intersectionSecondValue(FastLongObjectTimeSeries<T> other);

  FastObjectTimeSeries<FAST_DATE_T, T> intersectionSecondValue(FastIntObjectTimeSeries<T> other);
  // FastObjectTimeSeries<T> add(FastBackedDoubleTimeSeries<?>
  // other);
  //
  // FastObjectTimeSeries<T> add(FastObjectDoubleTimeSeries<T>
  // other);
  //
  // FastObjectTimeSeries<T> add(FastIntDoubleTimeSeries other);
  //
  // FastObjectTimeSeries<T> add(double other);
  //
  // FastObjectTimeSeries<T>
  // unionAdd(FastBackedDoubleTimeSeries<?> other);
  //
  // FastObjectTimeSeries<T> unionAdd(FastObjectDoubleTimeSeries<T>
  // other);
  //
  // FastObjectTimeSeries<T> unionAdd(FastIntDoubleTimeSeries
  // other);
  //
  // FastObjectTimeSeries<T>
  // subtract(FastBackedDoubleTimeSeries<?> other);
  //
  // FastObjectTimeSeries<T> subtract(FastObjectDoubleTimeSeries<T>
  // other);
  //
  // FastObjectTimeSeries<T> subtract(FastIntDoubleTimeSeries
  // other);
  //
  // FastObjectTimeSeries<T> subtract(double other);
  //
  // FastObjectTimeSeries<T> unionSubtract(
  // FastBackedDoubleTimeSeries<?> other);
  //
  // FastObjectTimeSeries<T>
  // unionSubtract(FastIntDoubleTimeSeries other);
  //
  // FastObjectTimeSeries<T>
  // unionSubtract(FastObjectDoubleTimeSeries<T> other);
  //
  // FastObjectTimeSeries<T>
  // multiply(FastBackedDoubleTimeSeries<?> other);
  //
  // FastObjectTimeSeries<T> multiply(FastObjectDoubleTimeSeries<T>
  // other);
  //
  // FastObjectTimeSeries<T> multiply(FastIntDoubleTimeSeries
  // other);
  //
  // FastObjectTimeSeries<T> multiply(double other);
  //
  // FastObjectTimeSeries<T> unionMultiply(
  // FastBackedDoubleTimeSeries<?> other);
  //
  // FastObjectTimeSeries<T>
  // unionMultiply(FastObjectDoubleTimeSeries<T> other);
  //
  // FastObjectTimeSeries<T>
  // unionMultiply(FastIntDoubleTimeSeries other);
  //
  // FastObjectTimeSeries<T>
  // divide(FastBackedDoubleTimeSeries<?> other);
  //
  // FastObjectTimeSeries<T> divide(FastObjectDoubleTimeSeries<T>
  // other);
  //
  // FastObjectTimeSeries<T> divide(FastIntDoubleTimeSeries
  // other);
  //
  // FastObjectTimeSeries<T> divide(double other);
  //
  // FastObjectTimeSeries<T> unionDivide(
  // FastBackedDoubleTimeSeries<?> other);
  //
  // FastObjectTimeSeries<T>
  // unionDivide(FastObjectDoubleTimeSeries<T> other);
  //
  // FastObjectTimeSeries<T> unionDivide(FastIntDoubleTimeSeries
  // other);
  //
  // FastObjectTimeSeries<T> power(FastBackedDoubleTimeSeries<?>
  // other);
  //
  // FastObjectTimeSeries<T> power(FastObjectDoubleTimeSeries<T>
  // other);
  //
  // FastObjectTimeSeries<T> power(FastIntDoubleTimeSeries
  // other);
  //
  // FastObjectTimeSeries<T> power(double other);
  //
  // FastObjectTimeSeries<T> unionPower(
  // FastBackedDoubleTimeSeries<?> other);
  //
  // FastObjectTimeSeries<T> unionPower(FastObjectDoubleTimeSeries<T>
  // other);
  //
  // FastObjectTimeSeries<T> unionPower(FastIntDoubleTimeSeries
  // other);
  //
  // FastObjectTimeSeries<T>
  // minimum(FastBackedDoubleTimeSeries<?> other);
  //
  // FastObjectTimeSeries<T> minimum(FastObjectDoubleTimeSeries<T>
  // other);
  //
  // FastObjectTimeSeries<T> minimum(FastIntDoubleTimeSeries
  // other);
  //
  // FastObjectTimeSeries<T> minimum(double other);
  //
  // FastObjectTimeSeries<T> unionMinimum(
  // FastBackedDoubleTimeSeries<?> other);
  //
  // FastObjectTimeSeries<T>
  // unionMinimum(FastObjectDoubleTimeSeries<T> other);
  //
  // FastObjectTimeSeries<T>
  // unionMinimum(FastIntDoubleTimeSeries other);
  //
  // FastObjectTimeSeries<T>
  // maximum(FastBackedDoubleTimeSeries<?> other);
  //
  // FastObjectTimeSeries<T> maximum(FastObjectDoubleTimeSeries<T>
  // other);
  //
  // FastObjectTimeSeries<T> maximum(FastIntDoubleTimeSeries
  // other);
  //
  // FastObjectTimeSeries<T> maximum(double other);
  //
  // FastObjectTimeSeries<T> unionMaximum(
  // FastBackedDoubleTimeSeries<?> other);
  //
  // FastObjectTimeSeries<T>
  // unionMaximum(FastObjectDoubleTimeSeries<T> other);
  //
  // FastObjectTimeSeries<T>
  // unionMaximum(FastIntDoubleTimeSeries other);
  //
  // FastObjectTimeSeries<T>
  // average(FastBackedDoubleTimeSeries<?> other);
  //
  // FastObjectTimeSeries<T> average(FastIntDoubleTimeSeries
  // other);
  //
  // FastObjectTimeSeries<T> average(FastObjectDoubleTimeSeries<T>
  // other);
  //
  // FastObjectTimeSeries<T> average(double other);
  //
  // FastObjectTimeSeries<T> unionAverage(
  // FastBackedDoubleTimeSeries<?> other);
  //
  // FastObjectTimeSeries<T>
  // unionAverage(FastObjectDoubleTimeSeries<T> other);
  //
  // FastObjectTimeSeries<T>
  // unionAverage(FastIntDoubleTimeSeries other);
  //

  //
  // FastObjectTimeSeries<T> negate();
  //
  // FastObjectTimeSeries<T> reciprocal();
  //
  // FastObjectTimeSeries<T> log();
  //
  // FastObjectTimeSeries<T> log10();
  //
  // FastObjectTimeSeries<T> abs();

  // double[] valuesArrayFast();

  // double maxValue();
  //
  // double minValue();
}
