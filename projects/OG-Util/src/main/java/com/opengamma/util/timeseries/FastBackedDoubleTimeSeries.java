/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.util.timeseries;

import com.opengamma.util.timeseries.DoubleTimeSeriesOperators.BinaryOperator;
import com.opengamma.util.timeseries.DoubleTimeSeriesOperators.UnaryOperator;
import com.opengamma.util.timeseries.fast.DateTimeNumericEncoding;
import com.opengamma.util.timeseries.fast.FastTimeSeries;
import com.opengamma.util.timeseries.fast.integer.FastIntDoubleTimeSeries;
import com.opengamma.util.timeseries.fast.integer.FastMutableIntDoubleTimeSeries;
import com.opengamma.util.timeseries.fast.longint.FastLongDoubleTimeSeries;
import com.opengamma.util.timeseries.fast.longint.FastMutableLongDoubleTimeSeries;

/**
 * @param <DATE_TYPE> The type of dates for the time series
 */
public interface FastBackedDoubleTimeSeries<DATE_TYPE> extends DoubleTimeSeries<DATE_TYPE> {

  DateTimeConverter<DATE_TYPE> getConverter();
  
  FastTimeSeries<?> getFastSeries();
  
  FastBackedDoubleTimeSeries<DATE_TYPE> operate(FastTimeSeries<?> other, BinaryOperator operator);
  
  FastBackedDoubleTimeSeries<DATE_TYPE> operate(FastBackedDoubleTimeSeries<?> other, BinaryOperator operator);
  
  FastBackedDoubleTimeSeries<DATE_TYPE> operate(double other, BinaryOperator operator);
  
  FastBackedDoubleTimeSeries<DATE_TYPE> operate(UnaryOperator operator);
  
  FastBackedDoubleTimeSeries<DATE_TYPE> unionOperate(FastTimeSeries<?> other, BinaryOperator operator);
  
  FastBackedDoubleTimeSeries<DATE_TYPE> unionOperate(FastBackedDoubleTimeSeries<?> other, BinaryOperator operator);
  
  FastBackedDoubleTimeSeries<DATE_TYPE> add(FastBackedDoubleTimeSeries<?> other);
  
  FastBackedDoubleTimeSeries<DATE_TYPE> add(FastIntDoubleTimeSeries other);
  
  FastBackedDoubleTimeSeries<DATE_TYPE> add(FastLongDoubleTimeSeries other);
  
  FastBackedDoubleTimeSeries<DATE_TYPE> add(double other);
  
  FastBackedDoubleTimeSeries<DATE_TYPE> unionAdd(FastBackedDoubleTimeSeries<?> other);
  
  FastBackedDoubleTimeSeries<DATE_TYPE> unionAdd(FastIntDoubleTimeSeries other);
  
  FastBackedDoubleTimeSeries<DATE_TYPE> unionAdd(FastLongDoubleTimeSeries other);
  
  FastBackedDoubleTimeSeries<DATE_TYPE> subtract(FastBackedDoubleTimeSeries<?> other);
  
  FastBackedDoubleTimeSeries<DATE_TYPE> subtract(FastLongDoubleTimeSeries other);
  
  FastBackedDoubleTimeSeries<DATE_TYPE> subtract(FastIntDoubleTimeSeries other);
  
  FastBackedDoubleTimeSeries<DATE_TYPE> subtract(double other);
  
  FastBackedDoubleTimeSeries<DATE_TYPE> unionSubtract(FastBackedDoubleTimeSeries<?> other);
  
  FastBackedDoubleTimeSeries<DATE_TYPE> unionSubtract(FastIntDoubleTimeSeries other);
  
  FastBackedDoubleTimeSeries<DATE_TYPE> unionSubtract(FastLongDoubleTimeSeries other);
  
  FastBackedDoubleTimeSeries<DATE_TYPE> multiply(FastBackedDoubleTimeSeries<?> other);
  
