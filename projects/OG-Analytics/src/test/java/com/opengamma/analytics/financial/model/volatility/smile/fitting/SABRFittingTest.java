/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility.smile.fitting;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import java.util.BitSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import cern.jet.random.engine.MersenneTwister;

import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.BlackFunctionData;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.analytics.financial.model.volatility.smile.function.SABRFormulaData;
import com.opengamma.analytics.financial.model.volatility.smile.function.SABRHaganVolatilityFunction;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.statistics.distribution.NormalDistribution;
import com.opengamma.analytics.math.statistics.distribution.ProbabilityDistribution;
import com.opengamma.analytics.math.statistics.leastsquare.LeastSquareResultsWithTransform;
import com.opengamma.util.monitor.OperationTimer;
import com.opengamma.util.test.TestGroup;

/**
 * @deprecated This class tests deprecated functionality
 */
@Deprecated
@Test(groups = TestGroup.UNIT)
public class SABRFittingTest {
  protected Logger _logger = LoggerFactory.getLogger(SABRFittingTest.class);
  protected int _hotspotWarmupCycles = 0;
  protected int _benchmarkCycles = 1;
  private static final double F = 0.03;
  private static final double T = 7.0;
  private static final double ALPHA = 0.05;
  private static final double BETA = 0.5;
  private static double NU = 0.2;
  private static double RHO = -0.3;
  private static final EuropeanVanillaOption[] OPTIONS;
  private static final BlackFunctionData[] DATA;
  private static final BlackFunctionData[] NOISY_DATA;
  private static double[] STRIKES;
  private static double[] ERRORS;
  private static final SABRFormulaData SABR_DATA = new SABRFormulaData(ALPHA, BETA, RHO, NU);
  private static SABRHaganVolatilityFunction SABR = new SABRHaganVolatilityFunction();
  private static ProbabilityDistribution<Double> RANDOM = new NormalDistribution(0, 1, new MersenneTwister(12));
  private static final SABRNonLinearLeastSquareFitter NLSS = new SABRNonLinearLeastSquareFitter(SABR);
  //  private static final SABRConjugateGradientLeastSquareFitter CG = new SABRConjugateGradientLeastSquareFitter(SABR);

  static {
    STRIKES = new double[] {0.005, 0.01, 0.02, 0.03, 0.04, 0.05, 0.07 };
    final int n = STRIKES.length;
    DATA = new BlackFunctionData[n];
    NOISY_DATA = new BlackFunctionData[n];
    ERRORS = new double[n];
    OPTIONS = new EuropeanVanillaOption[n];

    for (int i = 0; i < n; i++) {
      final EuropeanVanillaOption option = new EuropeanVanillaOption(STRIKES[i], T, true);
      OPTIONS[i] = option;
      final double vol = SABR.getVolatilityFunction(option, F).evaluate(SABR_DATA);
      DATA[i] = new BlackFunctionData(F, 1, vol);
      ERRORS[i] = 0.01;
      NOISY_DATA[i] = new BlackFunctionData(F, 1, vol + ERRORS[i] * RANDOM.nextRandom());
    }
  }

  @Test
  public void testExactFitNLSS() {
    final BitSet fixed = new BitSet();
    final double[] start = new double[] {0.03, 0.4, 0.1, 0.2 };
    final LeastSquareResultsWithTransform results = NLSS.getFitResult(OPTIONS, DATA, ERRORS, start, fixed);
    final double[] res = results.getModelParameters().getData();
    final double eps = 1e-4;
    assertEquals(ALPHA, res[0], eps);
    assertEquals(BETA, res[1], eps);
    assertEquals(RHO, res[2], eps);
    assertEquals(NU, res[3], eps);
    assertEquals(0.0, results.getChiSq(), eps);
  }

