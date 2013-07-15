/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility.surface;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;

import com.opengamma.analytics.financial.model.interestrate.curve.ForwardCurve;
import com.opengamma.analytics.math.surface.Surface;
import com.opengamma.analytics.math.surface.SurfaceShiftFunctionFactory;

/**
 * 
 */
public class BlackVolatilitySurfaceLogMoneyness extends BlackVolatilitySurface<LogMoneyness> {

  private final ForwardCurve _fc;

  public BlackVolatilitySurfaceLogMoneyness(final BlackVolatilitySurfaceMoneyness other) {
    super(other.getSurface());
    _fc = other.getForwardCurve();
  }

  /**
   * @param surface The surface, not null
   * @param forwardCurve The forward curve, not null
   */
  public BlackVolatilitySurfaceLogMoneyness(final Surface<Double, Double, Double> surface, final ForwardCurve forwardCurve) {
    super(surface);
    Validate.notNull(forwardCurve, "null forward curve");
    _fc = forwardCurve;
  }

  @Override
  public double getVolatility(final double t, final double k) {
    final double f = _fc.getForward(t);
    final LogMoneyness x = new LogMoneyness(k, f);
    return getVolatility(t, x);
  }

  /**
   * Return a volatility for the expiry, log-moneyness pair provided.
   * Interpolation/extrapolation behaviour depends on underlying surface
   * @param t time to maturity
   * @param x the log-moneyness  x = ln[k/F(T)], where k is the strike and F(T) is the forward for expiry at time T
   * @return The Black (implied) volatility
   */
  public double getVolatilityForLogMoneyness(final double t, final double x) {
    return getVolatility(t, new LogMoneyness(x));
  }

  public ForwardCurve getForwardCurve() {
    return _fc;
  }

  @Override
  public double getAbsoluteStrike(final double t, final LogMoneyness s) {
    return _fc.getForward(t) * Math.exp(s.value());
  }

  @Override
  public BlackVolatilitySurface<LogMoneyness> withShift(final double shift, final boolean useAdditive) {
    return new BlackVolatilitySurfaceLogMoneyness(SurfaceShiftFunctionFactory.getShiftedSurface(getSurface(), shift, useAdditive), _fc);
  }

  @Override
  public BlackVolatilitySurface<LogMoneyness> withSurface(final Surface<Double, Double, Double> surface) {
    return new BlackVolatilitySurfaceLogMoneyness(surface, _fc);
  }

  @Override
  public <S, U> U accept(final BlackVolatilitySurfaceVisitor<S, U> visitor, final S data) {
    return visitor.visitLogMoneyness(this, data);
  }

  @Override
  public <U> U accept(final BlackVolatilitySurfaceVisitor<?, U> visitor) {
    return visitor.visitLogMoneyness(this);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + _fc.hashCode();
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!super.equals(obj)) {
      return false;
    }
    if (!(obj instanceof BlackVolatilitySurfaceLogMoneyness)) {
      return false;
    }
    final BlackVolatilitySurfaceLogMoneyness other = (BlackVolatilitySurfaceLogMoneyness) obj;
    if (!ObjectUtils.equals(_fc, other._fc)) {
      return false;
    }
    return true;
  }

}
