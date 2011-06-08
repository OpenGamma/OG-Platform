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
import com.opengamma.util.timeseries.fast.integer.object.FastIntObjectTimeSeries;
import com.opengamma.util.timeseries.fast.integer.object.FastListIntObjectTimeSeries;
import com.opengamma.util.timeseries.fast.integer.object.FastMutableIntObjectTimeSeries;

/**
 * @param <T> The type of the data 
 */
public class ListSQLDateObjectTimeSeries<T> extends MutableSQLDateObjectTimeSeries.Integer<T> {
  /** A template time series */
  public static final FastIntObjectTimeSeries<?> TIMESERIES_TEMPLATE = new FastListIntObjectTimeSeries<Object>(
      DateTimeNumericEncoding.DATE_EPOCH_DAYS);
  /** An empty time series */
  public static final ListSQLDateObjectTimeSeries<?> EMPTY_SERIES = new ListSQLDateObjectTimeSeries<Object>();
  private static final DateTimeConverter<Date> s_converter = new SQLDateEpochDaysConverter();

  protected ListSQLDateObjectTimeSeries(final DateTimeConverter<Date> converter,
      final FastMutableIntObjectTimeSeries<T> fastTS) {
    super(converter, fastTS);
  }

  public ListSQLDateObjectTimeSeries() {
    super(new SQLDateEpochDaysConverter(), new FastListIntObjectTimeSeries<T>(DateTimeNumericEncoding.DATE_EPOCH_DAYS));
  }

  public ListSQLDateObjectTimeSeries(final Date[] dates, final T[] values) {
    super(s_converter, new FastListIntObjectTimeSeries<T>(DateTimeNumericEncoding.DATE_EPOCH_DAYS,
        s_converter.convertToInt(dates), values));
  }

  public ListSQLDateObjectTimeSeries(final TimeZone timeZone, final Date[] dates, final T[] values) {
    super(new SQLDateEpochDaysConverter(timeZone), new FastListIntObjectTimeSeries<T>(
        DateTimeNumericEncoding.DATE_EPOCH_DAYS, new SQLDateEpochDaysConverter(timeZone).convertToInt(dates),
        values));
  }

  public ListSQLDateObjectTimeSeries(final List<Date> dates, final List<T> values) {
    super(s_converter, new FastListIntObjectTimeSeries<T>(DateTimeNumericEncoding.DATE_EPOCH_DAYS,
        s_converter.convertToInt(dates), values));
  }

  public ListSQLDateObjectTimeSeries(final TimeZone timeZone, final List<Date> dates, final List<T> values) {
    super(new SQLDateEpochDaysConverter(timeZone), new FastListIntObjectTimeSeries<T>(
        DateTimeNumericEncoding.DATE_EPOCH_DAYS, new SQLDateEpochDaysConverter(timeZone).convertToInt(dates),
        values));
  }

  @SuppressWarnings("unchecked")
  public ListSQLDateObjectTimeSeries(final ObjectTimeSeries<Date, T> dts) {
    super(s_converter, (FastMutableIntObjectTimeSeries<T>) s_converter.convertToInt(
        (FastIntObjectTimeSeries<T>) TIMESERIES_TEMPLATE, dts));
  }

  @SuppressWarnings("unchecked")
  public ListSQLDateObjectTimeSeries(final TimeZone timeZone, final ObjectTimeSeries<Date, T> dts) {
    super(new SQLDateEpochDaysConverter(timeZone), (FastMutableIntObjectTimeSeries<T>) new SQLDateEpochDaysConverter(
        timeZone).convertToInt((FastIntObjectTimeSeries<T>) TIMESERIES_TEMPLATE, dts));
  }

  public ListSQLDateObjectTimeSeries(final FastMutableIntObjectTimeSeries<T> pmidts) {
    super(s_converter, pmidts);
  }

  public ListSQLDateObjectTimeSeries(final TimeZone timeZone, final FastMutableIntObjectTimeSeries<T> pmidts) {
    super(new SQLDateEpochDaysConverter(timeZone), pmidts);
  }

  @Override
  public SQLDateObjectTimeSeries<T> newInstanceFast(final Date[] dateTimes, final T[] values) {
    return new ListSQLDateObjectTimeSeries<T>(dateTimes, values);
  }

}
