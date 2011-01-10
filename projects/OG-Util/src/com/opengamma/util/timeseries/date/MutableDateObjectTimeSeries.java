/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.util.timeseries.date;

import java.util.Date;

import com.opengamma.util.timeseries.AbstractMutableIntObjectTimeSeries;
import com.opengamma.util.timeseries.AbstractMutableLongObjectTimeSeries;
import com.opengamma.util.timeseries.DateTimeConverter;
import com.opengamma.util.timeseries.MutableObjectTimeSeries;
import com.opengamma.util.timeseries.TimeSeries;
import com.opengamma.util.timeseries.fast.integer.object.FastMutableIntObjectTimeSeries;
import com.opengamma.util.timeseries.fast.longint.object.FastMutableLongObjectTimeSeries;

/**
 * 
 */
public interface MutableDateObjectTimeSeries<T> extends DateObjectTimeSeries<T>, MutableObjectTimeSeries<Date, T> {

  public abstract static class Integer<T> extends AbstractMutableIntObjectTimeSeries<Date, T> implements MutableDateObjectTimeSeries<T> {
    public Integer(final DateTimeConverter<Date> converter, final FastMutableIntObjectTimeSeries<T> timeSeries) {
      super(converter, timeSeries);
    }

    @Override
    public TimeSeries<Date, T> newInstance(final Date[] dateTimes, final T[] values) {
      return newInstanceFast(dateTimes, values);
    }

    public abstract DateObjectTimeSeries<T> newInstanceFast(Date[] dateTimes, T[] values);
  }

  public abstract static class Long<T> extends AbstractMutableLongObjectTimeSeries<Date, T> implements MutableDateObjectTimeSeries<T> {
    public Long(final DateTimeConverter<Date> converter, final FastMutableLongObjectTimeSeries<T> timeSeries) {
      super(converter, timeSeries);
    }

    @Override
    public TimeSeries<Date, T> newInstance(final Date[] dateTimes, final T[] values) {
      return newInstanceFast(dateTimes, values);
    }

    public abstract DateObjectTimeSeries<T> newInstanceFast(Date[] dateTimes, T[] values);
  }
}
