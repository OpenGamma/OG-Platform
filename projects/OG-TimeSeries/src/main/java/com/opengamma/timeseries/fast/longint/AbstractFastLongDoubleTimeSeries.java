/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.timeseries.fast.longint;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import com.opengamma.timeseries.DoubleTimeSeries;
import com.opengamma.timeseries.FastBackedDoubleTimeSeries;
import com.opengamma.timeseries.TimeSeriesUtils;
import com.opengamma.timeseries.DoubleTimeSeriesOperators.BinaryOperator;
import com.opengamma.timeseries.DoubleTimeSeriesOperators.UnaryOperator;
import com.opengamma.timeseries.fast.AbstractFastTimeSeries;
import com.opengamma.timeseries.fast.DateTimeNumericEncoding;
import com.opengamma.timeseries.fast.DateTimeResolution;
import com.opengamma.timeseries.fast.FastTimeSeries;
import com.opengamma.timeseries.fast.integer.FastArrayIntDoubleTimeSeries;
import com.opengamma.timeseries.fast.integer.FastIntDoubleTimeSeries;
import com.opengamma.timeseries.fast.integer.FastListIntDoubleTimeSeries;
import com.opengamma.timeseries.fast.integer.FastMutableIntDoubleTimeSeries;

/**
 * Contains methods to make Primitive time series work with the normal
 * non-primitive time series interface (where possible)
 */
