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
   * @param timeToExpiry Time to expiry
   * @param lowerBound Lower bound, KL
   * @param upperBound Upper bound, KH
   * @param steps Number of steps
   */
  public SupershareOptionFunctionProvider(final double timeToExpiry, final double lowerBound, final double upperBound, final int steps) {
    super(lowerBound, timeToExpiry, steps, true);
    ArgumentChecker.isTrue(upperBound > 0., "upperBound should be positive");
    ArgumentChecker.isTrue(Doubles.isFinite(upperBound), "upperBound should be finite");
    ArgumentChecker.isTrue(upperBound > lowerBound, "upperBound should be larger than lowerBound");
    _upperBound = upperBound;
  }

  @Override
  public double[] getPayoffAtExpiry(final double assetPrice, final double downFactor, final double upOverDown) {
    final double lowerBound = super.getStrike();
    final int nSteps = getNumberOfSteps();
    final int nStepsP = nSteps + 1;

    final double[] values = new double[nStepsP];
    double priceTmp = assetPrice * Math.pow(downFactor, nSteps);
    for (int i = 0; i < nStepsP; ++i) {
      values[i] = priceTmp >= lowerBound && priceTmp < _upperBound ? priceTmp / lowerBound : 0.;
      priceTmp *= upOverDown;
    }
    return values;
  }

  @Override
  public double[] getPayoffAtExpiryTrinomial(final double assetPrice, final double downFactor, final double middleOverDown) {
    final double lowerBound = super.getStrike();
    final int nSteps = getNumberOfSteps();
    final int nNodes = 2 * getNumberOfSteps() + 1;

    final double[] values = new double[nNodes];
    double priceTmp = assetPrice * Math.pow(downFactor, nSteps);
    for (int i = 0; i < nNodes; ++i) {
      values[i] = priceTmp >= lowerBound && priceTmp < _upperBound ? priceTmp / lowerBound : 0.;
      priceTmp *= middleOverDown;
    }
    return values;
  }

  /**
   * Access lower bound
   * @return _strike in superclass
   */
  public double getLowerBound() {
    return super.getStrike();
  }

  /**
   * Access upper bound 
   * @return _upperBound
   */
  public double getUpperBound() {
    return _upperBound;
  }

  @Override
  public double getSign() {
    throw new IllegalArgumentException("Call/put is not relevant for this option");
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    long temp;
    temp = Double.doubleToLongBits(_upperBound);
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
    if (!(obj instanceof SupershareOptionFunctionProvider)) {
      return false;
    }
    SupershareOptionFunctionProvider other = (SupershareOptionFunctionProvider) obj;
    if (Double.doubleToLongBits(_upperBound) != Double.doubleToLongBits(other._upperBound)) {
      return false;
    }
    return true;
  }

}
