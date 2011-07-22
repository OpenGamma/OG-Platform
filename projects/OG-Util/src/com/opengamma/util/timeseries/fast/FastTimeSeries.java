/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
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
 * @param <T> The type of the dates
 *
 */
public interface FastTimeSeries<T> extends DoubleTimeSeries<T> {

  DateTimeNumericEncoding getEncoding();

  DateTimeResolution getDateTimeResolution();

  FastTimeSeries<T> operate(final UnaryOperator operator);

  FastTimeSeries<T> operate(final double other, final BinaryOperator operator);

  FastTimeSeries<T> operate(final FastLongDoubleTimeSeries other, final BinaryOperator operator);

  FastTimeSeries<T> operate(final FastIntDoubleTimeSeries other, final BinaryOperator operator);

  FastTimeSeries<T> operate(final FastBackedDoubleTimeSeries<?> other, final BinaryOperator operator);

  FastTimeSeries<T> unionOperate(final FastLongDoubleTimeSeries other, final BinaryOperator operator);

  FastTimeSeries<T> unionOperate(final FastIntDoubleTimeSeries other, final BinaryOperator operator);

  FastTimeSeries<T> unionOperate(final FastBackedDoubleTimeSeries<?> other, final BinaryOperator operator);

  FastTimeSeries<T> add(FastBackedDoubleTimeSeries<?> other);

  FastTimeSeries<T> add(FastLongDoubleTimeSeries other);

  FastTimeSeries<T> add(FastIntDoubleTimeSeries other);

  FastTimeSeries<T> add(double other);

  FastTimeSeries<T> unionAdd(FastBackedDoubleTimeSeries<?> other);

  FastTimeSeries<T> unionAdd(FastLongDoubleTimeSeries other);

  FastTimeSeries<T> unionAdd(FastIntDoubleTimeSeries other);

  FastTimeSeries<T> subtract(FastBackedDoubleTimeSeries<?> other);

  FastTimeSeries<T> subtract(FastLongDoubleTimeSeries other);

  FastTimeSeries<T> subtract(FastIntDoubleTimeSeries other);

  FastTimeSeries<T> subtract(double other);

  FastTimeSeries<T> unionSubtract(FastBackedDoubleTimeSeries<?> other);

  FastTimeSeries<T> unionSubtract(FastIntDoubleTimeSeries other);

  FastTimeSeries<T> unionSubtract(FastLongDoubleTimeSeries other);

  FastTimeSeries<T> multiply(FastBackedDoubleTimeSeries<?> other);

  FastTimeSeries<T> multiply(FastLongDoubleTimeSeries other);

  FastTimeSeries<T> multiply(FastIntDoubleTimeSeries other);

  FastTimeSeries<T> multiply(double other);

  FastTimeSeries<T> unionMultiply(FastBackedDoubleTimeSeries<?> other);

  FastTimeSeries<T> unionMultiply(FastLongDoubleTimeSeries other);

  FastTimeSeries<T> unionMultiply(FastIntDoubleTimeSeries other);

  FastTimeSeries<T> divide(FastBackedDoubleTimeSeries<?> other);

  FastTimeSeries<T> divide(FastLongDoubleTimeSeries other);

  FastTimeSeries<T> divide(FastIntDoubleTimeSeries other);

  FastTimeSeries<T> divide(double other);

  FastTimeSeries<T> unionDivide(FastBackedDoubleTimeSeries<?> other);

  FastTimeSeries<T> unionDivide(FastLongDoubleTimeSeries other);

  FastTimeSeries<T> unionDivide(FastIntDoubleTimeSeries other);

  FastTimeSeries<T> power(FastBackedDoubleTimeSeries<?> other);

  FastTimeSeries<T> power(FastLongDoubleTimeSeries other);

  FastTimeSeries<T> power(FastIntDoubleTimeSeries other);

  FastTimeSeries<T> power(double other);

  FastTimeSeries<T> unionPower(FastBackedDoubleTimeSeries<?> other);

  FastTimeSeries<T> unionPower(FastLongDoubleTimeSeries other);

  FastTimeSeries<T> unionPower(FastIntDoubleTimeSeries other);

  FastTimeSeries<T> minimum(FastBackedDoubleTimeSeries<?> other);

  FastTimeSeries<T> minimum(FastLongDoubleTimeSeries other);

  FastTimeSeries<T> minimum(FastIntDoubleTimeSeries other);

  FastTimeSeries<T> minimum(double other);

  FastTimeSeries<T> unionMinimum(FastBackedDoubleTimeSeries<?> other);

  FastTimeSeries<T> unionMinimum(FastLongDoubleTimeSeries other);

  FastTimeSeries<T> unionMinimum(FastIntDoubleTimeSeries other);

  FastTimeSeries<T> maximum(FastBackedDoubleTimeSeries<?> other);

  FastTimeSeries<T> maximum(FastLongDoubleTimeSeries other);

  FastTimeSeries<T> maximum(FastIntDoubleTimeSeries other);

  FastTimeSeries<T> maximum(double other);

  FastTimeSeries<T> unionMaximum(FastBackedDoubleTimeSeries<?> other);

  FastTimeSeries<T> unionMaximum(FastLongDoubleTimeSeries other);

  FastTimeSeries<T> unionMaximum(FastIntDoubleTimeSeries other);

  FastTimeSeries<T> average(FastBackedDoubleTimeSeries<?> other);

  FastTimeSeries<T> average(FastIntDoubleTimeSeries other);

  FastTimeSeries<T> average(FastLongDoubleTimeSeries other);

  FastTimeSeries<T> average(double other);

  FastTimeSeries<T> unionAverage(FastBackedDoubleTimeSeries<?> other);

  FastTimeSeries<T> unionAverage(FastLongDoubleTimeSeries other);

  FastTimeSeries<T> unionAverage(FastIntDoubleTimeSeries other);

  FastTimeSeries<T> intersectionFirstValue(FastBackedDoubleTimeSeries<?> other);

  FastTimeSeries<T> intersectionFirstValue(FastLongDoubleTimeSeries other);

  FastTimeSeries<T> intersectionFirstValue(FastIntDoubleTimeSeries other);

  FastTimeSeries<T> intersectionSecondValue(FastBackedDoubleTimeSeries<?> other);

  FastTimeSeries<T> intersectionSecondValue(FastLongDoubleTimeSeries other);

  FastTimeSeries<T> intersectionSecondValue(FastIntDoubleTimeSeries other);

  FastTimeSeries<T> negate();

  FastTimeSeries<T> reciprocal();

  FastTimeSeries<T> log();

  FastTimeSeries<T> log10();

  FastTimeSeries<T> abs();

  double maxValue();

  double minValue();
}
