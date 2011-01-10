/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.util.timeseries.yearoffset;

import java.util.Date;
import java.util.List;

import javax.time.calendar.ZonedDateTime;

import com.opengamma.util.timeseries.fast.DateTimeNumericEncoding;
import com.opengamma.util.timeseries.fast.longint.object.FastListLongObjectTimeSeries;
import com.opengamma.util.timeseries.fast.longint.object.FastMutableLongObjectTimeSeries;

/**
 * @author jim
 * 
 */
public class ListYearOffsetObjectTimeSeries<T> extends MutableYearOffsetObjectTimeSeries.Long<T> {

  private ListYearOffsetObjectTimeSeries(final ZonedDateTime zeroDate) {
    super(new YearOffsetEpochMillisConverter(zeroDate), new FastListLongObjectTimeSeries<T>(DateTimeNumericEncoding.TIME_EPOCH_MILLIS));
  }

  public ListYearOffsetObjectTimeSeries(final ZonedDateTime zeroDate, final Double[] dates, final T[] values) {
    super(new YearOffsetEpochMillisConverter(zeroDate),
        new FastListLongObjectTimeSeries<T>(DateTimeNumericEncoding.TIME_EPOCH_MILLIS, new YearOffsetEpochMillisConverter(zeroDate).convertToLong(dates), values));
  }

  public ListYearOffsetObjectTimeSeries(final java.util.TimeZone timeZone, final Date zeroDate, final Double[] dates, final T[] values) {
    super(new YearOffsetEpochMillisConverter(timeZone, zeroDate), new FastListLongObjectTimeSeries<T>(DateTimeNumericEncoding.TIME_EPOCH_MILLIS, new YearOffsetEpochMillisConverter(timeZone, zeroDate)
        .convertToLong(dates), values));
  }

  public ListYearOffsetObjectTimeSeries(final ZonedDateTime zeroDate, final List<Double> dates, final List<T> values) {
    super(new YearOffsetEpochMillisConverter(zeroDate),
        new FastListLongObjectTimeSeries<T>(DateTimeNumericEncoding.TIME_EPOCH_MILLIS, new YearOffsetEpochMillisConverter(zeroDate).convertToLong(dates), values));
  }

  public ListYearOffsetObjectTimeSeries(final java.util.TimeZone timeZone, final Date zeroDate, final List<Double> dates, final List<T> values) {
    super(new YearOffsetEpochMillisConverter(timeZone, zeroDate), new FastListLongObjectTimeSeries<T>(DateTimeNumericEncoding.TIME_EPOCH_MILLIS, new YearOffsetEpochMillisConverter(timeZone, zeroDate)
        .convertToLong(dates), values));
  }

  // REVIEW jim 8-Mar-2010 -- we could probably just resuse the converter from dts - should be immutable...
  @SuppressWarnings("unchecked")
  public ListYearOffsetObjectTimeSeries(final YearOffsetObjectTimeSeries<T> dts) {
    super(new YearOffsetEpochMillisConverter(((YearOffsetEpochMillisConverter) dts.getConverter()).getZonedOffset()), 
        (FastMutableLongObjectTimeSeries<T>) new YearOffsetEpochMillisConverter(((YearOffsetEpochMillisConverter) dts.getConverter()).getZonedOffset()).convertToLong(new FastListLongObjectTimeSeries(DateTimeNumericEncoding.TIME_EPOCH_MILLIS), dts));
  }
  
  @SuppressWarnings("unchecked")
  public ListYearOffsetObjectTimeSeries(final ZonedDateTime zeroDate, final YearOffsetObjectTimeSeries<T> dts) {
    super(new YearOffsetEpochMillisConverter(zeroDate), (FastMutableLongObjectTimeSeries<T>) new YearOffsetEpochMillisConverter(zeroDate).convertToLong(new FastListLongObjectTimeSeries(
        DateTimeNumericEncoding.TIME_EPOCH_MILLIS), dts));
  }

  @SuppressWarnings("unchecked")
  public ListYearOffsetObjectTimeSeries(final java.util.TimeZone timeZone, final Date zeroDate, final YearOffsetObjectTimeSeries dts) {
    super(new YearOffsetEpochMillisConverter(timeZone, zeroDate), (FastMutableLongObjectTimeSeries<T>) new YearOffsetEpochMillisConverter(timeZone, zeroDate).convertToLong(new FastListLongObjectTimeSeries(
        DateTimeNumericEncoding.TIME_EPOCH_MILLIS), dts));
  }

  public ListYearOffsetObjectTimeSeries(final ZonedDateTime zeroDate, final FastMutableLongObjectTimeSeries<T> pmidts) {
    super(new YearOffsetEpochMillisConverter(zeroDate), pmidts);
  }

  public ListYearOffsetObjectTimeSeries(final java.util.TimeZone timeZone, final Date zeroDate, final FastMutableLongObjectTimeSeries<T> pmidts) {
    super(new YearOffsetEpochMillisConverter(timeZone, zeroDate), pmidts);
  }

  @Override
  public YearOffsetObjectTimeSeries<T> newInstanceFast(final Double[] dateTimes, final T[] values) {
    return new ListYearOffsetObjectTimeSeries<T>(((YearOffsetEpochMillisConverter) getConverter()).getZonedOffset(), dateTimes, values);
  }
}
