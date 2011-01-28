/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.pricing.analytic.formula;

import org.junit.Test;

import com.opengamma.financial.model.option.DistributionFromImpliedVolatility;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.statistics.distribution.ProbabilityDistribution;

/**
 * 
 */
public class SABRPDFTest {

  private static final double F = 5;
  // private static final double ATM_VOL = 0.15;
  private static final double ALPHA;// = 0.3;
  private static final double BETA = 0.99;
  private static final double NU = 0.4;
  private static final double RHO = -0.5;
  private static final double T = 25;
  private static final double ATM_VOL = 0.3;
  private static ProbabilityDistribution<Double> SABR_DIST;
  private static ProbabilityDistribution<Double> HAGAN_DIST;
  private static ProbabilityDistribution<Double> BERESTYCKI_DIST;
  private static ProbabilityDistribution<Double> PAULOT_DIST;
  private static ProbabilityDistribution<Double> JOHNSON_DIST;

  private final static Function1D<Double, Double> SABR = new Function1D<Double, Double>() {
    final SABRFormula sabr = new SABRFormulaHagan();

    @SuppressWarnings("synthetic-access")
    @Override
    public Double evaluate(Double k) {
      return sabr.impliedVolatility(F, ALPHA, BETA, NU, RHO, k, T);
    }
  };

  private final static Function1D<Double, Double> SABR_HAGAN = new Function1D<Double, Double>() {
    final SABRFormula sabr = new SABRFormulaHagan2();

    @SuppressWarnings("synthetic-access")
    @Override
    public Double evaluate(Double k) {
      return sabr.impliedVolatility(F, ALPHA, BETA, NU, RHO, k, T);
    }
  };

  private final static Function1D<Double, Double> SABR_BERESTYCKI = new Function1D<Double, Double>() {
    final SABRFormula sabr = new SABRFormulaBerestycki();

    @SuppressWarnings("synthetic-access")
    @Override
    public Double evaluate(Double k) {
      return sabr.impliedVolatility(F, ALPHA, BETA, NU, RHO, k, T);
    }
  };

  private final static Function1D<Double, Double> SABR_PAULOT = new Function1D<Double, Double>() {
    final SABRFormula sabr = new SABRFormulaPaulot();

    @SuppressWarnings("synthetic-access")
    @Override
    public Double evaluate(Double k) {
      return sabr.impliedVolatility(F, ALPHA, BETA, NU, RHO, k, T);
    }
  };

  private final static Function1D<Double, Double> SABR_JOHNSON = new Function1D<Double, Double>() {
    final SABRFormula sabr = new SABRFormulaJohnson();

    @SuppressWarnings("synthetic-access")
    @Override
    public Double evaluate(Double k) {
      return sabr.impliedVolatility(F, ALPHA, BETA, NU, RHO, k, T);
    }
  };

  static {
    ALPHA = ATM_VOL * Math.pow(F, 1 - BETA);
    SABR_DIST = new DistributionFromImpliedVolatility(F, T, SABR);
    HAGAN_DIST = new DistributionFromImpliedVolatility(F, T, SABR_HAGAN);
    BERESTYCKI_DIST = new DistributionFromImpliedVolatility(F, T, SABR_BERESTYCKI);
    PAULOT_DIST = new DistributionFromImpliedVolatility(F, T, SABR_PAULOT);
    JOHNSON_DIST = new DistributionFromImpliedVolatility(F, T, SABR_JOHNSON);
  }

  @Test
  public void test() {
    int n = 800;
    double[] strike = new double[n];
    double[] impliedVol = new double[n];
    double[] impliedVol2 = new double[n];
    double[] impliedVol3 = new double[n];
    double[] impliedVol4 = new double[n];
    double[] impliedVol5 = new double[n];
    double[] price = new double[n];
    double[] pdf1 = new double[n];
    double[] pdf2 = new double[n];
    double[] pdf3 = new double[n];
    double[] pdf4 = new double[n];
    double[] pdf5 = new double[n];
    // double sigmaRootT = ATM_VOL * Math.sqrt(T);
    // double sigmaRootT = ALPHA * Math.sqrt(T);
    double step = 20.0 / (n);
    // System.out.println("Strike \t SABR Vol \t Hagan Vol \t Berestycki vol \t Paulot vol \t Johnson vol \t SABR PDF \t Hagan PDF \t Berestycki PDF \t Paulot PDF \t Johnson");
    for (int i = 0; i < n; i++) {
      // double z = (i - 3 * n) * step;
      // double k = F * Math.exp(sigmaRootT * z) * 1.2;
      double k = 0.0 + (i + 1) * step;
      strike[i] = k;
      impliedVol[i] = SABR.evaluate(k);
      impliedVol2[i] = SABR_HAGAN.evaluate(k);
      impliedVol3[i] = SABR_BERESTYCKI.evaluate(k);
      impliedVol4[i] = SABR_PAULOT.evaluate(k);
      impliedVol5[i] = SABR_JOHNSON.evaluate(k);
      // price[i] = BLACK.callPrice(F, k, 1.0, impliedVol[i], T);
      pdf1[i] = SABR_DIST.getPDF(k);
      pdf2[i] = HAGAN_DIST.getPDF(k);
      pdf3[i] = BERESTYCKI_DIST.getPDF(k);
      pdf4[i] = PAULOT_DIST.getPDF(k);
      pdf5[i] = JOHNSON_DIST.getPDF(k);
      // System.out.println(strike[i] + "\t" + impliedVol[i] + "\t" + impliedVol2[i] + "\t" + impliedVol3[i] + "\t" + impliedVol4[i] + "\t" + impliedVol5[i] + "\t" + pdf1[i] + "\t" + pdf2[i] + "\t"
      // + pdf3[i] + "\t" + pdf4[i] + "\t" + pdf5[i]);
    }
  }

}
