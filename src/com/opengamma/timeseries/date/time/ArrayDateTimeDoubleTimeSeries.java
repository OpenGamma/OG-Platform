/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.timeseries.date.time;

import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import com.opengamma.timeseries.date.DateDoubleTimeSeries;
import com.opengamma.timeseries.date.DateEpochDaysConverter;
import com.opengamma.timeseries.fast.DateTimeNumericEncoding;
import com.opengamma.timeseries.fast.longint.FastArrayLongDoubleTimeSeries;
import com.opengamma.timeseries.fast.longint.FastListLongDoubleTimeSeries;
import com.opengamma.timeseries.fast.longint.FastLongDoubleTimeSeries;

/**
 * @author jim
 * 
 */
public class ArrayDateTimeDoubleTimeSeries extends DateTimeDoubleTimeSeries.Long {
  private static final FastListLongDoubleTimeSeries DEFAULT_SERIES_TEMPLATE = new FastListLongDoubleTimeSeries(DateTimeNumericEncoding.TIME_EPOCH_MILLIS);
  public static final ArrayDateTimeDoubleTimeSeries EMPTY_SERIES = new ArrayDateTimeDoubleTimeSeries();
  private static final DateEpochMillisConverter s_converter = new DateEpochMillisConverter();

  private ArrayDateTimeDoubleTimeSeries() {
    super(s_converter, FastArrayLongDoubleTimeSeries.EMPTY_SERIES);
  }

  public ArrayDateTimeDoubleTimeSeries(final Date[] dates, final double[] values) {
    super(s_converter, new FastArrayLongDoubleTimeSeries(DateTimeNumericEncoding.TIME_EPOCH_MILLIS, s_converter.convertToLong(dates), values));
  }

  public ArrayDateTimeDoubleTimeSeries(final TimeZone timeZone, final Date[] dates, final double[] values) {
    super(new DateEpochMillisConverter(timeZone), new FastArrayLongDoubleTimeSeries(DateTimeNumericEncoding.TIME_EPOCH_MILLIS, new DateEpochMillisConverter(timeZone)
        .convertToLong(dates), values));
  }

  public ArrayDateTimeDoubleTimeSeries(final List<Date> dates, final List<Double> values) {
    super(s_converter, new FastArrayLongDoubleTimeSeries(DateTimeNumericEncoding.TIME_EPOCH_MILLIS, s_converter.convertToLong(dates), values));
  }

  public ArrayDateTimeDoubleTimeSeries(final TimeZone timeZone, final List<Date> dates, final List<Double> values) {
    super(new DateEpochMillisConverter(timeZone), new FastArrayLongDoubleTimeSeries(DateTimeNumericEncoding.TIME_EPOCH_MILLIS, new DateEpochMillisConverter(timeZone)
        .convertToLong(dates), values));
  }

  public ArrayDateTimeDoubleTimeSeries(final DateTimeDoubleTimeSeries dts) {
    super(s_converter, s_converter.convertToLong(DEFAULT_SERIES_TEMPLATE, dts));
  }

  public ArrayDateTimeDoubleTimeSeries(final TimeZone timeZone, final DateTimeDoubleTimeSeries dts) {
    super(new DateEpochMillisConverter(timeZone), new DateEpochMillisConverter(timeZone).convertToLong(DEFAULT_SERIES_TEMPLATE, dts));
  }

  public ArrayDateTimeDoubleTimeSeries(final FastLongDoubleTimeSeries pidts) {
    super(s_converter, pidts);
  }

  public ArrayDateTimeDoubleTimeSeries(final TimeZone timeZone, final FastLongDoubleTimeSeries pidts) {
    super(new DateEpochDaysConverter(timeZone), pidts);
  }

  @Override
  public DateDoubleTimeSeries newInstanceFast(final Date[] dateTimes, final double[] values) {
    return new ArrayDateTimeDoubleTimeSeries(((DateEpochMillisConverter) getConverter()).getTimeZone(), dateTimes, values);
  }

}
