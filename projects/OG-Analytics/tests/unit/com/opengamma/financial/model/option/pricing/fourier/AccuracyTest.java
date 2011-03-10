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
  private static final BlackImpliedVolatilityFormula BLACK_IMPLIED_VOL = new BlackImpliedVolatilityFormula();

  @Test
  public void testVeryShortTime() {
    testEquals(1 / 52.0, 0.001 * EPS, 0.0001 * EPS);
  }

  @Test
  public void testShortTime() {
    testEquals(1 / 12.0, 0.01 * EPS, 0.001 * EPS);
  }

  @Test
  public void testOneYear() {
    testEquals(1.0, 0.1 * EPS, 0.01 * EPS);
  }

  @Test
  public void testLongTime() {
    testEquals(5.0, 0.1 * EPS, 0.01 * EPS);
  }

  @Test
  public void testVeryLongTime() {
    testEquals(30.0, 1.0 * EPS, 0.1 * EPS);
  }

  private void testEquals(final double t, final double integralTol, final double fftTol) {

    final CharacteristicExponent ce = new GaussianCharacteristicExponent(MU, SIGMA, t);

    final double maxLogMoneyness = 6.0 / N_STRIKES * SIGMA * Math.sqrt(t);
    final FourierPricer integralPricer = new FourierPricer(integralTol, 20);

    final double[][] fft_strikeNprice = FFT_PRICER.price(FORWARD, DF, true, ce, N_STRIKES, maxLogMoneyness, ALPHA, fftTol, SIGMA);
    final BlackFunctionData data = new BlackFunctionData(FORWARD, DF, 0);
    for (int i = 0; i < N_STRIKES; i++) {
      final double k = fft_strikeNprice[i][0];
      final double fft_price = fft_strikeNprice[i][1];
      final double integral_price = integralPricer.price(FORWARD, k, DF, true, ce, ALPHA, 0.1 * integralTol);
      final EuropeanVanillaOption option = new EuropeanVanillaOption(k, t, true);
      final double fft_vol = BLACK_IMPLIED_VOL.getImpliedVolatility(data, option, fft_price);
      final double integral_vol = BLACK_IMPLIED_VOL.getImpliedVolatility(data, option, integral_price);

      assertEquals(SIGMA, integral_vol, EPS);
      assertEquals(SIGMA, fft_vol, EPS);

    }
  }

}
