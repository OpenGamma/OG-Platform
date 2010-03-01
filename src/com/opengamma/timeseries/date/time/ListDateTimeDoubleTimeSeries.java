/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.timeseries.date.time;

import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import com.opengamma.timeseries.DateTimeConverter;
import com.opengamma.timeseries.date.DateEpochDaysConverter;
import com.opengamma.timeseries.fast.DateTimeNumericEncoding;
import com.opengamma.timeseries.fast.longint.FastListLongDoubleTimeSeries;
import com.opengamma.timeseries.fast.longint.FastMutableLongDoubleTimeSeries;

/**
 * @author jim
 * 
 */
public class ListDateTimeDoubleTimeSeries extends MutableDateTimeDoubleTimeSeries.Long {
  public static final ListDateTimeDoubleTimeSeries EMPTY_SERIES = new ListDateTimeDoubleTimeSeries();
  private static final DateTimeConverter<Date> s_converter = new DateEpochMillisConverter();

  private ListDateTimeDoubleTimeSeries() {
    super(new DateEpochMillisConverter(), new FastListLongDoubleTimeSeries(DateTimeNumericEncoding.TIME_EPOCH_MILLIS));
  }

  public ListDateTimeDoubleTimeSeries(final Date[] dates, final double[] values) {
    super(s_converter, new FastListLongDoubleTimeSeries(DateTimeNumericEncoding.TIME_EPOCH_MILLIS, s_converter.convertToLong(dates), values));
  }

  public ListDateTimeDoubleTimeSeries(final TimeZone timeZone, final Date[] dates, final double[] values) {
    super(new DateEpochMillisConverter(timeZone), new FastListLongDoubleTimeSeries(DateTimeNumericEncoding.TIME_EPOCH_MILLIS, new DateEpochMillisConverter(timeZone)
        .convertToLong(dates), values));
  }

  public ListDateTimeDoubleTimeSeries(final List<Date> dates, final List<Double> values) {
    super(s_converter, new FastListLongDoubleTimeSeries(DateTimeNumericEncoding.TIME_EPOCH_MILLIS, s_converter.convertToLong(dates), values));
  }

  public ListDateTimeDoubleTimeSeries(final TimeZone timeZone, final List<Date> dates, final List<Double> values) {
    super(new DateEpochMillisConverter(timeZone), new FastListLongDoubleTimeSeries(DateTimeNumericEncoding.TIME_EPOCH_MILLIS, new DateEpochMillisConverter(timeZone)
        .convertToLong(dates), values));
  }

  public ListDateTimeDoubleTimeSeries(final DateTimeDoubleTimeSeries dts) {
    super(s_converter, (FastMutableLongDoubleTimeSeries) s_converter.convertToLong(new FastListLongDoubleTimeSeries(DateTimeNumericEncoding.TIME_EPOCH_MILLIS), dts));
  }

  public ListDateTimeDoubleTimeSeries(final TimeZone timeZone, final DateTimeDoubleTimeSeries dts) {
    super(new DateEpochMillisConverter(timeZone), (FastMutableLongDoubleTimeSeries) new DateEpochMillisConverter(timeZone).convertToLong(new FastListLongDoubleTimeSeries(
        DateTimeNumericEncoding.TIME_EPOCH_MILLIS), dts));
  }

  public ListDateTimeDoubleTimeSeries(final FastMutableLongDoubleTimeSeries pmidts) {
    super(s_converter, pmidts);
  }

  public ListDateTimeDoubleTimeSeries(final TimeZone timeZone, final FastMutableLongDoubleTimeSeries pmidts) {
    super(new DateEpochDaysConverter(timeZone), pmidts);
  }

  @Override
  public DateTimeDoubleTimeSeries newInstanceFast(final Date[] dateTimes, final double[] values) {
    return new ListDateTimeDoubleTimeSeries(((DateEpochMillisConverter) getConverter()).getTimeZone(), dateTimes, values);
  }
}
