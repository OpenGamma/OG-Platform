/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.pricing.tree;

/**
 * 
 */
public class AmericanSingleBarrierOptionFunctionProvider extends BarrierOptionFunctionProvider {

  /**
   * @param strike Strike price
   * @param steps Number of steps
   * @param isCall True if call, false if put
   * @param barrier Barrier price
   * @param typeName {@link BarrierTypes}, DownAndOut or UpAndOut
   */
  public AmericanSingleBarrierOptionFunctionProvider(final double strike, final int steps, final boolean isCall, final double barrier, final BarrierTypes typeName) {
    super(strike, steps, isCall, barrier, typeName);
  }

  @Override
  public double[] getPayoffAtExpiry(final double assetPrice, final double upOverDown) {
    final double strike = getStrike();
    final int nStepsP = getNumberOfSteps() + 1;
    final double sign = getSign();

    final double[] values = new double[nStepsP];
    double priceTmp = assetPrice;
    for (int i = 0; i < nStepsP; ++i) {
      values[i] = getChecker().checkOut(priceTmp) ? 0. : Math.max(sign * (priceTmp - strike), 0.);
      priceTmp *= upOverDown;
    }
    return values;
  }

  @Override
  public double[] getNextOptionValues(final double discount, final double upProbability, final double downProbability, final double[] values, final double baseAssetPrice, final double sumCashDiv,
      final double downFactor, final double upOverDown, final int steps) {
    final double strike = getStrike();
    final double sign = getSign();
    final int nStepsP = steps + 1;

    final double[] res = new double[nStepsP];
    double assetPrice = baseAssetPrice * Math.pow(downFactor, steps);
    for (int j = 0; j < nStepsP; ++j) {
      res[j] = getChecker().checkOut(assetPrice + sumCashDiv) ? 0. : Math.max(discount * (upProbability * values[j + 1] + downProbability * values[j]), sign * (assetPrice + sumCashDiv - strike));
      assetPrice *= upOverDown;
    }
    return res;
  }
}
