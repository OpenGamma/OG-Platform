/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.timeseries.yearoffset;

import com.opengamma.timeseries.AbstractMutableIntDoubleTimeSeries;
import com.opengamma.timeseries.AbstractMutableLongDoubleTimeSeries;
import com.opengamma.timeseries.DateTimeConverter;
import com.opengamma.timeseries.DoubleTimeSeries;
import com.opengamma.timeseries.MutableDoubleTimeSeries;
import com.opengamma.timeseries.TimeSeriesUtils;
import com.opengamma.timeseries.fast.integer.FastMutableIntDoubleTimeSeries;
import com.opengamma.timeseries.fast.longint.FastMutableLongDoubleTimeSeries;

/**
 * 
 */
public interface MutableYearOffsetDoubleTimeSeries extends YearOffsetDoubleTimeSeries, MutableDoubleTimeSeries<Double> {
  /** */
  public abstract static class Integer extends AbstractMutableIntDoubleTimeSeries<Double> implements MutableYearOffsetDoubleTimeSeries, DoubleTimeSeries<Double> {
    public Integer(final DateTimeConverter<Double> converter, final FastMutableIntDoubleTimeSeries timeSeries) {
      super(converter, timeSeries);
    }

    @Override
    public MutableYearOffsetDoubleTimeSeries newInstance(final Double[] dateTimes, final Double[] values) {
      return newInstanceFast(dateTimes, TimeSeriesUtils.toPrimitive(values));
    }

    public abstract MutableYearOffsetDoubleTimeSeries newInstanceFast(Double[] dateTimes, double[] values);

  }

  /** */
  public abstract static class Long extends AbstractMutableLongDoubleTimeSeries<Double> implements MutableYearOffsetDoubleTimeSeries {
    public Long(final DateTimeConverter<Double> converter, final FastMutableLongDoubleTimeSeries timeSeries) {
      super(converter, timeSeries);
    }

    @Override
    public MutableYearOffsetDoubleTimeSeries newInstance(final Double[] dateTimes, final Double[] values) {
      return newInstanceFast(dateTimes, TimeSeriesUtils.toPrimitive(values));
    }

    public abstract MutableYearOffsetDoubleTimeSeries newInstanceFast(Double[] dateTimes, double[] values);
    
  }
}
