/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.util.timeseries.zoneddatetime;

import javax.time.calendar.ZonedDateTime;

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
 * 
 */
public interface ZonedDateTimeDoubleTimeSeries extends DoubleTimeSeries<ZonedDateTime>, FastBackedDoubleTimeSeries<ZonedDateTime> {

  public abstract static class Integer extends AbstractIntDoubleTimeSeries<ZonedDateTime> implements ZonedDateTimeDoubleTimeSeries {
    public Integer(final DateTimeConverter<ZonedDateTime> converter, final FastIntDoubleTimeSeries timeSeries) {
      super(converter, timeSeries);
    }

    @Override
    public TimeSeries<ZonedDateTime, Double> newInstance(final ZonedDateTime[] dateTimes, final Double[] values) {
      return newInstanceFast(dateTimes, ArrayUtils.toPrimitive(values));
    }

    public abstract ZonedDateTimeDoubleTimeSeries newInstanceFast(ZonedDateTime[] dateTimes, double[] values);
  }

  public abstract static class Long extends AbstractLongDoubleTimeSeries<ZonedDateTime> implements ZonedDateTimeDoubleTimeSeries {
    public Long(final DateTimeConverter<ZonedDateTime> converter, final FastLongDoubleTimeSeries timeSeries) {
      super(converter, timeSeries);
    }

    @Override
    public TimeSeries<ZonedDateTime, Double> newInstance(final ZonedDateTime[] dateTimes, final Double[] values) {
      return newInstanceFast(dateTimes, ArrayUtils.toPrimitive(values));
    }

    public abstract ZonedDateTimeDoubleTimeSeries newInstanceFast(ZonedDateTime[] dateTimes, double[] values);
  }
}
