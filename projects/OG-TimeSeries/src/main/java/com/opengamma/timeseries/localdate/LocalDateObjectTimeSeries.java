/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.timeseries.localdate;

import org.threeten.bp.LocalDate;

import com.opengamma.timeseries.FastBackedObjectTimeSeries;
import com.opengamma.timeseries.date.DateObjectTimeSeries;

/**
 * A time series that stores {@code Object} data values against {@code LocalDate} dates.
 * <p>
 * The "time" key to the time-series is a {@code LocalDate}.
 * See {@link DateObjectTimeSeries} for details about the "time" represented as an {@code int}.
 * 
 * @param <V>  the value being viewed over time
 */
public interface LocalDateObjectTimeSeries<V>
    extends DateObjectTimeSeries<LocalDate, V>, FastBackedObjectTimeSeries<LocalDate, V> {

  LocalDateObjectTimeSeries<V> lag(final int lagCount);

}
