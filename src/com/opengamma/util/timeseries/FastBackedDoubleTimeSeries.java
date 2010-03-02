/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
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
 * @param <DATE_TYPE>
 *
 * @author jim
 */
public interface FastBackedDoubleTimeSeries<DATE_TYPE> {

  public abstract DateTimeConverter<DATE_TYPE> getConverter();

  public abstract FastTimeSeries<?> getFastSeries();

  public abstract FastBackedDoubleTimeSeries<DATE_TYPE> operate(
      final FastTimeSeries<?> other, final BinaryOperator operator);

  public abstract FastBackedDoubleTimeSeries<DATE_TYPE> operate(
      final FastBackedDoubleTimeSeries<?> other,
      final BinaryOperator operator);

  public abstract FastBackedDoubleTimeSeries<DATE_TYPE> operate(
      final double other, final BinaryOperator operator);

  public abstract FastBackedDoubleTimeSeries<DATE_TYPE> operate(
      final UnaryOperator operator);

  public abstract FastBackedDoubleTimeSeries<DATE_TYPE> unionOperate(
      final FastTimeSeries<?> other, final BinaryOperator operator);

  public abstract FastBackedDoubleTimeSeries<DATE_TYPE> unionOperate(
      final FastBackedDoubleTimeSeries<?> other,
      final BinaryOperator operator);

  public abstract FastBackedDoubleTimeSeries<DATE_TYPE> add(
      FastBackedDoubleTimeSeries<?> other);

  public abstract FastBackedDoubleTimeSeries<DATE_TYPE> add(
      FastTimeSeries<?> other);

  public abstract FastBackedDoubleTimeSeries<DATE_TYPE> add(double other);

  public abstract FastBackedDoubleTimeSeries<DATE_TYPE> unionAdd(
      FastBackedDoubleTimeSeries<?> other);

  public abstract FastBackedDoubleTimeSeries<DATE_TYPE> unionAdd(
      FastTimeSeries<?> other);

  public abstract FastBackedDoubleTimeSeries<DATE_TYPE> subtract(
      FastBackedDoubleTimeSeries<?> other);

  public abstract FastBackedDoubleTimeSeries<DATE_TYPE> subtract(
      FastTimeSeries<?> other);

  public abstract FastBackedDoubleTimeSeries<DATE_TYPE> subtract(double other);

  public abstract FastBackedDoubleTimeSeries<DATE_TYPE> unionSubtract(
      FastBackedDoubleTimeSeries<?> other);

  public abstract FastBackedDoubleTimeSeries<DATE_TYPE> unionSubtract(
      FastTimeSeries<?> other);

  public abstract FastBackedDoubleTimeSeries<DATE_TYPE> multiply(
      FastBackedDoubleTimeSeries<?> other);

  public abstract FastBackedDoubleTimeSeries<DATE_TYPE> multiply(
      FastTimeSeries<?> other);

  public abstract FastBackedDoubleTimeSeries<DATE_TYPE> multiply(double other);

  public abstract FastBackedDoubleTimeSeries<DATE_TYPE> unionMultiply(
      FastBackedDoubleTimeSeries<?> other);

  public abstract FastBackedDoubleTimeSeries<DATE_TYPE> unionMultiply(
      FastTimeSeries<?> other);

  public abstract FastBackedDoubleTimeSeries<DATE_TYPE> divide(
      FastBackedDoubleTimeSeries<?> other);

  public abstract FastBackedDoubleTimeSeries<DATE_TYPE> divide(
      FastTimeSeries<?> other);

  public abstract FastBackedDoubleTimeSeries<DATE_TYPE> divide(double other);

  public abstract FastBackedDoubleTimeSeries<DATE_TYPE> unionDivide(
      FastBackedDoubleTimeSeries<?> other);

  public abstract FastBackedDoubleTimeSeries<DATE_TYPE> unionDivide(
      FastTimeSeries<?> other);

  public abstract FastBackedDoubleTimeSeries<DATE_TYPE> power(
      FastBackedDoubleTimeSeries<?> other);

  public abstract FastBackedDoubleTimeSeries<DATE_TYPE> power(
      FastTimeSeries<?> other);

  public abstract FastBackedDoubleTimeSeries<DATE_TYPE> power(double other);

  public abstract FastBackedDoubleTimeSeries<DATE_TYPE> unionPower(
      FastBackedDoubleTimeSeries<?> other);

  public abstract FastBackedDoubleTimeSeries<DATE_TYPE> unionPower(
      FastTimeSeries<?> other);

  public abstract FastBackedDoubleTimeSeries<DATE_TYPE> minimum(
      FastBackedDoubleTimeSeries<?> other);

  public abstract FastBackedDoubleTimeSeries<DATE_TYPE> minimum(
      FastTimeSeries<?> other);

  public abstract FastBackedDoubleTimeSeries<DATE_TYPE> minimum(double other);

  public abstract FastBackedDoubleTimeSeries<DATE_TYPE> unionMinimum(
      FastBackedDoubleTimeSeries<?> other);

  public abstract FastBackedDoubleTimeSeries<DATE_TYPE> unionMinimum(
      FastTimeSeries<?> other);

  public abstract FastBackedDoubleTimeSeries<DATE_TYPE> maximum(
      FastBackedDoubleTimeSeries<?> other);

  public abstract FastBackedDoubleTimeSeries<DATE_TYPE> maximum(
      FastTimeSeries<?> other);

  public abstract FastBackedDoubleTimeSeries<DATE_TYPE> maximum(double other);

  public abstract FastBackedDoubleTimeSeries<DATE_TYPE> unionMaximum(
      FastBackedDoubleTimeSeries<?> other);

  public abstract FastBackedDoubleTimeSeries<DATE_TYPE> unionMaximum(
      FastTimeSeries<?> other);

  public abstract FastBackedDoubleTimeSeries<DATE_TYPE> average(
      FastBackedDoubleTimeSeries<?> other);

  public abstract FastBackedDoubleTimeSeries<DATE_TYPE> average(
      FastTimeSeries<?> other);

  public abstract FastBackedDoubleTimeSeries<DATE_TYPE> average(double other);

  public abstract FastBackedDoubleTimeSeries<DATE_TYPE> unionAverage(
      FastBackedDoubleTimeSeries<?> other);

  public abstract FastBackedDoubleTimeSeries<DATE_TYPE> unionAverage(
      FastTimeSeries<?> other);

  public abstract FastBackedDoubleTimeSeries<DATE_TYPE> intersectionFirstValue(
      FastBackedDoubleTimeSeries<?> other);

  public abstract FastBackedDoubleTimeSeries<DATE_TYPE> intersectionFirstValue(
      FastTimeSeries<?> other);

  public abstract FastBackedDoubleTimeSeries<DATE_TYPE> intersectionSecondValue(
      FastBackedDoubleTimeSeries<?> other);

  public abstract FastBackedDoubleTimeSeries<DATE_TYPE> intersectionSecondValue(
      FastTimeSeries<?> other);

  public abstract FastBackedDoubleTimeSeries<DATE_TYPE> negate();

  public abstract FastBackedDoubleTimeSeries<DATE_TYPE> reciprocal();

  public abstract FastBackedDoubleTimeSeries<DATE_TYPE> log();

  public abstract FastBackedDoubleTimeSeries<DATE_TYPE> log10();

  public abstract FastBackedDoubleTimeSeries<DATE_TYPE> abs();

  public abstract double maxValue();

  public abstract double minValue();

  public abstract FastBackedDoubleTimeSeries<DATE_TYPE> lag(final int days);

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
