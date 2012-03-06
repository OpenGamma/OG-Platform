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
import com.opengamma.util.timeseries.DoubleTimeSeries;
import com.opengamma.util.timeseries.fast.DateTimeNumericEncoding;
import com.opengamma.util.timeseries.fast.longint.FastArrayLongDoubleTimeSeries;
import com.opengamma.util.timeseries.fast.longint.FastListLongDoubleTimeSeries;
import com.opengamma.util.timeseries.fast.longint.FastLongDoubleTimeSeries;

/**
 * 
 */
public class ArrayZonedDateTimeDoubleTimeSeries extends ZonedDateTimeDoubleTimeSeries.Long {
  private static final FastListLongDoubleTimeSeries DEFAULT_SERIES_TEMPLATE = new FastListLongDoubleTimeSeries(DateTimeNumericEncoding.TIME_EPOCH_MILLIS);
  private static final ZonedDateTimeEpochMillisConverter s_converter = new ZonedDateTimeEpochMillisConverter();
  public static final ArrayZonedDateTimeDoubleTimeSeries EMPTY_SERIES = new ArrayZonedDateTimeDoubleTimeSeries();
  
  public ArrayZonedDateTimeDoubleTimeSeries() {
    super(new ZonedDateTimeEpochMillisConverter(), FastArrayLongDoubleTimeSeries.EMPTY_SERIES);
  }

  public ArrayZonedDateTimeDoubleTimeSeries(final TimeZone timeZone) {
    super(new ZonedDateTimeEpochMillisConverter(timeZone), FastArrayLongDoubleTimeSeries.EMPTY_SERIES);
  }
  
  public ArrayZonedDateTimeDoubleTimeSeries(final ZonedDateTime[] dates, final double[] values) {
    super(s_converter, new FastArrayLongDoubleTimeSeries(DateTimeNumericEncoding.TIME_EPOCH_MILLIS, s_converter.convertToLong(dates), values));
  }

  public ArrayZonedDateTimeDoubleTimeSeries(final TimeZone timeZone, final ZonedDateTime[] dates, final double[] values) {
    super(new ZonedDateTimeEpochMillisConverter(timeZone), new FastArrayLongDoubleTimeSeries(DateTimeNumericEncoding.TIME_EPOCH_MILLIS, new ZonedDateTimeEpochMillisConverter(timeZone)
        .convertToLong(dates), values));
  }

  public ArrayZonedDateTimeDoubleTimeSeries(final List<ZonedDateTime> dates, final List<Double> values) {
    super(s_converter, new FastArrayLongDoubleTimeSeries(DateTimeNumericEncoding.TIME_EPOCH_MILLIS, s_converter.convertToLong(dates), values));
  }

  public ArrayZonedDateTimeDoubleTimeSeries(final TimeZone timeZone, final List<ZonedDateTime> dates, final List<Double> values) {
    super(new ZonedDateTimeEpochMillisConverter(timeZone), new FastArrayLongDoubleTimeSeries(DateTimeNumericEncoding.TIME_EPOCH_MILLIS, new ZonedDateTimeEpochMillisConverter(timeZone)
        .convertToLong(dates), values));
  }

  public ArrayZonedDateTimeDoubleTimeSeries(final ZonedDateTimeDoubleTimeSeries dts) {
    super(s_converter, s_converter.convertToLong(DEFAULT_SERIES_TEMPLATE, dts));
  }

  public ArrayZonedDateTimeDoubleTimeSeries(final TimeZone timeZone, final DoubleTimeSeries<ZonedDateTime> dts) {
    super(new ZonedDateTimeEpochMillisConverter(timeZone), new ZonedDateTimeEpochMillisConverter(timeZone).convertToLong(DEFAULT_SERIES_TEMPLATE, dts));
  }

  public ArrayZonedDateTimeDoubleTimeSeries(final FastLongDoubleTimeSeries pidts) {
    super(s_converter, pidts);
  }
  
  public ArrayZonedDateTimeDoubleTimeSeries(final DateTimeConverter<ZonedDateTime> converter, final FastLongDoubleTimeSeries pidts) {
    super(converter, pidts);
  }

  public ArrayZonedDateTimeDoubleTimeSeries(final TimeZone timeZone, final FastLongDoubleTimeSeries pidts) {
    super(new ZonedDateTimeEpochMillisConverter(timeZone), pidts);
  }

  @Override
  public ZonedDateTimeDoubleTimeSeries newInstanceFast(final ZonedDateTime[] dateTimes, final double[] values) {
    return new ArrayZonedDateTimeDoubleTimeSeries(((ZonedDateTimeEpochMillisConverter) getConverter()).getTimeZone310(), dateTimes, values);
  }


}
