/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.timeseries;

import java.util.Date;
import java.util.TimeZone;

import javax.time.calendar.ZonedDateTime;

import com.opengamma.util.timeseries.date.DateDoubleTimeSeries;
import com.opengamma.util.timeseries.date.MutableDateDoubleTimeSeries;
import com.opengamma.util.timeseries.date.time.DateTimeDoubleTimeSeries;
import com.opengamma.util.timeseries.date.time.MutableDateTimeDoubleTimeSeries;
import com.opengamma.util.timeseries.fast.DateTimeNumericEncoding;
import com.opengamma.util.timeseries.fast.integer.FastIntDoubleTimeSeries;
import com.opengamma.util.timeseries.fast.integer.FastMutableIntDoubleTimeSeries;
import com.opengamma.util.timeseries.fast.longint.FastLongDoubleTimeSeries;
import com.opengamma.util.timeseries.fast.longint.FastMutableLongDoubleTimeSeries;
import com.opengamma.util.timeseries.localdate.LocalDateDoubleTimeSeries;
import com.opengamma.util.timeseries.localdate.MutableLocalDateDoubleTimeSeries;
import com.opengamma.util.timeseries.sqldate.MutableSQLDateDoubleTimeSeries;
import com.opengamma.util.timeseries.sqldate.SQLDateDoubleTimeSeries;
import com.opengamma.util.timeseries.yearoffset.MutableYearOffsetDoubleTimeSeries;
import com.opengamma.util.timeseries.yearoffset.YearOffsetDoubleTimeSeries;
import com.opengamma.util.timeseries.zoneddatetime.MutableZonedDateTimeDoubleTimeSeries;
import com.opengamma.util.timeseries.zoneddatetime.ZonedDateTimeDoubleTimeSeries;

/**
 * 
 * @param <DATE_TYPE> The type of the date keys for the time series
 */
