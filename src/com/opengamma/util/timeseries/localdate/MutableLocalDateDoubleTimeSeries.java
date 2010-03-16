/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.util.timeseries.localdate;

import java.util.Date;
import java.util.TimeZone;

import javax.time.calendar.LocalDate;
import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.ArrayUtils;

import com.opengamma.util.timeseries.AbstractMutableIntDoubleTimeSeries;
import com.opengamma.util.timeseries.AbstractMutableLongDoubleTimeSeries;
import com.opengamma.util.timeseries.DateTimeConverter;
import com.opengamma.util.timeseries.MutableDoubleTimeSeries;
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
import com.opengamma.util.timeseries.fast.integer.FastMutableIntDoubleTimeSeries;
import com.opengamma.util.timeseries.fast.longint.FastMutableLongDoubleTimeSeries;
import com.opengamma.util.timeseries.sqldate.ArraySQLDateDoubleTimeSeries;
import com.opengamma.util.timeseries.sqldate.ListSQLDateDoubleTimeSeries;
import com.opengamma.util.timeseries.sqldate.MutableSQLDateDoubleTimeSeries;
import com.opengamma.util.timeseries.sqldate.SQLDateDoubleTimeSeries;
import com.opengamma.util.timeseries.yearoffset.ArrayYearOffsetDoubleTimeSeries;
import com.opengamma.util.timeseries.yearoffset.ListYearOffsetDoubleTimeSeries;
import com.opengamma.util.timeseries.yearoffset.MutableYearOffsetDoubleTimeSeries;
import com.opengamma.util.timeseries.yearoffset.YearOffsetDoubleTimeSeries;
import com.opengamma.util.timeseries.zoneddatetime.ArrayZonedDateTimeDoubleTimeSeries;
import com.opengamma.util.timeseries.zoneddatetime.ListZonedDateTimeDoubleTimeSeries;
import com.opengamma.util.timeseries.zoneddatetime.MutableZonedDateTimeDoubleTimeSeries;
import com.opengamma.util.timeseries.zoneddatetime.ZonedDateTimeDoubleTimeSeries;

/**
 * @author jim
 * 
 */
public interface MutableLocalDateDoubleTimeSeries extends LocalDateDoubleTimeSeries, MutableDoubleTimeSeries<LocalDate> {

  public static abstract class Integer extends AbstractMutableIntDoubleTimeSeries<LocalDate> implements MutableLocalDateDoubleTimeSeries {
    public Integer(final DateTimeConverter<LocalDate> converter, final FastMutableIntDoubleTimeSeries timeSeries) {
      super(converter, timeSeries);
    }

    @Override
    public LocalDateDoubleTimeSeries toLocalDateDoubleTimeSeries() {
      return new ArrayLocalDateDoubleTimeSeries(getFastSeries());
    }

    @Override
    public LocalDateDoubleTimeSeries toLocalDateDoubleTimeSeries(javax.time.calendar.TimeZone timeZone) {
      return new ArrayLocalDateDoubleTimeSeries(timeZone, toFastIntDoubleTimeSeries());
    }    
    
    @Override
    public MutableLocalDateDoubleTimeSeries toMutableLocalDateDoubleTimeSeries() {
      return new ListLocalDateDoubleTimeSeries(this);
    }

    @Override
    public MutableLocalDateDoubleTimeSeries toMutableLocalDateDoubleTimeSeries(javax.time.calendar.TimeZone timeZone) {
      return new ListLocalDateDoubleTimeSeries(timeZone, this);
    }
    
    @Override
    public TimeSeries<LocalDate, Double> newInstance(final LocalDate[] dateTimes, final Double[] values) {
      return newInstanceFast(dateTimes, ArrayUtils.toPrimitive(values));
    }

    public abstract LocalDateDoubleTimeSeries newInstanceFast(LocalDate[] dateTimes, double[] values);
    
    @Override
    public DateDoubleTimeSeries toDateDoubleTimeSeries() {
      return new ArrayDateDoubleTimeSeries(((LocalDateEpochDaysConverter)getConverter()).getTimeZone(), toFastIntDoubleTimeSeries());
    }

    @Override
    public DateDoubleTimeSeries toDateDoubleTimeSeries(TimeZone timeZone) {
      return new ArrayDateDoubleTimeSeries(timeZone, toFastIntDoubleTimeSeries());
    }

    
    
