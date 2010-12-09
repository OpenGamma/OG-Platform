/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.pricing.analytic.formula;

import com.opengamma.util.CompareUtils;

/**
 * 
 */
public class SABRFormulaHagan {
  private static final double EPS = 1e-15;

  public double impliedVolitility(final double f, final double alpha, final double beta, final double nu, final double rho, final double k, final double t) {

    double i0;
    final double beta1 = 1 - beta;
    if (CompareUtils.closeEquals(f, k, EPS)) {
      i0 = alpha / Math.pow(k, beta1);
    } else {
      final double x = Math.log(f / k);
      final double z = nu * (Math.pow(f, beta1) - Math.pow(k, beta1)) / alpha / beta1;
      final double zeta = nu * (f - k) / alpha / Math.pow(f * k, beta / 2);
      double temp = (Math.sqrt(1 + zeta * (zeta - 2 * rho)) + zeta - rho) / (1 - rho);
      i0 = nu * x * zeta / z / Math.log(temp);
    }
    final double f1sqrt = Math.pow(f * k, beta1 / 2);
    double i1 = beta1 * beta1 * alpha * alpha / 24 / f1sqrt / f1sqrt + rho * alpha * beta * nu / 4 / f1sqrt + nu * nu * (2 - 3 * rho * rho) / 24;

    return i0 * (1 + i1 * t);
  }

}
