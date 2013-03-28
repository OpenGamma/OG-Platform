/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.timeseries.zoneddatetime;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.timeseries.AbstractMutableIntObjectTimeSeries;
import com.opengamma.timeseries.AbstractMutableLongObjectTimeSeries;
import com.opengamma.timeseries.DateTimeConverter;
import com.opengamma.timeseries.MutableObjectTimeSeries;
import com.opengamma.timeseries.ObjectTimeSeries;
import com.opengamma.timeseries.fast.integer.object.FastMutableIntObjectTimeSeries;
import com.opengamma.timeseries.fast.longint.object.FastMutableLongObjectTimeSeries;

/**
 * @param <T> The type of the data
 */
public interface MutableZonedDateTimeObjectTimeSeries<T> extends ZonedDateTimeObjectTimeSeries<T>, MutableObjectTimeSeries<ZonedDateTime, T> {

  /** */
  public abstract static class Integer<T>
      extends AbstractMutableIntObjectTimeSeries<ZonedDateTime, T>
      implements MutableZonedDateTimeObjectTimeSeries<T> {

    /** Serialization version. */
    private static final long serialVersionUID = -7996257143330324739L;

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
  public abstract static class Long<T>
      extends AbstractMutableLongObjectTimeSeries<ZonedDateTime, T>
      implements MutableZonedDateTimeObjectTimeSeries<T> {

    /** Serialization version. */
    private static final long serialVersionUID = 8286781278040433947L;

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
