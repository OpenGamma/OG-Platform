/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.util.timeseries.localdate;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.time.calendar.TimeZone;

import javax.time.calendar.LocalDate;

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
public class ArrayLocalDateDoubleTimeSeries extends LocalDateDoubleTimeSeries.Integer {
  private static final DateTimeConverter<LocalDate> s_converter = new LocalDateEpochDaysConverter();

  public ArrayLocalDateDoubleTimeSeries() {
    super(new LocalDateEpochDaysConverter(), new FastArrayIntDoubleTimeSeries(DateTimeNumericEncoding.DATE_EPOCH_DAYS));
  }

  public ArrayLocalDateDoubleTimeSeries(final LocalDate[] dates, final double[] values) {
    super(s_converter, new FastArrayIntDoubleTimeSeries(DateTimeNumericEncoding.DATE_EPOCH_DAYS, s_converter.convertToInt(dates), values));
  }

  public ArrayLocalDateDoubleTimeSeries(final TimeZone timeZone, final LocalDate[] dates, final double[] values) {
    super(new LocalDateEpochDaysConverter(timeZone), new FastArrayIntDoubleTimeSeries(DateTimeNumericEncoding.DATE_EPOCH_DAYS, new LocalDateEpochDaysConverter(timeZone).convertToInt(dates),
        values));
  }

  public ArrayLocalDateDoubleTimeSeries(final List<LocalDate> dates, final List<Double> values) {
    super(s_converter, new FastArrayIntDoubleTimeSeries(DateTimeNumericEncoding.DATE_EPOCH_DAYS, s_converter.convertToInt(dates), values));
  }

  public ArrayLocalDateDoubleTimeSeries(final TimeZone timeZone, final List<LocalDate> dates, final List<Double> values) {
    super(new LocalDateEpochDaysConverter(timeZone), new FastArrayIntDoubleTimeSeries(DateTimeNumericEncoding.DATE_EPOCH_DAYS, new LocalDateEpochDaysConverter(timeZone).convertToInt(dates),
        values));
  }

  public ArrayLocalDateDoubleTimeSeries(final DoubleTimeSeries<LocalDate> dts) {
    super(s_converter, s_converter.convertToInt(new FastListIntDoubleTimeSeries(DateTimeNumericEncoding.DATE_EPOCH_DAYS), dts));
  }

  public ArrayLocalDateDoubleTimeSeries(final TimeZone timeZone, final LocalDateDoubleTimeSeries dts) {
    super(new LocalDateEpochDaysConverter(timeZone), new LocalDateEpochDaysConverter(timeZone).convertToInt(new FastListIntDoubleTimeSeries(DateTimeNumericEncoding.DATE_EPOCH_DAYS), dts));
  }

  public ArrayLocalDateDoubleTimeSeries(final FastIntDoubleTimeSeries pidts) {
    super(s_converter, pidts);
  }
  
  public ArrayLocalDateDoubleTimeSeries(final DateTimeConverter<LocalDate> converter, final FastIntDoubleTimeSeries pidts) {
    super(converter, pidts);
  }

  public ArrayLocalDateDoubleTimeSeries(final TimeZone timeZone, final FastIntDoubleTimeSeries pidts) {
    super(new LocalDateEpochDaysConverter(timeZone), pidts);
  }

  @Override
  public LocalDateDoubleTimeSeries newInstanceFast(final LocalDate[] dateTimes, final double[] values) {
    return new ArrayLocalDateDoubleTimeSeries(dateTimes, values);
  }

  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("ArrayLocalDateDoubleTimeSeries[");
    Iterator<Map.Entry<LocalDate, Double>> iterator = iterator();
    while (iterator.hasNext()) {
      Entry<LocalDate, Double> entry = iterator.next();
      sb.append("(");
      sb.append(entry.getKey());
      sb.append(", ");
      sb.append(entry.getValue());
      sb.append(")");
      if (iterator.hasNext()) {
        sb.append(", ");
      }
    }
    sb.append("]");
    return sb.toString();
  }

}
