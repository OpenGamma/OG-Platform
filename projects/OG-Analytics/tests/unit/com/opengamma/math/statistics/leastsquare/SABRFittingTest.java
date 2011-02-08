/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.statistics.leastsquare;

import static org.junit.Assert.assertEquals;

import java.util.BitSet;

import org.junit.Test;

import com.opengamma.financial.model.option.pricing.analytic.formula.SABRFitter;
import com.opengamma.financial.model.option.pricing.analytic.formula.SABRFormula;
import com.opengamma.financial.model.option.pricing.analytic.formula.SABRFormulaHagan;
import com.opengamma.math.matrix.DoubleMatrix2D;
import com.opengamma.math.statistics.distribution.NormalDistribution;
import com.opengamma.math.statistics.distribution.ProbabilityDistribution;

/**
 * 
 */
public class SABRFittingTest {

  private static final double F = 0.03;
  private static final double T = 7.0;
  private static final double ALPHA = 0.05;
  private static final double BETA = 0.5;
  private static double NU = 0.2;
  private static double RHO = -0.3;

  private static double[] STRIKES;
  private static double[] VOLS;
  private static double[] NOISY_VOLS;
  private static double[] ERRORS;
  private static SABRFormula SABR = new SABRFormulaHagan();
  private static ProbabilityDistribution<Double> RANDOM = new NormalDistribution(0, 1, new cern.jet.random.engine.MersenneTwister(12));

  static {
    STRIKES = new double[] {0.005, 0.01, 0.02, 0.03, 0.04, 0.05, 0.07};
    final int n = STRIKES.length;
    VOLS = new double[n];
    NOISY_VOLS = new double[n];
    ERRORS = new double[n];

    for (int i = 0; i < n; i++) {

      VOLS[i] = SABR.impliedVolatility(F, ALPHA, BETA, NU, RHO, STRIKES[i], T);
      ERRORS[i] = 0.01;
      NOISY_VOLS[i] = VOLS[i] + ERRORS[i] * RANDOM.nextRandom();
    }
  }

  @Test
  public void testExactFit() {
    final BitSet fixed = new BitSet();

    final double[] start = new double[] {0.03, 0.4, 0.1, 0.2};

    final SABRFitter fitter = new SABRFitter(SABR);
    final LeastSquareResults results = fitter.solve(F, T, STRIKES, VOLS, ERRORS, start, fixed, 0, false);
    final double[] res = results.getParameters().getData();
    final DoubleMatrix2D covar = results.getCovariance();

    assertEquals(ALPHA, res[0], 1e-7);
    assertEquals(BETA, res[1], 1e-7);
    assertEquals(NU, res[2], 1e-7);
    assertEquals(RHO, res[3], 1e-7);
    assertEquals(0.0, results.getChiSq(), 1e-7);
  }

  @Test
  public void testNoisyFit() {

    final BitSet fixed = new BitSet();
    // fixed.set(1, true);
    final double[] start = new double[] {0.03, 0.5, 0.1, 0.2};
    final SABRFitter fitter = new SABRFitter(SABR);
    final LeastSquareResults results = fitter.solve(F, T, STRIKES, NOISY_VOLS, ERRORS, start, fixed, 0, false);
    // assertTrue(results.getChiSq() < 3.0);
    final DoubleMatrix2D covar = results.getCovariance();
    final double sigmaLNRootT = ALPHA * Math.pow(F, BETA - 1) * Math.sqrt(T);

    // for (int i = 0; i < STRIKES.length; i++) {
    // System.out.print(STRIKES[i] + "\t" + VOLS[i] + "\t" + NOISY_VOLS[i] + "\n");
    // }

    final double[] res = results.getParameters().getData();

    double k, m;
    for (int i = 0; i < 100; i++) {
      m = -3 + i * 4.5 / 100;
      k = F * Math.exp(m * sigmaLNRootT);
      // System.out.print(k + "\t" + SABR.impliedVolatility(F, ALPHA, BETA, NU, RHO, k, T) + "\t" + SABR.impliedVolatility(F, res[0], res[1], res[2], res[3], k, T) + "\n");
    }

    // System.out.print("\n" + res[0] + "\t" + res[1] + "\t" + res[2] + "\t" + res[3] + "\n");
    // System.out.println(results.getChiSq());

    // assertEquals(ALPHA, res[0], 1e-7);
    // assertEquals(BETA, res[1], 1e-7);
    // assertEquals(NU, res[2], 1e-7);
    // assertEquals(RHO, res[3], 1e-7);
  }

  @Test
  public void sanityCheck() {

    final double alpha = 0.022218760837654682;
    final double beta = 0.2;// 0.991886529;
    final double nu = 0.20960993229450917;// 1.353913643;
    final double rho = 0.999999;
    final double t = 1.0;

    final double f = 0.039757;

    final double sigmaLNRootT = alpha * Math.pow(f, beta - 1) * Math.sqrt(t);
    double k, m;
    for (int i = 0; i < 100; i++) {
      m = -3 + i * 4.5 / 100;
      k = f * Math.exp(m * sigmaLNRootT);
      // System.out.print(k + "\t" + SABR.impliedVolatility(f, alpha, beta, nu, rho, k, t) + "\n");
    }
  }

  @Test
  public void sanityCheck2() {
    final double volATM = 1.5;
    final double f = 0.046744;
    final double beta = 0.5;// 0.991886529;
    final double alpha = volATM * Math.pow(f, 1 - beta);
    final double nu = 2.0;// 1.353913643;
    // double rho = -0.9;
    final double t = 2.0;

    final double rho0 = SABR.impliedVolatility(f, alpha, beta, nu, 0.0, f, t);
    for (int i = 0; i < 100; i++) {
      final double rho = -1 + i / 49.5;
      // System.out.print(rho + "\t" + SABR.impliedVolatility(f, alpha, beta, nu, rho, f, t) / rho0 + "\n");
    }
  }
}
