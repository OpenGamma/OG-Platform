/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.pricing.tree;

import org.testng.annotations.Test;

/**
 * Test class for {@link DividendFunctionProvider} and its subclasses, {@link CashDividendFunctionProvider} and {@link ProportionalDividendFunctionProvider}
 */
public class DividendFunctionProviderTest {
  private static final double[] _times = new double[] {1., 2., 3. };
  private static final double[] _dividends = new double[] {0.2, 0.1, 0.2 };

  /**
   * 
   */
  @SuppressWarnings("unused")
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullTimesTest() {
    new CashDividendFunctionProvider(null, _dividends);
  }

  /**
   * 
   */
  @SuppressWarnings("unused")
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullDividendsTest() {
    new CashDividendFunctionProvider(_times, null);
  }

  /**
   * 
   */
  @SuppressWarnings("unused")
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void negativeTimeTest() {
    final double[] times = new double[] {-1., 2., 3. };
    new CashDividendFunctionProvider(times, _dividends);
  }

  /**
   * 
   */
  @SuppressWarnings("unused")
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void negativeDividendTest() {
    final double[] dividends = new double[] {0.2, -0.1, 0.2 };
    new CashDividendFunctionProvider(_times, dividends);
  }

  /**
   * 
   */
  @SuppressWarnings("unused")
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void infiniteTimeTest() {
    final double[] times = new double[] {1., 2., Double.POSITIVE_INFINITY };
    new CashDividendFunctionProvider(times, _dividends);
  }

  /**
   * 
   */
  @SuppressWarnings("unused")
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void infiniteDividendTest() {
    final double[] dividends = new double[] {0.2, 0.1, Double.POSITIVE_INFINITY };
    new CashDividendFunctionProvider(_times, dividends);
  }

  /**
   * 
   */
  @SuppressWarnings("unused")
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void notInOrderTimeTest() {
    final double[] times = new double[] {1., 2., 1.5 };
    new CashDividendFunctionProvider(times, _dividends);
  }

  /**
   * 
   */
  @SuppressWarnings("unused")
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void wrongDataLengthTest() {
    final double[] times = new double[] {1., 2. };
    new CashDividendFunctionProvider(times, _dividends);
  }

  /**
   * 
   */
  @SuppressWarnings("unused")
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void tooBigCashDividendTest() {
    final double[] dividends = new double[] {1., 2., 300. };
    final DividendFunctionProvider div = new CashDividendFunctionProvider(_times, dividends);
    div.spotModifier(100, 0.05);
  }

  /**
   * 
   */
  @SuppressWarnings("unused")
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void dividendAfterExpiryPriceTest() {
    final double[] times = new double[] {1., 2., 45. };
    final DividendFunctionProvider div = new CashDividendFunctionProvider(times, _dividends);
    (new BinomialTreeOptionPricingModel()).getPrice(new TianLatticeSpecification(), new EuropeanVanillaOptionFunctionProvider(100, 10., 101, true), 100., 0.2, 0.05, div);
  }

  /**
   * 
   */
  @SuppressWarnings("unused")
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void dividendAfterExpiryGreeksTest() {
    final double[] times = new double[] {1., 2., 45. };
    final DividendFunctionProvider div = new CashDividendFunctionProvider(times, _dividends);
    (new BinomialTreeOptionPricingModel()).getGreeks(new TianLatticeSpecification(), new EuropeanVanillaOptionFunctionProvider(100, 10., 101, true), 100., 0.2, 0.05, div);
  }
}
