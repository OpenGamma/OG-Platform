/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
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
 * @param <FAST_DATE_T, T>
 * 
 * @author jim
 */
public interface FastObjectTimeSeries<FAST_DATE_T, T> extends ObjectTimeSeries<FAST_DATE_T, T> {

  public abstract DateTimeNumericEncoding getEncoding();

  public abstract DateTimeResolution getDateTimeResolution();

  public abstract FastObjectTimeSeries<FAST_DATE_T, T> operate(final UnaryOperator<T> operator);

  public abstract FastObjectTimeSeries<FAST_DATE_T, T> operate(final T other,
      final BinaryOperator<T> operator);

  public abstract FastObjectTimeSeries<FAST_DATE_T, T> operate(final FastLongObjectTimeSeries<T> other, final BinaryOperator<T> operator);

  public abstract FastObjectTimeSeries<FAST_DATE_T, T> operate(final FastIntObjectTimeSeries<T> other, final BinaryOperator<T> operator);

  public abstract FastObjectTimeSeries<FAST_DATE_T, T> operate(final FastBackedObjectTimeSeries<?, T> other, final BinaryOperator<T> operator);

  public abstract FastObjectTimeSeries<FAST_DATE_T, T> unionOperate(
      final FastLongObjectTimeSeries<T> other, final BinaryOperator<T> operator);

  public abstract FastObjectTimeSeries<FAST_DATE_T, T> unionOperate(
      final FastIntObjectTimeSeries<T> other, final BinaryOperator<T> operator);

  public abstract FastObjectTimeSeries<FAST_DATE_T, T> unionOperate(
      final FastBackedObjectTimeSeries<?, T> other, final BinaryOperator<T> operator);

  
  public abstract FastObjectTimeSeries<FAST_DATE_T, T> intersectionFirstValue(FastBackedObjectTimeSeries<?, T> other);
  
  public abstract FastObjectTimeSeries<FAST_DATE_T, T> intersectionFirstValue(FastLongObjectTimeSeries<T> other);
 
  public abstract FastObjectTimeSeries<FAST_DATE_T, T> intersectionFirstValue(FastIntObjectTimeSeries<T> other);
 
  public abstract FastObjectTimeSeries<FAST_DATE_T, T> intersectionSecondValue(FastBackedObjectTimeSeries<?, T> other);
 
  public abstract FastObjectTimeSeries<FAST_DATE_T, T> intersectionSecondValue(FastLongObjectTimeSeries<T> other);
 
