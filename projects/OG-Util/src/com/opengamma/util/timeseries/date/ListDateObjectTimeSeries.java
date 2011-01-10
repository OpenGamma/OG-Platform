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
import com.opengamma.util.timeseries.fast.integer.object.FastIntObjectTimeSeries;
import com.opengamma.util.timeseries.fast.integer.object.FastListIntObjectTimeSeries;
import com.opengamma.util.timeseries.fast.integer.object.FastMutableIntObjectTimeSeries;

/**
 * 
 */
public class ListDateObjectTimeSeries<T> extends MutableDateObjectTimeSeries.Integer<T> {
  private static final FastIntObjectTimeSeries<Object> TIMESERIES_TEMPLATE = new FastListIntObjectTimeSeries<Object>(DateTimeNumericEncoding.DATE_EPOCH_DAYS);
  private static final DateTimeConverter<Date> s_converter = new DateEpochDaysConverter();

  protected ListDateObjectTimeSeries(final DateTimeConverter<Date> converter, final FastMutableIntObjectTimeSeries<T> fastTS) {
    super(converter, fastTS);
  }

  public ListDateObjectTimeSeries() {
    super(new DateEpochDaysConverter(), new FastListIntObjectTimeSeries<T>(DateTimeNumericEncoding.DATE_EPOCH_DAYS));
  }

  public ListDateObjectTimeSeries(final Date[] dates, final T[] values) {
    super(s_converter, new FastListIntObjectTimeSeries<T>(DateTimeNumericEncoding.DATE_EPOCH_DAYS, s_converter.convertToInt(dates), values));
  }

  public ListDateObjectTimeSeries(final TimeZone timeZone, final Date[] dates, final T[] values) {
    super(new DateEpochDaysConverter(timeZone), new FastListIntObjectTimeSeries<T>(DateTimeNumericEncoding.DATE_EPOCH_DAYS, new DateEpochDaysConverter(timeZone).convertToInt(dates),
        values));
  }

  public ListDateObjectTimeSeries(final List<Date> dates, final List<T> values) {
    super(s_converter, new FastListIntObjectTimeSeries<T>(DateTimeNumericEncoding.DATE_EPOCH_DAYS, s_converter.convertToInt(dates), values));
  }

  public ListDateObjectTimeSeries(final TimeZone timeZone, final List<Date> dates, final List<T> values) {
    super(new DateEpochDaysConverter(timeZone), new FastListIntObjectTimeSeries<T>(DateTimeNumericEncoding.DATE_EPOCH_DAYS, new DateEpochDaysConverter(timeZone).convertToInt(dates),
        values));
  }

  @SuppressWarnings("unchecked")
  public ListDateObjectTimeSeries(final ObjectTimeSeries<Date, T> dts) {
    super(s_converter, (FastMutableIntObjectTimeSeries<T>) s_converter.convertToInt((FastIntObjectTimeSeries<T>) TIMESERIES_TEMPLATE, dts));
  }

  @SuppressWarnings("unchecked")
  public ListDateObjectTimeSeries(final TimeZone timeZone, final DateObjectTimeSeries<T> dts) {
    super(new DateEpochDaysConverter(timeZone), (FastMutableIntObjectTimeSeries<T>) new DateEpochDaysConverter(timeZone).convertToInt((FastIntObjectTimeSeries<T>) TIMESERIES_TEMPLATE, dts));
  }

  public ListDateObjectTimeSeries(final FastMutableIntObjectTimeSeries<T> pmidts) {
    super(s_converter, pmidts);
  }

  public ListDateObjectTimeSeries(final TimeZone timeZone, final FastMutableIntObjectTimeSeries<T> pmidts) {
    super(new DateEpochDaysConverter(timeZone), pmidts);
  }

  @Override
  public DateObjectTimeSeries<T> newInstanceFast(final Date[] dateTimes, final T[] values) {
    return new ListDateObjectTimeSeries<T>(dateTimes, values);
  }

}
