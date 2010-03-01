/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.timeseries;

import static com.opengamma.timeseries.DoubleTimeSeriesOperators.ABS_OPERATOR;
import static com.opengamma.timeseries.DoubleTimeSeriesOperators.ADD_OPERATOR;
import static com.opengamma.timeseries.DoubleTimeSeriesOperators.AVERAGE_OPERATOR;
import static com.opengamma.timeseries.DoubleTimeSeriesOperators.DIVIDE_OPERATOR;
import static com.opengamma.timeseries.DoubleTimeSeriesOperators.FIRST_OPERATOR;
import static com.opengamma.timeseries.DoubleTimeSeriesOperators.LOG10_OPERATOR;
import static com.opengamma.timeseries.DoubleTimeSeriesOperators.LOG_OPERATOR;
import static com.opengamma.timeseries.DoubleTimeSeriesOperators.MAXIMUM_OPERATOR;
import static com.opengamma.timeseries.DoubleTimeSeriesOperators.MINIMUM_OPERATOR;
import static com.opengamma.timeseries.DoubleTimeSeriesOperators.MULTIPLY_OPERATOR;
import static com.opengamma.timeseries.DoubleTimeSeriesOperators.NEGATE_OPERATOR;
import static com.opengamma.timeseries.DoubleTimeSeriesOperators.POWER_OPERATOR;
import static com.opengamma.timeseries.DoubleTimeSeriesOperators.RECIPROCAL_OPERATOR;
import static com.opengamma.timeseries.DoubleTimeSeriesOperators.SECOND_OPERATOR;
import static com.opengamma.timeseries.DoubleTimeSeriesOperators.SUBTRACT_OPERATOR;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.timeseries.DoubleTimeSeriesOperators.BinaryOperator;
import com.opengamma.timeseries.DoubleTimeSeriesOperators.UnaryOperator;
import com.opengamma.timeseries.fast.DateTimeNumericEncoding;
import com.opengamma.timeseries.fast.FastTimeSeries;
import com.opengamma.timeseries.fast.integer.FastIntDoubleTimeSeries;
import com.opengamma.timeseries.fast.integer.FastMutableIntDoubleTimeSeries;
import com.opengamma.timeseries.fast.longint.FastLongDoubleTimeSeries;
import com.opengamma.timeseries.fast.longint.FastMutableLongDoubleTimeSeries;

/**
 * @author jim
 * 
 */
public abstract class AbstractFastBackedDoubleTimeSeries<DATE_TYPE> implements DoubleTimeSeries<DATE_TYPE>, FastBackedDoubleTimeSeries<DATE_TYPE> {
  public abstract DateTimeConverter<DATE_TYPE> getConverter();

  public abstract FastTimeSeries<?> getFastSeries();
  
  public abstract FastBackedDoubleTimeSeries<DATE_TYPE> operate(final FastTimeSeries<?> other, final BinaryOperator operator);
  
  public abstract FastBackedDoubleTimeSeries<DATE_TYPE> operate(final FastBackedDoubleTimeSeries<?> other, final BinaryOperator operator);
  
  public abstract FastBackedDoubleTimeSeries<DATE_TYPE> operate(final double other, final BinaryOperator operator);
  
  public abstract FastBackedDoubleTimeSeries<DATE_TYPE> operate(final UnaryOperator operator);
  
  public abstract FastBackedDoubleTimeSeries<DATE_TYPE> unionOperate(final FastTimeSeries<?> other, final BinaryOperator operator);
  
  public abstract FastBackedDoubleTimeSeries<DATE_TYPE> unionOperate(final FastBackedDoubleTimeSeries<?> other, final BinaryOperator operator);
  
