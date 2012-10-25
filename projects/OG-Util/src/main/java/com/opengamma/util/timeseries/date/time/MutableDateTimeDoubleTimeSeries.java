/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.util.timeseries.date.time;

import java.util.Date;

import org.apache.commons.lang.ArrayUtils;

import com.opengamma.util.timeseries.AbstractMutableIntDoubleTimeSeries;
import com.opengamma.util.timeseries.AbstractMutableLongDoubleTimeSeries;
import com.opengamma.util.timeseries.DateTimeConverter;
import com.opengamma.util.timeseries.MutableDoubleTimeSeries;
import com.opengamma.util.timeseries.fast.integer.FastMutableIntDoubleTimeSeries;
import com.opengamma.util.timeseries.fast.longint.FastMutableLongDoubleTimeSeries;

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
      return newInstanceFast(dateTimes, ArrayUtils.toPrimitive(values));
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
      return newInstanceFast(dateTimes, ArrayUtils.toPrimitive(values));
    }

    public abstract MutableDateTimeDoubleTimeSeries newInstanceFast(Date[] dateTimes, double[] values);
  }
}
