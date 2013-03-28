/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.timeseries.localdate;

import org.threeten.bp.LocalDate;

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
public interface MutableLocalDateObjectTimeSeries<T> extends LocalDateObjectTimeSeries<T>,
    MutableObjectTimeSeries<LocalDate, T> {

  /** */
  public abstract static class Integer<T>
      extends AbstractMutableIntObjectTimeSeries<LocalDate, T>
      implements MutableLocalDateObjectTimeSeries<T> {

    /** Serialization version. */
    private static final long serialVersionUID = 759960440250501976L;

    public Integer(final DateTimeConverter<LocalDate> converter, final FastMutableIntObjectTimeSeries<T> timeSeries) {
      super(converter, timeSeries);
    }

    @Override
    public ObjectTimeSeries<LocalDate, T> newInstance(final LocalDate[] dateTimes, final T[] values) {
      return newInstanceFast(dateTimes, values);
    }

    public abstract LocalDateObjectTimeSeries<T> newInstanceFast(LocalDate[] dateTimes, T[] values);
  }

  /** */
  public abstract static class Long<T>
      extends AbstractMutableLongObjectTimeSeries<LocalDate, T>
      implements MutableLocalDateObjectTimeSeries<T> {

    /** Serialization version. */
    private static final long serialVersionUID = -8931458723329084801L;

    public Long(final DateTimeConverter<LocalDate> converter, final FastMutableLongObjectTimeSeries<T> timeSeries) {
      super(converter, timeSeries);
    }

    @Override
    public ObjectTimeSeries<LocalDate, T> newInstance(final LocalDate[] dateTimes, final T[] values) {
      return newInstanceFast(dateTimes, values);
    }

    public abstract LocalDateObjectTimeSeries<T> newInstanceFast(LocalDate[] dateTimes, T[] values);
  }
}
