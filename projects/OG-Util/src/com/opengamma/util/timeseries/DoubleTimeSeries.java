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
  DoubleTimeSeries<DATE_TYPE> add(DoubleTimeSeries<?> other);

  DoubleTimeSeries<DATE_TYPE> add(FastBackedDoubleTimeSeries<?> other);

  DoubleTimeSeries<DATE_TYPE> add(FastIntDoubleTimeSeries other);

  DoubleTimeSeries<DATE_TYPE> add(FastLongDoubleTimeSeries other);

  DoubleTimeSeries<DATE_TYPE> add(double other);

  DoubleTimeSeries<DATE_TYPE> unionAdd(DoubleTimeSeries<?> other);

  DoubleTimeSeries<DATE_TYPE> unionAdd(FastBackedDoubleTimeSeries<?> other);

  DoubleTimeSeries<DATE_TYPE> unionAdd(FastIntDoubleTimeSeries other);

  DoubleTimeSeries<DATE_TYPE> unionAdd(FastLongDoubleTimeSeries other);

  DoubleTimeSeries<DATE_TYPE> subtract(DoubleTimeSeries<?> other);

  DoubleTimeSeries<DATE_TYPE> subtract(FastBackedDoubleTimeSeries<?> other);

  DoubleTimeSeries<DATE_TYPE> subtract(FastLongDoubleTimeSeries other);

  DoubleTimeSeries<DATE_TYPE> subtract(FastIntDoubleTimeSeries other);

  DoubleTimeSeries<DATE_TYPE> subtract(double other);

  DoubleTimeSeries<DATE_TYPE> unionSubtract(DoubleTimeSeries<?> other);

  DoubleTimeSeries<DATE_TYPE> unionSubtract(FastBackedDoubleTimeSeries<?> other);

  DoubleTimeSeries<DATE_TYPE> unionSubtract(FastIntDoubleTimeSeries other);

  DoubleTimeSeries<DATE_TYPE> unionSubtract(FastLongDoubleTimeSeries other);

  DoubleTimeSeries<DATE_TYPE> multiply(DoubleTimeSeries<?> other);

  DoubleTimeSeries<DATE_TYPE> multiply(FastBackedDoubleTimeSeries<?> other);

  DoubleTimeSeries<DATE_TYPE> multiply(FastIntDoubleTimeSeries other);

  DoubleTimeSeries<DATE_TYPE> multiply(FastLongDoubleTimeSeries other);

  DoubleTimeSeries<DATE_TYPE> multiply(double other);

  DoubleTimeSeries<DATE_TYPE> unionMultiply(DoubleTimeSeries<?> other);

  DoubleTimeSeries<DATE_TYPE> unionMultiply(FastBackedDoubleTimeSeries<?> other);

  DoubleTimeSeries<DATE_TYPE> unionMultiply(FastIntDoubleTimeSeries other);

  DoubleTimeSeries<DATE_TYPE> unionMultiply(FastLongDoubleTimeSeries other);

  DoubleTimeSeries<DATE_TYPE> divide(DoubleTimeSeries<?> other);

  DoubleTimeSeries<DATE_TYPE> divide(FastBackedDoubleTimeSeries<?> other);

  DoubleTimeSeries<DATE_TYPE> divide(FastIntDoubleTimeSeries other);

  DoubleTimeSeries<DATE_TYPE> divide(FastLongDoubleTimeSeries other);

  DoubleTimeSeries<DATE_TYPE> divide(double other);

  DoubleTimeSeries<DATE_TYPE> unionDivide(DoubleTimeSeries<?> other);

  DoubleTimeSeries<DATE_TYPE> unionDivide(FastBackedDoubleTimeSeries<?> other);

  DoubleTimeSeries<DATE_TYPE> unionDivide(FastIntDoubleTimeSeries other);

  DoubleTimeSeries<DATE_TYPE> unionDivide(FastLongDoubleTimeSeries other);

  DoubleTimeSeries<DATE_TYPE> power(DoubleTimeSeries<?> other);

  DoubleTimeSeries<DATE_TYPE> power(FastBackedDoubleTimeSeries<?> other);

  DoubleTimeSeries<DATE_TYPE> power(FastIntDoubleTimeSeries other);

  DoubleTimeSeries<DATE_TYPE> power(FastLongDoubleTimeSeries other);

  DoubleTimeSeries<DATE_TYPE> power(double other);

  DoubleTimeSeries<DATE_TYPE> unionPower(DoubleTimeSeries<?> other);

  DoubleTimeSeries<DATE_TYPE> unionPower(FastBackedDoubleTimeSeries<?> other);

  DoubleTimeSeries<DATE_TYPE> unionPower(FastIntDoubleTimeSeries other);

  DoubleTimeSeries<DATE_TYPE> unionPower(FastLongDoubleTimeSeries other);

  DoubleTimeSeries<DATE_TYPE> minimum(DoubleTimeSeries<?> other);

  DoubleTimeSeries<DATE_TYPE> minimum(FastBackedDoubleTimeSeries<?> other);

  DoubleTimeSeries<DATE_TYPE> minimum(FastIntDoubleTimeSeries other);

  DoubleTimeSeries<DATE_TYPE> minimum(FastLongDoubleTimeSeries other);

  DoubleTimeSeries<DATE_TYPE> minimum(double other);

  DoubleTimeSeries<DATE_TYPE> unionMinimum(DoubleTimeSeries<?> other);

  DoubleTimeSeries<DATE_TYPE> unionMinimum(FastBackedDoubleTimeSeries<?> other);

  DoubleTimeSeries<DATE_TYPE> unionMinimum(FastIntDoubleTimeSeries other);

  DoubleTimeSeries<DATE_TYPE> unionMinimum(FastLongDoubleTimeSeries other);

  DoubleTimeSeries<DATE_TYPE> maximum(DoubleTimeSeries<?> other);

  DoubleTimeSeries<DATE_TYPE> maximum(FastBackedDoubleTimeSeries<?> other);

  DoubleTimeSeries<DATE_TYPE> maximum(FastIntDoubleTimeSeries other);

  DoubleTimeSeries<DATE_TYPE> maximum(FastLongDoubleTimeSeries other);

  DoubleTimeSeries<DATE_TYPE> maximum(double other);

  DoubleTimeSeries<DATE_TYPE> unionMaximum(DoubleTimeSeries<?> other);

  DoubleTimeSeries<DATE_TYPE> unionMaximum(FastBackedDoubleTimeSeries<?> other);

  DoubleTimeSeries<DATE_TYPE> unionMaximum(FastIntDoubleTimeSeries other);

  DoubleTimeSeries<DATE_TYPE> unionMaximum(FastLongDoubleTimeSeries other);

  DoubleTimeSeries<DATE_TYPE> average(DoubleTimeSeries<?> other);

  DoubleTimeSeries<DATE_TYPE> average(FastBackedDoubleTimeSeries<?> other);

  DoubleTimeSeries<DATE_TYPE> average(FastIntDoubleTimeSeries other);

  DoubleTimeSeries<DATE_TYPE> average(FastLongDoubleTimeSeries other);

  DoubleTimeSeries<DATE_TYPE> average(double other);

  DoubleTimeSeries<DATE_TYPE> unionAverage(DoubleTimeSeries<?> other);

  DoubleTimeSeries<DATE_TYPE> unionAverage(FastBackedDoubleTimeSeries<?> other);

  DoubleTimeSeries<DATE_TYPE> unionAverage(FastIntDoubleTimeSeries other);

  DoubleTimeSeries<DATE_TYPE> unionAverage(FastLongDoubleTimeSeries other);

  DoubleTimeSeries<DATE_TYPE> intersectionFirstValue(DoubleTimeSeries<?> other);

  DoubleTimeSeries<DATE_TYPE> intersectionFirstValue(FastBackedDoubleTimeSeries<?> other);

  DoubleTimeSeries<DATE_TYPE> intersectionFirstValue(FastIntDoubleTimeSeries other);

  DoubleTimeSeries<DATE_TYPE> intersectionFirstValue(FastLongDoubleTimeSeries other);

  DoubleTimeSeries<DATE_TYPE> intersectionSecondValue(DoubleTimeSeries<?> other);

  DoubleTimeSeries<DATE_TYPE> intersectionSecondValue(FastBackedDoubleTimeSeries<?> other);

  DoubleTimeSeries<DATE_TYPE> intersectionSecondValue(FastIntDoubleTimeSeries other);

  DoubleTimeSeries<DATE_TYPE> intersectionSecondValue(FastLongDoubleTimeSeries other);

  DoubleTimeSeries<DATE_TYPE> noIntersectionOperation(DoubleTimeSeries<?> other);

  DoubleTimeSeries<DATE_TYPE> noIntersectionOperation(FastBackedDoubleTimeSeries<?> other);

  DoubleTimeSeries<DATE_TYPE> noIntersectionOperation(FastIntDoubleTimeSeries other);

  DoubleTimeSeries<DATE_TYPE> noIntersectionOperation(FastLongDoubleTimeSeries other);

  DoubleTimeSeries<DATE_TYPE> negate();

  DoubleTimeSeries<DATE_TYPE> reciprocal();

  DoubleTimeSeries<DATE_TYPE> log();

  DoubleTimeSeries<DATE_TYPE> log10();

  DoubleTimeSeries<DATE_TYPE> abs();

  double maxValue();

  double minValue();

  DoubleTimeSeries<DATE_TYPE> subSeries(DATE_TYPE startTime, boolean inclusiveStart, DATE_TYPE endTime,
      boolean exclusiveEnd);

  DoubleTimeSeries<DATE_TYPE> subSeries(DATE_TYPE startTime, DATE_TYPE endTime);

  DoubleTimeSeries<DATE_TYPE> head(int numItems);

  DoubleTimeSeries<DATE_TYPE> tail(int numItems);

  DoubleTimeSeries<DATE_TYPE> lag(final int days);

  // conversions
  FastMutableIntDoubleTimeSeries toFastMutableIntDoubleTimeSeries();

  FastIntDoubleTimeSeries toFastIntDoubleTimeSeries();

  FastMutableLongDoubleTimeSeries toFastMutableLongDoubleTimeSeries();

  FastLongDoubleTimeSeries toFastLongDoubleTimeSeries();

  FastMutableIntDoubleTimeSeries toFastMutableIntDoubleTimeSeries(DateTimeNumericEncoding encoding);

  FastIntDoubleTimeSeries toFastIntDoubleTimeSeries(DateTimeNumericEncoding encoding);

  FastMutableLongDoubleTimeSeries toFastMutableLongDoubleTimeSeries(DateTimeNumericEncoding encoding);

  FastLongDoubleTimeSeries toFastLongDoubleTimeSeries(DateTimeNumericEncoding encoding);

  MutableDateDoubleTimeSeries toMutableDateDoubleTimeSeries();

  MutableDateDoubleTimeSeries toMutableDateDoubleTimeSeries(TimeZone timeZone);

  DateDoubleTimeSeries toDateDoubleTimeSeries();

  DateDoubleTimeSeries toDateDoubleTimeSeries(TimeZone timeZone);

  MutableSQLDateDoubleTimeSeries toMutableSQLDateDoubleTimeSeries();

  MutableSQLDateDoubleTimeSeries toMutableSQLDateDoubleTimeSeries(TimeZone timeZone);

  SQLDateDoubleTimeSeries toSQLDateDoubleTimeSeries();

  SQLDateDoubleTimeSeries toSQLDateDoubleTimeSeries(TimeZone timeZone);

  MutableDateTimeDoubleTimeSeries toMutableDateTimeDoubleTimeSeries();

  MutableDateTimeDoubleTimeSeries toMutableDateTimeDoubleTimeSeries(TimeZone timeZone);

  DateTimeDoubleTimeSeries toDateTimeDoubleTimeSeries();

  DateTimeDoubleTimeSeries toDateTimeDoubleTimeSeries(TimeZone timeZone);

  ZonedDateTimeDoubleTimeSeries toZonedDateTimeDoubleTimeSeries();

  ZonedDateTimeDoubleTimeSeries toZonedDateTimeDoubleTimeSeries(javax.time.calendar.TimeZone timeZone);

  MutableZonedDateTimeDoubleTimeSeries toMutableZonedDateTimeDoubleTimeSeries();

  MutableZonedDateTimeDoubleTimeSeries toMutableZonedDateTimeDoubleTimeSeries(
      javax.time.calendar.TimeZone timeZone);

  LocalDateDoubleTimeSeries toLocalDateDoubleTimeSeries();

  LocalDateDoubleTimeSeries toLocalDateDoubleTimeSeries(javax.time.calendar.TimeZone timeZone);

  MutableLocalDateDoubleTimeSeries toMutableLocalDateDoubleTimeSeries();

  MutableLocalDateDoubleTimeSeries toMutableLocalDateDoubleTimeSeries(
      javax.time.calendar.TimeZone timeZone);

  YearOffsetDoubleTimeSeries toYearOffsetDoubleTimeSeries(ZonedDateTime zeroDate);

  YearOffsetDoubleTimeSeries toYearOffsetDoubleTimeSeries(java.util.TimeZone timeZone, Date zeroDate);

  MutableYearOffsetDoubleTimeSeries toMutableYearOffsetDoubleTimeSeries(ZonedDateTime zeroDate);

  MutableYearOffsetDoubleTimeSeries toMutableYearOffsetDoubleTimeSeries(java.util.TimeZone timeZone,
      Date zeroDate);

}
