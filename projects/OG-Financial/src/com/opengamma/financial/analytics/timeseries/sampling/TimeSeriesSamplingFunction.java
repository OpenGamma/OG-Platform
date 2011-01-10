/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.timeseries.sampling;

import javax.time.calendar.LocalDate;

import com.opengamma.util.timeseries.DoubleTimeSeries;

/**
 * 
 */
public interface TimeSeriesSamplingFunction {

  DoubleTimeSeries<?> getSampledTimeSeries(final DoubleTimeSeries<?> ts, final LocalDate[] schedule);
}
