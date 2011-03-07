/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.pricing.fourier;

import org.junit.Test;

import com.opengamma.financial.model.option.pricing.analytic.formula.BlackFunctionData;
import com.opengamma.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.financial.model.volatility.BlackImpliedVolatilityFormula;
import com.opengamma.math.number.ComplexNumber;
import com.opengamma.math.rootfinding.VanWijngaardenDekkerBrentSingleRootFinder;

/**
 * 
 */
public class CGMYFourierPricerTest {

  private static final double FORWARD = 1;
  private static final double T = 2.0;
  private static final double DF = 0.93;

  private static final double C = 0.03;
  private static final double G = 0.001;
  private static final double M = 1.001;
  private static final double Y = 1.5;

  private static final CharacteristicExponent CGMY_CE = new CGMYCharacteristicExponent(C, G, M, Y, T);
  private static final BlackImpliedVolatilityFormula BLACK_IMPLIED_VOL = new BlackImpliedVolatilityFormula(new VanWijngaardenDekkerBrentSingleRootFinder());

  @Test
  public void test_CGMY() {
    final FourierPricer pricer = new FourierPricer();
    for (int i = 0; i < 21; i++) {
      final double k = 0.01 + 0.14 * i / 20.0;
      final double price = pricer.price(FORWARD, k * FORWARD, DF, true, CGMY_CE, -0.5, 1e-6);
      final EuropeanVanillaOption option = new EuropeanVanillaOption(k, T, true);
      final BlackFunctionData data = new BlackFunctionData(FORWARD, DF, 0);
      final double impVol = BLACK_IMPLIED_VOL.getImpliedVolatility(data, option, price);
      // System.out.println(k + "\t" + impVol);
    }
  }

  //TODO nothing is being tested in here
  @Test
  public void testIntegrand_CGMY() {
    final EuropeanPriceIntegrand integrand = new EuropeanPriceIntegrand(CGMY_CE, -0.5, FORWARD, 0.25 * FORWARD, true, 0.5);
    for (int i = 0; i < 201; i++) {
      final double x = -15. + i * 30. / 200.0;
      final ComplexNumber res = integrand.getIntegrand(x);
      // System.out.println(x + "\t" + res.getReal() + "\t" + res.getImaginary());
    }
  }

}
