/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.timeseries.zoneddatetime;

import org.threeten.bp.ZonedDateTime;

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
public interface ZonedDateTimeDoubleTimeSeries extends DoubleTimeSeries<ZonedDateTime>, FastBackedDoubleTimeSeries<ZonedDateTime> {

  /** */
  public abstract static class Integer
      extends AbstractIntDoubleTimeSeries<ZonedDateTime>
      implements ZonedDateTimeDoubleTimeSeries {

    /** Serialization version. */
    private static final long serialVersionUID = 3333283617280268397L;

    public Integer(final DateTimeConverter<ZonedDateTime> converter, final FastIntDoubleTimeSeries timeSeries) {
      super(converter, timeSeries);
    }

    @Override
    public ZonedDateTimeDoubleTimeSeries newInstance(final ZonedDateTime[] dateTimes, final Double[] values) {
      return newInstanceFast(dateTimes, TimeSeriesUtils.toPrimitive(values));
    }

    public abstract ZonedDateTimeDoubleTimeSeries newInstanceFast(ZonedDateTime[] dateTimes, double[] values);
  }

  /** */
  public abstract static class Long
      extends AbstractLongDoubleTimeSeries<ZonedDateTime>
      implements ZonedDateTimeDoubleTimeSeries {
    
    /** Serialization version. */
    private static final long serialVersionUID = 6698328262716700897L;

    public Long(final DateTimeConverter<ZonedDateTime> converter, final FastLongDoubleTimeSeries timeSeries) {
      super(converter, timeSeries);
    }

    @Override
    public ZonedDateTimeDoubleTimeSeries newInstance(final ZonedDateTime[] dateTimes, final Double[] values) {
      return newInstanceFast(dateTimes, TimeSeriesUtils.toPrimitive(values));
    }

    public abstract ZonedDateTimeDoubleTimeSeries newInstanceFast(ZonedDateTime[] dateTimes, double[] values);
  }

}
