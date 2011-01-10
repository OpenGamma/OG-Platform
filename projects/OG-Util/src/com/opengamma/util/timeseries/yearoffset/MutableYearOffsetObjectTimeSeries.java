/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.util.timeseries.yearoffset;

import com.opengamma.util.timeseries.AbstractMutableIntObjectTimeSeries;
import com.opengamma.util.timeseries.AbstractMutableLongObjectTimeSeries;
import com.opengamma.util.timeseries.DateTimeConverter;
import com.opengamma.util.timeseries.MutableObjectTimeSeries;
import com.opengamma.util.timeseries.ObjectTimeSeries;
import com.opengamma.util.timeseries.TimeSeries;
import com.opengamma.util.timeseries.fast.integer.object.FastMutableIntObjectTimeSeries;
import com.opengamma.util.timeseries.fast.longint.object.FastMutableLongObjectTimeSeries;

/**
 * @author jim
 * 
 */
public interface MutableYearOffsetObjectTimeSeries<T> extends YearOffsetObjectTimeSeries<T>, MutableObjectTimeSeries<Double, T> {
  public abstract static class Integer<T> extends AbstractMutableIntObjectTimeSeries<Double, T> implements MutableYearOffsetObjectTimeSeries<T>, ObjectTimeSeries<Double, T> {
    public Integer(final DateTimeConverter<Double> converter, final FastMutableIntObjectTimeSeries<T> timeSeries) {
      super(converter, timeSeries);
    }

    @Override
    public TimeSeries<Double, T> newInstance(final Double[] dateTimes, final T[] values) {
      return newInstanceFast(dateTimes, values);
    }

    public abstract YearOffsetObjectTimeSeries<T> newInstanceFast(Double[] dateTimes, T[] values);

  }

  public abstract static class Long<T> extends AbstractMutableLongObjectTimeSeries<Double, T> implements MutableYearOffsetObjectTimeSeries<T> {
    public Long(final DateTimeConverter<Double> converter, final FastMutableLongObjectTimeSeries<T> timeSeries) {
      super(converter, timeSeries);
    }

    @Override
    public TimeSeries<Double, T> newInstance(final Double[] dateTimes, final T[] values) {
      return newInstanceFast(dateTimes, values);
    }

    public abstract YearOffsetObjectTimeSeries<T> newInstanceFast(Double[] dateTimes, T[] values);
    
  }
}
