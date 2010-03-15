/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.util.timeseries.yearoffset;

import java.util.TimeZone;
import java.util.Date;
import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.ArrayUtils;

import com.opengamma.util.timeseries.AbstractIntDoubleTimeSeries;
import com.opengamma.util.timeseries.AbstractLongDoubleTimeSeries;
import com.opengamma.util.timeseries.DateTimeConverter;
import com.opengamma.util.timeseries.DoubleTimeSeries;
import com.opengamma.util.timeseries.FastBackedDoubleTimeSeries;
import com.opengamma.util.timeseries.TimeSeries;
import com.opengamma.util.timeseries.date.ArrayDateDoubleTimeSeries;
import com.opengamma.util.timeseries.date.DateDoubleTimeSeries;
import com.opengamma.util.timeseries.date.ListDateDoubleTimeSeries;
import com.opengamma.util.timeseries.date.MutableDateDoubleTimeSeries;
import com.opengamma.util.timeseries.date.time.ArrayDateTimeDoubleTimeSeries;
import com.opengamma.util.timeseries.date.time.DateTimeDoubleTimeSeries;
import com.opengamma.util.timeseries.date.time.ListDateTimeDoubleTimeSeries;
import com.opengamma.util.timeseries.date.time.MutableDateTimeDoubleTimeSeries;
import com.opengamma.util.timeseries.fast.DateTimeNumericEncoding;
import com.opengamma.util.timeseries.fast.integer.FastIntDoubleTimeSeries;
import com.opengamma.util.timeseries.fast.longint.FastLongDoubleTimeSeries;
import com.opengamma.util.timeseries.sqldate.ArraySQLDateDoubleTimeSeries;
import com.opengamma.util.timeseries.sqldate.ListSQLDateDoubleTimeSeries;
import com.opengamma.util.timeseries.sqldate.MutableSQLDateDoubleTimeSeries;
import com.opengamma.util.timeseries.sqldate.SQLDateDoubleTimeSeries;
import com.opengamma.util.timeseries.zoneddatetime.ArrayZonedDateTimeDoubleTimeSeries;
import com.opengamma.util.timeseries.zoneddatetime.ListZonedDateTimeDoubleTimeSeries;
import com.opengamma.util.timeseries.zoneddatetime.MutableZonedDateTimeDoubleTimeSeries;
import com.opengamma.util.timeseries.zoneddatetime.ZonedDateTimeDoubleTimeSeries;

/**
 * @author jim
 * 
 */
public interface YearOffsetDoubleTimeSeries extends DoubleTimeSeries<Double>, FastBackedDoubleTimeSeries<Double> {

  public abstract static class Integer extends AbstractIntDoubleTimeSeries<Double> implements YearOffsetDoubleTimeSeries {
    public Integer(final DateTimeConverter<Double> converter, final FastIntDoubleTimeSeries timeSeries) {
      super(converter, timeSeries);
    }

    @Override
    public TimeSeries<Double, Double> newInstance(final Double[] dateTimes, final Double[] values) {
      return newInstanceFast(dateTimes, ArrayUtils.toPrimitive(values));
    }

    public abstract YearOffsetDoubleTimeSeries newInstanceFast(Double[] dateTimes, double[] values);

    @Override
    public DateDoubleTimeSeries toDateDoubleTimeSeries() {
      return new ArrayDateDoubleTimeSeries(((YearOffsetEpochMillisConverter)getConverter()).getTimeZone(), getFastSeries().toFastIntDoubleTimeSeries(DateTimeNumericEncoding.DATE_EPOCH_DAYS));
    }

    @Override
    public DateDoubleTimeSeries toDateDoubleTimeSeries(TimeZone timeZone) {
      return new ArrayDateDoubleTimeSeries(timeZone, getFastSeries().toFastIntDoubleTimeSeries(DateTimeNumericEncoding.DATE_EPOCH_DAYS));
    }
    
