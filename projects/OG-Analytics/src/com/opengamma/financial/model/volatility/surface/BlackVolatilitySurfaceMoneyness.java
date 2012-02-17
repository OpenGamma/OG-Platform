/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.volatility.surface;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.model.interestrate.curve.ForwardCurve;
import com.opengamma.math.surface.Surface;
import com.opengamma.math.surface.SurfaceShiftFunctionFactory;

/**
 *  * A surface that contains the Black (implied) volatility  as a function of time to maturity and moneyness, m, defined
 *  as m = k/F(T), where k is the strike and F(T) is the forward for expiry at time T
 */
public class BlackVolatilitySurfaceMoneyness extends BlackVolatilitySurface<Moneyness> {

  private final ForwardCurve _fc;

  public BlackVolatilitySurfaceMoneyness(final BlackVolatilitySurfaceMoneyness other) {
    super(other.getSurface());
    _fc = other.getForwardCurve();
  }

  /**
   * @param surface A implied volatility surface parameterised by time and moneyness m = strike/forward
   * @param forwardCurve the forward curve
   */
  public BlackVolatilitySurfaceMoneyness(final Surface<Double, Double, Double> surface, final ForwardCurve forwardCurve) {
    super(surface);
    Validate.notNull(forwardCurve, "null forward curve");
    _fc = forwardCurve;
  }

  /**
   * Return a volatility for the expiry, strike pair provided.
   * Interpolation/extrapolation behaviour depends on underlying surface
   * @param t time to maturity
   * @param k strike
   * @return The Black (implied) volatility
   */
  @Override
  public double getVolatility(final double t, final double k) {
    final double f = _fc.getForward(t);
    final Moneyness x = new Moneyness(k, f);
    return getVolatility(t, x);
  }

  /**
   * Return a volatility for the expiry, moneyness pair provided.
   * Interpolation/extrapolation behaviour depends on underlying surface
   * @param t time to maturity
   * @param m the moneyness  m = k/F(T), where k is the strike and F(T) is the forward for expiry at time T
   * @return The Black (implied) volatility
   */
  public double getVolatilityForMoneyness(final double t, final double m) {
    return getVolatility(t, new Moneyness(m));
  }

  public ForwardCurve getForwardCurve() {
    return _fc;
  }

  @Override
  public double getAbsoluteStrike(final double t, final Moneyness s) {
    return _fc.getForward(t) * s.value();
  }

  @Override
  public BlackVolatilitySurface<Moneyness> withShift(final double shift, final boolean useAdditive) {
    return new BlackVolatilitySurfaceMoneyness(SurfaceShiftFunctionFactory.getShiftedSurface(getSurface(), shift, useAdditive), _fc);
  }

  @Override
  public BlackVolatilitySurface<Moneyness> withSurface(final Surface<Double, Double, Double> surface) {
    return new BlackVolatilitySurfaceMoneyness(surface, _fc);
  }

  @Override
  public <S, U> U accept(final BlackVolatilitySurfaceVisitor<S, U> visitor, final S data) {
    return visitor.visitMoneyness(this, data);
  }

  @Override
  public <U> U accept(final BlackVolatilitySurfaceVisitor<?, U> visitor) {
    return visitor.visitMoneyness(this);
  }



}
