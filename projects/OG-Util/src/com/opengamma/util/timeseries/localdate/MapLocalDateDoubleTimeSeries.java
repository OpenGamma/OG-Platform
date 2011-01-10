/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.util.timeseries.localdate;


import java.util.List;

import javax.time.calendar.LocalDate;
import javax.time.calendar.TimeZone;

import com.opengamma.util.timeseries.DateTimeConverter;
import com.opengamma.util.timeseries.DoubleTimeSeries;
import com.opengamma.util.timeseries.fast.DateTimeNumericEncoding;
import com.opengamma.util.timeseries.fast.integer.FastMapIntDoubleTimeSeries;
import com.opengamma.util.timeseries.fast.integer.FastMutableIntDoubleTimeSeries;

/**
 * @author jim
 * 
 */
public class MapLocalDateDoubleTimeSeries extends MutableLocalDateDoubleTimeSeries.Integer {
  private static final DateTimeConverter<LocalDate> s_converter = new LocalDateEpochDaysConverter();

  public MapLocalDateDoubleTimeSeries() {
    super(new LocalDateEpochDaysConverter(), new FastMapIntDoubleTimeSeries(DateTimeNumericEncoding.DATE_EPOCH_DAYS));
  }

  public MapLocalDateDoubleTimeSeries(final LocalDate[] dates, final double[] values) {
    super(s_converter, new FastMapIntDoubleTimeSeries(DateTimeNumericEncoding.DATE_EPOCH_DAYS, s_converter.convertToInt(dates), values));
  }

  public MapLocalDateDoubleTimeSeries(final TimeZone timeZone, final LocalDate[] dates, final double[] values) {
    super(new LocalDateEpochDaysConverter(timeZone), new FastMapIntDoubleTimeSeries(DateTimeNumericEncoding.DATE_EPOCH_DAYS, new LocalDateEpochDaysConverter(timeZone).convertToInt(dates),
        values));
  }

  public MapLocalDateDoubleTimeSeries(final List<LocalDate> dates, final List<Double> values) {
    super(s_converter, new FastMapIntDoubleTimeSeries(DateTimeNumericEncoding.DATE_EPOCH_DAYS, s_converter.convertToInt(dates), values));
  }

  public MapLocalDateDoubleTimeSeries(final TimeZone timeZone, final List<LocalDate> dates, final List<Double> values) {
    super(new LocalDateEpochDaysConverter(timeZone), new FastMapIntDoubleTimeSeries(DateTimeNumericEncoding.DATE_EPOCH_DAYS, new LocalDateEpochDaysConverter(timeZone).convertToInt(dates),
        values));
  }

  public MapLocalDateDoubleTimeSeries(final DoubleTimeSeries<LocalDate> dts) {
    super(s_converter, (FastMutableIntDoubleTimeSeries) s_converter.convertToInt(new FastMapIntDoubleTimeSeries(DateTimeNumericEncoding.DATE_EPOCH_DAYS), dts));
  }

  public MapLocalDateDoubleTimeSeries(final TimeZone timeZone, final LocalDateDoubleTimeSeries dts) {
    super(new LocalDateEpochDaysConverter(timeZone), (FastMutableIntDoubleTimeSeries) new LocalDateEpochDaysConverter(timeZone).convertToInt(new FastMapIntDoubleTimeSeries(
        DateTimeNumericEncoding.DATE_EPOCH_DAYS), dts));
  }

  public MapLocalDateDoubleTimeSeries(final FastMutableIntDoubleTimeSeries pmidts) {
    super(s_converter, pmidts);
  }
  
  public MapLocalDateDoubleTimeSeries(final DateTimeConverter<LocalDate> converter, final FastMutableIntDoubleTimeSeries pmidts) {
    super(converter, pmidts);
  }

  public MapLocalDateDoubleTimeSeries(final TimeZone timeZone, final FastMutableIntDoubleTimeSeries pmidts) {
    super(new LocalDateEpochDaysConverter(timeZone), pmidts);
  }

  @Override
  public MutableLocalDateDoubleTimeSeries newInstanceFast(final LocalDate[] dateTimes, final double[] values) {
    return new MapLocalDateDoubleTimeSeries(dateTimes, values);
  }
}
