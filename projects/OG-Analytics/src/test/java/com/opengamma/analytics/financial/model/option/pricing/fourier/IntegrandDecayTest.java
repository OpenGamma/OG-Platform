/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.pricing.fourier;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import org.testng.annotations.Test;

import com.opengamma.analytics.math.ComplexMathUtils;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.number.ComplexNumber;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class IntegrandDecayTest {
  private static final double SIGMA = 0.2;
  private static final double T = 1 / 52.0;

  private static final double KAPPA = 1.0; // mean reversion speed
  private static final double THETA = SIGMA * SIGMA; // reversion level
  private static final double VOL0 = THETA; // start level
  private static final double OMEGA = 0.35; // vol-of-vol
  private static final double RHO = -0.3; // correlation

  private static final double ALPHA = -0.5;

  private static final MartingaleCharacteristicExponent CEF = new GaussianMartingaleCharacteristicExponent(SIGMA);
  private static final EuropeanCallFourierTransform PSI = new EuropeanCallFourierTransform(CEF);

  @Test
  public void test() {
    ComplexNumber z = new ComplexNumber(0.0, -(1 + ALPHA));
    final Function1D<ComplexNumber, ComplexNumber> f = PSI.getFunction(T);
    final double mod0 = ComplexMathUtils.mod(f.evaluate(z));
    double previous = 0;
    for (int i = 1; i < 101; i++) {
      final double x = 0.0 + 100.0 * i / 100;
      z = new ComplexNumber(x, -(1 + ALPHA));
      final ComplexNumber u = f.evaluate(z);
      assertEquals(u.getImaginary(), 0, 1e-16);
      final double res = Math.log10(ComplexMathUtils.mod(u) / mod0);
      assertTrue(res < previous);
      previous = res;
    }
  }

  @Test
  public void testHeston() {
    final MartingaleCharacteristicExponent heston = new HestonCharacteristicExponent(KAPPA, THETA, VOL0, OMEGA, RHO);
    final EuropeanCallFourierTransform psi = new EuropeanCallFourierTransform(heston);
    final Function1D<ComplexNumber, ComplexNumber> f = psi.getFunction(T);
    ComplexNumber z = new ComplexNumber(0.0, -(1 + ALPHA));
    final double mod0 = ComplexMathUtils.mod(f.evaluate(z));
    double previous = 0;
    for (int i = 1; i < 101; i++) {
      final double x = 0.0 + 100.0 * i / 100;
      z = new ComplexNumber(x, -(1 + ALPHA));
      final ComplexNumber u = f.evaluate(z);
      final double res = Math.log10(ComplexMathUtils.mod(u) / mod0);
      assertTrue(res < previous);
      previous = res;
    }
  }

}