    @Override
    public SQLDateDoubleTimeSeries toSQLDateDoubleTimeSeries() {
      return new ArraySQLDateDoubleTimeSeries(((YearOffsetEpochMillisConverter)getConverter()).getTimeZone(), toFastIntDoubleTimeSeries());
    }

    @Override
    public SQLDateDoubleTimeSeries toSQLDateDoubleTimeSeries(TimeZone timeZone) {
      return new ArraySQLDateDoubleTimeSeries(timeZone, toFastIntDoubleTimeSeries());
    }

    @Override
    public MutableSQLDateDoubleTimeSeries toMutableSQLDateDoubleTimeSeries() {
      return new ListSQLDateDoubleTimeSeries(((YearOffsetEpochMillisConverter)getConverter()).getTimeZone(), toFastMutableIntDoubleTimeSeries());
    }

    @Override
    public MutableSQLDateDoubleTimeSeries toMutableSQLDateDoubleTimeSeries(TimeZone timeZone) {
      return new ListSQLDateDoubleTimeSeries(timeZone, toFastMutableIntDoubleTimeSeries());
    }

    @Override
    public DateTimeDoubleTimeSeries toDateTimeDoubleTimeSeries() {
      return new ArrayDateTimeDoubleTimeSeries(getFastSeries().toFastLongDoubleTimeSeries(DateTimeNumericEncoding.TIME_EPOCH_MILLIS));
    }

    @Override
    public DateTimeDoubleTimeSeries toDateTimeDoubleTimeSeries(TimeZone timeZone) {
      return new ArrayDateTimeDoubleTimeSeries(timeZone, getFastSeries().toFastLongDoubleTimeSeries(DateTimeNumericEncoding.TIME_EPOCH_MILLIS));
    }

    @Override
    public MutableDateDoubleTimeSeries toMutableDateDoubleTimeSeries() {
      return new ListDateDoubleTimeSeries(((YearOffsetEpochMillisConverter)getConverter()).getTimeZone(), getFastSeries().toFastMutableIntDoubleTimeSeries(DateTimeNumericEncoding.DATE_EPOCH_DAYS));
    }

    @Override
    public MutableDateDoubleTimeSeries toMutableDateDoubleTimeSeries(
        TimeZone timeZone) {
      return new ListDateDoubleTimeSeries(timeZone, getFastSeries().toFastMutableIntDoubleTimeSeries(DateTimeNumericEncoding.DATE_EPOCH_DAYS));
    }

    @Override
    public MutableDateTimeDoubleTimeSeries toMutableDateTimeDoubleTimeSeries() {
      return new ListDateTimeDoubleTimeSeries(((YearOffsetEpochMillisConverter)getConverter()).getTimeZone(), getFastSeries().toFastMutableLongDoubleTimeSeries(DateTimeNumericEncoding.TIME_EPOCH_MILLIS));
    }

    @Override
    public MutableDateTimeDoubleTimeSeries toMutableDateTimeDoubleTimeSeries(
        TimeZone timeZone) {
      return new ListDateTimeDoubleTimeSeries(timeZone, getFastSeries().toFastMutableLongDoubleTimeSeries());
    }
    
    // convenience method.
    public YearOffsetDoubleTimeSeries toYearOffsetDoubleTimeSeries() {
      return new ArrayYearOffsetDoubleTimeSeries(((YearOffsetEpochMillisConverter)getConverter()).getZonedOffset(), this);
    }

    @Override
    public YearOffsetDoubleTimeSeries toYearOffsetDoubleTimeSeries(ZonedDateTime zeroOffset) {
      return new ArrayYearOffsetDoubleTimeSeries(zeroOffset, this);
    }
    
    @Override
    public YearOffsetDoubleTimeSeries toYearOffsetDoubleTimeSeries(java.util.TimeZone timeZone, Date zeroDate) {
      return new ArrayYearOffsetDoubleTimeSeries(timeZone, zeroDate, this);
    }
    
