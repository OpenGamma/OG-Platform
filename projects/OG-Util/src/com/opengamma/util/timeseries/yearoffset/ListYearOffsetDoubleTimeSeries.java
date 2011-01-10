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
import com.opengamma.util.timeseries.fast.longint.FastListLongDoubleTimeSeries;
import com.opengamma.util.timeseries.fast.longint.FastMutableLongDoubleTimeSeries;

/**
 * @author jim
 * 
 */
public class ListYearOffsetDoubleTimeSeries extends MutableYearOffsetDoubleTimeSeries.Long {

  private ListYearOffsetDoubleTimeSeries(final ZonedDateTime zeroDate) {
    super(new YearOffsetEpochMillisConverter(zeroDate), new FastListLongDoubleTimeSeries(DateTimeNumericEncoding.TIME_EPOCH_MILLIS));
  }

  public ListYearOffsetDoubleTimeSeries(final ZonedDateTime zeroDate, final Double[] dates, final double[] values) {
    super(new YearOffsetEpochMillisConverter(zeroDate), 
          new FastListLongDoubleTimeSeries(DateTimeNumericEncoding.TIME_EPOCH_MILLIS, 
                                           new YearOffsetEpochMillisConverter(zeroDate).convertToLong(dates), 
                                           values));
  }

  public ListYearOffsetDoubleTimeSeries(final java.util.TimeZone timeZone, final Date zeroDate, final Double[] dates, final double[] values) {
    super(new YearOffsetEpochMillisConverter(timeZone, zeroDate), new FastListLongDoubleTimeSeries(DateTimeNumericEncoding.TIME_EPOCH_MILLIS, new YearOffsetEpochMillisConverter(timeZone, zeroDate)
        .convertToLong(dates), values));
  }

  public ListYearOffsetDoubleTimeSeries(final ZonedDateTime zeroDate, final List<Double> dates, final List<Double> values) {
    super(new YearOffsetEpochMillisConverter(zeroDate), 
          new FastListLongDoubleTimeSeries(DateTimeNumericEncoding.TIME_EPOCH_MILLIS, 
                                           new YearOffsetEpochMillisConverter(zeroDate).convertToLong(dates), 
                                           values));
  }

  public ListYearOffsetDoubleTimeSeries(final java.util.TimeZone timeZone, final Date zeroDate, final List<Double> dates, final List<Double> values) {
    super(new YearOffsetEpochMillisConverter(timeZone, zeroDate), 
          new FastListLongDoubleTimeSeries(DateTimeNumericEncoding.TIME_EPOCH_MILLIS, 
                                           new YearOffsetEpochMillisConverter(timeZone, zeroDate).convertToLong(dates), 
                                           values));
  }

  // REVIEW jim 8-Mar-2010 -- we could probably just resuse the converter from dts - should be immutable...
  public ListYearOffsetDoubleTimeSeries(final YearOffsetDoubleTimeSeries dts) {
    super(new YearOffsetEpochMillisConverter(
                ((YearOffsetEpochMillisConverter) dts.getConverter()).getZonedOffset()),
                (FastMutableLongDoubleTimeSeries) new YearOffsetEpochMillisConverter(
                  ((YearOffsetEpochMillisConverter) dts.getConverter()).getZonedOffset()).convertToLong(
                    new FastListLongDoubleTimeSeries(DateTimeNumericEncoding.TIME_EPOCH_MILLIS), dts));
  }
  
  public ListYearOffsetDoubleTimeSeries(final ZonedDateTime zeroDate, final YearOffsetDoubleTimeSeries dts) {
    super(new YearOffsetEpochMillisConverter(zeroDate), 
          (FastMutableLongDoubleTimeSeries) new YearOffsetEpochMillisConverter(zeroDate).convertToLong(
            new FastListLongDoubleTimeSeries(DateTimeNumericEncoding.TIME_EPOCH_MILLIS), dts));
  }

  public ListYearOffsetDoubleTimeSeries(final java.util.TimeZone timeZone, final Date zeroDate, final YearOffsetDoubleTimeSeries dts) {
    super(new YearOffsetEpochMillisConverter(timeZone, zeroDate),  
          (FastMutableLongDoubleTimeSeries) (new YearOffsetEpochMillisConverter(timeZone, zeroDate).convertToLong(
             new FastListLongDoubleTimeSeries(DateTimeNumericEncoding.TIME_EPOCH_MILLIS), dts)
          ));
  }

  public ListYearOffsetDoubleTimeSeries(final ZonedDateTime zeroDate, final FastMutableLongDoubleTimeSeries pmidts) {
    super(new YearOffsetEpochMillisConverter(zeroDate), pmidts);
  }
  
  public ListYearOffsetDoubleTimeSeries(final DateTimeConverter<Double> converter, final FastMutableLongDoubleTimeSeries pmidts) {
    super(converter, pmidts);
  }
  
  public ListYearOffsetDoubleTimeSeries(final java.util.TimeZone timeZone, final Date zeroDate, final FastMutableLongDoubleTimeSeries pmidts) {
    super(new YearOffsetEpochMillisConverter(timeZone, zeroDate), pmidts);
  }

  @Override
  public YearOffsetDoubleTimeSeries newInstanceFast(final Double[] dateTimes, final double[] values) {
    return new ListYearOffsetDoubleTimeSeries(((YearOffsetEpochMillisConverter) getConverter()).getZonedOffset(), dateTimes, values);
  }
}
