/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.util.timeseries;

import com.opengamma.util.timeseries.ObjectTimeSeriesOperators.BinaryOperator;
import com.opengamma.util.timeseries.ObjectTimeSeriesOperators.UnaryOperator;
import com.opengamma.util.timeseries.fast.DateTimeNumericEncoding;
import com.opengamma.util.timeseries.fast.FastObjectTimeSeries;
import com.opengamma.util.timeseries.fast.FastTimeSeries;
import com.opengamma.util.timeseries.fast.integer.FastIntDoubleTimeSeries;
import com.opengamma.util.timeseries.fast.integer.FastMutableIntDoubleTimeSeries;
import com.opengamma.util.timeseries.fast.longint.FastLongDoubleTimeSeries;
import com.opengamma.util.timeseries.fast.longint.FastMutableLongDoubleTimeSeries;

/**
 * @param <DATE_TYPE, T>
 *
 * @author jim
 */
public interface FastBackedObjectTimeSeries<DATE_TYPE, T> extends ObjectTimeSeries<DATE_TYPE, T> {

  public abstract DateTimeConverter<DATE_TYPE> getConverter();

  public abstract FastObjectTimeSeries<?, T> getFastSeries();

  public abstract FastBackedObjectTimeSeries<DATE_TYPE, T> operate(
      final FastTimeSeries<?> other, final BinaryOperator<T> operator);

  public abstract FastBackedObjectTimeSeries<DATE_TYPE, T> operate(
      final FastBackedObjectTimeSeries<?, T> other, final BinaryOperator<T> operator);

  public abstract FastBackedObjectTimeSeries<DATE_TYPE, T> operate(
      final double other, final BinaryOperator<T> operator);

  public abstract FastBackedObjectTimeSeries<DATE_TYPE, T> operate(
      final UnaryOperator<T> operator);

  public abstract FastBackedObjectTimeSeries<DATE_TYPE, T> unionOperate(
      final FastTimeSeries<?> other, final BinaryOperator<T> operator);

