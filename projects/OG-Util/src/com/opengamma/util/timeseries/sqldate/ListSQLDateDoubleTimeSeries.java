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
import com.opengamma.util.timeseries.fast.integer.FastIntDoubleTimeSeries;
import com.opengamma.util.timeseries.fast.integer.FastListIntDoubleTimeSeries;
import com.opengamma.util.timeseries.fast.integer.FastMutableIntDoubleTimeSeries;

/**
 * @author jim
 * 
 */
public class ListSQLDateDoubleTimeSeries extends MutableSQLDateDoubleTimeSeries.Integer {
  private static final FastIntDoubleTimeSeries TIMESERIES_TEMPLATE = new FastListIntDoubleTimeSeries(DateTimeNumericEncoding.DATE_EPOCH_DAYS);
  private static final DateTimeConverter<Date> s_converter = new SQLDateEpochDaysConverter();

  public ListSQLDateDoubleTimeSeries() {
    super(new SQLDateEpochDaysConverter(), new FastListIntDoubleTimeSeries(DateTimeNumericEncoding.DATE_EPOCH_DAYS));
  }

  public ListSQLDateDoubleTimeSeries(final Date[] dates, final double[] values) {
    super(s_converter, new FastListIntDoubleTimeSeries(DateTimeNumericEncoding.DATE_EPOCH_DAYS, s_converter.convertToInt(dates), values));
  }

  public ListSQLDateDoubleTimeSeries(final TimeZone timeZone, final Date[] dates, final double[] values) {
    super(new SQLDateEpochDaysConverter(timeZone), new FastListIntDoubleTimeSeries(DateTimeNumericEncoding.DATE_EPOCH_DAYS, new SQLDateEpochDaysConverter(timeZone).convertToInt(dates),
        values));
  }

  public ListSQLDateDoubleTimeSeries(final List<Date> dates, final List<Double> values) {
    super(s_converter, new FastListIntDoubleTimeSeries(DateTimeNumericEncoding.DATE_EPOCH_DAYS, s_converter.convertToInt(dates), values));
  }

  public ListSQLDateDoubleTimeSeries(final TimeZone timeZone, final List<Date> dates, final List<Double> values) {
    super(new SQLDateEpochDaysConverter(timeZone), new FastListIntDoubleTimeSeries(DateTimeNumericEncoding.DATE_EPOCH_DAYS, new SQLDateEpochDaysConverter(timeZone).convertToInt(dates),
        values));
  }

  public ListSQLDateDoubleTimeSeries(final DoubleTimeSeries<Date> dts) {
    super(s_converter, (FastMutableIntDoubleTimeSeries) s_converter.convertToInt(TIMESERIES_TEMPLATE, dts));
  }

  public ListSQLDateDoubleTimeSeries(final TimeZone timeZone, final DoubleTimeSeries<Date> dts) {
    super(new SQLDateEpochDaysConverter(timeZone), (FastMutableIntDoubleTimeSeries) new SQLDateEpochDaysConverter(timeZone).convertToInt(TIMESERIES_TEMPLATE, dts));
  }

  public ListSQLDateDoubleTimeSeries(final FastMutableIntDoubleTimeSeries pmidts) {
    super(s_converter, pmidts);
  }
  
  public ListSQLDateDoubleTimeSeries(final DateTimeConverter<Date> converter, final FastMutableIntDoubleTimeSeries pmidts) {
    super(converter, pmidts);
  }

  public ListSQLDateDoubleTimeSeries(final TimeZone timeZone, final FastMutableIntDoubleTimeSeries pmidts) {
    super(new SQLDateEpochDaysConverter(timeZone), pmidts);
  }

  @Override
  public SQLDateDoubleTimeSeries newInstanceFast(final Date[] dateTimes, final double[] values) {
    return new ListSQLDateDoubleTimeSeries(dateTimes, values);
  }

}