  //FIXME: test doesn't pass at all
  //  @SuppressWarnings("unused")
  //  @Test
  //  public void testExactFitCG() {
  //    final BitSet fixed = new BitSet();
  //    final double[] start = new double[] {0.03, 0.4, 0.1, 0.2};
  //    final LeastSquareResults results = CG.getFitResult(OPTIONS, DATA, ERRORS, start, fixed);
  //    final double[] res = results.getParameters().getData();
  //    final double eps = 1e-4;
  //    //    assertEquals(ALPHA, res[0], eps);
  //    //    assertEquals(BETA, res[1], eps);
  //    //    assertEquals(NU, res[2], eps);
  //    //    assertEquals(RHO, res[3], eps);
  //    //    assertEquals(0.0, results.getChiSq(), eps);
  //  }

  @Test
  public void testSmileGenerationTime() {
    final SABRHaganVolatilityFunction sabr = new SABRHaganVolatilityFunction();
    final SABRFormulaData data = new SABRFormulaData(ALPHA, BETA, RHO, NU);
    final EuropeanVanillaOption option = new EuropeanVanillaOption(0.9 * F, T, true);
    final Function1D<SABRFormulaData, Double> f = sabr.getVolatilityFunction(option, F);
    double x = 0;
    for (int i = 0; i < _hotspotWarmupCycles; i++) {
      x += f.evaluate(data);
    }
    if (_benchmarkCycles > 0) {
      final OperationTimer timer = new OperationTimer(_logger, "processing {} cycles generating SABR smile", _benchmarkCycles);
      for (int i = 0; i < _benchmarkCycles; i++) {
        x += f.evaluate(data);
      }
      timer.finished();
    }
  }

  @Test
  public void testFitTime() {
    assertFitTime(NLSS, "non-linear least squares");
    //    testFitTime(CG, "conjugate gradient");
  }

  private void assertFitTime(final LeastSquareSmileFitter fitter, final String name) {
    final BitSet fixed = new BitSet();
    final double[] start = new double[] {0.03, 0.4, 0.1, 0.2 };
    for (int i = 0; i < _hotspotWarmupCycles; i++) {
      fitter.getFitResult(OPTIONS, DATA, ERRORS, start, fixed);
    }
    if (_benchmarkCycles > 0) {
      final OperationTimer timer = new OperationTimer(_logger, "processing {} cycles fitting with {}", _benchmarkCycles, name);
      for (int i = 0; i < _benchmarkCycles; i++) {
        fitter.getFitResult(OPTIONS, DATA, ERRORS, start, fixed);
      }
      timer.finished();
    }
  }

  @Test
  public void testNoisyFitNLSS() {
    final BitSet fixed = new BitSet();
    fixed.set(1, true);
    final double[] start = new double[] {0.03, 0.5, 0.1, 0.2 };
    final LeastSquareResultsWithTransform results = NLSS.getFitResult(OPTIONS, NOISY_DATA, ERRORS, start, fixed);
    assertTrue(results.getChiSq() < 10.0);
    final double[] res = results.getModelParameters().getData();
    assertEquals(ALPHA, res[0], 1e-3);
    assertEquals(BETA, res[1], 1e-7);
    assertEquals(RHO, res[2], 1e-1);
    assertEquals(NU, res[3], 1e-2);
  }

  //FIXME: tests don't pass at all
  //  @SuppressWarnings("unused")
  //  @Test
  //  public void testNoisyFitCG() {
  //    final BitSet fixed = new BitSet();
  //    fixed.set(1, true);
  //    final double[] start = new double[] {0.03, 0.5, 0.1, 0.2};
  //    final LeastSquareResults results = CG.getFitResult(OPTIONS, NOISY_DATA, ERRORS, start, fixed);
  //    //    assertTrue(results.getChiSq() < 10.0);
  //    //    final double[] res = results.getParameters().getData();
  //    //    assertEquals(ALPHA, res[0], 1e-3);
  //    //    assertEquals(BETA, res[1], 1e-7);
  //    //    assertEquals(RHO, res[3], 1e-1);
  //    //    assertEquals(NU, res[2], 1e-2);
  //  }
}
