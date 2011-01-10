/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.util.timeseries.yearoffset;

import java.util.Date;
import java.util.List;

import javax.time.calendar.ZonedDateTime;

import com.opengamma.util.timeseries.DateTimeConverter;
import com.opengamma.util.timeseries.fast.DateTimeNumericEncoding;
import com.opengamma.util.timeseries.fast.longint.FastArrayLongDoubleTimeSeries;
import com.opengamma.util.timeseries.fast.longint.FastListLongDoubleTimeSeries;
import com.opengamma.util.timeseries.fast.longint.FastLongDoubleTimeSeries;

/**
 * @author jim
 * 
 */
public class ArrayYearOffsetDoubleTimeSeries extends YearOffsetDoubleTimeSeries.Long {
  private static final FastListLongDoubleTimeSeries DEFAULT_SERIES_TEMPLATE = new FastListLongDoubleTimeSeries(DateTimeNumericEncoding.TIME_EPOCH_MILLIS);

  private ArrayYearOffsetDoubleTimeSeries(final Date zeroDate) {
    super(new YearOffsetEpochMillisConverter(zeroDate), FastArrayLongDoubleTimeSeries.EMPTY_SERIES);
  }

  public ArrayYearOffsetDoubleTimeSeries(final ZonedDateTime zeroDate, final Double[] dates, final double[] values) {
    super(new YearOffsetEpochMillisConverter(zeroDate), 
          new FastArrayLongDoubleTimeSeries(DateTimeNumericEncoding.TIME_EPOCH_MILLIS, 
                                            new YearOffsetEpochMillisConverter(zeroDate).convertToLong(dates), 
                                            values));
  }

  public ArrayYearOffsetDoubleTimeSeries(final java.util.TimeZone timeZone, final Date zeroDate, final Double[] dates, final double[] values) {
    super(new YearOffsetEpochMillisConverter(timeZone, zeroDate), 
          new FastArrayLongDoubleTimeSeries(DateTimeNumericEncoding.TIME_EPOCH_MILLIS, 
                                            new YearOffsetEpochMillisConverter(timeZone, zeroDate).convertToLong(dates), 
                                            values));
  }

  public ArrayYearOffsetDoubleTimeSeries(final ZonedDateTime zeroDate, final List<Double> dates, final List<Double> values) {
    super(new YearOffsetEpochMillisConverter(zeroDate), 
          new FastArrayLongDoubleTimeSeries(DateTimeNumericEncoding.TIME_EPOCH_MILLIS, 
                                            new YearOffsetEpochMillisConverter(zeroDate).convertToLong(dates), 
                                            values));
  }

  public ArrayYearOffsetDoubleTimeSeries(final java.util.TimeZone timeZone, final Date zeroDate, final List<Double> dates, final List<Double> values) {
    super(new YearOffsetEpochMillisConverter(timeZone, zeroDate), 
          new FastArrayLongDoubleTimeSeries(DateTimeNumericEncoding.TIME_EPOCH_MILLIS, 
                                            new YearOffsetEpochMillisConverter(timeZone, zeroDate).convertToLong(dates), 
                                            values));
  }

  public ArrayYearOffsetDoubleTimeSeries(final ZonedDateTime zeroDate, final YearOffsetDoubleTimeSeries dts) {
    super(new YearOffsetEpochMillisConverter(zeroDate), 
          new YearOffsetEpochMillisConverter(zeroDate).convertToLong(DEFAULT_SERIES_TEMPLATE, dts));
  }

  public ArrayYearOffsetDoubleTimeSeries(final java.util.TimeZone timeZone, final Date zeroDate, final YearOffsetDoubleTimeSeries dts) {
    super(new YearOffsetEpochMillisConverter(timeZone, zeroDate), 
          new YearOffsetEpochMillisConverter(timeZone, zeroDate).convertToLong(DEFAULT_SERIES_TEMPLATE, dts));
  }

  public ArrayYearOffsetDoubleTimeSeries(final ZonedDateTime zeroDate, final FastLongDoubleTimeSeries pidts) {
    super(new YearOffsetEpochMillisConverter(zeroDate), pidts);
  }
  
  public ArrayYearOffsetDoubleTimeSeries(final DateTimeConverter<Double> converter, final FastLongDoubleTimeSeries pidts) {
    super(converter, pidts);
  }

  public ArrayYearOffsetDoubleTimeSeries(final java.util.TimeZone timeZone, final Date zeroDate, final FastLongDoubleTimeSeries pidts) {
    super(new YearOffsetEpochMillisConverter(timeZone, zeroDate), pidts);
  }

  @Override
  public YearOffsetDoubleTimeSeries newInstanceFast(final Double[] dateTimes, final double[] values) {
    return new ArrayYearOffsetDoubleTimeSeries(((YearOffsetEpochMillisConverter) getConverter()).getZonedOffset(), dateTimes, values);
  }


}
