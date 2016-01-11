/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.BlackFunctionData;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.BlackPriceFunction;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.NormalFunctionData;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.NormalPriceFunction;
import com.opengamma.analytics.financial.model.volatility.smile.function.SABRFormulaData;
import com.opengamma.analytics.financial.model.volatility.smile.function.SABRHaganVolatilityFunction;
import com.opengamma.util.test.TestGroup;

/**
 * Test {@link BlackImpliedVolatilityFormula}.
 */
@Test(groups = TestGroup.UNIT)
public class BlackImpliedVolatilityFormulaTest {
  private static final double FORWARD = 134.5;
  private static final double DF = 0.87;
  private static final double T = 4.5;
  private static final double SIGMA = 0.2;
  private static final int N = 10;
  private static final BlackFunctionData[] DATA = new BlackFunctionData[N];
  private static final EuropeanVanillaOption[] OPTIONS = new EuropeanVanillaOption[N];
  private static final double[] SIGMA_NORMAL = new double[N];
  private static final double[] PRICES = new double[N];
  private static final double[] STRIKES = new double[N];
  private static final double[] STRIKES_ATM = new double[N];
  private static final BlackPriceFunction FORMULA = new BlackPriceFunction();
  private static final NormalPriceFunction FUNCTION_PRICE_NORMAL = new NormalPriceFunction();
  private static final SABRHaganVolatilityFunction SABR = new SABRHaganVolatilityFunction();
  private static final SABRFormulaData SABR_DATA;
  
  private static final double TOLERANCE_PRICE = 1.0E-4;

  static {
    for (int i = 0; i < 10; i++) {
      STRIKES[i] = FORWARD - 20 + 40 / N * i;
      STRIKES_ATM[i] = FORWARD + (-0.5d * N + i) / 100.0d;
      DATA[i] = new BlackFunctionData(FORWARD, DF, SIGMA);
      OPTIONS[i] = new EuropeanVanillaOption(STRIKES[i], T, true);
      PRICES[i] = FORMULA.getPriceFunction(OPTIONS[i]).evaluate(DATA[i]);
      SIGMA_NORMAL[i] = 15.0 + i / 10.0d;
    }

    double beta = 0.6;
    double alpha = Math.pow(SIGMA, 1 - beta);
    double rho = -0.3;
    double nu = 0.4;
    SABR_DATA = new SABRFormulaData(alpha, beta, rho, nu);
  }

  @Test
  public void test() {
    final BlackImpliedVolatilityFormula formula = new BlackImpliedVolatilityFormula();
    for (int i = 0; i < N; i++) {
      final double vol = formula.getImpliedVolatility(DATA[i], OPTIONS[i], PRICES[i]);
      assertEquals(SIGMA, vol, 1e-6);
    }
  }

  @Test
  public void flatTest() {
    final double rootT = Math.sqrt(T);
    for (int i = 0; i < 51; i++) {
      double d = -5 + 12.0 * i / 50.;
      double k = FORWARD * Math.exp(d * rootT);
      boolean isCall = k >= FORWARD;

      double price = BlackFormulaRepository.price(FORWARD, k, T, SIGMA, isCall);
      double impVol = BlackFormulaRepository.impliedVolatility(price, FORWARD, k, T, isCall);
      assertEquals(SIGMA, impVol, 1e-6);
    }
  }

  @Test
  public void sabrTest() {
    final double rootT = Math.sqrt(T);
    //this has a lowest price of 4e-18
    for (int i = 0; i < 51; i++) {
      double d = -9.0 + 12.0 * i / 50.;
      double k = FORWARD * Math.exp(d * rootT);
      boolean isCall = k >= FORWARD;
      double vol = SABR.getVolatility(new EuropeanVanillaOption(k, T, true), FORWARD, SABR_DATA);
      double price = BlackFormulaRepository.price(FORWARD, k, T, vol, isCall);
      double impVol = BlackFormulaRepository.impliedVolatility(price, FORWARD, k, T, isCall);
      //   System.out.println(k + "\t" + price + "\t" + vol + "\t" + impVol);
      assertEquals(vol, impVol, 1e-8);
    }
    //this has a lowest price of 1e-186
    for (int i = 0; i < 21; i++) {
      double d = 3.0 + 4.0 * i / 20.;
      double k = FORWARD * Math.exp(d * rootT);
      boolean isCall = k >= FORWARD;
      double vol = SABR.getVolatility(new EuropeanVanillaOption(k, T, true), FORWARD, SABR_DATA);
      double price = BlackFormulaRepository.price(FORWARD, k, T, vol, isCall);
      double impVol = BlackFormulaRepository.impliedVolatility(price, FORWARD, k, T, isCall);
      //  System.out.println(k + "\t" + price + "\t" + vol + "\t" + impVol);
      assertEquals(vol, impVol, 1e-3);
    }
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void wrong_strike() {
    BlackImpliedVolatilityFormula.impliedVolatilityFromNormalApproximated(FORWARD, -1.0d, T, 0.20d);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void wrong_forward() {
    BlackImpliedVolatilityFormula.impliedVolatilityFromNormalApproximated(-1.0d, FORWARD, T, 0.20d);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void wrong_strike2() {
    BlackImpliedVolatilityFormula.impliedVolatilityFromNormalApproximated2(FORWARD, -1.0d, T, 0.20d);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void wrong_forward2() {
    BlackImpliedVolatilityFormula.impliedVolatilityFromNormalApproximated2(-1.0d, FORWARD, T, 0.20d);
  }


  @Test
  public void price_comparison_normal() {
    priceCheck(STRIKES);
    priceCheck(STRIKES_ATM);
  }

  private void priceCheck(double[] strikes) {
    for (int i = 5; i < N; i++) {
      double ivBlackComputed = BlackImpliedVolatilityFormula
          .impliedVolatilityFromNormalApproximated(FORWARD, strikes[i], T, SIGMA_NORMAL[i]);
      EuropeanVanillaOption o = new EuropeanVanillaOption(strikes[i], T, true);
      NormalFunctionData d = new NormalFunctionData(FORWARD, DF, SIGMA_NORMAL[i]);
      double priceBlackComputed = BlackFormulaRepository.price(FORWARD, strikes[i], T, ivBlackComputed, true) * DF;
      double priceNormal = FUNCTION_PRICE_NORMAL.getPriceFunction(o).evaluate(d);
      assertEquals("check " + i, 
          priceNormal, priceBlackComputed, TOLERANCE_PRICE);
    }
  }

}
