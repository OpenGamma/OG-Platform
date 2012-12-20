/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.util.timeseries.fast.longint.object;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import org.apache.commons.lang.ArrayUtils;

import com.opengamma.util.timeseries.FastBackedObjectTimeSeries;
import com.opengamma.util.timeseries.ObjectTimeSeries;
import com.opengamma.util.timeseries.ObjectTimeSeriesOperators.BinaryOperator;
import com.opengamma.util.timeseries.ObjectTimeSeriesOperators.UnaryOperator;
import com.opengamma.util.timeseries.fast.AbstractFastObjectTimeSeries;
import com.opengamma.util.timeseries.fast.DateTimeNumericEncoding;
import com.opengamma.util.timeseries.fast.DateTimeResolution;
import com.opengamma.util.timeseries.fast.FastObjectTimeSeries;
import com.opengamma.util.timeseries.fast.integer.object.FastArrayIntObjectTimeSeries;
import com.opengamma.util.timeseries.fast.integer.object.FastIntObjectTimeSeries;
import com.opengamma.util.timeseries.fast.integer.object.FastListIntObjectTimeSeries;
import com.opengamma.util.timeseries.fast.integer.object.FastMutableIntObjectTimeSeries;

/**
 *         Contains methods to make Primitive time series work with the normal
 *         non-primitive time series interface (where possible)
 *         @param <T> The type of the data
 */
