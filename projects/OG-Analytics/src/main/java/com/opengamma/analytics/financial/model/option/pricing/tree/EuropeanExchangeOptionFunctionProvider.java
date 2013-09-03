/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.pricing.tree;

import com.google.common.primitives.Doubles;
import com.opengamma.util.ArgumentChecker;

/**
 *  The payoff of European exchange-one-asset-for-another option is max(Q1 * S1 - Q2 * S2, 0) at expiration, 
 *  where Q1 is the quantity of asset S1 and Q2 is the quantity of asset S2.
 */
public class EuropeanExchangeOptionFunctionProvider extends OptionFunctionProvider2D {
  private double _quantity1;
  private double _quantity2;

  /**
   * @param steps Number of steps
   * @param quantity1 Quantity of asset 1
   * @param quantity2 Quantity of asset 2
   */
  public EuropeanExchangeOptionFunctionProvider(final int steps, final double quantity1, final double quantity2) {
    super(0., steps, true);
    ArgumentChecker.isTrue(quantity1 > 0., "quantity1 should be positive");
    ArgumentChecker.isTrue(Doubles.isFinite(quantity1), "quantity1 should be finite");
    ArgumentChecker.isTrue(quantity2 > 0., "quantity2 should be positive");
    ArgumentChecker.isTrue(Doubles.isFinite(quantity2), "quantity2 should be finite");

    _quantity1 = quantity1;
    _quantity2 = quantity2;
  }

  @Override
  public double[][] getPayoffAtExpiry(final double assetPrice1, final double assetPrice2, final double upOverDown1, final double upOverDown2) {
    final int nStepsP = getNumberOfSteps() + 1;

    final double[][] values = new double[nStepsP][nStepsP];
    double priceTmp1 = assetPrice1;
    for (int i = 0; i < nStepsP; ++i) {
      double priceTmp2 = assetPrice2;
      for (int j = 0; j < nStepsP; ++j) {
        values[i][j] = Math.max(_quantity1 * priceTmp1 - _quantity2 * priceTmp2, 0.);
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
    final double[][] res = new double[stepsP][stepsP];
    for (int j = 0; j < stepsP; ++j) {
      for (int i = 0; i < stepsP; ++i) {
        res[j][i] = discount * (uuProbability * values[j + 1][i + 1] + udProbability * values[j + 1][i] + duProbability * values[j][i + 1] + ddProbability * values[j][i]);
      }
    }
    return res;
  }

  @Override
  public double getSign() {
    throw new IllegalArgumentException("Call/put is not relevant for this option");
  }

  @Override
  public double getStrike() {
    throw new IllegalArgumentException("Strike is not relavant for this option");
  }

  /**
   * Access quantity of asset 1
   * @return _quantity1
   */
  public double getQuantity1() {
    return _quantity1;
  }

  /**
   * Access quantity of asset 2
   * @return _quantity2
   */
  public double getQuantity2() {
    return _quantity2;
  }
}
