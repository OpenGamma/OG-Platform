/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.util.timeseries.localdate;

import java.util.List;
import javax.time.calendar.TimeZone;

import javax.time.calendar.LocalDate;

import com.opengamma.util.timeseries.DateTimeConverter;
import com.opengamma.util.timeseries.ObjectTimeSeries;
import com.opengamma.util.timeseries.fast.DateTimeNumericEncoding;
import com.opengamma.util.timeseries.fast.integer.object.FastArrayIntObjectTimeSeries;
import com.opengamma.util.timeseries.fast.integer.object.FastIntObjectTimeSeries;
import com.opengamma.util.timeseries.fast.integer.object.FastListIntObjectTimeSeries;

/**
 * @author jim
 * 
 */
public class ArrayLocalDateObjectTimeSeries<T> extends LocalDateObjectTimeSeries.Integer<T> {
  public static final ArrayLocalDateObjectTimeSeries<?> EMPTY_SERIES = new ArrayLocalDateObjectTimeSeries<Object>();
  private static final DateTimeConverter<LocalDate> s_converter = new LocalDateEpochDaysConverter();

  public ArrayLocalDateObjectTimeSeries() {
    super(new LocalDateEpochDaysConverter(), new FastArrayIntObjectTimeSeries<T>(DateTimeNumericEncoding.DATE_EPOCH_DAYS));
  }

  public ArrayLocalDateObjectTimeSeries(final LocalDate[] dates, final T[] values) {
    super(s_converter, new FastArrayIntObjectTimeSeries<T>(DateTimeNumericEncoding.DATE_EPOCH_DAYS, s_converter.convertToInt(dates), values));
  }

  public ArrayLocalDateObjectTimeSeries(final TimeZone timeZone, final LocalDate[] dates, final T[] values) {
    super(new LocalDateEpochDaysConverter(timeZone), new FastArrayIntObjectTimeSeries<T>(DateTimeNumericEncoding.DATE_EPOCH_DAYS, new LocalDateEpochDaysConverter(timeZone).convertToInt(dates),
        values));
  }

  public ArrayLocalDateObjectTimeSeries(final List<LocalDate> dates, final List<T> values) {
    super(s_converter, new FastArrayIntObjectTimeSeries<T>(DateTimeNumericEncoding.DATE_EPOCH_DAYS, s_converter.convertToInt(dates), values));
  }

  public ArrayLocalDateObjectTimeSeries(final TimeZone timeZone, final List<LocalDate> dates, final List<T> values) {
    super(new LocalDateEpochDaysConverter(timeZone), new FastArrayIntObjectTimeSeries<T>(DateTimeNumericEncoding.DATE_EPOCH_DAYS, new LocalDateEpochDaysConverter(timeZone).convertToInt(dates),
        values));
  }

  public ArrayLocalDateObjectTimeSeries(final ObjectTimeSeries<LocalDate, T> dts) {
    super(s_converter, s_converter.convertToInt(new FastListIntObjectTimeSeries<T>(DateTimeNumericEncoding.DATE_EPOCH_DAYS), dts));
  }

  @SuppressWarnings("unchecked")
  public ArrayLocalDateObjectTimeSeries(final TimeZone timeZone, final LocalDateObjectTimeSeries<T> dts) {
    super(new LocalDateEpochDaysConverter(timeZone), new LocalDateEpochDaysConverter(timeZone).convertToInt(new FastListIntObjectTimeSeries(DateTimeNumericEncoding.DATE_EPOCH_DAYS), dts));
  }

  public ArrayLocalDateObjectTimeSeries(final FastIntObjectTimeSeries<T> pidts) {
    super(s_converter, pidts);
  }

  public ArrayLocalDateObjectTimeSeries(final TimeZone timeZone, final FastIntObjectTimeSeries<T> pidts) {
    super(new LocalDateEpochDaysConverter(timeZone), pidts);
  }

  @Override
  public LocalDateObjectTimeSeries<T> newInstanceFast(final LocalDate[] dateTimes, final T[] values) {
    return new ArrayLocalDateObjectTimeSeries<T>(dateTimes, values);
  }


}
