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
import com.opengamma.timeseries.DoubleTimeSeries;
import com.opengamma.timeseries.fast.DateTimeNumericEncoding;
import com.opengamma.timeseries.fast.integer.FastMapIntDoubleTimeSeries;
import com.opengamma.timeseries.fast.integer.FastMutableIntDoubleTimeSeries;

/**
 * Map-based implementation of {@code MutableLocalDateDoubleTimeSeries}.
 */
public class MapLocalDateDoubleTimeSeries extends AbstractMutableLocalDateDoubleTimeSeries {

  /** Serialization version. */
  private static final long serialVersionUID = -1719524159253442838L;
  /** Default converter. */
  private static final DateTimeConverter<LocalDate> CONVERTER = new LocalDateEpochDaysConverter();

  public MapLocalDateDoubleTimeSeries() {
    super(new LocalDateEpochDaysConverter(), new FastMapIntDoubleTimeSeries(DateTimeNumericEncoding.DATE_EPOCH_DAYS));
  }

  public MapLocalDateDoubleTimeSeries(final LocalDate[] dates, final double[] values) {
    super(CONVERTER, new FastMapIntDoubleTimeSeries(DateTimeNumericEncoding.DATE_EPOCH_DAYS, CONVERTER.convertToInt(dates), values));
  }

  public MapLocalDateDoubleTimeSeries(final ZoneId timeZone, final LocalDate[] dates, final double[] values) {
    super(new LocalDateEpochDaysConverter(timeZone), new FastMapIntDoubleTimeSeries(DateTimeNumericEncoding.DATE_EPOCH_DAYS, new LocalDateEpochDaysConverter(timeZone).convertToInt(dates),
        values));
  }

  public MapLocalDateDoubleTimeSeries(final List<LocalDate> dates, final List<Double> values) {
    super(CONVERTER, new FastMapIntDoubleTimeSeries(DateTimeNumericEncoding.DATE_EPOCH_DAYS, CONVERTER.convertToInt(dates), values));
  }

  public MapLocalDateDoubleTimeSeries(final ZoneId timeZone, final List<LocalDate> dates, final List<Double> values) {
    super(new LocalDateEpochDaysConverter(timeZone), new FastMapIntDoubleTimeSeries(DateTimeNumericEncoding.DATE_EPOCH_DAYS, new LocalDateEpochDaysConverter(timeZone).convertToInt(dates),
        values));
  }

  public MapLocalDateDoubleTimeSeries(final DoubleTimeSeries<LocalDate> dts) {
    super(CONVERTER, (FastMutableIntDoubleTimeSeries) CONVERTER.convertToInt(new FastMapIntDoubleTimeSeries(DateTimeNumericEncoding.DATE_EPOCH_DAYS), dts));
  }

  public MapLocalDateDoubleTimeSeries(final ZoneId timeZone, final LocalDateDoubleTimeSeries dts) {
    super(new LocalDateEpochDaysConverter(timeZone), (FastMutableIntDoubleTimeSeries) new LocalDateEpochDaysConverter(timeZone).convertToInt(new FastMapIntDoubleTimeSeries(
        DateTimeNumericEncoding.DATE_EPOCH_DAYS), dts));
  }

  public MapLocalDateDoubleTimeSeries(final FastMutableIntDoubleTimeSeries pmidts) {
    super(CONVERTER, pmidts);
  }
  
  public MapLocalDateDoubleTimeSeries(final DateTimeConverter<LocalDate> converter, final FastMutableIntDoubleTimeSeries pmidts) {
    super(converter, pmidts);
  }

  public MapLocalDateDoubleTimeSeries(final ZoneId timeZone, final FastMutableIntDoubleTimeSeries pmidts) {
    super(new LocalDateEpochDaysConverter(timeZone), pmidts);
  }

  @Override
  public MutableLocalDateDoubleTimeSeries newInstanceFast(final LocalDate[] dateTimes, final double[] values) {
    return new MapLocalDateDoubleTimeSeries(dateTimes, values);
  }
}
