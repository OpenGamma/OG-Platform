/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.util.timeseries.localdate;

import javax.time.calendar.LocalDate;

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
 * 
 */
public interface LocalDateObjectTimeSeries<T> extends ObjectTimeSeries<LocalDate, T>, FastBackedObjectTimeSeries<LocalDate, T> {

  public abstract static class Integer<T> extends AbstractIntObjectTimeSeries<LocalDate, T> implements LocalDateObjectTimeSeries<T> {
    public Integer(final DateTimeConverter<LocalDate> converter, final FastIntObjectTimeSeries<T> timeSeries) {
      super(converter, timeSeries);
    }
    
    @Override
    public TimeSeries<LocalDate, T> newInstance(final LocalDate[] dateTimes, final T[] values) {
      return newInstanceFast(dateTimes, values);
    }

    public abstract LocalDateObjectTimeSeries<T> newInstanceFast(LocalDate[] dateTimes, T[] values);
  }

  public abstract static class Long<T> extends AbstractLongObjectTimeSeries<LocalDate, T> implements LocalDateObjectTimeSeries<T> {
    public Long(final DateTimeConverter<LocalDate> converter, final FastLongObjectTimeSeries<T> timeSeries) {
      super(converter, timeSeries);
    }
    
    @Override
    public TimeSeries<LocalDate, T> newInstance(final LocalDate[] dateTimes, final T[] values) {
      return newInstanceFast(dateTimes, values);
    }

    public abstract LocalDateObjectTimeSeries<T> newInstanceFast(LocalDate[] dateTimes, T[] values);
  }
}
