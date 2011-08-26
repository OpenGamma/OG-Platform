/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.volatility.surface;

import javax.time.calendar.LocalDate;

import com.opengamma.id.ExternalId;

/**
 * Provides instruments for each point on the surface
 * @param <X> The type of the x-axis values
 * @param <Y> The type of the y-axis values
 */
public interface SurfaceInstrumentProvider<X, Y> {

  //TODO in general, each instrument type will need a different set of inputs - not sure how helpful this class actually is

  ExternalId getInstrument(X xAxis, Y yAxis);

  ExternalId getInstrument(X xAxis, Y yAxis, LocalDate surfaceDate);

  String getDataFieldName();
}
