/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.timeseries.zoneddatetime;

import org.threeten.bp.ZonedDateTime;

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
public interface MutableZonedDateTimeDoubleTimeSeries extends ZonedDateTimeDoubleTimeSeries, MutableDoubleTimeSeries<ZonedDateTime> {

  /** */
  public abstract static class Integer
      extends AbstractMutableIntDoubleTimeSeries<ZonedDateTime>
      implements MutableZonedDateTimeDoubleTimeSeries {

    /** Serialization version. */
    private static final long serialVersionUID = -181714699836889630L;

    public Integer(final DateTimeConverter<ZonedDateTime> converter, final FastMutableIntDoubleTimeSeries timeSeries) {
      super(converter, timeSeries);
    }

    @Override
    public MutableZonedDateTimeDoubleTimeSeries newInstance(final ZonedDateTime[] dateTimes, final Double[] values) {
      return newInstanceFast(dateTimes, TimeSeriesUtils.toPrimitive(values));
    }

    public abstract MutableZonedDateTimeDoubleTimeSeries newInstanceFast(ZonedDateTime[] dateTimes, double[] values);
  }

  /** */
  public abstract static class Long
      extends AbstractMutableLongDoubleTimeSeries<ZonedDateTime>
      implements MutableZonedDateTimeDoubleTimeSeries {

    /** Serialization version. */
    private static final long serialVersionUID = 2147484208841802248L;

    public Long(final DateTimeConverter<ZonedDateTime> converter, final FastMutableLongDoubleTimeSeries timeSeries) {
      super(converter, timeSeries);
    }

    @Override
    public MutableZonedDateTimeDoubleTimeSeries newInstance(final ZonedDateTime[] dateTimes, final Double[] values) {
      return newInstanceFast(dateTimes, TimeSeriesUtils.toPrimitive(values));
    }

    public abstract MutableZonedDateTimeDoubleTimeSeries newInstanceFast(ZonedDateTime[] dateTimes, double[] values);
  }

}