public interface DoubleTimeSeries<DATE_TYPE> extends TimeSeries<DATE_TYPE, Double> {
  // CSOFF: Too much to do
  public DoubleTimeSeries<DATE_TYPE> add(DoubleTimeSeries<?> other);
  public DoubleTimeSeries<DATE_TYPE> add(FastBackedDoubleTimeSeries<?> other);
  public DoubleTimeSeries<DATE_TYPE> add(FastIntDoubleTimeSeries other);
  public DoubleTimeSeries<DATE_TYPE> add(FastLongDoubleTimeSeries other);
  public DoubleTimeSeries<DATE_TYPE> add(double other);
  public DoubleTimeSeries<DATE_TYPE> unionAdd(DoubleTimeSeries<?> other);
  public DoubleTimeSeries<DATE_TYPE> unionAdd(FastBackedDoubleTimeSeries<?> other);
  public DoubleTimeSeries<DATE_TYPE> unionAdd(FastIntDoubleTimeSeries other);
  public DoubleTimeSeries<DATE_TYPE> unionAdd(FastLongDoubleTimeSeries other);
  public DoubleTimeSeries<DATE_TYPE> subtract(DoubleTimeSeries<?> other);
  public DoubleTimeSeries<DATE_TYPE> subtract(FastBackedDoubleTimeSeries<?> other);
  public DoubleTimeSeries<DATE_TYPE> subtract(FastLongDoubleTimeSeries other);
  public DoubleTimeSeries<DATE_TYPE> subtract(FastIntDoubleTimeSeries other);
  public DoubleTimeSeries<DATE_TYPE> subtract(double other);
  public DoubleTimeSeries<DATE_TYPE> unionSubtract(DoubleTimeSeries<?> other);
  public DoubleTimeSeries<DATE_TYPE> unionSubtract(FastBackedDoubleTimeSeries<?> other);
  public DoubleTimeSeries<DATE_TYPE> unionSubtract(FastIntDoubleTimeSeries other);
  public DoubleTimeSeries<DATE_TYPE> unionSubtract(FastLongDoubleTimeSeries other);
  public DoubleTimeSeries<DATE_TYPE> multiply(DoubleTimeSeries<?> other);
  public DoubleTimeSeries<DATE_TYPE> multiply(FastBackedDoubleTimeSeries<?> other);
  public DoubleTimeSeries<DATE_TYPE> multiply(FastIntDoubleTimeSeries other);
  public DoubleTimeSeries<DATE_TYPE> multiply(FastLongDoubleTimeSeries other);
  public DoubleTimeSeries<DATE_TYPE> multiply(double other);
  public DoubleTimeSeries<DATE_TYPE> unionMultiply(DoubleTimeSeries<?> other);
  public DoubleTimeSeries<DATE_TYPE> unionMultiply(FastBackedDoubleTimeSeries<?> other);
  public DoubleTimeSeries<DATE_TYPE> unionMultiply(FastIntDoubleTimeSeries other);
  public DoubleTimeSeries<DATE_TYPE> unionMultiply(FastLongDoubleTimeSeries other);
  public DoubleTimeSeries<DATE_TYPE> divide(DoubleTimeSeries<?> other);
  public DoubleTimeSeries<DATE_TYPE> divide(FastBackedDoubleTimeSeries<?> other);
  public DoubleTimeSeries<DATE_TYPE> divide(FastIntDoubleTimeSeries other);
  public DoubleTimeSeries<DATE_TYPE> divide(FastLongDoubleTimeSeries other);
  public DoubleTimeSeries<DATE_TYPE> divide(double other);
  public DoubleTimeSeries<DATE_TYPE> unionDivide(DoubleTimeSeries<?> other);
  public DoubleTimeSeries<DATE_TYPE> unionDivide(FastBackedDoubleTimeSeries<?> other);
  public DoubleTimeSeries<DATE_TYPE> unionDivide(FastIntDoubleTimeSeries other);
  public DoubleTimeSeries<DATE_TYPE> unionDivide(FastLongDoubleTimeSeries other);
  public DoubleTimeSeries<DATE_TYPE> power(DoubleTimeSeries<?> other);
  public DoubleTimeSeries<DATE_TYPE> power(FastBackedDoubleTimeSeries<?> other);
  public DoubleTimeSeries<DATE_TYPE> power(FastIntDoubleTimeSeries other);
  public DoubleTimeSeries<DATE_TYPE> power(FastLongDoubleTimeSeries other);
  public DoubleTimeSeries<DATE_TYPE> power(double other);
  public DoubleTimeSeries<DATE_TYPE> unionPower(DoubleTimeSeries<?> other);
  public DoubleTimeSeries<DATE_TYPE> unionPower(FastBackedDoubleTimeSeries<?> other);
  public DoubleTimeSeries<DATE_TYPE> unionPower(FastIntDoubleTimeSeries other);
  public DoubleTimeSeries<DATE_TYPE> unionPower(FastLongDoubleTimeSeries other);
  public DoubleTimeSeries<DATE_TYPE> minimum(DoubleTimeSeries<?> other);
  public DoubleTimeSeries<DATE_TYPE> minimum(FastBackedDoubleTimeSeries<?> other);
  public DoubleTimeSeries<DATE_TYPE> minimum(FastIntDoubleTimeSeries other);
  public DoubleTimeSeries<DATE_TYPE> minimum(FastLongDoubleTimeSeries other);
  public DoubleTimeSeries<DATE_TYPE> minimum(double other);
  public DoubleTimeSeries<DATE_TYPE> unionMinimum(DoubleTimeSeries<?> other);
  public DoubleTimeSeries<DATE_TYPE> unionMinimum(FastBackedDoubleTimeSeries<?> other);
  public DoubleTimeSeries<DATE_TYPE> unionMinimum(FastIntDoubleTimeSeries other);
  public DoubleTimeSeries<DATE_TYPE> unionMinimum(FastLongDoubleTimeSeries other);
  public DoubleTimeSeries<DATE_TYPE> maximum(DoubleTimeSeries<?> other);
  public DoubleTimeSeries<DATE_TYPE> maximum(FastBackedDoubleTimeSeries<?> other);
  public DoubleTimeSeries<DATE_TYPE> maximum(FastIntDoubleTimeSeries other);
  public DoubleTimeSeries<DATE_TYPE> maximum(FastLongDoubleTimeSeries other);
  public DoubleTimeSeries<DATE_TYPE> maximum(double other);
  public DoubleTimeSeries<DATE_TYPE> unionMaximum(DoubleTimeSeries<?> other);
  public DoubleTimeSeries<DATE_TYPE> unionMaximum(FastBackedDoubleTimeSeries<?> other);
  public DoubleTimeSeries<DATE_TYPE> unionMaximum(FastIntDoubleTimeSeries other);
  public DoubleTimeSeries<DATE_TYPE> unionMaximum(FastLongDoubleTimeSeries other);
  public DoubleTimeSeries<DATE_TYPE> average(DoubleTimeSeries<?> other);
  public DoubleTimeSeries<DATE_TYPE> average(FastBackedDoubleTimeSeries<?> other);
  public DoubleTimeSeries<DATE_TYPE> average(FastIntDoubleTimeSeries other);
  public DoubleTimeSeries<DATE_TYPE> average(FastLongDoubleTimeSeries other);
  public DoubleTimeSeries<DATE_TYPE> average(double other);
  public DoubleTimeSeries<DATE_TYPE> unionAverage(DoubleTimeSeries<?> other);
  public DoubleTimeSeries<DATE_TYPE> unionAverage(FastBackedDoubleTimeSeries<?> other);
  public DoubleTimeSeries<DATE_TYPE> unionAverage(FastIntDoubleTimeSeries other);
  public DoubleTimeSeries<DATE_TYPE> unionAverage(FastLongDoubleTimeSeries other);
  
