/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.volatility.surface;

import com.opengamma.id.Identifier;

/**
 * Provides instruments for each point on the surface
 * @param <X> The type of the start tenor 
 * @param <Y> The type of the maturity tenor
 */
public interface SurfaceInstrumentProvider<X, Y> {
  
  Identifier getInstrument(X startTenor, Y maturity);
}
