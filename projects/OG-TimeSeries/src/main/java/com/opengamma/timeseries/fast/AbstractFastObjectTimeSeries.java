/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.timeseries.fast;

import com.opengamma.timeseries.FastBackedObjectTimeSeries;
import com.opengamma.timeseries.ObjectTimeSeries;
import com.opengamma.timeseries.ObjectTimeSeriesOperators;
import com.opengamma.timeseries.TimeSeriesUtils;
import com.opengamma.timeseries.ObjectTimeSeriesOperators.BinaryOperator;
import com.opengamma.timeseries.ObjectTimeSeriesOperators.UnaryOperator;
import com.opengamma.timeseries.fast.integer.object.FastIntObjectTimeSeries;
import com.opengamma.timeseries.fast.integer.object.FastMutableIntObjectTimeSeries;
import com.opengamma.timeseries.fast.longint.object.FastLongObjectTimeSeries;
import com.opengamma.timeseries.fast.longint.object.FastMutableLongObjectTimeSeries;

/**
 * 
 * @param <FAST_DATE_T> The type of the dates (a "fast" type, e.g. long, where the numbers are second from the epoch).
 * @param <T> The type of the data
 */
public abstract class AbstractFastObjectTimeSeries<FAST_DATE_T, T>
    implements ObjectTimeSeries<FAST_DATE_T, T>, FastObjectTimeSeries<FAST_DATE_T, T> {

  /** Serialization version. */
  private static final long serialVersionUID = 6527123435049806406L;

  public abstract DateTimeNumericEncoding getEncoding();

  public abstract DateTimeResolution getDateTimeResolution();

  public abstract FastObjectTimeSeries<FAST_DATE_T, T> operate(final UnaryOperator<T> operator);

  public abstract FastObjectTimeSeries<FAST_DATE_T, T> operate(final T other, final BinaryOperator<T> operator);

  public abstract FastObjectTimeSeries<FAST_DATE_T, T> operate(final FastLongObjectTimeSeries<T> other,
      final BinaryOperator<T> operator);

  public abstract FastObjectTimeSeries<FAST_DATE_T, T> operate(final FastIntObjectTimeSeries<T> other,
      final BinaryOperator<T> operator);

  @SuppressWarnings("unchecked")
  public FastObjectTimeSeries<FAST_DATE_T, T> operate(final FastBackedObjectTimeSeries<?, T> other,
      final BinaryOperator<T> operator) {
    FastObjectTimeSeries<?, T> fastSeries = other.getFastSeries();
    if (fastSeries instanceof FastIntObjectTimeSeries<?>) {
      return operate((FastIntObjectTimeSeries<T>) fastSeries, operator);
    } else { // if (fastSeries instanceof FastLongObjectTimeSeries
      return operate((FastLongObjectTimeSeries<T>) fastSeries, operator);
    }
  }

  public abstract FastObjectTimeSeries<FAST_DATE_T, T> unionOperate(final FastLongObjectTimeSeries<T> other,
      final BinaryOperator<T> operator);

  public abstract FastObjectTimeSeries<FAST_DATE_T, T> unionOperate(final FastIntObjectTimeSeries<T> other,
      final BinaryOperator<T> operator);

  @SuppressWarnings("unchecked")
  public FastObjectTimeSeries<FAST_DATE_T, T> unionOperate(final FastBackedObjectTimeSeries<?, T> other,
      final BinaryOperator<T> operator) {
    FastObjectTimeSeries<?, T> fastSeries = other.getFastSeries();
    if (fastSeries instanceof FastIntObjectTimeSeries<?>) {
      return unionOperate((FastIntObjectTimeSeries<T>) fastSeries, operator);
    } else { // if (fastSeries instanceof FastLongObjectTimeSeries
      return unionOperate((FastLongObjectTimeSeries<T>) fastSeries, operator);
    }
  }

  @SuppressWarnings("unchecked")
  public FastObjectTimeSeries<FAST_DATE_T, T> intersectionFirstValue(ObjectTimeSeries<?, T> other) {
    if (other instanceof FastBackedObjectTimeSeries<?, ?>) {
      return operate((FastBackedObjectTimeSeries<?, T>) other, ObjectTimeSeriesOperators.<T>firstOperator());
    } else if (other instanceof FastIntObjectTimeSeries) {
      return operate((FastIntObjectTimeSeries<T>) other, ObjectTimeSeriesOperators.<T>firstOperator());
    } else { // if (other instanceof FastLongDoubleTimeSeries) {
      return operate((FastLongObjectTimeSeries<T>) other, ObjectTimeSeriesOperators.<T>firstOperator());
    }
  }

  public FastObjectTimeSeries<FAST_DATE_T, T> intersectionFirstValue(FastBackedObjectTimeSeries<?, T> other) {
    return operate(other, ObjectTimeSeriesOperators.<T>firstOperator());
  }

  public FastObjectTimeSeries<FAST_DATE_T, T> intersectionFirstValue(FastLongObjectTimeSeries<T> other) {
    return operate(other, ObjectTimeSeriesOperators.<T>firstOperator());
  }

  public FastObjectTimeSeries<FAST_DATE_T, T> intersectionFirstValue(FastIntObjectTimeSeries<T> other) {
    return operate(other, ObjectTimeSeriesOperators.<T>firstOperator());
  }

  @SuppressWarnings("unchecked")
  public FastObjectTimeSeries<FAST_DATE_T, T> intersectionSecondValue(ObjectTimeSeries<?, T> other) {
    if (other instanceof FastBackedObjectTimeSeries<?, ?>) {
      return operate((FastBackedObjectTimeSeries<?, T>) other, ObjectTimeSeriesOperators.<T>secondOperator());
    } else if (other instanceof FastIntObjectTimeSeries<?>) {
      return operate((FastIntObjectTimeSeries<T>) other, ObjectTimeSeriesOperators.<T>secondOperator());
    } else { // if (other instanceof FastLongDoubleTimeSeries) {
      return operate((FastLongObjectTimeSeries<T>) other, ObjectTimeSeriesOperators.<T>secondOperator());
    }
  }

  public FastObjectTimeSeries<FAST_DATE_T, T> intersectionSecondValue(FastBackedObjectTimeSeries<?, T> other) {
    return operate(other, ObjectTimeSeriesOperators.<T>secondOperator());
  }

  public FastObjectTimeSeries<FAST_DATE_T, T> intersectionSecondValue(FastLongObjectTimeSeries<T> other) {
    return operate(other, ObjectTimeSeriesOperators.<T>secondOperator());
  }

  public FastObjectTimeSeries<FAST_DATE_T, T> intersectionSecondValue(FastIntObjectTimeSeries<T> other) {
    return operate(other, ObjectTimeSeriesOperators.<T>secondOperator());
  }

  public FastIntObjectTimeSeries<T> toFastIntDaysDTS() {
    return toFastIntObjectTimeSeries(DateTimeNumericEncoding.DATE_EPOCH_DAYS);
  }

  public FastMutableIntObjectTimeSeries<T> toFastMutableIntDaysDTS() {
    return toFastMutableIntObjectTimeSeries(DateTimeNumericEncoding.DATE_EPOCH_DAYS);
  }

  public FastLongObjectTimeSeries<T> toFastLongMillisDTS() {
    return toFastLongObjectTimeSeries(DateTimeNumericEncoding.TIME_EPOCH_MILLIS);
  }

  public FastMutableLongObjectTimeSeries<T> toFastMutableLongMillisDTS() {
    return toFastMutableLongObjectTimeSeries(DateTimeNumericEncoding.TIME_EPOCH_MILLIS);
  }

  //  @Override
  //  public LocalDateObjectTimeSeries toLocalDateObjectTimeSeries() {
  //    return new ArrayLocalDateObjectTimeSeries(toFastIntDaysDTS());
  //  }
  //
  //  @Override
  //  public LocalDateObjectTimeSeries toLocalDateObjectTimeSeries(ZoneId timeZone) {
  //    return new ArrayLocalDateObjectTimeSeries(timeZone, toFastIntDaysDTS());
  //  }    
  //  
  //  @Override
  //  public MutableLocalDateObjectTimeSeries toMutableLocalDateObjectTimeSeries() {
  //    return new ListLocalDateObjectTimeSeries(toFastMutableIntDaysDTS());
  //  }
  //
  //  @Override
  //  public MutableLocalDateObjectTimeSeries toMutableLocalDateObjectTimeSeries(ZoneId timeZone) {
  //    return new ListLocalDateObjectTimeSeries(timeZone, toFastMutableIntDaysDTS());
  //  }
  //  
  //  public MutableDateObjectTimeSeries toMutableDateObjectTimeSeries() {
  //    return new ListDateObjectTimeSeries(toFastMutableIntDaysDTS()); 
  //  }
  //  
  //  public MutableDateObjectTimeSeries toMutableDateObjectTimeSeries(TimeZone timeZone) {
  //    return new ListDateObjectTimeSeries(timeZone, toFastMutableIntDaysDTS());
  //  }
  //  
  //  public DateObjectTimeSeries toDateObjectTimeSeries() {
  //    return new ArrayDateObjectTimeSeries(toFastIntDaysDTS());    
  //  }
  //  
  //  public DateObjectTimeSeries toDateObjectTimeSeries(TimeZone timeZone) {
  //    return new ArrayDateObjectTimeSeries(timeZone, toFastIntDaysDTS());
  //  }
  //  
  //  @Override
  //  public SQLDateObjectTimeSeries toSQLDateObjectTimeSeries() {
  //    return new ArraySQLDateObjectTimeSeries(toFastIntDaysDTS());
  //  }
  //
  //  @Override
  //  public SQLDateObjectTimeSeries toSQLDateObjectTimeSeries(TimeZone timeZone) {
  //    return new ArraySQLDateObjectTimeSeries(timeZone, toFastIntDaysDTS());
  //  }
  //
  //  @Override
  //  public MutableSQLDateObjectTimeSeries toMutableSQLDateObjectTimeSeries() {
  //    return new ListSQLDateObjectTimeSeries(toFastMutableIntDaysDTS());
  //  }
  //
  //  @Override
  //  public MutableSQLDateObjectTimeSeries toMutableSQLDateObjectTimeSeries(TimeZone timeZone) {
  //    return new ListSQLDateObjectTimeSeries(timeZone, toFastMutableIntDaysDTS());
  //  }
  //  
  //  public MutableDateTimeObjectTimeSeries toMutableDateTimeObjectTimeSeries() {
  //    return new ListDateTimeObjectTimeSeries(this.toFastMutableLongMillisDTS()); 
  //  }
  //  
  //  public MutableDateTimeObjectTimeSeries toMutableDateTimeObjectTimeSeries(TimeZone timeZone) {
  //    return new ListDateTimeObjectTimeSeries(timeZone, toFastMutableLongMillisDTS());
  //  }
  //  
  //  public DateTimeObjectTimeSeries toDateTimeObjectTimeSeries() {
  //    return new ArrayDateTimeObjectTimeSeries(toFastLongMillisDTS()); 
  //  }
  //  
  //  public DateTimeObjectTimeSeries toDateTimeObjectTimeSeries(TimeZone timeZone) {
  //    return new ArrayDateTimeObjectTimeSeries(timeZone, toFastLongMillisDTS());
  //  }
  //  
  //  @Override
  //  public ZonedDateTimeObjectTimeSeries toZonedDateTimeObjectTimeSeries() {
  //    return new ArrayZonedDateTimeObjectTimeSeries(toFastLongMillisDTS());
  //  }
  //  
  //  @Override
  //  public ZonedDateTimeObjectTimeSeries toZonedDateTimeObjectTimeSeries(ZoneId timeZone) {
  //    return new ArrayZonedDateTimeObjectTimeSeries(timeZone,toFastLongMillisDTS());
  //  }
  //
  //  @Override
  //  public MutableZonedDateTimeObjectTimeSeries toMutableZonedDateTimeObjectTimeSeries() {
  //    return new ListZonedDateTimeObjectTimeSeries(toFastMutableLongMillisDTS());
  //  }
  //  
  //  @Override
  //  public MutableZonedDateTimeObjectTimeSeries toMutableZonedDateTimeObjectTimeSeries(ZoneId timeZone) {
  //    return new ListZonedDateTimeObjectTimeSeries(timeZone, toFastMutableLongMillisDTS());
  //  }
  //  
  //  @Override
  //  public YearOffsetObjectTimeSeries toYearOffsetObjectTimeSeries(ZonedDateTime zeroDate) {
  //    return new ArrayYearOffsetObjectTimeSeries(zeroDate, toFastLongMillisDTS());
  //  }
  //
  //  @Override
  //  public YearOffsetObjectTimeSeries toYearOffsetObjectTimeSeries(java.util.TimeZone timeZone, Date zeroDate) {
  //    return new ArrayYearOffsetObjectTimeSeries(timeZone, zeroDate, toFastLongMillisDTS());
  //  }
  //
  //  @Override
  //  public MutableYearOffsetObjectTimeSeries toMutableYearOffsetObjectTimeSeries(ZonedDateTime zeroDate) {
  //    return new ListYearOffsetObjectTimeSeries(zeroDate, toFastMutableLongMillisDTS());
  //  }
  //  
  //  @Override
  //  public MutableYearOffsetObjectTimeSeries toMutableYearOffsetObjectTimeSeries(java.util.TimeZone timeZone, Date zeroDate) {
  //    return new ListYearOffsetObjectTimeSeries(timeZone, zeroDate, toFastMutableLongMillisDTS());
  //  }

  @Override
  public String toString() {
    return TimeSeriesUtils.toString(this);
  }
}
