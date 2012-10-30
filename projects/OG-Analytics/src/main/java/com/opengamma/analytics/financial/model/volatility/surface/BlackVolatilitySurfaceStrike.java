/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility.surface;

import com.opengamma.analytics.math.surface.Surface;
import com.opengamma.analytics.math.surface.SurfaceShiftFunctionFactory;

/**
 * 
 */
public class BlackVolatilitySurfaceStrike extends BlackVolatilitySurface<Strike> {

  /**
   * @param surface The time to maturity should be the first coordinate and the strike the second
   */
  public BlackVolatilitySurfaceStrike(final Surface<Double, Double, Double> surface) {
    super(surface);
  }

  @Override
  public double getVolatility(final double t, final double k) {
    return getVolatility(t, new Strike(k));
  }

  @Override
  public double getAbsoluteStrike(final double t, final Strike s) {
    return s.value();
  }

  @Override
  public BlackVolatilitySurface<Strike> withShift(final double shift, final boolean useAdditive) {
    return new BlackVolatilitySurfaceStrike(SurfaceShiftFunctionFactory.getShiftedSurface(getSurface(), shift, useAdditive));
  }

  @Override
  public BlackVolatilitySurface<Strike> withSurface(final Surface<Double, Double, Double> surface) {
    return new BlackVolatilitySurfaceStrike(surface);
  }

  @Override
  public <S, U> U accept(final BlackVolatilitySurfaceVisitor<S, U> visitor, final S data) {
    return visitor.visitStrike(this, data);
  }

  @Override
  public <U> U accept(final BlackVolatilitySurfaceVisitor<?, U> visitor) {
    return visitor.visitStrike(this);

  }

}
