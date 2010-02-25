/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.timeseries.fast;

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
import com.opengamma.timeseries.DoubleTimeSeries;
import com.opengamma.timeseries.FastBackedDoubleTimeSeries;
import com.opengamma.timeseries.DoubleTimeSeriesOperators.BinaryOperator;
import com.opengamma.timeseries.DoubleTimeSeriesOperators.UnaryOperator;
import com.opengamma.timeseries.fast.integer.FastIntDoubleTimeSeries;
import com.opengamma.timeseries.fast.longint.FastLongDoubleTimeSeries;

/**
 * @author jim
 * 
 */
public abstract class AbstractFastTimeSeries<T> implements DoubleTimeSeries<T>, FastTimeSeries<T> {
  public abstract DateTimeNumericEncoding getEncoding();

  public abstract DateTimeResolution getDateTimeResolution();
  
  public abstract FastTimeSeries<T> operate(final UnaryOperator operator);

  public abstract FastTimeSeries<T> operate(final double other, final BinaryOperator operator);
  
  public abstract FastTimeSeries<T> operate(final FastLongDoubleTimeSeries other, final BinaryOperator operator);
  
  public abstract FastTimeSeries<T> operate(final FastIntDoubleTimeSeries other, final BinaryOperator operator);
  
  public FastTimeSeries<T> operate(final FastBackedDoubleTimeSeries<?> other, final BinaryOperator operator) {
    FastTimeSeries<?> fastSeries = other.getFastSeries();
    if (fastSeries instanceof FastIntDoubleTimeSeries) {
      return operate((FastIntDoubleTimeSeries)fastSeries, operator);  
    } else { // if (fastSeries instanceof FastLongDoubleTimeSeries
      return operate((FastLongDoubleTimeSeries)fastSeries, operator);
    }   
  }

  public abstract FastTimeSeries<T> unionOperate(final FastLongDoubleTimeSeries other, final BinaryOperator operator);
  
  public abstract FastTimeSeries<T> unionOperate(final FastIntDoubleTimeSeries other, final BinaryOperator operator);
  
  public FastTimeSeries<T> unionOperate(final FastBackedDoubleTimeSeries<?> other, final BinaryOperator operator) {
    FastTimeSeries<?> fastSeries = other.getFastSeries();
    if (fastSeries instanceof FastIntDoubleTimeSeries) {
      return unionOperate((FastIntDoubleTimeSeries)fastSeries, operator);  
    } else { // if (fastSeries instanceof FastLongDoubleTimeSeries
      return unionOperate((FastLongDoubleTimeSeries)fastSeries, operator);
    }   
  }
  
