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
 * A surface that contains the Black (implied) volatility  as a function of time to maturity and strike.
 * Also known as Lognormal Vol.
 * @deprecated use BlackVolatilitySurface
 */
@Deprecated
public class BlackVolatilitySurfaceOld extends VolatilitySurface {

  //TODO get rid of this and have different types for different parameterisations
  private final StrikeParameterization _strikeParameterisation;

  /**
   * @param surface The time to maturity should be the first coordinate and the strike the second
   */
  public BlackVolatilitySurfaceOld(final Surface<Double, Double, Double> surface) {
    super(surface);
    _strikeParameterisation = StrikeParameterization.STRIKE;
  }

  /**
   * @param surface The time to maturity should be the first coordinate and the strike the second
   * @param strikeType StrikeParameterisation defines how to interpret the strike axis
   */
  public BlackVolatilitySurfaceOld(final Surface<Double, Double, Double> surface, final StrikeParameterization strikeType) {
    super(surface);
    _strikeParameterisation = strikeType;
  }

  /**
   * 
   * @param t time to maturity
   * @param k strike
   * @param forward forward price
   * @param isCall true if one desires a call price, false for a put
   * @return Forward price implied by the volatility and forward. Forward Price is today's value divided by the terminal zero bond, i.e. V(0,T) * exp(+rT)
   */
  public double getForwardPrice(final double t, final double k, final double forward, final boolean isCall) {
    return new BlackFormula(forward, k, t, getVolatility(t, k), null, isCall).computePrice();
  }

  /**
   * Gets the strikeParameterisation.
   * @return the strikeParameterisation
   */
  public final StrikeParameterization getStrikeParameterisation() {
    return _strikeParameterisation;
  }
}
