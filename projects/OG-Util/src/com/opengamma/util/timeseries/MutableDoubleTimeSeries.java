/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.timeseries;

/**
 * A mutable time-series, which represents the changes in a {@code double} value over time.
 * <p>
 * This interface is similar to both a {@code SortedMap} of value keyed by date-time
 * and a {@code List} of date-time to {@code double} value pairs.
 * As such, the date/times do not have to be evenly spread over time within the series.
 * 
 * @param <T> the date-time type, such as {@code Instant} or {@code LocalDate}
 */
public interface MutableDoubleTimeSeries<T> extends MutableTimeSeries<T, Double>, DoubleTimeSeries<T> {

}
