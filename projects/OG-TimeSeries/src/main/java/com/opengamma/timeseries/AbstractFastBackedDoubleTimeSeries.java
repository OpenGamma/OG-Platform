/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
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
import static com.opengamma.timeseries.DoubleTimeSeriesOperators.NO_INTERSECTION_OPERATOR;
import static com.opengamma.timeseries.DoubleTimeSeriesOperators.POWER_OPERATOR;
import static com.opengamma.timeseries.DoubleTimeSeriesOperators.RECIPROCAL_OPERATOR;
import static com.opengamma.timeseries.DoubleTimeSeriesOperators.SECOND_OPERATOR;
import static com.opengamma.timeseries.DoubleTimeSeriesOperators.SUBTRACT_OPERATOR;

import java.lang.reflect.Array;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.TimeZone;

import org.threeten.bp.ZoneId;

import com.opengamma.timeseries.DoubleTimeSeriesOperators.BinaryOperator;
import com.opengamma.timeseries.DoubleTimeSeriesOperators.UnaryOperator;
import com.opengamma.timeseries.fast.DateTimeNumericEncoding;
import com.opengamma.timeseries.fast.FastTimeSeries;
import com.opengamma.timeseries.fast.integer.FastIntDoubleTimeSeries;
import com.opengamma.timeseries.fast.integer.FastMutableIntDoubleTimeSeries;
import com.opengamma.timeseries.fast.longint.FastLongDoubleTimeSeries;
import com.opengamma.timeseries.fast.longint.FastMutableLongDoubleTimeSeries;
import com.opengamma.timeseries.localdate.ArrayLocalDateDoubleTimeSeries;
import com.opengamma.timeseries.localdate.ListLocalDateDoubleTimeSeries;
import com.opengamma.timeseries.localdate.LocalDateDoubleTimeSeries;
import com.opengamma.timeseries.localdate.MutableLocalDateDoubleTimeSeries;
import com.opengamma.timeseries.zoneddatetime.ArrayZonedDateTimeDoubleTimeSeries;
import com.opengamma.timeseries.zoneddatetime.ListZonedDateTimeDoubleTimeSeries;
import com.opengamma.timeseries.zoneddatetime.MutableZonedDateTimeDoubleTimeSeries;
import com.opengamma.timeseries.zoneddatetime.ZonedDateTimeDoubleTimeSeries;

/**
 * @param <DATE_TYPE> Type of the dates
 */
