/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.pricing.tree;

/**
 * 
 */
public class EuropeanVanillaOptionFunctionProvider extends OptionFunctionProvider {

  public EuropeanVanillaOptionFunctionProvider(final double strike, final int steps, final boolean isCall) {
    super(strike, steps, isCall);
  }

  @Override
  public double[] getPayoffAtExpiry(final double assetPrice, final double upOverDown) {
    final double strike = getStrike();
    final int nStepsP = getNumberOfSteps() + 1;
    final double sign = getSign();

    final double[] values = new double[nStepsP];
    double priceTmp = assetPrice;
    for (int i = 0; i < nStepsP; ++i) {
      values[i] = Math.max(sign * (priceTmp - strike), 0);
      priceTmp *= upOverDown;
    }
    return values;
  }

  @Override
  public double[] getNextOptionValues(final double discount, final double upProbability, final double downProbability, final double[] values, final double baseAssetPrice, final double downFactor,
      final double upOverDown, final int steps) {
    final int nStepsP = steps + 1;

    final double[] res = new double[nStepsP];
    for (int j = 0; j < nStepsP; ++j) {
      res[j] = discount * (upProbability * values[j + 1] + downProbability * values[j]);
    }
    return res;
  }

  //  /*
  //   * Functions for two-dimensional case
  //   */
  //  public double[][] getPayoffAtExpiry(final double assetPrice1, final double assetPrice2, final double strike, final int nSteps, final double sign, final double upOverDown1,
  //      final double upOverDown2) {
  //    final int nStepsP = nSteps + 1;
  //    final double[][] values = new double[nStepsP][nStepsP];
  //    double priceTmp1 = assetPrice1;
  //    for (int i = 0; i < nStepsP; ++i) {
  //      double priceTmp2 = assetPrice2;
  //      for (int j = 0; j < nStepsP; ++j) {
  //        values[i][j] = Math.max(sign * (priceTmp1 - priceTmp2 - strike), 0);
  //        priceTmp2 *= upOverDown2;
  //      }
  //      priceTmp1 *= upOverDown1;
  //    }
  //    return values;
  //  }
  //
  //  public double[][] getNextOptionValues(final double discount, final double uuProbability, final double udProbability, final double duProbability, final double ddProbability,
  //      final double[][] values, final int steps) {
  //    final int stepsP = steps + 1;
  //    final double[][] res = new double[stepsP][stepsP];
  //    for (int j = 0; j < stepsP; ++j) {
  //      for (int i = 0; i < stepsP; ++i) {
  //        res[j][i] = discount * (uuProbability * values[j + 1][i + 1] + udProbability * values[j + 1][i] + duProbability * values[j][i + 1] + ddProbability * values[j][i]);
  //      }
  //    }
  //    return res;
  //  }
  //
  //  public double[][] getNextOptionValues(final double discount, final double strike, final double uuProbability, final double udProbability, final double duProbability, final double ddProbability,
  //      final double[][] values, final double baseAssetPrice1, final double baseAssetPrice2, final double sign, final double downFactor1, final double downFactor2,
  //      final double upOverDown1, final double upOverDown2, final int steps) {
  //    final int stepsP = steps + 1;
  //    final double[][] res = new double[stepsP][stepsP];
  //    double assetPrice1 = baseAssetPrice1 * Math.pow(downFactor1, steps);
  //    for (int j = 0; j < stepsP; ++j) {
  //      double assetPrice2 = baseAssetPrice2 * Math.pow(downFactor2, steps);
  //      for (int i = 0; i < stepsP; ++i) {
  //        res[j][i] = discount * (uuProbability * values[j + 1][i + 1] + udProbability * values[j + 1][i] + duProbability * values[j][i + 1] + ddProbability * values[j][i]);
  //        res[j][i] = Math.max(res[j][i], sign * (assetPrice1 - assetPrice2 - strike));
  //        assetPrice2 *= upOverDown2;
  //      }
  //      assetPrice1 *= upOverDown1;
  //    }
  //    return res;
  //  }
}
