/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.util.timeseries.date;

import java.util.Date;

import com.opengamma.util.timeseries.AbstractIntObjectTimeSeries;
import com.opengamma.util.timeseries.AbstractLongObjectTimeSeries;
import com.opengamma.util.timeseries.DateTimeConverter;
import com.opengamma.util.timeseries.FastBackedObjectTimeSeries;
import com.opengamma.util.timeseries.ObjectTimeSeries;
import com.opengamma.util.timeseries.TimeSeries;
import com.opengamma.util.timeseries.fast.integer.object.FastIntObjectTimeSeries;
import com.opengamma.util.timeseries.fast.longint.object.FastLongObjectTimeSeries;

/**
 * 
 */
public interface DateObjectTimeSeries<T> extends ObjectTimeSeries<Date, T>, FastBackedObjectTimeSeries<Date, T> {

  public abstract static class Integer<T> extends AbstractIntObjectTimeSeries<Date, T> implements DateObjectTimeSeries<T> {
    public Integer(final DateTimeConverter<Date> converter, final FastIntObjectTimeSeries<T> timeSeries) {
      super(converter, timeSeries);
    }

    @Override
    public TimeSeries<Date, T> newInstance(final Date[] dateTimes, final T[] values) {
      return newInstanceFast(dateTimes, values);
    }

    public abstract DateObjectTimeSeries<T> newInstanceFast(Date[] dateTimes, T[] values);
   
  }

  public abstract static class Long<T> extends AbstractLongObjectTimeSeries<Date, T> implements DateObjectTimeSeries<T> {
    public Long(final DateTimeConverter<Date> converter, final FastLongObjectTimeSeries<T> timeSeries) {
      super(converter, timeSeries);
    }

    @Override
    public TimeSeries<Date, T> newInstance(final Date[] dateTimes, final T[] values) {
      return newInstanceFast(dateTimes, values);
    }

    public abstract DateObjectTimeSeries<T> newInstanceFast(Date[] dateTimes, T[] values);
  }
}
