/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.timeseries.localdate;

import java.util.NoSuchElementException;
import java.util.Map.Entry;

import org.threeten.bp.LocalDate;

import com.opengamma.timeseries.AbstractMutableIntDoubleTimeSeries;
import com.opengamma.timeseries.AbstractMutableLongDoubleTimeSeries;
import com.opengamma.timeseries.DateTimeConverter;
import com.opengamma.timeseries.MutableDoubleTimeSeries;
import com.opengamma.timeseries.TimeSeriesUtils;
import com.opengamma.timeseries.DoubleTimeSeriesOperators.UnaryOperator;
import com.opengamma.timeseries.fast.integer.FastMutableIntDoubleTimeSeries;
import com.opengamma.timeseries.fast.longint.FastMutableLongDoubleTimeSeries;

/**
 * 
 */
public interface MutableLocalDateDoubleTimeSeries extends LocalDateDoubleTimeSeries, MutableDoubleTimeSeries<LocalDate> {

  //-------------------------------------------------------------------------
  /**
   * Partial implementation of the {@link MutableLocalDateDoubleTimeSeries} that uses a {@code int} representation of the date.
   */
  public abstract static class Integer extends AbstractMutableIntDoubleTimeSeries<LocalDate> implements MutableLocalDateDoubleTimeSeries {
    private static final long serialVersionUID = 1L;

    public Integer(final DateTimeConverter<LocalDate> converter, final FastMutableIntDoubleTimeSeries timeSeries) {
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
    public MutableLocalDateDoubleTimeSeries newInstance(final LocalDate[] dateTimes, final Double[] values) {
      return newInstanceFast(dateTimes, TimeSeriesUtils.toPrimitive(values));
    }

    public abstract MutableLocalDateDoubleTimeSeries newInstanceFast(LocalDate[] dateTimes, double[] values);
  }

  //-------------------------------------------------------------------------
  /**
   * Partial implementation of the {@link MutableLocalDateDoubleTimeSeries} that uses a {@code long} representation of the date.
   */
  public abstract static class Long extends AbstractMutableLongDoubleTimeSeries<LocalDate> implements MutableLocalDateDoubleTimeSeries {
    private static final long serialVersionUID = 1L;

    public Long(final DateTimeConverter<LocalDate> converter, final FastMutableLongDoubleTimeSeries timeSeries) {
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
    public MutableLocalDateDoubleTimeSeries newInstance(final LocalDate[] dateTimes, final Double[] values) {
      return newInstanceFast(dateTimes, TimeSeriesUtils.toPrimitive(values));
    }

    public abstract MutableLocalDateDoubleTimeSeries newInstanceFast(LocalDate[] dateTimes, double[] values);
  }

}