  public DoubleTimeSeries<DATE_TYPE> intersectionFirstValue(DoubleTimeSeries<?> other);
  public DoubleTimeSeries<DATE_TYPE> intersectionFirstValue(FastBackedDoubleTimeSeries<?> other);
  public DoubleTimeSeries<DATE_TYPE> intersectionFirstValue(FastIntDoubleTimeSeries other);
  public DoubleTimeSeries<DATE_TYPE> intersectionFirstValue(FastLongDoubleTimeSeries other);
  public DoubleTimeSeries<DATE_TYPE> intersectionSecondValue(DoubleTimeSeries<?> other);
  public DoubleTimeSeries<DATE_TYPE> intersectionSecondValue(FastBackedDoubleTimeSeries<?> other);
  public DoubleTimeSeries<DATE_TYPE> intersectionSecondValue(FastIntDoubleTimeSeries other);
  public DoubleTimeSeries<DATE_TYPE> intersectionSecondValue(FastLongDoubleTimeSeries other);
  
  public DoubleTimeSeries<DATE_TYPE> noIntersectionOperation(DoubleTimeSeries<?> other);
  public DoubleTimeSeries<DATE_TYPE> noIntersectionOperation(FastBackedDoubleTimeSeries<?> other);
  public DoubleTimeSeries<DATE_TYPE> noIntersectionOperation(FastIntDoubleTimeSeries other);
  public DoubleTimeSeries<DATE_TYPE> noIntersectionOperation(FastLongDoubleTimeSeries other);

  public DoubleTimeSeries<DATE_TYPE> negate();
  public DoubleTimeSeries<DATE_TYPE> reciprocal();
  public DoubleTimeSeries<DATE_TYPE> log();
  public DoubleTimeSeries<DATE_TYPE> log10();
  public DoubleTimeSeries<DATE_TYPE> abs();
  
  public double maxValue();
  public double minValue();
  
  public DoubleTimeSeries<DATE_TYPE> subSeries(DATE_TYPE startTime, boolean inclusiveStart, DATE_TYPE endTime, boolean exclusiveEnd);
  public DoubleTimeSeries<DATE_TYPE> subSeries(DATE_TYPE startTime, DATE_TYPE endTime);
  public DoubleTimeSeries<DATE_TYPE> head(int numItems);
  public DoubleTimeSeries<DATE_TYPE> tail(int numItems);
 
  public DoubleTimeSeries<DATE_TYPE> lag(final int days);
  
  // conversions
  public abstract FastMutableIntDoubleTimeSeries toFastMutableIntDoubleTimeSeries();
  
