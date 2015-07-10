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

  /**
   * @param dividendTimes The dividend times
   * @param dividends Decrease ratios of asset price because of proportional dividends
   */
  public ProportionalDividendFunctionProvider(final double[] dividendTimes, final double[] dividends) {
    super(dividendTimes, dividends);
  }

  @Override
  public double spotModifier(final double spot, final double interestRate) {
    double res = spot;
    final double[] dividends = getDividends();
    final int nDiv = dividends.length;
    for (int i = 0; i < nDiv; ++i) {
      res *= (1. - dividends[i]);
    }
    return res;
  }

  @Override
  public double dividendCorrections(final double assetPrice, final double interestRate, final double offset, final int k) {
    final double dividend = getDividends()[k];
    final double res = assetPrice / (1. - dividend);
    return res;
  }

  @Override
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

  @Override
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

  @Override
  public double[] getAssetPricesForDelta(final double spot, final double interestRate, final int[] divSteps, final double upFactor, final double middleFactor, final double downFactor,
      final double sumDiscountDiv) {
    final double[] res = new double[3];
    final double[] dividends = getDividends();

    if (divSteps[0] == 0) {
      res[0] = spot * (1. - dividends[0]) * downFactor;
      res[1] = spot * (1. - dividends[0]) * middleFactor;
      res[2] = spot * (1. - dividends[0]) * upFactor;
    } else {
      res[0] = spot * downFactor;
      res[1] = spot * middleFactor;
      res[2] = spot * upFactor;
    }
    return res;
  }

  @Override
  public double[] getAssetPricesForGamma(final double spot, final double interestRate, final int[] divSteps, final double upFactor, final double middleFactor, final double downFactor,
      final double sumDiscountDiv) {
    final double[] res = new double[5];
    final double[] dividends = getDividends();

    double down = spot * downFactor;
    double up = spot * upFactor;
    if (divSteps[1] == 1) {
      down *= (1. - dividends[0]) * (1. - dividends[1]);
      up *= (1. - dividends[0]) * (1. - dividends[1]);
    } else {
      if (divSteps[0] == 1) {
        down *= (1. - dividends[0]);
        up *= (1. - dividends[0]);
      }
    }
    res[0] = down * downFactor;
    res[1] = down * middleFactor;
    res[2] = down * upFactor;
    res[3] = up * middleFactor;
    res[4] = up * upFactor;
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
    if (!(obj instanceof ProportionalDividendFunctionProvider)) {
      return false;
    }
    return super.equals(obj);
  }
}
