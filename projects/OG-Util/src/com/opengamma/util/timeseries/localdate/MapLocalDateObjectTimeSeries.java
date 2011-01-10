/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.util.timeseries.localdate;


import java.util.List;

import javax.time.calendar.LocalDate;
import javax.time.calendar.TimeZone;

import com.opengamma.util.timeseries.DateTimeConverter;
import com.opengamma.util.timeseries.ObjectTimeSeries;
import com.opengamma.util.timeseries.fast.DateTimeNumericEncoding;
import com.opengamma.util.timeseries.fast.integer.object.FastMapIntObjectTimeSeries;
import com.opengamma.util.timeseries.fast.integer.object.FastMutableIntObjectTimeSeries;

/**
 * @author jim
 * 
 */
public class MapLocalDateObjectTimeSeries<T> extends MutableLocalDateObjectTimeSeries.Integer<T> {
  public static final MapLocalDateObjectTimeSeries<?> EMPTY_SERIES = new MapLocalDateObjectTimeSeries<Object>();
  private static final DateTimeConverter<LocalDate> s_converter = new LocalDateEpochDaysConverter();

  public MapLocalDateObjectTimeSeries() {
    super(new LocalDateEpochDaysConverter(), new FastMapIntObjectTimeSeries<T>(DateTimeNumericEncoding.DATE_EPOCH_DAYS));
  }

  public MapLocalDateObjectTimeSeries(final LocalDate[] dates, final T[] values) {
    super(s_converter, new FastMapIntObjectTimeSeries<T>(DateTimeNumericEncoding.DATE_EPOCH_DAYS, s_converter.convertToInt(dates), values));
  }

  public MapLocalDateObjectTimeSeries(final TimeZone timeZone, final LocalDate[] dates, final T[] values) {
    super(new LocalDateEpochDaysConverter(timeZone), new FastMapIntObjectTimeSeries<T>(DateTimeNumericEncoding.DATE_EPOCH_DAYS, new LocalDateEpochDaysConverter(timeZone).convertToInt(dates),
        values));
  }

  public MapLocalDateObjectTimeSeries(final List<LocalDate> dates, final List<T> values) {
    super(s_converter, new FastMapIntObjectTimeSeries<T>(DateTimeNumericEncoding.DATE_EPOCH_DAYS, s_converter.convertToInt(dates), values));
  }

  public MapLocalDateObjectTimeSeries(final TimeZone timeZone, final List<LocalDate> dates, final List<T> values) {
    super(new LocalDateEpochDaysConverter(timeZone), new FastMapIntObjectTimeSeries<T>(DateTimeNumericEncoding.DATE_EPOCH_DAYS, new LocalDateEpochDaysConverter(timeZone).convertToInt(dates),
        values));
  }

  public MapLocalDateObjectTimeSeries(final ObjectTimeSeries<LocalDate, T> dts) {
    super(s_converter, (FastMutableIntObjectTimeSeries<T>) s_converter.convertToInt(new FastMapIntObjectTimeSeries<T>(DateTimeNumericEncoding.DATE_EPOCH_DAYS), dts));
  }

  public MapLocalDateObjectTimeSeries(final TimeZone timeZone, final LocalDateObjectTimeSeries<T> dts) {
    super(new LocalDateEpochDaysConverter(timeZone), (FastMutableIntObjectTimeSeries<T>) new LocalDateEpochDaysConverter(timeZone).convertToInt(new FastMapIntObjectTimeSeries<T>(
        DateTimeNumericEncoding.DATE_EPOCH_DAYS), dts));
  }

  public MapLocalDateObjectTimeSeries(final FastMutableIntObjectTimeSeries<T> pmidts) {
    super(s_converter, pmidts);
  }

  public MapLocalDateObjectTimeSeries(final TimeZone timeZone, final FastMutableIntObjectTimeSeries<T> pmidts) {
    super(new LocalDateEpochDaysConverter(timeZone), pmidts);
  }

  @Override
  public MutableLocalDateObjectTimeSeries<T> newInstanceFast(final LocalDate[] dateTimes, final T[] values) {
    return new MapLocalDateObjectTimeSeries<T>(dateTimes, values);
  }
}
