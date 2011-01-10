/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.util.timeseries.fast;

import com.opengamma.util.timeseries.DoubleTimeSeries;
import com.opengamma.util.timeseries.FastBackedDoubleTimeSeries;
import com.opengamma.util.timeseries.DoubleTimeSeriesOperators.BinaryOperator;
import com.opengamma.util.timeseries.DoubleTimeSeriesOperators.UnaryOperator;
import com.opengamma.util.timeseries.fast.integer.FastIntDoubleTimeSeries;
import com.opengamma.util.timeseries.fast.longint.FastLongDoubleTimeSeries;

/**
 * @param <T>
 *
 * @author jim
 */
public interface FastTimeSeries<T> extends DoubleTimeSeries<T> {

  public abstract DateTimeNumericEncoding getEncoding();

  public abstract DateTimeResolution getDateTimeResolution();

  public abstract FastTimeSeries<T> operate(final UnaryOperator operator);

  public abstract FastTimeSeries<T> operate(final double other,
      final BinaryOperator operator);

  public abstract FastTimeSeries<T> operate(
      final FastLongDoubleTimeSeries other, final BinaryOperator operator);

  public abstract FastTimeSeries<T> operate(
      final FastIntDoubleTimeSeries other, final BinaryOperator operator);

  public abstract FastTimeSeries<T> operate(
      final FastBackedDoubleTimeSeries<?> other, final BinaryOperator operator);

  public abstract FastTimeSeries<T> unionOperate(
      final FastLongDoubleTimeSeries other, final BinaryOperator operator);

  public abstract FastTimeSeries<T> unionOperate(
      final FastIntDoubleTimeSeries other, final BinaryOperator operator);

  public abstract FastTimeSeries<T> unionOperate(
      final FastBackedDoubleTimeSeries<?> other, final BinaryOperator operator);

  public abstract FastTimeSeries<T> add(FastBackedDoubleTimeSeries<?> other);

  public abstract FastTimeSeries<T> add(FastLongDoubleTimeSeries other);

  public abstract FastTimeSeries<T> add(FastIntDoubleTimeSeries other);

  public abstract FastTimeSeries<T> add(double other);

  public abstract FastTimeSeries<T> unionAdd(FastBackedDoubleTimeSeries<?> other);

  public abstract FastTimeSeries<T> unionAdd(FastLongDoubleTimeSeries other);

  public abstract FastTimeSeries<T> unionAdd(FastIntDoubleTimeSeries other);

  public abstract FastTimeSeries<T> subtract(FastBackedDoubleTimeSeries<?> other);

  public abstract FastTimeSeries<T> subtract(FastLongDoubleTimeSeries other);

  public abstract FastTimeSeries<T> subtract(FastIntDoubleTimeSeries other);

  public abstract FastTimeSeries<T> subtract(double other);

  public abstract FastTimeSeries<T> unionSubtract(
      FastBackedDoubleTimeSeries<?> other);

  public abstract FastTimeSeries<T> unionSubtract(FastIntDoubleTimeSeries other);

  public abstract FastTimeSeries<T> unionSubtract(FastLongDoubleTimeSeries other);

  public abstract FastTimeSeries<T> multiply(FastBackedDoubleTimeSeries<?> other);

  public abstract FastTimeSeries<T> multiply(FastLongDoubleTimeSeries other);

  public abstract FastTimeSeries<T> multiply(FastIntDoubleTimeSeries other);

  public abstract FastTimeSeries<T> multiply(double other);

  public abstract FastTimeSeries<T> unionMultiply(
      FastBackedDoubleTimeSeries<?> other);

  public abstract FastTimeSeries<T> unionMultiply(FastLongDoubleTimeSeries other);

  public abstract FastTimeSeries<T> unionMultiply(FastIntDoubleTimeSeries other);

  public abstract FastTimeSeries<T> divide(FastBackedDoubleTimeSeries<?> other);

  public abstract FastTimeSeries<T> divide(FastLongDoubleTimeSeries other);

  public abstract FastTimeSeries<T> divide(FastIntDoubleTimeSeries other);

