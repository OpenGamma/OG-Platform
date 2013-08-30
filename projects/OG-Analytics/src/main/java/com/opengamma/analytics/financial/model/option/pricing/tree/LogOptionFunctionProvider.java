/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.pricing.tree;

/**
 * Log option pays Max( log(S/K), 0 ) at maturity
 */
public class LogOptionFunctionProvider extends OptionFunctionProvider1D {

  /**
   * @param strike The strike
   * @param steps Number of steps
   */
  public LogOptionFunctionProvider(final double strike, final int steps) {
    super(strike, steps, true);
  }

  @Override
  public double[] getPayoffAtExpiry(final double assetPrice, final double upOverDown) {
    final double strike = getStrike();
    final int nStepsP = getNumberOfSteps() + 1;

    final double[] values = new double[nStepsP];
    double priceTmp = assetPrice;
    for (int i = 0; i < nStepsP; ++i) {
      values[i] = Math.max(Math.log(priceTmp / strike), 0.);
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
