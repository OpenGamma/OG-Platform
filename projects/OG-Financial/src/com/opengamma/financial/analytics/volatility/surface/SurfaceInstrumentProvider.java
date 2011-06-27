/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.volatility.surface;

import javax.time.calendar.LocalDate;

import com.opengamma.id.Identifier;

/**
 * Provides instruments for each point on the surface
 * @param <X> The type of the x-axis values
 * @param <Y> The type of the y-axis values
 */
public interface SurfaceInstrumentProvider<X, Y> {

  Identifier getInstrument(X xAxis, Y yAxis);

  Identifier getInstrument(X xAxis, Y yAxis, LocalDate surfaceDate);
}
