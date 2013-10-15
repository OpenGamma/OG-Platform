/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility.surface;

import com.opengamma.analytics.math.surface.Surface;
import com.opengamma.util.tuple.DoublesPair;

/**
 *  A surface with gives Black (implied) volatility  as a function of time to maturity and value some abstraction of strike
 *  @param <T> Parameter that describes the abstraction of strike - this could be the actual strike, the delta (most commonly used in FX), moneyness (defined as the strike/forward),
 *  the logarithm of moneyness or some other parameterisation
 */
public abstract class BlackVolatilitySurface<T extends StrikeType> extends VolatilitySurface {

  /**
   * @param surface  The time to maturity should be the first coordinate and the abstraction of strike the second
   */
  public BlackVolatilitySurface(final Surface<Double, Double, Double> surface) {
    super(surface);
  }

  public double getVolatility(final double t, final T s) {
    final DoublesPair temp = DoublesPair.of(t, s.value());
    return getVolatility(temp);
  }

  @Override
  public abstract double getVolatility(final double t, final double k);

  public abstract double getAbsoluteStrike(final double t, final T s);

  public abstract BlackVolatilitySurface<T> withShift(final double shift, final boolean useAdditive);

  public abstract BlackVolatilitySurface<T> withSurface(Surface<Double, Double, Double> surface);

  public abstract <S, U> U accept(final BlackVolatilitySurfaceVisitor<S, U> visitor, final S data);

  public abstract <U> U accept(final BlackVolatilitySurfaceVisitor<?, U> visitor);

}
