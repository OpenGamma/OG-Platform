/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility.smile.fitting;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import java.util.Arrays;
import java.util.BitSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import cern.jet.random.engine.MersenneTwister;

import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.BlackFunctionData;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.analytics.financial.model.volatility.smile.function.SABRFormulaData;
import com.opengamma.analytics.financial.model.volatility.smile.function.SABRHaganVolatilityFunction;
import com.opengamma.analytics.financial.model.volatility.smile.function.VolatilityFunctionProvider;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.statistics.distribution.NormalDistribution;
import com.opengamma.analytics.math.statistics.distribution.ProbabilityDistribution;
import com.opengamma.analytics.math.statistics.leastsquare.LeastSquareResultsWithTransform;
import com.opengamma.util.monitor.OperationTimer;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class SABRModelFitterTest {

  protected Logger _logger = LoggerFactory.getLogger(SABRModelFitterTest.class);
  protected int _hotspotWarmupCycles = 0;
  protected int _benchmarkCycles = 1;
  private static final double F = 0.03;
  private static final double T = 7.0;
  private static final double ALPHA = 0.05;
  private static final double BETA = 0.5;
  private static double RHO = -0.3;
  private static double NU = 0.2;

  private static final EuropeanOptionMarketData[] MARKETDATA;
  private static final EuropeanOptionMarketData[] NOISY_MARKETDATA;
  private static double[] STRIKES;
  private static double[] CLEAN_VOLS;
  private static double[] NOISY_VOLS;
  private static double[] ERRORS;
  private static final SABRFormulaData SABR_DATA = new SABRFormulaData(ALPHA, BETA, RHO, NU);
  private static VolatilityFunctionProvider<SABRFormulaData> SABR = new SABRHaganVolatilityFunction();
  private static ProbabilityDistribution<Double> RANDOM = new NormalDistribution(0, 1, new MersenneTwister(12));
  private static SmileModelFitter<SABRFormulaData> FITTER;
  private static SmileModelFitter<SABRFormulaData> NOISY_FITTER;

  static {
    STRIKES = new double[] {0.005, 0.01, 0.02, 0.03, 0.04, 0.05, 0.07 };
    final int n = STRIKES.length;
    MARKETDATA = new EuropeanOptionMarketData[n];
    NOISY_MARKETDATA = new EuropeanOptionMarketData[n];
    CLEAN_VOLS = new double[n];
    NOISY_VOLS = new double[n];
    ERRORS = new double[n];
    Arrays.fill(ERRORS, 0.0001); // 1bps error

    for (int i = 0; i < n; i++) {
      final EuropeanVanillaOption option = new EuropeanVanillaOption(STRIKES[i], T, true);
      CLEAN_VOLS[i] = SABR.getVolatilityFunction(option, F).evaluate(SABR_DATA);
      NOISY_VOLS[i] = CLEAN_VOLS[i] + RANDOM.nextRandom() * ERRORS[i];
    }

    FITTER = new SABRModelFitter(F, STRIKES, T, CLEAN_VOLS, ERRORS, SABR);
    NOISY_FITTER = new SABRModelFitter(F, STRIKES, T, NOISY_VOLS, ERRORS, SABR);
  }

  @Test
  public void testExactFit() {

    final double[] start = new double[] {0.1, 0.7, 0.0, 0.3 };
    final LeastSquareResultsWithTransform results = FITTER.solve(new DoubleMatrix1D(start));
    final double[] res = results.getModelParameters().getData();
    final double eps = 1e-6;
    assertEquals(ALPHA, res[0], eps);
    assertEquals(BETA, res[1], eps);
    assertEquals(RHO, res[2], eps);
    assertEquals(NU, res[3], eps);
    assertEquals(0.0, results.getChiSq(), eps);
  }

  @Test
  public void testExactFitOddStart() {
    final double[] start = new double[] {0.01, 0.99, 0.9, 0.4 };
    final LeastSquareResultsWithTransform results = FITTER.solve(new DoubleMatrix1D(start));
    final double[] res = results.getModelParameters().getData();
    final double eps = 1e-6;
    assertEquals(ALPHA, res[0], eps);
    assertEquals(BETA, res[1], eps);
    assertEquals(RHO, res[2], eps);
    assertEquals(NU, res[3], eps);
    assertEquals(0.0, results.getChiSq(), eps);
  }

  @Test
  public void testExactFitWithFixedBeta() {
    final double[] start = new double[] {0.1, 0.5, 0.0, 0.3 };
    final BitSet fixed = new BitSet();
    fixed.set(1);
    final LeastSquareResultsWithTransform results = FITTER.solve(new DoubleMatrix1D(start), fixed);
    final double[] res = results.getModelParameters().getData();
    final double eps = 1e-6;
    assertEquals(ALPHA, res[0], eps);
    assertEquals(BETA, res[1], eps);
    assertEquals(RHO, res[2], eps);
    assertEquals(NU, res[3], eps);
    assertEquals(0.0, results.getChiSq(), eps);
  }

  @Test
  public void testFitWithFixedWrongBeta() {

    final double[] start = new double[] {0.1, 0.8, 0.0, 0.3 };
    final BitSet fixed = new BitSet();
    fixed.set(1);
    final LeastSquareResultsWithTransform results = FITTER.solve(new DoubleMatrix1D(start), fixed);
    final double[] res = results.getModelParameters().getData();
    final double eps = 1e-6;
    assertEquals(0.8, res[1], eps);

    final double bpError = 35.0; // 35 bps error
    final int n = MARKETDATA.length;
    final double exChi2 = bpError * bpError * n;
    assertTrue("chi^2 " + results.getChiSq(), results.getChiSq() < exChi2);
  }

  @Test
  public void testNoisyFit() {

    final double[] start = new double[] {0.1, 0.7, 0.0, 0.3 };
    final LeastSquareResultsWithTransform results = NOISY_FITTER.solve(new DoubleMatrix1D(start));
    final double[] res = results.getModelParameters().getData();
    final double eps = 1e-2;
    assertEquals(ALPHA, res[0], eps);
    assertEquals(BETA, res[1], eps);
    assertEquals(RHO, res[2], eps);
    assertEquals(NU, res[3], eps);
    assertTrue(results.getChiSq() < 7);
  }

  // SABRModelFitterTest - 813ms-processing 1000 cycles fitting SABR smile
  // Disable test before commit
  @Test(enabled = false)
  public void fitTimeTest() {
    final int hotspotWarmupCycles = 200;
    final int benchmarkCycles = 1000;
    for (int i = 0; i < hotspotWarmupCycles; i++) {
      testNoisyFit();
    }
    if (benchmarkCycles > 0) {
      final OperationTimer timer = new OperationTimer(_logger, "processing {} cycles fitting SABR smile", benchmarkCycles);
      for (int i = 0; i < benchmarkCycles; i++) {
        testNoisyFit();
      }
      timer.finished();
    }
  }

  // SABRModelFitterTest - 1555ms-processing 1000 cycles fitting SABR smile - old
  @SuppressWarnings("deprecation")
  @Test
  public void testOldMethod() {
    // set to 1 and 0 before commit
    final int hotspotWarmupCycles = 1;
    final int benchmarkCycles = 0;
    final double[] start = new double[] {0.1, 0.7, 0.0, 0.3 };
    final BitSet fixed = new BitSet();
    final SABRNonLinearLeastSquareFitter fitter = new SABRNonLinearLeastSquareFitter(SABR);
    final int n = NOISY_MARKETDATA.length;
    final EuropeanVanillaOption[] options = new EuropeanVanillaOption[n];
    final BlackFunctionData[] data = new BlackFunctionData[n];
    for (int i = 0; i < n; i++) {
      options[i] = new EuropeanVanillaOption(STRIKES[i], T, true);
      data[i] = new BlackFunctionData(F, 1.0, CLEAN_VOLS[i]);
    }
    for (int i = 0; i < hotspotWarmupCycles; i++) {
      doOldTest(options, data, start, fixed, fitter);
    }
    if (benchmarkCycles > 0) {
      final OperationTimer timer = new OperationTimer(_logger, "processing {} cycles fitting SABR smile - old", benchmarkCycles);
      for (int i = 0; i < benchmarkCycles; i++) {
        doOldTest(options, data, start, fixed, fitter);
      }
      timer.finished();
    }

  }

  @SuppressWarnings("deprecation")
  private void doOldTest(final EuropeanVanillaOption[] options, final BlackFunctionData[] data, final double[] start, final BitSet fixed, final SABRNonLinearLeastSquareFitter fitter) {
    final LeastSquareResultsWithTransform lsRes = fitter.getFitResult(options, data, ERRORS, start, fixed);
    final double[] res = lsRes.getModelParameters().getData();
    final double eps = 1e-2;
    assertEquals(ALPHA, res[0], eps);
    assertEquals(BETA, res[1], eps);
    assertEquals(RHO, res[2], eps);
    assertEquals(NU, res[3], eps);
    assertTrue(lsRes.getChiSq() < 7);
  }



}