  public abstract FastBackedObjectTimeSeries<DATE_TYPE, T> unionOperate(
      final FastBackedObjectTimeSeries<?, T> other, final BinaryOperator<T> operator);

//  public abstract FastBackedObjectTimeSeries<DATE_TYPE, T> add(
//      FastBackedObjectTimeSeries<?, T> other);
//
//  public abstract FastBackedObjectTimeSeries<DATE_TYPE, T> add(
//      FastIntDoubleTimeSeries other);
//
//  public abstract FastBackedObjectTimeSeries<DATE_TYPE, T> add(
//      FastLongDoubleTimeSeries other);
//  
//  public abstract FastBackedObjectTimeSeries<DATE_TYPE, T> add(double other);
//
//  public abstract FastBackedObjectTimeSeries<DATE_TYPE, T> unionAdd(
//      FastBackedObjectTimeSeries<?, T> other);
//
//  public abstract FastBackedObjectTimeSeries<DATE_TYPE, T> unionAdd(
//      FastIntDoubleTimeSeries other);
//
//  public abstract FastBackedObjectTimeSeries<DATE_TYPE, T> unionAdd(
//      FastLongDoubleTimeSeries other);
//
//  public abstract FastBackedObjectTimeSeries<DATE_TYPE, T> subtract(
//      FastBackedObjectTimeSeries<?, T> other);
//
//  public abstract FastBackedObjectTimeSeries<DATE_TYPE, T> subtract(
//      FastLongDoubleTimeSeries other);
//
//  public abstract FastBackedObjectTimeSeries<DATE_TYPE, T> subtract(
//      FastIntDoubleTimeSeries other);
//
//  public abstract FastBackedObjectTimeSeries<DATE_TYPE, T> subtract(double other);
//
//  public abstract FastBackedObjectTimeSeries<DATE_TYPE, T> unionSubtract(
//      FastBackedObjectTimeSeries<?, T> other);
//
//  public abstract FastBackedObjectTimeSeries<DATE_TYPE, T> unionSubtract(
//      FastIntDoubleTimeSeries other);
//
//  public abstract FastBackedObjectTimeSeries<DATE_TYPE, T> unionSubtract(
//      FastLongDoubleTimeSeries other);
//
//  public abstract FastBackedObjectTimeSeries<DATE_TYPE, T> multiply(
//      FastBackedObjectTimeSeries<?, T> other);
//
//  public abstract FastBackedObjectTimeSeries<DATE_TYPE, T> multiply(
//      FastIntDoubleTimeSeries other);
//
//  public abstract FastBackedObjectTimeSeries<DATE_TYPE, T> multiply(
//      FastLongDoubleTimeSeries other);
//
//  public abstract FastBackedObjectTimeSeries<DATE_TYPE, T> multiply(double other);
//
//  public abstract FastBackedObjectTimeSeries<DATE_TYPE, T> unionMultiply(
//      FastBackedObjectTimeSeries<?, T> other);
//
//  public abstract FastBackedObjectTimeSeries<DATE_TYPE, T> unionMultiply(
//      FastIntDoubleTimeSeries other);
//
//  public abstract FastBackedObjectTimeSeries<DATE_TYPE, T> unionMultiply(
//      FastLongDoubleTimeSeries other);
//
//  public abstract FastBackedObjectTimeSeries<DATE_TYPE, T> divide(
//      FastBackedObjectTimeSeries<?, T> other);
//
//  public abstract FastBackedObjectTimeSeries<DATE_TYPE, T> divide(
//      FastIntDoubleTimeSeries other);
//
//  public abstract FastBackedObjectTimeSeries<DATE_TYPE, T> divide(
//      FastLongDoubleTimeSeries other);
//
//  public abstract FastBackedObjectTimeSeries<DATE_TYPE, T> divide(double other);
//
//  public abstract FastBackedObjectTimeSeries<DATE_TYPE, T> unionDivide(
//      FastBackedObjectTimeSeries<?, T> other);
//
//  public abstract FastBackedObjectTimeSeries<DATE_TYPE, T> unionDivide(
//      FastIntDoubleTimeSeries other);
//
//  public abstract FastBackedObjectTimeSeries<DATE_TYPE, T> unionDivide(
//      FastLongDoubleTimeSeries other);
//
//  public abstract FastBackedObjectTimeSeries<DATE_TYPE, T> power(
//      FastBackedObjectTimeSeries<?, T> other);
//
//  public abstract FastBackedObjectTimeSeries<DATE_TYPE, T> power(
//      FastIntDoubleTimeSeries other);
//
//  public abstract FastBackedObjectTimeSeries<DATE_TYPE, T> power(
//      FastLongDoubleTimeSeries other);
//
//  public abstract FastBackedObjectTimeSeries<DATE_TYPE, T> power(double other);
//
//  public abstract FastBackedObjectTimeSeries<DATE_TYPE, T> unionPower(
//      FastBackedObjectTimeSeries<?, T> other);
//
//  public abstract FastBackedObjectTimeSeries<DATE_TYPE, T> unionPower(
//      FastIntDoubleTimeSeries other);
//
//  public abstract FastBackedObjectTimeSeries<DATE_TYPE, T> unionPower(
//      FastLongDoubleTimeSeries other);
//
//  public abstract FastBackedObjectTimeSeries<DATE_TYPE, T> minimum(
//      FastBackedObjectTimeSeries<?, T> other);
//
//  public abstract FastBackedObjectTimeSeries<DATE_TYPE, T> minimum(
//      FastIntDoubleTimeSeries other);
//
//  public abstract FastBackedObjectTimeSeries<DATE_TYPE, T> minimum(
//      FastLongDoubleTimeSeries other);
//
//  public abstract FastBackedObjectTimeSeries<DATE_TYPE, T> minimum(double other);
//
//  public abstract FastBackedObjectTimeSeries<DATE_TYPE, T> unionMinimum(
//      FastBackedObjectTimeSeries<?, T> other);
//
//  public abstract FastBackedObjectTimeSeries<DATE_TYPE, T> unionMinimum(
//      FastIntDoubleTimeSeries other);
//
//  public abstract FastBackedObjectTimeSeries<DATE_TYPE, T> unionMinimum(
//      FastLongDoubleTimeSeries other);
//
//  public abstract FastBackedObjectTimeSeries<DATE_TYPE, T> maximum(
//      FastBackedObjectTimeSeries<?, T> other);
//
//  public abstract FastBackedObjectTimeSeries<DATE_TYPE, T> maximum(
//      FastIntDoubleTimeSeries other);
//
//  public abstract FastBackedObjectTimeSeries<DATE_TYPE, T> maximum(
//      FastLongDoubleTimeSeries other);
//
//  public abstract FastBackedObjectTimeSeries<DATE_TYPE, T> maximum(double other);
//
//  public abstract FastBackedObjectTimeSeries<DATE_TYPE, T> unionMaximum(
//      FastBackedObjectTimeSeries<?, T> other);
//
//  public abstract FastBackedObjectTimeSeries<DATE_TYPE, T> unionMaximum(
//      FastIntDoubleTimeSeries other);
//
//  public abstract FastBackedObjectTimeSeries<DATE_TYPE, T> unionMaximum(
//      FastLongDoubleTimeSeries other);
//
//  public abstract FastBackedObjectTimeSeries<DATE_TYPE, T> average(
//      FastBackedObjectTimeSeries<?, T> other);
//
//  public abstract FastBackedObjectTimeSeries<DATE_TYPE, T> average(
//      FastIntDoubleTimeSeries other);
//
//  public abstract FastBackedObjectTimeSeries<DATE_TYPE, T> average(
//      FastLongDoubleTimeSeries other);
//
//  public abstract FastBackedObjectTimeSeries<DATE_TYPE, T> average(double other);
//
//  public abstract FastBackedObjectTimeSeries<DATE_TYPE, T> unionAverage(
//      FastBackedObjectTimeSeries<?, T> other);
//
//  public abstract FastBackedObjectTimeSeries<DATE_TYPE, T> unionAverage(
//      FastIntDoubleTimeSeries other);
//
//  public abstract FastBackedObjectTimeSeries<DATE_TYPE, T> unionAverage(
//      FastLongDoubleTimeSeries other);

