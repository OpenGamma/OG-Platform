/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.util.timeseries.yearoffset;

import java.util.Date;
import java.util.List;

import javax.time.calendar.ZonedDateTime;

import com.opengamma.util.timeseries.DateTimeConverter;
import com.opengamma.util.timeseries.fast.DateTimeNumericEncoding;
import com.opengamma.util.timeseries.fast.longint.FastMapLongDoubleTimeSeries;
import com.opengamma.util.timeseries.fast.longint.FastMutableLongDoubleTimeSeries;

/**
 * @author jim
 * 
 */
public class MapYearOffsetDoubleTimeSeries extends MutableYearOffsetDoubleTimeSeries.Long {

  public MapYearOffsetDoubleTimeSeries(final ZonedDateTime zeroDate) {
    super(new YearOffsetEpochMillisConverter(zeroDate), 
          new FastMapLongDoubleTimeSeries(DateTimeNumericEncoding.TIME_EPOCH_MILLIS));
  }
  
  public MapYearOffsetDoubleTimeSeries(final java.util.TimeZone timeZone, final Date zeroDate) {
    super(new YearOffsetEpochMillisConverter(timeZone, zeroDate), 
          new FastMapLongDoubleTimeSeries(DateTimeNumericEncoding.TIME_EPOCH_MILLIS));
  }
  
  public MapYearOffsetDoubleTimeSeries(final ZonedDateTime zeroDate, final Double[] dates, final double[] values) {
    super(new YearOffsetEpochMillisConverter(zeroDate), 
          new FastMapLongDoubleTimeSeries(DateTimeNumericEncoding.TIME_EPOCH_MILLIS, 
                                          new YearOffsetEpochMillisConverter(zeroDate).convertToLong(dates), 
                                          values));
  }

  public MapYearOffsetDoubleTimeSeries(final java.util.TimeZone timeZone, Date zeroDate, final Double[] dates, final double[] values) {
    super(new YearOffsetEpochMillisConverter(timeZone, zeroDate), 
          new FastMapLongDoubleTimeSeries(DateTimeNumericEncoding.TIME_EPOCH_MILLIS, 
                                          new YearOffsetEpochMillisConverter(timeZone, zeroDate).convertToLong(dates), 
                                          values));
  }

  public MapYearOffsetDoubleTimeSeries(final ZonedDateTime zeroDate, final List<Double> dates, final List<Double> values) {
    super(new YearOffsetEpochMillisConverter(zeroDate), 
          new FastMapLongDoubleTimeSeries(DateTimeNumericEncoding.TIME_EPOCH_MILLIS, 
                                          new YearOffsetEpochMillisConverter(zeroDate).convertToLong(dates), 
                                          values));
  }

  public MapYearOffsetDoubleTimeSeries(final java.util.TimeZone timeZone, final Date zeroDate, final List<Double> dates, final List<Double> values) {
    super(new YearOffsetEpochMillisConverter(timeZone, zeroDate), 
          new FastMapLongDoubleTimeSeries(DateTimeNumericEncoding.TIME_EPOCH_MILLIS, 
                                          new YearOffsetEpochMillisConverter(timeZone, zeroDate).convertToLong(dates), 
                                          values));
  }

  // convenience method.
  public MapYearOffsetDoubleTimeSeries(final YearOffsetDoubleTimeSeries dts) {
    super(new YearOffsetEpochMillisConverter(
            ((YearOffsetEpochMillisConverter) dts.getConverter()).getZonedOffset()), 
            (FastMutableLongDoubleTimeSeries) (new YearOffsetEpochMillisConverter(
                ((YearOffsetEpochMillisConverter) dts.getConverter()).getZonedOffset()).convertToLong(
                    new FastMapLongDoubleTimeSeries(DateTimeNumericEncoding.TIME_EPOCH_MILLIS), dts)));
  }

  public MapYearOffsetDoubleTimeSeries(final ZonedDateTime zeroDate, final YearOffsetDoubleTimeSeries dts) {
    super(new YearOffsetEpochMillisConverter(zeroDate), 
          (FastMutableLongDoubleTimeSeries) new YearOffsetEpochMillisConverter(zeroDate).convertToLong(
              new FastMapLongDoubleTimeSeries(DateTimeNumericEncoding.TIME_EPOCH_MILLIS), dts
          ));
  }
  
  public MapYearOffsetDoubleTimeSeries(final java.util.TimeZone timeZone, final Date zeroDate, final YearOffsetDoubleTimeSeries dts) {
    super(new YearOffsetEpochMillisConverter(timeZone, zeroDate), 
          (FastMutableLongDoubleTimeSeries) new YearOffsetEpochMillisConverter(timeZone, zeroDate).convertToLong(
              new FastMapLongDoubleTimeSeries(DateTimeNumericEncoding.TIME_EPOCH_MILLIS), dts));
  }

  public MapYearOffsetDoubleTimeSeries(final ZonedDateTime zeroDate, final FastMutableLongDoubleTimeSeries pmidts) {
    super(new YearOffsetEpochMillisConverter(zeroDate), pmidts);
  }
  
  public MapYearOffsetDoubleTimeSeries(final DateTimeConverter<Double> converter, final FastMutableLongDoubleTimeSeries pmidts) {
    super(converter, pmidts);
  }

  public MapYearOffsetDoubleTimeSeries(final java.util.TimeZone timeZone, final Date zeroDate, final FastMutableLongDoubleTimeSeries pmidts) {
    super(new YearOffsetEpochMillisConverter(timeZone, zeroDate), pmidts);
  }

  @Override
  public MapYearOffsetDoubleTimeSeries newInstanceFast(final Double[] dateTimes, final double[] values) {
    return new MapYearOffsetDoubleTimeSeries(((YearOffsetEpochMillisConverter) getConverter()).getZonedOffset(), dateTimes, values);
  }

}
