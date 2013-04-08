/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.timeseries.date;

import java.util.AbstractList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import org.threeten.bp.ZoneId;

import com.opengamma.timeseries.TimeSeriesUtils;
import com.opengamma.timeseries.fast.DateTimeNumericEncoding;
import com.opengamma.timeseries.fast.integer.FastIntDoubleTimeSeries;
import com.opengamma.timeseries.fast.integer.FastListIntDoubleTimeSeries;
import com.opengamma.timeseries.fast.integer.FastMutableIntDoubleTimeSeries;
import com.opengamma.timeseries.fast.longint.FastArrayLongDoubleTimeSeries;
import com.opengamma.timeseries.fast.longint.FastListLongDoubleTimeSeries;
import com.opengamma.timeseries.fast.longint.FastLongDoubleTimeSeries;
import com.opengamma.timeseries.fast.longint.FastMutableLongDoubleTimeSeries;
import com.opengamma.timeseries.localdate.ListLocalDateDoubleTimeSeries;
import com.opengamma.timeseries.localdate.MutableLocalDateDoubleTimeSeries;
import com.opengamma.timeseries.zoneddatetime.ArrayZonedDateTimeDoubleTimeSeries;
import com.opengamma.timeseries.zoneddatetime.ListZonedDateTimeDoubleTimeSeries;
import com.opengamma.timeseries.zoneddatetime.MutableZonedDateTimeDoubleTimeSeries;
import com.opengamma.timeseries.zoneddatetime.ZonedDateTimeDoubleTimeSeries;

/**
 * Standard immutable implementation of {@code LocalDateDoubleTimeSeries}.
 * 
 * @param <T>  the date type
 */
public abstract class AbstractDateDoubleTimeSeries<T> implements DateDoubleTimeSeries<T> {

  /** Serialization version. */
  private static final long serialVersionUID = -1496963385919362510L;

  /**
   * Creates an instance.
   */
  public AbstractDateDoubleTimeSeries() {
  }

  //-------------------------------------------------------------------------
  /**
   * Converts the specified date to the {@code int} form.
   * 
   * @param date  the date to convert, not null
   * @return the {@code int} date
   */
  protected abstract int convertToInt(T date);

  /**
   * Converts the specified date from the {@code int} form.
   * 
   * @param date  the {@code int} date to convert
   * @return the date, not null
   */
  protected abstract T convertFromInt(int date);

  /**
   * Creates an array of the correct T type.
   * 
   * @param size  the size of the array to create
   * @return the array, not null
   */
  protected abstract T[] createArray(int size);

  //-------------------------------------------------------------------------
  @Override
  public boolean isEmpty() {
    return size() == 0;
  }

  //-------------------------------------------------------------------------
  @Override
  public boolean containsTime(T date) {
    return containsTime(convertToInt(date));
  }

  @Override
  public Double getValue(T date) {
    return getValue(convertToInt(date));
  }

  @Override
  public T getTimeAtIndex(int index) {
    return convertFromInt(getTimeAtIndexFast(index));
  }

  @Override
  public Double getValueAtIndex(int index) {
    return getValueAtIndexFast(index);
  }

  //-------------------------------------------------------------------------
  @Override
  public T getEarliestTime() {
    return convertFromInt(getEarliestTimeFast());
  }

  @Override
  public Double getEarliestValue() {
    return getEarliestValueFast();
  }

  @Override
  public T getLatestTime() {
    return convertFromInt(getLatestTimeFast());
  }

  @Override
  public Double getLatestValue() {
    return getLatestValueFast();
  }

  //-------------------------------------------------------------------------
  @Override
  public Iterator<T> timeIterator() {
    return new Iterator<T>() {
      private int _index = -1;
      @Override
      public boolean hasNext() {
        return _index < size();
      }
      @Override
      public T next() {
        if (hasNext() == false) {
          throw new NoSuchElementException("No more elements");
        }
        _index++;
        return getTimeAtIndex(_index);
      }
      @Override
      public void remove() {
        throw new UnsupportedOperationException("Immutable");
      }
    };
  }

  @Override
  public List<T> times() {
    return new AbstractList<T>() {
      @Override
      public T get(int index) {
        return getTimeAtIndex(index);
      }
      @Override
      public int size() {
        return AbstractDateDoubleTimeSeries.this.size();
      }
      @Override
      public Iterator<T> iterator() {
        return timeIterator();
      }
    };
  }

  @Override
  public T[] timesArray() {
    int[] times = timesArrayFast();
    T[] result = createArray(times.length);
    for (int i = 0; i < times.length; i++) {
      result[i] = convertFromInt(times[i]);
    }
    return result;
  }

  @Override
  public Iterator<Double> valuesIterator() {
    return new Iterator<Double>() {
      private int _index = -1;
      @Override
      public boolean hasNext() {
        return _index < size();
      }
      @Override
      public Double next() {
        if (hasNext() == false) {
          throw new NoSuchElementException("No more elements");
        }
        _index++;
        return getValueAtIndex(_index);
      }
      @Override
      public void remove() {
        throw new UnsupportedOperationException("Immutable");
      }
    };
  }

