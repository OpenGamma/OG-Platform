/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.util.timeseries.yearoffset;

import org.apache.commons.lang.ArrayUtils;

import com.opengamma.util.timeseries.AbstractMutableIntDoubleTimeSeries;
import com.opengamma.util.timeseries.AbstractMutableLongDoubleTimeSeries;
import com.opengamma.util.timeseries.DateTimeConverter;
import com.opengamma.util.timeseries.DoubleTimeSeries;
import com.opengamma.util.timeseries.MutableDoubleTimeSeries;
import com.opengamma.util.timeseries.TimeSeries;
import com.opengamma.util.timeseries.fast.integer.FastMutableIntDoubleTimeSeries;
import com.opengamma.util.timeseries.fast.longint.FastMutableLongDoubleTimeSeries;

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
    public TimeSeries<Double, Double> newInstance(final Double[] dateTimes, final Double[] values) {
      return newInstanceFast(dateTimes, ArrayUtils.toPrimitive(values));
    }

    public abstract YearOffsetDoubleTimeSeries newInstanceFast(Double[] dateTimes, double[] values);

  }

  /** */
  public abstract static class Long extends AbstractMutableLongDoubleTimeSeries<Double> implements MutableYearOffsetDoubleTimeSeries {
    public Long(final DateTimeConverter<Double> converter, final FastMutableLongDoubleTimeSeries timeSeries) {
      super(converter, timeSeries);
    }

    @Override
    public TimeSeries<Double, Double> newInstance(final Double[] dateTimes, final Double[] values) {
      return newInstanceFast(dateTimes, ArrayUtils.toPrimitive(values));
    }

    public abstract YearOffsetDoubleTimeSeries newInstanceFast(Double[] dateTimes, double[] values);
    
  }
}
