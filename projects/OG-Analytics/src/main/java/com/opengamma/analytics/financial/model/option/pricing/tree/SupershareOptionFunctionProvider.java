/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.pricing.tree;

import com.google.common.primitives.Doubles;
import com.opengamma.util.ArgumentChecker;

/**
 * Supershare option pays S/KL if KL <= S < KH at expiry. 
 */
public class SupershareOptionFunctionProvider extends OptionFunctionProvider1D {

  private double _upperBound;

  /**
   * @param lowerBound Lower bound, KL
   * @param upperBound Upper bound, KH
   * @param steps Number of steps
   */
  public SupershareOptionFunctionProvider(final double lowerBound, final double upperBound, final int steps) {
    super(lowerBound, steps, true);
    ArgumentChecker.isTrue(upperBound > 0., "upperBound should be positive");
    ArgumentChecker.isTrue(Doubles.isFinite(upperBound), "upperBound should be finite");
    ArgumentChecker.isTrue(upperBound > lowerBound, "upperBound should be larger than lowerBound");
    _upperBound = upperBound;
  }

  @Override
  public double[] getPayoffAtExpiry(final double assetPrice, final double upOverDown) {
    final double lowerBound = getStrike();
    final int nStepsP = getNumberOfSteps() + 1;

    final double[] values = new double[nStepsP];
    double priceTmp = assetPrice;
    for (int i = 0; i < nStepsP; ++i) {
      values[i] = priceTmp >= lowerBound && priceTmp < _upperBound ? priceTmp / lowerBound : 0.;
      priceTmp *= upOverDown;
    }
    return values;
  }

  @Override
  public double[] getNextOptionValues(final double discount, final double upProbability, final double downProbability, final double[] values, final double baseAssetPrice, final double sumCashDiv,
      final double downFactor, final double upOverDown, final int steps) {
    final int nStepsP = steps + 1;

    final double[] res = new double[nStepsP];
    for (int j = 0; j < nStepsP; ++j) {
      res[j] = discount * (upProbability * values[j + 1] + downProbability * values[j]);
    }
    return res;
  }

  @Override
  public double getSign() {
    throw new IllegalArgumentException("Call/put is not relevant");
  }
}
