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
public final class SineWeightingFunction extends WeightingFunction {
  private static final SineWeightingFunction s_instance = new SineWeightingFunction();

  public static SineWeightingFunction getInstance() {
    return s_instance;
  }

  private SineWeightingFunction() {
  }

  @Override
  public double getWeight(final double[] strikes, final int lowerBoundIndex, final double strike) {
    ArgumentChecker.notNull(strikes, "strikes");
    ArgumentChecker.notNegative(lowerBoundIndex, "index");
    ArgumentChecker.isTrue(lowerBoundIndex <= strikes.length - 2, "index cannot be larger than {}, have {}", strikes.length - 2, lowerBoundIndex);
    final double y = (strikes[lowerBoundIndex + 1] - strike) / (strikes[lowerBoundIndex + 1] - strikes[lowerBoundIndex]);
    return 0.5 * (Math.sin(Math.PI * (y - 0.5)) + 1);
  }

}
