/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.util.timeseries.localdate;

import javax.time.calendar.LocalDate;

import com.opengamma.util.timeseries.AbstractMutableIntObjectTimeSeries;
import com.opengamma.util.timeseries.AbstractMutableLongObjectTimeSeries;
import com.opengamma.util.timeseries.DateTimeConverter;
import com.opengamma.util.timeseries.MutableObjectTimeSeries;
import com.opengamma.util.timeseries.TimeSeries;
import com.opengamma.util.timeseries.fast.integer.object.FastMutableIntObjectTimeSeries;
import com.opengamma.util.timeseries.fast.longint.object.FastMutableLongObjectTimeSeries;

/**
 * @param <T> The type of the data
 */
public interface MutableLocalDateObjectTimeSeries<T> extends LocalDateObjectTimeSeries<T>,
    MutableObjectTimeSeries<LocalDate, T> {

  /** */
  public abstract static class Integer<T> extends AbstractMutableIntObjectTimeSeries<LocalDate, T> implements
      MutableLocalDateObjectTimeSeries<T> {
    public Integer(final DateTimeConverter<LocalDate> converter, final FastMutableIntObjectTimeSeries<T> timeSeries) {
      super(converter, timeSeries);
    }

    @Override
    public TimeSeries<LocalDate, T> newInstance(final LocalDate[] dateTimes, final T[] values) {
      return newInstanceFast(dateTimes, values);
    }

    public abstract LocalDateObjectTimeSeries<T> newInstanceFast(LocalDate[] dateTimes, T[] values);
  }

  /** */
  public abstract static class Long<T> extends AbstractMutableLongObjectTimeSeries<LocalDate, T> implements
      MutableLocalDateObjectTimeSeries<T> {
    public Long(final DateTimeConverter<LocalDate> converter, final FastMutableLongObjectTimeSeries<T> timeSeries) {
      super(converter, timeSeries);
    }

    @Override
    public TimeSeries<LocalDate, T> newInstance(final LocalDate[] dateTimes, final T[] values) {
      return newInstanceFast(dateTimes, values);
    }

    public abstract LocalDateObjectTimeSeries<T> newInstanceFast(LocalDate[] dateTimes, T[] values);
  }
}