public abstract class AbstractFastLongObjectTimeSeries<T> extends AbstractFastObjectTimeSeries<Long, T> implements
    FastLongObjectTimeSeries<T> {

  private final DateTimeNumericEncoding _encoding;

  protected AbstractFastLongObjectTimeSeries(final DateTimeNumericEncoding encoding) {
    _encoding = encoding;
  }

  @Override
  public Long getEarliestTime() {
    return getEarliestTimeFast();
  }

  @Override
  public T getEarliestValue() {
    return getEarliestValueFast();
  }

  @Override
  public Long getLatestTime() {
    return getLatestTimeFast();
  }

  @Override
  public T getLatestValue() {
    return getLatestValueFast();
  }

  @Override
  public Long getTimeAt(final int index) {
    return getTimeFast(index);
  }

  @Override
  public T getValue(final Long dateTime) {
    try {
      return getValueFast(dateTime);
    } catch (NoSuchElementException nsee) {
      return null;
    } catch (ArrayIndexOutOfBoundsException aioobe) {
      return null;
    }
  }

  @Override
  public T getValueAt(final int index) {
    return getValueAtFast(index);
  }

  @Override
  public ObjectTimeSeries<Long, T> subSeries(final Long startTime, final Long endTime) {
    return (ObjectTimeSeries<Long, T>) subSeriesFast(startTime, endTime);
  }

  @Override
  public ObjectTimeSeries<Long, T> subSeries(final Long startTime, final boolean includeStart, final Long endTime, final boolean includeEnd) {
    return (ObjectTimeSeries<Long, T>) subSeriesFast(startTime, includeStart, endTime, includeEnd);
  }

  @Override
  public Iterator<Long> timeIterator() {
    return timesIteratorFast();
  }

  @Override
  public List<Long> times() {
    return timesFast();
  }

  @Override
  public Long[] timesArray() {
    return ArrayUtils.toObject(timesArrayFast());
  }

  @Override
  public List<T> values() {
    return valuesFast();
  }

  @Override
  public T[] valuesArray() {
    return valuesArrayFast();
  }

  @Override
  public Iterator<T> valuesIterator() {
    return valuesIteratorFast();
  }

  @Override
  public DateTimeResolution getDateTimeResolution() {
    return getEncoding().getResolution();
  }

  @Override
  public DateTimeNumericEncoding getEncoding() {
    return _encoding;
  }

  @Override
  public ObjectTimeSeries<Long, T> newInstance(final Long[] times, final T[] values) {
    return (ObjectTimeSeries<Long, T>) newInstanceFast(ArrayUtils.toPrimitive(times), values);
  }

  @SuppressWarnings("unchecked")
  public FastLongObjectTimeSeries<T> operate(final UnaryOperator<T> operator) {
    final long[] aTimes = timesArrayFast();
    final T[] aValues = valuesArrayFast();
    final T[] results = (T[]) new Object[aValues.length]; // could modify in place, but will probably switch to in-place view of backing array.
    for (int i = 0; i < aValues.length; i++) {
      results[i] = operator.operate(aValues[i]);
    }
    return newInstanceFast(aTimes, results);
  }

  @SuppressWarnings("unchecked")
  public FastLongObjectTimeSeries<T> operate(final T other, final BinaryOperator<T> operator) {
    final long[] aTimes = timesArrayFast();
    final T[] aValues = valuesArrayFast();
    final T[] results = (T[]) new Object[aValues.length]; // could modify in place, but will probably switch to in-place view of backing array.
    for (int i = 0; i < aValues.length; i++) {
      results[i] = operator.operate(aValues[i], other);
    }
    return newInstanceFast(aTimes, results);
  }

  @SuppressWarnings("unchecked")
  public FastLongObjectTimeSeries<T> operate(final FastBackedObjectTimeSeries<?, T> other,
      final BinaryOperator<T> operator) {
    FastObjectTimeSeries<?, T> fastSeries = other.getFastSeries();
    if (fastSeries instanceof FastIntObjectTimeSeries<?>) {
      return operate((FastIntObjectTimeSeries<T>) fastSeries, operator);
    } else { // if (fastSeries instanceof FastLongObjectTimeSeries<T>
      return operate((FastLongObjectTimeSeries<T>) fastSeries, operator);
    }
  }

  @SuppressWarnings("unchecked")
  public FastLongObjectTimeSeries<T> operate(final FastLongObjectTimeSeries<T> other, final BinaryOperator<T> operator) {
    final long[] aTimes = timesArrayFast();
    final T[] aValues = valuesArrayFast();
    int aCount = 0;
    final long[] bTimes = other.timesArrayFast();
    if (getEncoding() != other.getEncoding()) { // convert to a's format -- NOTE: if we switch to using an underlying array rather than a copy, we can't modify it in-place like we're doing here.
      DateTimeNumericEncoding aEncoding = getEncoding();
      DateTimeNumericEncoding bEncoding = other.getEncoding();
      for (int i = 0; i < bTimes.length; i++) {
        bTimes[i] = bEncoding.convertToLong(bTimes[i], aEncoding);
      }
    }
    final T[] bValues = other.valuesArrayFast();
    int bCount = 0;
    final long[] resTimes = new long[aTimes.length + bTimes.length];
    final T[] resValues = (T[]) new Object[resTimes.length];
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
    long[] trimmedTimes = new long[resCount];
    T[] trimmedValues = (T[]) new Object[resCount];
    System.arraycopy(resTimes, 0, trimmedTimes, 0, resCount);
    System.arraycopy(resValues, 0, trimmedValues, 0, resCount);
    return newInstanceFast(trimmedTimes, trimmedValues);
  }

  @SuppressWarnings("unchecked")
  public FastLongObjectTimeSeries<T> operate(final FastIntObjectTimeSeries<T> other, final BinaryOperator<T> operator) {
    final long[] aTimes = timesArrayFast();
    final T[] aValues = valuesArrayFast();
    int aCount = 0;
    final int[] bTimesInt = other.timesArrayFast();
    final long[] bTimes = new long[bTimesInt.length];
    if (getEncoding() != other.getEncoding()) { // convert to a's format -- NOTE: if we switch to using an underlying array rather than a copy, we can't modify it in-place like we're doing here.
      DateTimeNumericEncoding aEncoding = getEncoding();
      DateTimeNumericEncoding bEncoding = other.getEncoding();
      for (int i = 0; i < bTimesInt.length; i++) {
        bTimes[i] = bEncoding.convertToLong(bTimesInt[i], aEncoding);
      }
    } else {
      for (int i = 0; i < bTimesInt.length; i++) {
        bTimes[i] = bTimesInt[i];
      }
    }
    final T[] bValues = other.valuesArrayFast();
    int bCount = 0;
    final long[] resTimes = new long[aTimes.length + bTimes.length];
    final T[] resValues = (T[]) new Object[resTimes.length];
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
    long[] trimmedTimes = new long[resCount];
    T[] trimmedValues = (T[]) new Object[resCount];
    System.arraycopy(resTimes, 0, trimmedTimes, 0, resCount);
    System.arraycopy(resValues, 0, trimmedValues, 0, resCount);
    return newInstanceFast(trimmedTimes, trimmedValues);
  }

  @SuppressWarnings("unchecked")
  public FastLongObjectTimeSeries<T> unionOperate(final FastBackedObjectTimeSeries<?, T> other,
      final BinaryOperator<T> operator) {
    FastObjectTimeSeries<?, T> fastSeries = other.getFastSeries();
    if (fastSeries instanceof FastIntObjectTimeSeries<?>) {
      return unionOperate((FastIntObjectTimeSeries<T>) fastSeries, operator);
    } else { // if (fastSeries instanceof FastLongObjectTimeSeries<T>
      return unionOperate((FastLongObjectTimeSeries<T>) fastSeries, operator);
    }
  }

  @SuppressWarnings("unchecked")
  public FastLongObjectTimeSeries<T> unionOperate(final FastIntObjectTimeSeries<T> other,
      final BinaryOperator<T> operator) {
    final long[] aTimes = timesArrayFast();
    final T[] aValues = valuesArrayFast();
    int aCount = 0;
    final int[] bTimesInt = other.timesArrayFast();
    final long[] bTimes = new long[bTimesInt.length];
    if (getEncoding() != other.getEncoding()) { // convert to a's format -- NOTE: if we switch to using an underlying array rather than a copy, we can't modify it in-place like we're doing here.
      DateTimeNumericEncoding aEncoding = getEncoding();
      DateTimeNumericEncoding bEncoding = other.getEncoding();
      for (int i = 0; i < bTimes.length; i++) {
        bTimes[i] = bEncoding.convertToLong(bTimesInt[i], aEncoding);
      }
    } else {
      for (int i = 0; i < bTimes.length; i++) {
        bTimes[i] = bTimesInt[i];
      }
    }
    final T[] bValues = other.valuesArrayFast();
    int bCount = 0;
    final long[] resTimes = new long[aTimes.length + bTimes.length];
    final T[] resValues = (T[]) new Object[resTimes.length];
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
    long[] trimmedTimes = new long[resCount];
    T[] trimmedValues = (T[]) new Object[resCount];
    System.arraycopy(resTimes, 0, trimmedTimes, 0, resCount);
    System.arraycopy(resValues, 0, trimmedValues, 0, resCount);
    return newInstanceFast(trimmedTimes, trimmedValues);
  }

  @SuppressWarnings("unchecked")
  public FastLongObjectTimeSeries<T> unionOperate(final FastLongObjectTimeSeries<T> other,
      final BinaryOperator<T> operator) {
    final long[] aTimes = timesArrayFast();
    final T[] aValues = valuesArrayFast();
    int aCount = 0;
    final long[] bTimes = other.timesArrayFast();
    if (getEncoding() != other.getEncoding()) { // convert to a's format -- NOTE: if we switch to using an underlying array rather than a copy, we can't modify it in-place like we're doing here.
      DateTimeNumericEncoding aEncoding = getEncoding();
      DateTimeNumericEncoding bEncoding = other.getEncoding();
      for (int i = 0; i < bTimes.length; i++) {
        bTimes[i] = bEncoding.convertToLong(bTimes[i], aEncoding);
      }
    }
    final T[] bValues = other.valuesArrayFast();
    int bCount = 0;
    final long[] resTimes = new long[aTimes.length + bTimes.length];
    final T[] resValues = (T[]) new Object[resTimes.length];
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
    long[] trimmedTimes = new long[resCount];
    T[] trimmedValues = (T[]) new Object[resCount];
    System.arraycopy(resTimes, 0, trimmedTimes, 0, resCount);
    System.arraycopy(resValues, 0, trimmedValues, 0, resCount);
    return newInstanceFast(trimmedTimes, trimmedValues);
  }

  @SuppressWarnings("unchecked")
  public FastLongObjectTimeSeries<T> lag(final int days) {
    long[] times = timesArrayFast();
    T[] values = valuesArrayFast();
    if (days == 0) {
      return newInstanceFast(times, values);
    } else if (days < 0) {
      long[] resultTimes = new long[times.length + days]; // remember days is -ve
      System.arraycopy(times, 0, resultTimes, 0, times.length + days);
      T[] resultValues = (T[]) new Object[times.length + days];
      System.arraycopy(values, -days, resultValues, 0, times.length + days);
      return newInstanceFast(times, values);
    } else { // if (days > 0) {
      long[] resultTimes = new long[times.length - days]; // remember days is +ve
      System.arraycopy(times, days, resultTimes, 0, times.length - days);
      T[] resultValues = (T[]) new Object[times.length - days];
      System.arraycopy(values, 0, resultValues, 0, times.length - days);
      return newInstanceFast(times, values);
    }
  }

  public FastLongObjectTimeSeries<T> subSeriesFast(long startTime, boolean includeStart, long endTime, boolean includeEnd) {
    if (startTime != endTime || includeStart || includeEnd) {
      startTime += (includeStart ? 0 : 1);
      endTime += (includeEnd ? 1 : 0);
    }
    return subSeriesFast(startTime, endTime);
  }

  public FastMutableIntObjectTimeSeries<T> toFastMutableIntObjectTimeSeries() {
    return new FastListIntObjectTimeSeries<T>(this);
  }

  public FastIntObjectTimeSeries<T> toFastIntObjectTimeSeries() {
    return new FastArrayIntObjectTimeSeries<T>(this);
  }

  public FastMutableLongObjectTimeSeries<T> toFastMutableLongObjectTimeSeries() {
    return new FastListLongObjectTimeSeries<T>(this);
  }

  public FastLongObjectTimeSeries<T> toFastLongObjectTimeSeries() {
    return this;
  }

  public FastMutableIntObjectTimeSeries<T> toFastMutableIntObjectTimeSeries(DateTimeNumericEncoding encoding) {
    return new FastListIntObjectTimeSeries<T>(encoding, this);
  }

  public FastIntObjectTimeSeries<T> toFastIntObjectTimeSeries(DateTimeNumericEncoding encoding) {
    return new FastArrayIntObjectTimeSeries<T>(encoding, this);
  }

  public FastMutableLongObjectTimeSeries<T> toFastMutableLongObjectTimeSeries(DateTimeNumericEncoding encoding) {
    return new FastListLongObjectTimeSeries<T>(encoding, this);
  }

  public FastLongObjectTimeSeries<T> toFastLongObjectTimeSeries(DateTimeNumericEncoding encoding) {
    return new FastArrayLongObjectTimeSeries<T>(encoding, this);
  }
}
