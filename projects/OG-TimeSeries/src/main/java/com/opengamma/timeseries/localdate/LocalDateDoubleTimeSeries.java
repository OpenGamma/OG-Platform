/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.timeseries.localdate;

import org.threeten.bp.LocalDate;

import com.opengamma.timeseries.DoubleTimeSeries;
import com.opengamma.timeseries.DoubleTimeSeriesOperators.UnaryOperator;
import com.opengamma.timeseries.FastBackedDoubleTimeSeries;

/**
 * A time series that stores {@code double} data values against {@code LocalDate} dates.
 * <p>
 * The "time" key to the time-series is a {@code LocalDate}.
 * Some methods on the API represent the "time" as an {@code int}.
 * Some implementations may store the {@code int} rather than {@code LocalDate} internally.
 * The mapping between the two is available using {@link LocalDateToIntConverter}.
 */
public interface LocalDateDoubleTimeSeries
    extends DoubleTimeSeries<LocalDate>, FastBackedDoubleTimeSeries<LocalDate> {

  /**
   * Gets an iterator over the date-value pairs.
   * <p>
   * Although the pairs are expressed as instances of {@code Map.Entry},
   * it is recommended to use the primitive methods on {@code LocalDateDoubleIterator}.
   * 
   * @return the iterator, not null
   */
  LocalDateDoubleIterator iterator();

  /**
   * Gets the value associated with the time, specifying the primitive {@code int} date.
   * 
   * @param date  the primitive date equivalent to a {@code LocalDate}
   * @return the matching value, null if there is no value for the date
   */
  Double getValue(int date);

  //-------------------------------------------------------------------------
  @Override  // override for covariant return type
  LocalDateDoubleTimeSeries subSeries(LocalDate startTime, boolean includeStart, LocalDate endTime, boolean includeEnd);

  @Override  // override for covariant return type
  LocalDateDoubleTimeSeries subSeries(LocalDate startTime, LocalDate endTime);

  @Override  // override for covariant return type
  LocalDateDoubleTimeSeries head(int numItems);

  @Override  // override for covariant return type
  LocalDateDoubleTimeSeries tail(int numItems);

  @Override  // override for covariant return type
  LocalDateDoubleTimeSeries lag(final int lagCount);

  @Override  // override for covariant return type
  LocalDateDoubleTimeSeries operate(UnaryOperator operator);

}
