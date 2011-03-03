/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.volatility;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.financial.model.option.pricing.analytic.formula.BlackFunctionData;
import com.opengamma.financial.model.option.pricing.analytic.formula.BlackPriceFunction;
import com.opengamma.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.math.rootfinding.BisectionSingleRootFinder;
import com.opengamma.math.rootfinding.NewtonRaphsonSingleRootFinder;
import com.opengamma.math.rootfinding.RealSingleRootFinder;
import com.opengamma.math.rootfinding.RidderSingleRootFinder;
import com.opengamma.math.rootfinding.VanWijngaardenDekkerBrentSingleRootFinder;
import com.opengamma.util.monitor.OperationTimer;

/**
 * 
 */
public class BlackImpliedVolatilityFunctionTest {
  private final Logger _logger = LoggerFactory.getLogger(BlackImpliedVolatilityFunctionTest.class);
  private final int _hotspotWarmupCycles = 200;
  private final int _benchmarkCycles = 10000;
  private static final double FORWARD = 134.5;
  private static final double DF = 0.87;
  private static final double T = 4.5;
  private static final double SIGMA = 0.2;
  private static final BlackFunctionData[] DATA;
  private static final EuropeanVanillaOption[] OPTIONS;
  private static final double[] PRICES;
  private static final double[] STRIKES;
  private static final BlackPriceFunction FORMULA = new BlackPriceFunction();
  private static final int N = 10;

  static {
    PRICES = new double[N];
    STRIKES = new double[N];
    DATA = new BlackFunctionData[N];
    OPTIONS = new EuropeanVanillaOption[N];
    for (int i = 0; i < 10; i++) {
      STRIKES[i] = 50 + 2 * i;
      DATA[i] = new BlackFunctionData(FORWARD, DF, SIGMA);
      OPTIONS[i] = new EuropeanVanillaOption(STRIKES[i], T, true);
      PRICES[i] = FORMULA.getPriceFunction(OPTIONS[i]).evaluate(DATA[i]);
    }
  }

  @Test
  public void test() {
    RealSingleRootFinder rootFinder = new VanWijngaardenDekkerBrentSingleRootFinder();
    testRootFinder(rootFinder);
    rootFinder = new BisectionSingleRootFinder();
    testRootFinder(rootFinder);
    rootFinder = new RidderSingleRootFinder();
    testRootFinder(rootFinder);
    rootFinder = new NewtonRaphsonSingleRootFinder();
    testRootFinder(rootFinder);
  }

  private void testRootFinder(final RealSingleRootFinder rootFinder) {
    final BlackImpliedVolatilityFormula formula = new BlackImpliedVolatilityFormula(rootFinder);
    for (int j = 0; j < _hotspotWarmupCycles; j++) {
      for (int i = 0; i < N; i++) {
        final double vol = formula.getImpliedVolatility(DATA[i], OPTIONS[i], PRICES[i]);
        assertEquals(SIGMA, vol, 1e-6);
      }
    }
    if (_benchmarkCycles > 0) {
      final OperationTimer timer = new OperationTimer(_logger, "processing {} cycles on {}", _benchmarkCycles, rootFinder.getClass().getSimpleName());
      for (int j = 0; j < _benchmarkCycles; j++) {
        for (int i = 0; i < N; i++) {
          final double vol = formula.getImpliedVolatility(DATA[i], OPTIONS[i], PRICES[i]);
          assertEquals(SIGMA, vol, 1e-6);
        }
      }
      timer.finished();
    }
  }
}
