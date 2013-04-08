/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.timeseries.date.localdate;

import java.util.Arrays;
import java.util.Collection;
import java.util.NoSuchElementException;

import org.threeten.bp.LocalDate;

import com.opengamma.timeseries.DoubleTimeSeries;
import com.opengamma.timeseries.DoubleTimeSeriesOperators.BinaryOperator;
import com.opengamma.timeseries.DoubleTimeSeriesOperators.UnaryOperator;
import com.opengamma.timeseries.date.DateDoubleTimeSeries;

/**
 * Standard immutable implementation of {@code LocalDateDoubleTimeSeries}.
 */
public final class ImmutableLocalDateDoubleTimeSeries
    extends AbstractLocalDateDoubleTimeSeries
    implements LocalDateDoubleTimeSeries {

  /** Empty instance. */
  public static final ImmutableLocalDateDoubleTimeSeries EMPTY_SERIES = new ImmutableLocalDateDoubleTimeSeries(new int[0], new double[0]);

  /** Serialization version. */
  private static final long serialVersionUID = -43654613865187568L;

  /**
   * The times in the series.
   */
  private final int[] _times;
  /**
   * The values in the series.
   */
  private final double[] _values;

  /**
   * Obtains a time-series from matching arrays of dates and values.
   * 
   * @param dates  the date array, not null
   * @param values  the value array, not null
   * @return the time-series, not null
   */
  public static ImmutableLocalDateDoubleTimeSeries of(LocalDate[] dates, Double[] values) {
    int[] timesArray = convertToIntArray(dates);
    double[] valuesArray = convertToDoubleArray(values);
    validate(timesArray, valuesArray);
    return new ImmutableLocalDateDoubleTimeSeries(timesArray, valuesArray);
  }

  /**
   * Obtains a time-series from matching arrays of dates and values.
   * 
   * @param dates  the date array, not null
   * @param values  the value array, not null
   * @return the time-series, not null
   */
  public static ImmutableLocalDateDoubleTimeSeries of(LocalDate[] dates, double[] values) {
    int[] timesArray = convertToIntArray(dates);
    validate(timesArray, values);
    double[] valuesArray = values.clone();
    return new ImmutableLocalDateDoubleTimeSeries(timesArray, valuesArray);
  }

  /**
   * Obtains a time-series from matching arrays of dates and values.
   * 
   * @param dates  the date array, not null
   * @param values  the value array, not null
   * @return the time-series, not null
   */
  public static ImmutableLocalDateDoubleTimeSeries of(int[] dates, double[] values) {
    validate(dates, values);
    int[] timesArray = dates.clone();
    double[] valuesArray = values.clone();
    return new ImmutableLocalDateDoubleTimeSeries(timesArray, valuesArray);
  }

  /**
   * Obtains a time-series from matching arrays of dates and values.
   * 
   * @param dates  the date list, not null
   * @param values  the value list, not null
   * @return the time-series, not null
   */
  public static ImmutableLocalDateDoubleTimeSeries of(Collection<LocalDate> dates, Collection<Double> values) {
    int[] timesArray = convertToIntArray(dates);
    double[] valuesArray = convertToDoubleArray(values);
    validate(timesArray, valuesArray);
    return new ImmutableLocalDateDoubleTimeSeries(timesArray, valuesArray);
  }

  /**
   * Obtains a time-series from another time-series.
   * 
   * @param timeSeries  the time-series, not null
   * @return the time-series, not null
   */
  public static ImmutableLocalDateDoubleTimeSeries of(DateDoubleTimeSeries<?> timeSeries) {
    if (timeSeries instanceof ImmutableLocalDateDoubleTimeSeries) {
      return (ImmutableLocalDateDoubleTimeSeries) timeSeries;
    }
    DateDoubleTimeSeries<?> other = (DateDoubleTimeSeries<?>) timeSeries;
    int[] timesArray = other.timesArrayFast();
    double[] valuesArray = other.valuesArrayFast();
    return new ImmutableLocalDateDoubleTimeSeries(timesArray, valuesArray);
  }

  //-------------------------------------------------------------------------
  /**
   * Obtains a time-series from another time-series.
   * 
   * @param timeSeries  the time-series, not null
   * @return the time-series, not null
   */
  public static ImmutableLocalDateDoubleTimeSeries from(DoubleTimeSeries<LocalDate> timeSeries) {
    if (timeSeries instanceof DateDoubleTimeSeries) {
      return of((DateDoubleTimeSeries<?>) timeSeries);
    }
    int[] timesArray = convertToIntArray(timeSeries.timesArray());
    double[] valuesArray = timeSeries.valuesArrayFast();
    return new ImmutableLocalDateDoubleTimeSeries(timesArray, valuesArray);
  }

  //-------------------------------------------------------------------------
  /**
   * Validates the data before creation.
   * 
   * @param times  the times, not null
   * @param values  the values, not null
   */
  private static void validate(int[] times, double[] values) {
    if (times == null || values == null) {
      throw new IllegalArgumentException("Array must not be null");
    }
    // check lengths
    if (times.length != values.length) {
      throw new IllegalArgumentException("Arrays are of different sizes: " + times.length + ", " + values.length);
    }
    // check dates are ordered
    int maxTime = Integer.MIN_VALUE;
    for (int time : times) {
      if (time < maxTime) {
        throw new IllegalArgumentException("dates must be ordered");
      }
      maxTime = time;
    }
  }

  /**
   * Creates an instance.
   * 
   * @param times  the times, not null
   * @param values  the values, not null
   */
  private ImmutableLocalDateDoubleTimeSeries(int[] times, double[] values) {
    _times = times;
    _values = values;
  }

//
//  @Override
//  protected FastBackedDoubleTimeSeries<LocalDate> intersectionFirstValueFast(FastLongDoubleTimeSeries other) {
//    //PLAT-1590
//    int[] aTimes = getFastSeries().timesArrayFast();
//    double[] aValues = valuesArrayFast();
//    int aCount = 0;
//    long[] bTimesLong = other.timesArrayFast();
//    int[] bTimes = new int[bTimesLong.length];
//    
//    DateTimeNumericEncoding aEncoding = getFastSeries().getEncoding();
//    DateTimeNumericEncoding bEncoding = other.getEncoding();
//    for (int i = 0; i < bTimesLong.length; i++) {
//      bTimes[i] = bEncoding.convertToInt(bTimesLong[i], aEncoding);
//    }
//    
//    
//    int bCount = 0;
//    int[] resTimes = new int[Math.min(aTimes.length, bTimes.length)];
//    double[] resValues = new double[resTimes.length];
//    int resCount = 0;
//    while (aCount < aTimes.length && bCount < bTimes.length) {
//      if (aTimes[aCount] == bTimes[bCount]) {
//        resTimes[resCount] = aTimes[aCount];
//        resValues[resCount] = aValues[aCount];
//        resCount++;
//        aCount++;
//        bCount++;
//      } else if (aTimes[aCount] < bTimes[bCount]) {
//        aCount++;
//      } else { // if (aTimes[aCount] > bTimes[bCount]) {
//        bCount++;
//      }
//    }
//    int[] trimmedTimes = new int[resCount];
//    double[] trimmedValues = new double[resCount];
//    System.arraycopy(resTimes, 0, trimmedTimes, 0, resCount);
//    System.arraycopy(resValues, 0, trimmedValues, 0, resCount);
//    return new ArrayLocalDateDoubleTimeSeries(new FastArrayIntDoubleTimeSeries(getFastSeries().getEncoding(), trimmedTimes, trimmedValues));
//  }

  //-------------------------------------------------------------------------
  @Override
  int[] timesArrayFast0() {
    return _times;
  }

  @Override
  double[] valuesArrayFast0() {
    return _values;
  }

  @Override
  LocalDateDoubleTimeSeries newInstanceFast(int[] times, double[] values) {
    return new ImmutableLocalDateDoubleTimeSeries(times, values);
  }

  //-------------------------------------------------------------------------
  @Override
  public int size() {
    return _times.length;
  }

  //-------------------------------------------------------------------------
  @Override
  public boolean containsTime(int date) {
    int binarySearch = Arrays.binarySearch(_times, date);
    return (binarySearch >= 0 && _times[binarySearch] == date);
  }

  @Override
  public Double getValue(int date) {
    int binarySearch = Arrays.binarySearch(_times, date);
    if (binarySearch >= 0) {
      return _values[binarySearch];
    } else {
      return null;
    }
  }

  @Override
  public int getTimeAtIndexFast(int index) {
    return _times[index];
  }

  @Override
  public double getValueAtIndexFast(int index) {
    return _values[index];
  }

  //-------------------------------------------------------------------------
  @Override
  public int getEarliestTimeFast() {
    try {
      return _times[0];
    } catch (IndexOutOfBoundsException ex) {
      throw new NoSuchElementException("Series is empty");
    }
  }

  @Override
  public double getEarliestValueFast() {
    try {
      return _values[0];
    } catch (IndexOutOfBoundsException ex) {
      throw new NoSuchElementException("Series is empty");
    }
  }

  @Override
  public int getLatestTimeFast() {
    try {
      return _times[_times.length - 1];
    } catch (IndexOutOfBoundsException ex) {
      throw new NoSuchElementException("Series is empty");
    }
  }

  @Override
  public double getLatestValueFast() {
    try {
      return _values[_values.length - 1];
    } catch (IndexOutOfBoundsException ex) {
      throw new NoSuchElementException("Series is empty");
    }
  }

  //-------------------------------------------------------------------------
  @Override
  public int[] timesArrayFast() {
    return _times.clone();
  }

  @Override
  public double[] valuesArrayFast() {
    return _values.clone();
  }

  //-------------------------------------------------------------------------
  @Override
  public LocalDateDoubleTimeSeries subSeriesFast(int startTime, int endTime) {
    if (isEmpty()) {
      return EMPTY_SERIES;
    }
    int startPos = Arrays.binarySearch(_times, startTime);
    int endPos = (endTime == Integer.MIN_VALUE) ? _times.length : Arrays.binarySearch(_times, endTime);
    startPos = startPos >= 0 ? startPos : -(startPos + 1);
    endPos = endPos >= 0 ? endPos : -(endPos + 1);
    if (endPos > _times.length) {
      endPos--;
    }
    int[] timesArray = Arrays.copyOfRange(_times, startPos, endPos);
    double[] valuesArray = Arrays.copyOfRange(_values, startPos, endPos);
    return new ImmutableLocalDateDoubleTimeSeries(timesArray, valuesArray);
  }

  //-------------------------------------------------------------------------
  @Override
  public LocalDateDoubleTimeSeries head(int numItems) {
    if (numItems == size()) {
      return this;
    }
    int[] timesArray = Arrays.copyOfRange(_times, 0, numItems);
    double[] valuesArray = Arrays.copyOfRange(_values, 0, numItems);
    return new ImmutableLocalDateDoubleTimeSeries(timesArray, valuesArray);
  }

  @Override
  public LocalDateDoubleTimeSeries tail(int numItems) {
    int size = size();
    if (numItems == size) {
      return this;
    }
    int[] timesArray = Arrays.copyOfRange(_times, size - numItems, size);
    double[] valuesArray = Arrays.copyOfRange(_values, size - numItems, size);
    return new ImmutableLocalDateDoubleTimeSeries(timesArray, valuesArray);
  }

  //-------------------------------------------------------------------------
  @Override
  public ImmutableLocalDateDoubleTimeSeries newInstance(LocalDate[] dates, Double[] values) {
    return of(dates, values);
  }

  //-------------------------------------------------------------------------
  @Override
  public LocalDateDoubleTimeSeries operate(UnaryOperator operator) {
    double[] valuesArray = valuesArrayFast();
    for (int i = 0; i < valuesArray.length; i++) {
      valuesArray[i] = operator.operate(valuesArray[i]);
    }
    return new ImmutableLocalDateDoubleTimeSeries(_times, valuesArray);  // immutable, so can share times
  }

  @Override
  public LocalDateDoubleTimeSeries operate(double other, BinaryOperator operator) {
    double[] valuesArray = valuesArrayFast();
    for (int i = 0; i < valuesArray.length; i++) {
      valuesArray[i] = operator.operate(valuesArray[i], other);
    }
    return new ImmutableLocalDateDoubleTimeSeries(_times, valuesArray);  // immutable, so can share times
  }

}
