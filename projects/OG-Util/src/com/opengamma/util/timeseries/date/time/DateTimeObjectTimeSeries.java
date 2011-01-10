/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.util.timeseries.date.time;

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
 * @author jim
 *         This class isn't really necessary but it meant to serve as a way to
 *         distinguish DateObjectTimeSeries that can store full time accuracy
 *         from that which can't.
 */
public interface DateTimeObjectTimeSeries<T> extends ObjectTimeSeries<Date, T>, FastBackedObjectTimeSeries<Date, T> {

  public abstract static class Integer<T> extends AbstractIntObjectTimeSeries<Date, T> implements DateTimeObjectTimeSeries<T> {
    public Integer(final DateTimeConverter<Date> converter, final FastIntObjectTimeSeries<T> timeSeries) {
      super(converter, timeSeries);
    }

    @Override
    public TimeSeries<Date, T> newInstance(final Date[] dateTimes, final T[] values) {
      return newInstanceFast(dateTimes, values);
    }

    public abstract DateTimeObjectTimeSeries<T> newInstanceFast(Date[] dateTimes, T[] values);
  }

  public abstract static class Long<T> extends AbstractLongObjectTimeSeries<Date, T> implements DateTimeObjectTimeSeries<T> {
    public Long(final DateTimeConverter<Date> converter, final FastLongObjectTimeSeries<T> timeSeries) {
      super(converter, timeSeries);
    }

    @Override
    public TimeSeries<Date, T> newInstance(final Date[] dateTimes, final T[] values) {
      return newInstanceFast(dateTimes, values);
    }

    public abstract DateTimeObjectTimeSeries<T> newInstanceFast(Date[] dateTimes, T[] values);
  }
}
