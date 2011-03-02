/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.volatility.smile.fitting;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.BitSet;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.financial.model.option.pricing.analytic.formula.SABRFormulaHagan;
import com.opengamma.financial.model.volatility.smile.function.SABRFormulaData;
import com.opengamma.financial.model.volatility.smile.function.SABRHaganVolatilityFunction;
import com.opengamma.math.statistics.distribution.NormalDistribution;
import com.opengamma.math.statistics.distribution.ProbabilityDistribution;
import com.opengamma.math.statistics.leastsquare.LeastSquareResults;
import com.opengamma.util.monitor.OperationTimer;

/**
 * 
 */
public class SABRFitting1Test {
  protected Logger _logger = LoggerFactory.getLogger(SABRFitting1Test.class);
  protected int _hotspotWarmupCycles = 200;
  protected int _benchmarkCycles = 1000;

  private static final double F = 0.03;
  private static final double T = 7.0;
  private static final double ALPHA = 0.05;
  private static final double BETA = 0.5;
  private static double NU = 0.2;
  private static double RHO = -0.3;
  private static final EuropeanVanillaOption[] OPTIONS;
  private static final SABRFormulaData DATA = new SABRFormulaData(F, ALPHA, BETA, NU, RHO);
  private static double[] STRIKES;
  private static double[] VOLS;
  private static double[] NOISY_VOLS;
  private static double[] ERRORS;
  private static SABRHaganVolatilityFunction SABR = new SABRHaganVolatilityFunction();
  private static ProbabilityDistribution<Double> RANDOM = new NormalDistribution(0, 1, new cern.jet.random.engine.MersenneTwister(12));

  static {
    STRIKES = new double[] {0.005, 0.01, 0.02, 0.03, 0.04, 0.05, 0.07};
    final int n = STRIKES.length;
    VOLS = new double[n];
    NOISY_VOLS = new double[n];
    ERRORS = new double[n];
    OPTIONS = new EuropeanVanillaOption[n];
    for (int i = 0; i < n; i++) {
      final EuropeanVanillaOption option = new EuropeanVanillaOption(STRIKES[i], T, true);
      OPTIONS[i] = option;
      VOLS[i] = SABR.getVolatilityFunction(option).evaluate(DATA);
      ERRORS[i] = 0.01;
      NOISY_VOLS[i] = VOLS[i] + ERRORS[i] * RANDOM.nextRandom();
    }
  }

  @Test
  public void testExactFit() {
    final BitSet fixed = new BitSet();

    final double[] start = new double[] {0.03, 0.4, 0.1, 0.2};

    final SABRFitter1 fitter = new SABRFitter1(SABR);
    final LeastSquareResults results = fitter.solve(OPTIONS, DATA, VOLS, ERRORS, start, fixed, 0, false);
    //    final LeastSquareResults results = fitter.solve(F, T, STRIKES, VOLS, ERRORS, start, fixed, 0, false);
    final double[] res = results.getParameters().getData();
    assertEquals(ALPHA, res[0], 1e-7);
    assertEquals(BETA, res[1], 1e-7);
    assertEquals(NU, res[2], 1e-7);
    assertEquals(RHO, res[3], 1e-7);
    assertEquals(0.0, results.getChiSq(), 1e-7);
  }

  @Test
  public void testTiming() {
    final SABRFormulaHagan hagan = new SABRFormulaHagan();

    for (int i = 0; i < _hotspotWarmupCycles; i++) {
      hagan.impliedVolatility(F, ALPHA, BETA, NU, RHO, 0.9 * F, T);

    }

    if (_benchmarkCycles > 0) {
      final OperationTimer timer = new OperationTimer(_logger, "processing {} cycles SABR", _benchmarkCycles);
      for (int i = 0; i < _benchmarkCycles; i++) {
        hagan.impliedVolatility(F, ALPHA, BETA, NU, RHO, 0.9 * F, T);
      }
      timer.finished();
    }

  }

  @Test
  public void testFitTime() {
    final BitSet fixed = new BitSet();

    final double[] start = new double[] {0.03, 0.4, 0.1, 0.2};

    final SABRFitter1 fitter = new SABRFitter1(SABR);

    for (int i = 0; i < _hotspotWarmupCycles; i++) {
      fitter.solve(OPTIONS, DATA, VOLS, ERRORS, start, fixed, 0, false);
    }

    if (_benchmarkCycles > 0) {
      final OperationTimer timer = new OperationTimer(_logger, "processing {} cycles fitting", _benchmarkCycles);
      for (int i = 0; i < _benchmarkCycles; i++) {
        fitter.solve(OPTIONS, DATA, VOLS, ERRORS, start, fixed, 0, false);
      }
      timer.finished();
    }

  }

  @Test
  public void testNoisyFit() {
    final BitSet fixed = new BitSet();
    fixed.set(1, true);
    final double[] start = new double[] {0.03, 0.5, 0.1, 0.2};
    final SABRFitter1 fitter = new SABRFitter1(SABR);
    final LeastSquareResults results = fitter.solve(OPTIONS, DATA, NOISY_VOLS, ERRORS, start, fixed, 0, false);
    assertTrue(results.getChiSq() < 10.0);
    final double[] res = results.getParameters().getData();
    assertEquals(ALPHA, res[0], 1e-3);
    assertEquals(BETA, res[1], 1e-7);
    assertEquals(RHO, res[3], 1e-1);
    assertEquals(NU, res[2], 1e-2);
  }
}
