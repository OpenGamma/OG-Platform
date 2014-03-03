/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.pricing.tree;

import com.google.common.primitives.Doubles;
import com.opengamma.util.ArgumentChecker;

/**
 * Gap call option pays 0 if S <= K1 and S-K2 if S > K1, whereas gap put option pays K2-S if S < K1 and 0 if S >= K1, 
 * where S is asset price at expiry.
 */
public class GapOptionFunctionProvider extends OptionFunctionProvider1D {

  private double _payoffStrike;

  /**
   * @param strike Strike price, K1
   * @param timeToExpiry Time to expiry
   * @param steps Number of steps
   * @param isCall True if call, false if put
   * @param payoffStrike Payoff-strike, K2
   */
  public GapOptionFunctionProvider(final double strike, final double timeToExpiry, final int steps, final boolean isCall, final double payoffStrike) {
    super(strike, timeToExpiry, steps, isCall);
    ArgumentChecker.isTrue(payoffStrike > 0., "payoffStrike should be positive");
    ArgumentChecker.isTrue(Doubles.isFinite(payoffStrike), "payoffStrike should be finite");
    _payoffStrike = payoffStrike;
  }

  @Override
  public double[] getPayoffAtExpiry(final double assetPrice, final double downFactor, final double upOverDown) {
    final double strike = getStrike();
    final int nSteps = getNumberOfSteps();
    final int nStepsP = nSteps + 1;
    final double sign = getSign();

    final double[] values = new double[nStepsP];
    double priceTmp = assetPrice * Math.pow(downFactor, nSteps);
    for (int i = 0; i < nStepsP; ++i) {
      values[i] = sign * (priceTmp - strike) > 0. ? sign * (priceTmp - _payoffStrike) : 0.;
      priceTmp *= upOverDown;
    }
    return values;
  }

  @Override
  public double[] getPayoffAtExpiryTrinomial(final double assetPrice, final double downFactor, final double middleOverDown) {
    final double strike = getStrike();
    final int nSteps = getNumberOfSteps();
    final int nNodes = 2 * getNumberOfSteps() + 1;
    final double sign = getSign();

    final double[] values = new double[nNodes];
    double priceTmp = assetPrice * Math.pow(downFactor, nSteps);
    for (int i = 0; i < nNodes; ++i) {
      values[i] = sign * (priceTmp - strike) > 0. ? sign * (priceTmp - _payoffStrike) : 0.;
      priceTmp *= middleOverDown;
    }
    return values;
  }

  /**
   * Access payoff-strike
   * @return _payoffStrike
   */
  public double getStrikePayoff() {
    return _payoffStrike;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    long temp;
    temp = Double.doubleToLongBits(_payoffStrike);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!super.equals(obj)) {
      return false;
    }
    if (!(obj instanceof GapOptionFunctionProvider)) {
      return false;
    }
    GapOptionFunctionProvider other = (GapOptionFunctionProvider) obj;
    if (Double.doubleToLongBits(_payoffStrike) != Double.doubleToLongBits(other._payoffStrike)) {
      return false;
    }
    return true;
  }

}