  public abstract FastObjectTimeSeries<FAST_DATE_T, T> intersectionSecondValue(FastIntObjectTimeSeries<T> other);
  // public abstract FastObjectTimeSeries<T> add(FastBackedDoubleTimeSeries<?>
  // other);
  //
  // public abstract FastObjectTimeSeries<T> add(FastObjectDoubleTimeSeries<T>
  // other);
  //
  // public abstract FastObjectTimeSeries<T> add(FastIntDoubleTimeSeries other);
  //
  // public abstract FastObjectTimeSeries<T> add(double other);
  //
  // public abstract FastObjectTimeSeries<T>
  // unionAdd(FastBackedDoubleTimeSeries<?> other);
  //
  // public abstract FastObjectTimeSeries<T> unionAdd(FastObjectDoubleTimeSeries<T>
  // other);
  //
  // public abstract FastObjectTimeSeries<T> unionAdd(FastIntDoubleTimeSeries
  // other);
  //
  // public abstract FastObjectTimeSeries<T>
  // subtract(FastBackedDoubleTimeSeries<?> other);
  //
  // public abstract FastObjectTimeSeries<T> subtract(FastObjectDoubleTimeSeries<T>
  // other);
  //
  // public abstract FastObjectTimeSeries<T> subtract(FastIntDoubleTimeSeries
  // other);
  //
  // public abstract FastObjectTimeSeries<T> subtract(double other);
  //
  // public abstract FastObjectTimeSeries<T> unionSubtract(
  // FastBackedDoubleTimeSeries<?> other);
  //
  // public abstract FastObjectTimeSeries<T>
  // unionSubtract(FastIntDoubleTimeSeries other);
  //
  // public abstract FastObjectTimeSeries<T>
  // unionSubtract(FastObjectDoubleTimeSeries<T> other);
  //
  // public abstract FastObjectTimeSeries<T>
  // multiply(FastBackedDoubleTimeSeries<?> other);
  //
  // public abstract FastObjectTimeSeries<T> multiply(FastObjectDoubleTimeSeries<T>
  // other);
  //
  // public abstract FastObjectTimeSeries<T> multiply(FastIntDoubleTimeSeries
  // other);
  //
  // public abstract FastObjectTimeSeries<T> multiply(double other);
  //
  // public abstract FastObjectTimeSeries<T> unionMultiply(
  // FastBackedDoubleTimeSeries<?> other);
  //
  // public abstract FastObjectTimeSeries<T>
  // unionMultiply(FastObjectDoubleTimeSeries<T> other);
  //
  // public abstract FastObjectTimeSeries<T>
  // unionMultiply(FastIntDoubleTimeSeries other);
  //
  // public abstract FastObjectTimeSeries<T>
  // divide(FastBackedDoubleTimeSeries<?> other);
  //
  // public abstract FastObjectTimeSeries<T> divide(FastObjectDoubleTimeSeries<T>
  // other);
  //
  // public abstract FastObjectTimeSeries<T> divide(FastIntDoubleTimeSeries
  // other);
  //
  // public abstract FastObjectTimeSeries<T> divide(double other);
  //
  // public abstract FastObjectTimeSeries<T> unionDivide(
  // FastBackedDoubleTimeSeries<?> other);
  //
  // public abstract FastObjectTimeSeries<T>
  // unionDivide(FastObjectDoubleTimeSeries<T> other);
  //
  // public abstract FastObjectTimeSeries<T> unionDivide(FastIntDoubleTimeSeries
  // other);
  //
  // public abstract FastObjectTimeSeries<T> power(FastBackedDoubleTimeSeries<?>
  // other);
  //
  // public abstract FastObjectTimeSeries<T> power(FastObjectDoubleTimeSeries<T>
  // other);
  //
  // public abstract FastObjectTimeSeries<T> power(FastIntDoubleTimeSeries
  // other);
  //
  // public abstract FastObjectTimeSeries<T> power(double other);
  //
  // public abstract FastObjectTimeSeries<T> unionPower(
  // FastBackedDoubleTimeSeries<?> other);
  //
  // public abstract FastObjectTimeSeries<T> unionPower(FastObjectDoubleTimeSeries<T>
  // other);
  //
  // public abstract FastObjectTimeSeries<T> unionPower(FastIntDoubleTimeSeries
  // other);
  //
  // public abstract FastObjectTimeSeries<T>
  // minimum(FastBackedDoubleTimeSeries<?> other);
  //
  // public abstract FastObjectTimeSeries<T> minimum(FastObjectDoubleTimeSeries<T>
  // other);
  //
  // public abstract FastObjectTimeSeries<T> minimum(FastIntDoubleTimeSeries
  // other);
  //
  // public abstract FastObjectTimeSeries<T> minimum(double other);
  //
  // public abstract FastObjectTimeSeries<T> unionMinimum(
  // FastBackedDoubleTimeSeries<?> other);
  //
  // public abstract FastObjectTimeSeries<T>
  // unionMinimum(FastObjectDoubleTimeSeries<T> other);
  //
  // public abstract FastObjectTimeSeries<T>
  // unionMinimum(FastIntDoubleTimeSeries other);
  //
  // public abstract FastObjectTimeSeries<T>
  // maximum(FastBackedDoubleTimeSeries<?> other);
  //
  // public abstract FastObjectTimeSeries<T> maximum(FastObjectDoubleTimeSeries<T>
  // other);
  //
  // public abstract FastObjectTimeSeries<T> maximum(FastIntDoubleTimeSeries
  // other);
  //
  // public abstract FastObjectTimeSeries<T> maximum(double other);
  //
  // public abstract FastObjectTimeSeries<T> unionMaximum(
  // FastBackedDoubleTimeSeries<?> other);
  //
  // public abstract FastObjectTimeSeries<T>
  // unionMaximum(FastObjectDoubleTimeSeries<T> other);
  //
  // public abstract FastObjectTimeSeries<T>
  // unionMaximum(FastIntDoubleTimeSeries other);
  //
  // public abstract FastObjectTimeSeries<T>
  // average(FastBackedDoubleTimeSeries<?> other);
  //
  // public abstract FastObjectTimeSeries<T> average(FastIntDoubleTimeSeries
  // other);
  //
  // public abstract FastObjectTimeSeries<T> average(FastObjectDoubleTimeSeries<T>
  // other);
  //
  // public abstract FastObjectTimeSeries<T> average(double other);
  //
  // public abstract FastObjectTimeSeries<T> unionAverage(
  // FastBackedDoubleTimeSeries<?> other);
  //
  // public abstract FastObjectTimeSeries<T>
  // unionAverage(FastObjectDoubleTimeSeries<T> other);
  //
  // public abstract FastObjectTimeSeries<T>
  // unionAverage(FastIntDoubleTimeSeries other);
  //

  //
  // public abstract FastObjectTimeSeries<T> negate();
  //
  // public abstract FastObjectTimeSeries<T> reciprocal();
  //
  // public abstract FastObjectTimeSeries<T> log();
  //
  // public abstract FastObjectTimeSeries<T> log10();
  //
  // public abstract FastObjectTimeSeries<T> abs();

  // public abstract double[] valuesArrayFast();

  // public abstract double maxValue();
  //
  // public abstract double minValue();
}
