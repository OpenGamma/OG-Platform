/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.pricing.tree;


/**
 * {@link EuropeanSpreadOptionFunctionProvider} with early exercise feature
 */
public class AmericanSpreadOptionFunctionProvider extends OptionFunctionProvider2D {

  /**
   * @param strike Strike price
   * @param timeToExpiry Time to expiry
   * @param steps Number of steps
   * @param isCall True if call, false if put
   */
  public AmericanSpreadOptionFunctionProvider(final double strike, final double timeToExpiry, final int steps, final boolean isCall) {
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
  public double[][] getNextOptionValues(final double discount, final double uuProbability, final double udProbability, final double duProbability, final double ddProbability,
      final double[][] values, final double baseAssetPrice1, final double baseAssetPrice2, final double downFactor1, final double downFactor2,
      final double upOverDown1, final double upOverDown2, final int steps) {
    final int stepsP = steps + 1;
    final double strike = getStrike();
    final double sign = getSign();
    final double assetPrice2Rest = baseAssetPrice2 * Math.pow(downFactor2, steps);

    final double[][] res = new double[stepsP][stepsP];
    double assetPrice1 = baseAssetPrice1 * Math.pow(downFactor1, steps);
    for (int j = 0; j < stepsP; ++j) {
      double assetPrice2 = assetPrice2Rest;
      for (int i = 0; i < stepsP; ++i) {
        res[j][i] = discount * (uuProbability * values[j + 1][i + 1] + udProbability * values[j + 1][i] + duProbability * values[j][i + 1] + ddProbability * values[j][i]);
        res[j][i] = Math.max(res[j][i], sign * (assetPrice1 - assetPrice2 - strike));
        assetPrice2 *= upOverDown2;
      }
      assetPrice1 *= upOverDown1;
    }
    return res;
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
  public double[][] getNextOptionValues(final double discount, final double uuProbability, final double umProbability, final double udProbability, final double muProbability,
      final double mmProbability, final double mdProbability, final double duProbability, final double dmProbability, final double ddProbability, final double[][] values,
      final double baseAssetPrice1, final double baseAssetPrice2, final double downFactor1, final double downFactor2, final double middleOverDown1, final double middleOverDown2, final int steps) {
    final int nNodes = 2 * steps + 1;
    final double strike = getStrike();
    final double sign = getSign();
    final double assetPrice2Rest = baseAssetPrice2 * Math.pow(downFactor2, steps);

    final double[][] res = new double[nNodes][nNodes];
    double assetPrice1 = baseAssetPrice1 * Math.pow(downFactor1, steps);
    for (int j = 0; j < nNodes; ++j) {
      double assetPrice2 = assetPrice2Rest;
      for (int i = 0; i < nNodes; ++i) {
        res[j][i] = discount * (uuProbability * values[j + 2][i + 2] + umProbability * values[j + 2][i + 1] + udProbability * values[j + 2][i] + muProbability * values[j + 1][i + 2] + mmProbability *
            values[j + 1][i + 1] + mdProbability * values[j + 1][i] + duProbability * values[j][i + 2] + dmProbability * values[j][i + 1] + ddProbability * values[j][i]);
        res[j][i] = Math.max(res[j][i], sign * (assetPrice1 - assetPrice2 - strike));
        assetPrice2 *= middleOverDown2;
      }
      assetPrice1 *= middleOverDown1;
    }
    return res;
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
    if (!(obj instanceof AmericanSpreadOptionFunctionProvider)) {
      return false;
    }
    return super.equals(obj);
  }
}
