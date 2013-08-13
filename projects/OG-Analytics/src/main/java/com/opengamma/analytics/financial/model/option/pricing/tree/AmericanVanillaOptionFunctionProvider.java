/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.pricing.tree;

/**
 * 
 */
public class AmericanVanillaOptionFunctionProvider extends OptionFunctionProvider {

  public AmericanVanillaOptionFunctionProvider(final double strike, final int steps, final boolean isCall) {
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
      values[i] = Math.max(sign * (priceTmp - strike), 0);
      priceTmp *= upOverDown;
    }
    return values;
  }

  @Override
  public double[] getNextOptionValues(final double discount, final double upProbability, final double downProbability, final double[] values, final double baseAssetPrice, final double downFactor,
      final double upOverDown, final int steps) {
    final double strike = getStrike();
    final double sign = getSign();
    final int nStepsP = steps + 1;

    final double[] res = new double[nStepsP];
    double assetPrice = baseAssetPrice * Math.pow(downFactor, steps);
    for (int j = 0; j < nStepsP; ++j) {
      res[j] = Math.max(discount * (upProbability * values[j + 1] + downProbability * values[j]), sign * (assetPrice - strike));
      assetPrice *= upOverDown;
    }
    return res;
  }
}
