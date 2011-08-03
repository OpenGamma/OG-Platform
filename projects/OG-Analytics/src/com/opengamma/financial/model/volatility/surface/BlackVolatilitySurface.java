/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.volatility.surface;

import com.opengamma.financial.equity.varswap.pricing.VarSwapStaticReplication.StrikeParameterisation;
import com.opengamma.financial.model.volatility.BlackFormula;
import com.opengamma.math.surface.Surface;
import com.opengamma.util.tuple.DoublesPair;

/**
 * A surface that contains the Black (implied) volatility  as a function of time to maturity and strike.
 * Also known as Lognormal Vol.
 */
public class BlackVolatilitySurface extends VolatilitySurface {

  @SuppressWarnings("unused")
  private final StrikeParameterisation _strikeParameterisation;

  /**
   * @param surface The time to maturity should be the first coordinate and the strike the second 
   */
  public BlackVolatilitySurface(Surface<Double, Double, Double> surface) {
    super(surface);
    _strikeParameterisation = StrikeParameterisation.STRIKE;
  }

  /**
   * @param surface The time to maturity should be the first coordinate and the strike the second 
   * @param strikeType StrikeParameterisation defines how to interpret the strike axis
   */
  public BlackVolatilitySurface(Surface<Double, Double, Double> surface, StrikeParameterisation strikeType) {
    super(surface);
    _strikeParameterisation = strikeType;
  }

  /**
   * Return a volatility for the expiry,strike pair provided. 
   * Interpolation/extrapolation behaviour depends on underlying surface  
   * @param t time to maturity
   * @param k strike
   * @return The Black (implied) volatility 
   */
  public double getVolatility(final double t, final double k) {
    DoublesPair temp = new DoublesPair(t, k);
    return getVolatility(temp);
  }

  /**
   * 
   * @param t time to maturity
   * @param k strike
   * @param forward forward price
   * @param isCall true if one desires a call price, false for a put
   * @return Forward price implied by the volatility and forward. Forward Price is today's value divided by the terminal zero bond, i.e. V(0,T) * exp(+rT)
   */
  public double getForwardPrice(final double t, final double k, final double forward, boolean isCall) {
    return new BlackFormula(forward, k, t, getVolatility(t, k), null, isCall).computePrice();
  }

  /**
   * Gets the strikeParameterisation.
   * @return the strikeParameterisation
   */
  public final StrikeParameterisation getStrikeParameterisation() {
    return _strikeParameterisation;
  }
}
