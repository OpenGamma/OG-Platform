/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.volatility.surface;

import com.opengamma.math.surface.Surface;
import com.opengamma.math.surface.SurfaceShiftFunctionFactory;

/**
 * 
 */
public class BlackVolatilitySurfaceStrike extends BlackVolatilitySurface<Strike> {

  /**
   * @param surface The time to maturity should be the first coordinate and the strike the second
   */
  public BlackVolatilitySurfaceStrike(Surface<Double, Double, Double> surface) {
    super(surface);
  }

  @Override
  public double getVolatility(double t, double k) {
    return getVolatility(t, new Strike(k));
  }

  @Override
  public double getAbsoluteStrike(double t, Strike s) {
    return s.value();
  }

  @Override
  public BlackVolatilitySurface<Strike> withShift(double shift, boolean UseAddative) {
    return new BlackVolatilitySurfaceStrike(SurfaceShiftFunctionFactory.getShiftedSurface(getSurface(), shift, UseAddative));
  }

  @Override
  public BlackVolatilitySurface<Strike> withSurface(Surface<Double, Double, Double> surface) {
    return new BlackVolatilitySurfaceStrike(surface);
  }

}