    @Override
    public DateTimeDoubleTimeSeries toDateTimeDoubleTimeSeries() {
      return new ArrayDateTimeDoubleTimeSeries(((LocalDateEpochDaysConverter)getConverter()).getTimeZone(), toFastLongDoubleTimeSeries());
    }

    @Override
    public DateTimeDoubleTimeSeries toDateTimeDoubleTimeSeries(TimeZone timeZone) {
      return new ArrayDateTimeDoubleTimeSeries(((LocalDateEpochDaysConverter)getConverter()).getTimeZone(), toFastLongDoubleTimeSeries());
    }

    @Override
    public MutableDateDoubleTimeSeries toMutableDateDoubleTimeSeries() {
      return new ListDateDoubleTimeSeries(toFastMutableIntDoubleTimeSeries());
    }

    @Override
    public MutableDateDoubleTimeSeries toMutableDateDoubleTimeSeries(
        TimeZone timeZone) {
      return new ListDateDoubleTimeSeries(((LocalDateEpochDaysConverter)getConverter()).getTimeZone(), toFastMutableIntDoubleTimeSeries());
    }

    @Override
    public SQLDateDoubleTimeSeries toSQLDateDoubleTimeSeries() {
      return new ArraySQLDateDoubleTimeSeries(((LocalDateEpochDaysConverter)getConverter()).getTimeZone(), toFastIntDoubleTimeSeries());
    }

    @Override
    public SQLDateDoubleTimeSeries toSQLDateDoubleTimeSeries(TimeZone timeZone) {
      return new ArraySQLDateDoubleTimeSeries(((LocalDateEpochDaysConverter)getConverter()).getTimeZone(), toFastIntDoubleTimeSeries());
    }

    @Override
    public MutableSQLDateDoubleTimeSeries toMutableSQLDateDoubleTimeSeries() {
      return new ListSQLDateDoubleTimeSeries(((LocalDateEpochDaysConverter)getConverter()).getTimeZone(), toFastMutableIntDoubleTimeSeries());
    }

    @Override
    public MutableSQLDateDoubleTimeSeries toMutableSQLDateDoubleTimeSeries(TimeZone timeZone) {
      return new ListSQLDateDoubleTimeSeries(timeZone, toFastMutableIntDoubleTimeSeries());
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
      return new ArrayZonedDateTimeDoubleTimeSeries(((LocalDateEpochDaysConverter)getConverter()).getTimeZone310(), getFastSeries().toFastLongDoubleTimeSeries(DateTimeNumericEncoding.TIME_EPOCH_MILLIS));
    }
    
    @Override
    public ZonedDateTimeDoubleTimeSeries toZonedDateTimeDoubleTimeSeries(javax.time.calendar.TimeZone timeZone) {
      return new ArrayZonedDateTimeDoubleTimeSeries(timeZone, getFastSeries().toFastLongDoubleTimeSeries(DateTimeNumericEncoding.TIME_EPOCH_MILLIS));
    }

    @Override
    public MutableZonedDateTimeDoubleTimeSeries toMutableZonedDateTimeDoubleTimeSeries() {
      return new ListZonedDateTimeDoubleTimeSeries(((LocalDateEpochDaysConverter)getConverter()).getTimeZone310(), getFastSeries().toFastMutableLongDoubleTimeSeries(DateTimeNumericEncoding.TIME_EPOCH_MILLIS));
    }
    
    @Override
    public MutableZonedDateTimeDoubleTimeSeries toMutableZonedDateTimeDoubleTimeSeries(javax.time.calendar.TimeZone timeZone) {
      return new ListZonedDateTimeDoubleTimeSeries(timeZone, getFastSeries().toFastMutableLongDoubleTimeSeries(DateTimeNumericEncoding.TIME_EPOCH_MILLIS));
    }
    
    @Override
    public YearOffsetDoubleTimeSeries toYearOffsetDoubleTimeSeries(ZonedDateTime zeroDate) {
      return new ArrayYearOffsetDoubleTimeSeries(zeroDate, getFastSeries().toFastLongDoubleTimeSeries(DateTimeNumericEncoding.TIME_EPOCH_MILLIS));
    }

    @Override
    public YearOffsetDoubleTimeSeries toYearOffsetDoubleTimeSeries(java.util.TimeZone timeZone, Date zeroDate) {
      return new ArrayYearOffsetDoubleTimeSeries(timeZone, zeroDate, getFastSeries().toFastLongDoubleTimeSeries(DateTimeNumericEncoding.TIME_EPOCH_MILLIS));
    }

