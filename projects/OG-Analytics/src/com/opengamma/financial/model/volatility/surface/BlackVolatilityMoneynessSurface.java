/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.volatility.surface;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.model.interestrate.curve.ForwardCurve;
import com.opengamma.math.surface.Surface;
import com.opengamma.util.tuple.DoublesPair;

/**
 *  * A surface that contains the Black (implied) volatility  as a function of time to maturity and moneyness, m, defined
 *  as m = k/F(T), where k is the strike and F(T) is the forward for expiry at time T
 */
public class BlackVolatilityMoneynessSurface extends BlackVolatilitySurface {

  private ForwardCurve _fc;

  /**
   * @param surface A implied volatility surface parameterised by time and moneyness m = strike/forward
   * @param forwardCurve the forward curve
   */
  public BlackVolatilityMoneynessSurface(final Surface<Double, Double, Double> surface, final ForwardCurve forwardCurve) {
    super(surface);
    Validate.notNull(forwardCurve, "null forward curve");
    _fc = forwardCurve;
  }

  /**
   * Return a volatility for the expiry,strike pair provided.
   * Interpolation/extrapolation behaviour depends on underlying surface
   * @param t time to maturity
   * @param k strike
   * @return The Black (implied) volatility
   */
  @Override
  public double getVolatility(final double t, final double k) {
    double f = _fc.getForward(t);
    double x = k / f;
    final DoublesPair temp = new DoublesPair(t, x);
    return getVolatility(temp);
  }

  /**
   * Return a volatility for the expiry,strike pair provided.
   * Interpolation/extrapolation behaviour depends on underlying surface
   * @param t time to maturity
   * @param m the moneyness  m = k/F(T), where k is the strike and F(T) is the forward for expiry at time T
   * @return The Black (implied) volatility
   */
  public double getVolatilityForMoneyness(final double t, final double m) {
    final DoublesPair temp = new DoublesPair(t, m);
    return getVolatility(temp);
  }

  public ForwardCurve getForwardCurve() {
    return _fc;
  }

}
