/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.util.timeseries.fast.longint;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import org.apache.commons.lang.ArrayUtils;

import com.opengamma.util.timeseries.DoubleTimeSeries;
import com.opengamma.util.timeseries.DoubleTimeSeriesOperators.BinaryOperator;
import com.opengamma.util.timeseries.DoubleTimeSeriesOperators.UnaryOperator;
import com.opengamma.util.timeseries.FastBackedDoubleTimeSeries;
import com.opengamma.util.timeseries.fast.AbstractFastTimeSeries;
import com.opengamma.util.timeseries.fast.DateTimeNumericEncoding;
import com.opengamma.util.timeseries.fast.DateTimeResolution;
import com.opengamma.util.timeseries.fast.FastTimeSeries;
import com.opengamma.util.timeseries.fast.integer.FastArrayIntDoubleTimeSeries;
import com.opengamma.util.timeseries.fast.integer.FastIntDoubleTimeSeries;
import com.opengamma.util.timeseries.fast.integer.FastListIntDoubleTimeSeries;
import com.opengamma.util.timeseries.fast.integer.FastMutableIntDoubleTimeSeries;

/**
 * @author jim
 *         Contains methods to make Primitive time series work with the normal
 *         non-primitive time series interface (where possible)
 */
public abstract class AbstractFastLongDoubleTimeSeries
    extends AbstractFastTimeSeries<Long>
    implements FastLongDoubleTimeSeries {

  private final DateTimeNumericEncoding _encoding;

  protected AbstractFastLongDoubleTimeSeries(final DateTimeNumericEncoding encoding) {
    _encoding = encoding;
  }

  @Override
  public Long getEarliestTime() {
    return getEarliestTimeFast();
  }

  @Override
  public Double getEarliestValue() {
    return getEarliestValueFast();
  }

  @Override
  public Long getLatestTime() {
    return getLatestTimeFast();
  }

  @Override
  public Double getLatestValue() {
    return getLatestValueFast();
  }

  @Override
  public Long getTimeAt(final int index) {
    return getTimeFast(index);
  }

  @Override
  public Double getValue(final Long dateTime) {
    try {
      return getValueFast(dateTime);
    } catch (NoSuchElementException nsee) {
      return null;
    } catch (ArrayIndexOutOfBoundsException aioobe) {
      return null;
    }
  }

  @Override
  public Double getValueAt(final int index) {
    return getValueAtFast(index);
  }

  @Override
  public DoubleTimeSeries<Long> subSeries(final Long startTime, final Long endTime) {
    return subSeriesFast(startTime, endTime);
  }
  
  @Override
  public DoubleTimeSeries<Long> subSeries(final Long startTime, final boolean includeStart, final Long endTime, final boolean exclusiveEnd) {
    return subSeriesFast(startTime, includeStart, endTime, !exclusiveEnd);
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
  public List<Double> values() {
    return valuesFast();
  }

  @Override
  public Double[] valuesArray() {
    return ArrayUtils.toObject(valuesArrayFast());
  }

  @Override
  public Iterator<Double> valuesIterator() {
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
  public DoubleTimeSeries<Long> newInstance(final Long[] times, final Double[] values) {
    return newInstanceFast(ArrayUtils.toPrimitive(times), ArrayUtils.toPrimitive(values));
  }

  public FastLongDoubleTimeSeries operate(final UnaryOperator operator) {
    final long[] aTimes = timesArrayFast();
    final double[] aValues = valuesArrayFast();
    final double[] results = new double[aValues.length]; // could modify in place, but will probably switch to in-place view of backing array.
    for (int i = 0; i < aValues.length; i++) {
      results[i] = operator.operate(aValues[i]);
    }
    return newInstanceFast(aTimes, results);
  }

  public FastLongDoubleTimeSeries operate(final double other, final BinaryOperator operator) {
    final long[] aTimes = timesArrayFast();
    final double[] aValues = valuesArrayFast();
    final double[] results = new double[aValues.length]; // could modify in place, but will probably switch to in-place view of backing array.
    for (int i = 0; i < aValues.length; i++) {
      results[i] = operator.operate(aValues[i], other);
    }
    return newInstanceFast(aTimes, results);
  }

  public FastLongDoubleTimeSeries operate(final FastBackedDoubleTimeSeries<?> other, final BinaryOperator operator) {
    FastTimeSeries<?> fastSeries = other.getFastSeries();
    if (fastSeries instanceof FastIntDoubleTimeSeries) {
      return operate((FastIntDoubleTimeSeries) fastSeries, operator);
    } else { // if (fastSeries instanceof FastLongDoubleTimeSeries
      return operate((FastLongDoubleTimeSeries) fastSeries, operator);
    }
  }

  @Override
  public FastLongDoubleTimeSeries operate(final FastLongDoubleTimeSeries other, final BinaryOperator operator) { 
    final long[] aTimes = timesArrayFast();
    final double[] aValues = valuesArrayFast();
    int aCount = 0;
    final long[] bTimes = other.timesArrayFast();
    if (getEncoding() != other.getEncoding()) { // convert to a's format -- NOTE: if we switch to using an underlying array rather than a copy, we can't modify it in-place like we're doing here.
      DateTimeNumericEncoding aEncoding = getEncoding();
      DateTimeNumericEncoding bEncoding = other.getEncoding();
      for (int i = 0; i < bTimes.length; i++) {
        bTimes[i] = bEncoding.convertToLong(bTimes[i], aEncoding);
      }
    }
    final double[] bValues = other.valuesArrayFast();
    int bCount = 0;
    final long[] resTimes = new long[aTimes.length + bTimes.length];
    final double[] resValues = new double[resTimes.length];
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
    double[] trimmedValues = new double[resCount];
    System.arraycopy(resTimes, 0, trimmedTimes, 0, resCount);
    System.arraycopy(resValues, 0, trimmedValues, 0, resCount);
    return newInstanceFast(trimmedTimes, trimmedValues);
  }

  @Override
  public FastLongDoubleTimeSeries operate(final FastIntDoubleTimeSeries other, final BinaryOperator operator) { 
    final long[] aTimes = timesArrayFast();
    final double[] aValues = valuesArrayFast();
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
    final double[] bValues = other.valuesArrayFast();
    int bCount = 0;
    final long[] resTimes = new long[aTimes.length + bTimes.length];
    final double[] resValues = new double[resTimes.length];
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
    double[] trimmedValues = new double[resCount];
    System.arraycopy(resTimes, 0, trimmedTimes, 0, resCount);
    System.arraycopy(resValues, 0, trimmedValues, 0, resCount);
    return newInstanceFast(trimmedTimes, trimmedValues);
  }

  @Override
  public FastLongDoubleTimeSeries unionOperate(final FastBackedDoubleTimeSeries<?> other, final BinaryOperator operator) {
    FastTimeSeries<?> fastSeries = other.getFastSeries();
    if (fastSeries instanceof FastIntDoubleTimeSeries) {
      return unionOperate((FastIntDoubleTimeSeries) fastSeries, operator);
    } else { // if (fastSeries instanceof FastLongDoubleTimeSeries
      return unionOperate((FastLongDoubleTimeSeries) fastSeries, operator);
    }
  }

  @Override
  public FastLongDoubleTimeSeries unionOperate(final FastIntDoubleTimeSeries other, final BinaryOperator operator) { 
    final long[] aTimes = timesArrayFast();
    final double[] aValues = valuesArrayFast();
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
    final double[] bValues = other.valuesArrayFast();
    int bCount = 0;
    final long[] resTimes = new long[aTimes.length + bTimes.length];
    final double[] resValues = new double[resTimes.length];
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
    double[] trimmedValues = new double[resCount];
    System.arraycopy(resTimes, 0, trimmedTimes, 0, resCount);
    System.arraycopy(resValues, 0, trimmedValues, 0, resCount);
    return newInstanceFast(trimmedTimes, trimmedValues);
  }

  @Override
  public FastLongDoubleTimeSeries unionOperate(final FastLongDoubleTimeSeries other, final BinaryOperator operator) { 
    final long[] aTimes = timesArrayFast();
    final double[] aValues = valuesArrayFast();
    int aCount = 0;
    final long[] bTimes = other.timesArrayFast();
    if (getEncoding() != other.getEncoding()) { // convert to a's format -- NOTE: if we switch to using an underlying array rather than a copy, we can't modify it in-place like we're doing here.
      DateTimeNumericEncoding aEncoding = getEncoding();
      DateTimeNumericEncoding bEncoding = other.getEncoding();
      for (int i = 0; i < bTimes.length; i++) {
        bTimes[i] = bEncoding.convertToLong(bTimes[i], aEncoding);
      }
    }
    final double[] bValues = other.valuesArrayFast();
    int bCount = 0;
    final long[] resTimes = new long[aTimes.length + bTimes.length];
    final double[] resValues = new double[resTimes.length];
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
    double[] trimmedValues = new double[resCount];
    System.arraycopy(resTimes, 0, trimmedTimes, 0, resCount);
    System.arraycopy(resValues, 0, trimmedValues, 0, resCount);
    return newInstanceFast(trimmedTimes, trimmedValues);
  }

  @Override
  public FastLongDoubleTimeSeries lag(final int days) {
    long[] times = timesArrayFast();
    double[] values = valuesArrayFast();
    if (days == 0) {
      return newInstanceFast(times, values);
    } else if (days < 0) {
      long[] resultTimes = new long[times.length + days]; // remember days is -ve
      System.arraycopy(times, 0, resultTimes, 0, times.length + days);
      Double[] resultValues = new Double[times.length + days];
      System.arraycopy(values, -days, resultValues, 0, times.length + days);
      return newInstanceFast(times, values);
    } else { // if (days > 0) {
      long[] resultTimes = new long[times.length - days]; // remember days is +ve
      System.arraycopy(times, days, resultTimes, 0, times.length - days);
      Double[] resultValues = new Double[times.length - days];
      System.arraycopy(values, 0, resultValues, 0, times.length - days);
      return newInstanceFast(times, values);
    }
  }

  @Override
  public FastLongDoubleTimeSeries subSeriesFast(final long startTime, final boolean includeStart, final long endTime, final boolean includeEnd) {
    return subSeriesFast(startTime + (includeStart ? 0 : 1), endTime + (includeEnd ? 1 : 0));
  }

  @Override
  public FastMutableIntDoubleTimeSeries toFastMutableIntDoubleTimeSeries() {
    return new FastListIntDoubleTimeSeries(this);
  }

  @Override
  public FastIntDoubleTimeSeries toFastIntDoubleTimeSeries() {
    return new FastArrayIntDoubleTimeSeries(this);
  }

  @Override
  public FastMutableLongDoubleTimeSeries toFastMutableLongDoubleTimeSeries() {
    return new FastListLongDoubleTimeSeries(this);
  }

  @Override
  public FastLongDoubleTimeSeries toFastLongDoubleTimeSeries() {
    return this;
  }

  @Override
  public FastMutableIntDoubleTimeSeries toFastMutableIntDoubleTimeSeries(DateTimeNumericEncoding encoding) {
    return new FastListIntDoubleTimeSeries(encoding, this);
  }

  @Override
  public FastIntDoubleTimeSeries toFastIntDoubleTimeSeries(DateTimeNumericEncoding encoding) {
    return new FastArrayIntDoubleTimeSeries(encoding, this);
  }

  @Override
  public FastMutableLongDoubleTimeSeries toFastMutableLongDoubleTimeSeries(DateTimeNumericEncoding encoding) {
    return new FastListLongDoubleTimeSeries(encoding, this);
  }

  @Override
  public FastLongDoubleTimeSeries toFastLongDoubleTimeSeries(DateTimeNumericEncoding encoding) {
    return new FastArrayLongDoubleTimeSeries(encoding, this);
  }

}
