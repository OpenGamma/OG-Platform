/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.timeseries.localdate;

import org.threeten.bp.LocalDate;

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
public interface LocalDateObjectTimeSeries<T> extends ObjectTimeSeries<LocalDate, T>,
    FastBackedObjectTimeSeries<LocalDate, T> {

  /** */
  public abstract static class Integer<T> extends AbstractIntObjectTimeSeries<LocalDate, T> implements
      LocalDateObjectTimeSeries<T> {
    public Integer(final DateTimeConverter<LocalDate> converter, final FastIntObjectTimeSeries<T> timeSeries) {
      super(converter, timeSeries);
    }

    @Override
    public ObjectTimeSeries<LocalDate, T> newInstance(final LocalDate[] dateTimes, final T[] values) {
      return newInstanceFast(dateTimes, values);
    }

    public abstract LocalDateObjectTimeSeries<T> newInstanceFast(LocalDate[] dateTimes, T[] values);
  }

  /** */
  public abstract static class Long<T> extends AbstractLongObjectTimeSeries<LocalDate, T> implements
      LocalDateObjectTimeSeries<T> {
    public Long(final DateTimeConverter<LocalDate> converter, final FastLongObjectTimeSeries<T> timeSeries) {
      super(converter, timeSeries);
    }

    @Override
    public ObjectTimeSeries<LocalDate, T> newInstance(final LocalDate[] dateTimes, final T[] values) {
      return newInstanceFast(dateTimes, values);
    }

    public abstract LocalDateObjectTimeSeries<T> newInstanceFast(LocalDate[] dateTimes, T[] values);
  }
}
