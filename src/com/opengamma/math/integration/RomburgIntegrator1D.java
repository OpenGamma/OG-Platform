/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.integration;

import com.opengamma.math.function.Function1D;
import com.opengamma.math.interpolation.PolynomialInterpolator1D;

/**
 * 
 * @author emcleod
 */
public class RomburgIntegrator1D extends Integrator1D<Double, Function1D<Double, Double>, Double> {
  private PolynomialInterpolator1D _interpolator;

  @Override
  public Double integrate(final Function1D<Double, Double> f, final Double lower, final Double upper) {
    // TODO Auto-generated method stub
    return null;
  }

}
