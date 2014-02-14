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
   * @param timeToExpiry Time to expiry
   * @param steps Number of steps
   * @param isCall True if call, false if put
   * @param power Power, i
   */
  public PoweredOptionFunctionProvider(final double strike, final double timeToExpiry, final int steps, final boolean isCall, final double power) {
    super(strike, timeToExpiry, steps, isCall);
    ArgumentChecker.isTrue(power > 0., "power should be positive");
    ArgumentChecker.isTrue(Doubles.isFinite(power), "power should be finite");
    _power = power;
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
      values[i] = Math.pow(Math.max(sign * (priceTmp - strike), 0.), _power);
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
      values[i] = Math.pow(Math.max(sign * (priceTmp - strike), 0.), _power);
      priceTmp *= middleOverDown;
    }
    return values;
  }

  /**
   * Access power
   * @return _power
   */
  public double getPower() {
    return _power;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    long temp;
    temp = Double.doubleToLongBits(_power);
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
    if (!(obj instanceof PoweredOptionFunctionProvider)) {
      return false;
    }
    PoweredOptionFunctionProvider other = (PoweredOptionFunctionProvider) obj;
    if (Double.doubleToLongBits(_power) != Double.doubleToLongBits(other._power)) {
      return false;
    }
    return true;
  }

}
