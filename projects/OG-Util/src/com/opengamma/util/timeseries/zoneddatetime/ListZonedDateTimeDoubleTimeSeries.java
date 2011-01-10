/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
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
import com.opengamma.util.timeseries.fast.longint.FastListLongDoubleTimeSeries;
import com.opengamma.util.timeseries.fast.longint.FastMutableLongDoubleTimeSeries;

/**
 * 
 */
public class ListZonedDateTimeDoubleTimeSeries extends MutableZonedDateTimeDoubleTimeSeries.Long {
  private static final DateTimeConverter<ZonedDateTime> s_converter = new ZonedDateTimeEpochMillisConverter();

  public ListZonedDateTimeDoubleTimeSeries() {
    super(new ZonedDateTimeEpochMillisConverter(), new FastListLongDoubleTimeSeries(DateTimeNumericEncoding.TIME_EPOCH_MILLIS));
  }
  
  public ListZonedDateTimeDoubleTimeSeries(TimeZone timeZone) {
    super(new ZonedDateTimeEpochMillisConverter(timeZone), new FastListLongDoubleTimeSeries(DateTimeNumericEncoding.TIME_EPOCH_MILLIS));
  }

  public ListZonedDateTimeDoubleTimeSeries(final ZonedDateTime[] dates, final double[] values) {
    super(s_converter, new FastListLongDoubleTimeSeries(DateTimeNumericEncoding.TIME_EPOCH_MILLIS, s_converter.convertToLong(dates), values));
  }

  public ListZonedDateTimeDoubleTimeSeries(final TimeZone timeZone, final ZonedDateTime[] dates, final double[] values) {
    super(new ZonedDateTimeEpochMillisConverter(timeZone), new FastListLongDoubleTimeSeries(DateTimeNumericEncoding.TIME_EPOCH_MILLIS, new ZonedDateTimeEpochMillisConverter(timeZone)
        .convertToLong(dates), values));
  }

  public ListZonedDateTimeDoubleTimeSeries(final List<ZonedDateTime> dates, final List<Double> values) {
    super(s_converter, new FastListLongDoubleTimeSeries(DateTimeNumericEncoding.TIME_EPOCH_MILLIS, s_converter.convertToLong(dates), values));
  }

  public ListZonedDateTimeDoubleTimeSeries(final TimeZone timeZone, final List<ZonedDateTime> dates, final List<Double> values) {
    super(new ZonedDateTimeEpochMillisConverter(timeZone), new FastListLongDoubleTimeSeries(DateTimeNumericEncoding.TIME_EPOCH_MILLIS, new ZonedDateTimeEpochMillisConverter(timeZone)
        .convertToLong(dates), values));
  }

  public ListZonedDateTimeDoubleTimeSeries(final DoubleTimeSeries<ZonedDateTime> dts) {
    super(s_converter, (FastMutableLongDoubleTimeSeries) s_converter.convertToLong(new FastListLongDoubleTimeSeries(DateTimeNumericEncoding.TIME_EPOCH_MILLIS), dts));
  }

  public ListZonedDateTimeDoubleTimeSeries(final TimeZone timeZone, final DoubleTimeSeries<ZonedDateTime> dts) {
    super(new ZonedDateTimeEpochMillisConverter(timeZone), (FastMutableLongDoubleTimeSeries) new ZonedDateTimeEpochMillisConverter(timeZone).convertToLong(new FastListLongDoubleTimeSeries(
        DateTimeNumericEncoding.TIME_EPOCH_MILLIS), dts));
  }

  public ListZonedDateTimeDoubleTimeSeries(final FastMutableLongDoubleTimeSeries pmidts) {
    super(s_converter, pmidts);
  }
  
  public ListZonedDateTimeDoubleTimeSeries(final DateTimeConverter<ZonedDateTime> converter, final FastMutableLongDoubleTimeSeries pmidts) {
    super(converter, pmidts);
  }

  public ListZonedDateTimeDoubleTimeSeries(final TimeZone timeZone, final FastMutableLongDoubleTimeSeries pmidts) {
    super(new ZonedDateTimeEpochMillisConverter(timeZone), pmidts);
  }

  @Override
  public ZonedDateTimeDoubleTimeSeries newInstanceFast(final ZonedDateTime[] dateTimes, final double[] values) {
    return new ListZonedDateTimeDoubleTimeSeries(((ZonedDateTimeEpochMillisConverter) getConverter()).getTimeZone310(), dateTimes, values);
  }
}
