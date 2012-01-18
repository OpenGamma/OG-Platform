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
public final class CosineWeightingFunction extends WeightingFunction {
  private static final CosineWeightingFunction s_instance = new CosineWeightingFunction();

  public static CosineWeightingFunction getInstance() {
    return s_instance;
  }

  private CosineWeightingFunction() {
  }

  @Override
  public double getWeight(final double[] strikes, final int lowerBoundIndex, final double strike) {
    ArgumentChecker.notNull(strikes, "strikes");
    ArgumentChecker.notNegative(lowerBoundIndex, "index");
    ArgumentChecker.isTrue(lowerBoundIndex <= strikes.length - 2, "index cannot be larger than {}, have {}", strikes.length - 2, lowerBoundIndex);
    final double cos = Math.cos(Math.PI / 2 * (strike - strikes[lowerBoundIndex]) / (strikes[lowerBoundIndex + 1] - strikes[lowerBoundIndex]));
    return cos * cos;
  }
}
