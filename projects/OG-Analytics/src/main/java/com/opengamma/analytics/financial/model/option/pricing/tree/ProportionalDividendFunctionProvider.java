/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.pricing.tree;

/**
 * 
 */
public class ProportionalDividendFunctionProvider extends DividendFunctionProvider {

  public ProportionalDividendFunctionProvider(final double[] dividendTimes, final double[] dividends) {
    super(dividendTimes, dividends);
  }

  public double spotModifier(final double spot, final double interestRate) {
    double res = spot;
    final double[] dividends = getDividends();
    final int nDiv = dividends.length;
    for (int i = 0; i < nDiv; ++i) {
      res *= (1. - dividends[i]);
    }
    return res;
  }

  public double dividendCorrections(final double assetPrice, final double interestRate, final double offset, final int k) {
    final double dividend = getDividends()[k];
    final double res = assetPrice / (1. - dividend);
    return res;
  }

  public double[] getAssetPricesForDelta(final double spot, final double interestRate, final int[] divSteps, final double upFactor, final double downFactor, final double sumDiscountDiv) {
    final double[] res = new double[2];
    final double[] dividends = getDividends();

    if (divSteps[0] == 0) {
      res[0] = spot * (1. - dividends[0]) * downFactor;
      res[1] = spot * (1. - dividends[0]) * upFactor;
    } else {
      res[0] = spot * downFactor;
      res[1] = spot * upFactor;
    }
    return res;
  }

  public double[] getAssetPricesForGamma(final double spot, final double interestRate, final int[] divSteps, final double upFactor, final double downFactor, final double sumDiscountDiv) {
    final double[] res = new double[3];
    final double[] dividends = getDividends();

    if (divSteps[1] == 1) {
      res[0] = spot * (1. - dividends[0]) * (1. - dividends[1]) * downFactor * downFactor;
      res[1] = spot * (1. - dividends[0]) * (1. - dividends[1]) * upFactor * downFactor;
      res[2] = spot * (1. - dividends[0]) * (1. - dividends[1]) * upFactor * upFactor;
    } else {
      if (divSteps[0] == 1) {
        res[0] = spot * (1. - dividends[0]) * downFactor * downFactor;
        res[1] = spot * (1. - dividends[0]) * upFactor * downFactor;
        res[2] = spot * (1. - dividends[0]) * upFactor * upFactor;
      } else {
        res[0] = spot * downFactor * downFactor;
        res[1] = spot * upFactor * downFactor;
        res[2] = spot * upFactor * upFactor;
      }
    }
    return res;
  }
}
