/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.schedule;

import org.threeten.bp.LocalDate;

import com.opengamma.util.timeseries.DoubleTimeSeries;

/**
 * 
 */
public interface TimeSeriesSamplingFunction {

  DoubleTimeSeries<?> getSampledTimeSeries(final DoubleTimeSeries<?> ts, final LocalDate[] schedule);
}