  public FastBackedDoubleTimeSeries<DATE_TYPE> add(FastBackedDoubleTimeSeries<?> other) {
    return operate(other, ADD_OPERATOR);
  }
  public FastBackedDoubleTimeSeries<DATE_TYPE> add(FastTimeSeries<?> other) {
    return operate(other, ADD_OPERATOR);
  }
  public FastBackedDoubleTimeSeries<DATE_TYPE> add(double other) {
    return operate(other, ADD_OPERATOR);
  }
  public FastBackedDoubleTimeSeries<DATE_TYPE> unionAdd(FastBackedDoubleTimeSeries<?> other) {
    return unionOperate(other, ADD_OPERATOR);
  }
  public FastBackedDoubleTimeSeries<DATE_TYPE> unionAdd(FastTimeSeries<?> other) {
    return unionOperate(other, ADD_OPERATOR);
  }
  public FastBackedDoubleTimeSeries<DATE_TYPE> subtract(FastBackedDoubleTimeSeries<?> other) {
    return operate(other, SUBTRACT_OPERATOR);
  }
  public FastBackedDoubleTimeSeries<DATE_TYPE> subtract(FastTimeSeries<?> other) {
    return operate(other, SUBTRACT_OPERATOR);
  }
  public FastBackedDoubleTimeSeries<DATE_TYPE> subtract(double other) {
    return operate(other, SUBTRACT_OPERATOR);
  }
  public FastBackedDoubleTimeSeries<DATE_TYPE> unionSubtract(FastBackedDoubleTimeSeries<?> other) {
    return unionOperate(other, SUBTRACT_OPERATOR);
  }
  public FastBackedDoubleTimeSeries<DATE_TYPE> unionSubtract(FastTimeSeries<?> other) {
    return unionOperate(other, SUBTRACT_OPERATOR);
  }
  public FastBackedDoubleTimeSeries<DATE_TYPE> multiply(FastBackedDoubleTimeSeries<?> other) {
    return operate(other, MULTIPLY_OPERATOR);
  }
  public FastBackedDoubleTimeSeries<DATE_TYPE> multiply(FastTimeSeries<?> other) {
    return operate(other, MULTIPLY_OPERATOR);
  }
  public FastBackedDoubleTimeSeries<DATE_TYPE> multiply(double other) {
    return operate(other, MULTIPLY_OPERATOR);
  }
  public FastBackedDoubleTimeSeries<DATE_TYPE> unionMultiply(FastBackedDoubleTimeSeries<?> other) {
    return unionOperate(other, MULTIPLY_OPERATOR);
  }
  public FastBackedDoubleTimeSeries<DATE_TYPE> unionMultiply(FastTimeSeries<?> other) {
    return unionOperate(other, MULTIPLY_OPERATOR);
  }
  public FastBackedDoubleTimeSeries<DATE_TYPE> divide(FastBackedDoubleTimeSeries<?> other) {
    return operate(other, DIVIDE_OPERATOR);
  }
  public FastBackedDoubleTimeSeries<DATE_TYPE> divide(FastTimeSeries<?> other) {
    return operate(other, DIVIDE_OPERATOR);
  }
  public FastBackedDoubleTimeSeries<DATE_TYPE> divide(double other) {
    return operate(other, DIVIDE_OPERATOR);
  }
  public FastBackedDoubleTimeSeries<DATE_TYPE> unionDivide(FastBackedDoubleTimeSeries<?> other) {
    return unionOperate(other, DIVIDE_OPERATOR);
  }
  public FastBackedDoubleTimeSeries<DATE_TYPE> unionDivide(FastTimeSeries<?> other) {
    return unionOperate(other, DIVIDE_OPERATOR);
  }
  public FastBackedDoubleTimeSeries<DATE_TYPE> power(FastBackedDoubleTimeSeries<?> other) {
    return operate(other, POWER_OPERATOR);
  }
  public FastBackedDoubleTimeSeries<DATE_TYPE> power(FastTimeSeries<?> other) {
    return operate(other, POWER_OPERATOR);
  }
  public FastBackedDoubleTimeSeries<DATE_TYPE> power(double other) {
    return operate(other, POWER_OPERATOR);
  }
  public FastBackedDoubleTimeSeries<DATE_TYPE> unionPower(FastBackedDoubleTimeSeries<?> other) {
    return unionOperate(other, POWER_OPERATOR);
  }
  public FastBackedDoubleTimeSeries<DATE_TYPE> unionPower(FastTimeSeries<?> other) {
    return unionOperate(other, POWER_OPERATOR);
  }
  public FastBackedDoubleTimeSeries<DATE_TYPE> minimum(FastBackedDoubleTimeSeries<?> other) {
    return operate(other, MINIMUM_OPERATOR);
  }
  public FastBackedDoubleTimeSeries<DATE_TYPE> minimum(FastTimeSeries<?> other) {
    return operate(other, MINIMUM_OPERATOR);
  }
  public FastBackedDoubleTimeSeries<DATE_TYPE> minimum(double other) {
    return operate(other, MINIMUM_OPERATOR);
  }
  public FastBackedDoubleTimeSeries<DATE_TYPE> unionMinimum(FastBackedDoubleTimeSeries<?> other) {
    return unionOperate(other, MINIMUM_OPERATOR);
  }
  public FastBackedDoubleTimeSeries<DATE_TYPE> unionMinimum(FastTimeSeries<?> other) {
    return unionOperate(other, MINIMUM_OPERATOR);
  }
  public FastBackedDoubleTimeSeries<DATE_TYPE> maximum(FastBackedDoubleTimeSeries<?> other) {
    return operate(other, MAXIMUM_OPERATOR);
  }
  public FastBackedDoubleTimeSeries<DATE_TYPE> maximum(FastTimeSeries<?> other) {
    return operate(other, MAXIMUM_OPERATOR);
  }
  public FastBackedDoubleTimeSeries<DATE_TYPE> maximum(double other) {
    return operate(other, MAXIMUM_OPERATOR);
  }
  public FastBackedDoubleTimeSeries<DATE_TYPE> unionMaximum(FastBackedDoubleTimeSeries<?> other) {
    return unionOperate(other, MAXIMUM_OPERATOR);
  }
  public FastBackedDoubleTimeSeries<DATE_TYPE> unionMaximum(FastTimeSeries<?> other) {
    return unionOperate(other, MAXIMUM_OPERATOR);
  }
  public FastBackedDoubleTimeSeries<DATE_TYPE> average(FastBackedDoubleTimeSeries<?> other) {
    return operate(other, AVERAGE_OPERATOR);
  }
  public FastBackedDoubleTimeSeries<DATE_TYPE> average(FastTimeSeries<?> other) {
    return operate(other, AVERAGE_OPERATOR);
  }
  public FastBackedDoubleTimeSeries<DATE_TYPE> average(double other) {
    return operate(other, AVERAGE_OPERATOR);
  }
  public FastBackedDoubleTimeSeries<DATE_TYPE> unionAverage(FastBackedDoubleTimeSeries<?> other) {
    return unionOperate(other, AVERAGE_OPERATOR);
  }
  public FastBackedDoubleTimeSeries<DATE_TYPE> unionAverage(FastTimeSeries<?> other) {
    return unionOperate(other, AVERAGE_OPERATOR);
  }
  
