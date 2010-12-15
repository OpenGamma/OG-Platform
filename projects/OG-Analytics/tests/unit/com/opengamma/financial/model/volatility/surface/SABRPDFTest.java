/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.volatility.surface;

import org.junit.Test;

import com.opengamma.financial.model.option.pricing.analytic.formula.BlackFormula;
import com.opengamma.financial.model.option.pricing.analytic.formula.SABRFormula;
import com.opengamma.financial.model.option.pricing.analytic.formula.SABRFormulaBerestycki;
import com.opengamma.financial.model.option.pricing.analytic.formula.SABRFormulaHagan;
import com.opengamma.financial.model.option.pricing.analytic.formula.SABRFormulaHagan2;
import com.opengamma.financial.model.option.pricing.analytic.formula.SABRFormulaJohnson;
import com.opengamma.financial.model.option.pricing.analytic.formula.SABRFormulaPaulot;
import com.opengamma.math.function.Function1D;
import com.opengamma.util.CompareUtils;

/**
 * 
 */
public class SABRPDFTest {

  private static final double F = 4;
  // private static final double ATM_VOL = 0.15;
  private static final double ALPHA = 0.3;
  private static final double BETA = 1.0;
  private static final double NU = 1.0;
  private static final double RHO = -0.5;
  private static final double T = 10;
  // private static final SABRFormula SABR = new SABRFormula();
  // private static final SABRFormulaHagan SABR_HAGAN = new SABRFormulaHagan();
  private static final BlackFormula BLACK = new BlackFormula();

  private final Function1D<Double, Double> SABR = new Function1D<Double, Double>() {
    final SABRFormula sabr = new SABRFormulaHagan();

    @SuppressWarnings("synthetic-access")
    @Override
    public Double evaluate(Double k) {
      return sabr.impliedVolitility(F, ALPHA, BETA, NU, RHO, k, T);
    }
  };

  private final Function1D<Double, Double> SABR_HAGAN = new Function1D<Double, Double>() {
    final SABRFormula sabr = new SABRFormulaHagan2();

    @SuppressWarnings("synthetic-access")
    @Override
    public Double evaluate(Double k) {
      return sabr.impliedVolitility(F, ALPHA, BETA, NU, RHO, k, T);
    }
  };

  private final Function1D<Double, Double> SABR_BERESTYCKI = new Function1D<Double, Double>() {
    final SABRFormula sabr = new SABRFormulaBerestycki();

    @SuppressWarnings("synthetic-access")
    @Override
    public Double evaluate(Double k) {
      return sabr.impliedVolitility(F, ALPHA, BETA, NU, RHO, k, T);
    }
  };

  private final Function1D<Double, Double> SABR_PAULOT = new Function1D<Double, Double>() {
    final SABRFormula sabr = new SABRFormulaPaulot();

    @SuppressWarnings("synthetic-access")
    @Override
    public Double evaluate(Double k) {
      return sabr.impliedVolitility(F, ALPHA, BETA, NU, RHO, k, T);
    }
  };

  private final Function1D<Double, Double> SABR_JOHNSON = new Function1D<Double, Double>() {
    final SABRFormula sabr = new SABRFormulaJohnson();

    @SuppressWarnings("synthetic-access")
    @Override
    public Double evaluate(Double k) {
      return sabr.impliedVolitility(F, ALPHA, BETA, NU, RHO, k, T);
    }
  };

