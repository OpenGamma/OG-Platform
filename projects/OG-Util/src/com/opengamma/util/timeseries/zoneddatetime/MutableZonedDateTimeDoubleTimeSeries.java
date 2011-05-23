/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.util.timeseries.zoneddatetime;

import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.ArrayUtils;

import com.opengamma.util.timeseries.AbstractMutableIntDoubleTimeSeries;
import com.opengamma.util.timeseries.AbstractMutableLongDoubleTimeSeries;
import com.opengamma.util.timeseries.DateTimeConverter;
import com.opengamma.util.timeseries.MutableDoubleTimeSeries;
import com.opengamma.util.timeseries.TimeSeries;
import com.opengamma.util.timeseries.fast.integer.FastMutableIntDoubleTimeSeries;
import com.opengamma.util.timeseries.fast.longint.FastMutableLongDoubleTimeSeries;

/**
 * 
 */
public interface MutableZonedDateTimeDoubleTimeSeries extends ZonedDateTimeDoubleTimeSeries, MutableDoubleTimeSeries<ZonedDateTime> {
  /** */
  public abstract static class Integer extends AbstractMutableIntDoubleTimeSeries<ZonedDateTime> implements MutableZonedDateTimeDoubleTimeSeries {
    public Integer(final DateTimeConverter<ZonedDateTime> converter, final FastMutableIntDoubleTimeSeries timeSeries) {
      super(converter, timeSeries);
    }

    @Override
    public TimeSeries<ZonedDateTime, Double> newInstance(final ZonedDateTime[] dateTimes, final Double[] values) {
      return newInstanceFast(dateTimes, ArrayUtils.toPrimitive(values));
    }

    public abstract ZonedDateTimeDoubleTimeSeries newInstanceFast(ZonedDateTime[] dateTimes, double[] values);
  }
  /** */
  public abstract static class Long extends AbstractMutableLongDoubleTimeSeries<ZonedDateTime> implements MutableZonedDateTimeDoubleTimeSeries {
    public Long(final DateTimeConverter<ZonedDateTime> converter, final FastMutableLongDoubleTimeSeries timeSeries) {
      super(converter, timeSeries);
    }

    @Override
    public TimeSeries<ZonedDateTime, Double> newInstance(final ZonedDateTime[] dateTimes, final Double[] values) {
      return newInstanceFast(dateTimes, ArrayUtils.toPrimitive(values));
    }

    public abstract ZonedDateTimeDoubleTimeSeries newInstanceFast(ZonedDateTime[] dateTimes, double[] values);
  }
}
