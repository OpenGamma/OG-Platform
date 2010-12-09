/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.pricing.analytic.formula;

import org.apache.commons.lang.Validate;

import com.opengamma.math.statistics.distribution.ChiSquareDistribution;
import com.opengamma.math.statistics.distribution.NonCentralChiSquareDistribution;

/**
 * 
 */
public class CEVFormula {

  public static double optionPrice(final double f, final double k, final double beta, final double discountFactor, final double sigma, final double t, boolean isCall) {

    Validate.isTrue(beta >= 0.0, "beta less than zero not supported");

    if (beta == 1.0) {
      return BlackFormula.optionPrice(f, k, discountFactor, sigma, t, isCall);
    }
    if (beta == 0.0) {
      return NormalFormula.optionPrice(f, k, discountFactor, sigma, t, isCall);
    }

    final double b = 1.0 / (1 - beta);
    final double x = b * b / sigma / sigma / t;
    final double a = Math.pow(k, 2 * (1 - beta)) * x;
    final double c = Math.pow(f, 2 * (1 - beta)) * x;

    if (beta < 1) {
      // debug
      // return discountFactor * (f * (1 - nonCenteredChiSquare(a, b + 2, c)) - k * nonCenteredChiSquare(c, b, a));
      // return discountFactor * (f * nonCenteredChiSquare(a, b + 2, c) - k * (1 - nonCenteredChiSquare(c, b, a)));

      NonCentralChiSquareDistribution chiSq1 = new NonCentralChiSquareDistribution(b + 2, c);
      NonCentralChiSquareDistribution chiSq2 = new NonCentralChiSquareDistribution(b, a);
      if (isCall) {
        return discountFactor * (f * (1 - chiSq1.getCDF(a)) - k * chiSq2.getCDF(c));
      }
      return discountFactor * (k * (1 - chiSq2.getCDF(c)) - f * chiSq1.getCDF(a));
    } else {
      NonCentralChiSquareDistribution chiSq1 = new NonCentralChiSquareDistribution(-b, a);
      NonCentralChiSquareDistribution chiSq2 = new NonCentralChiSquareDistribution(2 - b, c);
      if (isCall) {
        return discountFactor * (f * (1 - chiSq1.getCDF(c)) - k * chiSq2.getCDF(a));
      }
      return discountFactor * (k * (1 - chiSq2.getCDF(a)) - f * chiSq1.getCDF(c));
    }
  }

  private static double nonCenteredChiSquare(final double z, final double k, final double lambda) {
    double sum = 0.0;
    double fact = 1;
    for (int i = 0; i < 100; i++) {
      ChiSquareDistribution chiSq = new ChiSquareDistribution(k + 2 * i);

      sum += Math.pow(lambda / 2.0, i) / fact * chiSq.getCDF(z);
      fact *= (i + 1);
    }
    return Math.exp(-lambda / 2) * sum;
  }

}
