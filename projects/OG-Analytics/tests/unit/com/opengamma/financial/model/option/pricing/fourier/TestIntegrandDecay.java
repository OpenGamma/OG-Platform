/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.pricing.fourier;

import org.junit.Test;

import com.opengamma.math.ComplexMathUtils;
import com.opengamma.math.number.ComplexNumber;

/**
 * 
 */
public class TestIntegrandDecay {

  private static final double MU = 0.07;
  private static final double SIGMA = 0.2;
  private static final double T = 1 / 52.0;

  private static final double KAPPA = 1.0; // mean reversion speed
  private static final double THETA = SIGMA * SIGMA; // reversion level
  private static final double VOL0 = THETA; // start level
  private static final double OMEGA = 0.35; // vol-of-vol
  private static final double RHO = -0.3; // correlation

  private static final double ALPHA = -0.5;

  private static final CharacteristicExponent CEF = new GaussianCharacteristicExponent(-0.5 * SIGMA * SIGMA, SIGMA, T);
  private static final EuropeanCallFT PSI = new EuropeanCallFT(CEF);

  @Test
  public void test() {
    ComplexNumber z = new ComplexNumber(0.0, -(1 + ALPHA));
    double mod0 = ComplexMathUtils.mod(PSI.evaluate(z));
    for (int i = 0; i < 101; i++) {
      double x = 0.0 + 100.0 * i / 100;
      z = new ComplexNumber(x, -(1 + ALPHA));
      ComplexNumber u = PSI.evaluate(z);
      double res = Math.log10(ComplexMathUtils.mod(u) / mod0);
      System.out.println(x + "\t" + res);
    }
  }

  @Test
  public void testHeston() {
    final CharacteristicExponent heston = new HestonCharacteristicExponent(KAPPA, THETA, VOL0, OMEGA, RHO, T);
    final EuropeanCallFT psi = new EuropeanCallFT(heston);

    //    ComplexNumber z = new ComplexNumber(0.0, -(1 + ALPHA));
    //    double mod0 = ComplexMathUtils.mod(psi.evaluate(z));
    //    for (int i = 0; i < 101; i++) {
    //      double x = 0.0 + 100.0 * i / 100;
    //      z = new ComplexNumber(x, -(1 + ALPHA));
    //      ComplexNumber u = psi.evaluate(z);
    //      double res = Math.log10(ComplexMathUtils.mod(u) / mod0);
    //      System.out.println(x + "\t" + res);
    //    }

    IntegralLimitCalculator cal = new IntegralLimitCalculator();

    double x = 0;
    for (int i = 0; i < 1000; i++) {
      x = cal.solve(psi, ALPHA, 1e-6);
    }
    System.out.println("x: " + x);
  }

}
