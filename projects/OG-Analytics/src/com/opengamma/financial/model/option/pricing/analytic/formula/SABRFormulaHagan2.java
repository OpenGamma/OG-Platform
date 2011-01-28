/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.pricing.analytic.formula;

import com.opengamma.util.CompareUtils;

/**
 * This is the form given in Obloj, Fine-Tune Your Simle (2008), and supposedly corresponds to that given in Hagan, Managing Smile Risk (2002). However it differs from Hagan
 * @see SABRFormulaBerestycki   
 */
public class SABRFormulaHagan2 implements SABRFormula {
  private static final double EPS = 1e-15;

  public double impliedVolatility(final double f, final double alpha, final double beta, final double nu, final double rho, final double k, final double t) {

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

        double z, zeta;
        if (beta == 1.0) {
          z = nu * x / alpha;
          zeta = z;
        } else {
          z = nu * (Math.pow(f, beta1) - Math.pow(k, beta1)) / alpha / beta1;
          zeta = nu * (f - k) / alpha / Math.pow(f * k, beta / 2);
        }
        double temp = (Math.sqrt(1 + zeta * (zeta - 2 * rho)) + zeta - rho) / (1 - rho);
        i0 = nu * x * zeta / z / Math.log(temp);

      }
    }

    final double f1sqrt = Math.pow(f * k, beta1 / 2);
    double i1 = beta1 * beta1 * alpha * alpha / 24 / f1sqrt / f1sqrt + rho * alpha * beta * nu / 4 / f1sqrt + nu * nu * (2 - 3 * rho * rho) / 24;

    return i0 * (1 + i1 * t);
  }

}