  public abstract FastBackedObjectTimeSeries<DATE_TYPE, T> intersectionFirstValue(
      FastBackedObjectTimeSeries<?, T> other);

  public abstract FastBackedObjectTimeSeries<DATE_TYPE, T> intersectionFirstValue(
      FastIntDoubleTimeSeries other);

  public abstract FastBackedObjectTimeSeries<DATE_TYPE, T> intersectionFirstValue(
      FastLongDoubleTimeSeries other);

  public abstract FastBackedObjectTimeSeries<DATE_TYPE, T> intersectionSecondValue(
      FastBackedObjectTimeSeries<?, T> other);

  public abstract FastBackedObjectTimeSeries<DATE_TYPE, T> intersectionSecondValue(
      FastIntDoubleTimeSeries other);

  public abstract FastBackedObjectTimeSeries<DATE_TYPE, T> intersectionSecondValue(
      FastLongDoubleTimeSeries other);

//  public abstract FastBackedObjectTimeSeries<DATE_TYPE, T> negate();
//
//  public abstract FastBackedObjectTimeSeries<DATE_TYPE, T> reciprocal();
//
//  public abstract FastBackedObjectTimeSeries<DATE_TYPE, T> log();
//
//  public abstract FastBackedObjectTimeSeries<DATE_TYPE, T> log10();
//
//  public abstract FastBackedObjectTimeSeries<DATE_TYPE, T> abs();
//
//  public abstract double maxValue();
//
//  public abstract double minValue();

  public abstract FastBackedObjectTimeSeries<DATE_TYPE, T> lag(final int days);

  public abstract FastIntDoubleTimeSeries toFastIntDoubleTimeSeries();

  public abstract FastIntDoubleTimeSeries toFastIntDoubleTimeSeries(
      DateTimeNumericEncoding encoding);

  public abstract FastLongDoubleTimeSeries toFastLongDoubleTimeSeries();

  public abstract FastLongDoubleTimeSeries toFastLongDoubleTimeSeries(
      DateTimeNumericEncoding encoding);

  public abstract FastMutableIntDoubleTimeSeries toFastMutableIntDoubleTimeSeries();

  public abstract FastMutableIntDoubleTimeSeries toFastMutableIntDoubleTimeSeries(
      DateTimeNumericEncoding encoding);

  public abstract FastMutableLongDoubleTimeSeries toFastMutableLongDoubleTimeSeries();

  public abstract FastMutableLongDoubleTimeSeries toFastMutableLongDoubleTimeSeries(
      DateTimeNumericEncoding encoding);
  
}
