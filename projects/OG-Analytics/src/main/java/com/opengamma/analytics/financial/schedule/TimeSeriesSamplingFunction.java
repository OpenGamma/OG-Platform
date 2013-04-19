/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.schedule;

import org.threeten.bp.LocalDate;

import com.opengamma.timeseries.date.DateDoubleTimeSeries;
import com.opengamma.timeseries.date.localdate.LocalDateDoubleTimeSeries;

/**
 * 
 */
public interface TimeSeriesSamplingFunction {

  LocalDateDoubleTimeSeries getSampledTimeSeries(final DateDoubleTimeSeries<?> ts, final LocalDate[] schedule);

}