  @Override
  public List<Double> values() {
    return new AbstractList<Double>() {
      @Override
      public Double get(int index) {
        return getValueAtIndex(index);
      }
      @Override
      public int size() {
        return AbstractDateDoubleTimeSeries.this.size();
      }
      @Override
      public Iterator<Double> iterator() {
        return valuesIterator();
      }
    };
  }

  @Override
  public Double[] valuesArray() {
    double[] times = valuesArrayFast();
    Double[] result = new Double[times.length];
    for (int i = 0; i < times.length; i++) {
      result[i] = times[i];
    }
    return result;
  }

  //-------------------------------------------------------------------------
  private FastIntDoubleTimeSeries toFastIntDaysDTS() {
    return toFastIntDoubleTimeSeries(DateTimeNumericEncoding.DATE_EPOCH_DAYS);
  }

  private FastMutableIntDoubleTimeSeries toFastMutableIntDaysDTS() {
    return toFastMutableIntDoubleTimeSeries(DateTimeNumericEncoding.DATE_EPOCH_DAYS);
  }

  private FastLongDoubleTimeSeries toFastLongMillisDTS() {
    return toFastLongDoubleTimeSeries(DateTimeNumericEncoding.TIME_EPOCH_MILLIS);
  }

  private FastMutableLongDoubleTimeSeries toFastMutableLongMillisDTS() {
    return toFastMutableLongDoubleTimeSeries(DateTimeNumericEncoding.TIME_EPOCH_MILLIS);
  }

  @Override
  public FastIntDoubleTimeSeries toFastIntDoubleTimeSeries() {
    return toFastIntDaysDTS();
  }

  @Override
  public FastMutableIntDoubleTimeSeries toFastMutableIntDoubleTimeSeries() {
    return new FastListIntDoubleTimeSeries(toFastIntDoubleTimeSeries());
  }

  @Override
  public FastMutableIntDoubleTimeSeries toFastMutableIntDoubleTimeSeries(DateTimeNumericEncoding encoding) {
    return new FastListIntDoubleTimeSeries(encoding, toFastIntDoubleTimeSeries());
  }

  @Override
  public FastLongDoubleTimeSeries toFastLongDoubleTimeSeries() {
    return new FastArrayLongDoubleTimeSeries(toFastIntDaysDTS());
  }

  @Override
  public FastLongDoubleTimeSeries toFastLongDoubleTimeSeries(DateTimeNumericEncoding encoding) {
    return new FastArrayLongDoubleTimeSeries(encoding, toFastIntDaysDTS());
  }

  @Override
  public FastMutableLongDoubleTimeSeries toFastMutableLongDoubleTimeSeries() {
    return new FastListLongDoubleTimeSeries(toFastLongDoubleTimeSeries());
  }

  @Override
  public FastMutableLongDoubleTimeSeries toFastMutableLongDoubleTimeSeries(DateTimeNumericEncoding encoding) {
    return new FastListLongDoubleTimeSeries(encoding, toFastLongDoubleTimeSeries());
  }

  @Override
  public ZonedDateTimeDoubleTimeSeries toZonedDateTimeDoubleTimeSeries() {
    return new ArrayZonedDateTimeDoubleTimeSeries(toFastLongMillisDTS());
  }

  @Override
  public ZonedDateTimeDoubleTimeSeries toZonedDateTimeDoubleTimeSeries(ZoneId zone) {
    return new ArrayZonedDateTimeDoubleTimeSeries(zone, toFastLongMillisDTS());
  }

  @Override
  public MutableZonedDateTimeDoubleTimeSeries toMutableZonedDateTimeDoubleTimeSeries() {
    return new ListZonedDateTimeDoubleTimeSeries(toFastMutableLongMillisDTS());
  }

  @Override
  public MutableZonedDateTimeDoubleTimeSeries toMutableZonedDateTimeDoubleTimeSeries(ZoneId timeZone) {
    return new ListZonedDateTimeDoubleTimeSeries(timeZone, toFastMutableLongMillisDTS());
  }

  @Override
  public com.opengamma.timeseries.localdate.LocalDateDoubleTimeSeries toLocalDateDoubleTimeSeries() {
    return new com.opengamma.timeseries.localdate.ArrayLocalDateDoubleTimeSeries(toFastIntDaysDTS());
  }

  @Override
  public com.opengamma.timeseries.localdate.LocalDateDoubleTimeSeries toLocalDateDoubleTimeSeries(ZoneId zone) {
    return new com.opengamma.timeseries.localdate.ArrayLocalDateDoubleTimeSeries(zone, toFastIntDaysDTS());
  }

  @Override
  public MutableLocalDateDoubleTimeSeries toMutableLocalDateDoubleTimeSeries() {
    return new ListLocalDateDoubleTimeSeries(toFastMutableIntDaysDTS());
  }

  @Override
  public MutableLocalDateDoubleTimeSeries toMutableLocalDateDoubleTimeSeries(ZoneId zone) {
    return new ListLocalDateDoubleTimeSeries(zone, toFastMutableIntDaysDTS());
  }

  //-------------------------------------------------------------------------
  @Override
  public String toString() {
    return TimeSeriesUtils.toString(this);
  }

}
