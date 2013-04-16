/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.timeseries.date.time;

import java.util.Date;

import com.opengamma.timeseries.AbstractIntDoubleTimeSeries;
import com.opengamma.timeseries.AbstractLongDoubleTimeSeries;
import com.opengamma.timeseries.DateTimeConverter;
import com.opengamma.timeseries.DoubleTimeSeries;
import com.opengamma.timeseries.FastBackedDoubleTimeSeries;
import com.opengamma.timeseries.TimeSeriesUtils;
import com.opengamma.timeseries.fast.integer.FastIntDoubleTimeSeries;
import com.opengamma.timeseries.fast.longint.FastLongDoubleTimeSeries;

/**
 *         This class isn't really necessary but it meant to serve as a way to
 *         distinguish DateDoubleTimeSeries that can store full time accuracy
 *         from that which can't.
 */
public interface DateTimeDoubleTimeSeries extends DoubleTimeSeries<Date>, FastBackedDoubleTimeSeries<Date> {

  /** */
  public abstract static class Integer extends AbstractIntDoubleTimeSeries<Date> implements DateTimeDoubleTimeSeries {
    public Integer(final DateTimeConverter<Date> converter, final FastIntDoubleTimeSeries timeSeries) {
      super(converter, timeSeries);
    }

    @Override
    public DateTimeDoubleTimeSeries newInstance(final Date[] dateTimes, final Double[] values) {
      return newInstanceFast(dateTimes, TimeSeriesUtils.toPrimitive(values));
    }

    public abstract DateTimeDoubleTimeSeries newInstanceFast(Date[] dateTimes, double[] values);
  }

  /** */
  public abstract static class Long extends AbstractLongDoubleTimeSeries<Date> implements DateTimeDoubleTimeSeries {
    public Long(final DateTimeConverter<Date> converter, final FastLongDoubleTimeSeries timeSeries) {
      super(converter, timeSeries);
    }

    @Override
    public DateTimeDoubleTimeSeries newInstance(final Date[] dateTimes, final Double[] values) {
      return newInstanceFast(dateTimes, TimeSeriesUtils.toPrimitive(values));
    }

    public abstract DateTimeDoubleTimeSeries newInstanceFast(Date[] dateTimes, double[] values);
  }
}
