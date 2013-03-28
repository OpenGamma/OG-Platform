/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.timeseries.fast.integer;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
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
import com.opengamma.timeseries.fast.longint.FastArrayLongDoubleTimeSeries;
import com.opengamma.timeseries.fast.longint.FastListLongDoubleTimeSeries;
import com.opengamma.timeseries.fast.longint.FastLongDoubleTimeSeries;
import com.opengamma.timeseries.fast.longint.FastMutableLongDoubleTimeSeries;

/**
 * Contains methods to make Primitive time series work with the normal
 * non-primitive time series interface (where possible)
 */
public abstract class AbstractFastIntDoubleTimeSeries
    extends AbstractFastTimeSeries<Integer>
    implements FastIntDoubleTimeSeries {

  /** Serialization version. */
  private static final long serialVersionUID = 5328672809268001876L;

  /**
   * The date-time encoding.
   */
  private final DateTimeNumericEncoding _encoding;

  protected AbstractFastIntDoubleTimeSeries(final DateTimeNumericEncoding encoding) {
    _encoding = encoding;
  }

  //-------------------------------------------------------------------------
  @Override
  public Double getValue(final Integer dateTime) {
    try {
      return getValueFast(dateTime);
    } catch (final NoSuchElementException nsee) {
      return null;
    } catch (final ArrayIndexOutOfBoundsException aioobe) {
      return null;
    }
  }

  @Override
  public Integer getTimeAt(final int index) {
    return getTimeFast(index);
  }

  @Override
  public Double getValueAt(final int index) {
    return getValueAtFast(index);
  }

  //-------------------------------------------------------------------------
  @Override
  public Integer getEarliestTime() {
    return getEarliestTimeFast();
  }

  @Override
  public Double getEarliestValue() {
    return getEarliestValueFast();
  }

  @Override
  public Integer getLatestTime() {
    return getLatestTimeFast();
  }

  @Override
  public Double getLatestValue() {
    return getLatestValueFast();
  }

  //-------------------------------------------------------------------------
  @Override
  public DoubleTimeSeries<Integer> subSeries(final Integer startTime, final Integer endTime) {
    return subSeriesFast(startTime, endTime);
  }

  @Override
  public DoubleTimeSeries<Integer> subSeries(final Integer startTime, final boolean includeStart, final Integer endTime, final boolean includeEnd) {
    return subSeriesFast(startTime, includeStart, endTime, includeEnd);
  }

  @Override
  public Iterator<Integer> timeIterator() {
    return timesIteratorFast();
  }

  @Override
  public List<Integer> times() {
    return timesFast();
  }

  @Override
  public Integer[] timesArray() {
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

  @SuppressWarnings("unchecked")
  @Override
  public Iterator<Map.Entry<Integer, Double>> iterator() {
    return (Iterator<Map.Entry<Integer, Double>>) (Iterator<? extends Map.Entry<Integer, Double>>) iteratorFast();
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
  public DoubleTimeSeries<Integer> newInstance(final Integer[] times, final Double[] values) {
    return newInstanceFast(TimeSeriesUtils.toPrimitive(times), TimeSeriesUtils.toPrimitive(values));
  }

  @Override
  public FastIntDoubleTimeSeries operate(final UnaryOperator operator) {
    final int[] aTimes = timesArrayFast();
    final double[] aValues = valuesArrayFast();
    final double[] results = new double[aValues.length]; // could modify in place, but will probably switch to in-place view of backing array.
    for (int i = 0; i < aValues.length; i++) {
      results[i] = operator.operate(aValues[i]);
    }
    return newInstanceFast(aTimes, results);
  }

  @Override
  public FastIntDoubleTimeSeries operate(final double other, final BinaryOperator operator) {
    final int[] aTimes = timesArrayFast();
    final double[] aValues = valuesArrayFast();
    final double[] results = new double[aValues.length]; // could modify in place, but will probably switch to in-place view of backing array.
    for (int i = 0; i < aValues.length; i++) {
      results[i] = operator.operate(aValues[i], other);
    }
    return newInstanceFast(aTimes, results);
  }

  @Override
  public FastIntDoubleTimeSeries operate(final FastBackedDoubleTimeSeries<?> other, final BinaryOperator operator) {
    final FastTimeSeries<?> fastSeries = other.getFastSeries();
    if (fastSeries instanceof FastIntDoubleTimeSeries) {
      return operate((FastIntDoubleTimeSeries) fastSeries, operator);
    } else { // if (fastSeries instanceof FastLongDoubleTimeSeries
      return operate((FastLongDoubleTimeSeries) fastSeries, operator);
    }
  }

  @Override
  public FastIntDoubleTimeSeries operate(final FastLongDoubleTimeSeries other, final BinaryOperator operator) {
    final int[] aTimes = timesArrayFast();
    final double[] aValues = valuesArrayFast();
    int aCount = 0;
    final long[] bTimesLong = other.timesArrayFast();
    final int[] bTimes = new int[bTimesLong.length];
    if (getEncoding() != other.getEncoding()) { // convert to a's format -- NOTE: if we switch to using an underlying array rather than a copy, we can't modify it in-place like we're doing here.
      final DateTimeNumericEncoding aEncoding = getEncoding();
      final DateTimeNumericEncoding bEncoding = other.getEncoding();
      for (int i = 0; i < bTimes.length; i++) {
        bTimes[i] = bEncoding.convertToInt(bTimesLong[i], aEncoding);
      }
    } else {
      for (int i = 0; i < bTimes.length; i++) {
        bTimes[i] = (int) bTimesLong[i];
      }
    }
    final double[] bValues = other.valuesArrayFast();
    int bCount = 0;
    final int[] resTimes = new int[aTimes.length + bTimes.length];
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
    final int[] trimmedTimes = new int[resCount];
    final double[] trimmedValues = new double[resCount];
    System.arraycopy(resTimes, 0, trimmedTimes, 0, resCount);
    System.arraycopy(resValues, 0, trimmedValues, 0, resCount);
    return newInstanceFast(trimmedTimes, trimmedValues);
  }

  @Override
  public FastIntDoubleTimeSeries operate(final FastIntDoubleTimeSeries other, final BinaryOperator operator) {
    final int[] aTimes = timesArrayFast();
    final double[] aValues = valuesArrayFast();
    int aCount = 0;
    final int[] bTimes = other.timesArrayFast();
    if (getEncoding() != other.getEncoding()) { // convert to a's format -- NOTE: if we switch to using an underlying array rather than a copy, we can't modify it in-place like we're doing here.
      final DateTimeNumericEncoding aEncoding = getEncoding();
      final DateTimeNumericEncoding bEncoding = other.getEncoding();
      for (int i = 0; i < bTimes.length; i++) {
        bTimes[i] = bEncoding.convertToInt(bTimes[i], aEncoding);
      }
    }
    final double[] bValues = other.valuesArrayFast();
    int bCount = 0;
    final int[] resTimes = new int[aTimes.length + bTimes.length];
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
    final int[] trimmedTimes = new int[resCount];
    final double[] trimmedValues = new double[resCount];
    System.arraycopy(resTimes, 0, trimmedTimes, 0, resCount);
    System.arraycopy(resValues, 0, trimmedValues, 0, resCount);
    return newInstanceFast(trimmedTimes, trimmedValues);
  }

  @Override
  public FastIntDoubleTimeSeries unionOperate(final FastBackedDoubleTimeSeries<?> other, final BinaryOperator operator) {
    final FastTimeSeries<?> fastSeries = other.getFastSeries();
    if (fastSeries instanceof FastIntDoubleTimeSeries) {
      return unionOperate((FastIntDoubleTimeSeries) fastSeries, operator);
    } else { // if (fastSeries instanceof FastLongDoubleTimeSeries
      return unionOperate((FastLongDoubleTimeSeries) fastSeries, operator);
    }
  }

  @Override
  public FastIntDoubleTimeSeries unionOperate(final FastIntDoubleTimeSeries other, final BinaryOperator operator) {
    final int[] aTimes = timesArrayFast();
    final double[] aValues = valuesArrayFast();
    int aCount = 0;
    final int[] bTimes = other.timesArrayFast();
    if (getEncoding() != other.getEncoding()) { // convert to a's format -- NOTE: if we switch to using an underlying array rather than a copy, we can't modify it in-place like we're doing here.
      final DateTimeNumericEncoding aEncoding = getEncoding();
      final DateTimeNumericEncoding bEncoding = other.getEncoding();
      for (int i = 0; i < bTimes.length; i++) {
        bTimes[i] = bEncoding.convertToInt(bTimes[i], aEncoding);
      }
    }
    final double[] bValues = other.valuesArrayFast();
    int bCount = 0;
    final int[] resTimes = new int[aTimes.length + bTimes.length];
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
    final int[] trimmedTimes = new int[resCount];
    final double[] trimmedValues = new double[resCount];
    System.arraycopy(resTimes, 0, trimmedTimes, 0, resCount);
    System.arraycopy(resValues, 0, trimmedValues, 0, resCount);
    return newInstanceFast(trimmedTimes, trimmedValues);
  }

  @Override
  public FastIntDoubleTimeSeries unionOperate(final FastLongDoubleTimeSeries other, final BinaryOperator operator) {
    final int[] aTimes = timesArrayFast();
    final double[] aValues = valuesArrayFast();
    int aCount = 0;
    final long[] bTimesLong = other.timesArrayFast();
    final int[] bTimes = new int[bTimesLong.length];
    if (getEncoding() != other.getEncoding()) { // convert to a's format -- NOTE: if we switch to using an underlying array rather than a copy, we can't modify it in-place like we're doing here.
      final DateTimeNumericEncoding aEncoding = getEncoding();
      final DateTimeNumericEncoding bEncoding = other.getEncoding();
      for (int i = 0; i < bTimes.length; i++) {
        bTimes[i] = bEncoding.convertToInt(bTimesLong[i], aEncoding);
      }
    } else {
      for (int i = 0; i < bTimes.length; i++) {
        bTimes[i] = (int) bTimesLong[i];
      }
    }
    final double[] bValues = other.valuesArrayFast();
    int bCount = 0;
    final int[] resTimes = new int[aTimes.length + bTimes.length];
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
    final int[] trimmedTimes = new int[resCount];
    final double[] trimmedValues = new double[resCount];
    System.arraycopy(resTimes, 0, trimmedTimes, 0, resCount);
    System.arraycopy(resValues, 0, trimmedValues, 0, resCount);
    return newInstanceFast(trimmedTimes, trimmedValues);
  }

  @Override
  public FastIntDoubleTimeSeries lag(final int days) {
    final int[] times = timesArrayFast();
    final double[] values = valuesArrayFast();
    if (days == 0) {
      return newInstanceFast(times, values);
    } else if (days < 0) {
      if (-days < times.length) {
        final int[] resultTimes = new int[times.length + days]; // remember days is -ve
        System.arraycopy(times, 0, resultTimes, 0, times.length + days);
        final double[] resultValues = new double[times.length + days];
        System.arraycopy(values, -days, resultValues, 0, times.length + days);
        return newInstanceFast(resultTimes, resultValues);
      } else {
        return newInstanceFast(new int[0], new double[0]);
      }
    } else { // if (days > 0) {
      if (days < times.length) {
        final int[] resultTimes = new int[times.length - days]; // remember days is +ve
        System.arraycopy(times, days, resultTimes, 0, times.length - days);
        final double[] resultValues = new double[times.length - days];
        System.arraycopy(values, 0, resultValues, 0, times.length - days);
        return newInstanceFast(resultTimes, resultValues);
      } else {
        return newInstanceFast(new int[0], new double[0]);
      }
    }
  }

  @Override
  public FastIntDoubleTimeSeries subSeriesFast(int startTime, final boolean includeStart, int endTime, final boolean includeEnd) {
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
    return this;
  }

  @Override
  public FastMutableLongDoubleTimeSeries toFastMutableLongDoubleTimeSeries() {
    return new FastListLongDoubleTimeSeries(this);
  }

  @Override
  public FastLongDoubleTimeSeries toFastLongDoubleTimeSeries() {
    return new FastArrayLongDoubleTimeSeries(this);
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