  FastBackedDoubleTimeSeries<DATE_TYPE> multiply(FastIntDoubleTimeSeries other);
  
  FastBackedDoubleTimeSeries<DATE_TYPE> multiply(FastLongDoubleTimeSeries other);
  
  FastBackedDoubleTimeSeries<DATE_TYPE> multiply(double other);
  
  FastBackedDoubleTimeSeries<DATE_TYPE> unionMultiply(FastBackedDoubleTimeSeries<?> other);
  
  FastBackedDoubleTimeSeries<DATE_TYPE> unionMultiply(FastIntDoubleTimeSeries other);
  
  FastBackedDoubleTimeSeries<DATE_TYPE> unionMultiply(FastLongDoubleTimeSeries other);
  
  FastBackedDoubleTimeSeries<DATE_TYPE> divide(FastBackedDoubleTimeSeries<?> other);
  
  FastBackedDoubleTimeSeries<DATE_TYPE> divide(FastIntDoubleTimeSeries other);
  
  FastBackedDoubleTimeSeries<DATE_TYPE> divide(FastLongDoubleTimeSeries other);
  
  FastBackedDoubleTimeSeries<DATE_TYPE> divide(double other);
  
  FastBackedDoubleTimeSeries<DATE_TYPE> unionDivide(FastBackedDoubleTimeSeries<?> other);
  
  FastBackedDoubleTimeSeries<DATE_TYPE> unionDivide(FastIntDoubleTimeSeries other);
  
  FastBackedDoubleTimeSeries<DATE_TYPE> unionDivide(FastLongDoubleTimeSeries other);
  
  FastBackedDoubleTimeSeries<DATE_TYPE> power(FastBackedDoubleTimeSeries<?> other);
  
  FastBackedDoubleTimeSeries<DATE_TYPE> power(FastIntDoubleTimeSeries other);
  
  FastBackedDoubleTimeSeries<DATE_TYPE> power(FastLongDoubleTimeSeries other);
  
  FastBackedDoubleTimeSeries<DATE_TYPE> power(double other);
  
  FastBackedDoubleTimeSeries<DATE_TYPE> unionPower(FastBackedDoubleTimeSeries<?> other);
  
  FastBackedDoubleTimeSeries<DATE_TYPE> unionPower(FastIntDoubleTimeSeries other);
  
  FastBackedDoubleTimeSeries<DATE_TYPE> unionPower(FastLongDoubleTimeSeries other);
  
  FastBackedDoubleTimeSeries<DATE_TYPE> minimum(FastBackedDoubleTimeSeries<?> other);
  
  FastBackedDoubleTimeSeries<DATE_TYPE> minimum(FastIntDoubleTimeSeries other);
  
  FastBackedDoubleTimeSeries<DATE_TYPE> minimum(FastLongDoubleTimeSeries other);
  
  FastBackedDoubleTimeSeries<DATE_TYPE> minimum(double other);
  
  FastBackedDoubleTimeSeries<DATE_TYPE> unionMinimum(FastBackedDoubleTimeSeries<?> other);
  
  FastBackedDoubleTimeSeries<DATE_TYPE> unionMinimum(FastIntDoubleTimeSeries other);
  
  FastBackedDoubleTimeSeries<DATE_TYPE> unionMinimum(FastLongDoubleTimeSeries other);
  
  FastBackedDoubleTimeSeries<DATE_TYPE> maximum(FastBackedDoubleTimeSeries<?> other);
  
  FastBackedDoubleTimeSeries<DATE_TYPE> maximum(FastIntDoubleTimeSeries other);
  
  FastBackedDoubleTimeSeries<DATE_TYPE> maximum(FastLongDoubleTimeSeries other);
  
  FastBackedDoubleTimeSeries<DATE_TYPE> maximum(double other);
  
  FastBackedDoubleTimeSeries<DATE_TYPE> unionMaximum(FastBackedDoubleTimeSeries<?> other);
  
