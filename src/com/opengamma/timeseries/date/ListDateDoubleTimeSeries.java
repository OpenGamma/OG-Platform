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
import com.opengamma.timeseries.fast.integer.FastIntDoubleTimeSeries;
import com.opengamma.timeseries.fast.integer.FastListIntDoubleTimeSeries;
import com.opengamma.timeseries.fast.integer.FastMutableIntDoubleTimeSeries;

/**
 * @author jim
 * 
 */
public class ListDateDoubleTimeSeries extends MutableDateDoubleTimeSeries.Integer {
  public static final FastIntDoubleTimeSeries TIMESERIES_TEMPLATE = new FastListIntDoubleTimeSeries(DateTimeNumericEncoding.DATE_EPOCH_DAYS);
  public static final ListDateDoubleTimeSeries EMPTY_SERIES = new ListDateDoubleTimeSeries();
  private static final DateTimeConverter<Date> s_converter = new DateEpochDaysConverter();

  protected ListDateDoubleTimeSeries(final DateTimeConverter<Date> converter, final FastMutableIntDoubleTimeSeries fastTS) {
    super(converter, fastTS);
  }

  public ListDateDoubleTimeSeries() {
    super(new DateEpochDaysConverter(), new FastListIntDoubleTimeSeries(DateTimeNumericEncoding.DATE_EPOCH_DAYS));
  }

  public ListDateDoubleTimeSeries(final Date[] dates, final double[] values) {
    super(s_converter, new FastListIntDoubleTimeSeries(DateTimeNumericEncoding.DATE_EPOCH_DAYS, s_converter.convertToInt(dates), values));
  }

  public ListDateDoubleTimeSeries(final TimeZone timeZone, final Date[] dates, final double[] values) {
    super(new DateEpochDaysConverter(timeZone), new FastListIntDoubleTimeSeries(DateTimeNumericEncoding.DATE_EPOCH_DAYS, new DateEpochDaysConverter(timeZone).convertToInt(dates),
        values));
  }

  public ListDateDoubleTimeSeries(final List<Date> dates, final List<Double> values) {
    super(s_converter, new FastListIntDoubleTimeSeries(DateTimeNumericEncoding.DATE_EPOCH_DAYS, s_converter.convertToInt(dates), values));
  }

  public ListDateDoubleTimeSeries(final TimeZone timeZone, final List<Date> dates, final List<Double> values) {
    super(new DateEpochDaysConverter(timeZone), new FastListIntDoubleTimeSeries(DateTimeNumericEncoding.DATE_EPOCH_DAYS, new DateEpochDaysConverter(timeZone).convertToInt(dates),
        values));
  }

  public ListDateDoubleTimeSeries(final DateDoubleTimeSeries dts) {
    super(s_converter, (FastMutableIntDoubleTimeSeries) s_converter.convertToInt(TIMESERIES_TEMPLATE, dts));
  }

  public ListDateDoubleTimeSeries(final TimeZone timeZone, final DateDoubleTimeSeries dts) {
    super(new DateEpochDaysConverter(timeZone), (FastMutableIntDoubleTimeSeries) new DateEpochDaysConverter(timeZone).convertToInt(TIMESERIES_TEMPLATE, dts));
  }

  public ListDateDoubleTimeSeries(final FastMutableIntDoubleTimeSeries pmidts) {
    super(s_converter, pmidts);
  }

  public ListDateDoubleTimeSeries(final TimeZone timeZone, final FastMutableIntDoubleTimeSeries pmidts) {
    super(new DateEpochDaysConverter(timeZone), pmidts);
  }

  @Override
  public DateDoubleTimeSeries newInstanceFast(final Date[] dateTimes, final double[] values) {
    return new ListDateDoubleTimeSeries(dateTimes, values);
  }

}
