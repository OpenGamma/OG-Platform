/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.timeseries.date;

import java.util.Date;
import java.util.TimeZone;

import com.opengamma.timeseries.AbstractMutableIntDoubleTimeSeries;
import com.opengamma.timeseries.AbstractMutableLongDoubleTimeSeries;
import com.opengamma.timeseries.DateTimeConverter;
import com.opengamma.timeseries.MutableDoubleTimeSeries;
import com.opengamma.timeseries.TimeSeries;
import com.opengamma.timeseries.date.time.ArrayDateTimeDoubleTimeSeries;
import com.opengamma.timeseries.date.time.DateTimeDoubleTimeSeries;
import com.opengamma.timeseries.date.time.ListDateTimeDoubleTimeSeries;
import com.opengamma.timeseries.date.time.MutableDateTimeDoubleTimeSeries;
import com.opengamma.timeseries.fast.DateTimeNumericEncoding;
import com.opengamma.timeseries.fast.integer.FastMutableIntDoubleTimeSeries;
import com.opengamma.timeseries.fast.longint.FastMutableLongDoubleTimeSeries;
import com.opengamma.util.Primitives;

/**
 * @author jim
 * 
 */
public interface MutableDateDoubleTimeSeries extends DateDoubleTimeSeries, MutableDoubleTimeSeries<Date> {

  public static abstract class Integer extends AbstractMutableIntDoubleTimeSeries<Date> implements MutableDateDoubleTimeSeries {
    public Integer(final DateTimeConverter<Date> converter, final FastMutableIntDoubleTimeSeries timeSeries) {
      super(converter, timeSeries);
    }

    // @Override
    // protected MutableDoubleTimeSeries<Date> makeMutableListTimeSeries() {
    // return new MutableDateDoubleTimeSeries.Integer(getConverter(),
    // makePrimitiveMutableListTimeSeries());
    // }
    //
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
    
    @Override
    public DateDoubleTimeSeries toDateDoubleTimeSeries() {
      return new ArrayDateDoubleTimeSeries(((DateEpochDaysConverter)getConverter()).getTimeZone(), this);
    }

    @Override
    public DateDoubleTimeSeries toDateDoubleTimeSeries(TimeZone timeZone) {
      return new ArrayDateDoubleTimeSeries(timeZone, this);
    }

    @Override
    public DateTimeDoubleTimeSeries toDateTimeDoubleTimeSeries() {
      return new ArrayDateTimeDoubleTimeSeries(((DateEpochDaysConverter)getConverter()).getTimeZone(), toFastLongDoubleTimeSeries());
    }

    @Override
    public DateTimeDoubleTimeSeries toDateTimeDoubleTimeSeries(TimeZone timeZone) {
      return new ArrayDateTimeDoubleTimeSeries(((DateEpochDaysConverter)getConverter()).getTimeZone(), toFastLongDoubleTimeSeries());
    }

    @Override
    public MutableDateDoubleTimeSeries toMutableDateDoubleTimeSeries() {
      return new ListDateDoubleTimeSeries(this);
    }

    @Override
    public MutableDateDoubleTimeSeries toMutableDateDoubleTimeSeries(
        TimeZone timeZone) {
      return new ListDateDoubleTimeSeries(timeZone, this);
    }

    @Override
    public MutableDateTimeDoubleTimeSeries toMutableDateTimeDoubleTimeSeries() {
      return new ListDateTimeDoubleTimeSeries(getFastSeries().toFastMutableLongDoubleTimeSeries(DateTimeNumericEncoding.TIME_EPOCH_MILLIS));
    }

    @Override
    public MutableDateTimeDoubleTimeSeries toMutableDateTimeDoubleTimeSeries(
        TimeZone timeZone) {
      return new ListDateTimeDoubleTimeSeries(timeZone, getFastSeries().toFastMutableLongDoubleTimeSeries(DateTimeNumericEncoding.TIME_EPOCH_MILLIS));
    }
  }

  public abstract static class Long extends AbstractMutableLongDoubleTimeSeries<Date> implements MutableDateDoubleTimeSeries {
    public Long(final DateTimeConverter<Date> converter, final FastMutableLongDoubleTimeSeries timeSeries) {
      super(converter, timeSeries);
    }

    // @Override
    // protected MutableDoubleTimeSeries<Date> makeMutableListTimeSeries() {
    // return new MutableDateDoubleTimeSeries.Long(getConverter(),
    // makePrimitiveMutableListTimeSeries());
    // }

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
    
    @Override
    public DateDoubleTimeSeries toDateDoubleTimeSeries() {
      return new ArrayDateDoubleTimeSeries(((DateEpochDaysConverter)getConverter()).getTimeZone(), this);
    }

    @Override
    public DateDoubleTimeSeries toDateDoubleTimeSeries(TimeZone timeZone) {
      return new ArrayDateDoubleTimeSeries(timeZone, this);
    }

    @Override
    public DateTimeDoubleTimeSeries toDateTimeDoubleTimeSeries() {
      return new ArrayDateTimeDoubleTimeSeries(((DateEpochDaysConverter)getConverter()).getTimeZone(), toFastLongDoubleTimeSeries());
    }

    @Override
    public DateTimeDoubleTimeSeries toDateTimeDoubleTimeSeries(TimeZone timeZone) {
      return new ArrayDateTimeDoubleTimeSeries(((DateEpochDaysConverter)getConverter()).getTimeZone(), toFastLongDoubleTimeSeries());
    }

    @Override
    public MutableDateDoubleTimeSeries toMutableDateDoubleTimeSeries() {
      return new ListDateDoubleTimeSeries(this);
    }

    @Override
    public MutableDateDoubleTimeSeries toMutableDateDoubleTimeSeries(
        TimeZone timeZone) {
      return new ListDateDoubleTimeSeries(timeZone, this);
    }

    @Override
    public MutableDateTimeDoubleTimeSeries toMutableDateTimeDoubleTimeSeries() {
      return new ListDateTimeDoubleTimeSeries(getFastSeries().toFastMutableLongDoubleTimeSeries(DateTimeNumericEncoding.TIME_EPOCH_MILLIS));
    }

    @Override
    public MutableDateTimeDoubleTimeSeries toMutableDateTimeDoubleTimeSeries(
        TimeZone timeZone) {
      return new ListDateTimeDoubleTimeSeries(timeZone, getFastSeries().toFastMutableLongDoubleTimeSeries(DateTimeNumericEncoding.TIME_EPOCH_MILLIS));
    }
  }
}
