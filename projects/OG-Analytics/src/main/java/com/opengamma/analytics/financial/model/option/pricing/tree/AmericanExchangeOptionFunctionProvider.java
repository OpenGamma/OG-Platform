/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.pricing.tree;

import com.google.common.primitives.Doubles;
import com.opengamma.util.ArgumentChecker;

/**
 * {@link EuropeanExchangeOptionFunctionProvider} with early exercise feature
 */
public class AmericanExchangeOptionFunctionProvider extends OptionFunctionProvider2D {
  private double _quantity1;
  private double _quantity2;

  /**
   * @param timeToExpiry Time to expiry
   * @param steps Number of steps
   * @param quantity1 Quantity of asset 1
   * @param quantity2 Quantity of asset 2
   */
  public AmericanExchangeOptionFunctionProvider(final double timeToExpiry, final int steps, final double quantity1, final double quantity2) {
    super(0., timeToExpiry, steps, true);
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
    final double assetPrice2Rest = baseAssetPrice2 * Math.pow(downFactor2, steps);
    final double[][] res = new double[stepsP][stepsP];

    double assetPrice1 = baseAssetPrice1 * Math.pow(downFactor1, steps);
    for (int j = 0; j < stepsP; ++j) {
      double assetPrice2 = assetPrice2Rest;
      for (int i = 0; i < stepsP; ++i) {
        res[j][i] = discount * (uuProbability * values[j + 1][i + 1] + udProbability * values[j + 1][i] + duProbability * values[j][i + 1] + ddProbability * values[j][i]);
        res[j][i] = Math.max(res[j][i], _quantity1 * assetPrice1 - _quantity2 * assetPrice2);
        assetPrice2 *= upOverDown2;
      }
      assetPrice1 *= upOverDown1;
    }
    return res;
  }

  @Override
  public double[][] getPayoffAtExpiryTrinomial(final double assetPrice1, final double assetPrice2, final double middleOverDown1, final double middleOverDown2) {
    final int nNodes = 2 * getNumberOfSteps() + 1;

    final double[][] values = new double[nNodes][nNodes];
    double priceTmp1 = assetPrice1;
    for (int i = 0; i < nNodes; ++i) {
      double priceTmp2 = assetPrice2;
      for (int j = 0; j < nNodes; ++j) {
        values[i][j] = Math.max(_quantity1 * priceTmp1 - _quantity2 * priceTmp2, 0.);
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
    final double assetPrice2Rest = baseAssetPrice2 * Math.pow(downFactor2, steps);
    final double[][] res = new double[nNodes][nNodes];

    double assetPrice1 = baseAssetPrice1 * Math.pow(downFactor1, steps);
    for (int j = 0; j < nNodes; ++j) {
      double assetPrice2 = assetPrice2Rest;
      for (int i = 0; i < nNodes; ++i) {
        res[j][i] = discount * (uuProbability * values[j + 2][i + 2] + umProbability * values[j + 2][i + 1] + udProbability * values[j + 2][i] + muProbability * values[j + 1][i + 2] + mmProbability *
            values[j + 1][i + 1] + mdProbability * values[j + 1][i] + duProbability * values[j][i + 2] + dmProbability * values[j][i + 1] + ddProbability * values[j][i]);
        res[j][i] = Math.max(res[j][i], _quantity1 * assetPrice1 - _quantity2 * assetPrice2);
        assetPrice2 *= middleOverDown2;
      }
      assetPrice1 *= middleOverDown1;
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

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    long temp;
    temp = Double.doubleToLongBits(_quantity1);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_quantity2);
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
    if (!(obj instanceof AmericanExchangeOptionFunctionProvider)) {
      return false;
    }
    AmericanExchangeOptionFunctionProvider other = (AmericanExchangeOptionFunctionProvider) obj;
    if (Double.doubleToLongBits(_quantity1) != Double.doubleToLongBits(other._quantity1)) {
      return false;
    }
    if (Double.doubleToLongBits(_quantity2) != Double.doubleToLongBits(other._quantity2)) {
      return false;
    }
    return true;
  }

}
