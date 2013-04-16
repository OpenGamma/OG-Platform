/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.timeseries.sqldate;

import java.sql.Date;

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
public interface MutableSQLDateDoubleTimeSeries extends SQLDateDoubleTimeSeries, MutableDoubleTimeSeries<Date> {

  /** */
  public abstract static class Integer extends AbstractMutableIntDoubleTimeSeries<Date> implements
      MutableSQLDateDoubleTimeSeries {
    public Integer(final DateTimeConverter<Date> converter, final FastMutableIntDoubleTimeSeries timeSeries) {
      super(converter, timeSeries);
    }

    @Override
    public MutableSQLDateDoubleTimeSeries newInstance(final Date[] dateTimes, final Double[] values) {
      return newInstanceFast(dateTimes, TimeSeriesUtils.toPrimitive(values));
    }

    public abstract MutableSQLDateDoubleTimeSeries newInstanceFast(Date[] dateTimes, double[] values);
  }

  /** */
  public abstract static class Long extends AbstractMutableLongDoubleTimeSeries<Date> implements
      MutableSQLDateDoubleTimeSeries {
    public Long(final DateTimeConverter<Date> converter, final FastMutableLongDoubleTimeSeries timeSeries) {
      super(converter, timeSeries);
    }

    @Override
    public MutableSQLDateDoubleTimeSeries newInstance(final Date[] dateTimes, final Double[] values) {
      return newInstanceFast(dateTimes, TimeSeriesUtils.toPrimitive(values));
    }

    public abstract MutableSQLDateDoubleTimeSeries newInstanceFast(Date[] dateTimes, double[] values);

  }
}
