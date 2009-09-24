/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.volatility.surface;

import java.util.Collections;

import com.opengamma.math.interpolation.Interpolator2D;
import com.opengamma.util.Pair;

/**
 * 
 * A VolatilitySurface that has a constant volatility for all values of x and y
 * 
 * @author emcleod
 */
public class ConstantVolatilitySurface extends VolatilitySurface {
  private final double _sigma;

  public ConstantVolatilitySurface(Double sigma) {
    super(Collections.<Pair<Double, Double>, Double> singletonMap(new Pair<Double, Double>(0., 0.), sigma), null);
    _sigma = sigma;
  }

  @Override
  public Interpolator2D getInterpolator() {
    throw new UnsupportedOperationException();
  }

  @Override
  public double getVolatility(Double x, Double y) {
    return _sigma;
  }
}
