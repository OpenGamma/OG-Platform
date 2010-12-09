/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.pricing.analytic.formula;

import com.opengamma.util.CompareUtils;

/**
 * The classic SABR formula from Hagan P et al, Managing Smile Risk 
 */
public class SABRFormula {
  private static double EPS = 1e-15;

  /**
   * The classic SABR formula from Hagan P et al, Managing Smile Risk 
   * @param f The forward value of the underlying 
   * @param alpha The initial value of the stochastic volatility 
   * @param beta The CEV parameter 
   * @param nu The vol-of-vol
   * @param rho The correlation between the driver of the underlying and the driver of the stochastic volatility 
   * @param k The strike
   * @param t the time to maturity
   * @return The (Black) implied volatility 
   */
  public static double impliedVolitility(final double f, final double alpha, final double beta, final double nu, final double rho, final double k, final double t) {

    double vol, z, chi;
    final double beta1 = 1 - beta;
    if (CompareUtils.closeEquals(f, k, EPS)) {
      final double f1 = Math.pow(f, beta1);
      vol = alpha * (1 + t * (beta1 * beta1 * alpha * alpha / 24 / f1 / f1 + rho * alpha * beta * nu / 4 / f1 + nu * nu * (2 - 3 * rho * rho) / 24)) / f1;
    } else {
      if (CompareUtils.closeEquals(beta, 0, EPS)) {
        final double ln = Math.log(f / k);
        z = nu * Math.sqrt(f * k) * ln / alpha;
        chi = getChi(rho, z);
        vol = alpha * ln * getRatio(chi, z) * (1 + t * (alpha * alpha / f / k + nu * nu * (2 - 3 * rho * rho)) / 24) / (f - k);
      } else if (CompareUtils.closeEquals(beta, 1, EPS)) {
        final double ln = Math.log(f / k);
        z = nu * ln / alpha;
        chi = getChi(rho, z);
        vol = alpha * getRatio(chi, z) * (1 + t * (rho * alpha * nu / 4 + nu * nu * (2 - 3 * rho * rho) / 24));
      } else {
        final double ln = Math.log(f / k);
        final double f1 = Math.pow(f * k, beta1);
        final double f1Sqrt = Math.sqrt(f1);
        final double lnBetaSq = Math.pow(beta1 * ln, 2);
        z = nu * f1Sqrt * ln / alpha;
        chi = getChi(rho, z);
        final double first = alpha / (f1Sqrt * (1 + lnBetaSq / 24 + lnBetaSq * lnBetaSq / 1920));
        final double second = getRatio(chi, z);
        final double third = 1 + t * (beta1 * beta1 * alpha * alpha / 24 / f1 + rho * nu * beta * alpha / 4 / f1Sqrt + nu * nu * (2 - 3 * rho * rho) / 24);
        vol = first * second * third;
      }
    }

    return vol;
  }

  private static double getChi(final double rho, final double z) {
    return Math.log((Math.sqrt(1 - 2 * rho * z + z * z) + z - rho) / (1 - rho));
  }

  private static double getRatio(final double chi, final double z) {
    if (CompareUtils.closeEquals(chi, 0, EPS) && CompareUtils.closeEquals(z, 0, EPS)) {
      return 1;
    }
    return z / chi;
  }

}
