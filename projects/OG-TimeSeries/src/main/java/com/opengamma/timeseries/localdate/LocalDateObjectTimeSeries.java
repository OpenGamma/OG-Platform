/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.timeseries.localdate;

import org.threeten.bp.LocalDate;

import com.opengamma.timeseries.FastBackedObjectTimeSeries;
import com.opengamma.timeseries.ObjectTimeSeries;

/**
 * A time series that stores {@code Object} data values against {@code LocalDate} dates.
 * <p>
 * The "time" key to the time-series is a {@code LocalDate}.
 * Some methods on the API represent the "time" as an {@code int}.
 * Some implementations may store the {@code int} rather than {@code LocalDate} internally.
 * The mapping between the two is available using {@link LocalDateToIntConverter}.
 * 
 * @param <T>  the type of the data
 */
public interface LocalDateObjectTimeSeries<T>
    extends ObjectTimeSeries<LocalDate, T>, FastBackedObjectTimeSeries<LocalDate, T> {

}