  public abstract FastIntDoubleTimeSeries toFastIntDoubleTimeSeries();
  
  public abstract FastMutableLongDoubleTimeSeries toFastMutableLongDoubleTimeSeries();
  
  public abstract FastLongDoubleTimeSeries toFastLongDoubleTimeSeries();

  public abstract FastMutableIntDoubleTimeSeries toFastMutableIntDoubleTimeSeries(DateTimeNumericEncoding encoding);
  
  public abstract FastIntDoubleTimeSeries toFastIntDoubleTimeSeries(DateTimeNumericEncoding encoding);
  
  public abstract FastMutableLongDoubleTimeSeries toFastMutableLongDoubleTimeSeries(DateTimeNumericEncoding encoding);
  
  public abstract FastLongDoubleTimeSeries toFastLongDoubleTimeSeries(DateTimeNumericEncoding encoding);
  
  public abstract MutableDateDoubleTimeSeries toMutableDateDoubleTimeSeries();
  
  public abstract MutableDateDoubleTimeSeries toMutableDateDoubleTimeSeries(TimeZone timeZone);
  
  public abstract DateDoubleTimeSeries toDateDoubleTimeSeries();
  
  public abstract DateDoubleTimeSeries toDateDoubleTimeSeries(TimeZone timeZone);
  
  public abstract MutableSQLDateDoubleTimeSeries toMutableSQLDateDoubleTimeSeries();
  
  public abstract MutableSQLDateDoubleTimeSeries toMutableSQLDateDoubleTimeSeries(TimeZone timeZone);
  
  public abstract SQLDateDoubleTimeSeries toSQLDateDoubleTimeSeries();
  
  public abstract SQLDateDoubleTimeSeries toSQLDateDoubleTimeSeries(TimeZone timeZone);
  
  public abstract MutableDateTimeDoubleTimeSeries toMutableDateTimeDoubleTimeSeries();
  
  public abstract MutableDateTimeDoubleTimeSeries toMutableDateTimeDoubleTimeSeries(TimeZone timeZone);
  
  public abstract DateTimeDoubleTimeSeries toDateTimeDoubleTimeSeries();
  
  public abstract DateTimeDoubleTimeSeries toDateTimeDoubleTimeSeries(TimeZone timeZone);  
  
  public abstract ZonedDateTimeDoubleTimeSeries toZonedDateTimeDoubleTimeSeries();
  
  public abstract ZonedDateTimeDoubleTimeSeries toZonedDateTimeDoubleTimeSeries(javax.time.calendar.TimeZone timeZone);
  
  public abstract MutableZonedDateTimeDoubleTimeSeries toMutableZonedDateTimeDoubleTimeSeries();
  
  public abstract MutableZonedDateTimeDoubleTimeSeries toMutableZonedDateTimeDoubleTimeSeries(javax.time.calendar.TimeZone timeZone);

  public abstract LocalDateDoubleTimeSeries toLocalDateDoubleTimeSeries();
  
  public abstract LocalDateDoubleTimeSeries toLocalDateDoubleTimeSeries(javax.time.calendar.TimeZone timeZone);
  
  public abstract MutableLocalDateDoubleTimeSeries toMutableLocalDateDoubleTimeSeries();
  
  public abstract MutableLocalDateDoubleTimeSeries toMutableLocalDateDoubleTimeSeries(javax.time.calendar.TimeZone timeZone);
  
  public abstract YearOffsetDoubleTimeSeries toYearOffsetDoubleTimeSeries(ZonedDateTime zeroDate);

  public abstract YearOffsetDoubleTimeSeries toYearOffsetDoubleTimeSeries(java.util.TimeZone timeZone, Date zeroDate);

  public abstract MutableYearOffsetDoubleTimeSeries toMutableYearOffsetDoubleTimeSeries(ZonedDateTime zeroDate);
  
  public abstract MutableYearOffsetDoubleTimeSeries toMutableYearOffsetDoubleTimeSeries(java.util.TimeZone timeZone, Date zeroDate);

  // CSON: Too much to do
}
