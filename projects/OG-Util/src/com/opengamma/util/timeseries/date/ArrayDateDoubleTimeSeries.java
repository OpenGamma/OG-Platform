/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.util.timeseries.date;

import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import com.opengamma.util.timeseries.DateTimeConverter;
import com.opengamma.util.timeseries.DoubleTimeSeries;
import com.opengamma.util.timeseries.fast.DateTimeNumericEncoding;
import com.opengamma.util.timeseries.fast.integer.FastArrayIntDoubleTimeSeries;
import com.opengamma.util.timeseries.fast.integer.FastIntDoubleTimeSeries;
import com.opengamma.util.timeseries.fast.integer.FastListIntDoubleTimeSeries;

/**
 * @author jim
 * 
 */
public class ArrayDateDoubleTimeSeries extends DateDoubleTimeSeries.Integer {
  @SuppressWarnings("unused")
  private static final ArrayDateDoubleTimeSeries EMPTY_SERIES = new ArrayDateDoubleTimeSeries();
  private static final DateTimeConverter<Date> s_converter = new DateEpochDaysConverter();

  public ArrayDateDoubleTimeSeries() {
    super(new DateEpochDaysConverter(), new FastArrayIntDoubleTimeSeries(DateTimeNumericEncoding.DATE_EPOCH_DAYS));
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

  public ArrayDateDoubleTimeSeries(final DoubleTimeSeries<Date> dts) {
    super(s_converter, s_converter.convertToInt(new FastListIntDoubleTimeSeries(DateTimeNumericEncoding.DATE_EPOCH_DAYS), dts));
  }

  public ArrayDateDoubleTimeSeries(final TimeZone timeZone, final DateDoubleTimeSeries dts) {
    super(new DateEpochDaysConverter(timeZone), new DateEpochDaysConverter(timeZone).convertToInt(new FastListIntDoubleTimeSeries(DateTimeNumericEncoding.DATE_EPOCH_DAYS), dts));
  }

  public ArrayDateDoubleTimeSeries(final FastIntDoubleTimeSeries pidts) {
    super(s_converter, pidts);
  }
  
  public ArrayDateDoubleTimeSeries(final DateTimeConverter<Date> converter, final FastIntDoubleTimeSeries timeSeries) {
    super(converter, timeSeries);
  }

  public ArrayDateDoubleTimeSeries(final TimeZone timeZone, final FastIntDoubleTimeSeries pidts) {
    super(new DateEpochDaysConverter(timeZone), pidts);
  }

  @Override
  public DateDoubleTimeSeries newInstanceFast(final Date[] dateTimes, final double[] values) {
    return new ArrayDateDoubleTimeSeries(dateTimes, values);
  }


}
