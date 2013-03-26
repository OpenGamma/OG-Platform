/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.timeseries.yearoffset;

import com.opengamma.timeseries.AbstractIntDoubleTimeSeries;
import com.opengamma.timeseries.AbstractLongDoubleTimeSeries;
import com.opengamma.timeseries.DateTimeConverter;
import com.opengamma.timeseries.DoubleTimeSeries;
import com.opengamma.timeseries.FastBackedDoubleTimeSeries;
import com.opengamma.timeseries.TimeSeriesUtils;
import com.opengamma.timeseries.fast.integer.FastIntDoubleTimeSeries;
import com.opengamma.timeseries.fast.longint.FastLongDoubleTimeSeries;

/**
 * 
 */
public interface YearOffsetDoubleTimeSeries extends DoubleTimeSeries<Double>, FastBackedDoubleTimeSeries<Double> {
  /** */
  public abstract static class Integer extends AbstractIntDoubleTimeSeries<Double> implements YearOffsetDoubleTimeSeries {
    public Integer(final DateTimeConverter<Double> converter, final FastIntDoubleTimeSeries timeSeries) {
      super(converter, timeSeries);
    }

    @Override
    public YearOffsetDoubleTimeSeries newInstance(final Double[] dateTimes, final Double[] values) {
      return newInstanceFast(dateTimes, TimeSeriesUtils.toPrimitive(values));
    }

    public abstract YearOffsetDoubleTimeSeries newInstanceFast(Double[] dateTimes, double[] values);
  }
  /** */
  public abstract static class Long extends AbstractLongDoubleTimeSeries<Double> implements YearOffsetDoubleTimeSeries {
    public Long(final DateTimeConverter<Double> converter, final FastLongDoubleTimeSeries timeSeries) {
      super(converter, timeSeries);
    }

    @Override
    public YearOffsetDoubleTimeSeries newInstance(final Double[] dateTimes, final Double[] values) {
      return newInstanceFast(dateTimes, TimeSeriesUtils.toPrimitive(values));
    }

    public abstract YearOffsetDoubleTimeSeries newInstanceFast(Double[] dateTimes, double[] values);
  }
}
