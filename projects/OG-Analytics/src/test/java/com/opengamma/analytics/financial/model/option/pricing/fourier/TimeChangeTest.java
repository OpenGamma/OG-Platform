/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.pricing.fourier;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import org.testng.annotations.Test;

import com.opengamma.analytics.math.number.ComplexNumber;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class TimeChangeTest {
  private static final double FORWARD = 1.0;
  private static final double T = 3.0;
  private static final double DF = 0.93;
  private static final double SIGMA = 0.2;
  private static final double KAPPA = 0.7; // mean reversion speed
  private static final double THETA = SIGMA * SIGMA; // reversion level
  private static final double VOL0 = THETA; // start level
  private static final double OMEGA = 0.2; // vol-of-vol
  private static final double RH0 = -0.0; // correlation
  private static double ALPHA = -0.5;
  private static final double MAX_LOG_MONEYNESS = 0.1;
  private static final double EPS = 1e-6;
  private static final MartingaleCharacteristicExponent HESTON = new HestonCharacteristicExponent(KAPPA, THETA, VOL0, OMEGA, RH0);
  private static final StocasticClockCharcteristicExponent CIR = new IntegratedCIRTimeChangeCharacteristicExponent(KAPPA, THETA / VOL0, OMEGA / Math.sqrt(VOL0));
  private static final MartingaleCharacteristicExponent NORMAL = new GaussianMartingaleCharacteristicExponent(Math.sqrt(VOL0));
  private static final CharacteristicExponent NORMAL_CIR = new TimeChangedCharacteristicExponent(NORMAL, CIR);
  private static final FFTPricer FFT_PRICER = new FFTPricer();

  @Test
  public void testAgainstHeston() {
    final int n = 21;
    final double[][] heston_strikeNprice = FFT_PRICER.price(FORWARD, DF, T, true, HESTON, n, MAX_LOG_MONEYNESS, SIGMA, ALPHA, 0.01 * EPS);
    final double[][] normalCIR_strikeNprice = FFT_PRICER.price(FORWARD, DF, T, true, HESTON, n, MAX_LOG_MONEYNESS, SIGMA, ALPHA, 0.01 * EPS);

    for (int i = 0; i < n; i++) {
      assertEquals(heston_strikeNprice[i][0], normalCIR_strikeNprice[i][0], 1e-12);//should have the same strikes
      assertEquals(heston_strikeNprice[i][1], normalCIR_strikeNprice[i][1], 1e-12);//should have the same price
    }

  }

  @Test
  public void testCE() {
    for (int i = 0; i < 101; i++) {
      final double x = 10.0 * i / 100.0;
      final ComplexNumber z = new ComplexNumber(x, -(1 + ALPHA));
      final ComplexNumber res1 = NORMAL_CIR.getFunction(T).evaluate(z);
      final ComplexNumber res2 = NORMAL.getFunction(1).evaluate(z);
      final ComplexNumber res3 = HESTON.getFunction(T).evaluate(z);
      assertTrue(Math.abs(res1.getImaginary()) < EPS);
      assertTrue(Math.abs(res2.getImaginary()) < EPS);
      assertTrue(Math.abs(res3.getImaginary()) < EPS);
      assertEquals(res1.getReal(), res3.getReal(), EPS);
    }

  }

}
