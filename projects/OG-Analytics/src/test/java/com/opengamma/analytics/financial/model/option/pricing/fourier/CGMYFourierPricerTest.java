/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.pricing.fourier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.BlackFunctionData;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.analytics.financial.model.volatility.BlackImpliedVolatilityFormula;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.util.monitor.OperationTimer;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class CGMYFourierPricerTest {
  private static final Logger s_logger = LoggerFactory.getLogger(CGMYFourierPricerTest.class);
  private static final int WARMUP_CYCLES = 0;
  private static final int BENCHMARK_CYCLES = 1;
  private static final boolean TEST_TIMING = false;
  private static final double FORWARD = 1;
  private static final double T = 2.0;
  private static final double DF = 0.93;
  private static final double C = 0.03;
  private static final double G = 0.001;
  private static final double M = 1.001;
  private static final double Y = 1.5;
  private static final MartingaleCharacteristicExponent CGMY_CE = new CGMYMartingaleCharacteristicExponent(C, G, M, Y);
  private static final BlackImpliedVolatilityFormula BLACK_IMPLIED_VOL = new BlackImpliedVolatilityFormula();

  @Test
  public void testCGMY() {
    final FourierPricer pricer = new FourierPricer();
    final BlackFunctionData data = new BlackFunctionData(FORWARD, DF, 0);
    for (int i = 0; i < 21; i++) {
      final double k = 0.01 + 0.14 * i / 20.0;
      final EuropeanVanillaOption option = new EuropeanVanillaOption(k, T, true);
      final double price = pricer.price(data, option, CGMY_CE, -0.5, 1e-6);
      @SuppressWarnings("unused")
      final double impVol = BLACK_IMPLIED_VOL.getImpliedVolatility(data, option, price);
      //System.out.println(k + "\t" + impVol);
    }
  }

  //TODO nothing is being tested in here
  @Test
  public void testIntegrandCGMY() {
    final EuropeanPriceIntegrand integrand = new EuropeanPriceIntegrand(CGMY_CE, -0.5, true);
    final EuropeanVanillaOption option = new EuropeanVanillaOption(0.25 * FORWARD, T, true);
    final BlackFunctionData data = new BlackFunctionData(FORWARD, DF, 0.5);
    final Function1D<Double, Double> function = integrand.getFunction(data, option);
    if (TEST_TIMING) {
      double res = 0;
      for (int count = 0; count < WARMUP_CYCLES; count++) {
        for (int i = 0; i < 100; i++) {
          final double x = -15. + i * 30. / 200.0;
          res += function.evaluate(x);
        }
      }
      res *= 2;
      if (BENCHMARK_CYCLES > 0) {
        final OperationTimer timer = new OperationTimer(s_logger, "processing {} cycles on integral", BENCHMARK_CYCLES);
        for (int count = 0; count < BENCHMARK_CYCLES; count++) {
          for (int i = 0; i < 100; i++) {
            final double x = -15. + i * 30. / 200.0;
            res += function.evaluate(x);
          }
        }
        timer.finished();
      }
      res *= 2;
    }
  }

}