    @Override
    public MutableYearOffsetDoubleTimeSeries toMutableYearOffsetDoubleTimeSeries(ZonedDateTime zeroDate) {
      return new ListYearOffsetDoubleTimeSeries(zeroDate, getFastSeries().toFastMutableLongDoubleTimeSeries(DateTimeNumericEncoding.TIME_EPOCH_MILLIS));
    }
    
    @Override
    public MutableYearOffsetDoubleTimeSeries toMutableYearOffsetDoubleTimeSeries(java.util.TimeZone timeZone, Date zeroDate) {
      return new ListYearOffsetDoubleTimeSeries(timeZone, zeroDate, getFastSeries().toFastMutableLongDoubleTimeSeries(DateTimeNumericEncoding.TIME_EPOCH_MILLIS));
    }
  }

  public abstract static class Long extends AbstractMutableLongDoubleTimeSeries<LocalDate> implements MutableLocalDateDoubleTimeSeries {
    public Long(final DateTimeConverter<LocalDate> converter, final FastMutableLongDoubleTimeSeries timeSeries) {
      super(converter, timeSeries);
    }

    @Override
    public LocalDateDoubleTimeSeries toLocalDateDoubleTimeSeries() {
      return new ArrayLocalDateDoubleTimeSeries(((LocalDateEpochDaysConverter)getConverter()).getTimeZone310(), getFastSeries().toFastIntDoubleTimeSeries(DateTimeNumericEncoding.DATE_EPOCH_DAYS));
    }

    @Override
    public LocalDateDoubleTimeSeries toLocalDateDoubleTimeSeries(javax.time.calendar.TimeZone timeZone) {
      return new ArrayLocalDateDoubleTimeSeries(timeZone, toFastIntDoubleTimeSeries(DateTimeNumericEncoding.DATE_EPOCH_DAYS));
    }    
    
    @Override
    public MutableLocalDateDoubleTimeSeries toMutableLocalDateDoubleTimeSeries() {
      return new ListLocalDateDoubleTimeSeries(((LocalDateEpochDaysConverter)getConverter()).getTimeZone310(), getFastSeries().toFastMutableIntDoubleTimeSeries(DateTimeNumericEncoding.DATE_EPOCH_DAYS));
    }

    @Override
    public MutableLocalDateDoubleTimeSeries toMutableLocalDateDoubleTimeSeries(javax.time.calendar.TimeZone timeZone) {
      return new ListLocalDateDoubleTimeSeries(timeZone, getFastSeries().toFastMutableIntDoubleTimeSeries(DateTimeNumericEncoding.DATE_EPOCH_DAYS));
    }

    @Override
    public TimeSeries<LocalDate, Double> newInstance(final LocalDate[] dateTimes, final Double[] values) {
      return newInstanceFast(dateTimes, ArrayUtils.toPrimitive(values));
    }

    public abstract LocalDateDoubleTimeSeries newInstanceFast(LocalDate[] dateTimes, double[] values);
    
    @Override
    public DateDoubleTimeSeries toDateDoubleTimeSeries() {
      return new ArrayDateDoubleTimeSeries(((LocalDateEpochDaysConverter)getConverter()).getTimeZone(), toFastIntDoubleTimeSeries());
    }

    @Override
    public DateDoubleTimeSeries toDateDoubleTimeSeries(TimeZone timeZone) {
      return new ArrayDateDoubleTimeSeries(timeZone, toFastIntDoubleTimeSeries());
    }

    @Override
    public DateTimeDoubleTimeSeries toDateTimeDoubleTimeSeries() {
      return new ArrayDateTimeDoubleTimeSeries(((LocalDateEpochDaysConverter)getConverter()).getTimeZone(), toFastLongDoubleTimeSeries());
    }

    @Override
    public DateTimeDoubleTimeSeries toDateTimeDoubleTimeSeries(TimeZone timeZone) {
      return new ArrayDateTimeDoubleTimeSeries(((LocalDateEpochDaysConverter)getConverter()).getTimeZone(), toFastLongDoubleTimeSeries());
    }

    @Override
    public MutableDateDoubleTimeSeries toMutableDateDoubleTimeSeries() {
      return new ListDateDoubleTimeSeries(((LocalDateEpochDaysConverter)getConverter()).getTimeZone(), toFastMutableIntDoubleTimeSeries());
    }

