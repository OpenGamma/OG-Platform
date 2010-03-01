/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.timeseries.date.time;

import java.util.Date;
import java.util.TimeZone;

import com.opengamma.timeseries.AbstractIntDoubleTimeSeries;
import com.opengamma.timeseries.AbstractLongDoubleTimeSeries;
import com.opengamma.timeseries.DateTimeConverter;
import com.opengamma.timeseries.DoubleTimeSeries;
import com.opengamma.timeseries.TimeSeries;
import com.opengamma.timeseries.date.ArrayDateDoubleTimeSeries;
import com.opengamma.timeseries.date.DateDoubleTimeSeries;
import com.opengamma.timeseries.date.ListDateDoubleTimeSeries;
import com.opengamma.timeseries.date.MutableDateDoubleTimeSeries;
import com.opengamma.timeseries.fast.DateTimeNumericEncoding;
import com.opengamma.timeseries.fast.integer.FastIntDoubleTimeSeries;
import com.opengamma.timeseries.fast.longint.FastLongDoubleTimeSeries;
import com.opengamma.util.Primitives;

/**
 * @author jim
 *         This class isn't really necessary but it meant to serve as a way to
 *         distinguish DateDoubleTimeSeries that can store full time accuracy
 *         from that which can't.
 */
public interface DateTimeDoubleTimeSeries extends DoubleTimeSeries<Date> {

  public abstract static class Integer extends AbstractIntDoubleTimeSeries<Date> implements DateTimeDoubleTimeSeries {
    public Integer(final DateTimeConverter<Date> converter, final FastIntDoubleTimeSeries timeSeries) {
      super(converter, timeSeries);
    }

    @Override
    public TimeSeries<Date, Double> newInstance(final Date[] dateTimes, final Double[] values) {
      return newInstanceFast(dateTimes, Primitives.unbox(values));
    }

    public abstract DateTimeDoubleTimeSeries newInstanceFast(Date[] dateTimes, double[] values);

    @Override
    public DateDoubleTimeSeries toDateDoubleTimeSeries() {
      return new ArrayDateDoubleTimeSeries(((DateEpochMillisConverter)getConverter()).getTimeZone(), getFastSeries().toFastIntDoubleTimeSeries(DateTimeNumericEncoding.DATE_EPOCH_DAYS));
    }

    @Override
    public DateDoubleTimeSeries toDateDoubleTimeSeries(TimeZone timeZone) {
      return new ArrayDateDoubleTimeSeries(timeZone, getFastSeries().toFastIntDoubleTimeSeries(DateTimeNumericEncoding.DATE_EPOCH_DAYS));
    }

    @Override
    public DateTimeDoubleTimeSeries toDateTimeDoubleTimeSeries() {
      return this;
    }

    @Override
    public DateTimeDoubleTimeSeries toDateTimeDoubleTimeSeries(TimeZone timeZone) {
      return new ArrayDateTimeDoubleTimeSeries(timeZone, this);
    }

    @Override
    public MutableDateDoubleTimeSeries toMutableDateDoubleTimeSeries() {
      return new ListDateDoubleTimeSeries(((DateEpochMillisConverter)getConverter()).getTimeZone(), getFastSeries().toFastMutableIntDoubleTimeSeries(DateTimeNumericEncoding.DATE_EPOCH_DAYS));
    }

    @Override
    public MutableDateDoubleTimeSeries toMutableDateDoubleTimeSeries(
        TimeZone timeZone) {
      return new ListDateDoubleTimeSeries(timeZone, getFastSeries().toFastMutableIntDoubleTimeSeries(DateTimeNumericEncoding.DATE_EPOCH_DAYS));
    }

    @Override
    public MutableDateTimeDoubleTimeSeries toMutableDateTimeDoubleTimeSeries() {
      return new ListDateTimeDoubleTimeSeries(((DateEpochMillisConverter)getConverter()).getTimeZone(), this);
    }

    @Override
    public MutableDateTimeDoubleTimeSeries toMutableDateTimeDoubleTimeSeries(
        TimeZone timeZone) {
      return new ListDateTimeDoubleTimeSeries(timeZone, this);
    }

    
  }

  public abstract static class Long extends AbstractLongDoubleTimeSeries<Date> implements DateTimeDoubleTimeSeries {
    public Long(final DateTimeConverter<Date> converter, final FastLongDoubleTimeSeries timeSeries) {
      super(converter, timeSeries);
    }

    @Override
    public TimeSeries<Date, Double> newInstance(final Date[] dateTimes, final Double[] values) {
      return newInstanceFast(dateTimes, Primitives.unbox(values));
    }

    public abstract DateTimeDoubleTimeSeries newInstanceFast(Date[] dateTimes, double[] values);
    
    @Override
    public DateDoubleTimeSeries toDateDoubleTimeSeries() {
      return new ArrayDateDoubleTimeSeries(((DateEpochMillisConverter)getConverter()).getTimeZone(), getFastSeries().toFastIntDoubleTimeSeries(DateTimeNumericEncoding.DATE_EPOCH_DAYS));
    }

    @Override
    public DateDoubleTimeSeries toDateDoubleTimeSeries(TimeZone timeZone) {
      return new ArrayDateDoubleTimeSeries(timeZone, getFastSeries().toFastIntDoubleTimeSeries(DateTimeNumericEncoding.DATE_EPOCH_DAYS));
    }

    @Override
    public DateTimeDoubleTimeSeries toDateTimeDoubleTimeSeries() {
      return this;
    }

    @Override
    public DateTimeDoubleTimeSeries toDateTimeDoubleTimeSeries(TimeZone timeZone) {
      return new ArrayDateTimeDoubleTimeSeries(timeZone, this);
    }

    @Override
    public MutableDateDoubleTimeSeries toMutableDateDoubleTimeSeries() {
      return new ListDateDoubleTimeSeries(((DateEpochMillisConverter)getConverter()).getTimeZone(), getFastSeries().toFastMutableIntDoubleTimeSeries(DateTimeNumericEncoding.DATE_EPOCH_DAYS));
    }

    @Override
    public MutableDateDoubleTimeSeries toMutableDateDoubleTimeSeries(
        TimeZone timeZone) {
      return new ListDateDoubleTimeSeries(timeZone, getFastSeries().toFastMutableIntDoubleTimeSeries(DateTimeNumericEncoding.DATE_EPOCH_DAYS));
    }

    @Override
    public MutableDateTimeDoubleTimeSeries toMutableDateTimeDoubleTimeSeries() {
      return new ListDateTimeDoubleTimeSeries(((DateEpochMillisConverter)getConverter()).getTimeZone(), this);
    }

    @Override
    public MutableDateTimeDoubleTimeSeries toMutableDateTimeDoubleTimeSeries(
        TimeZone timeZone) {
      return new ListDateTimeDoubleTimeSeries(timeZone, this);
    }

  }
}
