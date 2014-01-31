/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.pricing.tree;


/**
 * European call spread option pays max(S1-S2-K,0) whereas European put spread option pays max(K-S1+S2,0)
 */
public class EuropeanSpreadOptionFunctionProvider extends OptionFunctionProvider2D {

  /**
   * @param strike Strike price
   * @param timeToExpiry Time to expiry
   * @param steps Number of steps
   * @param isCall True if call, false if put
   */
  public EuropeanSpreadOptionFunctionProvider(final double strike, final double timeToExpiry, final int steps, final boolean isCall) {
    super(strike, timeToExpiry, steps, isCall);
  }

  @Override
  public double[][] getPayoffAtExpiry(final double assetPrice1, final double assetPrice2, final double upOverDown1, final double upOverDown2) {
    final double strike = getStrike();
    final int nStepsP = getNumberOfSteps() + 1;
    final double sign = getSign();

    final double[][] values = new double[nStepsP][nStepsP];
    double priceTmp1 = assetPrice1;
    for (int i = 0; i < nStepsP; ++i) {
      double priceTmp2 = assetPrice2;
      for (int j = 0; j < nStepsP; ++j) {
        values[i][j] = Math.max(sign * (priceTmp1 - priceTmp2 - strike), 0.);
        priceTmp2 *= upOverDown2;
      }
      priceTmp1 *= upOverDown1;
    }
    return values;
  }

  @Override
  public double[][] getPayoffAtExpiryTrinomial(final double assetPrice1, final double assetPrice2, final double middleOverDown1, final double middleOverDown2) {
    final double strike = getStrike();
    final int nNodes = 2 * getNumberOfSteps() + 1;
    final double sign = getSign();

    final double[][] values = new double[nNodes][nNodes];
    double priceTmp1 = assetPrice1;
    for (int i = 0; i < nNodes; ++i) {
      double priceTmp2 = assetPrice2;
      for (int j = 0; j < nNodes; ++j) {
        values[i][j] = Math.max(sign * (priceTmp1 - priceTmp2 - strike), 0.);
        priceTmp2 *= middleOverDown2;
      }
      priceTmp1 *= middleOverDown1;
    }
    return values;
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
    if (!(obj instanceof EuropeanSpreadOptionFunctionProvider)) {
      return false;
    }
    return super.equals(obj);
  }
}
