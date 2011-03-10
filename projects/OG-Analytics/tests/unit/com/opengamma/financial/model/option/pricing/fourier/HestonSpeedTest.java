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
import com.opengamma.util.monitor.OperationTimer;

/**
 * 
 */
public class HestonSpeedTest {

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
  private static final double EPS = 1e-6;

  private static final double ALPHA = -0.5;

  protected Logger _logger = LoggerFactory.getLogger(HestonSpeedTest.class);
  protected int _hotspotWarmupCycles = 0;
  protected int _benchmarkCycles = 1;

  final CharacteristicExponent HESTON = new HestonCharacteristicExponent(KAPPA, THETA, VOL0, OMEGA, RH0, T);
  private static final FourierPricer INTEGRAL_PRICER = new FourierPricer(0.01 * EPS, 20);
  private static final FFTPricer FFT_PRICER = new FFTPricer();
  private static final BlackImpliedVolatilityFormula BLACK_IMPLIED_VOL = new BlackImpliedVolatilityFormula();

  //Accuracy
  @Test
  public void testEquals() {
    final int n = 21;
    final double[][] fft_strikeNprice = FFT_PRICER.price(FORWARD, DF, true, HESTON, n, MAX_LOG_MONEYNESS, ALPHA, 0.01 * EPS, SIGMA);
    for (int i = 0; i < n; i++) {
      final double k = fft_strikeNprice[i][0];
      final double fft_price = fft_strikeNprice[i][1];
      final double price = INTEGRAL_PRICER.price(FORWARD, k, DF, true, HESTON, ALPHA, 0.1 * EPS);
      final double priceWithCorrection = INTEGRAL_PRICER.price(FORWARD, k, DF, true, HESTON, ALPHA, 0.1 * EPS, SIGMA);
      assertEquals(price, fft_price, 0.01 * EPS);
      final EuropeanVanillaOption option = new EuropeanVanillaOption(k, T, true);
      final BlackFunctionData data = new BlackFunctionData(FORWARD, DF, 0);
      final double fft_vol = BLACK_IMPLIED_VOL.getImpliedVolatility(data, option, fft_price);
      final double integral_vol = BLACK_IMPLIED_VOL.getImpliedVolatility(data, option, price);
      final double integral_vol_corrected = BLACK_IMPLIED_VOL.getImpliedVolatility(data, option, priceWithCorrection);
      assertEquals(fft_vol, integral_vol, EPS);
      assertEquals(fft_vol, integral_vol_corrected, EPS);
    }
  }

  @Test
  public void testIntegral() {
    for (int i = 0; i < _hotspotWarmupCycles; i++) {
      priceWithIntegral();
    }
    if (_benchmarkCycles > 0) {
      final OperationTimer timer = new OperationTimer(_logger, "processing {} cycles on integral", _benchmarkCycles);
      for (int i = 0; i < _benchmarkCycles; i++) {
        priceWithIntegral();
      }
      timer.finished();
    }
  }

  @Test
  public void testIntegralCorrection() {
    for (int i = 0; i < _hotspotWarmupCycles; i++) {
      priceWithIntegralCorrection();
    }
    if (_benchmarkCycles > 0) {
      final OperationTimer timer = new OperationTimer(_logger, "processing {} cycles on integral (corrected)", _benchmarkCycles);
      for (int i = 0; i < _benchmarkCycles; i++) {
        priceWithIntegralCorrection();
      }
      timer.finished();
    }
  }

  @Test
  public void testFFT() {
    for (int i = 0; i < _hotspotWarmupCycles; i++) {
      priceWithFFT();
    }
    if (_benchmarkCycles > 0) {
      final OperationTimer timer = new OperationTimer(_logger, "processing {} cycles on FFT", _benchmarkCycles);
      for (int i = 0; i < _benchmarkCycles; i++) {
        priceWithFFT();
      }
      timer.finished();
    }
  }

  private void priceWithIntegral() {
    final int n = 7;

    for (int i = 0; i < n; i++) {
      final double k = FORWARD * Math.exp((i - n / 2) * MAX_LOG_MONEYNESS);
      final double price = INTEGRAL_PRICER.price(FORWARD, k, DF, true, HESTON, ALPHA, 0.1 * EPS);
    }
  }

  private void priceWithIntegralCorrection() {
    final int n = 7;

    for (int i = 0; i < n; i++) {
      final double k = FORWARD * Math.exp((i - n / 2) * MAX_LOG_MONEYNESS);
      final double price = INTEGRAL_PRICER.price(FORWARD, k, DF, true, HESTON, ALPHA, 0.1 * EPS, SIGMA);
    }
  }

  private void priceWithFFT() {
    final int n = 7;
    final double[][] fft_strikeNprice = FFT_PRICER.price(FORWARD, DF, true, HESTON, n, MAX_LOG_MONEYNESS, ALPHA, 0.01 * EPS, SIGMA);
  }

}
