/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.pricing.fourier;

import static org.testng.AssertJUnit.assertEquals;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.BlackFunctionData;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.analytics.financial.model.volatility.BlackImpliedVolatilityFormula;
import com.opengamma.analytics.math.integration.RungeKuttaIntegrator1D;
import com.opengamma.util.monitor.OperationTimer;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class HestonSpeedTest {
  private static Logger s_logger = LoggerFactory.getLogger(HestonSpeedTest.class);
  private static int WARMUP_CYCLES = 0;
  private static int BENCHMARK_CYCLES = 1;

  private static final double FORWARD = 0.04;
  private static final double T = 2.0;
  private static final double DF = 0.93;
  private static final double SIGMA = 0.36;

  private static final double KAPPA = 1.0; // mean reversion speed
  private static final double THETA = SIGMA * SIGMA; // reversion level
  private static final double VOL0 = THETA; // start level
  private static final double OMEGA = 0.25; // vol-of-vol
  private static final double RH0 = -0.3; // correlation
  private static final double MAX_LOG_MONEYNESS = 0.1;
  private static final BlackFunctionData DATA = new BlackFunctionData(FORWARD, DF, SIGMA);

  private static final double EPS = 1e-6;

  private static final double ALPHA = -0.5;

  private static final MartingaleCharacteristicExponent HESTON = new HestonCharacteristicExponent(KAPPA, THETA, VOL0, OMEGA, RH0);
  private static final RungeKuttaIntegrator1D INTEGRATOR = new RungeKuttaIntegrator1D(1e-8, 20);
  private static final FourierPricer INTEGRAL_PRICER = new FourierPricer(INTEGRATOR);
  private static final FFTPricer FFT_PRICER = new FFTPricer();
  private static final BlackImpliedVolatilityFormula BLACK_IMPLIED_VOL = new BlackImpliedVolatilityFormula();

  //Accuracy
  @Test
  public void testEquals() {
    final int n = 21;
    EuropeanVanillaOption option = new EuropeanVanillaOption(FORWARD, T, true);
    final double[][] fft_strikeNprice = FFT_PRICER.price(FORWARD, DF, T, true, HESTON, n, MAX_LOG_MONEYNESS, SIGMA, ALPHA, 0.01 * EPS);
    for (int i = 0; i < n; i++) {
      final double k = fft_strikeNprice[i][0];
      final double fft_price = fft_strikeNprice[i][1];
      option = new EuropeanVanillaOption(k, T, true);
      final double price = INTEGRAL_PRICER.price(DATA, option, HESTON, ALPHA, 0.1 * EPS);
      final double priceWithCorrection = INTEGRAL_PRICER.price(DATA, option, HESTON, ALPHA, 0.1 * EPS, true);
      assertEquals(price, fft_price, 0.01 * EPS);
      final double fft_vol = BLACK_IMPLIED_VOL.getImpliedVolatility(DATA, option, fft_price);
      final double integral_vol = BLACK_IMPLIED_VOL.getImpliedVolatility(DATA, option, price);
      final double integral_vol_corrected = BLACK_IMPLIED_VOL.getImpliedVolatility(DATA, option, priceWithCorrection);
      assertEquals(fft_vol, integral_vol, EPS);
      assertEquals(fft_vol, integral_vol_corrected, EPS);
    }
  }

  @Test
  public void testIntegral() {
    for (int i = 0; i < WARMUP_CYCLES; i++) {
      priceWithIntegral();
    }
    if (BENCHMARK_CYCLES > 0) {
      final OperationTimer timer = new OperationTimer(s_logger, "processing {} cycles on integral", BENCHMARK_CYCLES);
      for (int i = 0; i < BENCHMARK_CYCLES; i++) {
        priceWithIntegral();
      }
      timer.finished();
    }
  }

  @Test
  public void testIntegralCorrection() {
    for (int i = 0; i < WARMUP_CYCLES; i++) {
      priceWithIntegralCorrection();
    }
    if (BENCHMARK_CYCLES > 0) {
      final OperationTimer timer = new OperationTimer(s_logger, "processing {} cycles on integral (corrected)", BENCHMARK_CYCLES);
      for (int i = 0; i < BENCHMARK_CYCLES; i++) {
        priceWithIntegralCorrection();
      }
      timer.finished();
    }
  }

  @Test
  public void testFFT() {
    for (int i = 0; i < WARMUP_CYCLES; i++) {
      priceWithFFT();
    }
    if (BENCHMARK_CYCLES > 0) {
      final OperationTimer timer = new OperationTimer(s_logger, "processing {} cycles on FFT", BENCHMARK_CYCLES);
      for (int i = 0; i < BENCHMARK_CYCLES; i++) {
        priceWithFFT();
      }
      timer.finished();
    }
  }

  private void priceWithIntegral() {
    final int n = 7;
    final BlackFunctionData data = new BlackFunctionData(FORWARD, DF, SIGMA);
    for (int i = 0; i < n; i++) {
      final double k = FORWARD * Math.exp((i - n / 2) * MAX_LOG_MONEYNESS);
      final EuropeanVanillaOption option = new EuropeanVanillaOption(k, T, true);
      INTEGRAL_PRICER.price(data, option, HESTON, ALPHA, 0.1 * EPS);
    }
  }

  private void priceWithIntegralCorrection() {
    final int n = 7;
    for (int i = 0; i < n; i++) {
      final double k = FORWARD * Math.exp((i - n / 2) * MAX_LOG_MONEYNESS);
      final EuropeanVanillaOption option = new EuropeanVanillaOption(k, T, true);
      INTEGRAL_PRICER.price(DATA, option, HESTON, ALPHA, 0.1 * EPS, true);
    }
  }

  private void priceWithFFT() {
    final int n = 7;
    FFT_PRICER.price(FORWARD, DF, T, true, HESTON, n, MAX_LOG_MONEYNESS, SIGMA, ALPHA, 0.01 * EPS);
  }

}
