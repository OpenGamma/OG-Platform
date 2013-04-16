/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.timeseries.date.time;

import java.util.Date;

import com.opengamma.timeseries.AbstractMutableIntDoubleTimeSeries;
import com.opengamma.timeseries.AbstractMutableLongDoubleTimeSeries;
import com.opengamma.timeseries.DateTimeConverter;
import com.opengamma.timeseries.MutableDoubleTimeSeries;
import com.opengamma.timeseries.TimeSeriesUtils;
import com.opengamma.timeseries.fast.integer.FastMutableIntDoubleTimeSeries;
import com.opengamma.timeseries.fast.longint.FastMutableLongDoubleTimeSeries;

/**
 * 
 */
public interface MutableDateTimeDoubleTimeSeries extends DateTimeDoubleTimeSeries, MutableDoubleTimeSeries<Date> {
  /** */
  public abstract static class Integer extends AbstractMutableIntDoubleTimeSeries<Date> implements MutableDateTimeDoubleTimeSeries {
    public Integer(final DateTimeConverter<Date> converter, final FastMutableIntDoubleTimeSeries timeSeries) {
      super(converter, timeSeries);
    }

    @Override
    public MutableDateTimeDoubleTimeSeries newInstance(final Date[] dateTimes, final Double[] values) {
      return newInstanceFast(dateTimes, TimeSeriesUtils.toPrimitive(values));
    }

    public abstract MutableDateTimeDoubleTimeSeries newInstanceFast(Date[] dateTimes, double[] values);
  }

  /** */
  public abstract static class Long extends AbstractMutableLongDoubleTimeSeries<Date> implements MutableDateTimeDoubleTimeSeries {
    public Long(final DateTimeConverter<Date> converter, final FastMutableLongDoubleTimeSeries timeSeries) {
      super(converter, timeSeries);
    }

    @Override
    public MutableDateTimeDoubleTimeSeries newInstance(final Date[] dateTimes, final Double[] values) {
      return newInstanceFast(dateTimes, TimeSeriesUtils.toPrimitive(values));
    }

    public abstract MutableDateTimeDoubleTimeSeries newInstanceFast(Date[] dateTimes, double[] values);
  }
}
