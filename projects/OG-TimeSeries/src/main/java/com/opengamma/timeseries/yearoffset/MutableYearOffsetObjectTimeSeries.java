/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.timeseries.yearoffset;

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
public interface MutableYearOffsetObjectTimeSeries<T> extends YearOffsetObjectTimeSeries<T>, MutableObjectTimeSeries<Double, T> {
  /** */
  public abstract static class Integer<T> extends AbstractMutableIntObjectTimeSeries<Double, T> implements MutableYearOffsetObjectTimeSeries<T>, ObjectTimeSeries<Double, T> {
    public Integer(final DateTimeConverter<Double> converter, final FastMutableIntObjectTimeSeries<T> timeSeries) {
      super(converter, timeSeries);
    }

    @Override
    public ObjectTimeSeries<Double, T> newInstance(final Double[] dateTimes, final T[] values) {
      return newInstanceFast(dateTimes, values);
    }

    public abstract YearOffsetObjectTimeSeries<T> newInstanceFast(Double[] dateTimes, T[] values);

  }

  /** */
  public abstract static class Long<T> extends AbstractMutableLongObjectTimeSeries<Double, T> implements MutableYearOffsetObjectTimeSeries<T> {
    public Long(final DateTimeConverter<Double> converter, final FastMutableLongObjectTimeSeries<T> timeSeries) {
      super(converter, timeSeries);
    }

    @Override
    public ObjectTimeSeries<Double, T> newInstance(final Double[] dateTimes, final T[] values) {
      return newInstanceFast(dateTimes, values);
    }

    public abstract YearOffsetObjectTimeSeries<T> newInstanceFast(Double[] dateTimes, T[] values);
    
  }
}
