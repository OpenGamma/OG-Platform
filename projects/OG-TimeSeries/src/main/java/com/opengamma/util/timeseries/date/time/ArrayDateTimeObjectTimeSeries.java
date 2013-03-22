/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.util.timeseries.date.time;

import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import com.opengamma.util.timeseries.ObjectTimeSeries;
import com.opengamma.util.timeseries.fast.DateTimeNumericEncoding;
import com.opengamma.util.timeseries.fast.longint.object.FastArrayLongObjectTimeSeries;
import com.opengamma.util.timeseries.fast.longint.object.FastListLongObjectTimeSeries;
import com.opengamma.util.timeseries.fast.longint.object.FastLongObjectTimeSeries;

/**
 * 
 * @param <T> The type of the data
 */
public class ArrayDateTimeObjectTimeSeries<T> extends DateTimeObjectTimeSeries.Long<T> {
  private static final FastListLongObjectTimeSeries<?> DEFAULT_SERIES_TEMPLATE = new FastListLongObjectTimeSeries<Object>(DateTimeNumericEncoding.TIME_EPOCH_MILLIS);
  /** An empty date-time series with data of type Object */
  public static final ArrayDateTimeObjectTimeSeries<?> EMPTY_SERIES = new ArrayDateTimeObjectTimeSeries<Object>();
  private static final FastArrayLongObjectTimeSeries<?> EMPTY_FAST_SERIES = new FastArrayLongObjectTimeSeries<Object>(DateTimeNumericEncoding.TIME_EPOCH_MILLIS);
  private static final DateEpochMillisConverter s_converter = new DateEpochMillisConverter();

  @SuppressWarnings("unchecked")
  public ArrayDateTimeObjectTimeSeries() { 
    super(new DateEpochMillisConverter(), (FastLongObjectTimeSeries<T>) EMPTY_FAST_SERIES);
  }

  @SuppressWarnings({"unchecked", "rawtypes" })
  public ArrayDateTimeObjectTimeSeries(final Date[] dates, final T[] values) {
    super(s_converter, new FastArrayLongObjectTimeSeries(DateTimeNumericEncoding.TIME_EPOCH_MILLIS, s_converter.convertToLong(dates), values));
  }

  public ArrayDateTimeObjectTimeSeries(final TimeZone timeZone, final Date[] dates, final T[] values) {
    super(new DateEpochMillisConverter(timeZone), new FastArrayLongObjectTimeSeries<T>(DateTimeNumericEncoding.TIME_EPOCH_MILLIS, new DateEpochMillisConverter(timeZone)
        .convertToLong(dates), values));
  }

  public ArrayDateTimeObjectTimeSeries(final List<Date> dates, final List<T> values) {
    super(s_converter, new FastArrayLongObjectTimeSeries<T>(DateTimeNumericEncoding.TIME_EPOCH_MILLIS, s_converter.convertToLong(dates), values));
  }

  public ArrayDateTimeObjectTimeSeries(final TimeZone timeZone, final List<Date> dates, final List<T> values) {
    super(new DateEpochMillisConverter(timeZone), new FastArrayLongObjectTimeSeries<T>(DateTimeNumericEncoding.TIME_EPOCH_MILLIS, new DateEpochMillisConverter(timeZone)
        .convertToLong(dates), values));
  }

  @SuppressWarnings("unchecked")
  public ArrayDateTimeObjectTimeSeries(final ObjectTimeSeries<Date, T> dts) {
    super(s_converter, s_converter.convertToLong((FastLongObjectTimeSeries<T>) DEFAULT_SERIES_TEMPLATE, dts));
  }

  @SuppressWarnings("unchecked")
  public ArrayDateTimeObjectTimeSeries(final TimeZone timeZone, final ObjectTimeSeries<Date, T> dts) {
    super(new DateEpochMillisConverter(timeZone), new DateEpochMillisConverter(timeZone).convertToLong((FastLongObjectTimeSeries<T>) DEFAULT_SERIES_TEMPLATE, dts));
  }

  public ArrayDateTimeObjectTimeSeries(final FastLongObjectTimeSeries<T> pidts) {
    super(s_converter, pidts);
  }

  public ArrayDateTimeObjectTimeSeries(final TimeZone timeZone, final FastLongObjectTimeSeries<T> pidts) {
    super(new DateEpochMillisConverter(timeZone), pidts);
  }

  @Override
  public DateTimeObjectTimeSeries<T> newInstanceFast(final Date[] dateTimes, final T[] values) {
    return new ArrayDateTimeObjectTimeSeries<T>(((DateEpochMillisConverter) getConverter()).getTimeZone(), dateTimes, values);
  }


}