  static {
    // ALPHA = ATM_VOL * Math.pow(F, 1 - BETA);
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
    double step = 10.0 / (n);
    System.out.println("Strike \t SABR Vol \t Hagan Vol \t Berestycki vol \t Paulot vol \t Johnson vol \t SABR PDF \t Hagan PDF \t Berestycki PDF \t Paulot PDF \t Johnson");
    for (int i = 0; i < n; i++) {
      // double z = (i - 3 * n) * step;
      // double k = F * Math.exp(sigmaRootT * z) * 1.2;
      double k = (i + 1) * step;
      strike[i] = k;
      impliedVol[i] = SABR.evaluate(k);
      impliedVol2[i] = SABR_HAGAN.evaluate(k);
      impliedVol3[i] = SABR_BERESTYCKI.evaluate(k);
      impliedVol4[i] = SABR_PAULOT.evaluate(k);
      impliedVol5[i] = SABR_JOHNSON.evaluate(k);
      // price[i] = BLACK.callPrice(F, k, 1.0, impliedVol[i], T);
      pdf1[i] = pdf(k, SABR);
      pdf2[i] = pdf(k, SABR_HAGAN);
      pdf3[i] = pdf(k, SABR_BERESTYCKI);
      pdf4[i] = pdf(k, SABR_PAULOT);
      pdf5[i] = pdf(k, SABR_JOHNSON);
      System.out.println(strike[i] + "\t" + impliedVol[i] + "\t" + impliedVol2[i] + "\t" + impliedVol3[i] + "\t" + impliedVol4[i] + "\t" + impliedVol5[i] + "\t" + pdf1[i] + "\t" + pdf2[i] + "\t"
          + pdf3[i] + "\t" + pdf4[i] + "\t" + pdf5[i]);
    }
  }

  // private double pdf(final double k) {
  // double eps = 1e-3;
  // double impliedVol = SABR.impliedVolitility(F, ALPHA, BETA, NU, RHO, k, T);
  // double price = BLACK.callPrice(F, k, 1.0, impliedVol, T);
  // impliedVol = SABR.impliedVolitility(F, ALPHA, BETA, NU, RHO, k + eps, T);
  // double priceUp = BLACK.callPrice(F, k + eps, 1.0, impliedVol, T);
  // impliedVol = SABR.impliedVolitility(F, ALPHA, BETA, NU, RHO, k - eps, T);
  // double priceDown = BLACK.callPrice(F, k - eps, 1.0, impliedVol, T);
  //
  // return (priceUp + priceDown - 2 * price) / eps / eps;
  // }

  private double pdf(final double k, Function1D<Double, Double> sabrFunction) {

    double sigma = sabrFunction.evaluate(k);
    double rootT = Math.sqrt(T);
    double d1 = getD1(F, k, sigma * rootT);
    double sigmaPrime = sigmaPrime(k, sabrFunction);
    double sigmaDoublePrime = sigmaDoublePrime(k, sabrFunction);
    double d1Prime = getD1Prime(F, k, sigma, T, sigmaPrime);
    double d2Prime = d1Prime - rootT * sigmaPrime;
    double nPrimed1 = Math.exp(-d1 * d1 / 2) / Math.sqrt(2 * Math.PI);
    return -F * nPrimed1 * (rootT * (d1 * d1Prime * sigmaPrime - sigmaDoublePrime) + d2Prime / k);
  }

  private double sigmaPrime(final double k, Function1D<Double, Double> sabrFunction) {
    double eps = 1e-3;
    double impliedVolUp = sabrFunction.evaluate(k + eps);
    double impliedVolDown = sabrFunction.evaluate(k - eps);
    return (impliedVolUp - impliedVolDown) / 2 / eps;
  }

  private double sigmaDoublePrime(final double k, Function1D<Double, Double> sabrFunction) {
    double eps = 1e-3;
    double impliedVol = sabrFunction.evaluate(k);
    double impliedVolUp = sabrFunction.evaluate(k + eps);
    double impliedVolDown = sabrFunction.evaluate(k - eps);
    return (impliedVolUp + impliedVolDown - 2 * impliedVol) / eps / eps;
  }

  private double getD1(final double f, final double k, final double simgaRootT) {
    final double numerator = (Math.log(f / k) + simgaRootT * simgaRootT / 2);
    if (CompareUtils.closeEquals(numerator, 0, 1e-16)) {
      return 0;
    }
    return numerator / simgaRootT;
  }

  private double getD1Prime(final double f, final double k, final double sigma, final double t, final double sigmaPrime) {
    double rootT = Math.sqrt(t);
    double res = -1 / k / sigma / rootT - (Math.log(f / k) / sigma / sigma / rootT - 0.5 * rootT) * sigmaPrime;
    return res;
  }

}
