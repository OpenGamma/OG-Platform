/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.pricing.tree;

import com.google.common.primitives.Doubles;
import com.opengamma.util.ArgumentChecker;

/**
 * Payoff of capped power option is min( max( S^i - K , 0 ) , C ) for call and min( max( K - S^i , 0 ) , C ) for put with i > 0, K > C > 0
 */
public class CappedPowerOptionFunctionProvider extends OptionFunctionProvider1D {

  private double _power;
  private double _cap;

  /**
   * @param strike Strike price, K
   * @param timeToExpiry Time to expiry
   * @param steps Number of steps
   * @param isCall True if call, false if put
   * @param power Power, i
   * @param cap Cap, C
   */
  public CappedPowerOptionFunctionProvider(final double strike, final double timeToExpiry, final int steps, final boolean isCall, final double power, final double cap) {
    super(strike, timeToExpiry, steps, isCall);
    ArgumentChecker.isTrue(power > 0., "power should be positive");
    ArgumentChecker.isTrue(Doubles.isFinite(power), "power should be finite");
    ArgumentChecker.isTrue(cap > 0., "cap should be positive");
    ArgumentChecker.isTrue(Doubles.isFinite(cap), "cap should be finite");
    if (!isCall) {
      ArgumentChecker.isTrue(cap < strike, "cap should be smaller than strike for put");
    }
    _power = power;
    _cap = cap;
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
      values[i] = Math.min(Math.max(sign * (Math.pow(priceTmp, _power) - strike), 0.), _cap);
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
      values[i] = Math.min(Math.max(sign * (Math.pow(priceTmp, _power) - strike), 0.), _cap);
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

  /**
   * Access cap
   * @return _cap
   */
  public double getCap() {
    return _cap;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    long temp;
    temp = Double.doubleToLongBits(_cap);
    result = prime * result + (int) (temp ^ (temp >>> 32));
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
    if (!(obj instanceof CappedPowerOptionFunctionProvider)) {
      return false;
    }
    CappedPowerOptionFunctionProvider other = (CappedPowerOptionFunctionProvider) obj;
    if (Double.doubleToLongBits(_cap) != Double.doubleToLongBits(other._cap)) {
      return false;
    }
    if (Double.doubleToLongBits(_power) != Double.doubleToLongBits(other._power)) {
      return false;
    }
    return true;
  }
}
