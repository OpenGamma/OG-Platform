/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.util.timeseries.zoneddatetime;

import java.util.List;

import javax.time.calendar.TimeZone;
import javax.time.calendar.ZonedDateTime;

import com.opengamma.util.timeseries.DateTimeConverter;
import com.opengamma.util.timeseries.ObjectTimeSeries;
import com.opengamma.util.timeseries.fast.DateTimeNumericEncoding;
import com.opengamma.util.timeseries.fast.longint.object.FastListLongObjectTimeSeries;
import com.opengamma.util.timeseries.fast.longint.object.FastMutableLongObjectTimeSeries;

/**
 * @param <T> The type of the data
 */
public class ListZonedDateTimeObjectTimeSeries<T> extends MutableZonedDateTimeObjectTimeSeries.Long<T> {
  private static final DateTimeConverter<ZonedDateTime> s_converter = new ZonedDateTimeEpochMillisConverter();

  public ListZonedDateTimeObjectTimeSeries() {
    super(new ZonedDateTimeEpochMillisConverter(), new FastListLongObjectTimeSeries<T>(DateTimeNumericEncoding.TIME_EPOCH_MILLIS));
  }
  
  public ListZonedDateTimeObjectTimeSeries(TimeZone timeZone) {
    super(new ZonedDateTimeEpochMillisConverter(timeZone), new FastListLongObjectTimeSeries<T>(DateTimeNumericEncoding.TIME_EPOCH_MILLIS));
  }

  public ListZonedDateTimeObjectTimeSeries(final ZonedDateTime[] dates, final T[] values) {
    super(s_converter, new FastListLongObjectTimeSeries<T>(DateTimeNumericEncoding.TIME_EPOCH_MILLIS, s_converter.convertToLong(dates), values));
  }

  public ListZonedDateTimeObjectTimeSeries(final TimeZone timeZone, final ZonedDateTime[] dates, final T[] values) {
    super(new ZonedDateTimeEpochMillisConverter(timeZone), new FastListLongObjectTimeSeries<T>(DateTimeNumericEncoding.TIME_EPOCH_MILLIS, new ZonedDateTimeEpochMillisConverter(timeZone)
        .convertToLong(dates), values));
  }

  public ListZonedDateTimeObjectTimeSeries(final List<ZonedDateTime> dates, final List<T> values) {
    super(s_converter, new FastListLongObjectTimeSeries<T>(DateTimeNumericEncoding.TIME_EPOCH_MILLIS, s_converter.convertToLong(dates), values));
  }

  public ListZonedDateTimeObjectTimeSeries(final TimeZone timeZone, final List<ZonedDateTime> dates, final List<T> values) {
    super(new ZonedDateTimeEpochMillisConverter(timeZone), new FastListLongObjectTimeSeries<T>(DateTimeNumericEncoding.TIME_EPOCH_MILLIS, new ZonedDateTimeEpochMillisConverter(timeZone)
        .convertToLong(dates), values));
  }

  @SuppressWarnings({"unchecked", "rawtypes" })
  public ListZonedDateTimeObjectTimeSeries(final ObjectTimeSeries<ZonedDateTime, T> dts) {
    super(s_converter, (FastMutableLongObjectTimeSeries<T>) s_converter.convertToLong(new FastListLongObjectTimeSeries(DateTimeNumericEncoding.TIME_EPOCH_MILLIS), dts));
  }

  @SuppressWarnings({"unchecked", "rawtypes" })
  public ListZonedDateTimeObjectTimeSeries(final TimeZone timeZone, final ObjectTimeSeries<ZonedDateTime, T> dts) {
    super(new ZonedDateTimeEpochMillisConverter(timeZone), (FastMutableLongObjectTimeSeries<T>) new ZonedDateTimeEpochMillisConverter(timeZone).convertToLong(new FastListLongObjectTimeSeries(
        DateTimeNumericEncoding.TIME_EPOCH_MILLIS), dts));
  }

  public ListZonedDateTimeObjectTimeSeries(final FastMutableLongObjectTimeSeries<T> pmidts) {
    super(s_converter, pmidts);
  }

  public ListZonedDateTimeObjectTimeSeries(final TimeZone timeZone, final FastMutableLongObjectTimeSeries<T> pmidts) {
    super(new ZonedDateTimeEpochMillisConverter(timeZone), pmidts);
  }

  @Override
  public ZonedDateTimeObjectTimeSeries<T> newInstanceFast(final ZonedDateTime[] dateTimes, final T[] values) {
    return new ListZonedDateTimeObjectTimeSeries<T>(((ZonedDateTimeEpochMillisConverter) getConverter()).getTimeZone310(), dateTimes, values);
  }
}
