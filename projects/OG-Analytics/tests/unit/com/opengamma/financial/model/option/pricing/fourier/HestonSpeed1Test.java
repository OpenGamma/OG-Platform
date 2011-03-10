/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.pricing.fourier;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.financial.model.option.pricing.analytic.formula.BlackFunctionData;
import com.opengamma.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.financial.model.volatility.BlackImpliedVolatilityFormula;
import com.opengamma.math.integration.RungeKuttaIntegrator1D;
import com.opengamma.util.monitor.OperationTimer;

/**
 * 
 */
public class HestonSpeed1Test {
  private static Logger s_logger = LoggerFactory.getLogger(HestonSpeed1Test.class);
  private static int WARMUP_CYCLES = 200;
  private static int BENCHMARK_CYCLES = 1000;

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

  private static final CharacteristicExponent1 HESTON = new HestonCharacteristicExponent1(KAPPA, THETA, VOL0, OMEGA, RH0);
  private static final RungeKuttaIntegrator1D INTEGRATOR = new RungeKuttaIntegrator1D(1e-8, 20);
  private static final FourierPricer1 INTEGRAL_PRICER = new FourierPricer1(INTEGRATOR);
  private static final FFTPricer1 FFT_PRICER = new FFTPricer1();
  private static final BlackImpliedVolatilityFormula BLACK_IMPLIED_VOL = new BlackImpliedVolatilityFormula();

  //Accuracy
  @Test
  public void testEquals() {
    final int n = 21;
    EuropeanVanillaOption option = new EuropeanVanillaOption(FORWARD, T, true);
    final double[][] fft_strikeNprice = FFT_PRICER.price(DATA, option, HESTON, n, MAX_LOG_MONEYNESS, ALPHA, 0.01 * EPS);
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
    final EuropeanVanillaOption option = new EuropeanVanillaOption(FORWARD, T, true);
    FFT_PRICER.price(DATA, option, HESTON, n, MAX_LOG_MONEYNESS, ALPHA, 0.01 * EPS);
  }

}
