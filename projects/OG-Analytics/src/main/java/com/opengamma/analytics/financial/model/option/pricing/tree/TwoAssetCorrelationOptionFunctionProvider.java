/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.pricing.tree;

import com.google.common.primitives.Doubles;
import com.opengamma.util.ArgumentChecker;

/**
 * The call option pays off max[S2 - X2, 0] if S1 > X1 and 0 otherwise, 
 * whereas the put pays off max[X2 - S2, 0] if S1 < X1 and 0 otherwise
 */
public class TwoAssetCorrelationOptionFunctionProvider extends OptionFunctionProvider2D {
  private double _strike2;

  /**
   * @param strike1 Strike price for asset 1, X1
   * @param strike2 Strike price for asset 2, X2
   * @param timeToExpiry Time to expiry
   * @param steps Number of steps
   * @param isCall True if call, false if put
   */
  public TwoAssetCorrelationOptionFunctionProvider(final double strike1, final double strike2, final double timeToExpiry, final int steps, final boolean isCall) {
    super(strike1, timeToExpiry, steps, isCall);
    ArgumentChecker.isTrue(strike2 > 0., "strike2 should be positive");
    ArgumentChecker.isTrue(Doubles.isFinite(strike2), "strike2 should be finite");
    _strike2 = strike2;
  }

  @Override
  public double[][] getPayoffAtExpiry(final double assetPrice1, final double assetPrice2, final double upOverDown1, final double upOverDown2) {
    final double strike1 = super.getStrike();
    final int nStepsP = getNumberOfSteps() + 1;
    final double sign = getSign();

    final double[][] values = new double[nStepsP][nStepsP];
    double priceTmp1 = assetPrice1;
    for (int i = 0; i < nStepsP; ++i) {
      double priceTmp2 = assetPrice2;
      for (int j = 0; j < nStepsP; ++j) {
        values[i][j] = sign * (priceTmp1 - strike1) > 0. ? Math.max(sign * (priceTmp2 - _strike2), 0.) : 0.;
        priceTmp2 *= upOverDown2;
      }
      priceTmp1 *= upOverDown1;
    }
    return values;
  }

  @Override
  public double[][] getPayoffAtExpiryTrinomial(final double assetPrice1, final double assetPrice2, final double middleOverDown1, final double middleOverDown2) {
    final double strike1 = super.getStrike();
    final int nNodes = 2 * getNumberOfSteps() + 1;
    final double sign = getSign();

    final double[][] values = new double[nNodes][nNodes];
    double priceTmp1 = assetPrice1;
    for (int i = 0; i < nNodes; ++i) {
      double priceTmp2 = assetPrice2;
      for (int j = 0; j < nNodes; ++j) {
        values[i][j] = sign * (priceTmp1 - strike1) > 0. ? Math.max(sign * (priceTmp2 - _strike2), 0.) : 0.;
        priceTmp2 *= middleOverDown2;
      }
      priceTmp1 *= middleOverDown1;
    }
    return values;
  }

  /**
   * Access strike for asset 1
   * @return strike1
   */
  public double getStrike1() {
    return super.getStrike();
  }

  /**
   * Access strike for asset 2
   * @return strike2
   */
  public double getStrike2() {
    return _strike2;
  }

  @Override
  public double getStrike() {
    throw new IllegalArgumentException("Specify strike for asset 1 or strike for asset 2");
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    long temp;
    temp = Double.doubleToLongBits(_strike2);
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
    if (!(obj instanceof TwoAssetCorrelationOptionFunctionProvider)) {
      return false;
    }
    TwoAssetCorrelationOptionFunctionProvider other = (TwoAssetCorrelationOptionFunctionProvider) obj;
    if (Double.doubleToLongBits(_strike2) != Double.doubleToLongBits(other._strike2)) {
      return false;
    }
    return true;
  }
}