  public FastBackedDoubleTimeSeries<DATE_TYPE> intersectionFirstValue(FastBackedDoubleTimeSeries<?> other) {
    return operate(other, FIRST_OPERATOR);
  }
  public FastBackedDoubleTimeSeries<DATE_TYPE> intersectionFirstValue(FastTimeSeries<?> other) {
    return operate(other, FIRST_OPERATOR);
  }
  public FastBackedDoubleTimeSeries<DATE_TYPE> intersectionSecondValue(FastBackedDoubleTimeSeries<?> other) {
    return operate(other, SECOND_OPERATOR);
  }
  public FastBackedDoubleTimeSeries<DATE_TYPE> intersectionSecondValue(FastTimeSeries<?> other) {
    return operate(other, SECOND_OPERATOR);
  }

  public FastBackedDoubleTimeSeries<DATE_TYPE> negate() {
    return operate(NEGATE_OPERATOR);
  }
  public FastBackedDoubleTimeSeries<DATE_TYPE> reciprocal() {
    return operate(RECIPROCAL_OPERATOR);
  }
  public FastBackedDoubleTimeSeries<DATE_TYPE> log() {
    return operate(LOG_OPERATOR);
  }
  public FastBackedDoubleTimeSeries<DATE_TYPE> log10() {
    return operate(LOG10_OPERATOR);
  }
  public FastBackedDoubleTimeSeries<DATE_TYPE> abs() {
    return operate(ABS_OPERATOR);
  }
  
