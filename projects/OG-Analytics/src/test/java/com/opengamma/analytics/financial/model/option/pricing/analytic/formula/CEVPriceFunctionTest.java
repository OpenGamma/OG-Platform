/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.pricing.analytic.formula;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.model.volatility.BlackImpliedVolatilityFormula;
import com.opengamma.analytics.financial.model.volatility.smile.function.SABRFormulaData;
import com.opengamma.analytics.financial.model.volatility.smile.function.SABRHaganVolatilityFunction;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class CEVPriceFunctionTest {
  private static final SABRHaganVolatilityFunction SABR = new SABRHaganVolatilityFunction();
  private static final CEVPriceFunction CEV = new CEVPriceFunction();
  private static final BlackImpliedVolatilityFormula BLACK_IMPLIED_VOL = new BlackImpliedVolatilityFormula();

  /**
   * For short dated options should have good agreement with the SABR formula for nu = 0
   */
  @Test
  public void testBeta() {
    final double f = 4;
    final double k = 3.5;
    final double atmVol = 0.3;
    final double t = 0.1;
    double beta;

    for (int i = 0; i < 200; i++) {
      beta = i / 100.0;
      final double sigma = atmVol * Math.pow(f, 1 - beta);
      final EuropeanVanillaOption option = new EuropeanVanillaOption(k, t, true);
      final CEVFunctionData cevData = new CEVFunctionData(f, 1.0, sigma, beta);
      final double price = CEV.getPriceFunction(option).evaluate(cevData);
      final double vol = BLACK_IMPLIED_VOL.getImpliedVolatility(new BlackFunctionData(f, 1.0, sigma), option, price);
      final SABRFormulaData sabrData = new SABRFormulaData(sigma, beta, 0.0, 0.0);
      final double sabrVol = SABR.getVolatilityFunction(option, f).evaluate(sabrData);
      assertEquals(sabrVol, vol, 1e-4);//TODO this used to work with 1e-5????
    }
  }

  @Test
  public void testStrike() {
    final double f = 4;
    double k;
    final double atmVol = 0.3;
    final double t = 0.5;
    final double beta = 0.5;
    final double sigma = atmVol * Math.pow(f, 1 - beta);

    for (int i = 0; i < 20; i++) {
      k = 1.0 + i / 2.5;
      final EuropeanVanillaOption option = new EuropeanVanillaOption(k, t, true);
      final CEVFunctionData cevData = new CEVFunctionData(f, 1.0, sigma, beta);
      final double price = CEV.getPriceFunction(option).evaluate(cevData);
      final double vol = BLACK_IMPLIED_VOL.getImpliedVolatility(new BlackFunctionData(f, 1.0, sigma), option, price);
      final SABRFormulaData sabrData = new SABRFormulaData(sigma, beta, 0.0, 0.0);
      final double sabrVol = SABR.getVolatilityFunction(option, f).evaluate(sabrData);
      assertEquals(sabrVol, vol, 1e-4);
    }
  }

  @Test
  public void testBetaAndStrike() {
    final double f = 4;
    double k;
    final double atmVol = 0.3;
    final double t = 0.1;
    double beta;
    double sigma;

    for (int i = 0; i < 20; i++) {
      beta = (i + 1) / 20.0;
      sigma = atmVol * Math.pow(f, 1 - beta);
      for (int j = 0; j < 20; j++) {
        k = 3.0 + j / 10.0;
        final EuropeanVanillaOption option = new EuropeanVanillaOption(k, t, true);
        final CEVFunctionData cevData = new CEVFunctionData(f, 1.0, sigma, beta);
        final double price = CEV.getPriceFunction(option).evaluate(cevData);
        final double vol = BLACK_IMPLIED_VOL.getImpliedVolatility(new BlackFunctionData(f, 1.0, sigma), option, price);
        final SABRFormulaData sabrData = new SABRFormulaData(sigma, beta, 0.0, 0.0);
        final double sabrVol = SABR.getVolatilityFunction(option, f).evaluate(sabrData);
        assertEquals(sabrVol, vol, 1e-4);//TODO this used to work with 1e-5????
      }
    }
  }

  @Test(enabled = false)
  public void funnySmileTest() {

    final double beta = 0.4;
    final double t = 5.0;
    final double r = 0.0;
    final double spot = 100;
    final double k = spot * Math.exp(-r * t);

    final double atmVol = 0.20;
    final double volBeta = atmVol * Math.pow(k, 1 - beta);

    final EuropeanVanillaOption option = new EuropeanVanillaOption(k, t, true);

    for (int i = 0; i < 101; i++) {
      final double f = 350.0 + 1.0 * i;
      final CEVFunctionData cevData = new CEVFunctionData(f, 1.0, volBeta, beta);
      final double cevPrice = CEV.getPriceFunction(option).evaluate(cevData);
      @SuppressWarnings("unused")
      final double cevVol = BLACK_IMPLIED_VOL.getImpliedVolatility(new BlackFunctionData(f, 1.0, volBeta), option, cevPrice);

      // System.out.println(f +"\t"+cevPrice+"\t"+cevVol);
    }

  }

  @Test
  public void testDebug() {

    final double beta = 0.4;
    final double t = 5.0;
    final double r = 0.1;
    final double forward = 163.10;
    final double k = 164.87;

    final double volBeta = 4.298;

    final EuropeanVanillaOption option = new EuropeanVanillaOption(k, t, true);

    final CEVFunctionData cevData = new CEVFunctionData(forward, Math.exp(-r * t), volBeta, beta);
    final double cevPrice = CEV.getPriceFunction(option).evaluate(cevData);
    @SuppressWarnings("unused")
    final double cevVol = BLACK_IMPLIED_VOL.getImpliedVolatility(new BlackFunctionData(forward, Math.exp(-r * t), volBeta), option, cevPrice);

    //System.out.println(cevPrice+"\t"+cevVol);

  }

}
