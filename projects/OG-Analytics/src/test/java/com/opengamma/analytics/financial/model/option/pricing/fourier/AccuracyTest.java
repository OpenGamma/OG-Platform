/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.pricing.fourier;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.BlackFunctionData;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.analytics.financial.model.volatility.BlackImpliedVolatilityFormula;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class AccuracyTest {
  private static final double FORWARD = 0.04;
  private static final double DF = 0.96;
  private static final double SIGMA = 0.2;
  private static final int N_STRIKES = 21;
  private static final double ALPHA = -0.5;
  private static final double EPS = 1e-6;

  private static final BlackImpliedVolatilityFormula BLACK_IMPLIED_VOL = new BlackImpliedVolatilityFormula();

  @Test
  public void testVeryShortTime() {
    assertAccuracy(1 / 52.0, 0.001 * EPS, 0.0001 * EPS);
  }

  @Test
  public void testShortTime() {
    assertAccuracy(1 / 12.0, 0.01 * EPS, 0.001 * EPS);
  }

  @Test
  public void testOneYear() {
    assertAccuracy(1.0, 0.1 * EPS, 0.01 * EPS);
  }

  @Test
  public void testLongTime() {
    assertAccuracy(5.0, 0.1 * EPS, 0.01 * EPS);
  }

  @Test
  public void testVeryLongTime() {
    assertAccuracy(30.0, 1.0 * EPS, 0.1 * EPS);
  }

  private void assertAccuracy(final double t, final double integralTol, final double fftTol) {
    final MartingaleCharacteristicExponent ce = new GaussianMartingaleCharacteristicExponent(SIGMA);

    final double maxLogMoneyness = 6.0 / N_STRIKES * SIGMA * Math.sqrt(t);
    final FourierPricer integralPricer = new FourierPricer();
    final FFTPricer fftPricer = new FFTPricer();

    final double[][] fftStrikeNprice = fftPricer.price(FORWARD, DF, t, true, ce, N_STRIKES, maxLogMoneyness, SIGMA, ALPHA, fftTol);
    final BlackFunctionData data = new BlackFunctionData(FORWARD, DF, 0);
    for (int i = 0; i < N_STRIKES; i++) {
      final double k = fftStrikeNprice[i][0];
      final double fftPrice = fftStrikeNprice[i][1];
      final EuropeanVanillaOption o = new EuropeanVanillaOption(k, t, true);
      final double integralPrice = integralPricer.price(data, o, ce, ALPHA, 0.1 * integralTol, false);
      final double fftVol = BLACK_IMPLIED_VOL.getImpliedVolatility(data, o, fftPrice);
      final double integral_vol = BLACK_IMPLIED_VOL.getImpliedVolatility(data, o, integralPrice);
      assertEquals(SIGMA, integral_vol, EPS);
      assertEquals(SIGMA, fftVol, EPS);
    }
  }

}