  public double maxValue() {
    if (isEmpty()) {
      throw new OpenGammaRuntimeException("TimeSeries is empty");
    }
    Double[] values = valuesArray();
    double max = Double.MIN_VALUE;
    for (int i=0; i< values.length; i++) {
      if (values[i] > max) {
        max = values[i];
      }
    }
    return max;
  }
  public double minValue() {
    if (isEmpty()) {
      throw new OpenGammaRuntimeException("TimeSeries is empty");
    }
    Double[] values = valuesArray();
    double min = Double.MAX_VALUE;
    for (int i=0; i< values.length; i++) {
      if (values[i] < min) {
        min = values[i];
      }
    }
    return min;
  }
  @SuppressWarnings("unchecked")
  public FastBackedDoubleTimeSeries<DATE_TYPE> lag(final int days) {
    DATE_TYPE[] times = timesArray();
    Double[] values = valuesArray();
    if (days == 0) {
      return (FastBackedDoubleTimeSeries<DATE_TYPE>) newInstance(times, values);
    } else if (days < 0) {
      DATE_TYPE[] resultTimes = (DATE_TYPE[]) new Object[times.length + days]; // remember days is -ve
      System.arraycopy(times, 0, resultTimes, 0, times.length + days);
      Double[] resultValues = new Double[times.length + days];
      System.arraycopy(values, -days, resultValues, 0, times.length + days);
      return (FastBackedDoubleTimeSeries<DATE_TYPE>) newInstance(times, values);
    } else { // if (days > 0) {
      DATE_TYPE[] resultTimes = (DATE_TYPE[]) new Object[times.length - days]; // remember days is +ve
      System.arraycopy(times, days, resultTimes, 0, times.length - days);
      Double[] resultValues = new Double[times.length - days];
      System.arraycopy(values, 0, resultValues, 0, times.length - days);
      return (FastBackedDoubleTimeSeries<DATE_TYPE>) newInstance(times, values);
    }
  }
  
  @Override
  public FastIntDoubleTimeSeries toFastIntDoubleTimeSeries() {
    return getFastSeries().toFastIntDoubleTimeSeries();
  }

  @Override
  public FastIntDoubleTimeSeries toFastIntDoubleTimeSeries(
      DateTimeNumericEncoding encoding) {
    return getFastSeries().toFastIntDoubleTimeSeries(encoding);
  }

  @Override
  public FastLongDoubleTimeSeries toFastLongDoubleTimeSeries() {
    return getFastSeries().toFastLongDoubleTimeSeries();
  }

  @Override
  public FastLongDoubleTimeSeries toFastLongDoubleTimeSeries(
      DateTimeNumericEncoding encoding) {
    return getFastSeries().toFastLongDoubleTimeSeries(encoding);
  }

  @Override
  public FastMutableIntDoubleTimeSeries toFastMutableIntDoubleTimeSeries() {
    return getFastSeries().toFastMutableIntDoubleTimeSeries();
  }

  @Override
  public FastMutableIntDoubleTimeSeries toFastMutableIntDoubleTimeSeries(
      DateTimeNumericEncoding encoding) {
    return getFastSeries().toFastMutableIntDoubleTimeSeries(encoding);
  }

  @Override
  public FastMutableLongDoubleTimeSeries toFastMutableLongDoubleTimeSeries() {
    return getFastSeries().toFastMutableLongDoubleTimeSeries();
  }

  @Override
  public FastMutableLongDoubleTimeSeries toFastMutableLongDoubleTimeSeries(
      DateTimeNumericEncoding encoding) {
    return getFastSeries().toFastMutableLongDoubleTimeSeries(encoding);
  }

}