  public abstract FastTimeSeries<T> divide(double other);

  public abstract FastTimeSeries<T> unionDivide(
      FastBackedDoubleTimeSeries<?> other);

  public abstract FastTimeSeries<T> unionDivide(FastLongDoubleTimeSeries other);

  public abstract FastTimeSeries<T> unionDivide(FastIntDoubleTimeSeries other);

  public abstract FastTimeSeries<T> power(FastBackedDoubleTimeSeries<?> other);

  public abstract FastTimeSeries<T> power(FastLongDoubleTimeSeries other);

  public abstract FastTimeSeries<T> power(FastIntDoubleTimeSeries other);

  public abstract FastTimeSeries<T> power(double other);

  public abstract FastTimeSeries<T> unionPower(
      FastBackedDoubleTimeSeries<?> other);

  public abstract FastTimeSeries<T> unionPower(FastLongDoubleTimeSeries other);

  public abstract FastTimeSeries<T> unionPower(FastIntDoubleTimeSeries other);

  public abstract FastTimeSeries<T> minimum(FastBackedDoubleTimeSeries<?> other);

  public abstract FastTimeSeries<T> minimum(FastLongDoubleTimeSeries other);

  public abstract FastTimeSeries<T> minimum(FastIntDoubleTimeSeries other);

  public abstract FastTimeSeries<T> minimum(double other);

  public abstract FastTimeSeries<T> unionMinimum(
      FastBackedDoubleTimeSeries<?> other);

  public abstract FastTimeSeries<T> unionMinimum(FastLongDoubleTimeSeries other);

  public abstract FastTimeSeries<T> unionMinimum(FastIntDoubleTimeSeries other);

  public abstract FastTimeSeries<T> maximum(FastBackedDoubleTimeSeries<?> other);

  public abstract FastTimeSeries<T> maximum(FastLongDoubleTimeSeries other);

  public abstract FastTimeSeries<T> maximum(FastIntDoubleTimeSeries other);

  public abstract FastTimeSeries<T> maximum(double other);

  public abstract FastTimeSeries<T> unionMaximum(
      FastBackedDoubleTimeSeries<?> other);

  public abstract FastTimeSeries<T> unionMaximum(FastLongDoubleTimeSeries other);

  public abstract FastTimeSeries<T> unionMaximum(FastIntDoubleTimeSeries other);

  public abstract FastTimeSeries<T> average(FastBackedDoubleTimeSeries<?> other);

  public abstract FastTimeSeries<T> average(FastIntDoubleTimeSeries other);

  public abstract FastTimeSeries<T> average(FastLongDoubleTimeSeries other);

  public abstract FastTimeSeries<T> average(double other);

  public abstract FastTimeSeries<T> unionAverage(
      FastBackedDoubleTimeSeries<?> other);

  public abstract FastTimeSeries<T> unionAverage(FastLongDoubleTimeSeries other);

  public abstract FastTimeSeries<T> unionAverage(FastIntDoubleTimeSeries other);

  public abstract FastTimeSeries<T> intersectionFirstValue(
      FastBackedDoubleTimeSeries<?> other);

  public abstract FastTimeSeries<T> intersectionFirstValue(
      FastLongDoubleTimeSeries other);

  public abstract FastTimeSeries<T> intersectionFirstValue(
      FastIntDoubleTimeSeries other);

  public abstract FastTimeSeries<T> intersectionSecondValue(
      FastBackedDoubleTimeSeries<?> other);

  public abstract FastTimeSeries<T> intersectionSecondValue(
      FastLongDoubleTimeSeries other);

  public abstract FastTimeSeries<T> intersectionSecondValue(
      FastIntDoubleTimeSeries other);

  public abstract FastTimeSeries<T> negate();

  public abstract FastTimeSeries<T> reciprocal();

  public abstract FastTimeSeries<T> log();

  public abstract FastTimeSeries<T> log10();

  public abstract FastTimeSeries<T> abs();

  public abstract double[] valuesArrayFast();

  public abstract double maxValue();

  public abstract double minValue();
}
