/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.timeseries;

import com.opengamma.timeseries.DoubleTimeSeriesOperators.BinaryOperator;
import com.opengamma.timeseries.DoubleTimeSeriesOperators.UnaryOperator;
import com.opengamma.timeseries.fast.DateTimeNumericEncoding;
import com.opengamma.timeseries.fast.FastTimeSeries;
import com.opengamma.timeseries.fast.integer.FastIntDoubleTimeSeries;
import com.opengamma.timeseries.fast.integer.FastMutableIntDoubleTimeSeries;
import com.opengamma.timeseries.fast.longint.FastLongDoubleTimeSeries;
import com.opengamma.timeseries.fast.longint.FastMutableLongDoubleTimeSeries;

/**
 * @param <T> the type of dates for the time series
 */
public interface FastBackedDoubleTimeSeries<T> extends DoubleTimeSeries<T> {

  DateTimeConverter<T> getConverter();

  FastTimeSeries<?> getFastSeries();

  @Override
  FastBackedDoubleTimeSeries<T> lag(int days);

  //-------------------------------------------------------------------------
  FastBackedDoubleTimeSeries<T> operate(FastTimeSeries<?> other, BinaryOperator operator);

  FastBackedDoubleTimeSeries<T> operate(FastBackedDoubleTimeSeries<?> other, BinaryOperator operator);

  FastBackedDoubleTimeSeries<T> operate(double other, BinaryOperator operator);

  FastBackedDoubleTimeSeries<T> operate(UnaryOperator operator);

  FastBackedDoubleTimeSeries<T> unionOperate(FastTimeSeries<?> other, BinaryOperator operator);

  FastBackedDoubleTimeSeries<T> unionOperate(FastBackedDoubleTimeSeries<?> other, BinaryOperator operator);

  //-------------------------------------------------------------------------
  @Override
  FastBackedDoubleTimeSeries<T> add(double other);

  @Override
  FastBackedDoubleTimeSeries<T> subtract(double other);

  @Override
  FastBackedDoubleTimeSeries<T> multiply(double other);

  @Override
  FastBackedDoubleTimeSeries<T> divide(double other);

  @Override
  FastBackedDoubleTimeSeries<T> power(double other);

  @Override
  FastBackedDoubleTimeSeries<T> minimum(double other);

  @Override
  FastBackedDoubleTimeSeries<T> maximum(double other);

  @Override
  FastBackedDoubleTimeSeries<T> average(double other);

  //-------------------------------------------------------------------------
  @Override
  FastBackedDoubleTimeSeries<T> negate();

  @Override
  FastBackedDoubleTimeSeries<T> reciprocal();

  @Override
  FastBackedDoubleTimeSeries<T> log();

  @Override
  FastBackedDoubleTimeSeries<T> log10();

  @Override
  FastBackedDoubleTimeSeries<T> abs();

  @Override
  double maxValue();

  @Override
  double minValue();

  //-------------------------------------------------------------------------
  FastIntDoubleTimeSeries toFastIntDoubleTimeSeries();

  FastIntDoubleTimeSeries toFastIntDoubleTimeSeries(DateTimeNumericEncoding encoding);

  FastLongDoubleTimeSeries toFastLongDoubleTimeSeries();

  FastLongDoubleTimeSeries toFastLongDoubleTimeSeries(DateTimeNumericEncoding encoding);

  FastMutableIntDoubleTimeSeries toFastMutableIntDoubleTimeSeries();

  FastMutableIntDoubleTimeSeries toFastMutableIntDoubleTimeSeries(DateTimeNumericEncoding encoding);

  FastMutableLongDoubleTimeSeries toFastMutableLongDoubleTimeSeries();

  FastMutableLongDoubleTimeSeries toFastMutableLongDoubleTimeSeries(DateTimeNumericEncoding encoding);

}
