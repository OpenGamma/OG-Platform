/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.timeseries.date;

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
public interface MutableDateDoubleTimeSeries extends DateDoubleTimeSeries, MutableDoubleTimeSeries<Date> {

  /** */
  public abstract static class Integer extends AbstractMutableIntDoubleTimeSeries<Date> implements MutableDateDoubleTimeSeries {
    public Integer(final DateTimeConverter<Date> converter, final FastMutableIntDoubleTimeSeries timeSeries) {
      super(converter, timeSeries);
    }

    @Override
    public MutableDateDoubleTimeSeries newInstance(final Date[] dateTimes, final Double[] values) {
      return newInstanceFast(dateTimes, TimeSeriesUtils.toPrimitive(values));
    }

    public abstract MutableDateDoubleTimeSeries newInstanceFast(Date[] dateTimes, double[] values);
  }

  /** */
  public abstract static class Long extends AbstractMutableLongDoubleTimeSeries<Date> implements MutableDateDoubleTimeSeries {
    public Long(final DateTimeConverter<Date> converter, final FastMutableLongDoubleTimeSeries timeSeries) {
      super(converter, timeSeries);
    }

    @Override
    public MutableDateDoubleTimeSeries newInstance(final Date[] dateTimes, final Double[] values) {
      return newInstanceFast(dateTimes, TimeSeriesUtils.toPrimitive(values));
    }

    public abstract MutableDateDoubleTimeSeries newInstanceFast(Date[] dateTimes, double[] values);
  }
}
