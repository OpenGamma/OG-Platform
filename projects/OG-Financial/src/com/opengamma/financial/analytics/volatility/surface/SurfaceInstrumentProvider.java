/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.volatility.surface;

import com.opengamma.id.Identifier;

/**
 * Provides instruments for each point on the surface
 */
public interface SurfaceInstrumentProvider<X, Y> {
  public Identifier getInstrument(X startTenor, Y maturity);
}
