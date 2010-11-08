/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.util.timeseries.fast;

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
import static com.opengamma.util.timeseries.DoubleTimeSeriesOperators.NO_INTERSECTION_OPERATOR;
import static com.opengamma.util.timeseries.DoubleTimeSeriesOperators.POWER_OPERATOR;
import static com.opengamma.util.timeseries.DoubleTimeSeriesOperators.RECIPROCAL_OPERATOR;
import static com.opengamma.util.timeseries.DoubleTimeSeriesOperators.SECOND_OPERATOR;
import static com.opengamma.util.timeseries.DoubleTimeSeriesOperators.SUBTRACT_OPERATOR;

import java.util.Date;
import java.util.TimeZone;

import javax.time.calendar.ZonedDateTime;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.util.timeseries.DoubleTimeSeries;
import com.opengamma.util.timeseries.DoubleTimeSeriesOperators.BinaryOperator;
import com.opengamma.util.timeseries.DoubleTimeSeriesOperators.UnaryOperator;
import com.opengamma.util.timeseries.FastBackedDoubleTimeSeries;
import com.opengamma.util.timeseries.ToStringHelper;
import com.opengamma.util.timeseries.date.ArrayDateDoubleTimeSeries;
import com.opengamma.util.timeseries.date.DateDoubleTimeSeries;
import com.opengamma.util.timeseries.date.ListDateDoubleTimeSeries;
import com.opengamma.util.timeseries.date.MutableDateDoubleTimeSeries;
import com.opengamma.util.timeseries.date.time.ArrayDateTimeDoubleTimeSeries;
import com.opengamma.util.timeseries.date.time.DateTimeDoubleTimeSeries;
import com.opengamma.util.timeseries.date.time.ListDateTimeDoubleTimeSeries;
import com.opengamma.util.timeseries.date.time.MutableDateTimeDoubleTimeSeries;
import com.opengamma.util.timeseries.fast.integer.FastIntDoubleTimeSeries;
import com.opengamma.util.timeseries.fast.integer.FastMutableIntDoubleTimeSeries;
import com.opengamma.util.timeseries.fast.longint.FastLongDoubleTimeSeries;
import com.opengamma.util.timeseries.fast.longint.FastMutableLongDoubleTimeSeries;
import com.opengamma.util.timeseries.localdate.ArrayLocalDateDoubleTimeSeries;
import com.opengamma.util.timeseries.localdate.ListLocalDateDoubleTimeSeries;
import com.opengamma.util.timeseries.localdate.LocalDateDoubleTimeSeries;
import com.opengamma.util.timeseries.localdate.MutableLocalDateDoubleTimeSeries;
import com.opengamma.util.timeseries.sqldate.ArraySQLDateDoubleTimeSeries;
import com.opengamma.util.timeseries.sqldate.ListSQLDateDoubleTimeSeries;
import com.opengamma.util.timeseries.sqldate.MutableSQLDateDoubleTimeSeries;
import com.opengamma.util.timeseries.sqldate.SQLDateDoubleTimeSeries;
import com.opengamma.util.timeseries.yearoffset.ArrayYearOffsetDoubleTimeSeries;
import com.opengamma.util.timeseries.yearoffset.ListYearOffsetDoubleTimeSeries;
import com.opengamma.util.timeseries.yearoffset.MutableYearOffsetDoubleTimeSeries;
import com.opengamma.util.timeseries.yearoffset.YearOffsetDoubleTimeSeries;
import com.opengamma.util.timeseries.zoneddatetime.ArrayZonedDateTimeDoubleTimeSeries;
import com.opengamma.util.timeseries.zoneddatetime.ListZonedDateTimeDoubleTimeSeries;
import com.opengamma.util.timeseries.zoneddatetime.MutableZonedDateTimeDoubleTimeSeries;
import com.opengamma.util.timeseries.zoneddatetime.ZonedDateTimeDoubleTimeSeries;

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
      return operate((FastIntDoubleTimeSeries) fastSeries, operator);  
    } else { // if (fastSeries instanceof FastLongDoubleTimeSeries
      return operate((FastLongDoubleTimeSeries) fastSeries, operator);
    }   
  }

  public abstract FastTimeSeries<T> unionOperate(final FastLongDoubleTimeSeries other, final BinaryOperator operator);
  
  public abstract FastTimeSeries<T> unionOperate(final FastIntDoubleTimeSeries other, final BinaryOperator operator);
  
  public FastTimeSeries<T> unionOperate(final FastBackedDoubleTimeSeries<?> other, final BinaryOperator operator) {
    FastTimeSeries<?> fastSeries = other.getFastSeries();
    if (fastSeries instanceof FastIntDoubleTimeSeries) {
      return unionOperate((FastIntDoubleTimeSeries) fastSeries, operator);  
    } else { // if (fastSeries instanceof FastLongDoubleTimeSeries
      return unionOperate((FastLongDoubleTimeSeries) fastSeries, operator);
    }   
  }

  public FastTimeSeries<T> add(DoubleTimeSeries<?> other) {
    if (other instanceof FastBackedDoubleTimeSeries<?>) {
      return operate((FastBackedDoubleTimeSeries<?>) other, ADD_OPERATOR);
    } else if (other instanceof FastIntDoubleTimeSeries) {
      return operate((FastIntDoubleTimeSeries) other, ADD_OPERATOR);
    } else { // if (other instanceof FastLongDoubleTimeSeries) {
      return operate((FastLongDoubleTimeSeries) other, ADD_OPERATOR);
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
  public FastTimeSeries<T> noIntersectionOperation(DoubleTimeSeries<?> other) {
    if (other instanceof FastBackedDoubleTimeSeries<?>) {
      return unionOperate((FastBackedDoubleTimeSeries<?>) other, NO_INTERSECTION_OPERATOR);
    } else if (other instanceof FastIntDoubleTimeSeries) {
      return unionOperate((FastIntDoubleTimeSeries) other, NO_INTERSECTION_OPERATOR);
    } else { // if (other instanceof FastLongDoubleTimeSeries) {
      return unionOperate((FastLongDoubleTimeSeries) other, NO_INTERSECTION_OPERATOR);
    }
  }
  public FastTimeSeries<T> noIntersectionOperation(FastBackedDoubleTimeSeries<?> other) {
    return unionOperate(other, NO_INTERSECTION_OPERATOR);
  }
  public FastTimeSeries<T> noIntersectionOperation(FastLongDoubleTimeSeries other) {
    return unionOperate(other, NO_INTERSECTION_OPERATOR);
  }
  public FastTimeSeries<T> noIntersectionOperation(FastIntDoubleTimeSeries other) {
    return unionOperate(other, NO_INTERSECTION_OPERATOR);
  }
  public FastTimeSeries<T> unionAdd(DoubleTimeSeries<?> other) {
    if (other instanceof FastBackedDoubleTimeSeries<?>) {
      return unionOperate((FastBackedDoubleTimeSeries<?>) other, ADD_OPERATOR);
    } else if (other instanceof FastIntDoubleTimeSeries) {
      return unionOperate((FastIntDoubleTimeSeries) other, ADD_OPERATOR);
    } else { // if (other instanceof FastLongDoubleTimeSeries) {
      return unionOperate((FastLongDoubleTimeSeries) other, ADD_OPERATOR);
    }
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
  public FastTimeSeries<T> subtract(DoubleTimeSeries<?> other) {
    if (other instanceof FastBackedDoubleTimeSeries<?>) {
      return operate((FastBackedDoubleTimeSeries<?>) other, SUBTRACT_OPERATOR);
    } else if (other instanceof FastIntDoubleTimeSeries) {
      return operate((FastIntDoubleTimeSeries) other, SUBTRACT_OPERATOR);
    } else { // if (other instanceof FastLongDoubleTimeSeries) {
      return operate((FastLongDoubleTimeSeries) other, SUBTRACT_OPERATOR);
    }
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
  public FastTimeSeries<T> unionSubtract(DoubleTimeSeries<?> other) {
    if (other instanceof FastBackedDoubleTimeSeries<?>) {
      return unionOperate((FastBackedDoubleTimeSeries<?>) other, SUBTRACT_OPERATOR);
    } else if (other instanceof FastIntDoubleTimeSeries) {
      return unionOperate((FastIntDoubleTimeSeries) other, SUBTRACT_OPERATOR);
    } else { // if (other instanceof FastLongDoubleTimeSeries) {
      return unionOperate((FastLongDoubleTimeSeries) other, SUBTRACT_OPERATOR);
    }
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
  public FastTimeSeries<T> multiply(DoubleTimeSeries<?> other) {
    if (other instanceof FastBackedDoubleTimeSeries<?>) {
      return operate((FastBackedDoubleTimeSeries<?>) other, MULTIPLY_OPERATOR);
    } else if (other instanceof FastIntDoubleTimeSeries) {
      return operate((FastIntDoubleTimeSeries) other, MULTIPLY_OPERATOR);
    } else { // if (other instanceof FastLongDoubleTimeSeries) {
      return operate((FastLongDoubleTimeSeries) other, MULTIPLY_OPERATOR);
    }
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
  public FastTimeSeries<T> unionMultiply(DoubleTimeSeries<?> other) {
    if (other instanceof FastBackedDoubleTimeSeries<?>) {
      return unionOperate((FastBackedDoubleTimeSeries<?>) other, MULTIPLY_OPERATOR);
    } else if (other instanceof FastIntDoubleTimeSeries) {
      return unionOperate((FastIntDoubleTimeSeries) other, MULTIPLY_OPERATOR);
    } else { // if (other instanceof FastLongDoubleTimeSeries) {
      return unionOperate((FastLongDoubleTimeSeries) other, MULTIPLY_OPERATOR);
    }
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
  public FastTimeSeries<T> divide(DoubleTimeSeries<?> other) {
    if (other instanceof FastBackedDoubleTimeSeries<?>) {
      return operate((FastBackedDoubleTimeSeries<?>) other, DIVIDE_OPERATOR);
    } else if (other instanceof FastIntDoubleTimeSeries) {
      return operate((FastIntDoubleTimeSeries) other, DIVIDE_OPERATOR);
    } else { // if (other instanceof FastLongDoubleTimeSeries) {
      return operate((FastLongDoubleTimeSeries) other, DIVIDE_OPERATOR);
    }
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
  public FastTimeSeries<T> unionDivide(DoubleTimeSeries<?> other) {
    if (other instanceof FastBackedDoubleTimeSeries<?>) {
      return unionOperate((FastBackedDoubleTimeSeries<?>) other, DIVIDE_OPERATOR);
    } else if (other instanceof FastIntDoubleTimeSeries) {
      return unionOperate((FastIntDoubleTimeSeries) other, DIVIDE_OPERATOR);
    } else { // if (other instanceof FastLongDoubleTimeSeries) {
      return unionOperate((FastLongDoubleTimeSeries) other, DIVIDE_OPERATOR);
    }
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
  public FastTimeSeries<T> power(DoubleTimeSeries<?> other) {
    if (other instanceof FastBackedDoubleTimeSeries<?>) {
      return operate((FastBackedDoubleTimeSeries<?>) other, POWER_OPERATOR);
    } else if (other instanceof FastIntDoubleTimeSeries) {
      return operate((FastIntDoubleTimeSeries) other, POWER_OPERATOR);
    } else { // if (other instanceof FastLongDoubleTimeSeries) {
      return operate((FastLongDoubleTimeSeries) other, POWER_OPERATOR);
    }
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
  public FastTimeSeries<T> unionPower(DoubleTimeSeries<?> other) {
    if (other instanceof FastBackedDoubleTimeSeries<?>) {
      return unionOperate((FastBackedDoubleTimeSeries<?>) other, POWER_OPERATOR);
    } else if (other instanceof FastIntDoubleTimeSeries) {
      return unionOperate((FastIntDoubleTimeSeries) other, POWER_OPERATOR);
    } else { // if (other instanceof FastLongDoubleTimeSeries) {
      return unionOperate((FastLongDoubleTimeSeries) other, POWER_OPERATOR);
    }
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
  public FastTimeSeries<T> minimum(DoubleTimeSeries<?> other) {
    if (other instanceof FastBackedDoubleTimeSeries<?>) {
      return operate((FastBackedDoubleTimeSeries<?>) other, MINIMUM_OPERATOR);
    } else if (other instanceof FastIntDoubleTimeSeries) {
      return operate((FastIntDoubleTimeSeries) other, MINIMUM_OPERATOR);
    } else { // if (other instanceof FastLongDoubleTimeSeries) {
      return operate((FastLongDoubleTimeSeries) other, MINIMUM_OPERATOR);
    }
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
  public FastTimeSeries<T> unionMinimum(DoubleTimeSeries<?> other) {
    if (other instanceof FastBackedDoubleTimeSeries<?>) {
      return unionOperate((FastBackedDoubleTimeSeries<?>) other, MINIMUM_OPERATOR);
    } else if (other instanceof FastIntDoubleTimeSeries) {
      return unionOperate((FastIntDoubleTimeSeries) other, MINIMUM_OPERATOR);
    } else { // if (other instanceof FastLongDoubleTimeSeries) {
      return unionOperate((FastLongDoubleTimeSeries) other, MINIMUM_OPERATOR);
    }
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
  public FastTimeSeries<T> maximum(DoubleTimeSeries<?> other) {
    if (other instanceof FastBackedDoubleTimeSeries<?>) {
      return operate((FastBackedDoubleTimeSeries<?>) other, MAXIMUM_OPERATOR);
    } else if (other instanceof FastIntDoubleTimeSeries) {
      return operate((FastIntDoubleTimeSeries) other, MAXIMUM_OPERATOR);
    } else { // if (other instanceof FastLongDoubleTimeSeries) {
      return operate((FastLongDoubleTimeSeries) other, MAXIMUM_OPERATOR);
    }
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
  public FastTimeSeries<T> unionMaximum(DoubleTimeSeries<?> other) {
    if (other instanceof FastBackedDoubleTimeSeries<?>) {
      return unionOperate((FastBackedDoubleTimeSeries<?>) other, MAXIMUM_OPERATOR);
    } else if (other instanceof FastIntDoubleTimeSeries) {
      return unionOperate((FastIntDoubleTimeSeries) other, MAXIMUM_OPERATOR);
    } else { // if (other instanceof FastLongDoubleTimeSeries) {
      return unionOperate((FastLongDoubleTimeSeries) other, MAXIMUM_OPERATOR);
    }
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
  public FastTimeSeries<T> average(DoubleTimeSeries<?> other) {
    if (other instanceof FastBackedDoubleTimeSeries<?>) {
      return operate((FastBackedDoubleTimeSeries<?>) other, AVERAGE_OPERATOR);
    } else if (other instanceof FastIntDoubleTimeSeries) {
      return operate((FastIntDoubleTimeSeries) other, AVERAGE_OPERATOR);
    } else { // if (other instanceof FastLongDoubleTimeSeries) {
      return operate((FastLongDoubleTimeSeries) other, AVERAGE_OPERATOR);
    }
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
  public FastTimeSeries<T> unionAverage(DoubleTimeSeries<?> other) {
    if (other instanceof FastBackedDoubleTimeSeries<?>) {
      return unionOperate((FastBackedDoubleTimeSeries<?>) other, AVERAGE_OPERATOR);
    } else if (other instanceof FastIntDoubleTimeSeries) {
      return unionOperate((FastIntDoubleTimeSeries) other, AVERAGE_OPERATOR);
    } else { // if (other instanceof FastLongDoubleTimeSeries) {
      return unionOperate((FastLongDoubleTimeSeries) other, AVERAGE_OPERATOR);
    }
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
  public FastTimeSeries<T> intersectionFirstValue(DoubleTimeSeries<?> other) {
    if (other instanceof FastBackedDoubleTimeSeries<?>) {
      return operate((FastBackedDoubleTimeSeries<?>) other, FIRST_OPERATOR);
    } else if (other instanceof FastIntDoubleTimeSeries) {
      return operate((FastIntDoubleTimeSeries) other, FIRST_OPERATOR);
    } else { // if (other instanceof FastLongDoubleTimeSeries) {
      return operate((FastLongDoubleTimeSeries) other, FIRST_OPERATOR);
    }
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
  public FastTimeSeries<T> intersectionSecondValue(DoubleTimeSeries<?> other) {
    if (other instanceof FastBackedDoubleTimeSeries<?>) {
      return operate((FastBackedDoubleTimeSeries<?>) other, SECOND_OPERATOR);
    } else if (other instanceof FastIntDoubleTimeSeries) {
      return operate((FastIntDoubleTimeSeries) other, SECOND_OPERATOR);
    } else { // if (other instanceof FastLongDoubleTimeSeries) {
      return operate((FastLongDoubleTimeSeries) other, SECOND_OPERATOR);
    }
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
    for (int i = 0; i < values.length; i++) {
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
    for (int i = 0; i < values.length; i++) {
      if (values[i] < min) {
        min = values[i];
      }
    }
    return min;
  }
  
  public FastIntDoubleTimeSeries toFastIntDaysDTS() {
    return toFastIntDoubleTimeSeries(DateTimeNumericEncoding.DATE_EPOCH_DAYS);
  }
  
  public FastMutableIntDoubleTimeSeries toFastMutableIntDaysDTS() {
    return toFastMutableIntDoubleTimeSeries(DateTimeNumericEncoding.DATE_EPOCH_DAYS);
  }
  
  public FastLongDoubleTimeSeries toFastLongMillisDTS() {
    return toFastLongDoubleTimeSeries(DateTimeNumericEncoding.TIME_EPOCH_MILLIS);
  }
  
  public FastMutableLongDoubleTimeSeries toFastMutableLongMillisDTS() {
    return toFastMutableLongDoubleTimeSeries(DateTimeNumericEncoding.TIME_EPOCH_MILLIS);
  }
  
  @Override
  public LocalDateDoubleTimeSeries toLocalDateDoubleTimeSeries() {
    return new ArrayLocalDateDoubleTimeSeries(toFastIntDaysDTS());
  }

  @Override
  public LocalDateDoubleTimeSeries toLocalDateDoubleTimeSeries(javax.time.calendar.TimeZone timeZone) {
    return new ArrayLocalDateDoubleTimeSeries(timeZone, toFastIntDaysDTS());
  }    
  
  @Override
  public MutableLocalDateDoubleTimeSeries toMutableLocalDateDoubleTimeSeries() {
    return new ListLocalDateDoubleTimeSeries(toFastMutableIntDaysDTS());
  }

  @Override
  public MutableLocalDateDoubleTimeSeries toMutableLocalDateDoubleTimeSeries(javax.time.calendar.TimeZone timeZone) {
    return new ListLocalDateDoubleTimeSeries(timeZone, toFastMutableIntDaysDTS());
  }
  
  public MutableDateDoubleTimeSeries toMutableDateDoubleTimeSeries() {
    return new ListDateDoubleTimeSeries(toFastMutableIntDaysDTS()); 
  }
  
  public MutableDateDoubleTimeSeries toMutableDateDoubleTimeSeries(TimeZone timeZone) {
    return new ListDateDoubleTimeSeries(timeZone, toFastMutableIntDaysDTS());
  }
  
  public DateDoubleTimeSeries toDateDoubleTimeSeries() {
    return new ArrayDateDoubleTimeSeries(toFastIntDaysDTS());    
  }
  
  public DateDoubleTimeSeries toDateDoubleTimeSeries(TimeZone timeZone) {
    return new ArrayDateDoubleTimeSeries(timeZone, toFastIntDaysDTS());
  }
  
  @Override
  public SQLDateDoubleTimeSeries toSQLDateDoubleTimeSeries() {
    return new ArraySQLDateDoubleTimeSeries(toFastIntDaysDTS());
  }

  @Override
  public SQLDateDoubleTimeSeries toSQLDateDoubleTimeSeries(TimeZone timeZone) {
    return new ArraySQLDateDoubleTimeSeries(timeZone, toFastIntDaysDTS());
  }

  @Override
  public MutableSQLDateDoubleTimeSeries toMutableSQLDateDoubleTimeSeries() {
    return new ListSQLDateDoubleTimeSeries(toFastMutableIntDaysDTS());
  }

  @Override
  public MutableSQLDateDoubleTimeSeries toMutableSQLDateDoubleTimeSeries(TimeZone timeZone) {
    return new ListSQLDateDoubleTimeSeries(timeZone, toFastMutableIntDaysDTS());
  }
  
  public MutableDateTimeDoubleTimeSeries toMutableDateTimeDoubleTimeSeries() {
    return new ListDateTimeDoubleTimeSeries(this.toFastMutableLongMillisDTS()); 
  }
  
  public MutableDateTimeDoubleTimeSeries toMutableDateTimeDoubleTimeSeries(TimeZone timeZone) {
    return new ListDateTimeDoubleTimeSeries(timeZone, toFastMutableLongMillisDTS());
  }
  
  public DateTimeDoubleTimeSeries toDateTimeDoubleTimeSeries() {
    return new ArrayDateTimeDoubleTimeSeries(toFastLongMillisDTS()); 
  }
  
  public DateTimeDoubleTimeSeries toDateTimeDoubleTimeSeries(TimeZone timeZone) {
    return new ArrayDateTimeDoubleTimeSeries(timeZone, toFastLongMillisDTS());
  }
  
  @Override
  public ZonedDateTimeDoubleTimeSeries toZonedDateTimeDoubleTimeSeries() {
    return new ArrayZonedDateTimeDoubleTimeSeries(toFastLongMillisDTS());
  }
  
  @Override
  public ZonedDateTimeDoubleTimeSeries toZonedDateTimeDoubleTimeSeries(javax.time.calendar.TimeZone timeZone) {
    return new ArrayZonedDateTimeDoubleTimeSeries(timeZone, toFastLongMillisDTS());
  }

  @Override
  public MutableZonedDateTimeDoubleTimeSeries toMutableZonedDateTimeDoubleTimeSeries() {
    return new ListZonedDateTimeDoubleTimeSeries(toFastMutableLongMillisDTS());
  }
  
  @Override
  public MutableZonedDateTimeDoubleTimeSeries toMutableZonedDateTimeDoubleTimeSeries(javax.time.calendar.TimeZone timeZone) {
    return new ListZonedDateTimeDoubleTimeSeries(timeZone, toFastMutableLongMillisDTS());
  }
  
  @Override
  public YearOffsetDoubleTimeSeries toYearOffsetDoubleTimeSeries(ZonedDateTime zeroDate) {
    return new ArrayYearOffsetDoubleTimeSeries(zeroDate, toFastLongMillisDTS());
  }

  @Override
  public YearOffsetDoubleTimeSeries toYearOffsetDoubleTimeSeries(java.util.TimeZone timeZone, Date zeroDate) {
    return new ArrayYearOffsetDoubleTimeSeries(timeZone, zeroDate, toFastLongMillisDTS());
  }

  @Override
  public MutableYearOffsetDoubleTimeSeries toMutableYearOffsetDoubleTimeSeries(ZonedDateTime zeroDate) {
    return new ListYearOffsetDoubleTimeSeries(zeroDate, toFastMutableLongMillisDTS());
  }
  
  @Override
  public MutableYearOffsetDoubleTimeSeries toMutableYearOffsetDoubleTimeSeries(java.util.TimeZone timeZone, Date zeroDate) {
    return new ListYearOffsetDoubleTimeSeries(timeZone, zeroDate, toFastMutableLongMillisDTS());
  }
  
  @Override
  public String toString() {
    return ToStringHelper.toString(this);
  }

}
