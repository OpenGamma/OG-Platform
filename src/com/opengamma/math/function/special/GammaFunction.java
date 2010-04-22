/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.function.special;

import cern.jet.stat.Gamma;

import com.opengamma.math.function.Function1D;

/**
 * 
 * @author emcleod
 */
public class GammaFunction extends Function1D<Double, Double> {
  private final Function1D<Double, Double> _lnGamma = new NaturalLogGammaFunction();

  @Override
  public Double evaluate(final Double x) {
    return Gamma.gamma(x);
  }

}
