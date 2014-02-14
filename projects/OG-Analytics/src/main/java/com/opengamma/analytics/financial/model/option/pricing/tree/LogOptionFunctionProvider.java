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
   * @param timeToExpiry Time to expiry
   * @param steps Number of steps
   */
  public LogOptionFunctionProvider(final double strike, final double timeToExpiry, final int steps) {
    super(strike, timeToExpiry, steps, true);
  }

  @Override
  public double[] getPayoffAtExpiry(final double assetPrice, final double downFactor, final double upOverDown) {
    final double strike = getStrike();
    final int nSteps = getNumberOfSteps();
    final int nStepsP = nSteps + 1;

    final double[] values = new double[nStepsP];
    double priceTmp = assetPrice * Math.pow(downFactor, nSteps);
    for (int i = 0; i < nStepsP; ++i) {
      values[i] = Math.max(Math.log(priceTmp / strike), 0.);
      priceTmp *= upOverDown;
    }
    return values;
  }

  @Override
  public double[] getPayoffAtExpiryTrinomial(final double assetPrice, final double downFactor, final double middleOverDown) {
    final double strike = getStrike();
    final int nSteps = getNumberOfSteps();
    final int nNodes = 2 * getNumberOfSteps() + 1;

    final double[] values = new double[nNodes];
    double priceTmp = assetPrice * Math.pow(downFactor, nSteps);
    for (int i = 0; i < nNodes; ++i) {
      values[i] = Math.max(Math.log(priceTmp / strike), 0.);
      priceTmp *= middleOverDown;
    }
    return values;
  }

  @Override
  public double getSign() {
    throw new IllegalArgumentException("Call/put is not relevant");
  }

  @Override
  public int hashCode() {
    return super.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof LogOptionFunctionProvider)) {
      return false;
    }
    return super.equals(obj);
  }
}
