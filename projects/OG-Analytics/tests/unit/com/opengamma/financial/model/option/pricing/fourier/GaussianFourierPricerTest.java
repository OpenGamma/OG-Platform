/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.pricing.fourier;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.opengamma.financial.model.option.pricing.analytic.formula.BlackFunctionData;
import com.opengamma.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.financial.model.volatility.BlackImpliedVolatilityFormula;
import com.opengamma.math.number.ComplexNumber;
import com.opengamma.math.rootfinding.VanWijngaardenDekkerBrentSingleRootFinder;

/**
 * 
 */
public class GaussianFourierPricerTest {

  private static final double FORWARD = 1;
  private static final double T = 2.0;
  private static final double DF = 0.93;
  private static final double MU = 0.07;
  private static final double SIGMA = 0.2;
  private static final BlackImpliedVolatilityFormula BLACK_IMPLIED_VOL = new BlackImpliedVolatilityFormula(new VanWijngaardenDekkerBrentSingleRootFinder());

  private static final CharacteristicExponent CEF = new GaussianCharacteristicExponent(-0.5 * SIGMA * SIGMA, SIGMA, T);

  @Test
  public void test() {
    boolean isCall;
    final FourierPricer pricer = new FourierPricer(1e-10, 20);
    for (int i = 0; i < 21; i++) {
      final double k = 0.2 + 3 * i / 20.0;
      if (k > 1.0) {
        isCall = true;
      } else {
        isCall = false;
      }
      final double price = pricer.price(FORWARD, k * FORWARD, DF, isCall, CEF, -1.3, 1e-11);
      double impVol = 0;
      final EuropeanVanillaOption option = new EuropeanVanillaOption(k * FORWARD, T, isCall);
      final BlackFunctionData data = new BlackFunctionData(FORWARD, DF, 0);
      impVol = BLACK_IMPLIED_VOL.getImpliedVolatility(data, option, price);
      assertEquals(SIGMA, impVol, 1e-5);
    }
  }

  @Test
  public void testExpectation() {
    final double mu = 0.05;
    final double sigma = 0.2;
    final CharacteristicExponent ce = new GaussianCharacteristicExponent(mu, sigma, 1.0);
    final ComplexNumber res = ce.evaluate(new ComplexNumber(0, -1));
    assertEquals(mu + 0.5 * sigma * sigma, res.getReal(), 1e-12);
    assertEquals(0.0, res.getImaginary(), 1e-12);
  }

  @Test
  public void testIntegrand() {
    final EuropeanPriceIntegrand integrand = new EuropeanPriceIntegrand(CEF, 0.5, FORWARD, 1.1 * FORWARD, false, 0.15);
    // for (int i = 0; i < 100; i++) {
    // double x = -0. + i * 1000. / 100.0;
    // ComplexNumber res = intergrand.getIntegrand(x);
    // System.out.println(x + "\t" + res.getReal() + "\t" + res.getImaginary());
    // }
    // for (int i = 0; i < 101; i++) {
    // double x = 1000. + i * 10000. / 100.0;
    // ComplexNumber res = intergrand.getIntegrand(x);
    // System.out.println(x + "\t" + res.getReal() + "\t" + res.getImaginary());
    // }
  }

}
