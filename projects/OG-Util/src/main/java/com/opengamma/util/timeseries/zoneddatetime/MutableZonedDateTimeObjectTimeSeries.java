/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.util.timeseries.zoneddatetime;

import javax.time.calendar.ZonedDateTime;

import com.opengamma.util.timeseries.AbstractMutableIntObjectTimeSeries;
import com.opengamma.util.timeseries.AbstractMutableLongObjectTimeSeries;
import com.opengamma.util.timeseries.DateTimeConverter;
import com.opengamma.util.timeseries.MutableObjectTimeSeries;
import com.opengamma.util.timeseries.ObjectTimeSeries;
import com.opengamma.util.timeseries.fast.integer.object.FastMutableIntObjectTimeSeries;
import com.opengamma.util.timeseries.fast.longint.object.FastMutableLongObjectTimeSeries;

/**
 * @param <T> The type of the data
 */
public interface MutableZonedDateTimeObjectTimeSeries<T> extends ZonedDateTimeObjectTimeSeries<T>, MutableObjectTimeSeries<ZonedDateTime, T> {
  /** */
  public abstract static class Integer<T> extends AbstractMutableIntObjectTimeSeries<ZonedDateTime, T> implements MutableZonedDateTimeObjectTimeSeries<T> {
    public Integer(final DateTimeConverter<ZonedDateTime> converter, final FastMutableIntObjectTimeSeries<T> timeSeries) {
      super(converter, timeSeries);
    }

    @Override
    public ObjectTimeSeries<ZonedDateTime, T> newInstance(final ZonedDateTime[] dateTimes, final T[] values) {
      return newInstanceFast(dateTimes, values);
    }

    public abstract ZonedDateTimeObjectTimeSeries<T> newInstanceFast(ZonedDateTime[] dateTimes, T[] values);
  }
  /** */
  public abstract static class Long<T> extends AbstractMutableLongObjectTimeSeries<ZonedDateTime, T> implements MutableZonedDateTimeObjectTimeSeries<T> {
    public Long(final DateTimeConverter<ZonedDateTime> converter, final FastMutableLongObjectTimeSeries<T> timeSeries) {
      super(converter, timeSeries);
    }

    @Override
    public ObjectTimeSeries<ZonedDateTime, T> newInstance(final ZonedDateTime[] dateTimes, final T[] values) {
      return newInstanceFast(dateTimes, values);
    }

    public abstract ZonedDateTimeObjectTimeSeries<T> newInstanceFast(ZonedDateTime[] dateTimes, T[] values);
  }
}
