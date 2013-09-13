/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.pricing.tree;

import com.opengamma.util.ArgumentChecker;

/**
 *
 */
public class CashDividendFunctionProvider extends DividendFunctionProvider {

  /**
   * @param dividendTimes The dividend times
   * @param dividends The cash dividends
   */
  public CashDividendFunctionProvider(final double[] dividendTimes, final double[] dividends) {
    super(dividendTimes, dividends);
  }

  @Override
  public double spotModifier(final double spot, final double interestRate) {
    double res = spot;
    final double[] dividendTimes = getDividendTimes();
    final double[] dividends = getDividends();
    final int nDiv = dividends.length;
    for (int i = 0; i < nDiv; ++i) {
      res -= dividends[i] * Math.exp(-interestRate * dividendTimes[i]);
    }
    ArgumentChecker.isTrue(res > 0., "Dividends are too large");
    return res;
  }

  @Override
  public double dividendCorrections(final double sumDiscountDiv, final double interestRate, final double offset, final int k) {
    final double dividendTime = getDividendTimes()[k];
    final double dividend = getDividends()[k];
    final double res = sumDiscountDiv + dividend * Math.exp(-interestRate * (dividendTime - offset));
    return res;
  }

  @Override
  public double[] getAssetPricesForDelta(final double assetPriceBase, final double interestRate, final int[] divSteps, final double upFactor, final double downFactor, final double sumDiscountDiv) {
    final double[] res = new double[2];
    res[0] = assetPriceBase * downFactor + sumDiscountDiv;
    res[1] = assetPriceBase * upFactor + sumDiscountDiv;
    return res;
  }

  @Override
  public double[] getAssetPricesForGamma(final double assetPriceBase, final double interestRate, final int[] divSteps, final double upFactor, final double downFactor, final double sumDiscountDiv) {
    final double[] res = new double[3];
    res[0] = assetPriceBase * downFactor * downFactor + sumDiscountDiv;
    res[1] = assetPriceBase * upFactor * downFactor + sumDiscountDiv;
    res[2] = assetPriceBase * upFactor * upFactor + sumDiscountDiv;
    return res;
  }

  @Override
  public double[] getAssetPricesForDelta(final double assetPriceBase, final double interestRate, final int[] divSteps, final double upFactor, final double middleFactor, final double downFactor,
      final double sumDiscountDiv) {
    final double[] res = new double[3];
    res[0] = assetPriceBase * downFactor + sumDiscountDiv;
    res[1] = assetPriceBase * middleFactor + sumDiscountDiv;
    res[2] = assetPriceBase * upFactor + sumDiscountDiv;
    return res;
  }

  @Override
  public double[] getAssetPricesForGamma(final double assetPriceBase, final double interestRate, final int[] divSteps, final double upFactor, final double middleFactor, final double downFactor,
      final double sumDiscountDiv) {
    final double[] res = new double[5];
    final double up = assetPriceBase * upFactor;
    final double down = assetPriceBase * downFactor;

    res[0] = down * downFactor + sumDiscountDiv;
    res[1] = down * middleFactor + sumDiscountDiv;
    res[2] = down * upFactor + sumDiscountDiv;
    res[3] = up * middleFactor + sumDiscountDiv;
    res[4] = up * upFactor + sumDiscountDiv;
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
    if (!(obj instanceof CashDividendFunctionProvider)) {
      return false;
    }
    return super.equals(obj);
  }
}
