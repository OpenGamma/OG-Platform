/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.pricing.tree;

import java.util.Arrays;

import com.google.common.primitives.Doubles;
import com.opengamma.util.ArgumentChecker;

/**
 * Provide functions of discrete dividend, needed for tree option pricing
 */
public abstract class DividendFunctionProvider {

  private double[] _dividendTimes;
  private double[] _dividends;
  private int _nDividends;

  /**
   * Constructor. Dividend data must be sorted in chronological order
   * @param dividendTimes The dividend times
   * @param dividends The dividend payment amount/ratio
   */
  public DividendFunctionProvider(final double[] dividendTimes, final double[] dividends) {
    ArgumentChecker.notNull(dividendTimes, "dividendTimes");
    ArgumentChecker.notNull(dividends, "dividends");

    final int nDiv = dividendTimes.length;
    ArgumentChecker.isTrue(nDiv == dividends.length, "Wrong data length");
    for (int i = 0; i < nDiv; ++i) {
      ArgumentChecker.isTrue(dividendTimes[i] > 0., "dividendTimes should be positive");
      ArgumentChecker.isTrue(Doubles.isFinite(dividendTimes[i]), "dividendTimes should be finite");
      ArgumentChecker.isTrue(dividends[i] > 0., "dividends should be positive");
      ArgumentChecker.isTrue(Doubles.isFinite(dividends[i]), "dividends should be finite");
    }
    for (int i = 1; i < nDiv; ++i) {
      ArgumentChecker.isTrue(dividendTimes[i] - dividendTimes[i - 1] > 0., "dividendTimes should be in ascending order");
    }

    _dividendTimes = Arrays.copyOf(dividendTimes, nDiv);
    _dividends = Arrays.copyOf(dividends, nDiv);
    _nDividends = nDiv;
  }

  /**
   * Compute the asset price modified due to dividend payments
   * @param spot The spot price of asset
   * @param interestRate The interest rate
   * @return  The modified spot price
   */
  public abstract double spotModifier(final double spot, final double interestRate);

  /**
   * Compute correction to asset price due to dividends up to the k-th payment
   * @param assetPrice The asset price just before the k-th payment
   * @param interestRate The interest rate 
   * @param offset Time in the layer just before the k-th payment
   * @param k  
   * @return The correction
   */
  public abstract double dividendCorrections(final double assetPrice, final double interestRate, final double offset, final int k);

  /**
   * Asset prices in the 1st layer, i.e., S_{10} and S_{11}
   * @param spot The spot
   * @param interestRate The interest rate 
   * @param divSteps The position of layers where dividends are paid
   * @param upFactor Up factor
   * @param downFactor Down factor
   * @param sumDiscountDiv Sum of discounted (cash) dividends
   * @return { S_{10}, S_{11} }
   */
  public abstract double[] getAssetPricesForDelta(final double spot, final double interestRate, final int[] divSteps, final double upFactor, final double downFactor, final double sumDiscountDiv);

  /**
   * Asset prices in the second layer, i.e., S_{20}, S_{21} and S_{22}
   * @param spot The spot
   * @param interestRate The interest rate 
   * @param divSteps The positions of layers where dividends are paid
   * @param upFactor Up factor
   * @param downFactor Down factor
   * @param sumDiscountDiv Sum of discounted (cash) dividends
   * @return { S_{20}, S_{21}, S_{22} }
   */
  public abstract double[] getAssetPricesForGamma(final double spot, final double interestRate, final int[] divSteps, final double upFactor, final double downFactor, final double sumDiscountDiv);

  /**
   * Asset prices in the 1st layer, i.e., S_{10} and S_{11}
   * @param spot The spot
   * @param interestRate The interest rate 
   * @param divSteps The position of layers where dividends are paid
   * @param upFactor Up factor
   * @param middleFactor Middle factor
   * @param downFactor Down factor
   * @param sumDiscountDiv Sum of discounted (cash) dividends
   * @return { S_{10}, S_{11}, S_{12} }
   */
  public abstract double[] getAssetPricesForDelta(final double spot, final double interestRate, final int[] divSteps, final double upFactor, final double middleFactor, final double downFactor,
      final double sumDiscountDiv);

  /**
   * Asset prices in the second layer, i.e., S_{20}, S_{21} and S_{22}
   * @param spot The spot
   * @param interestRate The interest rate 
   * @param divSteps The positions of layers where dividends are paid
   * @param upFactor Up factor
   * @param middleFactor Middle factor
   * @param downFactor Down factor
   * @param sumDiscountDiv Sum of discounted (cash) dividends
   * @return { S_{20}, S_{21}, S_{22}, S_{23}, S_{24} }
   */
  public abstract double[] getAssetPricesForGamma(final double spot, final double interestRate, final int[] divSteps, final double upFactor, final double middleFactor, final double downFactor,
      final double sumDiscountDiv);

  /**
   * @param dt Time step
   * @return The positions of layers where dividends are paid
   */
  public int[] getDividendSteps(final double dt) {
    final int nDivs = _dividendTimes.length;
    final int[] divSteps = new int[nDivs];
    for (int i = 0; i < nDivs; ++i) {
      divSteps[i] = (int) (_dividendTimes[i] / dt);
    }

    return divSteps;
  }

  /**
   * Compare time step width with payment time width
   * @param dt Time step width
   * @return True if (time step width) < (payment time width) for all dividends
   */
  public boolean checkTimeSteps(final double dt) {
    final int nDivM = _nDividends - 1;
    for (int i = 0; i < nDivM; ++i) {
      if (_dividendTimes[i + 1] - _dividendTimes[i] < dt) {
        return false;
      }
    }
    return true;
  }

  /**
   * @param timeToExpiry Time to expiry
   * @return True if all of the dividend times are before expiry, false otherwise
   */
  public boolean checkDividendBeforeExpiry(final double timeToExpiry) {
    final int nDiv = _nDividends;
    for (int i = 0; i < nDiv; ++i) {
      if (_dividendTimes[i] > timeToExpiry) {
        return false;
      }
    }
    return true;
  }

  /**
   * Access dividend times
   * @return _dividendTimes
   */
  public double[] getDividendTimes() {
    return _dividendTimes;
  }

  /**
   * Access dividend amount/ratio
   * @return _dividends
   */
  public double[] getDividends() {
    return _dividends;
  }

  /**
   * Access number of dividend payments
   * @return _nDividends
   */
  public int getNumberOfDividends() {
    return _nDividends;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + Arrays.hashCode(_dividendTimes);
    result = prime * result + Arrays.hashCode(_dividends);
    result = prime * result + _nDividends;
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    /*
     * This case is always successful because this equals() is necessarily called by a subclass
     */
    DividendFunctionProvider other = (DividendFunctionProvider) obj;
    if (!Arrays.equals(_dividendTimes, other._dividendTimes)) {
      return false;
    }
    if (!Arrays.equals(_dividends, other._dividends)) {
      return false;
    }
    return true;
  }
}
