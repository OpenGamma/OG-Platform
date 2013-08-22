/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.pricing.tree;

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

  public double spotModifier(final double spot, final double interestRate) {
    double res = spot;
    final double[] dividendTimes = getDividendTimes();
    final double[] dividends = getDividends();
    final int nDiv = dividends.length;
    for (int i = 0; i < nDiv; ++i) {
      res -= dividends[i] * Math.exp(-interestRate * dividendTimes[i]);
    }
    return res;
  }

  public double dividendCorrections(final double sumDiscountDiv, final double interestRate, final double offset, final int k) {
    final double dividendTime = getDividendTimes()[k];
    final double dividend = getDividends()[k];
    final double res = sumDiscountDiv + dividend * Math.exp(-interestRate * (dividendTime - offset));
    return res;
  }

  public double[] getAssetPricesForDelta(final double assetPriceBase, final double interestRate, final int[] divSteps, final double upFactor, final double downFactor, final double sumDiscountDiv) {
    final double[] res = new double[2];
    res[0] = assetPriceBase * downFactor + sumDiscountDiv;
    res[1] = assetPriceBase * upFactor + sumDiscountDiv;
    return res;
  }

  public double[] getAssetPricesForGamma(final double assetPriceBase, final double interestRate, final int[] divSteps, final double upFactor, final double downFactor, final double sumDiscountDiv) {
    final double[] res = new double[3];
    res[0] = assetPriceBase * downFactor * downFactor + sumDiscountDiv;
    res[1] = assetPriceBase * upFactor * downFactor + sumDiscountDiv;
    res[2] = assetPriceBase * upFactor * upFactor + sumDiscountDiv;
    return res;
  }
}