  public FastTimeSeries<T> add(FastBackedDoubleTimeSeries<?> other) {
    return operate(other, ADD_OPERATOR);
  }
  public FastTimeSeries<T> add(FastLongDoubleTimeSeries other) {
    return operate(other, ADD_OPERATOR);
  }
  public FastTimeSeries<T> add(FastIntDoubleTimeSeries other) {
    return operate(other, ADD_OPERATOR);
  }
  public FastTimeSeries<T> add(double other) {
    return operate(other, ADD_OPERATOR);
  }
  public FastTimeSeries<T> unionAdd(FastBackedDoubleTimeSeries<?> other) {
    return unionOperate(other, ADD_OPERATOR);
  }
  public FastTimeSeries<T> unionAdd(FastLongDoubleTimeSeries other) {
    return unionOperate(other, ADD_OPERATOR);
  }
  public FastTimeSeries<T> unionAdd(FastIntDoubleTimeSeries other) {
    return unionOperate(other, ADD_OPERATOR);
  }
  public FastTimeSeries<T> subtract(FastBackedDoubleTimeSeries<?> other) {
    return operate(other, SUBTRACT_OPERATOR);
  }
  public FastTimeSeries<T> subtract(FastLongDoubleTimeSeries other) {
    return operate(other, SUBTRACT_OPERATOR);
  }
  public FastTimeSeries<T> subtract(FastIntDoubleTimeSeries other) {
    return operate(other, SUBTRACT_OPERATOR);
  }
  public FastTimeSeries<T> subtract(double other) {
    return operate(other, SUBTRACT_OPERATOR);
  }
  public FastTimeSeries<T> unionSubtract(FastBackedDoubleTimeSeries<?> other) {
    return unionOperate(other, SUBTRACT_OPERATOR);
  }
  public FastTimeSeries<T> unionSubtract(FastIntDoubleTimeSeries other) {
    return unionOperate(other, SUBTRACT_OPERATOR);
  }
  public FastTimeSeries<T> unionSubtract(FastLongDoubleTimeSeries other) {
    return unionOperate(other, SUBTRACT_OPERATOR);
  }
  public FastTimeSeries<T> multiply(FastBackedDoubleTimeSeries<?> other) {
    return operate(other, MULTIPLY_OPERATOR);
  }
  public FastTimeSeries<T> multiply(FastLongDoubleTimeSeries other) {
    return operate(other, MULTIPLY_OPERATOR);
  }
  public FastTimeSeries<T> multiply(FastIntDoubleTimeSeries other) {
    return operate(other, MULTIPLY_OPERATOR);
  }
  public FastTimeSeries<T> multiply(double other) {
    return operate(other, MULTIPLY_OPERATOR);
  }
  public FastTimeSeries<T> unionMultiply(FastBackedDoubleTimeSeries<?> other) {
    return unionOperate(other, MULTIPLY_OPERATOR);
  }
  public FastTimeSeries<T> unionMultiply(FastLongDoubleTimeSeries other) {
    return unionOperate(other, MULTIPLY_OPERATOR);
  }
  public FastTimeSeries<T> unionMultiply(FastIntDoubleTimeSeries other) {
    return unionOperate(other, MULTIPLY_OPERATOR);
  }
  public FastTimeSeries<T> divide(FastBackedDoubleTimeSeries<?> other) {
    return operate(other, DIVIDE_OPERATOR);
  }
  public FastTimeSeries<T> divide(FastLongDoubleTimeSeries other) {
    return operate(other, DIVIDE_OPERATOR);
  }
  public FastTimeSeries<T> divide(FastIntDoubleTimeSeries other) {
    return operate(other, DIVIDE_OPERATOR);
  }
  public FastTimeSeries<T> divide(double other) {
    return operate(other, DIVIDE_OPERATOR);
  }
  public FastTimeSeries<T> unionDivide(FastBackedDoubleTimeSeries<?> other) {
    return unionOperate(other, DIVIDE_OPERATOR);
  }
  public FastTimeSeries<T> unionDivide(FastLongDoubleTimeSeries other) {
    return unionOperate(other, DIVIDE_OPERATOR);
  }
  public FastTimeSeries<T> unionDivide(FastIntDoubleTimeSeries other) {
    return unionOperate(other, DIVIDE_OPERATOR);
  }
  public FastTimeSeries<T> power(FastBackedDoubleTimeSeries<?> other) {
    return operate(other, POWER_OPERATOR);
  }
  public FastTimeSeries<T> power(FastLongDoubleTimeSeries other) {
    return operate(other, POWER_OPERATOR);
  }
  public FastTimeSeries<T> power(FastIntDoubleTimeSeries other) {
    return operate(other, POWER_OPERATOR);
  }
  public FastTimeSeries<T> power(double other) {
    return operate(other, POWER_OPERATOR);
  }
  public FastTimeSeries<T> unionPower(FastBackedDoubleTimeSeries<?> other) {
    return unionOperate(other, POWER_OPERATOR);
  }
  public FastTimeSeries<T> unionPower(FastLongDoubleTimeSeries other) {
    return unionOperate(other, POWER_OPERATOR);
  }
  public FastTimeSeries<T> unionPower(FastIntDoubleTimeSeries other) {
    return unionOperate(other, POWER_OPERATOR);
  }
  public FastTimeSeries<T> minimum(FastBackedDoubleTimeSeries<?> other) {
    return operate(other, MINIMUM_OPERATOR);
  }
  public FastTimeSeries<T> minimum(FastLongDoubleTimeSeries other) {
    return operate(other, MINIMUM_OPERATOR);
  }
  public FastTimeSeries<T> minimum(FastIntDoubleTimeSeries other) {
    return operate(other, MINIMUM_OPERATOR);
  }
  public FastTimeSeries<T> minimum(double other) {
    return operate(other, MINIMUM_OPERATOR);
  }
  public FastTimeSeries<T> unionMinimum(FastBackedDoubleTimeSeries<?> other) {
    return unionOperate(other, MINIMUM_OPERATOR);
  }
  public FastTimeSeries<T> unionMinimum(FastLongDoubleTimeSeries other) {
    return unionOperate(other, MINIMUM_OPERATOR);
  }
  public FastTimeSeries<T> unionMinimum(FastIntDoubleTimeSeries other) {
    return unionOperate(other, MINIMUM_OPERATOR);
  }
  public FastTimeSeries<T> maximum(FastBackedDoubleTimeSeries<?> other) {
    return operate(other, MAXIMUM_OPERATOR);
  }
  public FastTimeSeries<T> maximum(FastLongDoubleTimeSeries other) {
    return operate(other, MAXIMUM_OPERATOR);
  }
  public FastTimeSeries<T> maximum(FastIntDoubleTimeSeries other) {
    return operate(other, MAXIMUM_OPERATOR);
  }
  public FastTimeSeries<T> maximum(double other) {
    return operate(other, MAXIMUM_OPERATOR);
  }
  public FastTimeSeries<T> unionMaximum(FastBackedDoubleTimeSeries<?> other) {
    return unionOperate(other, MAXIMUM_OPERATOR);
  }
  public FastTimeSeries<T> unionMaximum(FastLongDoubleTimeSeries other) {
    return unionOperate(other, MAXIMUM_OPERATOR);
  }
  public FastTimeSeries<T> unionMaximum(FastIntDoubleTimeSeries other) {
    return unionOperate(other, MAXIMUM_OPERATOR);
  }
  public FastTimeSeries<T> average(FastBackedDoubleTimeSeries<?> other) {
    return operate(other, AVERAGE_OPERATOR);
  }
  public FastTimeSeries<T> average(FastIntDoubleTimeSeries other) {
    return operate(other, AVERAGE_OPERATOR);
  }
  public FastTimeSeries<T> average(FastLongDoubleTimeSeries other) {
    return operate(other, AVERAGE_OPERATOR);
  }  
  public FastTimeSeries<T> average(double other) {
    return operate(other, AVERAGE_OPERATOR);
  }
  public FastTimeSeries<T> unionAverage(FastBackedDoubleTimeSeries<?> other) {
    return unionOperate(other, AVERAGE_OPERATOR);
  }
  public FastTimeSeries<T> unionAverage(FastLongDoubleTimeSeries other) {
    return unionOperate(other, AVERAGE_OPERATOR);
  }
  public FastTimeSeries<T> unionAverage(FastIntDoubleTimeSeries other) {
    return unionOperate(other, AVERAGE_OPERATOR);
  }
  
