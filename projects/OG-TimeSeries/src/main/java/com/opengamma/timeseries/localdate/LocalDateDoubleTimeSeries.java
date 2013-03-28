/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.timeseries.localdate;

import java.util.NoSuchElementException;
import java.util.Map.Entry;

import org.threeten.bp.LocalDate;

import com.opengamma.timeseries.AbstractIntDoubleTimeSeries;
import com.opengamma.timeseries.AbstractLongDoubleTimeSeries;
import com.opengamma.timeseries.DateTimeConverter;
import com.opengamma.timeseries.DoubleTimeSeries;
import com.opengamma.timeseries.DoubleTimeSeriesOperators.UnaryOperator;
import com.opengamma.timeseries.FastBackedDoubleTimeSeries;
import com.opengamma.timeseries.TimeSeriesUtils;
import com.opengamma.timeseries.fast.integer.FastIntDoubleTimeSeries;
import com.opengamma.timeseries.fast.longint.FastLongDoubleTimeSeries;

/**
 * Abstraction of a time series that stores {@code double} data values against {@link LocalDate} dates. 
 */
public interface LocalDateDoubleTimeSeries extends DoubleTimeSeries<LocalDate>, FastBackedDoubleTimeSeries<LocalDate> {

  /**
   * Gets an iterator over the date-value pairs.
   * <p>
   * Although the pairs are expressed as instances of {@code Map.Entry},
   * it is recommended to use the primitive methods on {@code LocalDateDoubleIterator}.
   * 
   * @return the iterator, not null
   */
  LocalDateDoubleIterator iterator();

  /**
   * Gets the value associated with the time, specifying the primitive {@code int} date.
   * 
   * @param date  the primitive date equivalent to a {@code LocalDate}
   * @return the matching value, null if there is no value for the date
   */
  Double getValue(int date);

  //-------------------------------------------------------------------------
  @Override  // override for covariant return type
  LocalDateDoubleTimeSeries subSeries(LocalDate startTime, boolean includeStart, LocalDate endTime, boolean includeEnd);

  @Override  // override for covariant return type
  LocalDateDoubleTimeSeries subSeries(LocalDate startTime, LocalDate endTime);

  @Override  // override for covariant return type
  LocalDateDoubleTimeSeries head(int numItems);

  @Override  // override for covariant return type
  LocalDateDoubleTimeSeries tail(int numItems);

  @Override  // override for covariant return type
  LocalDateDoubleTimeSeries lag(final int lagCount);

  @Override  // override for covariant return type
  LocalDateDoubleTimeSeries operate(UnaryOperator operator);

  //-------------------------------------------------------------------------
  /**
   * Partial implementation of the {@link LocalDateDoubleTimeSeries} that uses an {@code integer} representation of the date.
   */
  public abstract static class Integer extends AbstractIntDoubleTimeSeries<LocalDate> implements LocalDateDoubleTimeSeries {
    private static final long serialVersionUID = 1L;

    public Integer(final DateTimeConverter<LocalDate> converter, final FastIntDoubleTimeSeries timeSeries) {
      super(converter, timeSeries);
    }

    //-------------------------------------------------------------------------
    @Override
    public LocalDateDoubleIterator iterator() {
      return new LocalDateDoubleIterator() {
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
          int date = getFastSeries().getTimeFast(_index);
          Double value = getFastSeries().getValueAt(_index);
          return getConverter().makeMapEntry(getConverter().convertFromInt(date), value);
        }
        @Override
        public int nextDate() {
          if (hasNext() == false) {
            throw new NoSuchElementException("No more elements in the iteration");
          }
          _index++;
          return getFastSeries().getTimeFast(_index);
        }
        @Override
        public LocalDate nextLocalDate() {
          return getConverter().convertFromInt(nextDate());
        }
        @Override
        public int currentDate() {
          if (_index < 0) {
            throw new IllegalStateException("Iterator has nor has nextDate() or next() called yet");
          }
          return getFastSeries().getTimeFast(_index);
        }
        @Override
        public LocalDate currentLocalDate() {
          return getConverter().convertFromInt(currentDate());
        }
        @Override
        public double currentValue() {
          if (_index < 0) {
            throw new IllegalStateException("Iterator has nor has nextDate() or next() called yet");
          }
          return getFastSeries().getValueAtFast(_index);
        }
        @Override
        public void remove() {
          throw new UnsupportedOperationException("Immutable iterator");
        }
      };
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

  //-------------------------------------------------------------------------
  /**
   * Partial implementation of the {@link LocalDateDoubleTimeSeries} that uses a {@code long} representation of the date.
   */
  public abstract static class Long extends AbstractLongDoubleTimeSeries<LocalDate> implements LocalDateDoubleTimeSeries {
    private static final long serialVersionUID = 1L;

    public Long(final DateTimeConverter<LocalDate> converter, final FastLongDoubleTimeSeries timeSeries) {
      super(converter, timeSeries);
    }

    //-------------------------------------------------------------------------
    @Override
    public LocalDateDoubleIterator iterator() {
      return new LocalDateDoubleIterator() {
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
          long date = getFastSeries().getTimeFast(_index);
          Double value = getFastSeries().getValueAt(_index);
          return getConverter().makeMapEntry(getConverter().convertFromLong(date), value);
        }
        @Override
        public int nextDate() {
          throw new UnsupportedOperationException("long dates are not supported");
        }
        @Override
        public LocalDate nextLocalDate() {
          return getConverter().convertFromLong(nextDate());
        }
        @Override
        public int currentDate() {
          throw new UnsupportedOperationException("long dates are not supported");
        }
        @Override
        public LocalDate currentLocalDate() {
          return getConverter().convertFromLong(currentDate());
        }
        @Override
        public double currentValue() {
          if (_index < 0) {
            throw new IllegalStateException("Iterator has nor has nextDate() or next() called yet");
          }
          return getFastSeries().getValueAtFast(_index);
        }
        @Override
        public void remove() {
          throw new UnsupportedOperationException("Immutable iterator");
        }
      };
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
  }

}
