/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
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
import com.opengamma.util.timeseries.fast.integer.FastMapIntDoubleTimeSeries;
import com.opengamma.util.timeseries.fast.integer.FastMutableIntDoubleTimeSeries;

/**
 * @author jim
 * 
 */
public class MapSQLDateDoubleTimeSeries extends MutableSQLDateDoubleTimeSeries.Integer {
  private static final DateTimeConverter<Date> s_converter = new SQLDateEpochDaysConverter();

  public MapSQLDateDoubleTimeSeries() {
    super(new SQLDateEpochDaysConverter(), new FastMapIntDoubleTimeSeries(DateTimeNumericEncoding.DATE_EPOCH_DAYS));
  }

  public MapSQLDateDoubleTimeSeries(final Date[] dates, final double[] values) {
    super(s_converter, new FastMapIntDoubleTimeSeries(DateTimeNumericEncoding.DATE_EPOCH_DAYS, s_converter.convertToInt(dates), values));
  }

  public MapSQLDateDoubleTimeSeries(final TimeZone timeZone, final Date[] dates, final double[] values) {
    super(new SQLDateEpochDaysConverter(timeZone), new FastMapIntDoubleTimeSeries(DateTimeNumericEncoding.DATE_EPOCH_DAYS, new SQLDateEpochDaysConverter(timeZone).convertToInt(dates),
        values));
  }

  public MapSQLDateDoubleTimeSeries(final List<Date> dates, final List<Double> values) {
    super(s_converter, new FastMapIntDoubleTimeSeries(DateTimeNumericEncoding.DATE_EPOCH_DAYS, s_converter.convertToInt(dates), values));
  }

  public MapSQLDateDoubleTimeSeries(final TimeZone timeZone, final List<Date> dates, final List<Double> values) {
    super(new SQLDateEpochDaysConverter(timeZone), new FastMapIntDoubleTimeSeries(DateTimeNumericEncoding.DATE_EPOCH_DAYS, new SQLDateEpochDaysConverter(timeZone).convertToInt(dates),
        values));
  }

  public MapSQLDateDoubleTimeSeries(final DoubleTimeSeries<Date> dts) {
    super(s_converter, (FastMutableIntDoubleTimeSeries) s_converter.convertToInt(new FastMapIntDoubleTimeSeries(DateTimeNumericEncoding.DATE_EPOCH_DAYS), dts));
  }

  public MapSQLDateDoubleTimeSeries(final TimeZone timeZone, final DoubleTimeSeries<Date> dts) {
    super(new SQLDateEpochDaysConverter(timeZone), (FastMutableIntDoubleTimeSeries) new SQLDateEpochDaysConverter(timeZone).convertToInt(new FastMapIntDoubleTimeSeries(
        DateTimeNumericEncoding.DATE_EPOCH_DAYS), dts));
  }

  public MapSQLDateDoubleTimeSeries(final FastMutableIntDoubleTimeSeries pmidts) {
    super(s_converter, pmidts);
  }
  
  public MapSQLDateDoubleTimeSeries(final DateTimeConverter<Date> converter, final FastMutableIntDoubleTimeSeries pmidts) {
    super(converter, pmidts);
  }

  public MapSQLDateDoubleTimeSeries(final TimeZone timeZone, final FastMutableIntDoubleTimeSeries pmidts) {
    super(new SQLDateEpochDaysConverter(timeZone), pmidts);
  }

  @Override
  public MutableSQLDateDoubleTimeSeries newInstanceFast(final Date[] dateTimes, final double[] values) {
    return new MapSQLDateDoubleTimeSeries(dateTimes, values);
  }
}
