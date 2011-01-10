/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.pricing.analytic.formula;

import com.opengamma.util.CompareUtils;

/**
 *  This is the form given in Obloj, Fine-Tune Your Simle (2008), and supposedly corresponds to that given in Berestycki, 
 *  Computing the implied volatility in stochastic volatility models (2004). However appears to be the same as Hagan's 
 */
public class SABRFormulaBerestycki implements SABRFormula {
  private static final double EPS = 1e-15;

  public double impliedVolitility(final double f, final double alpha, final double beta, final double nu, final double rho, final double k, final double t) {

    double i0;
    final double beta1 = 1 - beta;

    if (CompareUtils.closeEquals(f, k, EPS)) {
      i0 = alpha / Math.pow(k, beta1);
    } else {

      final double x = Math.log(f / k);

      if (CompareUtils.closeEquals(nu, 0, EPS)) {
        if (CompareUtils.closeEquals(beta, 1.0, EPS)) {
          return alpha; // this is just log-normal
        }
        i0 = x * alpha * beta1 / (Math.pow(f, beta1) - Math.pow(k, beta1));
      } else {

        double z;
        if (beta == 1.0) {
          z = nu * x / alpha;

        } else {
          z = nu * (Math.pow(f, beta1) - Math.pow(k, beta1)) / alpha / beta1;
        }
        double temp = (Math.sqrt(1 + z * (z - 2 * rho)) + z - rho) / (1 - rho);
        i0 = nu * x / Math.log(temp);

      }
    }

    final double f1sqrt = Math.pow(f * k, beta1 / 2);
    double i1 = beta1 * beta1 * alpha * alpha / 24 / f1sqrt / f1sqrt + rho * alpha * beta * nu / 4 / f1sqrt + nu * nu * (2 - 3 * rho * rho) / 24;

    return i0 * (1 + i1 * t);
  }

}
