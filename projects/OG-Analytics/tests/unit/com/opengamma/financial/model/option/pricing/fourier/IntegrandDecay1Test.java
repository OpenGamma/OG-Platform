/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.pricing.fourier;

import org.junit.Test;

import com.opengamma.math.ComplexMathUtils;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.number.ComplexNumber;

/**
 * 
 */
public class IntegrandDecay1Test {

  private static final double MU = 0.07;
  private static final double SIGMA = 0.2;
  private static final double T = 1 / 52.0;

  private static final double KAPPA = 1.0; // mean reversion speed
  private static final double THETA = SIGMA * SIGMA; // reversion level
  private static final double VOL0 = THETA; // start level
  private static final double OMEGA = 0.35; // vol-of-vol
  private static final double RHO = -0.3; // correlation

  private static final double ALPHA = -0.5;

  private static final CharacteristicExponent1 CEF = new GaussianCharacteristicExponent1(-0.5 * SIGMA * SIGMA, SIGMA);
  private static final EuropeanCallFT1 PSI = new EuropeanCallFT1(CEF);

  @Test
  public void test() {
    ComplexNumber z = new ComplexNumber(0.0, -(1 + ALPHA));
    final Function1D<ComplexNumber, ComplexNumber> f = PSI.getFunction(T);
    final double mod0 = ComplexMathUtils.mod(f.evaluate(z));
    for (int i = 0; i < 101; i++) {
      final double x = 0.0 + 100.0 * i / 100;
      z = new ComplexNumber(x, -(1 + ALPHA));
      final ComplexNumber u = f.evaluate(z);
      final double res = Math.log10(ComplexMathUtils.mod(u) / mod0);
      //System.out.println(x + "\t" + res);
    }
  }

  @Test
  public void testHeston() {
    final CharacteristicExponent1 heston = new HestonCharacteristicExponent1(KAPPA, THETA, VOL0, OMEGA, RHO);
    final EuropeanCallFT1 psi = new EuropeanCallFT1(heston);

    //    ComplexNumber z = new ComplexNumber(0.0, -(1 + ALPHA));
    //    double mod0 = ComplexMathUtils.mod(psi.evaluate(z));
    //    for (int i = 0; i < 101; i++) {
    //      double x = 0.0 + 100.0 * i / 100;
    //      z = new ComplexNumber(x, -(1 + ALPHA));
    //      ComplexNumber u = psi.evaluate(z);
    //      double res = Math.log10(ComplexMathUtils.mod(u) / mod0);
    //      System.out.println(x + "\t" + res);
    //    }

    final IntegralLimitCalculator1 cal = new IntegralLimitCalculator1();

    double x = 0;
    for (int i = 0; i < 1000; i++) {
      x = cal.solve(psi.getFunction(T), ALPHA, 1e-6);
    }
    // System.out.println("x: " + x);
  }

}
