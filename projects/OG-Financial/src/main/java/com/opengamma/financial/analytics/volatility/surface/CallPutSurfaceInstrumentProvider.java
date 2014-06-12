/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.volatility.surface;

import com.opengamma.financial.convention.expirycalc.ExchangeTradedInstrumentExpiryCalculator;

/**
 * Provides instruments for each point on the surface and provides a strike value above which calls are used
 * @param <X> The type of the x-axis values
 * @param <Y> The type of the y-axis values
 */
public interface CallPutSurfaceInstrumentProvider<X, Y> extends SurfaceInstrumentProvider<X, Y> {

  Double useCallAboveStrike();
  
  ExchangeTradedInstrumentExpiryCalculator getExpiryRuleCalculator();
}
