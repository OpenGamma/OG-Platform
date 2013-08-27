/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.pricing.tree;

/**
 * Cash-or-nothing call option pays 0 if S <= K and K if S > K, whereas cash-or-nothing put option pays K if S < K and 0 if S >= K, 
 * where S is asset price at expiry.
 */
public class CashOrNothingOptionFunctionProvider extends OptionFunctionProvider1D {

  /**
   * @param strike Strike price
   * @param steps Number of steps
   * @param isCall True if call, false if put
   */
  public CashOrNothingOptionFunctionProvider(final double strike, final int steps, final boolean isCall) {
    super(strike, steps, isCall);
  }

  @Override
  public double[] getPayoffAtExpiry(final double assetPrice, final double upOverDown) {
    final double strike = getStrike();
    final int nStepsP = getNumberOfSteps() + 1;
    final double sign = getSign();

    final double[] values = new double[nStepsP];
    double priceTmp = assetPrice;
    for (int i = 0; i < nStepsP; ++i) {
      values[i] = sign * (priceTmp - strike) > 0. ? strike : 0.;
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

}
