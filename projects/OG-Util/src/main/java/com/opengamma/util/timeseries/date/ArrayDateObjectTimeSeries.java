/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.util.timeseries.date;

import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import com.opengamma.util.timeseries.DateTimeConverter;
import com.opengamma.util.timeseries.ObjectTimeSeries;
import com.opengamma.util.timeseries.fast.DateTimeNumericEncoding;
import com.opengamma.util.timeseries.fast.integer.object.FastArrayIntObjectTimeSeries;
import com.opengamma.util.timeseries.fast.integer.object.FastIntObjectTimeSeries;
import com.opengamma.util.timeseries.fast.integer.object.FastListIntObjectTimeSeries;

/**
 * @param <T> Object type for the timeseries
 */
public class ArrayDateObjectTimeSeries<T> extends DateObjectTimeSeries.Integer<T> {
  private static final DateTimeConverter<Date> s_converter = new DateEpochDaysConverter();

  public ArrayDateObjectTimeSeries() {
    super(new DateEpochDaysConverter(), new FastArrayIntObjectTimeSeries<T>(DateTimeNumericEncoding.DATE_EPOCH_DAYS));
  }

  public ArrayDateObjectTimeSeries(final Date[] dates, final T[] values) {
    super(s_converter, new FastArrayIntObjectTimeSeries<T>(DateTimeNumericEncoding.DATE_EPOCH_DAYS, s_converter.convertToInt(dates), values));
  }

  public ArrayDateObjectTimeSeries(final TimeZone timeZone, final Date[] dates, final T[] values) {
    super(new DateEpochDaysConverter(timeZone), new FastArrayIntObjectTimeSeries<T>(DateTimeNumericEncoding.DATE_EPOCH_DAYS, new DateEpochDaysConverter(timeZone).convertToInt(dates),
        values));
  }

  public ArrayDateObjectTimeSeries(final List<Date> dates, final List<T> values) {
    super(s_converter, new FastArrayIntObjectTimeSeries<T>(DateTimeNumericEncoding.DATE_EPOCH_DAYS, s_converter.convertToInt(dates), values));
  }

  public ArrayDateObjectTimeSeries(final TimeZone timeZone, final List<Date> dates, final List<T> values) {
    super(new DateEpochDaysConverter(timeZone), new FastArrayIntObjectTimeSeries<T>(DateTimeNumericEncoding.DATE_EPOCH_DAYS, new DateEpochDaysConverter(timeZone).convertToInt(dates),
        values));
  }

  public ArrayDateObjectTimeSeries(final ObjectTimeSeries<Date, T> dts) {
    super(s_converter, s_converter.convertToInt(new FastListIntObjectTimeSeries<T>(DateTimeNumericEncoding.DATE_EPOCH_DAYS), dts));
  }

  public ArrayDateObjectTimeSeries(final TimeZone timeZone, final DateObjectTimeSeries<T> dts) {
    super(new DateEpochDaysConverter(timeZone), new DateEpochDaysConverter(timeZone).convertToInt(new FastListIntObjectTimeSeries<T>(DateTimeNumericEncoding.DATE_EPOCH_DAYS), dts));
  }

  public ArrayDateObjectTimeSeries(final FastIntObjectTimeSeries<T> pidts) {
    super(s_converter, pidts);
  }

  public ArrayDateObjectTimeSeries(final TimeZone timeZone, final FastIntObjectTimeSeries<T> pidts) {
    super(new DateEpochDaysConverter(timeZone), pidts);
  }

  @Override
  public DateObjectTimeSeries<T> newInstanceFast(final Date[] dateTimes, final T[] values) {
    return new ArrayDateObjectTimeSeries<T>(dateTimes, values);
  }
}
