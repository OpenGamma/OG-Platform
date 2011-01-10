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
import com.opengamma.util.timeseries.fast.longint.object.FastArrayLongObjectTimeSeries;
import com.opengamma.util.timeseries.fast.longint.object.FastListLongObjectTimeSeries;
import com.opengamma.util.timeseries.fast.longint.object.FastLongObjectTimeSeries;

/**
 * @author jim
 * 
 */
public class ArrayYearOffsetObjectTimeSeries<T> extends YearOffsetObjectTimeSeries.Long<T> {
  private static final FastListLongObjectTimeSeries<?> DEFAULT_SERIES_TEMPLATE = new FastListLongObjectTimeSeries<Object>(DateTimeNumericEncoding.TIME_EPOCH_MILLIS);

  private ArrayYearOffsetObjectTimeSeries(final Date zeroDate) {
    super(new YearOffsetEpochMillisConverter(zeroDate), new FastArrayLongObjectTimeSeries<T>(DateTimeNumericEncoding.TIME_EPOCH_MILLIS));
  }

  public ArrayYearOffsetObjectTimeSeries(final ZonedDateTime zeroDate, final Double[] dates, final T[] values) {
    super(new YearOffsetEpochMillisConverter(zeroDate),
        new FastArrayLongObjectTimeSeries<T>(DateTimeNumericEncoding.TIME_EPOCH_MILLIS, new YearOffsetEpochMillisConverter(zeroDate).convertToLong(dates), values));
  }

  public ArrayYearOffsetObjectTimeSeries(final java.util.TimeZone timeZone, final Date zeroDate, final Double[] dates, final T[] values) {
    super(new YearOffsetEpochMillisConverter(timeZone, zeroDate), new FastArrayLongObjectTimeSeries<T>(DateTimeNumericEncoding.TIME_EPOCH_MILLIS, new YearOffsetEpochMillisConverter(timeZone, zeroDate)
        .convertToLong(dates), values));
  }

  public ArrayYearOffsetObjectTimeSeries(final ZonedDateTime zeroDate, final List<Double> dates, final List<T> values) {
    super(new YearOffsetEpochMillisConverter(zeroDate),
         new FastArrayLongObjectTimeSeries<T>(DateTimeNumericEncoding.TIME_EPOCH_MILLIS, new YearOffsetEpochMillisConverter(zeroDate).convertToLong(dates), values));
  }

  public ArrayYearOffsetObjectTimeSeries(final java.util.TimeZone timeZone, final Date zeroDate, final List<Double> dates, final List<T> values) {
    super(new YearOffsetEpochMillisConverter(timeZone, zeroDate), new FastArrayLongObjectTimeSeries<T>(DateTimeNumericEncoding.TIME_EPOCH_MILLIS, new YearOffsetEpochMillisConverter(timeZone, zeroDate)
        .convertToLong(dates), values));
  }

  @SuppressWarnings("unchecked")
  public ArrayYearOffsetObjectTimeSeries(final ZonedDateTime zeroDate, final YearOffsetObjectTimeSeries<T> dts) {
    super(new YearOffsetEpochMillisConverter(zeroDate),
        new YearOffsetEpochMillisConverter(zeroDate).convertToLong((FastListLongObjectTimeSeries<T>) DEFAULT_SERIES_TEMPLATE, dts));
  }

  @SuppressWarnings("unchecked")
  public ArrayYearOffsetObjectTimeSeries(final java.util.TimeZone timeZone, final Date zeroDate, final YearOffsetObjectTimeSeries<T> dts) {
    super(new YearOffsetEpochMillisConverter(timeZone, zeroDate), new YearOffsetEpochMillisConverter(timeZone, zeroDate).convertToLong((FastListLongObjectTimeSeries<T>) DEFAULT_SERIES_TEMPLATE, dts));
  }

  public ArrayYearOffsetObjectTimeSeries(final ZonedDateTime zeroDate, final FastLongObjectTimeSeries<T> pidts) {
    super(new YearOffsetEpochMillisConverter(zeroDate), pidts);
  }

  public ArrayYearOffsetObjectTimeSeries(final java.util.TimeZone timeZone, final Date zeroDate, final FastLongObjectTimeSeries<T> pidts) {
    super(new YearOffsetEpochMillisConverter(timeZone, zeroDate), pidts);
  }

  @Override
  public YearOffsetObjectTimeSeries<T> newInstanceFast(final Double[] dateTimes, final T[] values) {
    return new ArrayYearOffsetObjectTimeSeries<T>(((YearOffsetEpochMillisConverter) getConverter()).getZonedOffset(), dateTimes, values);
  }


}
