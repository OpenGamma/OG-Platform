/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.pricing.fourier;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.opengamma.financial.model.option.pricing.analytic.formula.BlackImpliedVolFormula;

/**
 * 
 */
public class AccuracyTest {

  private static final double FORWARD = 0.04;

  private static final double DF = 0.96;
  private static final double MU = 0.07;
  private static final double SIGMA = 0.2;
  private static final int N_STRIKES = 21;

  private static final double ALPHA = -0.5;
  private static final double EPS = 1e-6;

  private static final FFTPricer FFT_PRICER = new FFTPricer();

  @Test
  public void TestVeryShortTime() {
    testEquals(1 / 52.0, 0.001 * EPS, 0.0001 * EPS);
  }

  @Test
  public void TestShortTime() {
    testEquals(1 / 12.0, 0.01 * EPS, 0.001 * EPS);
  }

  @Test
  public void TestOneYear() {
    testEquals(1.0, 0.1 * EPS, 0.01 * EPS);
  }

  @Test
  public void TestLongTime() {
    testEquals(5.0, 0.1 * EPS, 0.01 * EPS);
  }

  @Test
  public void TestVeryLongTime() {
    testEquals(30.0, 1.0 * EPS, 0.1 * EPS);
  }

  private void testEquals(double t, double integralTol, double fftTol) {

    final CharacteristicExponent ce = new GaussianCharacteristicExponent(MU, SIGMA, t);

    final double maxLogMoneyness = 6.0 / N_STRIKES * SIGMA * Math.sqrt(t);
    final FourierPricer integralPricer = new FourierPricer(integralTol, 20);

    double[][] fft_strikeNprice = FFT_PRICER.price(FORWARD, DF, true, ce, N_STRIKES, maxLogMoneyness, ALPHA, fftTol, SIGMA);
    for (int i = 0; i < N_STRIKES; i++) {
      double k = fft_strikeNprice[i][0];
      double fft_price = fft_strikeNprice[i][1];
      double integral_price = integralPricer.price(FORWARD, k, DF, true, ce, ALPHA, 0.1 * integralTol);
      double fft_vol = BlackImpliedVolFormula.impliedVol(fft_price, FORWARD, k, DF, t, true);
      double integral_vol = BlackImpliedVolFormula.impliedVol(integral_price, FORWARD, k, DF, t, true);

      assertEquals(SIGMA, integral_vol, EPS);
      assertEquals(SIGMA, fft_vol, EPS);

    }
  }

}
