/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.util.timeseries.localdate;

import javax.time.calendar.LocalDate;

import org.apache.commons.lang.ArrayUtils;

import com.opengamma.util.PublicAPI;
import com.opengamma.util.timeseries.AbstractIntDoubleTimeSeries;
import com.opengamma.util.timeseries.AbstractLongDoubleTimeSeries;
import com.opengamma.util.timeseries.DateTimeConverter;
import com.opengamma.util.timeseries.DoubleTimeSeries;
import com.opengamma.util.timeseries.FastBackedDoubleTimeSeries;
import com.opengamma.util.timeseries.TimeSeries;
import com.opengamma.util.timeseries.fast.integer.FastIntDoubleTimeSeries;
import com.opengamma.util.timeseries.fast.longint.FastLongDoubleTimeSeries;

/**
 * @author jim
 * 
 */
@PublicAPI
public interface LocalDateDoubleTimeSeries extends DoubleTimeSeries<LocalDate>, FastBackedDoubleTimeSeries<LocalDate> {

  public abstract static class Integer extends AbstractIntDoubleTimeSeries<LocalDate> implements LocalDateDoubleTimeSeries {
    public Integer(final DateTimeConverter<LocalDate> converter, final FastIntDoubleTimeSeries timeSeries) {
      super(converter, timeSeries);
    }
    
    @Override
    public TimeSeries<LocalDate, Double> newInstance(final LocalDate[] dateTimes, final Double[] values) {
      return newInstanceFast(dateTimes, ArrayUtils.toPrimitive(values));
    }

    public abstract LocalDateDoubleTimeSeries newInstanceFast(LocalDate[] dateTimes, double[] values);
  }

  public abstract static class Long extends AbstractLongDoubleTimeSeries<LocalDate> implements LocalDateDoubleTimeSeries {
    public Long(final DateTimeConverter<LocalDate> converter, final FastLongDoubleTimeSeries timeSeries) {
      super(converter, timeSeries);
    }
    
    @Override
    public TimeSeries<LocalDate, Double> newInstance(final LocalDate[] dateTimes, final Double[] values) {
      return newInstanceFast(dateTimes, ArrayUtils.toPrimitive(values));
    }

    public abstract LocalDateDoubleTimeSeries newInstanceFast(LocalDate[] dateTimes, double[] values);
  }
}
