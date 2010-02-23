/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.timeseries.date;

import java.util.Date;

import com.opengamma.timeseries.AbstractIntDoubleTimeSeries;
import com.opengamma.timeseries.AbstractLongDoubleTimeSeries;
import com.opengamma.timeseries.DateTimeConverter;
import com.opengamma.timeseries.DoubleTimeSeries;
import com.opengamma.timeseries.TimeSeries;
import com.opengamma.timeseries.fast.integer.FastIntDoubleTimeSeries;
import com.opengamma.timeseries.fast.longint.FastLongDoubleTimeSeries;
import com.opengamma.util.Primitives;

/**
 * @author jim
 * 
 */
public interface DateDoubleTimeSeries extends DoubleTimeSeries<Date> {

  public abstract static class Integer extends AbstractIntDoubleTimeSeries<Date> implements DateDoubleTimeSeries {
    public Integer(final DateTimeConverter<Date> converter, final FastIntDoubleTimeSeries timeSeries) {
      super(converter, timeSeries);
    }

    // @Override
    // protected MutableDoubleTimeSeries<Date> makeMutableListTimeSeries() {
    // return new MutableDateDoubleTimeSeries.Integer(getConverter(),
    // makePrimitiveMutableListTimeSeries());
    // }

    // @Override
    // protected FastMutableIntDoubleTimeSeries
    // makePrimitiveMutableListTimeSeries() {
    // return new FastListIntDoubleTimeSeries(getFastSeries().getEncoding());
    // }

    @Override
    public TimeSeries<Date, Double> newInstance(final Date[] dateTimes, final Double[] values) {
      return newInstanceFast(dateTimes, Primitives.unbox(values));
    }

    public abstract DateDoubleTimeSeries newInstanceFast(Date[] dateTimes, double[] values);

  }

  public abstract static class Long extends AbstractLongDoubleTimeSeries<Date> implements DateDoubleTimeSeries {
    public Long(final DateTimeConverter<Date> converter, final FastLongDoubleTimeSeries timeSeries) {
      super(converter, timeSeries);
    }

    // @Override
    // protected MutableDoubleTimeSeries<Date> makeMutableListTimeSeries() {
    // return new MutableDateDoubleTimeSeries.Long(getConverter(),
    // makePrimitiveMutableListTimeSeries());
    // }
    //
    // @Override
    // protected FastMutableLongDoubleTimeSeries
    // makePrimitiveMutableListTimeSeries() {
    // return new FastListLongDoubleTimeSeries(getFastSeries().getEncoding());
    // }

    @Override
    public TimeSeries<Date, Double> newInstance(final Date[] dateTimes, final Double[] values) {
      return newInstanceFast(dateTimes, Primitives.unbox(values));
    }

    public abstract DateDoubleTimeSeries newInstanceFast(Date[] dateTimes, double[] values);
  }
}
