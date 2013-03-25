/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.timeseries.fast;

import com.opengamma.timeseries.DoubleTimeSeries;
import com.opengamma.timeseries.FastBackedDoubleTimeSeries;
import com.opengamma.timeseries.DoubleTimeSeriesOperators.BinaryOperator;
import com.opengamma.timeseries.DoubleTimeSeriesOperators.UnaryOperator;
import com.opengamma.timeseries.fast.integer.FastIntDoubleTimeSeries;
import com.opengamma.timeseries.fast.longint.FastLongDoubleTimeSeries;

/**
 * @param <T> The type of the dates
 *
 */
public interface FastTimeSeries<T> extends DoubleTimeSeries<T> {

  DateTimeNumericEncoding getEncoding();

  DateTimeResolution getDateTimeResolution();

  @Override
  FastTimeSeries<T> lag(int days);

  //-------------------------------------------------------------------------
  FastTimeSeries<T> operate(final UnaryOperator operator);

  FastTimeSeries<T> operate(final double other, final BinaryOperator operator);

  FastTimeSeries<T> operate(final FastLongDoubleTimeSeries other, final BinaryOperator operator);

  FastTimeSeries<T> operate(final FastIntDoubleTimeSeries other, final BinaryOperator operator);

  FastTimeSeries<T> operate(final FastBackedDoubleTimeSeries<?> other, final BinaryOperator operator);

  FastTimeSeries<T> unionOperate(final FastLongDoubleTimeSeries other, final BinaryOperator operator);

  FastTimeSeries<T> unionOperate(final FastIntDoubleTimeSeries other, final BinaryOperator operator);

  FastTimeSeries<T> unionOperate(final FastBackedDoubleTimeSeries<?> other, final BinaryOperator operator);

  //-------------------------------------------------------------------------
  @Override
  FastTimeSeries<T> add(double other);

  @Override
  FastTimeSeries<T> subtract(double other);

  @Override
  FastTimeSeries<T> multiply(double other);

  @Override
  FastTimeSeries<T> divide(double other);

  @Override
  FastTimeSeries<T> power(double other);

  @Override
  FastTimeSeries<T> minimum(double other);

  @Override
  FastTimeSeries<T> maximum(double other);

  @Override
  FastTimeSeries<T> average(double other);

  //-------------------------------------------------------------------------
  @Override
  FastTimeSeries<T> negate();

  @Override
  FastTimeSeries<T> reciprocal();

  @Override
  FastTimeSeries<T> log();

  @Override
  FastTimeSeries<T> log10();

  @Override
  FastTimeSeries<T> abs();

  @Override
  double maxValue();

  @Override
  double minValue();

}
