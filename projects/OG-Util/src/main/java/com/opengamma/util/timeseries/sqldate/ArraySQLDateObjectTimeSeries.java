/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.util.timeseries.sqldate;

import java.sql.Date;
import java.util.List;
import java.util.TimeZone;

import com.opengamma.util.timeseries.DateTimeConverter;
import com.opengamma.util.timeseries.ObjectTimeSeries;
import com.opengamma.util.timeseries.fast.DateTimeNumericEncoding;
import com.opengamma.util.timeseries.fast.integer.object.FastArrayIntObjectTimeSeries;
import com.opengamma.util.timeseries.fast.integer.object.FastIntObjectTimeSeries;
import com.opengamma.util.timeseries.fast.integer.object.FastListIntObjectTimeSeries;

/**
 * @param <T> The type of the data
 */
public class ArraySQLDateObjectTimeSeries<T> extends SQLDateObjectTimeSeries.Integer<T> {
  /** An empty time series */
  public static final ArraySQLDateObjectTimeSeries<?> EMPTY_SERIES = new ArraySQLDateObjectTimeSeries<Object>();
  private static final DateTimeConverter<Date> s_converter = new SQLDateEpochDaysConverter();

  public ArraySQLDateObjectTimeSeries() {
    super(new SQLDateEpochDaysConverter(), new FastArrayIntObjectTimeSeries<T>(DateTimeNumericEncoding.DATE_EPOCH_DAYS));
  }

  public ArraySQLDateObjectTimeSeries(final Date[] dates, final T[] values) {
    super(s_converter, new FastArrayIntObjectTimeSeries<T>(DateTimeNumericEncoding.DATE_EPOCH_DAYS,
        s_converter.convertToInt(dates), values));
  }

  public ArraySQLDateObjectTimeSeries(final TimeZone timeZone, final Date[] dates, final T[] values) {
    super(new SQLDateEpochDaysConverter(timeZone), new FastArrayIntObjectTimeSeries<T>(
        DateTimeNumericEncoding.DATE_EPOCH_DAYS, new SQLDateEpochDaysConverter(timeZone).convertToInt(dates),
        values));
  }

  public ArraySQLDateObjectTimeSeries(final List<Date> dates, final List<T> values) {
    super(s_converter, new FastArrayIntObjectTimeSeries<T>(DateTimeNumericEncoding.DATE_EPOCH_DAYS,
        s_converter.convertToInt(dates), values));
  }

  public ArraySQLDateObjectTimeSeries(final TimeZone timeZone, final List<Date> dates, final List<T> values) {
    super(new SQLDateEpochDaysConverter(timeZone), new FastArrayIntObjectTimeSeries<T>(
        DateTimeNumericEncoding.DATE_EPOCH_DAYS, new SQLDateEpochDaysConverter(timeZone).convertToInt(dates),
        values));
  }

  public ArraySQLDateObjectTimeSeries(final ObjectTimeSeries<Date, T> dts) {
    super(s_converter, s_converter.convertToInt(new FastListIntObjectTimeSeries<T>(
        DateTimeNumericEncoding.DATE_EPOCH_DAYS), dts));
  }

  public ArraySQLDateObjectTimeSeries(final TimeZone timeZone, final ObjectTimeSeries<Date, T> dts) {
    super(new SQLDateEpochDaysConverter(timeZone), new SQLDateEpochDaysConverter(timeZone).convertToInt(
        new FastListIntObjectTimeSeries<T>(DateTimeNumericEncoding.DATE_EPOCH_DAYS), dts));
  }

  public ArraySQLDateObjectTimeSeries(final FastIntObjectTimeSeries<T> pidts) {
    super(s_converter, pidts);
  }

  public ArraySQLDateObjectTimeSeries(final TimeZone timeZone, final FastIntObjectTimeSeries<T> pidts) {
    super(new SQLDateEpochDaysConverter(timeZone), pidts);
  }

  @Override
  public SQLDateObjectTimeSeries<T> newInstanceFast(final Date[] dateTimes, final T[] values) {
    return new ArraySQLDateObjectTimeSeries<T>(dateTimes, values);
  }

}
