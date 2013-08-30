/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.pricing.tree;

import com.google.common.primitives.Doubles;
import com.opengamma.util.ArgumentChecker;

/**
 * Payoff of powered option is max( S - K , 0 )^i for call and max( K - S , 0 )^i for put with i > 0
 */
public class PoweredOptionFunctionProvider extends OptionFunctionProvider1D {

  private double _power;

  /**
   * @param strike Strike price, K
   * @param steps Number of steps
   * @param isCall True if call, false if put
   * @param power Power, i
   */
  public PoweredOptionFunctionProvider(final double strike, final int steps, final boolean isCall, final double power) {
    super(strike, steps, isCall);
    ArgumentChecker.isTrue(power > 0., "power should be positive");
    ArgumentChecker.isTrue(Doubles.isFinite(power), "power should be finite");
    _power = power;
  }

  @Override
  public double[] getPayoffAtExpiry(final double assetPrice, final double upOverDown) {
    final double strike = getStrike();
    final int nStepsP = getNumberOfSteps() + 1;
    final double sign = getSign();

    final double[] values = new double[nStepsP];
    double priceTmp = assetPrice;
    for (int i = 0; i < nStepsP; ++i) {
      values[i] = Math.pow(Math.max(sign * (priceTmp - strike), 0.), _power);
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

  /**
   * Access power
   * @return _power
   */
  public double getPower() {
    return _power;
  }
}