  FastBackedDoubleTimeSeries<DATE_TYPE> unionMaximum(FastIntDoubleTimeSeries other);
  
  FastBackedDoubleTimeSeries<DATE_TYPE> unionMaximum(FastLongDoubleTimeSeries other);
  
  FastBackedDoubleTimeSeries<DATE_TYPE> average(FastBackedDoubleTimeSeries<?> other);
  
  FastBackedDoubleTimeSeries<DATE_TYPE> average(FastIntDoubleTimeSeries other);
  
  FastBackedDoubleTimeSeries<DATE_TYPE> average(FastLongDoubleTimeSeries other);
  
  FastBackedDoubleTimeSeries<DATE_TYPE> average(double other);
  
  FastBackedDoubleTimeSeries<DATE_TYPE> unionAverage(FastBackedDoubleTimeSeries<?> other);
  
  FastBackedDoubleTimeSeries<DATE_TYPE> unionAverage(FastIntDoubleTimeSeries other);
  
  FastBackedDoubleTimeSeries<DATE_TYPE> unionAverage(FastLongDoubleTimeSeries other);
  
  FastBackedDoubleTimeSeries<DATE_TYPE> noIntersectionOperation(FastBackedDoubleTimeSeries<?> other);
  
  FastBackedDoubleTimeSeries<DATE_TYPE> noIntersectionOperation(FastIntDoubleTimeSeries other);
  
  FastBackedDoubleTimeSeries<DATE_TYPE> noIntersectionOperation(FastLongDoubleTimeSeries other);
  
  FastBackedDoubleTimeSeries<DATE_TYPE> intersectionFirstValue(FastBackedDoubleTimeSeries<?> other);
  
  FastBackedDoubleTimeSeries<DATE_TYPE> intersectionFirstValue(FastIntDoubleTimeSeries other);
  
  FastBackedDoubleTimeSeries<DATE_TYPE> intersectionFirstValue(FastLongDoubleTimeSeries other);
  
  FastBackedDoubleTimeSeries<DATE_TYPE> intersectionSecondValue(FastBackedDoubleTimeSeries<?> other);
  
  FastBackedDoubleTimeSeries<DATE_TYPE> intersectionSecondValue(FastIntDoubleTimeSeries other);
   
  FastBackedDoubleTimeSeries<DATE_TYPE> intersectionSecondValue(FastLongDoubleTimeSeries other);
  
  FastBackedDoubleTimeSeries<DATE_TYPE> negate();
  
  FastBackedDoubleTimeSeries<DATE_TYPE> reciprocal();
  
  FastBackedDoubleTimeSeries<DATE_TYPE> log();
  
  FastBackedDoubleTimeSeries<DATE_TYPE> log10();
  
  FastBackedDoubleTimeSeries<DATE_TYPE> abs();
  
  double maxValue();
  
  double minValue();
  
  FastBackedDoubleTimeSeries<DATE_TYPE> lag(final int days);
  
  FastIntDoubleTimeSeries toFastIntDoubleTimeSeries();
  
  FastIntDoubleTimeSeries toFastIntDoubleTimeSeries(DateTimeNumericEncoding encoding);
  
  FastLongDoubleTimeSeries toFastLongDoubleTimeSeries();
  
  FastLongDoubleTimeSeries toFastLongDoubleTimeSeries(DateTimeNumericEncoding encoding);
  
  FastMutableIntDoubleTimeSeries toFastMutableIntDoubleTimeSeries();
  
  FastMutableIntDoubleTimeSeries toFastMutableIntDoubleTimeSeries(DateTimeNumericEncoding encoding);
  
  FastMutableLongDoubleTimeSeries toFastMutableLongDoubleTimeSeries();
  
  FastMutableLongDoubleTimeSeries toFastMutableLongDoubleTimeSeries(DateTimeNumericEncoding encoding);

}
