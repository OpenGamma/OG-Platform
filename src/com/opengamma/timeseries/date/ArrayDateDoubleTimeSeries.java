/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.timeseries.date;

import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import com.opengamma.timeseries.DateTimeConverter;
import com.opengamma.timeseries.fast.DateTimeNumericEncoding;
import com.opengamma.timeseries.fast.integer.FastArrayIntDoubleTimeSeries;
import com.opengamma.timeseries.fast.integer.FastIntDoubleTimeSeries;
import com.opengamma.timeseries.fast.integer.FastListIntDoubleTimeSeries;

/**
 * @author jim
 * 
 */
public class ArrayDateDoubleTimeSeries extends DateDoubleTimeSeries.Integer {
  public static final ArrayDateDoubleTimeSeries EMPTY_SERIES = new ArrayDateDoubleTimeSeries();
  private static final DateTimeConverter<Date> s_converter = new DateEpochDaysConverter();

  private ArrayDateDoubleTimeSeries() {
    super(s_converter, FastArrayIntDoubleTimeSeries.EMPTY_SERIES);
  }

  public ArrayDateDoubleTimeSeries(final Date[] dates, final double[] values) {
    super(s_converter, new FastArrayIntDoubleTimeSeries(DateTimeNumericEncoding.DATE_EPOCH_DAYS, s_converter.convertToInt(dates), values));
  }

  public ArrayDateDoubleTimeSeries(final TimeZone timeZone, final Date[] dates, final double[] values) {
    super(new DateEpochDaysConverter(timeZone), new FastArrayIntDoubleTimeSeries(DateTimeNumericEncoding.DATE_EPOCH_DAYS, new DateEpochDaysConverter(timeZone).convertToInt(dates),
        values));
  }

  public ArrayDateDoubleTimeSeries(final List<Date> dates, final List<Double> values) {
    super(s_converter, new FastArrayIntDoubleTimeSeries(DateTimeNumericEncoding.DATE_EPOCH_DAYS, s_converter.convertToInt(dates), values));
  }

  public ArrayDateDoubleTimeSeries(final TimeZone timeZone, final List<Date> dates, final List<Double> values) {
    super(new DateEpochDaysConverter(timeZone), new FastArrayIntDoubleTimeSeries(DateTimeNumericEncoding.DATE_EPOCH_DAYS, new DateEpochDaysConverter(timeZone).convertToInt(dates),
        values));
  }

  public ArrayDateDoubleTimeSeries(final DateDoubleTimeSeries dts) {
    super(s_converter, s_converter.convertToInt(new FastListIntDoubleTimeSeries(DateTimeNumericEncoding.DATE_EPOCH_DAYS), dts));
  }

  public ArrayDateDoubleTimeSeries(final TimeZone timeZone, final DateDoubleTimeSeries dts) {
    super(new DateEpochDaysConverter(timeZone), new DateEpochDaysConverter(timeZone).convertToInt(new FastListIntDoubleTimeSeries(DateTimeNumericEncoding.DATE_EPOCH_DAYS), dts));
  }

  public ArrayDateDoubleTimeSeries(final FastIntDoubleTimeSeries pidts) {
    super(s_converter, pidts);
  }

  public ArrayDateDoubleTimeSeries(final TimeZone timeZone, final FastIntDoubleTimeSeries pidts) {
    super(new DateEpochDaysConverter(timeZone), pidts);
  }

  @Override
  public DateDoubleTimeSeries newInstanceFast(final Date[] dateTimes, final double[] values) {
    return new ArrayDateDoubleTimeSeries(dateTimes, values);
  }

}
