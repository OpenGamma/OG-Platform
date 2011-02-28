/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.pricing.analytic.formula;

import java.util.BitSet;

import org.junit.Test;

import com.opengamma.financial.model.option.DistributionFromImpliedVolatility;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.matrix.DoubleMatrix1D;
import com.opengamma.math.statistics.distribution.ProbabilityDistribution;
import com.opengamma.math.statistics.leastsquare.LeastSquareResults;

/**
 * 
 */
public class SVIPDFTest {

  private static final double A = 0.7;
  private static final double B = -1.0;
  private static final double RHO = 0.4;
  private static final double SIGMA = 0.4;
  private static final double M = 0.04;
  private static final double F = 0.04;
  private static final double T = 2.5;

  private static ProbabilityDistribution<Double> SVI_DIST;

  private final static Function1D<Double, Double> SVI = new Function1D<Double, Double>() {
    final SVIFormula svi = new SVIFormula();

    @Override
    public Double evaluate(final Double k) {
      return svi.impliedVolatility(k, A, B, RHO, SIGMA, M);
    }
  };

  static {
    SVI_DIST = new DistributionFromImpliedVolatility(F, T, SVI);
  }

  @Test
  public void test() {

    for (int i = 0; i < 100; i++) {
      final double k = 0.001 + i * 0.1 / 100;
      final double vol = SVI.evaluate(k);
      final double pdf = SVI_DIST.getPDF(k);
      final double cdf = SVI_DIST.getCDF(k);

      // System.out.println(k + "\t" + vol + "\t" + pdf + "\t" + cdf);
    }

  }

  @Test
  public void testSABR() {
    final double[] strikes = new double[] {0.02, 0.03, 0.035, 0.0375, 0.04, 0.0425, 0.045, 0.05, 0.06};
    final int n = strikes.length;
    final double[] vols = new double[n];
    final double[] errors = new double[n];
    for (int i = 0; i < n; i++) {
      errors[i] = 0.001;
      vols[i] = SVI.evaluate(strikes[i]);
    }

    final double[] initialValues = new double[] {0.04, 1, 0.2, -0.3};
    final BitSet fixed = new BitSet();
    final SABRFormula sabr = new SABRFormulaHagan();

    final SABRFitter fitter = new SABRFitter(sabr);
    final LeastSquareResults result = fitter.solve(F, T, strikes, vols, errors, initialValues, fixed, 0, false);

    final double chiSqr = result.getChiSq();
    final DoubleMatrix1D parms = result.getParameters();

    // TODO real test
    // / System.out.println("\n params, chisq: \t" + parms.getEntry(0) + "\t" + parms.getEntry(1) + "\t" + parms.getEntry(2) + "\t" + parms.getEntry(3) + "\t" + chiSqr + "\n");

    final Function1D<Double, Double> sabrFunction = new Function1D<Double, Double>() {
      @Override
      public Double evaluate(final Double k) {
        return sabr.impliedVolatility(F, parms.getEntry(0), parms.getEntry(1), parms.getEntry(2), parms.getEntry(3), k, T);
      }
    };

    final ProbabilityDistribution<Double> sabrDist = new DistributionFromImpliedVolatility(F, T, sabrFunction);

    for (int i = 0; i < 100; i++) {
      final double k = 0.001 + i * 0.1 / 100;

      final double vol = sabrFunction.evaluate(k);
      final double pdf = sabrDist.getPDF(k);
      final double cdf = sabrDist.getCDF(k);
      // System.out.println(k + "\t" + vol + "\t" + pdf + "\t" + cdf);
    }

  }

}
