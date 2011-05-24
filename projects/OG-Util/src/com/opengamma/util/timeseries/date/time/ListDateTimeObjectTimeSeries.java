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
import com.opengamma.util.timeseries.fast.longint.object.FastListLongObjectTimeSeries;
import com.opengamma.util.timeseries.fast.longint.object.FastMutableLongObjectTimeSeries;

/**
 * 
 * @param <T> The type of the data
 */
public class ListDateTimeObjectTimeSeries<T> extends MutableDateTimeObjectTimeSeries.Long<T> {
  /** An empty date-time series with data type Object */
  public static final ListDateTimeObjectTimeSeries<?> EMPTY_SERIES = new ListDateTimeObjectTimeSeries<Object>();
  private static final DateTimeConverter<Date> s_converter = new DateEpochMillisConverter();

  public ListDateTimeObjectTimeSeries() {
    super(new DateEpochMillisConverter(), new FastListLongObjectTimeSeries<T>(DateTimeNumericEncoding.TIME_EPOCH_MILLIS));
  }

  public ListDateTimeObjectTimeSeries(final Date[] dates, final T[] values) {
    super(s_converter, new FastListLongObjectTimeSeries<T>(DateTimeNumericEncoding.TIME_EPOCH_MILLIS, s_converter.convertToLong(dates), values));
  }

  public ListDateTimeObjectTimeSeries(final TimeZone timeZone, final Date[] dates, final T[] values) {
    super(new DateEpochMillisConverter(timeZone), new FastListLongObjectTimeSeries<T>(DateTimeNumericEncoding.TIME_EPOCH_MILLIS, new DateEpochMillisConverter(timeZone)
        .convertToLong(dates), values));
  }

  public ListDateTimeObjectTimeSeries(final List<Date> dates, final List<T> values) {
    super(s_converter, new FastListLongObjectTimeSeries<T>(DateTimeNumericEncoding.TIME_EPOCH_MILLIS, s_converter.convertToLong(dates), values));
  }

  public ListDateTimeObjectTimeSeries(final TimeZone timeZone, final List<Date> dates, final List<T> values) {
    super(new DateEpochMillisConverter(timeZone), new FastListLongObjectTimeSeries<T>(DateTimeNumericEncoding.TIME_EPOCH_MILLIS, new DateEpochMillisConverter(timeZone)
        .convertToLong(dates), values));
  }

  public ListDateTimeObjectTimeSeries(final ObjectTimeSeries<Date, T> dts) {
    super(s_converter, (FastMutableLongObjectTimeSeries<T>) s_converter.convertToLong(new FastListLongObjectTimeSeries<T>(DateTimeNumericEncoding.TIME_EPOCH_MILLIS), dts));
  }

  @SuppressWarnings({"unchecked", "rawtypes" })
  public ListDateTimeObjectTimeSeries(final TimeZone timeZone, final ObjectTimeSeries<Date, T> dts) {
    super(new DateEpochMillisConverter(timeZone), (FastMutableLongObjectTimeSeries<T>) new DateEpochMillisConverter(timeZone).convertToLong(new FastListLongObjectTimeSeries(
        DateTimeNumericEncoding.TIME_EPOCH_MILLIS), dts));
  }

  public ListDateTimeObjectTimeSeries(final FastMutableLongObjectTimeSeries<T> pmidts) {
    super(s_converter, pmidts);
  }

  public ListDateTimeObjectTimeSeries(final TimeZone timeZone, final FastMutableLongObjectTimeSeries<T> pmidts) {
    super(new DateEpochMillisConverter(timeZone), pmidts);
  }

  @Override
  public DateTimeObjectTimeSeries<T> newInstanceFast(final Date[] dateTimes, final T[] values) {
    return new ListDateTimeObjectTimeSeries<T>(((DateEpochMillisConverter) getConverter()).getTimeZone(), dateTimes, values);
  }
}
