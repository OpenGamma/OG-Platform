/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.util.timeseries.date.time;

import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import com.opengamma.util.timeseries.DateTimeConverter;
import com.opengamma.util.timeseries.ObjectTimeSeries;
import com.opengamma.util.timeseries.fast.DateTimeNumericEncoding;
import com.opengamma.util.timeseries.fast.longint.object.FastMapLongObjectTimeSeries;
import com.opengamma.util.timeseries.fast.longint.object.FastMutableLongObjectTimeSeries;

/**
 * @author jim
 * 
 */
public class MapDateTimeObjectTimeSeries<T> extends MutableDateTimeObjectTimeSeries.Long<T> {
  public static final MapDateTimeObjectTimeSeries<?> EMPTY_SERIES = new MapDateTimeObjectTimeSeries<Object>();
  private static final DateTimeConverter<Date> s_converter = new DateEpochMillisConverter();

  public MapDateTimeObjectTimeSeries() {
    super(new DateEpochMillisConverter(), new FastMapLongObjectTimeSeries<T>(DateTimeNumericEncoding.TIME_EPOCH_MILLIS));
  }

  public MapDateTimeObjectTimeSeries(final Date[] dates, final T[] values) {
    super(s_converter, new FastMapLongObjectTimeSeries<T>(DateTimeNumericEncoding.TIME_EPOCH_MILLIS, s_converter.convertToLong(dates), values));
  }

  public MapDateTimeObjectTimeSeries(final TimeZone timeZone, final Date[] dates, final T[] values) {
    super(new DateEpochMillisConverter(timeZone), new FastMapLongObjectTimeSeries<T>(DateTimeNumericEncoding.TIME_EPOCH_MILLIS, new DateEpochMillisConverter(timeZone)
        .convertToLong(dates), values));
  }

  public MapDateTimeObjectTimeSeries(final List<Date> dates, final List<T> values) {
    super(s_converter, new FastMapLongObjectTimeSeries<T>(DateTimeNumericEncoding.TIME_EPOCH_MILLIS, s_converter.convertToLong(dates), values));
  }

  public MapDateTimeObjectTimeSeries(final TimeZone timeZone, final List<Date> dates, final List<T> values) {
    super(new DateEpochMillisConverter(timeZone), new FastMapLongObjectTimeSeries<T>(DateTimeNumericEncoding.TIME_EPOCH_MILLIS, new DateEpochMillisConverter(timeZone)
        .convertToLong(dates), values));
  }

  public MapDateTimeObjectTimeSeries(final ObjectTimeSeries<Date, T> dts) {
    super(s_converter, (FastMutableLongObjectTimeSeries<T>) s_converter.convertToLong(new FastMapLongObjectTimeSeries<T>(DateTimeNumericEncoding.TIME_EPOCH_MILLIS), dts));
  }

  @SuppressWarnings("unchecked")
  public MapDateTimeObjectTimeSeries(final TimeZone timeZone, final ObjectTimeSeries<Date, T> dts) {
    super(new DateEpochMillisConverter(timeZone), (FastMutableLongObjectTimeSeries<T>) new DateEpochMillisConverter(timeZone).convertToLong(new FastMapLongObjectTimeSeries(
        DateTimeNumericEncoding.TIME_EPOCH_MILLIS), dts));
  }

  public MapDateTimeObjectTimeSeries(final FastMutableLongObjectTimeSeries<T> pmidts) {
    super(s_converter, pmidts);
  }

  public MapDateTimeObjectTimeSeries(final TimeZone timeZone, final FastMutableLongObjectTimeSeries<T> pmidts) {
    super(new DateEpochMillisConverter(timeZone), pmidts);
  }

  @Override
  public DateTimeObjectTimeSeries<T> newInstanceFast(final Date[] dateTimes, final T[] values) {
    return new MapDateTimeObjectTimeSeries<T>(((DateEpochMillisConverter) getConverter()).getTimeZone(), dateTimes, values);
  }
}
