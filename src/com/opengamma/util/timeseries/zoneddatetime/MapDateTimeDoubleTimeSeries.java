/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.util.timeseries.zoneddatetime;

import java.util.List;

import javax.time.calendar.TimeZone;
import javax.time.calendar.ZonedDateTime;

import com.opengamma.util.timeseries.DateTimeConverter;
import com.opengamma.util.timeseries.fast.DateTimeNumericEncoding;
import com.opengamma.util.timeseries.fast.longint.FastMapLongDoubleTimeSeries;
import com.opengamma.util.timeseries.fast.longint.FastMutableLongDoubleTimeSeries;

/**
 * @author jim
 * 
 */
public class MapDateTimeDoubleTimeSeries extends MutableZonedDateTimeDoubleTimeSeries.Long {
  public static final MapDateTimeDoubleTimeSeries EMPTY_SERIES = new MapDateTimeDoubleTimeSeries();
  private static final DateTimeConverter<ZonedDateTime> s_converter = new ZonedDateTimeEpochMillisConverter();

  private MapDateTimeDoubleTimeSeries() {
    super(new ZonedDateTimeEpochMillisConverter(), new FastMapLongDoubleTimeSeries(DateTimeNumericEncoding.TIME_EPOCH_MILLIS));
  }

  public MapDateTimeDoubleTimeSeries(final ZonedDateTime[] dates, final double[] values) {
    super(s_converter, new FastMapLongDoubleTimeSeries(DateTimeNumericEncoding.TIME_EPOCH_MILLIS, s_converter.convertToLong(dates), values));
  }

  public MapDateTimeDoubleTimeSeries(final TimeZone timeZone, final ZonedDateTime[] dates, final double[] values) {
    super(new ZonedDateTimeEpochMillisConverter(timeZone), new FastMapLongDoubleTimeSeries(DateTimeNumericEncoding.TIME_EPOCH_MILLIS, new ZonedDateTimeEpochMillisConverter(timeZone)
        .convertToLong(dates), values));
  }

  public MapDateTimeDoubleTimeSeries(final List<ZonedDateTime> dates, final List<Double> values) {
    super(s_converter, new FastMapLongDoubleTimeSeries(DateTimeNumericEncoding.TIME_EPOCH_MILLIS, s_converter.convertToLong(dates), values));
  }

  public MapDateTimeDoubleTimeSeries(final TimeZone timeZone, final List<ZonedDateTime> dates, final List<Double> values) {
    super(new ZonedDateTimeEpochMillisConverter(timeZone), new FastMapLongDoubleTimeSeries(DateTimeNumericEncoding.TIME_EPOCH_MILLIS, new ZonedDateTimeEpochMillisConverter(timeZone)
        .convertToLong(dates), values));
  }

  public MapDateTimeDoubleTimeSeries(final ZonedDateTimeDoubleTimeSeries dts) {
    super(s_converter, (FastMutableLongDoubleTimeSeries) s_converter.convertToLong(new FastMapLongDoubleTimeSeries(DateTimeNumericEncoding.TIME_EPOCH_MILLIS), dts));
  }

  public MapDateTimeDoubleTimeSeries(final TimeZone timeZone, final ZonedDateTimeDoubleTimeSeries dts) {
    super(new ZonedDateTimeEpochMillisConverter(timeZone), (FastMutableLongDoubleTimeSeries) new ZonedDateTimeEpochMillisConverter(timeZone).convertToLong(new FastMapLongDoubleTimeSeries(
        DateTimeNumericEncoding.TIME_EPOCH_MILLIS), dts));
  }

  public MapDateTimeDoubleTimeSeries(final FastMutableLongDoubleTimeSeries pmidts) {
    super(s_converter, pmidts);
  }

  public MapDateTimeDoubleTimeSeries(final TimeZone timeZone, final FastMutableLongDoubleTimeSeries pmidts) {
    super(new ZonedDateTimeEpochMillisConverter(timeZone), pmidts);
  }

  @Override
  public ZonedDateTimeDoubleTimeSeries newInstanceFast(final ZonedDateTime[] dateTimes, final double[] values) {
    return new MapDateTimeDoubleTimeSeries(((ZonedDateTimeEpochMillisConverter) getConverter()).getTimeZone310(), dateTimes, values);
  }
}
