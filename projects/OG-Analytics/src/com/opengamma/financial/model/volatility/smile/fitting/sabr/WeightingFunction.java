/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.volatility.smile.fitting.sabr;

import com.opengamma.util.ArgumentChecker;


/**
 * 
 */
public abstract class WeightingFunction {

  public double getWeight(final double[] strikes, final double strike) {
    ArgumentChecker.notNull(strikes, "strikes");
    final int index = SurfaceArrayUtils.getLowerBoundIndex(strikes, strike);
    return getWeight(strikes, index, strike);
  }

  public abstract double getWeight(double[] strikes, int lowerBoundIndex, double strike);

}
