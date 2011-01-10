/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.util.timeseries.zoneddatetime;

import java.util.List;

import javax.time.calendar.TimeZone;
import javax.time.calendar.ZonedDateTime;

import com.opengamma.util.timeseries.ObjectTimeSeries;
import com.opengamma.util.timeseries.fast.DateTimeNumericEncoding;
import com.opengamma.util.timeseries.fast.longint.object.FastArrayLongObjectTimeSeries;
import com.opengamma.util.timeseries.fast.longint.object.FastListLongObjectTimeSeries;
import com.opengamma.util.timeseries.fast.longint.object.FastLongObjectTimeSeries;

/**
 * 
 */
public class ArrayZonedDateTimeObjectTimeSeries<T> extends ZonedDateTimeObjectTimeSeries.Long<T> {
  private static final FastListLongObjectTimeSeries<?> DEFAULT_SERIES_TEMPLATE = new FastListLongObjectTimeSeries<Object>(DateTimeNumericEncoding.TIME_EPOCH_MILLIS);
  private static final ZonedDateTimeEpochMillisConverter s_converter = new ZonedDateTimeEpochMillisConverter();

  public ArrayZonedDateTimeObjectTimeSeries() {
    super(new ZonedDateTimeEpochMillisConverter(), new FastArrayLongObjectTimeSeries<T>(DateTimeNumericEncoding.TIME_EPOCH_MILLIS));
  }

  public ArrayZonedDateTimeObjectTimeSeries(final TimeZone timeZone) {
    super(new ZonedDateTimeEpochMillisConverter(timeZone), new FastArrayLongObjectTimeSeries<T>(DateTimeNumericEncoding.TIME_EPOCH_MILLIS));
  }
  
  public ArrayZonedDateTimeObjectTimeSeries(final ZonedDateTime[] dates, final T[] values) {
    super(s_converter, new FastArrayLongObjectTimeSeries<T>(DateTimeNumericEncoding.TIME_EPOCH_MILLIS, s_converter.convertToLong(dates), values));
  }

  public ArrayZonedDateTimeObjectTimeSeries(final TimeZone timeZone, final ZonedDateTime[] dates, final T[] values) {
    super(new ZonedDateTimeEpochMillisConverter(timeZone), new FastArrayLongObjectTimeSeries<T>(DateTimeNumericEncoding.TIME_EPOCH_MILLIS, new ZonedDateTimeEpochMillisConverter(timeZone)
        .convertToLong(dates), values));
  }

  public ArrayZonedDateTimeObjectTimeSeries(final List<ZonedDateTime> dates, final List<T> values) {
    super(s_converter, new FastArrayLongObjectTimeSeries<T>(DateTimeNumericEncoding.TIME_EPOCH_MILLIS, s_converter.convertToLong(dates), values));
  }

  public ArrayZonedDateTimeObjectTimeSeries(final TimeZone timeZone, final List<ZonedDateTime> dates, final List<T> values) {
    super(new ZonedDateTimeEpochMillisConverter(timeZone), new FastArrayLongObjectTimeSeries<T>(DateTimeNumericEncoding.TIME_EPOCH_MILLIS, new ZonedDateTimeEpochMillisConverter(timeZone)
        .convertToLong(dates), values));
  }
  @SuppressWarnings("unchecked")
  public ArrayZonedDateTimeObjectTimeSeries(final ZonedDateTimeObjectTimeSeries<T> dts) {
    super(s_converter, s_converter.convertToLong((FastListLongObjectTimeSeries<T>) DEFAULT_SERIES_TEMPLATE, dts));
  }
  @SuppressWarnings("unchecked")
  public ArrayZonedDateTimeObjectTimeSeries(final TimeZone timeZone, final ObjectTimeSeries<ZonedDateTime, T> dts) {
    super(new ZonedDateTimeEpochMillisConverter(timeZone), new ZonedDateTimeEpochMillisConverter(timeZone).convertToLong((FastListLongObjectTimeSeries<T>) DEFAULT_SERIES_TEMPLATE, dts));
  }

  public ArrayZonedDateTimeObjectTimeSeries(final FastLongObjectTimeSeries<T> pidts) {
    super(s_converter, pidts);
  }

  public ArrayZonedDateTimeObjectTimeSeries(final TimeZone timeZone, final FastLongObjectTimeSeries<T> pidts) {
    super(new ZonedDateTimeEpochMillisConverter(timeZone), pidts);
  }

  @Override
  public ZonedDateTimeObjectTimeSeries<T> newInstanceFast(final ZonedDateTime[] dateTimes, final T[] values) {
    return new ArrayZonedDateTimeObjectTimeSeries<T>(((ZonedDateTimeEpochMillisConverter) getConverter()).getTimeZone310(), dateTimes, values);
  }
}
