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
import com.opengamma.util.timeseries.DoubleTimeSeries;
import com.opengamma.util.timeseries.fast.DateTimeNumericEncoding;
import com.opengamma.util.timeseries.fast.integer.FastArrayIntDoubleTimeSeries;
import com.opengamma.util.timeseries.fast.integer.FastIntDoubleTimeSeries;
import com.opengamma.util.timeseries.fast.integer.FastListIntDoubleTimeSeries;

/**
 * 
 */
public class ArraySQLDateDoubleTimeSeries extends SQLDateDoubleTimeSeries.Integer {
  @SuppressWarnings("unused")
  private static final ArraySQLDateDoubleTimeSeries EMPTY_SERIES = new ArraySQLDateDoubleTimeSeries();
  private static final DateTimeConverter<Date> s_converter = new SQLDateEpochDaysConverter();

  public ArraySQLDateDoubleTimeSeries() {
    super(new SQLDateEpochDaysConverter(), new FastArrayIntDoubleTimeSeries(DateTimeNumericEncoding.DATE_EPOCH_DAYS));
  }

  public ArraySQLDateDoubleTimeSeries(final Date[] dates, final double[] values) {
    super(s_converter, new FastArrayIntDoubleTimeSeries(DateTimeNumericEncoding.DATE_EPOCH_DAYS,
        s_converter.convertToInt(dates), values));
  }

  public ArraySQLDateDoubleTimeSeries(final TimeZone timeZone, final Date[] dates, final double[] values) {
    super(new SQLDateEpochDaysConverter(timeZone), new FastArrayIntDoubleTimeSeries(
        DateTimeNumericEncoding.DATE_EPOCH_DAYS, new SQLDateEpochDaysConverter(timeZone).convertToInt(dates),
        values));
  }

  public ArraySQLDateDoubleTimeSeries(final List<Date> dates, final List<Double> values) {
    super(s_converter, new FastArrayIntDoubleTimeSeries(DateTimeNumericEncoding.DATE_EPOCH_DAYS,
        s_converter.convertToInt(dates), values));
  }

  public ArraySQLDateDoubleTimeSeries(final TimeZone timeZone, final List<Date> dates, final List<Double> values) {
    super(new SQLDateEpochDaysConverter(timeZone), new FastArrayIntDoubleTimeSeries(
        DateTimeNumericEncoding.DATE_EPOCH_DAYS, new SQLDateEpochDaysConverter(timeZone).convertToInt(dates),
        values));
  }

  public ArraySQLDateDoubleTimeSeries(final DoubleTimeSeries<Date> dts) {
    super(s_converter, s_converter.convertToInt(
        new FastListIntDoubleTimeSeries(DateTimeNumericEncoding.DATE_EPOCH_DAYS), dts));
  }

  public ArraySQLDateDoubleTimeSeries(final TimeZone timeZone, final DoubleTimeSeries<Date> dts) {
    super(new SQLDateEpochDaysConverter(timeZone), new SQLDateEpochDaysConverter(timeZone).convertToInt(
        new FastListIntDoubleTimeSeries(DateTimeNumericEncoding.DATE_EPOCH_DAYS), dts));
  }

  public ArraySQLDateDoubleTimeSeries(final FastIntDoubleTimeSeries pidts) {
    super(s_converter, pidts);
  }

  public ArraySQLDateDoubleTimeSeries(final DateTimeConverter<Date> converter, final FastIntDoubleTimeSeries pidts) {
    super(converter, pidts);
  }

  public ArraySQLDateDoubleTimeSeries(final TimeZone timeZone, final FastIntDoubleTimeSeries pidts) {
    super(new SQLDateEpochDaysConverter(timeZone), pidts);
  }

  @Override
  public SQLDateDoubleTimeSeries newInstanceFast(final Date[] dateTimes, final double[] values) {
    return new ArraySQLDateDoubleTimeSeries(dateTimes, values);
  }

}
