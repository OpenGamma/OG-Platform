/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.statistics.distribution;


/**
 * The implementation of the cdf is taken from "Better Approximations to Cumulative Normal Functions", West
 * (<a href="http://www.codeplanet.eu/files/download/accuratecumnorm.pdf">link</a>).
 */
public class BivariateNormalDistributionWest2004 extends BivariateNormalDistribution {

  private static final ProbabilityDistribution<Double> NORMAL = new NormalDistribution(0, 1);
  private static final double[] X = new double[] {0.04691008, 0.23076534, 0.5, 0.76923466, 0.95308992};
  private static final double[] Y = new double[] {0.018854042, 0.038088059, 0.0452707394, 0.038088059, 0.018854042};


  @Override
  protected double getCDF(final double a, double b, final double rho) {
    if (a == Double.POSITIVE_INFINITY || b == Double.POSITIVE_INFINITY) {
      return 1;
    }
    if (a == Double.NEGATIVE_INFINITY || b == Double.NEGATIVE_INFINITY) {
      return 0;
    }
    final double sumSq = (a * a + b * b) / 2.;
    double rho1, rho2, rho3, ab, absDiff, h5, c, d, mult = 0, rho3Sq, eab, e, result;
    if (Math.abs(rho) >= 0.7) {
      rho1 = 1 - rho * rho;
      rho2 = Math.sqrt(rho1);
      if (rho < 0) {
        b *= -1;
      }
      ab = a * b;
      eab = Math.exp(-ab / 2.);
      if (Math.abs(rho) < 1) {
        absDiff = Math.abs(a - b);
        h5 = absDiff * absDiff / 2.;
        absDiff = absDiff / rho2;
        c = 0.5 - ab / 8.;
        d = 3. - 2. * c * h5;
        mult = 0.13298076 * absDiff * d * (1 - NORMAL.getCDF(absDiff)) - Math.exp(-h5 / rho1) * (d + c * rho1) * 0.053051647;
        for (int i = 0; i < 5; i++) {
          rho3 = rho2 * X[i];
          rho3Sq = rho3 * rho3;
          rho1 = Math.sqrt(1 - rho3Sq);
          if (eab == 0) {
            e = 0;
          } else {
            e = Math.exp(-ab / (1 + rho1)) / rho1 / eab;
          }
          mult = mult - Y[i] * Math.exp(-h5 / rho3Sq) * (e - 1 - c * rho3Sq);
        }
      }
      result = mult * rho2 * eab + NORMAL.getCDF(Math.min(a, b));
      if (rho < 0) {
        result = NORMAL.getCDF(a) - result;
      }
      return result;
    }
    ab = a * b;
    if (rho != 0) {
      for (int i = 0; i < 5; i++) {
        rho3 = rho * X[i];
        rho1 = 1 - rho3 * rho3;
        mult = mult + Y[i] * Math.exp((rho3 * ab - sumSq) / rho1) / Math.sqrt(rho1);
      }
    }
    return NORMAL.getCDF(a) * NORMAL.getCDF(b) + rho * mult;
  }

}