public abstract class AbstractFastBackedDoubleTimeSeries<DATE_TYPE>
    implements DoubleTimeSeries<DATE_TYPE>, FastBackedDoubleTimeSeries<DATE_TYPE> {

  /** Serialization version. */
  private static final long serialVersionUID = 2676866983197027948L;

  @Override
  public abstract DateTimeConverter<DATE_TYPE> getConverter();

  @Override
  public abstract FastTimeSeries<?> getFastSeries();

  @SuppressWarnings("unchecked")
  @Override
  public FastBackedDoubleTimeSeries<DATE_TYPE> lag(final int days) {
    final DATE_TYPE[] times = timesArray();
    final Double[] values = valuesArray();
    if (days == 0) {
      // REVIEW Andrew 2013-01-24 -- why not just return "this" ?
      return (FastBackedDoubleTimeSeries<DATE_TYPE>) newInstance(times, values);
    } else {
      final Class<?> dateType = times.getClass().getComponentType();
      if (days < 0) {
        if (-days < times.length) {
          final DATE_TYPE[] resultTimes = (DATE_TYPE[]) Array.newInstance(dateType, times.length + days); // remember days is -ve
          System.arraycopy(times, 0, resultTimes, 0, times.length + days);
          final Double[] resultValues = new Double[times.length + days];
          System.arraycopy(values, -days, resultValues, 0, times.length + days);
          return (FastBackedDoubleTimeSeries<DATE_TYPE>) newInstance(resultTimes, resultValues);
        }
      } else { // if (days > 0) {
        if (days < times.length) {
          final DATE_TYPE[] resultTimes = (DATE_TYPE[]) Array.newInstance(dateType, times.length - days); // remember days is +ve
          System.arraycopy(times, days, resultTimes, 0, times.length - days);
          final Double[] resultValues = new Double[times.length - days];
          System.arraycopy(values, 0, resultValues, 0, times.length - days);
          return (FastBackedDoubleTimeSeries<DATE_TYPE>) newInstance(resultTimes, resultValues);
        }
      }
      return (FastBackedDoubleTimeSeries<DATE_TYPE>) newInstance((DATE_TYPE[]) Array.newInstance(dateType, 0), new Double[0]);
    }
  }

  //-------------------------------------------------------------------------
  @Override
  public abstract FastBackedDoubleTimeSeries<DATE_TYPE> operate(final FastTimeSeries<?> other, final BinaryOperator operator);

  @Override
  public abstract FastBackedDoubleTimeSeries<DATE_TYPE> operate(final FastBackedDoubleTimeSeries<?> other, final BinaryOperator operator);

  @Override
  public abstract FastBackedDoubleTimeSeries<DATE_TYPE> operate(final double other, final BinaryOperator operator);

  @Override
  public abstract FastBackedDoubleTimeSeries<DATE_TYPE> operate(final UnaryOperator operator);

  @Override
  public abstract FastBackedDoubleTimeSeries<DATE_TYPE> unionOperate(final FastTimeSeries<?> other, final BinaryOperator operator);

  @Override
  public abstract FastBackedDoubleTimeSeries<DATE_TYPE> unionOperate(final FastBackedDoubleTimeSeries<?> other, final BinaryOperator operator);

  //-------------------------------------------------------------------------
  public FastBackedDoubleTimeSeries<DATE_TYPE> add(final double other) {
    return operate(other, ADD_OPERATOR);
  }

  @Override
  public FastBackedDoubleTimeSeries<DATE_TYPE> add(final DoubleTimeSeries<?> other) {
    if (other instanceof FastBackedDoubleTimeSeries<?>) {
      return operate((FastBackedDoubleTimeSeries<?>) other, ADD_OPERATOR);
    } else if (other instanceof FastIntDoubleTimeSeries) {
      return operate((FastIntDoubleTimeSeries) other, ADD_OPERATOR);
    } else { // if (other instanceof FastLongDoubleTimeSeries) {
      return operate((FastLongDoubleTimeSeries) other, ADD_OPERATOR);
    }
  }

  @Override
  public FastBackedDoubleTimeSeries<DATE_TYPE> unionAdd(final DoubleTimeSeries<?> other) {
    if (other instanceof FastBackedDoubleTimeSeries<?>) {
      return unionOperate((FastBackedDoubleTimeSeries<?>) other, ADD_OPERATOR);
    } else if (other instanceof FastIntDoubleTimeSeries) {
      return unionOperate((FastIntDoubleTimeSeries) other, ADD_OPERATOR);
    } else { // if (other instanceof FastLongDoubleTimeSeries) {
      return unionOperate((FastLongDoubleTimeSeries) other, ADD_OPERATOR);
    }
  }

  //-------------------------------------------------------------------------
  @Override
  public FastBackedDoubleTimeSeries<DATE_TYPE> subtract(final double other) {
    return operate(other, SUBTRACT_OPERATOR);
  }

  @Override
  public FastBackedDoubleTimeSeries<DATE_TYPE> subtract(final DoubleTimeSeries<?> other) {
    if (other instanceof FastBackedDoubleTimeSeries<?>) {
      return operate((FastBackedDoubleTimeSeries<?>) other, SUBTRACT_OPERATOR);
    } else if (other instanceof FastIntDoubleTimeSeries) {
      return operate((FastIntDoubleTimeSeries) other, SUBTRACT_OPERATOR);
    } else { // if (other instanceof FastLongDoubleTimeSeries) {
      return operate((FastLongDoubleTimeSeries) other, SUBTRACT_OPERATOR);
    }
  }

  @Override
  public FastBackedDoubleTimeSeries<DATE_TYPE> unionSubtract(final DoubleTimeSeries<?> other) {
    if (other instanceof FastBackedDoubleTimeSeries<?>) {
      return unionOperate((FastBackedDoubleTimeSeries<?>) other, SUBTRACT_OPERATOR);
    } else if (other instanceof FastIntDoubleTimeSeries) {
      return unionOperate((FastIntDoubleTimeSeries) other, SUBTRACT_OPERATOR);
    } else { // if (other instanceof FastLongDoubleTimeSeries) {
      return unionOperate((FastLongDoubleTimeSeries) other, SUBTRACT_OPERATOR);
    }
  }

  //-------------------------------------------------------------------------
  @Override
  public FastBackedDoubleTimeSeries<DATE_TYPE> multiply(final double other) {
    return operate(other, MULTIPLY_OPERATOR);
  }

  @Override
  public FastBackedDoubleTimeSeries<DATE_TYPE> multiply(final DoubleTimeSeries<?> other) {
    if (other instanceof FastBackedDoubleTimeSeries<?>) {
      return operate((FastBackedDoubleTimeSeries<?>) other, MULTIPLY_OPERATOR);
    } else if (other instanceof FastIntDoubleTimeSeries) {
      return operate((FastIntDoubleTimeSeries) other, MULTIPLY_OPERATOR);
    } else { // if (other instanceof FastLongDoubleTimeSeries) {
      return operate((FastLongDoubleTimeSeries) other, MULTIPLY_OPERATOR);
    }
  }

  @Override
  public FastBackedDoubleTimeSeries<DATE_TYPE> unionMultiply(final DoubleTimeSeries<?> other) {
    if (other instanceof FastBackedDoubleTimeSeries<?>) {
      return unionOperate((FastBackedDoubleTimeSeries<?>) other, MULTIPLY_OPERATOR);
    } else if (other instanceof FastIntDoubleTimeSeries) {
      return unionOperate((FastIntDoubleTimeSeries) other, MULTIPLY_OPERATOR);
    } else { // if (other instanceof FastLongDoubleTimeSeries) {
      return unionOperate((FastLongDoubleTimeSeries) other, MULTIPLY_OPERATOR);
    }
  }

  //-------------------------------------------------------------------------
  @Override
  public FastBackedDoubleTimeSeries<DATE_TYPE> divide(final double other) {
    return operate(other, DIVIDE_OPERATOR);
  }

  @Override
  public FastBackedDoubleTimeSeries<DATE_TYPE> divide(final DoubleTimeSeries<?> other) {
    if (other instanceof FastBackedDoubleTimeSeries<?>) {
      return operate((FastBackedDoubleTimeSeries<?>) other, DIVIDE_OPERATOR);
    } else if (other instanceof FastIntDoubleTimeSeries) {
      return operate((FastIntDoubleTimeSeries) other, DIVIDE_OPERATOR);
    } else { // if (other instanceof FastLongDoubleTimeSeries) {
      return operate((FastLongDoubleTimeSeries) other, DIVIDE_OPERATOR);
    }
  }

  @Override
  public FastBackedDoubleTimeSeries<DATE_TYPE> unionDivide(final DoubleTimeSeries<?> other) {
    if (other instanceof FastBackedDoubleTimeSeries<?>) {
      return unionOperate((FastBackedDoubleTimeSeries<?>) other, DIVIDE_OPERATOR);
    } else if (other instanceof FastIntDoubleTimeSeries) {
      return unionOperate((FastIntDoubleTimeSeries) other, DIVIDE_OPERATOR);
    } else { // if (other instanceof FastLongDoubleTimeSeries) {
      return unionOperate((FastLongDoubleTimeSeries) other, DIVIDE_OPERATOR);
    }
  }

  //-------------------------------------------------------------------------
  @Override
  public FastBackedDoubleTimeSeries<DATE_TYPE> power(final double other) {
    return operate(other, POWER_OPERATOR);
  }

  @Override
  public FastBackedDoubleTimeSeries<DATE_TYPE> power(final DoubleTimeSeries<?> other) {
    if (other instanceof FastBackedDoubleTimeSeries<?>) {
      return operate((FastBackedDoubleTimeSeries<?>) other, POWER_OPERATOR);
    } else if (other instanceof FastIntDoubleTimeSeries) {
      return operate((FastIntDoubleTimeSeries) other, POWER_OPERATOR);
    } else { // if (other instanceof FastLongDoubleTimeSeries) {
      return operate((FastLongDoubleTimeSeries) other, POWER_OPERATOR);
    }
  }

  @Override
  public FastBackedDoubleTimeSeries<DATE_TYPE> unionPower(final DoubleTimeSeries<?> other) {
    if (other instanceof FastBackedDoubleTimeSeries<?>) {
      return unionOperate((FastBackedDoubleTimeSeries<?>) other, POWER_OPERATOR);
    } else if (other instanceof FastIntDoubleTimeSeries) {
      return unionOperate((FastIntDoubleTimeSeries) other, POWER_OPERATOR);
    } else { // if (other instanceof FastLongDoubleTimeSeries) {
      return unionOperate((FastLongDoubleTimeSeries) other, POWER_OPERATOR);
    }
  }

  //-------------------------------------------------------------------------
  @Override
  public FastBackedDoubleTimeSeries<DATE_TYPE> minimum(final double other) {
    return operate(other, MINIMUM_OPERATOR);
  }

  @Override
  public FastBackedDoubleTimeSeries<DATE_TYPE> minimum(final DoubleTimeSeries<?> other) {
    if (other instanceof FastBackedDoubleTimeSeries<?>) {
      return operate((FastBackedDoubleTimeSeries<?>) other, MINIMUM_OPERATOR);
    } else if (other instanceof FastIntDoubleTimeSeries) {
      return operate((FastIntDoubleTimeSeries) other, MINIMUM_OPERATOR);
    } else { // if (other instanceof FastLongDoubleTimeSeries) {
      return operate((FastLongDoubleTimeSeries) other, MINIMUM_OPERATOR);
    }
  }

  @Override
  public FastBackedDoubleTimeSeries<DATE_TYPE> unionMinimum(final DoubleTimeSeries<?> other) {
    if (other instanceof FastBackedDoubleTimeSeries<?>) {
      return unionOperate((FastBackedDoubleTimeSeries<?>) other, MINIMUM_OPERATOR);
    } else if (other instanceof FastIntDoubleTimeSeries) {
      return unionOperate((FastIntDoubleTimeSeries) other, MINIMUM_OPERATOR);
    } else { // if (other instanceof FastLongDoubleTimeSeries) {
      return unionOperate((FastLongDoubleTimeSeries) other, MINIMUM_OPERATOR);
    }
  }

  //-------------------------------------------------------------------------
  @Override
  public FastBackedDoubleTimeSeries<DATE_TYPE> maximum(final double other) {
    return operate(other, MAXIMUM_OPERATOR);
  }

  @Override
  public FastBackedDoubleTimeSeries<DATE_TYPE> maximum(final DoubleTimeSeries<?> other) {
    if (other instanceof FastBackedDoubleTimeSeries<?>) {
      return operate((FastBackedDoubleTimeSeries<?>) other, MAXIMUM_OPERATOR);
    } else if (other instanceof FastIntDoubleTimeSeries) {
      return operate((FastIntDoubleTimeSeries) other, MAXIMUM_OPERATOR);
    } else { // if (other instanceof FastLongDoubleTimeSeries) {
      return operate((FastLongDoubleTimeSeries) other, MAXIMUM_OPERATOR);
    }
  }

  @Override
  public FastBackedDoubleTimeSeries<DATE_TYPE> unionMaximum(final DoubleTimeSeries<?> other) {
    if (other instanceof FastBackedDoubleTimeSeries<?>) {
      return unionOperate((FastBackedDoubleTimeSeries<?>) other, MAXIMUM_OPERATOR);
    } else if (other instanceof FastIntDoubleTimeSeries) {
      return unionOperate((FastIntDoubleTimeSeries) other, MAXIMUM_OPERATOR);
    } else { // if (other instanceof FastLongDoubleTimeSeries) {
      return unionOperate((FastLongDoubleTimeSeries) other, MAXIMUM_OPERATOR);
    }
  }

  //-------------------------------------------------------------------------
  @Override
  public FastBackedDoubleTimeSeries<DATE_TYPE> average(final double other) {
    return operate(other, AVERAGE_OPERATOR);
  }

  @Override
  public FastBackedDoubleTimeSeries<DATE_TYPE> average(final DoubleTimeSeries<?> other) {
    if (other instanceof FastBackedDoubleTimeSeries<?>) {
      return operate((FastBackedDoubleTimeSeries<?>) other, AVERAGE_OPERATOR);
    } else if (other instanceof FastIntDoubleTimeSeries) {
      return operate((FastIntDoubleTimeSeries) other, AVERAGE_OPERATOR);
    } else { // if (other instanceof FastLongDoubleTimeSeries) {
      return operate((FastLongDoubleTimeSeries) other, AVERAGE_OPERATOR);
    }
  }

  @Override
  public FastBackedDoubleTimeSeries<DATE_TYPE> unionAverage(final DoubleTimeSeries<?> other) {
    if (other instanceof FastBackedDoubleTimeSeries<?>) {
      return unionOperate((FastBackedDoubleTimeSeries<?>) other, AVERAGE_OPERATOR);
    } else if (other instanceof FastIntDoubleTimeSeries) {
      return unionOperate((FastIntDoubleTimeSeries) other, AVERAGE_OPERATOR);
    } else { // if (other instanceof FastLongDoubleTimeSeries) {
      return unionOperate((FastLongDoubleTimeSeries) other, AVERAGE_OPERATOR);
    }
  }

  //-------------------------------------------------------------------------
  @Override
  public FastBackedDoubleTimeSeries<DATE_TYPE> noIntersectionOperation(final DoubleTimeSeries<?> other) {
    if (other instanceof FastBackedDoubleTimeSeries<?>) {
      return unionOperate((FastBackedDoubleTimeSeries<?>) other, NO_INTERSECTION_OPERATOR);
    } else if (other instanceof FastIntDoubleTimeSeries) {
      return unionOperate((FastIntDoubleTimeSeries) other, NO_INTERSECTION_OPERATOR);
    } else { // if (other instanceof FastLongDoubleTimeSeries) {
      return unionOperate((FastLongDoubleTimeSeries) other, NO_INTERSECTION_OPERATOR);
    }
  }

  @Override
  public FastBackedDoubleTimeSeries<DATE_TYPE> intersectionFirstValue(final DoubleTimeSeries<?> other) {
    if (other instanceof FastBackedDoubleTimeSeries<?>) {
      return operate((FastBackedDoubleTimeSeries<?>) other, FIRST_OPERATOR);
    } else if (other instanceof FastIntDoubleTimeSeries) {
      return operate((FastIntDoubleTimeSeries) other, FIRST_OPERATOR);
    } else { // if (other instanceof FastLongDoubleTimeSeries) {
      //PLAT-1590 : this one is optimized for some types
      return intersectionFirstValueFast((FastLongDoubleTimeSeries) other);
    }
  }

  protected FastBackedDoubleTimeSeries<DATE_TYPE> intersectionFirstValueFast(final FastLongDoubleTimeSeries other) {
    return operate(other, FIRST_OPERATOR);
  }

  @Override
  public FastBackedDoubleTimeSeries<DATE_TYPE> intersectionSecondValue(final DoubleTimeSeries<?> other) {
    if (other instanceof FastBackedDoubleTimeSeries<?>) {
      return operate((FastBackedDoubleTimeSeries<?>) other, SECOND_OPERATOR);
    } else if (other instanceof FastIntDoubleTimeSeries) {
      return operate((FastIntDoubleTimeSeries) other, SECOND_OPERATOR);
    } else { // if (other instanceof FastLongDoubleTimeSeries) {
      return operate((FastLongDoubleTimeSeries) other, SECOND_OPERATOR);
    }
  }

  //-------------------------------------------------------------------------
  @Override
  public FastBackedDoubleTimeSeries<DATE_TYPE> negate() {
    return operate(NEGATE_OPERATOR);
  }

  @Override
  public FastBackedDoubleTimeSeries<DATE_TYPE> reciprocal() {
    return operate(RECIPROCAL_OPERATOR);
  }

  @Override
  public FastBackedDoubleTimeSeries<DATE_TYPE> log() {
    return operate(LOG_OPERATOR);
  }

  @Override
  public FastBackedDoubleTimeSeries<DATE_TYPE> log10() {
    return operate(LOG10_OPERATOR);
  }

  @Override
  public FastBackedDoubleTimeSeries<DATE_TYPE> abs() {
    return operate(ABS_OPERATOR);
  }

  @Override
  public double maxValue() {
    if (isEmpty()) {
      throw new NoSuchElementException("TimeSeries is empty");
    }
    final double[] values = valuesArrayFast();
    double max = Double.MIN_VALUE;
    for (int i = 0; i < values.length; i++) {
      if (values[i] > max) {
        max = values[i];
      }
    }
    return max;
  }

  @Override
  public double minValue() {
    if (isEmpty()) {
      throw new NoSuchElementException("TimeSeries is empty");
    }
    final double[] values = valuesArrayFast();
    double min = Double.MAX_VALUE;
    for (int i = 0; i < values.length; i++) {
      if (values[i] < min) {
        min = values[i];
      }
    }
    return min;
  }

  //-------------------------------------------------------------------------
  @Override
  public Iterator<Double> valuesIterator() {
    return getFastSeries().valuesIterator();
  }

  @Override
  public List<Double> values() {
    return getFastSeries().values();
  }

  @Override
  public Double[] valuesArray() {
    return getFastSeries().valuesArray();
  }

  @Override
  public double[] valuesArrayFast() {
    return getFastSeries().valuesArrayFast();
  }

  //-------------------------------------------------------------------------
  public ZoneId getTimeZone310() {
    return getConverter().getTimeZone310();
  }

  public TimeZone getTimeZone() {
    return getConverter().getTimeZone();
  }

  public FastIntDoubleTimeSeries toFastIntDaysDTS() {
    return getFastSeries().toFastIntDoubleTimeSeries(DateTimeNumericEncoding.DATE_EPOCH_DAYS);
  }

  public FastMutableIntDoubleTimeSeries toFastMutableIntDaysDTS() {
    return getFastSeries().toFastMutableIntDoubleTimeSeries(DateTimeNumericEncoding.DATE_EPOCH_DAYS);
  }

  public FastLongDoubleTimeSeries toFastLongMillisDTS() {
    return getFastSeries().toFastLongDoubleTimeSeries(DateTimeNumericEncoding.TIME_EPOCH_MILLIS);
  }

  public FastMutableLongDoubleTimeSeries toFastMutableLongMillisDTS() {
    return getFastSeries().toFastMutableLongDoubleTimeSeries(DateTimeNumericEncoding.TIME_EPOCH_MILLIS);
  }

  @Override
  public LocalDateDoubleTimeSeries toLocalDateDoubleTimeSeries() {
    return new ArrayLocalDateDoubleTimeSeries(getTimeZone310(), toFastIntDaysDTS());
  }

  @Override
  public LocalDateDoubleTimeSeries toLocalDateDoubleTimeSeries(final ZoneId timeZone) {
    return new ArrayLocalDateDoubleTimeSeries(timeZone, toFastIntDaysDTS());
  }

  @Override
  public MutableLocalDateDoubleTimeSeries toMutableLocalDateDoubleTimeSeries() {
    return new ListLocalDateDoubleTimeSeries(getTimeZone310(), toFastMutableIntDaysDTS());
  }

  @Override
  public MutableLocalDateDoubleTimeSeries toMutableLocalDateDoubleTimeSeries(final ZoneId timeZone) {
    return new ListLocalDateDoubleTimeSeries(timeZone, toFastMutableIntDaysDTS());
  }

  @Override
  public ZonedDateTimeDoubleTimeSeries toZonedDateTimeDoubleTimeSeries() {
    return new ArrayZonedDateTimeDoubleTimeSeries(getTimeZone310(), toFastLongMillisDTS());
  }

  @Override
  public ZonedDateTimeDoubleTimeSeries toZonedDateTimeDoubleTimeSeries(final ZoneId timeZone) {
    return new ArrayZonedDateTimeDoubleTimeSeries(timeZone, toFastLongMillisDTS());
  }

  @Override
  public MutableZonedDateTimeDoubleTimeSeries toMutableZonedDateTimeDoubleTimeSeries() {
    return new ListZonedDateTimeDoubleTimeSeries(getTimeZone310(), toFastMutableLongMillisDTS());
  }

  @Override
  public MutableZonedDateTimeDoubleTimeSeries toMutableZonedDateTimeDoubleTimeSeries(final ZoneId timeZone) {
    return new ListZonedDateTimeDoubleTimeSeries(timeZone, toFastMutableLongMillisDTS());
  }

  @Override
  public FastIntDoubleTimeSeries toFastIntDoubleTimeSeries() {
    return getFastSeries().toFastIntDoubleTimeSeries();
  }

  @Override
  public FastIntDoubleTimeSeries toFastIntDoubleTimeSeries(
      final DateTimeNumericEncoding encoding) {
    return getFastSeries().toFastIntDoubleTimeSeries(encoding);
  }

  @Override
  public FastLongDoubleTimeSeries toFastLongDoubleTimeSeries() {
    return getFastSeries().toFastLongDoubleTimeSeries();
  }

  @Override
  public FastLongDoubleTimeSeries toFastLongDoubleTimeSeries(
      final DateTimeNumericEncoding encoding) {
    return getFastSeries().toFastLongDoubleTimeSeries(encoding);
  }

  @Override
  public FastMutableIntDoubleTimeSeries toFastMutableIntDoubleTimeSeries() {
    return getFastSeries().toFastMutableIntDoubleTimeSeries();
  }

  @Override
  public FastMutableIntDoubleTimeSeries toFastMutableIntDoubleTimeSeries(
      final DateTimeNumericEncoding encoding) {
    return getFastSeries().toFastMutableIntDoubleTimeSeries(encoding);
  }

  @Override
  public FastMutableLongDoubleTimeSeries toFastMutableLongDoubleTimeSeries() {
    return getFastSeries().toFastMutableLongDoubleTimeSeries();
  }

  @Override
  public FastMutableLongDoubleTimeSeries toFastMutableLongDoubleTimeSeries(
      final DateTimeNumericEncoding encoding) {
    return getFastSeries().toFastMutableLongDoubleTimeSeries(encoding);
  }
}
