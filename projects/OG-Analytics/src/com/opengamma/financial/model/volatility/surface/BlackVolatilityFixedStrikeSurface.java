/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.volatility.surface;

import com.opengamma.financial.equity.variance.pricing.VarianceSwapStaticReplication.StrikeParameterization;
import com.opengamma.financial.model.volatility.BlackFormula;
import com.opengamma.math.surface.Surface;

/**
 * Like BlackVolatilitySurface, a surface that contains the Black (aka implied, aka lognormal) volatility  as a function of time to maturity and strike.
 * Used to differentiate from other parameterisations of the strike. In this one, the strikes are fixed absolute levels. When the underlying moves, the vol at any strike remains fixed.
 */
public class BlackVolatilityFixedStrikeSurface extends BlackVolatilitySurfaceOld {

  public BlackVolatilityFixedStrikeSurface(Surface<Double, Double, Double> surface) {
    super(surface, StrikeParameterization.STRIKE);
  }

  @Override
  public double getForwardPrice(final double expiry, final double strike, final double forward, boolean isCall) {
    double vol = getVolatility(expiry, strike);
    BlackFormula black = new BlackFormula(forward, strike, expiry, vol, null, isCall);
    return black.computePrice();
  }
}
