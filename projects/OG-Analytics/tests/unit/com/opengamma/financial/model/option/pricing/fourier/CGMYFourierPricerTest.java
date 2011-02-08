/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.pricing.fourier;

import org.junit.Test;

import com.opengamma.financial.model.option.pricing.analytic.formula.BlackImpliedVolFormula;
import com.opengamma.math.number.ComplexNumber;

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

  private static final CharacteristicExponent CGMY_CE = new CGMYCharacteristicExponent(C, G, M, Y);

  @Test
  public void test_CGMY() {
    FourierPricer pricer = new FourierPricer(-0.5);
    for (int i = 0; i < 21; i++) {
      double k = 0.01 + 0.14 * i / 20.0;
      double price = pricer.price(FORWARD, k * FORWARD, T, DF, true, CGMY_CE);
      double impVol = BlackImpliedVolFormula.impliedVol(price, FORWARD, k * FORWARD, DF, T, true);
      // System.out.println(k + "\t" + impVol);
    }
  }

  @Test
  public void testIntergrad_CGMY() {
    EuropeanPriceIntegrand intergrand = new EuropeanPriceIntegrand(CGMY_CE, -0.5, FORWARD, 0.25 * FORWARD, T, true, 0.5);
    for (int i = 0; i < 201; i++) {
      double x = -15. + i * 30. / 200.0;
      ComplexNumber res = intergrand.getIntegrand(x);
      // System.out.println(x + "\t" + res.getReal() + "\t" + res.getImaginary());
    }
  }

}