    public MutableYearOffsetDoubleTimeSeries toMutableYearOffsetDoubleTimeSeries() {
      return new ListYearOffsetDoubleTimeSeries(((YearOffsetEpochMillisConverter)getConverter()).getZonedOffset(), this);
    }

    @Override
    public MutableYearOffsetDoubleTimeSeries toMutableYearOffsetDoubleTimeSeries(ZonedDateTime zeroDate) {
      return new ListYearOffsetDoubleTimeSeries(zeroDate, this);
    }
    
    @Override
    public MutableYearOffsetDoubleTimeSeries toMutableYearOffsetDoubleTimeSeries(java.util.TimeZone timeZone, Date zeroDate) {
      return new ListYearOffsetDoubleTimeSeries(timeZone, zeroDate, this);
    }
  }

  public abstract static class Long extends AbstractLongDoubleTimeSeries<Double> implements YearOffsetDoubleTimeSeries {
    public Long(final DateTimeConverter<Double> converter, final FastLongDoubleTimeSeries timeSeries) {
      super(converter, timeSeries);
    }

    @Override
    public TimeSeries<Double, Double> newInstance(final Double[] dateTimes, final Double[] values) {
      return newInstanceFast(dateTimes, ArrayUtils.toPrimitive(values));
    }

    public abstract YearOffsetDoubleTimeSeries newInstanceFast(Double[] dateTimes, double[] values);
    
    @Override
    public DateDoubleTimeSeries toDateDoubleTimeSeries() {
      return new ArrayDateDoubleTimeSeries(((YearOffsetEpochMillisConverter)getConverter()).getTimeZone(), getFastSeries().toFastIntDoubleTimeSeries(DateTimeNumericEncoding.DATE_EPOCH_DAYS));
    }

    @Override
    public DateDoubleTimeSeries toDateDoubleTimeSeries(TimeZone timeZone) {
      return new ArrayDateDoubleTimeSeries(timeZone, getFastSeries().toFastIntDoubleTimeSeries(DateTimeNumericEncoding.DATE_EPOCH_DAYS));
    }

    @Override
    public SQLDateDoubleTimeSeries toSQLDateDoubleTimeSeries() {
      return new ArraySQLDateDoubleTimeSeries(((YearOffsetEpochMillisConverter)getConverter()).getTimeZone(), toFastIntDoubleTimeSeries());
    }

    @Override
    public SQLDateDoubleTimeSeries toSQLDateDoubleTimeSeries(TimeZone timeZone) {
      return new ArraySQLDateDoubleTimeSeries(timeZone, toFastIntDoubleTimeSeries());
    }

    @Override
    public MutableSQLDateDoubleTimeSeries toMutableSQLDateDoubleTimeSeries() {
      return new ListSQLDateDoubleTimeSeries(((YearOffsetEpochMillisConverter)getConverter()).getTimeZone(), toFastMutableIntDoubleTimeSeries());
    }

    @Override
    public MutableSQLDateDoubleTimeSeries toMutableSQLDateDoubleTimeSeries(TimeZone timeZone) {
      return new ListSQLDateDoubleTimeSeries(timeZone, toFastMutableIntDoubleTimeSeries());
    }
    
    @Override
    public DateTimeDoubleTimeSeries toDateTimeDoubleTimeSeries() {
      return new ArrayDateTimeDoubleTimeSeries(((YearOffsetEpochMillisConverter)getConverter()).getTimeZone(), getFastSeries());
    }

    @Override
    public DateTimeDoubleTimeSeries toDateTimeDoubleTimeSeries(TimeZone timeZone) {
      return new ArrayDateTimeDoubleTimeSeries(timeZone, getFastSeries());
    }

    @Override
    public MutableDateDoubleTimeSeries toMutableDateDoubleTimeSeries() {
      return new ListDateDoubleTimeSeries(((YearOffsetEpochMillisConverter)getConverter()).getTimeZone(), getFastSeries().toFastMutableIntDoubleTimeSeries(DateTimeNumericEncoding.DATE_EPOCH_DAYS));
    }

