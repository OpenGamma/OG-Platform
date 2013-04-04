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
import com.opengamma.timeseries.fast.integer.object.FastMapIntObjectTimeSeries;
import com.opengamma.timeseries.fast.integer.object.FastMutableIntObjectTimeSeries;

/**
 * Map-based implementation of {@code MutableLocalDateObjectTimeSeries}.
 * 
 * @param <T> The type of the time series
 */
public class MapLocalDateObjectTimeSeries<T> extends AbstractMutableLocalDateObjectTimeSeries<T> {

  /** Serialization version. */
  private static final long serialVersionUID = 8061489067205401915L;

  /** An empty time series */
  public static final MapLocalDateObjectTimeSeries<?> EMPTY_SERIES = new MapLocalDateObjectTimeSeries<Object>();
  /** Default converter. */
  private static final DateTimeConverter<LocalDate> CONVERTER = new LocalDateEpochDaysConverter();

  public MapLocalDateObjectTimeSeries() {
    super(new LocalDateEpochDaysConverter(), new FastMapIntObjectTimeSeries<T>(DateTimeNumericEncoding.DATE_EPOCH_DAYS));
  }

  public MapLocalDateObjectTimeSeries(final LocalDate[] dates, final T[] values) {
    super(CONVERTER, new FastMapIntObjectTimeSeries<T>(DateTimeNumericEncoding.DATE_EPOCH_DAYS,
        CONVERTER.convertToInt(dates), values));
  }

  public MapLocalDateObjectTimeSeries(final ZoneId timeZone, final LocalDate[] dates, final T[] values) {
    super(new LocalDateEpochDaysConverter(timeZone), new FastMapIntObjectTimeSeries<T>(
        DateTimeNumericEncoding.DATE_EPOCH_DAYS, new LocalDateEpochDaysConverter(timeZone).convertToInt(dates),
        values));
  }

  public MapLocalDateObjectTimeSeries(final List<LocalDate> dates, final List<T> values) {
    super(CONVERTER, new FastMapIntObjectTimeSeries<T>(DateTimeNumericEncoding.DATE_EPOCH_DAYS,
        CONVERTER.convertToInt(dates), values));
  }

  public MapLocalDateObjectTimeSeries(final ZoneId timeZone, final List<LocalDate> dates, final List<T> values) {
    super(new LocalDateEpochDaysConverter(timeZone), new FastMapIntObjectTimeSeries<T>(
        DateTimeNumericEncoding.DATE_EPOCH_DAYS, new LocalDateEpochDaysConverter(timeZone).convertToInt(dates),
        values));
  }

  public MapLocalDateObjectTimeSeries(final ObjectTimeSeries<LocalDate, T> dts) {
    super(CONVERTER, (FastMutableIntObjectTimeSeries<T>) CONVERTER.convertToInt(new FastMapIntObjectTimeSeries<T>(
        DateTimeNumericEncoding.DATE_EPOCH_DAYS), dts));
  }

  public MapLocalDateObjectTimeSeries(final ZoneId timeZone, final LocalDateObjectTimeSeries<T> dts) {
    super(new LocalDateEpochDaysConverter(timeZone),
        (FastMutableIntObjectTimeSeries<T>) new LocalDateEpochDaysConverter(timeZone).convertToInt(
            new FastMapIntObjectTimeSeries<T>(
                DateTimeNumericEncoding.DATE_EPOCH_DAYS), dts));
  }

  public MapLocalDateObjectTimeSeries(final FastMutableIntObjectTimeSeries<T> pmidts) {
    super(CONVERTER, pmidts);
  }

  public MapLocalDateObjectTimeSeries(final ZoneId timeZone, final FastMutableIntObjectTimeSeries<T> pmidts) {
    super(new LocalDateEpochDaysConverter(timeZone), pmidts);
  }

  @Override
  public MutableLocalDateObjectTimeSeries<T> newInstanceFast(final LocalDate[] dateTimes, final T[] values) {
    return new MapLocalDateObjectTimeSeries<T>(dateTimes, values);
  }
}