public abstract class AbstractFastLongDoubleTimeSeries
    extends AbstractFastTimeSeries<Long>
    implements FastLongDoubleTimeSeries {

  /** Serialization version. */
  private static final long serialVersionUID = -7004318382647061986L;

  /**
   * The date-time encoding.
   */
  private final DateTimeNumericEncoding _encoding;

  protected AbstractFastLongDoubleTimeSeries(final DateTimeNumericEncoding encoding) {
    _encoding = encoding;
  }

  //-------------------------------------------------------------------------
  @Override
  public Double getValue(final Long dateTime) {
    try {
      return getValueFast(dateTime);
    } catch (final NoSuchElementException nsee) {
      return null;
    } catch (final ArrayIndexOutOfBoundsException aioobe) {
      return null;
    }
  }

  @Override
  public Long getTimeAt(final int index) {
    return getTimeFast(index);
  }

  @Override
  public Double getValueAt(final int index) {
    return getValueAtFast(index);
  }

  //-------------------------------------------------------------------------
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

  //-------------------------------------------------------------------------
  @Override
  public DoubleTimeSeries<Long> subSeries(final Long startTime, final Long endTime) {
    return subSeriesFast(startTime, endTime);
  }

  @Override
  public DoubleTimeSeries<Long> subSeries(final Long startTime, final boolean includeStart, final Long endTime, final boolean includeEnd) {
    return subSeriesFast(startTime, includeStart, endTime, includeEnd);
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
    return TimeSeriesUtils.toObject(timesArrayFast());
  }

  @Override
  public List<Double> values() {
    return valuesFast();
  }

  @Override
  public Double[] valuesArray() {
    return TimeSeriesUtils.toObject(valuesArrayFast());
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
    return newInstanceFast(TimeSeriesUtils.toPrimitive(times), TimeSeriesUtils.toPrimitive(values));
  }

  @Override
  public FastLongDoubleTimeSeries operate(final UnaryOperator operator) {
    final long[] aTimes = timesArrayFast();
    final double[] aValues = valuesArrayFast();
    final double[] results = new double[aValues.length]; // could modify in place, but will probably switch to in-place view of backing array.
    for (int i = 0; i < aValues.length; i++) {
      results[i] = operator.operate(aValues[i]);
    }
    return newInstanceFast(aTimes, results);
  }

  @Override
  public FastLongDoubleTimeSeries operate(final double other, final BinaryOperator operator) {
    final long[] aTimes = timesArrayFast();
    final double[] aValues = valuesArrayFast();
    final double[] results = new double[aValues.length]; // could modify in place, but will probably switch to in-place view of backing array.
    for (int i = 0; i < aValues.length; i++) {
      results[i] = operator.operate(aValues[i], other);
    }
    return newInstanceFast(aTimes, results);
  }

  @Override
  public FastLongDoubleTimeSeries operate(final FastBackedDoubleTimeSeries<?> other, final BinaryOperator operator) {
    final FastTimeSeries<?> fastSeries = other.getFastSeries();
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
      final DateTimeNumericEncoding aEncoding = getEncoding();
      final DateTimeNumericEncoding bEncoding = other.getEncoding();
      for (int i = 0; i < bTimes.length; i++) {
        bTimes[i] = bEncoding.convertToLong(bTimes[i], aEncoding);
      }
    }
    final double[] bValues = other.valuesArrayFast();
    int bCount = 0;
    final long[] resTimes = new long[Math.min(aTimes.length, bTimes.length)];
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
    if (resTimes.length == resCount) {
      return newInstanceFast(resTimes, resValues);
    }
    final long[] trimmedTimes = new long[resCount];
    final double[] trimmedValues = new double[resCount];
    System.arraycopy(resTimes, 0, trimmedTimes, 0, resCount);
    System.arraycopy(resValues, 0, trimmedValues, 0, resCount);
    return newInstanceFast(trimmedTimes, trimmedValues);
  }

  @Override
  protected FastTimeSeries<Long> intersectionFirstValueFast(final FastLongDoubleTimeSeries other) {
    //PLAT-1590
    final long[] aTimes = timesArrayFast();
    final double[] aValues = valuesArrayFast();
    int aCount = 0;
    final long[] bTimes = other.timesArrayFast();
    if (getEncoding() != other.getEncoding()) { // convert to a's format -- NOTE: if we switch to using an underlying array rather than a copy, we can't modify it in-place like we're doing here.
      final DateTimeNumericEncoding aEncoding = getEncoding();
      final DateTimeNumericEncoding bEncoding = other.getEncoding();
      for (int i = 0; i < bTimes.length; i++) {
        bTimes[i] = bEncoding.convertToLong(bTimes[i], aEncoding);
      }
    }

    int bCount = 0;
    final long[] resTimes = new long[Math.min(aTimes.length, bTimes.length)];
    final double[] resValues = new double[resTimes.length];
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
    if (resTimes.length == resCount) {
      return newInstanceFast(resTimes, resValues);
    }
    final long[] trimmedTimes = new long[resCount];
    final double[] trimmedValues = new double[resCount];
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
      final DateTimeNumericEncoding aEncoding = getEncoding();
      final DateTimeNumericEncoding bEncoding = other.getEncoding();
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
    final long[] trimmedTimes = new long[resCount];
    final double[] trimmedValues = new double[resCount];
    System.arraycopy(resTimes, 0, trimmedTimes, 0, resCount);
    System.arraycopy(resValues, 0, trimmedValues, 0, resCount);
    return newInstanceFast(trimmedTimes, trimmedValues);
  }

  @Override
  public FastLongDoubleTimeSeries unionOperate(final FastBackedDoubleTimeSeries<?> other, final BinaryOperator operator) {
    final FastTimeSeries<?> fastSeries = other.getFastSeries();
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
      final DateTimeNumericEncoding aEncoding = getEncoding();
      final DateTimeNumericEncoding bEncoding = other.getEncoding();
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
        final int bRemaining = bTimes.length - bCount;
        System.arraycopy(bTimes, bCount, resTimes, resCount, bRemaining);
        System.arraycopy(bValues, bCount, resValues, resCount, bRemaining);
        resCount += bRemaining;
        break;
      } else if (bCount >= bTimes.length) {
        final int aRemaining = aTimes.length - aCount;
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
    final long[] trimmedTimes = new long[resCount];
    final double[] trimmedValues = new double[resCount];
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
      final DateTimeNumericEncoding aEncoding = getEncoding();
      final DateTimeNumericEncoding bEncoding = other.getEncoding();
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
        final int bRemaining = bTimes.length - bCount;
        System.arraycopy(bTimes, bCount, resTimes, resCount, bRemaining);
        System.arraycopy(bValues, bCount, resValues, resCount, bRemaining);
        resCount += bRemaining;
        break;
      } else if (bCount >= bTimes.length) {
        final int aRemaining = aTimes.length - aCount;
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
    final long[] trimmedTimes = new long[resCount];
    final double[] trimmedValues = new double[resCount];
    System.arraycopy(resTimes, 0, trimmedTimes, 0, resCount);
    System.arraycopy(resValues, 0, trimmedValues, 0, resCount);
    return newInstanceFast(trimmedTimes, trimmedValues);
  }

  @Override
  public FastLongDoubleTimeSeries lag(final int days) {
    final long[] times = timesArrayFast();
    final double[] values = valuesArrayFast();
    if (days == 0) {
      // REVIEW Andrew 2013-01-24 -- Why not just return "this" ?
      return newInstanceFast(times, values);
    } else if (days < 0) {
      if (-days < times.length) {
        final long[] resultTimes = new long[times.length + days]; // remember days is -ve
        System.arraycopy(times, 0, resultTimes, 0, times.length + days);
        final double[] resultValues = new double[times.length + days];
        System.arraycopy(values, -days, resultValues, 0, times.length + days);
        return newInstanceFast(resultTimes, resultValues);
      } else {
        return newInstanceFast(new long[0], new double[0]);
      }
    } else { // if (days > 0) {
      if (days < times.length) {
        final long[] resultTimes = new long[times.length - days]; // remember days is +ve
        System.arraycopy(times, days, resultTimes, 0, times.length - days);
        final double[] resultValues = new double[times.length - days];
        System.arraycopy(values, 0, resultValues, 0, times.length - days);
        return newInstanceFast(resultTimes, resultValues);
      } else {
        return newInstanceFast(new long[0], new double[0]);
      }
    }
  }

  @Override
  public FastLongDoubleTimeSeries subSeriesFast(long startTime, final boolean includeStart, long endTime, final boolean includeEnd) {
    if (startTime != endTime || includeStart || includeEnd) {
      startTime += (includeStart ? 0 : 1);
      endTime += (includeEnd ? 1 : 0);
    }
    return subSeriesFast(startTime, endTime);
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
  public FastMutableIntDoubleTimeSeries toFastMutableIntDoubleTimeSeries(final DateTimeNumericEncoding encoding) {
    return new FastListIntDoubleTimeSeries(encoding, this);
  }

  @Override
  public FastIntDoubleTimeSeries toFastIntDoubleTimeSeries(final DateTimeNumericEncoding encoding) {
    return new FastArrayIntDoubleTimeSeries(encoding, this);
  }

  @Override
  public FastMutableLongDoubleTimeSeries toFastMutableLongDoubleTimeSeries(final DateTimeNumericEncoding encoding) {
    return new FastListLongDoubleTimeSeries(encoding, this);
  }

  @Override
  public FastLongDoubleTimeSeries toFastLongDoubleTimeSeries(final DateTimeNumericEncoding encoding) {
    return new FastArrayLongDoubleTimeSeries(encoding, this);
  }

}
