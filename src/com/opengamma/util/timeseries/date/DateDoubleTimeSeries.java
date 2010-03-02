/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.util.timeseries.date;

import java.util.Date;
import java.util.TimeZone;

import com.opengamma.util.Primitives;
import com.opengamma.util.timeseries.AbstractIntDoubleTimeSeries;
import com.opengamma.util.timeseries.AbstractLongDoubleTimeSeries;
import com.opengamma.util.timeseries.DateTimeConverter;
import com.opengamma.util.timeseries.DoubleTimeSeries;
import com.opengamma.util.timeseries.FastBackedDoubleTimeSeries;
import com.opengamma.util.timeseries.TimeSeries;
import com.opengamma.util.timeseries.date.time.ArrayDateTimeDoubleTimeSeries;
import com.opengamma.util.timeseries.date.time.DateTimeDoubleTimeSeries;
import com.opengamma.util.timeseries.date.time.ListDateTimeDoubleTimeSeries;
import com.opengamma.util.timeseries.date.time.MutableDateTimeDoubleTimeSeries;
import com.opengamma.util.timeseries.fast.DateTimeNumericEncoding;
import com.opengamma.util.timeseries.fast.integer.FastIntDoubleTimeSeries;
import com.opengamma.util.timeseries.fast.longint.FastLongDoubleTimeSeries;
import com.opengamma.util.timeseries.zoneddatetime.ArrayZonedDateTimeDoubleTimeSeries;
import com.opengamma.util.timeseries.zoneddatetime.ListZonedDateTimeDoubleTimeSeries;
import com.opengamma.util.timeseries.zoneddatetime.MutableZonedDateTimeDoubleTimeSeries;
import com.opengamma.util.timeseries.zoneddatetime.ZonedDateTimeDoubleTimeSeries;

/**
 * @author jim
 * 
 */
public interface DateDoubleTimeSeries extends DoubleTimeSeries<Date>, FastBackedDoubleTimeSeries<Date> {

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
    
    @Override
    public ZonedDateTimeDoubleTimeSeries toZonedDateTimeDoubleTimeSeries() {
      return new ArrayZonedDateTimeDoubleTimeSeries(((DateEpochDaysConverter)getConverter()).getTimeZone310(), getFastSeries().toFastLongDoubleTimeSeries(DateTimeNumericEncoding.TIME_EPOCH_MILLIS));
    }
    
    @Override
    public ZonedDateTimeDoubleTimeSeries toZonedDateTimeDoubleTimeSeries(javax.time.calendar.TimeZone timeZone) {
      return new ArrayZonedDateTimeDoubleTimeSeries(timeZone, getFastSeries().toFastLongDoubleTimeSeries(DateTimeNumericEncoding.TIME_EPOCH_MILLIS));
    }

    @Override
    public MutableZonedDateTimeDoubleTimeSeries toMutableZonedDateTimeDoubleTimeSeries() {
      return new ListZonedDateTimeDoubleTimeSeries(((DateEpochDaysConverter)getConverter()).getTimeZone310(), getFastSeries().toFastMutableLongDoubleTimeSeries(DateTimeNumericEncoding.TIME_EPOCH_MILLIS));
    }
    
    @Override
    public MutableZonedDateTimeDoubleTimeSeries toMutableZonedDateTimeDoubleTimeSeries(javax.time.calendar.TimeZone timeZone) {
      return new ListZonedDateTimeDoubleTimeSeries(timeZone, getFastSeries().toFastMutableLongDoubleTimeSeries(DateTimeNumericEncoding.TIME_EPOCH_MILLIS));
    }

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
    
    @Override
    public ZonedDateTimeDoubleTimeSeries toZonedDateTimeDoubleTimeSeries() {
      return new ArrayZonedDateTimeDoubleTimeSeries(((DateEpochDaysConverter)getConverter()).getTimeZone310(), getFastSeries().toFastLongDoubleTimeSeries(DateTimeNumericEncoding.TIME_EPOCH_MILLIS));
    }
    
    @Override
    public ZonedDateTimeDoubleTimeSeries toZonedDateTimeDoubleTimeSeries(javax.time.calendar.TimeZone timeZone) {
      return new ArrayZonedDateTimeDoubleTimeSeries(timeZone, getFastSeries().toFastLongDoubleTimeSeries(DateTimeNumericEncoding.TIME_EPOCH_MILLIS));
    }

    @Override
    public MutableZonedDateTimeDoubleTimeSeries toMutableZonedDateTimeDoubleTimeSeries() {
      return new ListZonedDateTimeDoubleTimeSeries(((DateEpochDaysConverter)getConverter()).getTimeZone310(), getFastSeries().toFastMutableLongDoubleTimeSeries(DateTimeNumericEncoding.TIME_EPOCH_MILLIS));
    }
    
    @Override
    public MutableZonedDateTimeDoubleTimeSeries toMutableZonedDateTimeDoubleTimeSeries(javax.time.calendar.TimeZone timeZone) {
      return new ListZonedDateTimeDoubleTimeSeries(timeZone, getFastSeries().toFastMutableLongDoubleTimeSeries(DateTimeNumericEncoding.TIME_EPOCH_MILLIS));
    }
  }
}