  public FastTimeSeries<T> intersectionFirstValue(FastBackedDoubleTimeSeries<?> other) {
    return operate(other, FIRST_OPERATOR);
  }
  public FastTimeSeries<T> intersectionFirstValue(FastLongDoubleTimeSeries other) {
    return operate(other, FIRST_OPERATOR);
  }
  public FastTimeSeries<T> intersectionFirstValue(FastIntDoubleTimeSeries other) {
    return operate(other, FIRST_OPERATOR);
  }
  public FastTimeSeries<T> intersectionSecondValue(FastBackedDoubleTimeSeries<?> other) {
    return operate(other, SECOND_OPERATOR);
  }
  public FastTimeSeries<T> intersectionSecondValue(FastLongDoubleTimeSeries other) {
    return operate(other, SECOND_OPERATOR);
  }
  public FastTimeSeries<T> intersectionSecondValue(FastIntDoubleTimeSeries other) {
    return operate(other, SECOND_OPERATOR);
  }

  public FastTimeSeries<T> negate() {
    return operate(NEGATE_OPERATOR);
  }
  public FastTimeSeries<T> reciprocal() {
    return operate(RECIPROCAL_OPERATOR);
  }
  public FastTimeSeries<T> log() {
    return operate(LOG_OPERATOR);
  }
  public FastTimeSeries<T> log10() {
    return operate(LOG10_OPERATOR);
  }
  public FastTimeSeries<T> abs() {
    return operate(ABS_OPERATOR);
  }
  
  public abstract double[] valuesArrayFast();
  
  public double maxValue() {
    if (isEmpty()) {
      throw new OpenGammaRuntimeException("TimeSeries is empty");
    }
    double[] values = valuesArrayFast();
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
    double[] values = valuesArrayFast();
    double min = Double.MAX_VALUE;
    for (int i=0; i< values.length; i++) {
      if (values[i] < min) {
        min = values[i];
      }
    }
    return min;
  }

}