    @Override
    public MutableDateDoubleTimeSeries toMutableDateDoubleTimeSeries(
        TimeZone timeZone) {
      return new ListDateDoubleTimeSeries(timeZone, toFastMutableIntDoubleTimeSeries());
    }
    
    @Override
    public SQLDateDoubleTimeSeries toSQLDateDoubleTimeSeries() {
      return new ArraySQLDateDoubleTimeSeries(((LocalDateEpochDaysConverter)getConverter()).getTimeZone(), toFastIntDoubleTimeSeries());
    }

    @Override
    public SQLDateDoubleTimeSeries toSQLDateDoubleTimeSeries(TimeZone timeZone) {
      return new ArraySQLDateDoubleTimeSeries(timeZone, toFastIntDoubleTimeSeries());
    }

    @Override
    public MutableSQLDateDoubleTimeSeries toMutableSQLDateDoubleTimeSeries() {
      return new ListSQLDateDoubleTimeSeries(((LocalDateEpochDaysConverter)getConverter()).getTimeZone(), toFastMutableIntDoubleTimeSeries());
    }

    @Override
    public MutableSQLDateDoubleTimeSeries toMutableSQLDateDoubleTimeSeries(TimeZone timeZone) {
      return new ListSQLDateDoubleTimeSeries(timeZone, toFastMutableIntDoubleTimeSeries());
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
      return new ArrayZonedDateTimeDoubleTimeSeries(((LocalDateEpochDaysConverter)getConverter()).getTimeZone310(), getFastSeries().toFastLongDoubleTimeSeries(DateTimeNumericEncoding.TIME_EPOCH_MILLIS));
    }
    
    @Override
    public ZonedDateTimeDoubleTimeSeries toZonedDateTimeDoubleTimeSeries(javax.time.calendar.TimeZone timeZone) {
      return new ArrayZonedDateTimeDoubleTimeSeries(timeZone, getFastSeries().toFastLongDoubleTimeSeries(DateTimeNumericEncoding.TIME_EPOCH_MILLIS));
    }

    @Override
    public MutableZonedDateTimeDoubleTimeSeries toMutableZonedDateTimeDoubleTimeSeries() {
      return new ListZonedDateTimeDoubleTimeSeries(((LocalDateEpochDaysConverter)getConverter()).getTimeZone310(), getFastSeries().toFastMutableLongDoubleTimeSeries(DateTimeNumericEncoding.TIME_EPOCH_MILLIS));
    }
    
    @Override
    public MutableZonedDateTimeDoubleTimeSeries toMutableZonedDateTimeDoubleTimeSeries(javax.time.calendar.TimeZone timeZone) {
      return new ListZonedDateTimeDoubleTimeSeries(timeZone, getFastSeries().toFastMutableLongDoubleTimeSeries(DateTimeNumericEncoding.TIME_EPOCH_MILLIS));
    }
    
    @Override
    public YearOffsetDoubleTimeSeries toYearOffsetDoubleTimeSeries(ZonedDateTime zeroDate) {
      return new ArrayYearOffsetDoubleTimeSeries(zeroDate, getFastSeries().toFastLongDoubleTimeSeries(DateTimeNumericEncoding.TIME_EPOCH_MILLIS));
    }

    @Override
    public YearOffsetDoubleTimeSeries toYearOffsetDoubleTimeSeries(java.util.TimeZone timeZone, Date zeroDate) {
      return new ArrayYearOffsetDoubleTimeSeries(timeZone, zeroDate, getFastSeries().toFastLongDoubleTimeSeries(DateTimeNumericEncoding.TIME_EPOCH_MILLIS));
    }

    @Override
    public MutableYearOffsetDoubleTimeSeries toMutableYearOffsetDoubleTimeSeries(ZonedDateTime zeroDate) {
      return new ListYearOffsetDoubleTimeSeries(zeroDate, getFastSeries().toFastMutableLongDoubleTimeSeries(DateTimeNumericEncoding.TIME_EPOCH_MILLIS));
    }
    
    @Override
    public MutableYearOffsetDoubleTimeSeries toMutableYearOffsetDoubleTimeSeries(java.util.TimeZone timeZone, Date zeroDate) {
      return new ListYearOffsetDoubleTimeSeries(timeZone, zeroDate, getFastSeries().toFastMutableLongDoubleTimeSeries(DateTimeNumericEncoding.TIME_EPOCH_MILLIS));
    }
    
    
  }
}
