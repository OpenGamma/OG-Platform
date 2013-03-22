/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.timeseries.sqldate;

import java.sql.Date;

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
public interface MutableSQLDateObjectTimeSeries<T> extends SQLDateObjectTimeSeries<T>, MutableObjectTimeSeries<Date, T> {

  /** */
  public abstract static class Integer<T> extends AbstractMutableIntObjectTimeSeries<Date, T> implements
      MutableSQLDateObjectTimeSeries<T> {
    public Integer(final DateTimeConverter<Date> converter, final FastMutableIntObjectTimeSeries<T> timeSeries) {
      super(converter, timeSeries);
    }

    @Override
    public ObjectTimeSeries<Date, T> newInstance(final Date[] dateTimes, final T[] values) {
      return newInstanceFast(dateTimes, values);
    }

    public abstract SQLDateObjectTimeSeries<T> newInstanceFast(Date[] dateTimes, T[] values);

  }

  /** */
  public abstract static class Long<T> extends AbstractMutableLongObjectTimeSeries<Date, T> implements
      MutableSQLDateObjectTimeSeries<T> {
    public Long(final DateTimeConverter<Date> converter, final FastMutableLongObjectTimeSeries<T> timeSeries) {
      super(converter, timeSeries);
    }

    @Override
    public ObjectTimeSeries<Date, T> newInstance(final Date[] dateTimes, final T[] values) {
      return newInstanceFast(dateTimes, values);
    }

    public abstract SQLDateObjectTimeSeries<T> newInstanceFast(Date[] dateTimes, T[] values);
  }
}
