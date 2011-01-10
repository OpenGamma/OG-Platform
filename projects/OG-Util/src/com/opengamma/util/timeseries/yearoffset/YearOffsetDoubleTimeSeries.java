/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.util.timeseries.yearoffset;

import org.apache.commons.lang.ArrayUtils;

import com.opengamma.util.timeseries.AbstractIntDoubleTimeSeries;
import com.opengamma.util.timeseries.AbstractLongDoubleTimeSeries;
import com.opengamma.util.timeseries.DateTimeConverter;
import com.opengamma.util.timeseries.DoubleTimeSeries;
import com.opengamma.util.timeseries.FastBackedDoubleTimeSeries;
import com.opengamma.util.timeseries.TimeSeries;
import com.opengamma.util.timeseries.fast.integer.FastIntDoubleTimeSeries;
import com.opengamma.util.timeseries.fast.longint.FastLongDoubleTimeSeries;

/**
 * @author jim
 * 
 */
public interface YearOffsetDoubleTimeSeries extends DoubleTimeSeries<Double>, FastBackedDoubleTimeSeries<Double> {

  public abstract static class Integer extends AbstractIntDoubleTimeSeries<Double> implements YearOffsetDoubleTimeSeries {
    public Integer(final DateTimeConverter<Double> converter, final FastIntDoubleTimeSeries timeSeries) {
      super(converter, timeSeries);
    }

    @Override
    public TimeSeries<Double, Double> newInstance(final Double[] dateTimes, final Double[] values) {
      return newInstanceFast(dateTimes, ArrayUtils.toPrimitive(values));
    }

    public abstract YearOffsetDoubleTimeSeries newInstanceFast(Double[] dateTimes, double[] values);
  }

  public abstract static class Long extends AbstractLongDoubleTimeSeries<Double> implements YearOffsetDoubleTimeSeries {
    public Long(final DateTimeConverter<Double> converter, final FastLongDoubleTimeSeries timeSeries) {
      super(converter, timeSeries);
    }

    @Override
    public TimeSeries<Double, Double> newInstance(final Double[] dateTimes, final Double[] values) {
      return newInstanceFast(dateTimes, ArrayUtils.toPrimitive(values));
    }

    public abstract YearOffsetDoubleTimeSeries newInstanceFast(Double[] dateTimes, double[] values);
  }
}
