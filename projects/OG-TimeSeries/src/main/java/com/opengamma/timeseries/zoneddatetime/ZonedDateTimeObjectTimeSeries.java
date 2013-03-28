/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.timeseries.zoneddatetime;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.timeseries.AbstractIntObjectTimeSeries;
import com.opengamma.timeseries.AbstractLongObjectTimeSeries;
import com.opengamma.timeseries.DateTimeConverter;
import com.opengamma.timeseries.FastBackedObjectTimeSeries;
import com.opengamma.timeseries.ObjectTimeSeries;
import com.opengamma.timeseries.fast.integer.object.FastIntObjectTimeSeries;
import com.opengamma.timeseries.fast.longint.object.FastLongObjectTimeSeries;

/**
 * @param <T> The type of the data
 */
public interface ZonedDateTimeObjectTimeSeries<T> extends ObjectTimeSeries<ZonedDateTime, T>, FastBackedObjectTimeSeries<ZonedDateTime, T> {

  /** */
  public abstract static class Integer<T>
      extends AbstractIntObjectTimeSeries<ZonedDateTime, T>
      implements ZonedDateTimeObjectTimeSeries<T> {

    /** Serialization version. */
    private static final long serialVersionUID = 2512280250874304941L;

    public Integer(final DateTimeConverter<ZonedDateTime> converter, final FastIntObjectTimeSeries<T> timeSeries) {
      super(converter, timeSeries);
    }

    @Override
    public ObjectTimeSeries<ZonedDateTime, T> newInstance(final ZonedDateTime[] dateTimes, final T[] values) {
      return newInstanceFast(dateTimes, values);
    }

    public abstract ZonedDateTimeObjectTimeSeries<T> newInstanceFast(ZonedDateTime[] dateTimes, T[] values);
  }

  /** */
  public abstract static class Long<T>
      extends AbstractLongObjectTimeSeries<ZonedDateTime, T>
      implements ZonedDateTimeObjectTimeSeries<T> {

    /** Serialization version. */
    private static final long serialVersionUID = -3633293052369247357L;

    public Long(final DateTimeConverter<ZonedDateTime> converter, final FastLongObjectTimeSeries<T> timeSeries) {
      super(converter, timeSeries);
    }

    @Override
    public ObjectTimeSeries<ZonedDateTime, T> newInstance(final ZonedDateTime[] dateTimes, final T[] values) {
      return newInstanceFast(dateTimes, values);
    }

    public abstract ZonedDateTimeObjectTimeSeries<T> newInstanceFast(ZonedDateTime[] dateTimes, T[] values);
  }

}
