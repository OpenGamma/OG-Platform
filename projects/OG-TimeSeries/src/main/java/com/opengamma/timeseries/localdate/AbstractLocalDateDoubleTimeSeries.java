/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.timeseries.localdate;

import java.util.Map.Entry;
import java.util.NoSuchElementException;

import org.threeten.bp.LocalDate;

import com.opengamma.timeseries.AbstractIntDoubleTimeSeries;
import com.opengamma.timeseries.DateTimeConverter;
import com.opengamma.timeseries.DoubleTimeSeriesOperators.UnaryOperator;
import com.opengamma.timeseries.TimeSeriesUtils;
import com.opengamma.timeseries.fast.integer.FastIntDoubleTimeSeries;

/**
 * Partial implementation of the {@link LocalDateDoubleTimeSeries} that uses
 * an {@code int} representation of the date.
 */
public abstract class AbstractLocalDateDoubleTimeSeries
    extends AbstractIntDoubleTimeSeries<LocalDate>
    implements LocalDateDoubleTimeSeries {

  /** Serialization version. */
  private static final long serialVersionUID = -992587881504589146L;

  /**
   * Creates an instance.
   * 
   * @param converter  the converter to use, not null
   * @param timeSeries  the underlying time-series, not null
   */
  public AbstractLocalDateDoubleTimeSeries(final DateTimeConverter<LocalDate> converter, final FastIntDoubleTimeSeries timeSeries) {
    super(converter, timeSeries);
  }

  //-------------------------------------------------------------------------
  @Override
  public boolean containsTime(int date) {
    return getFastSeries().containsTime(date);
  }

  @Override
  public Double getValue(int time) {
    try {
      return getFastSeries().getValueFast(time);
    } catch (NoSuchElementException ex) {
      return null;
    }
  }

  //-------------------------------------------------------------------------
  @Override
  public int getTimeAtIndexFast(int index) {
    return getFastSeries().getTimeAtIndexFast(index);
  }

  @Override
  public double getValueAtIndexFast(int index) {
    return getFastSeries().getValueAtIndexFast(index);
  }

  //-------------------------------------------------------------------------
  @Override
  public int getEarliestTimeFast() {
    return getFastSeries().getEarliestTimeFast();
  }

  @Override
  public double getEarliestValueFast() {
    return getFastSeries().getEarliestTimeFast();
  }

  @Override
  public int getLatestTimeFast() {
    return getFastSeries().getLatestTimeFast();
  }

  @Override
  public double getLatestValueFast() {
    return getFastSeries().getLatestTimeFast();
  }

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
        int date = getFastSeries().getTimeAtIndexFast(_index);
        Double value = getFastSeries().getValueAtIndex(_index);
        return getConverter().makeMapEntry(getConverter().convertFromInt(date), value);
      }

      @Override
      public int nextTimeFast() {
        if (hasNext() == false) {
          throw new NoSuchElementException("No more elements in the iteration");
        }
        _index++;
        return getFastSeries().getTimeAtIndexFast(_index);
      }

      @Override
      public LocalDate nextTime() {
        return getConverter().convertFromInt(nextTimeFast());
      }

      @Override
      public int currentTimeFast() {
        if (_index < 0) {
          throw new IllegalStateException("Iterator has nor has nextDate() or next() called yet");
        }
        return getFastSeries().getTimeAtIndexFast(_index);
      }

      @Override
      public LocalDate currentTime() {
        return getConverter().convertFromInt(currentTimeFast());
      }

      @Override
      public double currentValue() {
        if (_index < 0) {
          throw new IllegalStateException("Iterator has nor has nextDate() or next() called yet");
        }
        return getFastSeries().getValueAtIndexFast(_index);
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

  @Override
  public int[] timesArrayFast() {
    return getFastSeries().timesArrayFast();
  }

  //-------------------------------------------------------------------------
  @Override
  public LocalDateDoubleTimeSeries subSeries(LocalDate startTime, boolean includeStart, LocalDate endTime, boolean includeEnd) {
    return (LocalDateDoubleTimeSeries) super.subSeries(startTime, includeStart, endTime, includeEnd);
  }

  @Override
  public LocalDateDoubleTimeSeries subSeries(LocalDate startTime, LocalDate endTime) {
    return (LocalDateDoubleTimeSeries) super.subSeries(startTime, endTime);
  }

  @Override
  public LocalDateDoubleTimeSeries head(int numItems) {
    return (LocalDateDoubleTimeSeries) super.head(numItems);
  }

  @Override
  public LocalDateDoubleTimeSeries tail(int numItems) {
    return (LocalDateDoubleTimeSeries) super.tail(numItems);
  }

  @Override
  public LocalDateDoubleTimeSeries lag(final int lagCount) {
    return (LocalDateDoubleTimeSeries) super.lag(lagCount);
  }

  @Override
  public LocalDateDoubleTimeSeries operate(final UnaryOperator operator) {
    return (LocalDateDoubleTimeSeries) super.operate(operator);
  }

  @Override
  public LocalDateDoubleTimeSeries newInstance(final LocalDate[] dateTimes, final Double[] values) {
    return newInstanceFast(dateTimes, TimeSeriesUtils.toPrimitive(values));
  }

  public abstract LocalDateDoubleTimeSeries newInstanceFast(LocalDate[] dateTimes, double[] values);

  @Override
  public LocalDateDoubleTimeSeries toLocalDateDoubleTimeSeries() {
    return this;
  }

}
