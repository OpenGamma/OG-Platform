/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.util.timeseries.localdate;

import java.util.List;
import javax.time.calendar.TimeZone;

import javax.time.calendar.LocalDate;

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
public class ListLocalDateDoubleTimeSeries extends MutableLocalDateDoubleTimeSeries.Integer {
  private static final FastIntDoubleTimeSeries TIMESERIES_TEMPLATE = new FastListIntDoubleTimeSeries(DateTimeNumericEncoding.DATE_EPOCH_DAYS);
  private static final DateTimeConverter<LocalDate> s_converter = new LocalDateEpochDaysConverter();

  public ListLocalDateDoubleTimeSeries(final DateTimeConverter<LocalDate> converter, final FastMutableIntDoubleTimeSeries fastTS) {
    super(converter, fastTS);
  }

  public ListLocalDateDoubleTimeSeries() {
    super(new LocalDateEpochDaysConverter(), new FastListIntDoubleTimeSeries(DateTimeNumericEncoding.DATE_EPOCH_DAYS));
  }

  public ListLocalDateDoubleTimeSeries(final LocalDate[] dates, final double[] values) {
    super(s_converter, new FastListIntDoubleTimeSeries(DateTimeNumericEncoding.DATE_EPOCH_DAYS, s_converter.convertToInt(dates), values));
  }

  public ListLocalDateDoubleTimeSeries(final TimeZone timeZone, final LocalDate[] dates, final double[] values) {
    super(new LocalDateEpochDaysConverter(timeZone), new FastListIntDoubleTimeSeries(DateTimeNumericEncoding.DATE_EPOCH_DAYS, new LocalDateEpochDaysConverter(timeZone).convertToInt(dates),
        values));
  }

  public ListLocalDateDoubleTimeSeries(final List<LocalDate> dates, final List<Double> values) {
    super(s_converter, new FastListIntDoubleTimeSeries(DateTimeNumericEncoding.DATE_EPOCH_DAYS, s_converter.convertToInt(dates), values));
  }

  public ListLocalDateDoubleTimeSeries(final TimeZone timeZone, final List<LocalDate> dates, final List<Double> values) {
    super(new LocalDateEpochDaysConverter(timeZone), new FastListIntDoubleTimeSeries(DateTimeNumericEncoding.DATE_EPOCH_DAYS, new LocalDateEpochDaysConverter(timeZone).convertToInt(dates),
        values));
  }

  public ListLocalDateDoubleTimeSeries(final DoubleTimeSeries<LocalDate> dts) {
    super(s_converter, (FastMutableIntDoubleTimeSeries) s_converter.convertToInt(TIMESERIES_TEMPLATE, dts));
  }

  public ListLocalDateDoubleTimeSeries(final TimeZone timeZone, final LocalDateDoubleTimeSeries dts) {
    super(new LocalDateEpochDaysConverter(timeZone), (FastMutableIntDoubleTimeSeries) new LocalDateEpochDaysConverter(timeZone).convertToInt(TIMESERIES_TEMPLATE, dts));
  }

  public ListLocalDateDoubleTimeSeries(final FastMutableIntDoubleTimeSeries pmidts) {
    super(s_converter, pmidts);
  }

  public ListLocalDateDoubleTimeSeries(final TimeZone timeZone, final FastMutableIntDoubleTimeSeries pmidts) {
    super(new LocalDateEpochDaysConverter(timeZone), pmidts);
  }

  @Override
  public LocalDateDoubleTimeSeries newInstanceFast(final LocalDate[] dateTimes, final double[] values) {
    return new ListLocalDateDoubleTimeSeries(dateTimes, values);
  }

  

}
