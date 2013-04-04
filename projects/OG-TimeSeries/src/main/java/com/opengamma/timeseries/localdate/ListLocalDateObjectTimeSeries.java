/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.timeseries.localdate;

import java.util.List;

import org.threeten.bp.LocalDate;
import org.threeten.bp.ZoneId;

import com.opengamma.timeseries.DateTimeConverter;
import com.opengamma.timeseries.ObjectTimeSeries;
import com.opengamma.timeseries.fast.DateTimeNumericEncoding;
import com.opengamma.timeseries.fast.integer.object.FastIntObjectTimeSeries;
import com.opengamma.timeseries.fast.integer.object.FastListIntObjectTimeSeries;
import com.opengamma.timeseries.fast.integer.object.FastMutableIntObjectTimeSeries;

/**
 * List-based implementation of {@code MutableLocalDateObjectTimeSeries}.
 * 
 * @param <T> The type of the data
 */
public class ListLocalDateObjectTimeSeries<T> extends AbstractMutableLocalDateObjectTimeSeries<T> {

  /** Serialization version. */
  private static final long serialVersionUID = 352644516800771296L;

  /** A template time series with date encoding */
  public static final FastIntObjectTimeSeries<?> TIMESERIES_TEMPLATE = new FastListIntObjectTimeSeries<Object>(
      DateTimeNumericEncoding.DATE_EPOCH_DAYS);
  /** An empty time series */
  public static final ListLocalDateObjectTimeSeries<?> EMPTY_SERIES = new ListLocalDateObjectTimeSeries<Object>();
  /** Default converter. */
  private static final DateTimeConverter<LocalDate> CONVERTER = new LocalDateEpochDaysConverter();

  protected ListLocalDateObjectTimeSeries(final DateTimeConverter<LocalDate> converter,
      final FastMutableIntObjectTimeSeries<T> fastTS) {
    super(converter, fastTS);
  }

  public ListLocalDateObjectTimeSeries() {
    super(new LocalDateEpochDaysConverter(),
        new FastListIntObjectTimeSeries<T>(DateTimeNumericEncoding.DATE_EPOCH_DAYS));
  }

  public ListLocalDateObjectTimeSeries(final LocalDate[] dates, final T[] values) {
    super(CONVERTER, new FastListIntObjectTimeSeries<T>(DateTimeNumericEncoding.DATE_EPOCH_DAYS,
        CONVERTER.convertToInt(dates), values));
  }

  public ListLocalDateObjectTimeSeries(final ZoneId timeZone, final LocalDate[] dates, final T[] values) {
    super(new LocalDateEpochDaysConverter(timeZone), new FastListIntObjectTimeSeries<T>(
        DateTimeNumericEncoding.DATE_EPOCH_DAYS, new LocalDateEpochDaysConverter(timeZone).convertToInt(dates),
        values));
  }

  public ListLocalDateObjectTimeSeries(final List<LocalDate> dates, final List<T> values) {
    super(CONVERTER, new FastListIntObjectTimeSeries<T>(DateTimeNumericEncoding.DATE_EPOCH_DAYS,
        CONVERTER.convertToInt(dates), values));
  }

  public ListLocalDateObjectTimeSeries(final ZoneId timeZone, final List<LocalDate> dates, final List<T> values) {
    super(new LocalDateEpochDaysConverter(timeZone), new FastListIntObjectTimeSeries<T>(
        DateTimeNumericEncoding.DATE_EPOCH_DAYS, new LocalDateEpochDaysConverter(timeZone).convertToInt(dates),
        values));
  }

  @SuppressWarnings("unchecked")
  public ListLocalDateObjectTimeSeries(final ObjectTimeSeries<LocalDate, T> dts) {
    super(CONVERTER, (FastMutableIntObjectTimeSeries<T>) CONVERTER.convertToInt(
        (FastIntObjectTimeSeries<T>) TIMESERIES_TEMPLATE, dts));
  }

  @SuppressWarnings("unchecked")
  public ListLocalDateObjectTimeSeries(final ZoneId timeZone, final LocalDateObjectTimeSeries<T> dts) {
    super(new LocalDateEpochDaysConverter(timeZone),
        (FastMutableIntObjectTimeSeries<T>) new LocalDateEpochDaysConverter(timeZone).convertToInt(
            (FastIntObjectTimeSeries<T>) TIMESERIES_TEMPLATE, dts));
  }

  public ListLocalDateObjectTimeSeries(final FastMutableIntObjectTimeSeries<T> pmidts) {
    super(CONVERTER, pmidts);
  }

  public ListLocalDateObjectTimeSeries(final ZoneId timeZone, final FastMutableIntObjectTimeSeries<T> pmidts) {
    super(new LocalDateEpochDaysConverter(timeZone), pmidts);
  }

  @Override
  public LocalDateObjectTimeSeries<T> newInstanceFast(final LocalDate[] dateTimes, final T[] values) {
    return new ListLocalDateObjectTimeSeries<T>(dateTimes, values);
  }

}
