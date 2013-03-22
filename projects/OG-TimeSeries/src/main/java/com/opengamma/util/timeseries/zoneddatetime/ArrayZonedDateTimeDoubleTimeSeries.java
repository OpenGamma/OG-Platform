/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.util.timeseries.zoneddatetime;

import java.util.List;

import org.threeten.bp.ZoneId;
import org.threeten.bp.ZonedDateTime;

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
  /** Empty instance */
  public static final ArrayZonedDateTimeDoubleTimeSeries EMPTY_SERIES = new ArrayZonedDateTimeDoubleTimeSeries();
  private static final FastListLongDoubleTimeSeries DEFAULT_SERIES_TEMPLATE = new FastListLongDoubleTimeSeries(DateTimeNumericEncoding.TIME_EPOCH_MILLIS);
  private static final ZonedDateTimeEpochMillisConverter s_converter = new ZonedDateTimeEpochMillisConverter();
  
  public ArrayZonedDateTimeDoubleTimeSeries() {
    super(new ZonedDateTimeEpochMillisConverter(), FastArrayLongDoubleTimeSeries.EMPTY_SERIES);
  }

  public ArrayZonedDateTimeDoubleTimeSeries(final ZoneId timeZone) {
    super(new ZonedDateTimeEpochMillisConverter(timeZone), FastArrayLongDoubleTimeSeries.EMPTY_SERIES);
  }
  
  public ArrayZonedDateTimeDoubleTimeSeries(final ZonedDateTime[] dates, final double[] values) {
    super(s_converter, new FastArrayLongDoubleTimeSeries(DateTimeNumericEncoding.TIME_EPOCH_MILLIS, s_converter.convertToLong(dates), values));
  }

  public ArrayZonedDateTimeDoubleTimeSeries(final ZoneId timeZone, final ZonedDateTime[] dates, final double[] values) {
    super(new ZonedDateTimeEpochMillisConverter(timeZone), new FastArrayLongDoubleTimeSeries(DateTimeNumericEncoding.TIME_EPOCH_MILLIS, new ZonedDateTimeEpochMillisConverter(timeZone)
        .convertToLong(dates), values));
  }

  public ArrayZonedDateTimeDoubleTimeSeries(final List<ZonedDateTime> dates, final List<Double> values) {
    super(s_converter, new FastArrayLongDoubleTimeSeries(DateTimeNumericEncoding.TIME_EPOCH_MILLIS, s_converter.convertToLong(dates), values));
  }

  public ArrayZonedDateTimeDoubleTimeSeries(final ZoneId timeZone, final List<ZonedDateTime> dates, final List<Double> values) {
    super(new ZonedDateTimeEpochMillisConverter(timeZone), new FastArrayLongDoubleTimeSeries(DateTimeNumericEncoding.TIME_EPOCH_MILLIS, new ZonedDateTimeEpochMillisConverter(timeZone)
        .convertToLong(dates), values));
  }

  public ArrayZonedDateTimeDoubleTimeSeries(final ZonedDateTimeDoubleTimeSeries dts) {
    super(s_converter, s_converter.convertToLong(DEFAULT_SERIES_TEMPLATE, dts));
  }

  public ArrayZonedDateTimeDoubleTimeSeries(final ZoneId timeZone, final DoubleTimeSeries<ZonedDateTime> dts) {
    super(new ZonedDateTimeEpochMillisConverter(timeZone), new ZonedDateTimeEpochMillisConverter(timeZone).convertToLong(DEFAULT_SERIES_TEMPLATE, dts));
  }

  public ArrayZonedDateTimeDoubleTimeSeries(final FastLongDoubleTimeSeries pidts) {
    super(s_converter, pidts);
  }
  
  public ArrayZonedDateTimeDoubleTimeSeries(final DateTimeConverter<ZonedDateTime> converter, final FastLongDoubleTimeSeries pidts) {
    super(converter, pidts);
  }

  public ArrayZonedDateTimeDoubleTimeSeries(final ZoneId timeZone, final FastLongDoubleTimeSeries pidts) {
    super(new ZonedDateTimeEpochMillisConverter(timeZone), pidts);
  }

  @Override
  public ZonedDateTimeDoubleTimeSeries newInstanceFast(final ZonedDateTime[] dateTimes, final double[] values) {
    return new ArrayZonedDateTimeDoubleTimeSeries(((ZonedDateTimeEpochMillisConverter) getConverter()).getTimeZone310(), dateTimes, values);
  }


}
