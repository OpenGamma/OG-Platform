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
import com.opengamma.math.function.Function1D;
import com.opengamma.math.number.ComplexNumber;
import com.opengamma.util.monitor.OperationTimer;

/**
 * 
 */
public class GaussianFourierPricerTest {
  private static final Logger s_logger = LoggerFactory.getLogger(GaussianFourierPricerTest.class);
  private static final int WARMUP_CYCLES = 200;
  private static final int BENCHMARK_CYCLES = 10000;
  private static final boolean TEST_TIMING = false;
  private static final double FORWARD = 1;
  private static final double T = 2.0;
  private static final double DF = 0.93;
  private static final double SIGMA = 0.2;
  private static final double MU = -0.5 * SIGMA * SIGMA;
  private static final BlackImpliedVolatilityFormula BLACK_IMPLIED_VOL = new BlackImpliedVolatilityFormula();
  private static final CharacteristicExponent CEF = new GaussianCharacteristicExponent(MU, SIGMA);

  @Test
  public void test() {
    boolean isCall;
    final FourierPricer pricer = new FourierPricer();
    final BlackFunctionData data = new BlackFunctionData(FORWARD, DF, 0);
    for (int i = 0; i < 21; i++) {
      final double k = 0.2 + 3 * i / 20.0;
      isCall = k > 1.0 ? true : false;
      final EuropeanVanillaOption option = new EuropeanVanillaOption(k * FORWARD, T, isCall);
      final double price = pricer.price(data, option, CEF, -1.3, 1e-11);
      final double impVol = BLACK_IMPLIED_VOL.getImpliedVolatility(data, option, price);
      assertEquals(SIGMA, impVol, 1e-5);
    }
  }

  @Test
  public void testExpectation() {
    final double mu = 0.05;
    final double sigma = 0.2;
    final CharacteristicExponent ce = new GaussianCharacteristicExponent(mu, sigma);
    final ComplexNumber res = ce.getFunction(1).evaluate(new ComplexNumber(0, -1));
    assertEquals(mu + 0.5 * sigma * sigma, res.getReal(), 1e-12);
    assertEquals(0.0, res.getImaginary(), 1e-12);
  }

  @Test
  public void testIntegrand() {
    final EuropeanPriceIntegrand integrand = new EuropeanPriceIntegrand(CEF, 0.5, false);
    final EuropeanVanillaOption option = new EuropeanVanillaOption(1.1 * FORWARD, T, true);
    final BlackFunctionData data = new BlackFunctionData(FORWARD, DF, 0.15);
    final Function1D<Double, Double> function = integrand.getFunction(data, option);
    if (TEST_TIMING) {
      double res = 0;
      for (int count = 0; count < WARMUP_CYCLES; count++) {
        for (int i = 0; i < 100; i++) {
          final double x = -0. + i * 1000. / 100.0;
          res += function.evaluate(x);
        }
      }
      res *= 2;
      if (BENCHMARK_CYCLES > 0) {
        final OperationTimer timer = new OperationTimer(s_logger, "processing {} cycles on integral", BENCHMARK_CYCLES);
        for (int count = 0; count < BENCHMARK_CYCLES; count++) {
          for (int i = 0; i < 100; i++) {
            final double x = -0. + i * 1000. / 100.0;
            res += function.evaluate(x);
          }
        }
        timer.finished();
      }
      res *= 3;
    }
  }
}
