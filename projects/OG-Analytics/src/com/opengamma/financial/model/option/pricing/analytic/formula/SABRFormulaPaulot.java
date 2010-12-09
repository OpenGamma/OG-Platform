/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.pricing.analytic.formula;

import static com.opengamma.math.UtilFunctions.square;

import com.opengamma.util.CompareUtils;

/**
 * Expansion from Paulot, Louis, Asymptotic Implied Volatility at the Second Order With Applications to the SABR Model
 */
public class SABRFormulaPaulot {

  private static final double EPS = 1e-15;

  public double impliedVolitility(final double f, final double alpha, final double beta, final double nu, final double rho, final double k, final double t) {

    double sigma0, sigma1;
    final double beta1 = 1 - beta;
    final double a = Math.pow(f, beta1);

    if (CompareUtils.closeEquals(f, k, EPS)) {
      sigma0 = alpha / Math.pow(k, beta1);
      sigma1 = beta1 * beta1 * alpha * alpha / 24 / a / a + rho * alpha * beta * nu / 4 / a + nu * nu * (2 - 3 * rho * rho) / 24;
    } else {
      final double x = Math.log(f / k);

      double q;
      if (CompareUtils.closeEquals(beta, 1.0, EPS)) {
        q = -x;
      } else {
        q = (Math.pow(k, beta1) - Math.pow(f, beta1)) / alpha / beta1;
      }

      final double vMin = Math.sqrt(alpha * alpha + 2 * rho * alpha * nu * q + nu * nu * q * q);
      final double logTerm = Math.log((vMin + rho * alpha + q * nu) / (1 + rho) / alpha);
      sigma0 = -nu * x / logTerm;

      final double cTilde = getCTilde(f, k, alpha, beta, rho, q);
      sigma1 = -square(nu / logTerm) * (cTilde + Math.log(sigma0 / nu * Math.sqrt(k * f)));

    }

    return sigma0 * (1 + sigma1 * t);
  }

  private double getCTilde(final double f, final double k, final double alpha, final double beta, final double rho, final double q) {
    final double rhoStar = Math.sqrt(1 - rho * rho);
    final double beta1 = 1 - beta;
    final double vMin = Math.sqrt(alpha * alpha + 2 * rho * alpha * q + q * q);
    double res = -0.5 * Math.log(alpha * vMin * Math.pow(f * k, beta));
    if (CompareUtils.closeEquals(beta, 1.0, EPS)) {
      res += rho / 2 / rhoStar * (rho * Math.log(k / f) - vMin + alpha);
    } else {
      final double a = Math.pow(f, beta1);
      final double b = beta1 * rhoStar;
      final double c = beta1 * rho;
      final double x1 = -rho * alpha / rhoStar;
      final double x2 = (q - rho * vMin) / rhoStar;
      final double xCap = (x2 * x2 - x1 * x1 + vMin * vMin - alpha * alpha) / 2 / (x2 - x1);
      final double rCap = Math.sqrt(alpha * alpha + square(x1 - xCap));
      final double t1 = Math.sqrt((rCap - x1 + xCap) / (rCap + x1 - xCap));
      final double t2 = Math.sqrt((rCap - x2 + xCap) / (rCap + x2 - xCap));
      res -= rho * beta / beta1 / rhoStar * (getG(a, b, c, xCap, rCap, beta, t2) - getG(a, b, c, xCap, rCap, beta, t1));
    }
    return res;
  }

  private double getG(final double a, final double b, final double c, double xCap, final double rCap, final double beta, final double t) {
    final double beta1 = 1 - beta;
    double res = Math.atan(t);
    double y = square(a + b * xCap) - square((beta1 * rCap));
    if (y > 0) {
      double temp = Math.sqrt(y);
      res -= (a + b * xCap) / temp * Math.atan((c * rCap + t * (a + b * (xCap - rCap))) / temp);
    } else if (y < 0) {
      double temp = Math.sqrt(-y);
      res += (b + b * xCap) / temp * modAtanh((c * rCap + t * (a + b * (xCap - rCap))) / temp);
    } else {
      res += (a + b * xCap) / (c * rCap + t * (a + b * (xCap - rCap)));
    }
    return res;
  }

  private double modAtanh(double z) {
    return 0.5 * Math.log(Math.abs((1 + z) / (1 - z)));
  }
}
