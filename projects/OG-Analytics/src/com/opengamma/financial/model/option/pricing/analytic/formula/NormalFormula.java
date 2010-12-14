/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.pricing.analytic.formula;

import com.opengamma.math.statistics.distribution.NormalDistribution;
import com.opengamma.math.statistics.distribution.ProbabilityDistribution;

/**
 * Bachelier pricing formula. European option price when the forward value follows a Brownian dynamic df = sigma*f^beta dw 
 */
public class NormalFormula {

  private static final ProbabilityDistribution<Double> NORMAL = new NormalDistribution(0, 1);

  public static double optionPrice(final double f, final double k, final double discountFactor, final double sigma, final double t, boolean isCall) {

    final double sigmaRootT = sigma * Math.sqrt(t);

    final int sign = isCall ? 1 : -1;
    double arg = sign * (f - k) / sigmaRootT;
    return discountFactor * (sign * (f - k) * NORMAL.getCDF(arg) + sigmaRootT * NORMAL.getPDF(arg));
  }
}