    @Override
    public MutableDateDoubleTimeSeries toMutableDateDoubleTimeSeries(
        TimeZone timeZone) {
      return new ListDateDoubleTimeSeries(timeZone, getFastSeries().toFastMutableIntDoubleTimeSeries(DateTimeNumericEncoding.DATE_EPOCH_DAYS));
    }

    @Override
    public MutableDateTimeDoubleTimeSeries toMutableDateTimeDoubleTimeSeries() {
      return new ListDateTimeDoubleTimeSeries(((YearOffsetEpochMillisConverter)getConverter()).getTimeZone(), getFastSeries().toFastMutableLongDoubleTimeSeries());
    }

    @Override
    public MutableDateTimeDoubleTimeSeries toMutableDateTimeDoubleTimeSeries(
        TimeZone timeZone) {
      return new ListDateTimeDoubleTimeSeries(timeZone, getFastSeries().toFastMutableLongDoubleTimeSeries());
    }

    // convenience method.
    public YearOffsetDoubleTimeSeries toYearOffsetDoubleTimeSeries() {
      return new ArrayYearOffsetDoubleTimeSeries(((YearOffsetEpochMillisConverter)getConverter()).getZonedOffset(), this);
    }

    public YearOffsetDoubleTimeSeries toYearOffsetDoubleTimeSeries(ZonedDateTime zeroDate) {
      return new ArrayYearOffsetDoubleTimeSeries(zeroDate, this);
    }
    
    @Override
    public YearOffsetDoubleTimeSeries toYearOffsetDoubleTimeSeries(java.util.TimeZone timeZone, Date zeroDate) {
      return new ArrayYearOffsetDoubleTimeSeries(timeZone, zeroDate, this);
    }

    // convenience method.
    public MutableYearOffsetDoubleTimeSeries toMutableYearOffsetDoubleTimeSeries() {
      return new ListYearOffsetDoubleTimeSeries(((YearOffsetEpochMillisConverter)getConverter()).getZonedOffset(), getFastSeries().toFastMutableLongDoubleTimeSeries());
    }

    @Override
    public MutableYearOffsetDoubleTimeSeries toMutableYearOffsetDoubleTimeSeries(ZonedDateTime zeroDate) {
      return new ListYearOffsetDoubleTimeSeries(zeroDate, getFastSeries().toFastMutableLongDoubleTimeSeries());
    }
    
    @Override
    public MutableYearOffsetDoubleTimeSeries toMutableYearOffsetDoubleTimeSeries(java.util.TimeZone timeZone, Date zeroDate) {
      return new ListYearOffsetDoubleTimeSeries(timeZone, zeroDate, getFastSeries().toFastMutableLongDoubleTimeSeries());
    }
    
    @Override
    public MutableZonedDateTimeDoubleTimeSeries toMutableZonedDateTimeDoubleTimeSeries() {
      return new ListZonedDateTimeDoubleTimeSeries(((YearOffsetEpochMillisConverter)getConverter()).getTimeZone310(), getFastSeries().toFastMutableLongDoubleTimeSeries());
    }

    @Override
    public MutableZonedDateTimeDoubleTimeSeries toMutableZonedDateTimeDoubleTimeSeries(javax.time.calendar.TimeZone timeZone) {
      return new ListZonedDateTimeDoubleTimeSeries(timeZone, getFastSeries().toFastMutableLongDoubleTimeSeries());
    }

    @Override
    public ZonedDateTimeDoubleTimeSeries toZonedDateTimeDoubleTimeSeries() {
      return new ArrayZonedDateTimeDoubleTimeSeries(((YearOffsetEpochMillisConverter)getConverter()).getTimeZone310(), getFastSeries());
    }

    @Override
    public ZonedDateTimeDoubleTimeSeries toZonedDateTimeDoubleTimeSeries(javax.time.calendar.TimeZone timeZone) {
      return new ArrayZonedDateTimeDoubleTimeSeries(timeZone, getFastSeries());
    }
  }
}
