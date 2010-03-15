/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.util.timeseries;

import static com.opengamma.util.timeseries.DoubleTimeSeriesOperators.ABS_OPERATOR;
import static com.opengamma.util.timeseries.DoubleTimeSeriesOperators.ADD_OPERATOR;
import static com.opengamma.util.timeseries.DoubleTimeSeriesOperators.AVERAGE_OPERATOR;
import static com.opengamma.util.timeseries.DoubleTimeSeriesOperators.DIVIDE_OPERATOR;
import static com.opengamma.util.timeseries.DoubleTimeSeriesOperators.FIRST_OPERATOR;
import static com.opengamma.util.timeseries.DoubleTimeSeriesOperators.LOG10_OPERATOR;
import static com.opengamma.util.timeseries.DoubleTimeSeriesOperators.LOG_OPERATOR;
import static com.opengamma.util.timeseries.DoubleTimeSeriesOperators.MAXIMUM_OPERATOR;
import static com.opengamma.util.timeseries.DoubleTimeSeriesOperators.MINIMUM_OPERATOR;
import static com.opengamma.util.timeseries.DoubleTimeSeriesOperators.MULTIPLY_OPERATOR;
import static com.opengamma.util.timeseries.DoubleTimeSeriesOperators.NEGATE_OPERATOR;
import static com.opengamma.util.timeseries.DoubleTimeSeriesOperators.POWER_OPERATOR;
import static com.opengamma.util.timeseries.DoubleTimeSeriesOperators.RECIPROCAL_OPERATOR;
import static com.opengamma.util.timeseries.DoubleTimeSeriesOperators.SECOND_OPERATOR;
import static com.opengamma.util.timeseries.DoubleTimeSeriesOperators.SUBTRACT_OPERATOR;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.util.timeseries.DoubleTimeSeriesOperators.BinaryOperator;
import com.opengamma.util.timeseries.DoubleTimeSeriesOperators.UnaryOperator;
import com.opengamma.util.timeseries.fast.DateTimeNumericEncoding;
import com.opengamma.util.timeseries.fast.FastTimeSeries;
import com.opengamma.util.timeseries.fast.integer.FastIntDoubleTimeSeries;
import com.opengamma.util.timeseries.fast.integer.FastMutableIntDoubleTimeSeries;
import com.opengamma.util.timeseries.fast.longint.FastLongDoubleTimeSeries;
import com.opengamma.util.timeseries.fast.longint.FastMutableLongDoubleTimeSeries;

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

  public FastBackedDoubleTimeSeries<DATE_TYPE> add(DoubleTimeSeries<?> other) {
    if (other instanceof FastBackedDoubleTimeSeries<?>) {
      return operate((FastBackedDoubleTimeSeries<?>)other, ADD_OPERATOR);
    } else if (other instanceof FastIntDoubleTimeSeries) {
      return operate((FastIntDoubleTimeSeries)other, ADD_OPERATOR);
    } else { // if (other instanceof FastLongDoubleTimeSeries) {
      return operate((FastLongDoubleTimeSeries)other, ADD_OPERATOR);
    }
  }
  
  public FastBackedDoubleTimeSeries<DATE_TYPE> add(FastBackedDoubleTimeSeries<?> other) {
    return operate(other, ADD_OPERATOR);
  }
  public FastBackedDoubleTimeSeries<DATE_TYPE> add(FastIntDoubleTimeSeries other) {
    return operate(other, ADD_OPERATOR);
  }
  public FastBackedDoubleTimeSeries<DATE_TYPE> add(FastLongDoubleTimeSeries other) {
	    return operate(other, ADD_OPERATOR);
	  }
  public FastBackedDoubleTimeSeries<DATE_TYPE> add(double other) {
    return operate(other, ADD_OPERATOR);
  }
  
  public FastBackedDoubleTimeSeries<DATE_TYPE> unionAdd(DoubleTimeSeries<?> other) {
    if (other instanceof FastBackedDoubleTimeSeries<?>) {
      return unionOperate((FastBackedDoubleTimeSeries<?>)other, ADD_OPERATOR);
    } else if (other instanceof FastIntDoubleTimeSeries) {
      return unionOperate((FastIntDoubleTimeSeries)other, ADD_OPERATOR);
    } else { // if (other instanceof FastLongDoubleTimeSeries) {
      return unionOperate((FastLongDoubleTimeSeries)other, ADD_OPERATOR);
    }
  }
  public FastBackedDoubleTimeSeries<DATE_TYPE> unionAdd(FastBackedDoubleTimeSeries<?> other) {
    return unionOperate(other, ADD_OPERATOR);
  }
  public FastBackedDoubleTimeSeries<DATE_TYPE> unionAdd(FastIntDoubleTimeSeries other) {
    return unionOperate(other, ADD_OPERATOR);
  }
  public FastBackedDoubleTimeSeries<DATE_TYPE> unionAdd(FastLongDoubleTimeSeries other) {
    return unionOperate(other, ADD_OPERATOR);
  }
  public FastBackedDoubleTimeSeries<DATE_TYPE> subtract(DoubleTimeSeries<?> other) {
    if (other instanceof FastBackedDoubleTimeSeries<?>) {
      return operate((FastBackedDoubleTimeSeries<?>)other, SUBTRACT_OPERATOR);
    } else if (other instanceof FastIntDoubleTimeSeries) {
      return operate((FastIntDoubleTimeSeries)other, SUBTRACT_OPERATOR);
    } else { // if (other instanceof FastLongDoubleTimeSeries) {
      return operate((FastLongDoubleTimeSeries)other, SUBTRACT_OPERATOR);
    }
  }
  public FastBackedDoubleTimeSeries<DATE_TYPE> subtract(FastBackedDoubleTimeSeries<?> other) {
    return operate(other, SUBTRACT_OPERATOR);
  }
  public FastBackedDoubleTimeSeries<DATE_TYPE> subtract(FastLongDoubleTimeSeries other) {
    return operate(other, SUBTRACT_OPERATOR);
  }
  public FastBackedDoubleTimeSeries<DATE_TYPE> subtract(FastIntDoubleTimeSeries other) {
    return operate(other, SUBTRACT_OPERATOR);
  }
  public FastBackedDoubleTimeSeries<DATE_TYPE> subtract(double other) {
    return operate(other, SUBTRACT_OPERATOR);
  }
  public FastBackedDoubleTimeSeries<DATE_TYPE> unionSubtract(DoubleTimeSeries<?> other) {
    if (other instanceof FastBackedDoubleTimeSeries<?>) {
      return unionOperate((FastBackedDoubleTimeSeries<?>)other, SUBTRACT_OPERATOR);
    } else if (other instanceof FastIntDoubleTimeSeries) {
      return unionOperate((FastIntDoubleTimeSeries)other, SUBTRACT_OPERATOR);
    } else { // if (other instanceof FastLongDoubleTimeSeries) {
      return unionOperate((FastLongDoubleTimeSeries)other, SUBTRACT_OPERATOR);
    }
  }
  public FastBackedDoubleTimeSeries<DATE_TYPE> unionSubtract(FastBackedDoubleTimeSeries<?> other) {
    return unionOperate(other, SUBTRACT_OPERATOR);
  }
  public FastBackedDoubleTimeSeries<DATE_TYPE> unionSubtract(FastIntDoubleTimeSeries other) {
    return unionOperate(other, SUBTRACT_OPERATOR);
  }
  public FastBackedDoubleTimeSeries<DATE_TYPE> unionSubtract(FastLongDoubleTimeSeries other) {
    return unionOperate(other, SUBTRACT_OPERATOR);
  }
  public FastBackedDoubleTimeSeries<DATE_TYPE> multiply(DoubleTimeSeries<?> other) {
    if (other instanceof FastBackedDoubleTimeSeries<?>) {
      return operate((FastBackedDoubleTimeSeries<?>)other, MULTIPLY_OPERATOR);
    } else if (other instanceof FastIntDoubleTimeSeries) {
      return operate((FastIntDoubleTimeSeries)other, MULTIPLY_OPERATOR);
    } else { // if (other instanceof FastLongDoubleTimeSeries) {
      return operate((FastLongDoubleTimeSeries)other, MULTIPLY_OPERATOR);
    }
  }
  public FastBackedDoubleTimeSeries<DATE_TYPE> multiply(FastBackedDoubleTimeSeries<?> other) {
    return operate(other, MULTIPLY_OPERATOR);
  }
  public FastBackedDoubleTimeSeries<DATE_TYPE> multiply(FastIntDoubleTimeSeries other) {
    return operate(other, MULTIPLY_OPERATOR);
  }
  public FastBackedDoubleTimeSeries<DATE_TYPE> multiply(FastLongDoubleTimeSeries other) {
    return operate(other, MULTIPLY_OPERATOR);
  }
  public FastBackedDoubleTimeSeries<DATE_TYPE> multiply(double other) {
    return operate(other, MULTIPLY_OPERATOR);
  }
  public FastBackedDoubleTimeSeries<DATE_TYPE> unionMultiply(DoubleTimeSeries<?> other) {
    if (other instanceof FastBackedDoubleTimeSeries<?>) {
      return unionOperate((FastBackedDoubleTimeSeries<?>)other, MULTIPLY_OPERATOR);
    } else if (other instanceof FastIntDoubleTimeSeries) {
      return unionOperate((FastIntDoubleTimeSeries)other, MULTIPLY_OPERATOR);
    } else { // if (other instanceof FastLongDoubleTimeSeries) {
      return unionOperate((FastLongDoubleTimeSeries)other, MULTIPLY_OPERATOR);
    }
  }
  public FastBackedDoubleTimeSeries<DATE_TYPE> unionMultiply(FastBackedDoubleTimeSeries<?> other) {
    return unionOperate(other, MULTIPLY_OPERATOR);
  }
  public FastBackedDoubleTimeSeries<DATE_TYPE> unionMultiply(FastIntDoubleTimeSeries other) {
    return unionOperate(other, MULTIPLY_OPERATOR);
  }
  public FastBackedDoubleTimeSeries<DATE_TYPE> unionMultiply(FastLongDoubleTimeSeries other) {
    return unionOperate(other, MULTIPLY_OPERATOR);
  }
  public FastBackedDoubleTimeSeries<DATE_TYPE> divide(DoubleTimeSeries<?> other) {
    if (other instanceof FastBackedDoubleTimeSeries<?>) {
      return operate((FastBackedDoubleTimeSeries<?>)other, DIVIDE_OPERATOR);
    } else if (other instanceof FastIntDoubleTimeSeries) {
      return operate((FastIntDoubleTimeSeries)other, DIVIDE_OPERATOR);
    } else { // if (other instanceof FastLongDoubleTimeSeries) {
      return operate((FastLongDoubleTimeSeries)other, DIVIDE_OPERATOR);
    }
  }
  public FastBackedDoubleTimeSeries<DATE_TYPE> divide(FastBackedDoubleTimeSeries<?> other) {
    return operate(other, DIVIDE_OPERATOR);
  }
  public FastBackedDoubleTimeSeries<DATE_TYPE> divide(FastIntDoubleTimeSeries other) {
    return operate(other, DIVIDE_OPERATOR);
  }
  public FastBackedDoubleTimeSeries<DATE_TYPE> divide(FastLongDoubleTimeSeries other) {
    return operate(other, DIVIDE_OPERATOR);
  }
  public FastBackedDoubleTimeSeries<DATE_TYPE> divide(double other) {
    return operate(other, DIVIDE_OPERATOR);
  }
  public FastBackedDoubleTimeSeries<DATE_TYPE> unionDivide(DoubleTimeSeries<?> other) {
    if (other instanceof FastBackedDoubleTimeSeries<?>) {
      return unionOperate((FastBackedDoubleTimeSeries<?>)other, DIVIDE_OPERATOR);
    } else if (other instanceof FastIntDoubleTimeSeries) {
      return unionOperate((FastIntDoubleTimeSeries)other, DIVIDE_OPERATOR);
    } else { // if (other instanceof FastLongDoubleTimeSeries) {
      return unionOperate((FastLongDoubleTimeSeries)other, DIVIDE_OPERATOR);
    }
  }
  public FastBackedDoubleTimeSeries<DATE_TYPE> unionDivide(FastBackedDoubleTimeSeries<?> other) {
    return unionOperate(other, DIVIDE_OPERATOR);
  }
  public FastBackedDoubleTimeSeries<DATE_TYPE> unionDivide(FastIntDoubleTimeSeries other) {
    return unionOperate(other, DIVIDE_OPERATOR);
  }
  public FastBackedDoubleTimeSeries<DATE_TYPE> unionDivide(FastLongDoubleTimeSeries other) {
    return unionOperate(other, DIVIDE_OPERATOR);
  }
  public FastBackedDoubleTimeSeries<DATE_TYPE> power(DoubleTimeSeries<?> other) {
    if (other instanceof FastBackedDoubleTimeSeries<?>) {
      return operate((FastBackedDoubleTimeSeries<?>)other, POWER_OPERATOR);
    } else if (other instanceof FastIntDoubleTimeSeries) {
      return operate((FastIntDoubleTimeSeries)other, POWER_OPERATOR);
    } else { // if (other instanceof FastLongDoubleTimeSeries) {
      return operate((FastLongDoubleTimeSeries)other, POWER_OPERATOR);
    }
  }
  public FastBackedDoubleTimeSeries<DATE_TYPE> power(FastBackedDoubleTimeSeries<?> other) {
    return operate(other, POWER_OPERATOR);
  }
  public FastBackedDoubleTimeSeries<DATE_TYPE> power(FastIntDoubleTimeSeries other) {
    return operate(other, POWER_OPERATOR);
  }
  public FastBackedDoubleTimeSeries<DATE_TYPE> power(FastLongDoubleTimeSeries other) {
    return operate(other, POWER_OPERATOR);
  }
  public FastBackedDoubleTimeSeries<DATE_TYPE> power(double other) {
    return operate(other, POWER_OPERATOR);
  }
  public FastBackedDoubleTimeSeries<DATE_TYPE> unionPower(DoubleTimeSeries<?> other) {
    if (other instanceof FastBackedDoubleTimeSeries<?>) {
      return unionOperate((FastBackedDoubleTimeSeries<?>)other, POWER_OPERATOR);
    } else if (other instanceof FastIntDoubleTimeSeries) {
      return unionOperate((FastIntDoubleTimeSeries)other, POWER_OPERATOR);
    } else { // if (other instanceof FastLongDoubleTimeSeries) {
      return unionOperate((FastLongDoubleTimeSeries)other, POWER_OPERATOR);
    }
  }
  public FastBackedDoubleTimeSeries<DATE_TYPE> unionPower(FastBackedDoubleTimeSeries<?> other) {
    return unionOperate(other, POWER_OPERATOR);
  }
  public FastBackedDoubleTimeSeries<DATE_TYPE> unionPower(FastIntDoubleTimeSeries other) {
    return unionOperate(other, POWER_OPERATOR);
  }
  public FastBackedDoubleTimeSeries<DATE_TYPE> unionPower(FastLongDoubleTimeSeries other) {
    return unionOperate(other, POWER_OPERATOR);
  }
  public FastBackedDoubleTimeSeries<DATE_TYPE> minimum(DoubleTimeSeries<?> other) {
    if (other instanceof FastBackedDoubleTimeSeries<?>) {
      return operate((FastBackedDoubleTimeSeries<?>)other, MINIMUM_OPERATOR);
    } else if (other instanceof FastIntDoubleTimeSeries) {
      return operate((FastIntDoubleTimeSeries)other, MINIMUM_OPERATOR);
    } else { // if (other instanceof FastLongDoubleTimeSeries) {
      return operate((FastLongDoubleTimeSeries)other, MINIMUM_OPERATOR);
    }
  }
  public FastBackedDoubleTimeSeries<DATE_TYPE> minimum(FastBackedDoubleTimeSeries<?> other) {
    return operate(other, MINIMUM_OPERATOR);
  }
  public FastBackedDoubleTimeSeries<DATE_TYPE> minimum(FastIntDoubleTimeSeries other) {
    return operate(other, MINIMUM_OPERATOR);
  }
  public FastBackedDoubleTimeSeries<DATE_TYPE> minimum(FastLongDoubleTimeSeries other) {
    return operate(other, MINIMUM_OPERATOR);
  }
  public FastBackedDoubleTimeSeries<DATE_TYPE> minimum(double other) {
    return operate(other, MINIMUM_OPERATOR);
  }
  public FastBackedDoubleTimeSeries<DATE_TYPE> unionMinimum(DoubleTimeSeries<?> other) {
    if (other instanceof FastBackedDoubleTimeSeries<?>) {
      return unionOperate((FastBackedDoubleTimeSeries<?>)other, MINIMUM_OPERATOR);
    } else if (other instanceof FastIntDoubleTimeSeries) {
      return unionOperate((FastIntDoubleTimeSeries)other, MINIMUM_OPERATOR);
    } else { // if (other instanceof FastLongDoubleTimeSeries) {
      return unionOperate((FastLongDoubleTimeSeries)other, MINIMUM_OPERATOR);
    }
  }
  public FastBackedDoubleTimeSeries<DATE_TYPE> unionMinimum(FastBackedDoubleTimeSeries<?> other) {
    return unionOperate(other, MINIMUM_OPERATOR);
  }
  public FastBackedDoubleTimeSeries<DATE_TYPE> unionMinimum(FastIntDoubleTimeSeries other) {
    return unionOperate(other, MINIMUM_OPERATOR);
  }
  public FastBackedDoubleTimeSeries<DATE_TYPE> unionMinimum(FastLongDoubleTimeSeries other) {
    return unionOperate(other, MINIMUM_OPERATOR);
  }
  public FastBackedDoubleTimeSeries<DATE_TYPE> maximum(DoubleTimeSeries<?> other) {
    if (other instanceof FastBackedDoubleTimeSeries<?>) {
      return operate((FastBackedDoubleTimeSeries<?>)other, MAXIMUM_OPERATOR);
    } else if (other instanceof FastIntDoubleTimeSeries) {
      return operate((FastIntDoubleTimeSeries)other, MAXIMUM_OPERATOR);
    } else { // if (other instanceof FastLongDoubleTimeSeries) {
      return operate((FastLongDoubleTimeSeries)other, MAXIMUM_OPERATOR);
    }
  }
  public FastBackedDoubleTimeSeries<DATE_TYPE> maximum(FastBackedDoubleTimeSeries<?> other) {
    return operate(other, MAXIMUM_OPERATOR);
  }
  public FastBackedDoubleTimeSeries<DATE_TYPE> maximum(FastIntDoubleTimeSeries other) {
    return operate(other, MAXIMUM_OPERATOR);
  }
  public FastBackedDoubleTimeSeries<DATE_TYPE> maximum(FastLongDoubleTimeSeries other) {
    return operate(other, MAXIMUM_OPERATOR);
  }
  public FastBackedDoubleTimeSeries<DATE_TYPE> maximum(double other) {
    return operate(other, MAXIMUM_OPERATOR);
  }
  public FastBackedDoubleTimeSeries<DATE_TYPE> unionMaximum(DoubleTimeSeries<?> other) {
    if (other instanceof FastBackedDoubleTimeSeries<?>) {
      return unionOperate((FastBackedDoubleTimeSeries<?>)other, MAXIMUM_OPERATOR);
    } else if (other instanceof FastIntDoubleTimeSeries) {
      return unionOperate((FastIntDoubleTimeSeries)other, MAXIMUM_OPERATOR);
    } else { // if (other instanceof FastLongDoubleTimeSeries) {
      return unionOperate((FastLongDoubleTimeSeries)other, MAXIMUM_OPERATOR);
    }
  }
  public FastBackedDoubleTimeSeries<DATE_TYPE> unionMaximum(FastBackedDoubleTimeSeries<?> other) {
    return unionOperate(other, MAXIMUM_OPERATOR);
  }
  public FastBackedDoubleTimeSeries<DATE_TYPE> unionMaximum(FastIntDoubleTimeSeries other) {
    return unionOperate(other, MAXIMUM_OPERATOR);
  }
  public FastBackedDoubleTimeSeries<DATE_TYPE> unionMaximum(FastLongDoubleTimeSeries other) {
    return unionOperate(other, MAXIMUM_OPERATOR);
  }
  public FastBackedDoubleTimeSeries<DATE_TYPE> average(DoubleTimeSeries<?> other) {
    if (other instanceof FastBackedDoubleTimeSeries<?>) {
      return operate((FastBackedDoubleTimeSeries<?>)other, AVERAGE_OPERATOR);
    } else if (other instanceof FastIntDoubleTimeSeries) {
      return operate((FastIntDoubleTimeSeries)other, AVERAGE_OPERATOR);
    } else { // if (other instanceof FastLongDoubleTimeSeries) {
      return operate((FastLongDoubleTimeSeries)other, AVERAGE_OPERATOR);
    }
  }
  public FastBackedDoubleTimeSeries<DATE_TYPE> average(FastBackedDoubleTimeSeries<?> other) {
    return operate(other, AVERAGE_OPERATOR);
  }
  public FastBackedDoubleTimeSeries<DATE_TYPE> average(FastIntDoubleTimeSeries other) {
    return operate(other, AVERAGE_OPERATOR);
  }
  public FastBackedDoubleTimeSeries<DATE_TYPE> average(FastLongDoubleTimeSeries other) {
    return operate(other, AVERAGE_OPERATOR);
  }
  public FastBackedDoubleTimeSeries<DATE_TYPE> average(double other) {
    return operate(other, AVERAGE_OPERATOR);
  }
  public FastBackedDoubleTimeSeries<DATE_TYPE> unionAverage(DoubleTimeSeries<?> other) {
    if (other instanceof FastBackedDoubleTimeSeries<?>) {
      return unionOperate((FastBackedDoubleTimeSeries<?>)other, AVERAGE_OPERATOR);
    } else if (other instanceof FastIntDoubleTimeSeries) {
      return unionOperate((FastIntDoubleTimeSeries)other, AVERAGE_OPERATOR);
    } else { // if (other instanceof FastLongDoubleTimeSeries) {
      return unionOperate((FastLongDoubleTimeSeries)other, AVERAGE_OPERATOR);
    }
  }
  public FastBackedDoubleTimeSeries<DATE_TYPE> unionAverage(FastBackedDoubleTimeSeries<?> other) {
    return unionOperate(other, AVERAGE_OPERATOR);
  }
  public FastBackedDoubleTimeSeries<DATE_TYPE> unionAverage(FastIntDoubleTimeSeries other) {
    return unionOperate(other, AVERAGE_OPERATOR);
  }
  public FastBackedDoubleTimeSeries<DATE_TYPE> unionAverage(FastLongDoubleTimeSeries other) {
    return unionOperate(other, AVERAGE_OPERATOR);
  }
  public FastBackedDoubleTimeSeries<DATE_TYPE> intersectionFirstValue(DoubleTimeSeries<?> other) {
    if (other instanceof FastBackedDoubleTimeSeries<?>) {
      return operate((FastBackedDoubleTimeSeries<?>)other, FIRST_OPERATOR);
    } else if (other instanceof FastIntDoubleTimeSeries) {
      return operate((FastIntDoubleTimeSeries)other, FIRST_OPERATOR);
    } else { // if (other instanceof FastLongDoubleTimeSeries) {
      return operate((FastLongDoubleTimeSeries)other, FIRST_OPERATOR);
    }
  }
  public FastBackedDoubleTimeSeries<DATE_TYPE> intersectionFirstValue(FastBackedDoubleTimeSeries<?> other) {
    return operate(other, FIRST_OPERATOR);
  }
  public FastBackedDoubleTimeSeries<DATE_TYPE> intersectionFirstValue(FastIntDoubleTimeSeries other) {
    return operate(other, FIRST_OPERATOR);
  }
  public FastBackedDoubleTimeSeries<DATE_TYPE> intersectionFirstValue(FastLongDoubleTimeSeries other) {
    return operate(other, FIRST_OPERATOR);
  }
  public FastBackedDoubleTimeSeries<DATE_TYPE> intersectionSecondValue(DoubleTimeSeries<?> other) {
    if (other instanceof FastBackedDoubleTimeSeries<?>) {
      return operate((FastBackedDoubleTimeSeries<?>)other, SECOND_OPERATOR);
    } else if (other instanceof FastIntDoubleTimeSeries) {
      return operate((FastIntDoubleTimeSeries)other, SECOND_OPERATOR);
    } else { // if (other instanceof FastLongDoubleTimeSeries) {
      return operate((FastLongDoubleTimeSeries)other, SECOND_OPERATOR);
    }
  }
  public FastBackedDoubleTimeSeries<DATE_TYPE> intersectionSecondValue(FastBackedDoubleTimeSeries<?> other) {
    return operate(other, SECOND_OPERATOR);
  }
  public FastBackedDoubleTimeSeries<DATE_TYPE> intersectionSecondValue(FastIntDoubleTimeSeries other) {
    return operate(other, SECOND_OPERATOR);
  }
  public FastBackedDoubleTimeSeries<DATE_TYPE> intersectionSecondValue(FastLongDoubleTimeSeries other) {
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
