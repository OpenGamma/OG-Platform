/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.timeseries.localdate;

import java.util.Map.Entry;
import java.util.NoSuchElementException;

import org.threeten.bp.LocalDate;

import com.opengamma.timeseries.AbstractIntObjectTimeSeries;
import com.opengamma.timeseries.DateTimeConverter;
import com.opengamma.timeseries.ObjectTimeSeries;
import com.opengamma.timeseries.fast.integer.object.FastIntObjectTimeSeries;

/**
 * Partial implementation of the {@link LocalDateObjectTimeSeries} that uses
 * an {@code int} representation of the date.
 * 
 * @param <V>  the type of the values
 */
public abstract class AbstractLocalDateObjectTimeSeries<V>
    extends AbstractIntObjectTimeSeries<LocalDate, V>
    implements LocalDateObjectTimeSeries<V> {

  /** Serialization version. */
  private static final long serialVersionUID = -4801663305568678025L;

  /**
   * Creates an instance.
   * 
   * @param converter  the converter to use, not null
   * @param timeSeries  the underlying time-series, not null
   */
  public AbstractLocalDateObjectTimeSeries(final DateTimeConverter<LocalDate> converter, final FastIntObjectTimeSeries<V> timeSeries) {
    super(converter, timeSeries);
  }

  //-------------------------------------------------------------------------
  @Override
  public boolean containsTime(int date) {
    return getFastSeries().containsTime(date);
  }

  @Override
  public V getValue(int time) {
    try {
      return getFastSeries().getValueFast(time);
    } catch (NoSuchElementException ex) {
      return null;
    }
  }

  //-------------------------------------------------------------------------
  @Override
  public int getTimeAtIndexFast(int index) {
    return getFastSeries().getTimeFast(index);
  }

  //-------------------------------------------------------------------------
  @Override
  public int getEarliestTimeFast() {
    return getFastSeries().getEarliestTimeFast();
  }

  @Override
  public int getLatestTimeFast() {
    return getFastSeries().getLatestTimeFast();
  }

  //-------------------------------------------------------------------------
  @Override
  public LocalDateObjectEntryIterator<V> iterator() {
    return new LocalDateObjectEntryIterator<V>() {
      private int _index = -1;

      @Override
      public boolean hasNext() {
        return (_index + 1) < size();
      }

      @Override
      public Entry<LocalDate, V> next() {
        if (hasNext() == false) {
          throw new NoSuchElementException("No more elements in the iteration");
        }
        _index++;
        int date = getFastSeries().getTimeFast(_index);
        V value = getFastSeries().getValueAtIndex(_index);
        return getConverter().makeMapEntry(getConverter().convertFromInt(date), value);
      }

      @Override
      public int nextTimeFast() {
        if (hasNext() == false) {
          throw new NoSuchElementException("No more elements in the iteration");
        }
        _index++;
        return getFastSeries().getTimeFast(_index);
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
        return getFastSeries().getTimeFast(_index);
      }

      @Override
      public LocalDate currentTime() {
        return getConverter().convertFromInt(currentTimeFast());
      }

      @Override
      public V currentValue() {
        if (_index < 0) {
          throw new IllegalStateException("Iterator has nor has nextDate() or next() called yet");
        }
        return getFastSeries().getValueAtFast(_index);
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
  public ObjectTimeSeries<LocalDate, V> newInstance(final LocalDate[] dateTimes, final V[] values) {
    return newInstanceFast(dateTimes, values);
  }

  public abstract LocalDateObjectTimeSeries<V> newInstanceFast(LocalDate[] dateTimes, V[] values);

}
