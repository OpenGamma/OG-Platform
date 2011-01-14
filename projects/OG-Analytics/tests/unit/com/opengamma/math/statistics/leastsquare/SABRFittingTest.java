/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.statistics.leastsquare;

import static org.junit.Assert.assertEquals;

import org.apache.activemq.util.BitArray;
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
    int n = STRIKES.length;
    VOLS = new double[n];
    NOISY_VOLS = new double[n];
    ERRORS = new double[n];

    for (int i = 0; i < n; i++) {

      VOLS[i] = SABR.impliedVolitility(F, ALPHA, BETA, NU, RHO, STRIKES[i], T);
      ERRORS[i] = 0.01;
      NOISY_VOLS[i] = VOLS[i] + ERRORS[i] * RANDOM.nextRandom();
    }
  }

  @Test
  public void TestExactFit() {
    BitArray fixed = new BitArray();

    double[] start = new double[] {0.03, 0.4, 0.1, 0.2};

    SABRFitter fitter = new SABRFitter(SABR);
    LeastSquareResults results = fitter.solve(F, T, STRIKES, VOLS, ERRORS, start, fixed);
    double[] res = results.getParameters().getData();
    DoubleMatrix2D covar = results.getCovariance();

    assertEquals(ALPHA, res[0], 1e-7);
    assertEquals(BETA, res[1], 1e-7);
    assertEquals(NU, res[2], 1e-7);
    assertEquals(RHO, res[3], 1e-7);
    assertEquals(0.0, results.getChiSq(), 1e-7);
  }

  @Test
  public void TestNoisyFit() {

    BitArray fixed = new BitArray();
    // fixed.set(1, true);
    double[] start = new double[] {0.03, 0.5, 0.1, 0.2};
    SABRFitter fitter = new SABRFitter(SABR);
    LeastSquareResults results = fitter.solve(F, T, STRIKES, NOISY_VOLS, ERRORS, start, fixed);
    // assertTrue(results.getChiSq() < 3.0);
    DoubleMatrix2D covar = results.getCovariance();
    double sigmaLNRootT = ALPHA * Math.pow(F, BETA - 1) * Math.sqrt(T);

    for (int i = 0; i < STRIKES.length; i++) {
      System.out.print(STRIKES[i] + "\t" + VOLS[i] + "\t" + NOISY_VOLS[i] + "\n");
    }

    double[] res = results.getParameters().getData();

    double k, m;
    for (int i = 0; i < 100; i++) {
      m = -3 + i * 4.5 / 100;
      k = F * Math.exp(m * sigmaLNRootT);
      System.out.print(k + "\t" + SABR.impliedVolitility(F, ALPHA, BETA, NU, RHO, k, T) + "\t" + SABR.impliedVolitility(F, res[0], res[1], res[2], res[3], k, T) + "\n");
    }

    System.out.print("\n" + res[0] + "\t" + res[1] + "\t" + res[2] + "\t" + res[3] + "\n");
    System.out.println(results.getChiSq());

    // assertEquals(ALPHA, res[0], 1e-7);
    // assertEquals(BETA, res[1], 1e-7);
    // assertEquals(NU, res[2], 1e-7);
    // assertEquals(RHO, res[3], 1e-7);
  }
}
