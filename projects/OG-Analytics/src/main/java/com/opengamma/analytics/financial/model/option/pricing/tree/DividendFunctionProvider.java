/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.pricing.tree;

import java.util.Arrays;

import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public abstract class DividendFunctionProvider {

  private double[] _dividendTimes;
  private double[] _dividends;
  private int _nDividends;

  public DividendFunctionProvider(final double[] dividendTimes, final double[] dividends) {
    final int nDiv = dividendTimes.length;
    ArgumentChecker.isTrue(nDiv == dividends.length, "Wrong data length");

    _dividendTimes = Arrays.copyOf(dividendTimes, nDiv);
    _dividends = Arrays.copyOf(dividends, nDiv);
    _nDividends = nDiv;
  }

  public abstract double spotModifier(final double spot, final double interestRate);

  public abstract double dividendCorrections(final double assetPrice, final double interestRate, final double offset, final int k);

  public abstract double[] getAssetPricesForDelta(final double spot, final double interestRate, final int[] divSteps, final double upFactor, final double downFactor, final double sumDiscountDiv);

  public abstract double[] getAssetPricesForGamma(final double spot, final double interestRate, final int[] divSteps, final double upFactor, final double downFactor, final double sumDiscountDiv);

  public int[] getDividendSteps(final double dt) {
    final int nDivs = _dividendTimes.length;
    final int[] divSteps = new int[nDivs];
    for (int i = 0; i < nDivs; ++i) {
      divSteps[i] = (int) (_dividendTimes[i] / dt);
    }

    return divSteps;
  }

  public double[] getDividendTimes() {
    return _dividendTimes;
  }

  public double[] getDividends() {
    return _dividends;
  }

  public int getNumberOfDividends() {
    return _nDividends;
  }
}
