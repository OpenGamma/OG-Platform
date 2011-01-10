/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.util.timeseries.zoneddatetime;

import javax.time.calendar.ZonedDateTime;

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
public interface ZonedDateTimeObjectTimeSeries<T> extends ObjectTimeSeries<ZonedDateTime, T>, FastBackedObjectTimeSeries<ZonedDateTime, T> {

  public abstract static class Integer<T> extends AbstractIntObjectTimeSeries<ZonedDateTime, T> implements ZonedDateTimeObjectTimeSeries<T> {
    public Integer(final DateTimeConverter<ZonedDateTime> converter, final FastIntObjectTimeSeries<T> timeSeries) {
      super(converter, timeSeries);
    }

    @Override
    public TimeSeries<ZonedDateTime, T> newInstance(final ZonedDateTime[] dateTimes, final T[] values) {
      return newInstanceFast(dateTimes, values);
    }

    public abstract ZonedDateTimeObjectTimeSeries<T> newInstanceFast(ZonedDateTime[] dateTimes, T[] values);
  }

  public abstract static class Long<T> extends AbstractLongObjectTimeSeries<ZonedDateTime, T> implements ZonedDateTimeObjectTimeSeries<T> {
    public Long(final DateTimeConverter<ZonedDateTime> converter, final FastLongObjectTimeSeries<T> timeSeries) {
      super(converter, timeSeries);
    }

    @Override
    public TimeSeries<ZonedDateTime, T> newInstance(final ZonedDateTime[] dateTimes, final T[] values) {
      return newInstanceFast(dateTimes, values);
    }

    public abstract ZonedDateTimeObjectTimeSeries<T> newInstanceFast(ZonedDateTime[] dateTimes, T[] values);
  }
}
