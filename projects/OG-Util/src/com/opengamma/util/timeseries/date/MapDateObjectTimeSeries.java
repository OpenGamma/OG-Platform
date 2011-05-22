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
import com.opengamma.util.timeseries.fast.integer.object.FastMapIntObjectTimeSeries;
import com.opengamma.util.timeseries.fast.integer.object.FastMutableIntObjectTimeSeries;

/**
 * 
 * @param <T> The type of the data
 */
public class MapDateObjectTimeSeries<T> extends MutableDateObjectTimeSeries.Integer<T> {
  @SuppressWarnings("unused")
  private static final MapDateObjectTimeSeries<?> EMPTY_SERIES = new MapDateObjectTimeSeries<Object>();
  private static final DateTimeConverter<Date> s_converter = new DateEpochDaysConverter();

  public MapDateObjectTimeSeries() {
    super(new DateEpochDaysConverter(), new FastMapIntObjectTimeSeries<T>(DateTimeNumericEncoding.DATE_EPOCH_DAYS));
  }

  public MapDateObjectTimeSeries(final Date[] dates, final T[] values) {
    super(s_converter, new FastMapIntObjectTimeSeries<T>(DateTimeNumericEncoding.DATE_EPOCH_DAYS, s_converter.convertToInt(dates), values));
  }

  public MapDateObjectTimeSeries(final TimeZone timeZone, final Date[] dates, final T[] values) {
    super(new DateEpochDaysConverter(timeZone), new FastMapIntObjectTimeSeries<T>(DateTimeNumericEncoding.DATE_EPOCH_DAYS, new DateEpochDaysConverter(timeZone).convertToInt(dates),
        values));
  }

  public MapDateObjectTimeSeries(final List<Date> dates, final List<T> values) {
    super(s_converter, new FastMapIntObjectTimeSeries<T>(DateTimeNumericEncoding.DATE_EPOCH_DAYS, s_converter.convertToInt(dates), values));
  }

  public MapDateObjectTimeSeries(final TimeZone timeZone, final List<Date> dates, final List<T> values) {
    super(new DateEpochDaysConverter(timeZone), new FastMapIntObjectTimeSeries<T>(DateTimeNumericEncoding.DATE_EPOCH_DAYS, new DateEpochDaysConverter(timeZone).convertToInt(dates),
        values));
  }

  public MapDateObjectTimeSeries(final ObjectTimeSeries<Date, T> dts) {
    super(s_converter, (FastMutableIntObjectTimeSeries<T>) s_converter.convertToInt(new FastMapIntObjectTimeSeries<T>(DateTimeNumericEncoding.DATE_EPOCH_DAYS), dts));
  }

  public MapDateObjectTimeSeries(final TimeZone timeZone, final DateObjectTimeSeries<T> dts) {
    super(new DateEpochDaysConverter(timeZone), (FastMutableIntObjectTimeSeries<T>) new DateEpochDaysConverter(timeZone).convertToInt(new FastMapIntObjectTimeSeries<T>(
        DateTimeNumericEncoding.DATE_EPOCH_DAYS), dts));
  }

  public MapDateObjectTimeSeries(final FastMutableIntObjectTimeSeries<T> pmidts) {
    super(s_converter, pmidts);
  }

  public MapDateObjectTimeSeries(final TimeZone timeZone, final FastMutableIntObjectTimeSeries<T> pmidts) {
    super(new DateEpochDaysConverter(timeZone), pmidts);
  }

  @Override
  public MutableDateObjectTimeSeries<T> newInstanceFast(final Date[] dateTimes, final T[] values) {
    return new MapDateObjectTimeSeries<T>(dateTimes, values);
  }
}
