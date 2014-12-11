/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.timeseries.date.localdate;

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

import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map.Entry;
import java.util.NoSuchElementException;

import org.threeten.bp.LocalDate;

import com.opengamma.timeseries.DoubleTimeSeries;
import com.opengamma.timeseries.DoubleTimeSeriesOperators.BinaryOperator;
import com.opengamma.timeseries.date.AbstractDateDoubleTimeSeries;
import com.opengamma.timeseries.date.DateDoubleTimeSeries;

/**
 * Abstract implementation of {@code LocalDateDoubleTimeSeries}.
 */
abstract class AbstractLocalDateDoubleTimeSeries
    extends AbstractDateDoubleTimeSeries<LocalDate>
    implements LocalDateDoubleTimeSeries {

  /**
   * Creates an instance.
   */
  public AbstractLocalDateDoubleTimeSeries() {
  }

  //-------------------------------------------------------------------------
  static int[] convertToIntArray(Collection<LocalDate> times) {
    int[] timesArray = new int[times.size()];
    int i = 0;
    for (LocalDate time : times) {
      timesArray[i++] = LocalDateToIntConverter.convertToInt(time);
    }
    return timesArray;
  }

  static int[] convertToIntArray(LocalDate[] dates) {
    int[] timesArray = new int[dates.length];
    for (int i = 0; i < timesArray.length; i++) {
      timesArray[i] = LocalDateToIntConverter.convertToInt(dates[i]);
    }
    return timesArray;
  }

  static double[] convertToDoubleArray(Collection<Double> values) {
    double[] valuesArray = new double[values.size()];
    int i = 0;
    for (Double value : values) {
      valuesArray[i++] = value;
    }
    return valuesArray;
  }

  static double[] convertToDoubleArray(Double[] values) {
    double[] valuesArray = new double[values.length];
    for (int i = 0; i < valuesArray.length; i++) {
      valuesArray[i] = values[i];
    }
    return valuesArray;
  }

  static Entry<LocalDate, Double> makeMapEntry(LocalDate key, Double value) {
    return new SimpleImmutableEntry<LocalDate, Double>(key, value);
  }

  //-------------------------------------------------------------------------
  @Override
  protected int convertToInt(LocalDate date) {
    return LocalDateToIntConverter.convertToInt(date);
  }

  @Override
  protected LocalDate convertFromInt(int date) {
    return LocalDateToIntConverter.convertToLocalDate(date);
  }

  @Override
  protected LocalDate[] createArray(int size) {
    return new LocalDate[size];
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the internal storage array without cloning.
   * 
   * @return the array, not null
   */
  abstract int[] timesArrayFast0();

  /**
   * Gets the internal storage array without cloning.
   * 
   * @return the array, not null
   */
  abstract double[] valuesArrayFast0();

  /**
   * Creates a new instance without cloning.
   * 
   * @param times  the times array, not null
   * @param values  the values array, not null
   * @return the new instance, not null
   */
  abstract LocalDateDoubleTimeSeries newInstanceFast(int[] times, double[] values);

  //-------------------------------------------------------------------------
  @Override
  public LocalDateDoubleEntryIterator iterator() {
    return new LocalDateDoubleEntryIterator() {
      private int _index = -1;

      @Override
      public boolean hasNext() {
        return (_index + 1) < size();
      }

      @Override
      public Entry<LocalDate, Double> next() {
        if (hasNext() == false) {
          throw new NoSuchElementException("No more elements in the iteration");
        }
        _index++;
        int date = AbstractLocalDateDoubleTimeSeries.this.getTimeAtIndexFast(_index);
        Double value = AbstractLocalDateDoubleTimeSeries.this.getValueAtIndex(_index);
        return makeMapEntry(AbstractLocalDateDoubleTimeSeries.this.convertFromInt(date), value);
      }

      @Override
      public int nextTimeFast() {
        if (hasNext() == false) {
          throw new NoSuchElementException("No more elements in the iteration");
        }
        _index++;
        return AbstractLocalDateDoubleTimeSeries.this.getTimeAtIndexFast(_index);
      }

      @Override
      public LocalDate nextTime() {
        return AbstractLocalDateDoubleTimeSeries.this.convertFromInt(nextTimeFast());
      }

      @Override
      public int currentTimeFast() {
        if (_index < 0) {
          throw new IllegalStateException("Iterator has not yet been started");
        }
        return AbstractLocalDateDoubleTimeSeries.this.getTimeAtIndexFast(_index);
      }

      @Override
      public LocalDate currentTime() {
        return AbstractLocalDateDoubleTimeSeries.this.convertFromInt(currentTimeFast());
      }

      @Override
      public Double currentValue() {
        if (_index < 0) {
          throw new IllegalStateException("Iterator has not yet been started");
        }
        return AbstractLocalDateDoubleTimeSeries.this.getValueAtIndex(_index);
      }

      @Override
      public double currentValueFast() {
        if (_index < 0) {
          throw new IllegalStateException("Iterator has not yet been started");
        }
        return AbstractLocalDateDoubleTimeSeries.this.getValueAtIndexFast(_index);
      }

      @Override
      public int currentIndex() {
        return _index;
      }

      @Override
      public void remove() {
        throw new UnsupportedOperationException("Immutable iterator");
      }
    };
  }

  //-------------------------------------------------------------------------
  @Override
  public LocalDateDoubleTimeSeries subSeries(LocalDate startTime, LocalDate endTime) {
    return subSeriesFast(convertToInt(startTime), true, convertToInt(endTime), false);
  }

  @Override
  public LocalDateDoubleTimeSeries subSeries(LocalDate startTime, boolean includeStart, LocalDate endTime, boolean includeEnd) {
    return subSeriesFast(convertToInt(startTime), includeStart, convertToInt(endTime), includeEnd);
  }

  @Override
  public LocalDateDoubleTimeSeries subSeriesFast(int startTime, int endTime) {
    return subSeriesFast(startTime, true, endTime, false);
  }

  //-------------------------------------------------------------------------
  @Override
  public LocalDateDoubleTimeSeries lag(int days) {
    int[] times = timesArrayFast0();
    double[] values = valuesArrayFast0();
    if (days == 0) {
      return newInstanceFast(times, values);
    } else if (days < 0) {
      if (-days < times.length) {
        int[] resultTimes = new int[times.length + days]; // remember days is -ve
        System.arraycopy(times, 0, resultTimes, 0, times.length + days);
        double[] resultValues = new double[times.length + days];
        System.arraycopy(values, -days, resultValues, 0, times.length + days);
        return newInstanceFast(resultTimes, resultValues);
      } else {
        return newInstanceFast(new int[0], new double[0]);
      }
    } else { // if (days > 0) {
      if (days < times.length) {
        int[] resultTimes = new int[times.length - days]; // remember days is +ve
        System.arraycopy(times, days, resultTimes, 0, times.length - days);
        double[] resultValues = new double[times.length - days];
        System.arraycopy(values, 0, resultValues, 0, times.length - days);
        return newInstanceFast(resultTimes, resultValues);
      } else {
        return newInstanceFast(new int[0], new double[0]);
      }
    }
  }

  //-------------------------------------------------------------------------
  private LocalDateDoubleTimeSeries operate(DoubleTimeSeries<?> other, BinaryOperator operator) {
    if (other instanceof DateDoubleTimeSeries) {
      return operate((DateDoubleTimeSeries<?>) other, operator);
    }
    throw new UnsupportedOperationException("Can only operate on a DateDoubleTimeSeries");
  }

  public LocalDateDoubleTimeSeries operate(DateDoubleTimeSeries<?> other, BinaryOperator operator) {
    int[] aTimes = timesArrayFast0();
    // if the series share a common set of times use the common series
    if (other instanceof AbstractLocalDateDoubleTimeSeries &&
        aTimes == ((AbstractLocalDateDoubleTimeSeries) other).timesArrayFast0()) {
      return operateWithSameTimes(other, operator);
    }
    double[] aValues = valuesArrayFast0();
    int aCount = 0;
    int[] bTimes = other.timesArrayFast();
    double[] bValues = other.valuesArrayFast();
    int bCount = 0;
    int[] resTimes = new int[Math.min(aTimes.length, bTimes.length)];
    double[] resValues = new double[resTimes.length];
    int resCount = 0;
    while (aCount < aTimes.length && bCount < bTimes.length) {
      if (aTimes[aCount] == bTimes[bCount]) {
        resTimes[resCount] = aTimes[aCount];
        resValues[resCount] = operator.operate(aValues[aCount], bValues[bCount]);
        resCount++;
        aCount++;
        bCount++;
      } else if (aTimes[aCount] < bTimes[bCount]) {
        aCount++;
      } else { // if (aTimes[aCount] > bTimes[bCount]) {
        bCount++;
      }
    }
    int[] trimmedTimes = new int[resCount];
    double[] trimmedValues = new double[resCount];
    System.arraycopy(resTimes, 0, trimmedTimes, 0, resCount);
    System.arraycopy(resValues, 0, trimmedValues, 0, resCount);
    return newInstanceFast(trimmedTimes, trimmedValues);
  }

  private LocalDateDoubleTimeSeries unionOperate(DoubleTimeSeries<?> other, BinaryOperator operator) {
    if (other instanceof DateDoubleTimeSeries) {
      return unionOperate((DateDoubleTimeSeries<?>) other, operator);
    }
    throw new UnsupportedOperationException("Can only operate on a DateDoubleTimeSeries");
  }

  public LocalDateDoubleTimeSeries unionOperate(DateDoubleTimeSeries<?> other, BinaryOperator operator) {
    int[] aTimes = timesArrayFast0();
    // if the series share a common set of times use the common series
    if (other instanceof AbstractLocalDateDoubleTimeSeries &&
        aTimes == ((AbstractLocalDateDoubleTimeSeries) other).timesArrayFast0()) {
      return operateWithSameTimes(other, operator);
    }
    double[] aValues = valuesArrayFast0();
    int aCount = 0;
    int[] bTimes = other.timesArrayFast();
    double[] bValues = other.valuesArrayFast();
    int bCount = 0;
    int[] resTimes = new int[aTimes.length + bTimes.length];
    double[] resValues = new double[resTimes.length];
    int resCount = 0;
    while (aCount < aTimes.length || bCount < bTimes.length) {
      if (aCount >= aTimes.length) {
        int bRemaining = bTimes.length - bCount;
        System.arraycopy(bTimes, bCount, resTimes, resCount, bRemaining);
        System.arraycopy(bValues, bCount, resValues, resCount, bRemaining);
        resCount += bRemaining;
        break;
      } else if (bCount >= bTimes.length) {
        int aRemaining = aTimes.length - aCount;
        System.arraycopy(aTimes, aCount, resTimes, resCount, aRemaining);
        System.arraycopy(aValues, aCount, resValues, resCount, aRemaining);
        resCount += aRemaining;
        break;
      } else if (aTimes[aCount] == bTimes[bCount]) {
        resTimes[resCount] = aTimes[aCount];
        resValues[resCount] = operator.operate(aValues[aCount], bValues[bCount]);
        resCount++;
        aCount++;
        bCount++;
      } else if (aTimes[aCount] < bTimes[bCount]) {
        resTimes[resCount] = aTimes[aCount];
        resValues[resCount] = aValues[aCount];
        resCount++;
        aCount++;
      } else { // if (aTimes[aCount] > bTimes[bCount]) {
        resTimes[resCount] = bTimes[bCount];
        resValues[resCount] = bValues[bCount];
        resCount++;
        bCount++;
      }
    }
    int[] trimmedTimes = new int[resCount];
    double[] trimmedValues = new double[resCount];
    System.arraycopy(resTimes, 0, trimmedTimes, 0, resCount);
    System.arraycopy(resValues, 0, trimmedValues, 0, resCount);
    return newInstanceFast(trimmedTimes, trimmedValues);
  }

  //-------------------------------------------------------------------------
  @Override
  public LocalDateDoubleTimeSeries add(double amountToAdd) {
    return operate(amountToAdd, ADD_OPERATOR);
  }

  @Override
  public LocalDateDoubleTimeSeries add(DoubleTimeSeries<?> other) {
    return operate(other, ADD_OPERATOR);
  }

  @Override
  public LocalDateDoubleTimeSeries unionAdd(DoubleTimeSeries<?> other) {
    return unionOperate(other, ADD_OPERATOR);
  }

  //-------------------------------------------------------------------------
  @Override
  public LocalDateDoubleTimeSeries subtract(double amountToSubtract) {
    return operate(amountToSubtract, SUBTRACT_OPERATOR);
  }

  @Override
  public LocalDateDoubleTimeSeries subtract(DoubleTimeSeries<?> other) {
    return operate(other, SUBTRACT_OPERATOR);
  }

  @Override
  public LocalDateDoubleTimeSeries unionSubtract(DoubleTimeSeries<?> other) {
    return unionOperate(other, SUBTRACT_OPERATOR);
  }

  //-------------------------------------------------------------------------
  @Override
  public LocalDateDoubleTimeSeries multiply(double amountToMultiplyBy) {
    return operate(amountToMultiplyBy, MULTIPLY_OPERATOR);
  }

  @Override
  public LocalDateDoubleTimeSeries multiply(DoubleTimeSeries<?> other) {
    return operate(other, MULTIPLY_OPERATOR);
  }

  @Override
  public LocalDateDoubleTimeSeries unionMultiply(DoubleTimeSeries<?> other) {
    return unionOperate(other, MULTIPLY_OPERATOR);
  }

  //-------------------------------------------------------------------------
  @Override
  public LocalDateDoubleTimeSeries divide(double amountToDivideBy) {
    return operate(amountToDivideBy, DIVIDE_OPERATOR);
  }

  @Override
  public LocalDateDoubleTimeSeries divide(DoubleTimeSeries<?> other) {
    return operate(other, DIVIDE_OPERATOR);
  }

  @Override
  public LocalDateDoubleTimeSeries unionDivide(DoubleTimeSeries<?> other) {
    return unionOperate(other, DIVIDE_OPERATOR);
  }

  //-------------------------------------------------------------------------
  @Override
  public LocalDateDoubleTimeSeries power(double power) {
    return operate(power, POWER_OPERATOR);
  }

  @Override
  public LocalDateDoubleTimeSeries power(DoubleTimeSeries<?> other) {
    return operate(other, POWER_OPERATOR);
  }

  @Override
  public LocalDateDoubleTimeSeries unionPower(DoubleTimeSeries<?> other) {
    return unionOperate(other, POWER_OPERATOR);
  }

  //-------------------------------------------------------------------------
  @Override
  public LocalDateDoubleTimeSeries minimum(double minValue) {
    return operate(minValue, MINIMUM_OPERATOR);
  }

  @Override
  public LocalDateDoubleTimeSeries minimum(DoubleTimeSeries<?> other) {
    return operate(other, MINIMUM_OPERATOR);
  }

  @Override
  public LocalDateDoubleTimeSeries unionMinimum(DoubleTimeSeries<?> other) {
    return unionOperate(other, MINIMUM_OPERATOR);
  }

  //-------------------------------------------------------------------------
  @Override
  public LocalDateDoubleTimeSeries maximum(double maxValue) {
    return operate(maxValue, MAXIMUM_OPERATOR);
  }

  @Override
  public LocalDateDoubleTimeSeries maximum(DoubleTimeSeries<?> other) {
    return operate(other, MAXIMUM_OPERATOR);
  }

  @Override
  public LocalDateDoubleTimeSeries unionMaximum(DoubleTimeSeries<?> other) {
    return unionOperate(other, MAXIMUM_OPERATOR);
  }

  //-------------------------------------------------------------------------
  @Override
  public LocalDateDoubleTimeSeries average(double value) {
    return operate(value, AVERAGE_OPERATOR);
  }

  @Override
  public LocalDateDoubleTimeSeries average(DoubleTimeSeries<?> other) {
    return operate(other, AVERAGE_OPERATOR);
  }

  @Override
  public LocalDateDoubleTimeSeries unionAverage(DoubleTimeSeries<?> other) {
    return unionOperate(other, AVERAGE_OPERATOR);
  }

  //-------------------------------------------------------------------------
  @Override
  public LocalDateDoubleTimeSeries intersectionFirstValue(DoubleTimeSeries<?> other) {
    // optimize PLAT-1590
    if (other instanceof AbstractLocalDateDoubleTimeSeries) {
      int[] aTimes = timesArrayFast0();
      double[] aValues = valuesArrayFast0();
      int aCount = 0;
      int[] bTimes = ((AbstractLocalDateDoubleTimeSeries) other).timesArrayFast0();
      int bCount = 0;
      int[] resTimes = new int[Math.min(aTimes.length, bTimes.length)];
      double[] resValues = new double[resTimes.length];
      int resCount = 0;
      while (aCount < aTimes.length && bCount < bTimes.length) {
        if (aTimes[aCount] == bTimes[bCount]) {
          resTimes[resCount] = aTimes[aCount];
          resValues[resCount] = aValues[aCount];
          resCount++;
          aCount++;
          bCount++;
        } else if (aTimes[aCount] < bTimes[bCount]) {
          aCount++;
        } else { // if (aTimes[aCount] > bTimes[bCount]) {
          bCount++;
        }
      }
      int[] trimmedTimes = new int[resCount];
      double[] trimmedValues = new double[resCount];
      System.arraycopy(resTimes, 0, trimmedTimes, 0, resCount);
      System.arraycopy(resValues, 0, trimmedValues, 0, resCount);
      return newInstanceFast(trimmedTimes, trimmedValues);
    }
    return operate(other, FIRST_OPERATOR);
  }

  @Override
  public LocalDateDoubleTimeSeries intersectionSecondValue(DoubleTimeSeries<?> other) {
    return operate(other, SECOND_OPERATOR);
  }

  @Override
  public LocalDateDoubleTimeSeries noIntersectionOperation(DoubleTimeSeries<?> other) {
    return unionOperate(other, NO_INTERSECTION_OPERATOR);
  }

  //-------------------------------------------------------------------------
  @Override
  public LocalDateDoubleTimeSeries negate() {
    return operate(NEGATE_OPERATOR);
  }

  @Override
  public LocalDateDoubleTimeSeries reciprocal() {
    return operate(RECIPROCAL_OPERATOR);
  }

  @Override
  public LocalDateDoubleTimeSeries log() {
    return operate(LOG_OPERATOR);
  }

  @Override
  public LocalDateDoubleTimeSeries log10() {
    return operate(LOG10_OPERATOR);
  }

  @Override
  public LocalDateDoubleTimeSeries abs() {
    return operate(ABS_OPERATOR);
  }

  //-------------------------------------------------------------------------
  @Override
  public double maxValue() {
    if (isEmpty()) {
      throw new NoSuchElementException("Time-series is empty");
    }
    double max = Double.MIN_VALUE;
    for (double value : valuesArrayFast0()) {
      max = Math.max(max, value);
    }
    return max;
  }

  @Override
  public double minValue() throws NoSuchElementException {
    if (isEmpty()) {
      throw new NoSuchElementException("Time-series is empty");
    }
    double min = Double.MAX_VALUE;
    for (double value : valuesArrayFast0()) {
      min = Math.min(min, value);
    }
    return min;
  }

  //-------------------------------------------------------------------------
  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj instanceof AbstractLocalDateDoubleTimeSeries) {
      AbstractLocalDateDoubleTimeSeries other = (AbstractLocalDateDoubleTimeSeries) obj;
      return Arrays.equals(timesArrayFast0(), other.timesArrayFast0()) &&
              Arrays.equals(valuesArrayFast0(), other.valuesArrayFast0());
    }
    if (obj instanceof DateDoubleTimeSeries) {
      DateDoubleTimeSeries<?> other = (DateDoubleTimeSeries<?>) obj;
      return Arrays.equals(timesArrayFast0(), other.timesArrayFast()) &&
              Arrays.equals(valuesArrayFast0(), other.valuesArrayFast());
    }
    return false;
  }

  @Override
  public int hashCode() {
    return Arrays.hashCode(timesArrayFast0()) ^ Arrays.hashCode(valuesArrayFast0());
  }

  private LocalDateDoubleTimeSeries operateWithSameTimes(DateDoubleTimeSeries<?> other, BinaryOperator operator) {
    // series share a common set of times so use the common series
    int[] aTimes = timesArrayFast0();
    double[] aValues = valuesArrayFast0();
    double[] resValues = new double[aTimes.length];
    for (int i = 0; i < aTimes.length; i++) {
      resValues[i] = operator.operate(aValues[i], other.getValueAtIndexFast(i));
    }
    return newInstanceFast(aTimes, resValues);
  }

}
