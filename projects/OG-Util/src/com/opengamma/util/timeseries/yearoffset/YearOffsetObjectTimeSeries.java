/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.util.timeseries.yearoffset;

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
public interface YearOffsetObjectTimeSeries<T> extends ObjectTimeSeries<Double, T>, FastBackedObjectTimeSeries<Double, T> {

  public abstract static class Integer<T> extends AbstractIntObjectTimeSeries<Double, T> implements YearOffsetObjectTimeSeries<T> {
    public Integer(final DateTimeConverter<Double> converter, final FastIntObjectTimeSeries<T> timeSeries) {
      super(converter, timeSeries);
    }

    @Override
    public TimeSeries<Double, T> newInstance(final Double[] dateTimes, final T[] values) {
      return newInstanceFast(dateTimes, values);
    }

    public abstract YearOffsetObjectTimeSeries<T> newInstanceFast(Double[] dateTimes, T[] values);
  }

  public abstract static class Long<T> extends AbstractLongObjectTimeSeries<Double, T> implements YearOffsetObjectTimeSeries<T> {
    public Long(final DateTimeConverter<Double> converter, final FastLongObjectTimeSeries<T> timeSeries) {
      super(converter, timeSeries);
    }

    @Override
    public TimeSeries<Double, T> newInstance(final Double[] dateTimes, final T[] values) {
      return newInstanceFast(dateTimes, values);
    }

    public abstract YearOffsetObjectTimeSeries<T> newInstanceFast(Double[] dateTimes, T[] values);
  }
}
