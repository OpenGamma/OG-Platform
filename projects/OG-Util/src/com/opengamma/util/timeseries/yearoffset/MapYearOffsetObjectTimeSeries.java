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
import com.opengamma.util.timeseries.fast.longint.object.FastMapLongObjectTimeSeries;
import com.opengamma.util.timeseries.fast.longint.object.FastMutableLongObjectTimeSeries;

/**
 * @author jim
 * 
 */
public class MapYearOffsetObjectTimeSeries<T> extends MutableYearOffsetObjectTimeSeries.Long<T> {

  public MapYearOffsetObjectTimeSeries(final ZonedDateTime zeroDate) {
    super(new YearOffsetEpochMillisConverter(zeroDate), new FastMapLongObjectTimeSeries<T>(DateTimeNumericEncoding.TIME_EPOCH_MILLIS));
  }
  
  public MapYearOffsetObjectTimeSeries(final java.util.TimeZone timeZone, final Date zeroDate) {
    super(new YearOffsetEpochMillisConverter(timeZone, zeroDate), new FastMapLongObjectTimeSeries<T>(DateTimeNumericEncoding.TIME_EPOCH_MILLIS));
  }
  
  public MapYearOffsetObjectTimeSeries(final ZonedDateTime zeroDate, final Double[] dates, final T[] values) {
    super(new YearOffsetEpochMillisConverter(zeroDate), new FastMapLongObjectTimeSeries<T>(DateTimeNumericEncoding.TIME_EPOCH_MILLIS, new YearOffsetEpochMillisConverter(zeroDate).convertToLong(dates), values));
  }

  public MapYearOffsetObjectTimeSeries(final java.util.TimeZone timeZone, Date zeroDate, final Double[] dates, final T[] values) {
    super(new YearOffsetEpochMillisConverter(timeZone, zeroDate), new FastMapLongObjectTimeSeries<T>(DateTimeNumericEncoding.TIME_EPOCH_MILLIS, new YearOffsetEpochMillisConverter(timeZone, zeroDate)
        .convertToLong(dates), values));
  }

  public MapYearOffsetObjectTimeSeries(final ZonedDateTime zeroDate, final List<Double> dates, final List<T> values) {
    super(new YearOffsetEpochMillisConverter(zeroDate), new FastMapLongObjectTimeSeries<T>(DateTimeNumericEncoding.TIME_EPOCH_MILLIS, new YearOffsetEpochMillisConverter(zeroDate).convertToLong(dates), values));
  }

  public MapYearOffsetObjectTimeSeries(final java.util.TimeZone timeZone, final Date zeroDate, final List<Double> dates, final List<T> values) {
    super(new YearOffsetEpochMillisConverter(timeZone, zeroDate), new FastMapLongObjectTimeSeries<T>(DateTimeNumericEncoding.TIME_EPOCH_MILLIS, new YearOffsetEpochMillisConverter(timeZone, zeroDate)
        .convertToLong(dates), values));
  }

  // convenience method.
  public MapYearOffsetObjectTimeSeries(final YearOffsetObjectTimeSeries<T> dts) {
    super(new YearOffsetEpochMillisConverter(((YearOffsetEpochMillisConverter) dts.getConverter()).getZonedOffset()), (FastMutableLongObjectTimeSeries<T>) new YearOffsetEpochMillisConverter(((YearOffsetEpochMillisConverter) dts.getConverter()).getZonedOffset()).convertToLong(new FastMapLongObjectTimeSeries<T>(DateTimeNumericEncoding.TIME_EPOCH_MILLIS), dts));
  }

  @SuppressWarnings("unchecked")
  public MapYearOffsetObjectTimeSeries(final ZonedDateTime zeroDate, final YearOffsetObjectTimeSeries<T> dts) {
    super(new YearOffsetEpochMillisConverter(zeroDate), (FastMutableLongObjectTimeSeries<T>) new YearOffsetEpochMillisConverter(zeroDate).convertToLong(new FastMapLongObjectTimeSeries(DateTimeNumericEncoding.TIME_EPOCH_MILLIS), dts));
  }
  
  @SuppressWarnings("unchecked")
  public MapYearOffsetObjectTimeSeries(final java.util.TimeZone timeZone, final Date zeroDate, final YearOffsetObjectTimeSeries<T> dts) {
    super(new YearOffsetEpochMillisConverter(timeZone, zeroDate), (FastMutableLongObjectTimeSeries<T>) new YearOffsetEpochMillisConverter(timeZone, zeroDate).convertToLong(new FastMapLongObjectTimeSeries(
        DateTimeNumericEncoding.TIME_EPOCH_MILLIS), dts));
  }

  public MapYearOffsetObjectTimeSeries(final ZonedDateTime zeroDate, final FastMutableLongObjectTimeSeries<T> pmidts) {
    super(new YearOffsetEpochMillisConverter(zeroDate), pmidts);
  }

  public MapYearOffsetObjectTimeSeries(final java.util.TimeZone timeZone, final Date zeroDate, final FastMutableLongObjectTimeSeries<T> pmidts) {
    super(new YearOffsetEpochMillisConverter(timeZone, zeroDate), pmidts);
  }

  @Override
  public YearOffsetObjectTimeSeries<T> newInstanceFast(final Double[] dateTimes, final T[] values) {
    return new MapYearOffsetObjectTimeSeries<T>(((YearOffsetEpochMillisConverter) getConverter()).getZonedOffset(), dateTimes, values);
  }

}
